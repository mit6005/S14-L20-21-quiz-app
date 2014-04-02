package multiplayerquiz.server;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import multiplayerquiz.common.model.BoardState;
import multiplayerquiz.common.model.Question;
import multiplayerquiz.common.model.UserState;



/**
 * The server state.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class ServerState {
    // constants
    private static final int INIT_POINTS_PER_QUESTION = 1000;
    private static final int USER_INITIAL_SCORE = 100;
    private static final long RESERVATION_EXPIRY_MILLIS = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

    private final List<String> categories;
    private final List<List<Question>> questions;
    private final List<List<Integer>> answerKey;
    private final Map<Question, List<PointReservation>> reservations = new HashMap<Question, List<PointReservation>>();
    private final Map<UUID, UserState> userStates = new HashMap<UUID, UserState>();
    private BoardState boardState;
    private final BoardStateUpdateThread updateThread;
    
    /** 
     * Concurrency argument
     * Multiple client threads will be accessing the same ServerState
     * However, all of them will be accessing using synchronized, thus
     * only one client can be reading/modifying the board state at a time.
     * Or will be reading immutable data
     */

    public ServerState(String questionsFile, BoardStateUpdateThread updateThread) throws IOException {
        List<String> lines = new ArrayList<String>();
        FileReader file = new FileReader(questionsFile);
        BufferedReader reader = new BufferedReader(file);
        String cl = reader.readLine();
        while(cl != null) {
            lines.add(cl);
            cl = reader.readLine();
        }
        reader.close();

        //One line containing the number of categories and questions per category.
        String[] fragments = lines.remove(0).split(" ");
        int numCategories = Integer.parseInt(fragments[0]);
        int questionsPerCategory = Integer.parseInt(fragments[1]);

        //Category names, one per line.
        this.categories = Collections.unmodifiableList(new ArrayList<String>(lines.subList(0, numCategories)));
        lines.subList(0, numCategories).clear();

        //Questions: question, numAnswers rightAnswerIndex, choices... (one per line)
        List<List<Question>> questions = new ArrayList<List<Question>>(categories.size());
        List<List<Integer>> answerKey = new ArrayList<List<Integer>>(categories.size());
        for (int c = 0; c < categories.size(); ++c) {
            List<Question> list = new ArrayList<Question>(questionsPerCategory);
            List<Integer> rightAnswers = new ArrayList<Integer>(questionsPerCategory);

            for (int i = 0; i < questionsPerCategory; ++i) {
                fragments = lines.remove(0).split(" ");
                int questionlines = Integer.parseInt(fragments[0]);
                int numAnswers = Integer.parseInt(fragments[1]);
                int rightAnswer = Integer.parseInt(fragments[2]);
                rightAnswers.add(rightAnswer);
                List<String> ql = lines.subList(0, questionlines);
                String ques = "";
                for (String l : ql)
                    ques += l + "\n";
                lines.subList(0, questionlines).clear();
                List<String> choices = new ArrayList<String>(lines.subList(0, numAnswers));
                lines.subList(0, numAnswers).clear();
                list.add(new Question(ques, choices));
                //Question q = new Question(ques, choices);
                //System.out.println("\nCategory: " + categories.get(c).toString() + " question:" + i);
                //System.out.println(q.toProtocolString());
            }

            questions.add(Collections.unmodifiableList(list));
            answerKey.add(Collections.unmodifiableList(rightAnswers));
        }
        this.questions = Collections.unmodifiableList(questions);
        this.answerKey = Collections.unmodifiableList(answerKey);
        this.boardState = new BoardState(INIT_POINTS_PER_QUESTION, categories.size(), questionsPerCategory);
        for (List<Question> l : questions)
            for (Question q : l)
                reservations.put(q, new ArrayList<PointReservation>());
        this.updateThread = updateThread;
   
        printUserTotals();
    }

    /**
     * 
     * @return
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * 
     * @param category
     * @return
     */
    public List<Question> getQuestions(int category) {
        return questions.get(category);
    }

    /**
     * Return the current user state
     * @param userId
     * @return
     */
    public synchronized UserState getUserState(UUID userId) {
        UserState state = userStates.get(userId);
        if (state == null) {
            userStates.put(userId, (state = new UserState(USER_INITIAL_SCORE, getCategories().size(), getQuestions(0).size())));
            printUserTotals();
        }
        return state;
    }

    /**
     * 
     * @return
     */
    public synchronized BoardState getBoardState() {
        return boardState;
    }
    

    /**
     * If the board and the question has sufficient points, create a reservation
     * and take that many points out of the question and put it in the reservation.
     * If question don't have sufficient funding, see if funding can be reclaimed
     * from expired reservations
     * @param userId
     * @param category 0 <= category < numCategories
     * @param question 0 <= question < numQuestions
     * @param points  points > 0
     * @return
     * @modifies reservations, boardState 
     */
    public synchronized PointReservation tryBid(UUID userId, int category, int question, int points) {
        if (points > boardState.getAvailablePoints(category, question)) {
            expireReservations(category, question);
            if (points > boardState.getAvailablePoints(category, question))
                return null;
        }
        Question q = getQuestions(category).get(question);
        PointReservation r = new PointReservation(userId, points, new Date().getTime()+RESERVATION_EXPIRY_MILLIS);
        reservations.get(q).add(r);
        boardState = boardState.withUpdate(category, question, -points);
        updateThread.update(boardState);
        return r;
    }
    
    /**
     * If the answer is correct and the reservation is there, give the points to the user
     * If the answer is correct by the reservation has timed out, take the points from the user
     *    and put it back in the board
     * If the answer is incorrect, take the points from the user and put it back in the board
     * @param r valid reservation
     * @param category 0 <= category < numCategories
     * @param question 0 <= question < numQuestions
     * @return "correct", "incorrect" or "timed out"
     * @modifies userStates, reservations, boardState
     */
    public synchronized String redeemReservation(PointReservation r, int category, int question, int answer) {
        Question q = getQuestions(category).get(question);
        List<PointReservation> resers = reservations.get(q);
        if (resers.contains(r) && answerKey.get(category).get(question) == answer) {
            // correct
            userStates.put(r.user, userStates.get(r.user).withUpdate(category, question, r.points));
            resers.remove(r);
            printUserTotals();
            return "correct";
        } else if(!resers.contains(r)) {
            //expired 
            userStates.put(r.user, userStates.get(r.user).withUpdate(category, question, -r.points));
            boardState = boardState.withUpdate(category, question, r.points);
            updateThread.update(boardState);
            printUserTotals();
            return "timed out";
        } else {
            // wrong
            userStates.put(r.user, userStates.get(r.user).withUpdate(category, question, -r.points));
            boardState = boardState.withUpdate(category, question, 2*r.points);
            updateThread.update(boardState);
            resers.remove(r);
            printUserTotals();
            return "incorrect";
        }
    }

    /**
     * Check if any reservations have expired and 
     * @param category category 0 <= category < numCategories
     * @param question 0 <= question < numQuestions
     */
    private void expireReservations(int category, int question) {
        //TODO: we could be clever and expire only as many reservations as
        //necessary to free up the points.
        BoardState oldState = boardState;
        long now = new Date().getTime();
        Question q = getQuestions(category).get(question);
        for (Iterator<PointReservation> i = reservations.get(q).iterator(); i.hasNext();) {
            PointReservation r = i.next();
            if (r.expiryTimestamp < now) {
                i.remove();
                boardState = boardState.withUpdate(category, question, r.points);
            }
        }
        //Only push an update if we expired.
        if (boardState != oldState)
            updateThread.update(boardState);
    }

    public static final class PointReservation {
        private final UUID user;
        private final int points;
        private final long expiryTimestamp;
        public PointReservation(UUID user, int points, long expiryTimestamp) {
            this.user = user;
            this.points = points;
            this.expiryTimestamp = expiryTimestamp;
        }
    }
    
    private void printUserTotals() {
        List<Integer> tot = new ArrayList<Integer>();
        int totUser = 0;
        for(UserState u : userStates.values()) {
            totUser += u.getScore();
            tot.add(u.getScore());
        }
        Collections.sort(tot, new IntComparable());  
        System.out.println(tot.size() + " users with " + totUser + " points, server has " + boardState.totalPoints() + " points " + tot.toString());
    }
    
    private class IntComparable implements Comparator<Integer>{   
        @Override
        public int compare(Integer o1, Integer o2) {
            return (o1>o2 ? -1 : (o1==o2 ? 0 : 1));
        }
    }
}

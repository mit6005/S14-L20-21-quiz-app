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
    // Constants 
    private static final int INIT_POINTS_PER_QUESTION = 1000;
    private static final int USER_INITIAL_SCORE = 100;
    private static final long RESERVATION_EXPIRY_MILLIS = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

    private final List<String> categories;
    private final List<List<Question>> questions;
    private final List<List<Integer>> answerKey;
    private final Map<UUID, UserState> userStates = new HashMap<UUID, UserState>();
    private BoardState boardState;
    private final BoardStateUpdateThread updateThread;
    private final Object lock[][];
    
    /** 
     * Concurrency argument
     * Multiple client threads will be accessing the same ServerState
     * However, all of them will be accessing using synchronized, thus
     * only one client can be reading/modifying the board state at a time.
     * Or will be reading immutable data
     */

    /**
     * Parse the questions file and create the data structures
     * @param questionsFile
     * @param updateThread
     * @throws IOException
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
        
        lock = new Object[numCategories][questionsPerCategory];

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
        this.updateThread = updateThread;
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
    
   
    public Object getLock(int category, int question) {
        return lock[category][question];
    }

    /**
     * Get a new user state
     * No two users with the same userID will be created 
     * 
     * @param userId
     * @return
     */
    public synchronized UserState getUserState(UUID userId) {
        UserState state = userStates.get(userId);
        if (state == null)
            userStates.put(userId, (state = new UserState(USER_INITIAL_SCORE, getCategories().size(), getQuestions(0).size())));
        return state;
    }

    public synchronized BoardState getBoardState() {
        return boardState;
    }

    public synchronized boolean tryBid(UUID userId, int category, int question, int points) {
        if (points > boardState.getAvailablePoints(category, question)) {
                return false;
        }     
        return true;
    }

    public synchronized String checkAnswer(UUID user, int category, int question, int answer, int points) {  
        if (answerKey.get(category).get(question) != answer) {
            // wrong
            userStates.put(user, userStates.get(user).withUpdate(category, question, -points));
            boardState = boardState.withUpdate(category, question, points);
            updateThread.update(boardState);
            return  "incorrect";
        } else {
            userStates.put(user, userStates.get(user).withUpdate(category, question, points));
            boardState = boardState.withUpdate(category, question, -points);
            updateThread.update(boardState);
            return "correct";
        }
    }
}

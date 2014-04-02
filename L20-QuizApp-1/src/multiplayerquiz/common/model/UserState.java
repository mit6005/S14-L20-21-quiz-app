package multiplayerquiz.common.model;

/**
 * The state of a user, containing its current score and its answer history.
 * Instances of this class are immutable.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public final class UserState {
    private final int score;
    /**
     * Positive numbers indicate correct answers, negative numbers incorrect
     * answers, and 0 questions not attempted.  The number's magnitude indicates
     * the points risked on that question.
     */
    private final int[][] history;

    /**
     * Creates a new UserState with the given score and empty answer history.
     * @param score
     * @param categories
     * @param questionsPerCategory
     */
    public UserState(int score, int categories, int questionsPerCategory) {
        this.score = score;
        this.history = new int[categories][questionsPerCategory];
    }

    /**
     * Creates a new UserState with the given score and history.  The new state
     * takes ownership of the history array, so they should not be modified.
     * (Note: because the constructor is private, we can easily check all
     * callers to ensure they don't modify the array or retain a reference; a
     * public constructor would make a copy here.)
     * @param score
     * @param history
     */
    private UserState(int score, int[][] history) {
        this.score = score;
        this.history = history;
    }

    /**
     * Returns a new UserState as the result of answering the specified question
     * with the given point update (positive if correct, negative if incorrect).
     * @param category
     * @param question
     * @param pointUpdate the points risked (positive if correct, else negative)
     * @return a new, updated UserState
     */
    public UserState withUpdate(int category, int question, int pointUpdate) {
        int score = this.score + pointUpdate;
        int[][] history = new int[this.history.length][];
        for(int i=0; i< this.history.length; i++)
            history[i] = (i == category)?this.history[i].clone():this.history[i];
        history[category][question] += pointUpdate;
        return new UserState(score, history);
    }

    public int getScore() {
        return score;
    }

    public boolean attemptedQuestion(int category, int question) {
        return getHistory(category, question) != 0;
    }

    public int getHistory(int category, int question) {
        return history[category][question];
    }

    public static UserState parse(String string) {
        String[] fragments = string.split(" ");
        int score = Integer.parseInt(fragments[0]);
        int categories = Integer.parseInt(fragments[1]);
        int questionsPerCategory = Integer.parseInt(fragments[2]);
        int[][] history = new int[categories][questionsPerCategory];

        for (int i = 0; i < categories; ++i)
            for (int j = 0; j < questionsPerCategory; ++j)
                history[i][j] = Integer.parseInt(fragments[i*questionsPerCategory + j + 3]);
        return new UserState(score, history);
    }

    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(score)).append(' ');
        sb.append(Integer.toString(history.length)).append(' ');
        sb.append(Integer.toString(history[0].length)).append(' ');
        for (int[] x : history)
            for (int y : x)
                sb.append(Integer.toString(y)).append(' ');
        //remove extra space at end
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}

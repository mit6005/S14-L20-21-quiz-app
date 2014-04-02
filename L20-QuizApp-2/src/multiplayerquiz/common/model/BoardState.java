package multiplayerquiz.common.model;

import java.util.Arrays;

/**
 * The board state, as number of points currently available per question.
 * Instances of this class are immutable.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public final class BoardState {
    /**
     * Number of points currently associated with each question.
     * Each point has to be a non-negative integer
     */
    private final int[][] points;
    
    /**
     * Create a BoardState with a given number of points for a 2D grid
     * of categories x questions
     * @param pointsPerQuestion
     * @param categories
     * @param categoriesPerQuestion
     */
    public BoardState(int pointsPerQuestion, int categories, int categoriesPerQuestion) {
        this.points = new int[categories][categoriesPerQuestion];
        for (int[] category : points)
            Arrays.fill(category, pointsPerQuestion);
    }
    
    /**
     * Creates a new BoardState owning the given points array.  The new state
     * takes ownership of the points array, so they should not be modified.
     * (Note: because the constructor is private, we can easily check all
     * callers to ensure they don't modify the array or retain a reference; a
     * public constructor would make a copy here.)
     * @param points
     */
    private BoardState(int[][] points) {
        this.points = points;
    }

    /**
     * Returns a new BoardState as the result of updating a specific
     * localtion of the point table
     * @param category  0 <= category < numCategories
     * @param question  0 <= question <= numQuestions
     * @param pointUpdate
     * @return
     */
    public BoardState withUpdate(int category, int question, int pointUpdate) {
        int[][] points = new int[this.points.length][];
        for(int i=0; i< this.points.length; i++)
            points[i] = (i == category)?this.points[i].clone():this.points[i];
        points[category][question] += pointUpdate;
        assert(points[category][question] >= 0);
        return new BoardState(points);
    }
    

    /**
     * 
     * @param category  0 <= category < numCategories
     * @param question  0 <= question < numQuestions
     * @return the current point value of the <category,question>
     */
    public int getAvailablePoints(int category, int question) {
        return points[category][question];
    }

    /**
     * 
     * @param string Given a 'right' formatted string, create a BoardState
     * @return
     */
    public static BoardState parse(String string) {
        String[] fragments = string.split(" ");
        int categories = Integer.parseInt(fragments[0]);
        int questionsPerCategory = Integer.parseInt(fragments[1]);
        int[][] points = new int[categories][questionsPerCategory];

        for (int i = 0; i < categories; ++i)
            for (int j = 0; j < questionsPerCategory; ++j)
                points[i][j] = Integer.parseInt(fragments[i*questionsPerCategory + j + 2]);
        return new BoardState(points);
    }

    /**
     * Create a formatted string representing the BoardState
     * @return
     */
    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(points.length)).append(' ');
        sb.append(Integer.toString(points[0].length)).append(' ');
        for (int[] x : points)
            for (int y : x)
                sb.append(Integer.toString(y)).append(' ');
        //remove extra space at end
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    
    public int totalPoints() {
        int tot = 0;
        for (int i = 0; i < points.length; ++i)
            for (int j = 0; j < points[0].length; ++j)
                tot += points[i][j];
        return tot;
    }
}

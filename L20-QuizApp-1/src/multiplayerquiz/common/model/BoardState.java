package multiplayerquiz.common.model;

import java.util.Arrays;

/**
 * The board state, as number of points currently available per question.
 * Instances of this class are immutable.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public final class BoardState {
    private final int[][] points;
    public BoardState(int pointsPerQuestion, int categories, int categoriesPerQuestion) {
        this.points = new int[categories][categoriesPerQuestion];
        for (int[] category : points)
            Arrays.fill(category, pointsPerQuestion);
    }
    /**
     * Creates a new BoardState owning the given points array.
     * @param points
     */
    private BoardState(int[][] points) {
        this.points = points;
    }

    public BoardState withUpdate(int category, int question, int pointUpdate) {
        int[][] points = this.points;
        points[category] = points[category].clone();
        points[category][question] += pointUpdate;
        return new BoardState(points);
    }

    public int getAvailablePoints(int category, int question) {
        return points[category][question];
    }

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
}

package multiplayerquiz.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import multiplayerquiz.common.protocol.Protocol;

/**
 * A multiple-choice quiz question.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class Question {
    // The text of the question
    private final String question;
    // All the answers for the question
    private final List<String> choices;

    /**
     * Create a new question given the question text and the answers
     * Use of unmodifiableList to avoid rep exposure
     * @param question
     * @param choices
     */
    public Question(String question, List<String> choices) {
        this.question = question;
        this.choices = Collections.unmodifiableList(choices);
    }

    /**
     * 
     * @return the question text
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Returns the choices. No need for defensive copying 
     * @return List of answers
     */
    public List<String> getChoices() {
        return choices;
    }

    /**
     * Create a string to represent the question
     * Note that escape characters need to be remapped
     * @return
     */
    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Protocol.escape(question)).append(' ');
        sb.append(choices.size()).append(' ');
        sb.append(Protocol.escape(choices.get(0)));
        for (int i = 1; i < choices.size(); ++i)
            sb.append(' ').append(Protocol.escape(choices.get(i)));
        return sb.toString();
    }

    /**
     * Given a 'right' formatted string, create a Question
     * @param string
     * @return
     */
    public static Question parse(String string) {
        String[] fragments = string.split(" ");
        String question = Protocol.unescape(fragments[0]);
        int numChoices = Integer.parseInt(fragments[1]);
        List<String> choices = new ArrayList<String>(numChoices);
        for (int i = 2; i < 2+numChoices; ++i)
            choices.add(Protocol.unescape(fragments[i]));
        return new Question(question, choices);
    }
}

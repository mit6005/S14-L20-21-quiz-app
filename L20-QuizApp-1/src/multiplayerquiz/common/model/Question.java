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
    private final String question;
    private final List<String> choices;

    public Question(String question, List<String> choices) {
        this.question = question;
        this.choices = Collections.unmodifiableList(choices);
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Protocol.escape(question)).append(' ');
        sb.append(choices.size()).append(' ');
        sb.append(Protocol.escape(choices.get(0)));
        for (int i = 1; i < choices.size(); ++i)
            sb.append(' ').append(Protocol.escape(choices.get(i)));
        return sb.toString();
    }

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

package multiplayerquiz.common.protocol;

/**
 * Sent by the client to answer the question posed in a {@link BidAccepted}.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class Answer implements ProtocolMessage {
    private static final String ID = "Answer";
    private final int answer;

    public Answer(int answer) {
        this.answer = answer;
    }

    public int getAnswer() {
        return answer;
    }

    protected static final class AnswerSerializer extends AbstractMessageSerializer {
        public AnswerSerializer() {
            super(Answer.class, Answer.ID);
        }
        @Override
        public String serialize(ProtocolMessage message) {
            return String.format("%s %d", ID, ((Answer)message).getAnswer());
        }
        @Override
        public ProtocolMessage deserialize(String string) {
            String[] fragments = string.split(" ");
            if (fragments.length != 2 || !fragments[0].equals(ID))
                throw new ProtocolException(string);
            return new Answer(Integer.parseInt(fragments[1]));
        }
    }
}

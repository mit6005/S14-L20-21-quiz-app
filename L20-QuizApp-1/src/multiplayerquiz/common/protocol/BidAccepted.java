package multiplayerquiz.common.protocol;

import multiplayerquiz.common.model.Question;

/**
 * Sent by the server in response to a {@link Bid} that was accepted.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class BidAccepted implements ProtocolMessage {
    private static final String ID = "BidAccepted";
    private final Question question;
    public BidAccepted(Question question) {
        this.question = question;
    }

    public Question getQuestion() {
        return question;
    }

    protected static final class BidAcceptedSerializer extends AbstractMessageSerializer {
        public BidAcceptedSerializer() {
            super(BidAccepted.class, BidAccepted.ID);
        }
        @Override
        public String serialize(ProtocolMessage message) {
            return String.format("%s %s", ID, ((BidAccepted)message).getQuestion().toProtocolString());
        }
        @Override
        public ProtocolMessage deserialize(String string) {
            if (!string.startsWith(ID))
                throw new ProtocolException(string);
            return new BidAccepted(Question.parse(string.substring(ID.length()+1)));
        }
    }
}

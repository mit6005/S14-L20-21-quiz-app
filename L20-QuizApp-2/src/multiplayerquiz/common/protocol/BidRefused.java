package multiplayerquiz.common.protocol;

/**
 * Sent by the server in response to a {@link Bid} that was refused.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class BidRefused implements ProtocolMessage {
    private static final String ID = "BidRefused";
    private final String reason;
    
    public BidRefused(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    protected static final class BidRefusedSerializer extends AbstractMessageSerializer {
        public BidRefusedSerializer() {
            super(BidRefused.class, BidRefused.ID);
        }
        @Override
        public String serialize(ProtocolMessage message) {
            return String.format("%s %s", ID, Protocol.escape(((BidRefused)message).getReason()));
        }
        @Override
        public ProtocolMessage deserialize(String string) {
            String[] fragments = string.split(" ");
            if (fragments.length != 2 || !fragments[0].equals(ID))
                throw new ProtocolException(string);
            return new BidRefused(Protocol.unescape(fragments[1]));
        }
    }
}

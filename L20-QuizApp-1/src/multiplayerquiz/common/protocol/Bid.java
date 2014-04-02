package multiplayerquiz.common.protocol;

/**
 * Sent by the client to risk points on a question.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class Bid implements ProtocolMessage {
    private static final String ID = "BidBegin";
    private final int category, question, amount;

    public Bid(int category, int question, int amount) {
        this.category = category;
        this.question = question;
        this.amount = amount;
    }

    public int getCategory() {
        return category;
    }

    public int getQuestion() {
        return question;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %d", ID, category, question, amount);
    }

    protected static final class BidSerializer extends AbstractMessageSerializer {
        public BidSerializer() {
            super(Bid.class, Bid.ID);
        }
        @Override
        public String serialize(ProtocolMessage message) {
            return message.toString();
        }
        @Override
        public ProtocolMessage deserialize(String string) {
            String[] fragments = string.split(" ");
            if (fragments.length != 4 || !fragments[0].equals(ID))
                throw new ProtocolException(string);
            return new Bid(Integer.parseInt(fragments[1]),
                    Integer.parseInt(fragments[2]),
                    Integer.parseInt(fragments[3]));
        }
    }
}

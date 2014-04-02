package multiplayerquiz.common.protocol;

import multiplayerquiz.common.model.BoardState;

/**
 * Sent synchronously by the server immediately after {@link ServerHello}, then
 * asynchronously thereafter.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class BoardStateUpdate implements ProtocolMessage {
    private static final String ID = "BoardStateUpdate";
    private final BoardState boardState;
    public BoardStateUpdate(BoardState boardState) {
        this.boardState = boardState;
    }

    public BoardState getBoardState() {
        return boardState;
    }

    protected static final class BoardStateUpdateSerializer extends AbstractMessageSerializer {
        public BoardStateUpdateSerializer() {
            super(BoardStateUpdate.class, BoardStateUpdate.ID);
        }
        @Override
        public String serialize(ProtocolMessage message) {
            return String.format("%s %s", ID, ((BoardStateUpdate)message).getBoardState().toProtocolString());
        }
        @Override
        public ProtocolMessage deserialize(String string) {
            if (!string.startsWith(ID))
                throw new ProtocolException(string);
            return new BoardStateUpdate(BoardState.parse(string.substring(ID.length()+1)));
        }
    }
}

package multiplayerquiz.common.protocol;

import java.util.UUID;

/**
 * Sent by the client to identify itself.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public final class ClientHello implements ProtocolMessage {
    private static final String ID = "ClientHello";
    private UUID userId;
    
    public ClientHello() {
        this(UUID.randomUUID());
    }
    public ClientHello(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return String.format("%s %s\n", ID, userId.toString());
    }

    protected static final class ClientHelloSerializer extends AbstractMessageSerializer {
        public ClientHelloSerializer() {
            super(ClientHello.class, ClientHello.ID);
        }
        @Override
        public String serialize(ProtocolMessage message) {
            return message.toString();
        }
        @Override
        public ProtocolMessage deserialize(String string) {
            String[] fragments = string.trim().split(" ");
            if (fragments.length != 2 || !fragments[0].equals(ID))
                throw new ProtocolException(string);
            UUID userId;
            try {
                userId = UUID.fromString(fragments[1]);
            } catch (IllegalArgumentException ex) {
                throw new ProtocolException(string, ex);
            }
            return new ClientHello(userId);
        }
    }

    public static void main(String[] args) {
        System.out.println(Protocol.serialize(new ClientHello()));
    }
}

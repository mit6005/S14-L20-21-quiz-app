package multiplayerquiz.common.protocol;

/**
 * A MessageSerializer for one specific message class.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public abstract class AbstractMessageSerializer implements MessageSerializer {
    private final Class<? extends ProtocolMessage> messageClass;
    private final String messageId;

    public AbstractMessageSerializer(Class<? extends ProtocolMessage> messageClass, String messageId) {
        this.messageClass = messageClass;
        this.messageId = messageId;
    }

    @Override
    public boolean canSerialize(ProtocolMessage message) {
        return messageClass.isInstance(message);
    }

    @Override
    public boolean canDeserialize(String message) {
        return message.startsWith(messageId);
    }

    @Override
    public String toString() {
        return String.format("%s:%s/%s", getClass().getSimpleName(), messageClass, messageId);
    }
}

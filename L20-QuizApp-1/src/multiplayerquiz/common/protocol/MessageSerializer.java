package multiplayerquiz.common.protocol;

/**
 * A service interface implementing message (de)serialization for a message
 * class.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public interface MessageSerializer {
    public boolean canSerialize(ProtocolMessage message);
    public String serialize(ProtocolMessage message);

    public boolean canDeserialize(String message);
    public ProtocolMessage deserialize(String message);
}

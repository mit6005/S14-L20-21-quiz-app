package multiplayerquiz.common.protocol;

import java.util.ServiceLoader;

/**
 * Entry point for protocol operations.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public final class Protocol {
    private static final ServiceLoader<MessageSerializer> SERVICE_LOADER = ServiceLoader.load(MessageSerializer.class);
    private Protocol() {}

    public static String serialize(ProtocolMessage message) {
        for (MessageSerializer s : SERVICE_LOADER)
            if (s.canSerialize(message)) {
                String str = s.serialize(message);
                if (!str.endsWith("\n"))
                    str += "\n";
                return str;
            }
        throw new ProtocolException("No serializer for "+message);
    }

    public static ProtocolMessage deserialize(String message) {
        for (MessageSerializer s : SERVICE_LOADER)
            if (s.canDeserialize(message))
                return s.deserialize(message);
        throw new ProtocolException("No deserializer for "+message);
    }

    private static final char[][] ESCAPES = {
        {' ', '\0'},
        {'\n', '\1'},
        {'\r', '\2'},
    };

    public static String escape(String string) {
        for (char[] c : ESCAPES)
            string = string.replace(c[0], c[1]);
        return string;
    }

    public static String unescape(String string) {
        for (char[] c : ESCAPES)
            string = string.replace(c[1], c[0]);
        return string;
    }
}

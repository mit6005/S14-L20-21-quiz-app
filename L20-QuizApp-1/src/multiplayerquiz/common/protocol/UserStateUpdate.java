package multiplayerquiz.common.protocol;

import multiplayerquiz.common.model.UserState;

/**
 * Sent by the server to update the client with the results of its
 * {@link Answer}. (The client checks the message history to determine whether
 * it was correct.)
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class UserStateUpdate implements ProtocolMessage {
    private static final String ID = "UserStateUpdate";
    private final UserState userState;
    private final String message;
    public UserStateUpdate(UserState userState, String message) {
        this.userState = userState;
        this.message = message;
    }

    public UserState getUserState() {
        return userState;
    }

    public String getMessage() {
        return message;
    }

    protected static final class UserStateUpdateSerializer extends AbstractMessageSerializer {
        public UserStateUpdateSerializer() {
            super(UserStateUpdate.class, UserStateUpdate.ID);
        }
        @Override
        public String serialize(ProtocolMessage message) {
            UserStateUpdate m = (UserStateUpdate)message;
            return String.format("%s %s %s", ID, Protocol.escape(m.getMessage()), m.getUserState().toProtocolString());
        }
        @Override
        public ProtocolMessage deserialize(String string) {
            if (!string.startsWith(ID))
                throw new ProtocolException(string);
            int secondSpace = string.indexOf(" ", ID.length()+2);
            String message = Protocol.unescape(string.substring(ID.length()+1, secondSpace));
            return new UserStateUpdate(UserState.parse(string.substring(secondSpace+1)), message);
        }
    }
}

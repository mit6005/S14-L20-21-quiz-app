package multiplayerquiz.common.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import multiplayerquiz.common.model.UserState;

/**
 * Sent by the server in response to a {@link ClientHello} message.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public final class ServerHello implements ProtocolMessage {
    private static final String ID = "ServerHello";
    private final List<String> categories;
    private final int questionsPerCategory;
    private final UserState userState;
    public ServerHello(List<String> categories, int questionsPerCategory, UserState userState) {
        this.categories = Collections.unmodifiableList(categories);
        this.questionsPerCategory = questionsPerCategory;
        this.userState = userState;
    }

    public List<String> getCategories() {
        return categories;
    }

    public int getQuestionsPerCategory() {
        return questionsPerCategory;
    }

    public UserState getUserState() {
        return userState;
    }

    protected static final class ServerHelloSerializer extends AbstractMessageSerializer {
        public ServerHelloSerializer() {
            super(ServerHello.class, ServerHello.ID);
        }
        @Override
        public String serialize(ProtocolMessage message0) {
            ServerHello message = (ServerHello)message0;
            StringBuilder string = new StringBuilder();
            string.append(ID).append(' ');

            //number of categories, followed by each category name with spaces replaced by nulls
            string.append(Integer.toString(message.getCategories().size())).append(' ');
            for (String category : message.getCategories())
                string.append(Protocol.escape(category)).append(" ");

            string.append(Integer.toString(message.getQuestionsPerCategory())).append(' ');

            string.append(message.userState.toProtocolString()).append('\n');
            return string.toString();
        }
        @Override
        public ProtocolMessage deserialize(String string) {
            String[] fragments = string.trim().split(" ");
            int numCategories = Integer.parseInt(fragments[1]);
            List<String> categories = new ArrayList<String>(numCategories);
            for (int i = 2; i < numCategories+2; ++i)
                categories.add(Protocol.unescape(fragments[i]));
            int questionsPerCategory = Integer.parseInt(fragments[2 + numCategories]);

            StringBuilder sb = new StringBuilder();
            sb.append(fragments[2+numCategories+1]);
            for (int i = 2+numCategories+2; i < fragments.length; ++i)
                sb.append(' ').append(fragments[i]);
            UserState userState = UserState.parse(sb.toString());
            return new ServerHello(categories, questionsPerCategory, userState);
        }
    }
}

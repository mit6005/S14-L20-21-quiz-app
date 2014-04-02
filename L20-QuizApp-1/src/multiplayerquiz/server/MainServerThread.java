package multiplayerquiz.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * The main server thread, which spawns new threads for incoming connections.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class MainServerThread extends Thread {
    public static final int SERVER_PORT = 4444;
    private final ServerState state;
    private final BoardStateUpdateThread updateThread = new BoardStateUpdateThread();
    public MainServerThread(String questionsFile) throws IOException {
        super("MainServerThread");
        state = new ServerState(questionsFile, updateThread);
    }

    @Override
    public void run() {
        updateThread.start();
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientThread(socket, state, updateThread).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

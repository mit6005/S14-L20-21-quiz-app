package multiplayerquiz;

import java.io.IOException;

import multiplayerquiz.client.MainFrame;
import multiplayerquiz.server.MainServerThread;


/**
 * The JAR entry point, redirecting to the client or server based on
 * command-line options.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length >= 2 && args[0].contains("server"))
            new MainServerThread(args[1]).start();
        else
            MainFrame.main();
    }
}

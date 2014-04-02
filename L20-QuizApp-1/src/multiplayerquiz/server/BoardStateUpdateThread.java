package multiplayerquiz.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import multiplayerquiz.common.model.BoardState;
import multiplayerquiz.common.protocol.BoardStateUpdate;
import multiplayerquiz.common.protocol.Protocol;

/**
 * Sends BoardStateUpdate messages to clients.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class BoardStateUpdateThread extends Thread {
    private final List<BufferedWriter> clientsToNotify = new CopyOnWriteArrayList<BufferedWriter>();
    private final BlockingQueue<BoardState> boardStates = new LinkedBlockingQueue<BoardState>();
    public BoardStateUpdateThread() {
        super("BoardStateUpdateThread");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            BoardState state;
            try {
                state = boardStates.take();
            } catch (InterruptedException ex) {
                break;
            }
            String message = Protocol.serialize(new BoardStateUpdate(state));
            for (BufferedWriter w : clientsToNotify)
                synchronized (w) {
                    try {
                        w.write(message);
                        w.flush();
                    } catch (IOException e) {
                        //Ignore: maybe the client just disconnected, etc. Must
                        //still service the other clients.
                    }
                }
        }
    }

    public void subscribe(BufferedWriter client) {
        clientsToNotify.add(client);
    }

    public void unsubscribe(BufferedWriter client) {
        clientsToNotify.remove(client);
    }

    public void update(BoardState state) {
        try {
            boardStates.put(state);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}

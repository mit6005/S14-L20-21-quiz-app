package multiplayerquiz.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;

import multiplayerquiz.common.model.Question;
import multiplayerquiz.common.protocol.*;

/**
 * Handles interactions with a single client.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class ClientThread extends Thread {
    private final Socket socket;
    private final ServerState state;
    private final BoardStateUpdateThread updateThread;
    private BufferedReader reader;
    private BufferedWriter writer;
    private UUID userId;
    public ClientThread(Socket socket, ServerState state, BoardStateUpdateThread updateThread) {
        this.socket = socket;
        this.state = state;
        this.updateThread = updateThread;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = reader;
            this.writer = writer;

            while (true) {
                String line = reader.readLine();
                if (line == null) //client closed socket
                    break;
                ProtocolMessage message = Protocol.deserialize(line);
                if (message instanceof ClientHello)
                    sendServerHello((ClientHello)message);
                else if (message instanceof Bid)
                    processBid((Bid)message);
                else
                    throw new ProtocolException("unexpected "+line);
            }
        } catch (IOException ex) {
            //TODO: clean up client state, if any
        } finally {
            updateThread.unsubscribe(writer);
        }
    }

    private void sendServerHello(ClientHello clientHello) throws IOException {
        setName("Client-"+clientHello.getUserId());
        userId = clientHello.getUserId();
        ServerHello msg = new ServerHello(state.getCategories(), state.getQuestions(0).size(), state.getUserState(userId));
        write(Protocol.serialize(msg));
        write(Protocol.serialize(new BoardStateUpdate(state.getBoardState())));
        updateThread.subscribe(writer);
    }

    private void processBid(Bid Bid) throws IOException {
        //Enforce these server-side to defend against dishonest clients.
        if (Bid.getAmount() <= 0) {
            write(Protocol.serialize(new BidRefused("must Bid positive points (tried "+Bid.getAmount()+")")));
            return;
        }
        int currentScore = state.getUserState(userId).getScore();
        if (Bid.getAmount() > currentScore) {
            write(Protocol.serialize(new BidRefused(String.format(
                    "can't Bid more points than you have (have %d, tried to Bid %d)",
                    currentScore, Bid.getAmount()))));
            return;
        }

        ServerState.PointReservation r = state.tryBid(userId,Bid.getCategory(), Bid.getQuestion(), Bid.getAmount());
        if (r == null) {
            write(Protocol.serialize(new BidRefused("too few points remain")));
            return;
        }

        Question q = state.getQuestions(Bid.getCategory()).get(Bid.getQuestion());
        write(Protocol.serialize(new BidAccepted(q)));

        String line = reader.readLine();
        ProtocolMessage message = Protocol.deserialize(line);
        if (!(message instanceof Answer))
            throw new ProtocolException("expected Answer, got "+message);
        int answer = ((Answer)message).getAnswer();
        String reasonString = state.redeemReservation(r, Bid.getCategory(), Bid.getQuestion(), answer);

        write(Protocol.serialize(new UserStateUpdate(state.getUserState(userId), reasonString)));
    }

    private void write(String msg) throws IOException {
        synchronized (writer) {
            writer.write(msg);
            writer.flush();
        }
    }
}

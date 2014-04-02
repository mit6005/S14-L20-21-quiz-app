package multiplayerquiz.client;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import multiplayerquiz.common.model.BoardState;
import multiplayerquiz.common.model.UserState;
import multiplayerquiz.common.protocol.Answer;
import multiplayerquiz.common.protocol.BoardStateUpdate;
import multiplayerquiz.common.protocol.ClientHello;
import multiplayerquiz.common.protocol.Protocol;
import multiplayerquiz.common.protocol.ProtocolException;
import multiplayerquiz.common.protocol.ProtocolMessage;
import multiplayerquiz.common.protocol.Bid;
import multiplayerquiz.common.protocol.BidAccepted;
import multiplayerquiz.common.protocol.BidRefused;
import multiplayerquiz.common.protocol.ServerHello;
import multiplayerquiz.common.protocol.UserStateUpdate;
import multiplayerquiz.server.MainServerThread;

/**
 * The main client window.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private UserState userState;
    private BoardState boardState;
    private final List<QuestionButton> questionButtons = new ArrayList<QuestionButton>();
    private final JLabel scoreLabel;
    private final BlockingQueue<ProtocolMessage> messageQueue = new LinkedBlockingQueue<ProtocolMessage>();
    private MainFrame(BufferedReader reader, BufferedWriter writer, ServerHello hello, BoardState boardState) {
        this.reader = reader;
        this.writer = writer;
        this.userState = hello.getUserState();
        this.boardState = boardState;
        new ReaderThread().start();

        int numRows = hello.getQuestionsPerCategory() + 2;
        int numCols = hello.getCategories().size();
        GridLayout layout = new GridLayout(numRows, numCols);
        layout.setHgap(10);
        layout.setVgap(5);
        setLayout(layout);
        for (String c : hello.getCategories())
            add(new JLabel(c, JLabel.CENTER));
        for (int i = 0; i < hello.getQuestionsPerCategory(); ++i)
            for (int j = 0; j < hello.getCategories().size(); ++j) {
                QuestionButton b = new QuestionButton(j, i);
                questionButtons.add(b);
                add(b);
            }

        this.scoreLabel = new JLabel("score: TODO");
        add(scoreLabel);
        pack();
        update();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private class QuestionButton extends JButton implements ActionListener {
        private static final long serialVersionUID = 1L;
        public final int category, question;
        public QuestionButton(int category, int question) {
            super();
            addActionListener(this);
            this.category = category;
            this.question = question;
            update();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            processBid(category, question);
        }

        public void update() {
            if (userState.attemptedQuestion(category, question)) {
                setEnabled(false);
                int result = userState.getHistory(category, question);
                setText(result > 0 ? "+"+result : ""+result);
            } else {
                setEnabled(true);
                setText(""+boardState.getAvailablePoints(category, question));
            }
        }
    }

    private void processBid(int category, int question) {
        try {
            int amount = Integer.parseInt(JOptionPane.showInputDialog("How much do you want to risk?"));
            //TODO: event thread I/O == bad
            writer.write(Protocol.serialize(new Bid(category, question, amount)));
            writer.flush();

            ProtocolMessage reply = messageQueue.take();
            if (reply instanceof BidRefused) {
                JOptionPane.showMessageDialog(this, ((BidRefused)reply).getReason());
                return;
            }
            BidAccepted ra = (BidAccepted)reply;
            QuestionDialog qd = new QuestionDialog(this, ra.getQuestion());
            qd.setVisible(true);
            int answer = qd.getAnswer();
            writer.write(Protocol.serialize(new Answer(answer)));
            writer.flush();

            UserStateUpdate update = (UserStateUpdate)messageQueue.take();
            this.userState = update.getUserState();
            update();
            JOptionPane.showMessageDialog(this, update.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void update() {
        for (QuestionButton b : questionButtons)
            b.update();
        scoreLabel.setText("score: "+userState.getScore());
    }

    /**
     * Reads messages from the socket.  BoardStateUpdate messages are posted to
     * the event dispatch thread for update; other messages are put into a
     * BlockingQueue.
     */
    private final class ReaderThread extends Thread {
        ReaderThread() {
            super("ReaderThread");
            setDaemon(true);
        }
        @Override
        public void run() {
            while (true) {
                try {
                    final ProtocolMessage message = Protocol.deserialize(reader.readLine());
                    if (message instanceof BoardStateUpdate)
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                boardState = ((BoardStateUpdate)message).getBoardState();
                                update();
                            }
                        });
                    else
                        messageQueue.put(message);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main() {
        String serverIP = JOptionPane.showInputDialog("Enter the server's hostname or IP address.");
        int serverPort = MainServerThread.SERVER_PORT;
        try {
            Socket socket = new Socket(InetAddress.getByName(serverIP), serverPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(Protocol.serialize(new ClientHello()));
            writer.flush();
            ProtocolMessage message = Protocol.deserialize(reader.readLine());
            if (!(message instanceof ServerHello))
                throw new ProtocolException("expected ServerHello, got "+message);
            ServerHello hello = (ServerHello)message;

            message = Protocol.deserialize(reader.readLine());
            if (!(message instanceof BoardStateUpdate))
                throw new ProtocolException("expected BoardStateUpdateResponse, got "+message);
            BoardState boardState = ((BoardStateUpdate)message).getBoardState();

            final JFrame frame = new MainFrame(reader, writer, hello, boardState);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.setVisible(true);
                }
            });
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
    }
}

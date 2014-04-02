package multiplayerquiz.client;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import multiplayerquiz.common.model.Question;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/30/2014
 */
public final class QuestionDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final Question question;
    private final ButtonGroup buttonGroup;
    private int answer;
    public QuestionDialog(Frame owner, Question question) {
        super(owner, true);
        this.question = question;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(String.format("<html> %s </html>",
                question.getQuestion().replace("\n", "<br>"))));
        this.buttonGroup = new ButtonGroup();
        for (String choice : question.getChoices()) {
            JRadioButton rdo = new JRadioButton(choice);
            buttonGroup.add(rdo);
            panel.add(rdo);
        }
        JButton ok = new JButton("OK");
        ok.addActionListener(this);
        panel.add(ok);
        add(panel);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Enumeration<AbstractButton> btns = buttonGroup.getElements();
        int selected = -1, idx = 0;
        while (btns.hasMoreElements()) {
            if (btns.nextElement().isSelected())
                selected = idx;
            ++idx;
        }
        if (selected == -1)
            return;
        answer = selected;
        setVisible(false);
    }

    public int getAnswer() {
        return answer;
    }
}

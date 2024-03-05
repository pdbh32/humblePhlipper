package humblePhlipper;

import org.dreambot.api.Client;

import javax.swing.*;
import java.awt.event.*;
import java.util.Collections;

public class ErrorModal extends JDialog {
    humblePhlipper.ResourceManager rm;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea;
    private JTextField textField;

    public ErrorModal(humblePhlipper.ResourceManager rm) {
        this.rm = rm;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setLocationRelativeTo(Client.getCanvas());
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        textArea.setText("Failed to fetch data from the OSRS Wiki API.\n" +
                "Your current User-Agent,\n" +
                "\n" +
                ((rm.identity.requestHeaders != null) ? rm.identity.requestHeaders.get("User-Agent") : null) + "\n" +
                "\n" +
                "is probably being blocked. Try setting a new\n" +
                "one below or in\n" +
                "\n" +
                "DreamBot/Scripts/humblePhlipper/Identity.json\n" +
                "\n" +
                "and restarting the script. The OSRS Wiki API\n" +
                "recommends using a descriptive User-Agent like,\n" +
                "\n" +
                "\"volume_tracker - @ThisIsMyUsername on Discord\"");

        textField.requestFocusInWindow();
        pack();
        setVisible(true);
    }

    private void onOK() {
        if (!textField.getText().isEmpty()) {
            rm.identity.requestHeaders.put("User-Agent", Collections.singletonList(textField.getText()));
            rm.saveIdentity();
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
import org.dreambot.api.Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GUI extends JFrame {

    // Selection items
    final List<String> availableItemsList = new ArrayList<>();
    final List<Integer> restrictedIdList = new ArrayList<>();
    public GUI() {
        Collections.addAll(availableItemsList, "Death rune", "Cosmic rune", "Nature rune", "Logs", "Swordfish",
                "Raw swordfish", "Adamant arrow", "Steel bar", "Gold bar");

        Collections.addAll(restrictedIdList, 1521, 1519, 1515, 317, 315, 321, 319, 377, 379, 434, 1761,
                436, 438, 440, 442, 444, 453, 447, 449, 451, 1739, 229, 227, 1937, 313, 314, 221, 245, 556, 555, 557, 554, 558, 562);

        JFrame frame = new JFrame();

        frame.setTitle("humblePhlipper");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(Client.getCanvas());
        frame.setPreferredSize(new Dimension(400, 400));
        frame.getContentPane().setLayout(new BorderLayout());

        // Parameter panel
        JPanel parameterPanel = new JPanel();
        parameterPanel.setLayout(new GridLayout(0, 2));

        JLabel timeoutLabel = new JLabel();
        timeoutLabel.setText("Timeout (Minutes)");
        parameterPanel.add(timeoutLabel);

        JTextField timeoutField = new JTextField();
        parameterPanel.add(timeoutField);

        JLabel maxBidVolLabel = new JLabel();
        maxBidVolLabel.setText("Max Bid Order (% of Target)");
        parameterPanel.add(maxBidVolLabel);

        JTextField maxBidVolField = new JTextField();
        parameterPanel.add(maxBidVolField);

        JLabel sysExitLabel = new JLabel();
        sysExitLabel.setText("Close Client on Stop");
        parameterPanel.add(sysExitLabel);

        JCheckBox sysExitCheckBox = new JCheckBox();
        parameterPanel.add(sysExitCheckBox);

        JLabel vSpace1 = new JLabel();
        parameterPanel.add(vSpace1);

        JLabel vSpace2 = new JLabel();
        parameterPanel.add(vSpace2);

        // Table panel
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        // Table model, table, and scroll pane
        String[] columnNames = {"Name", "ID", "Members", "Restricted", "Profit", "Target Vol"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only allow editing for target vol
            }
        };
        addItemRow(tableModel,"Death rune");
        addItemRow(tableModel, "Logs");
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Reset-table button
        JButton resetButton = new JButton("Clear Item Selection");
        resetButton.addActionListener(e -> tableModel.setRowCount(0));
        tablePanel.add(resetButton, BorderLayout.SOUTH);

        // Item add-to-table-button panel
        JPanel itemAddPanel = new JPanel();
        itemAddPanel.setLayout(new GridLayout(4, 3));

        for (String name : availableItemsList) {
            JButton button = new JButton();
            button.setText(name);
            button.addActionListener(l -> addItemRow(tableModel, name));
            itemAddPanel.add(button);
        }
        tablePanel.add(itemAddPanel, BorderLayout.NORTH);

        // Item search
        JTextField searchField = new JTextField();
        parameterPanel.add(searchField);

        JButton searchButton = new JButton();
        searchButton.setText("Add by ID or Name");
        searchButton.addActionListener(l -> addItemRow(tableModel, searchField.getText()));
        parameterPanel.add(searchButton);

        JLabel vSpace3 = new JLabel();
        parameterPanel.add(vSpace3);

        JLabel vSpace4 = new JLabel();
        parameterPanel.add(vSpace4);

        // Start button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton startButton = new JButton();
        startButton.setText("Start");
        startButton.addActionListener(l -> {

            List<String> paramsList = new ArrayList<>();

            paramsList.add("[sysExit:" +  sysExitCheckBox.isSelected() + "]");
            paramsList.add("[timeout:" + timeoutField.getText() + "]");
            paramsList.add("[maxBidVol:" + timeoutField.getText() + "]");

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                String id = tableModel.getValueAt(row, 1).toString();
                String targetVol = tableModel.getValueAt(row, 4).toString();
                paramsList.add("{" + id + ":" + targetVol + "}");
            }
            String[] params = paramsList.toArray(new String[0]);

            Main.config.setParams(params);
            Main.isRunning = true;
            frame.dispose();
        });
        buttonPanel.add(startButton);

        frame.getContentPane().add(parameterPanel, BorderLayout.NORTH);
        frame.getContentPane().add(tablePanel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    // One of each item at most
    private boolean containsName(DefaultTableModel model, String name) {
        boolean match = false;
        for (int row = 0; row < model.getRowCount(); row++) {
            if (model.getValueAt(row, 0).toString().equalsIgnoreCase(name)) {
                match = true;
                break;
            }
        }
        if (match) {
            return true;
        }
        return false; // No matching row found
    }

    private void addItemRow(DefaultTableModel model, String input) {
        int ID = Main.api.getIdFromString(input);
        if (ID != -1) {
            API.Mapping mapping = Main.api.mappingMap.get(ID);
            if (!containsName(model, mapping.getName())) {
                model.addRow(new Object[]{
                        mapping.getName(),
                        mapping.getId(),
                        mapping.getMembers(),
                        restrictedIdList.contains(mapping.getId()),
                        Math.ceil(0.99 * Main.api.latestMap.get(mapping.getId()).getHigh() - Main.api.latestMap.get(mapping.getId()).getLow()),
                        mapping.getLimit()
                });
            }
        }
    }
}
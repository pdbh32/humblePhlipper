//GUI.java

import org.dreambot.api.Client;
import org.dreambot.api.settings.ScriptSettings;
import org.dreambot.api.utilities.Timer;

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

    // Objects to populate with Config data
    private JTextField timeoutField;
    private JCheckBox sysExitCheckBox;
    private JTextField maxBidVolField;
    private JTextField profitCutoffField;
    private DefaultTableModel tableModel;

    public GUI() {
        Collections.addAll(availableItemsList, "Death rune", "Cosmic rune", "Nature rune", "Logs", "Swordfish",
                "Raw swordfish", "Adamant arrow", "Steel bar", "Gold bar", "Law rune", "Gold necklace", "Maple longbow");

        Collections.addAll(restrictedIdList, 1521, 1519, 1515, 317, 315, 321, 319, 377, 379, 434, 1761,
                436, 438, 440, 442, 444, 453, 447, 449, 451, 1739, 229, 227, 1937, 313, 314, 221, 245, 556, 555, 557, 554, 558, 562);

        JFrame frame = new JFrame();

        frame.setTitle("humblePhlipper");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(Client.getCanvas());
        frame.setPreferredSize(new Dimension(500, 600));
        frame.getContentPane().setLayout(new BorderLayout());

        // Parameter panel
        JPanel parameterPanel = new JPanel();
        parameterPanel.setLayout(new GridLayout(0, 2));

        // Load button
        JButton loadCustom1 = new JButton("Load Custom 1");
        loadCustom1.addActionListener(l -> {
            try {
                Main.config = ScriptSettings.load(Config.class, "humblePhlipper", "Custom1Config.json");
                Main.config.setConfig();
                populateFieldsWithParams();
            } catch (Exception e) {
                // Config file doesn't exist yet
            }
        });
        parameterPanel.add(loadCustom1);

        // Save button
        JButton saveCustom1 = new JButton("Save to Custom 1");
        saveCustom1.addActionListener(l -> {
            String[] params = getParamsFromFields();
            Main.config.setParams(params);
            ScriptSettings.save(Main.config, "humblePhlipper", "Custom1Config.json");
        });
        parameterPanel.add(saveCustom1);

        // Load button
        JButton loadCustom2 = new JButton("Load Custom 2");
        loadCustom2.addActionListener(l -> {
            try {
                Main.config = ScriptSettings.load(Config.class, "humblePhlipper", "Custom2Config.json");
                Main.config.setConfig();
                populateFieldsWithParams();
            } catch (Exception e) {
                // Config file doesn't exist yet
            }
        });
        parameterPanel.add(loadCustom2);

        // Save button
        JButton saveCustom2Button = new JButton("Save to Custom 2");
        saveCustom2Button.addActionListener(l -> {
            String[] params = getParamsFromFields();
            Main.config.setParams(params);
            ScriptSettings.save(Main.config, "humblePhlipper", "Custom2Config.json");
        });
        parameterPanel.add(saveCustom2Button);

        JLabel vSpace1 = new JLabel();
        parameterPanel.add(vSpace1);

        JLabel vSpace2 = new JLabel();
        parameterPanel.add(vSpace2);

        JLabel timeoutLabel = new JLabel();
        timeoutLabel.setText("Timeout (Minutes)");
        parameterPanel.add(timeoutLabel);

        timeoutField = new JTextField();
        parameterPanel.add(timeoutField);

        JLabel maxBidVolLabel = new JLabel();
        maxBidVolLabel.setText("Max Bid Order (% of Target)");
        parameterPanel.add(maxBidVolLabel);

        maxBidVolField = new JTextField();
        parameterPanel.add(maxBidVolField);

        JLabel profitCutoffLabel = new JLabel();
        profitCutoffLabel.setText("Profit Cutoff");
        parameterPanel.add(profitCutoffLabel);

        profitCutoffField = new JTextField();
        parameterPanel.add(profitCutoffField);

        JLabel sysExitLabel = new JLabel();
        sysExitLabel.setText("Close Client on Stop");
        parameterPanel.add(sysExitLabel);

        sysExitCheckBox = new JCheckBox();
        parameterPanel.add(sysExitCheckBox);

        JLabel vSpace3 = new JLabel();
        parameterPanel.add(vSpace3);

        JLabel vSpace4 = new JLabel();
        parameterPanel.add(vSpace4);

        // Table panel
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        // Table model, table, and scroll pane
        String[] columnNames = {"Name", "ID", "Members", "Restricted", "Profit", "1hr Vol", "Target Vol"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only allow editing for target vol
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
        itemAddPanel.setLayout(new GridLayout(5, 3));

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
        searchButton.addActionListener(l -> {
            addItemRow(tableModel, searchField.getText());
            searchField.setText("");
        });
        parameterPanel.add(searchButton);

        JLabel vSpace5 = new JLabel();
        parameterPanel.add(vSpace5);

        JLabel vSpace6 = new JLabel();
        parameterPanel.add(vSpace6);

        // Start button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton startButton = new JButton();
        startButton.setText("Start");
        startButton.addActionListener(l -> {

            String[] params = getParamsFromFields();

            Main.config.setParams(params);
            Main.config.setConfig();
            Main.isRunning = true;
            Main.timer = new Timer();

            frame.dispose();
        });
        buttonPanel.add(startButton);

        // Set default config and populate fields
        Main.config.setConfig();
        populateFieldsWithParams();

        frame.getContentPane().add(parameterPanel, BorderLayout.NORTH);
        frame.getContentPane().add(tablePanel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
    private void populateFieldsWithParams() {
        timeoutField.setText(Float.toString(Main.getTimeout()));
        sysExitCheckBox.setSelected(Main.getSysExit());
        maxBidVolField.setText(Float.toString(Main.getMaxBidVol()));
        profitCutoffField.setText(Integer.toString(Main.getProfitCutoff()));
        tableModel.setRowCount(0);
        for (Integer id : Main.getItemMap().keySet()) {
            addItemRow(tableModel, String.valueOf(id));
        }
    }

    private String[] getParamsFromFields() {
        List<String> paramsList = new ArrayList<>();

        paramsList.add("[sysExit:" +  sysExitCheckBox.isSelected() + "]");
        paramsList.add("[timeout:" + timeoutField.getText() + "]");
        paramsList.add("[maxBidVol:" + maxBidVolField.getText() + "]");
        paramsList.add("[profitCutoff:" + profitCutoffField.getText() + "]");

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String id = tableModel.getValueAt(row, 1).toString();
            String targetVol = tableModel.getValueAt(row, 6).toString();
            paramsList.add("{" + id + ":" + targetVol + "}");
        }

        String[] params = paramsList.toArray(new String[0]);
        return params;
    }

    private boolean containsName(DefaultTableModel model, String name) {
        for (int row = 0; row < model.getRowCount(); row++) {
            if (model.getValueAt(row, 0).toString().equalsIgnoreCase(name)) {
                return true; // Match found, no need to continue checking
            }
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
                        Main.api.oneHrMap.get(mapping.getId()).getHighPriceVolume() + Main.api.oneHrMap.get(mapping.getId()).getLowPriceVolume(),
                        mapping.getLimit()
                });
            }
        }
    }
}
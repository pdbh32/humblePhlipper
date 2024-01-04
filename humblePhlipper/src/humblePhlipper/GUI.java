// GUI,java

package humblePhlipper;

import humblePhlipper.Resources.Items;
import humblePhlipper.Resources.SavedData.Config;
import org.dreambot.api.Client;
import org.dreambot.api.utilities.Timer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;

public class GUI extends JFrame {
    // Objects to populate with Config data
    private JTextField timeoutField;
    private JTextField profitCutOffField;
    private JCheckBox sysExitCheckBox;
    private JTextField maxBidValueField;
    private JTextField maxBidVolField;
    private JComboBox<String> pricingComboBox;
    private JSlider priorityCapitalBindingSlider;
    private JSlider priorityVolSlider;
    private JSlider priorityProfitSlider;
    private JTable selectionsTable;


    // Other (IntelliJ IDEA GUI3 Designer generated) fields
    private JPanel contentPanel;
    private JTabbedPane mainPane;
    private JButton startButton;
    private JPanel headerPanel;
    private JPanel paramsPanel;
    private JButton saveButton;
    private JPanel selectionsPanel;
    private JTabbedPane selectionsPane;
    private JScrollPane selectionsTableScrollPane;
    private JPanel manualPanel;
    private JComboBox configComboBox;
    private JButton addButton;
    private JButton resetButton;
    private JTextField addItemField;
    private JPanel autoPanel;
    private JTextField profitMarginVeTextField;
    private JTextField a1HourVolumeVeTextField;
    private JTextField capitalBindingVeTextField;
    private JTextField timeoutMinutesTextField;
    private JTextField profitCutoffTextField;
    private JTextField closeClientOnStopTextField;
    private JTextField maxValueOfAnyBidTextField;
    private JTextField maxVolumeOfAnyBidTextField;
    private JTextField minVolField;
    private JTextField maxBidAskVolRatioField;
    private JTextField minMarginField;
    private JTextField maxBidPriceField;
    private JCheckBox tradeRestrictedCheckBox;
    private JSpinner numToSelectSpinner;
    private JPanel miscellaneousPanel;
    private JButton generateButton;
    private JPanel savePanel;
    private JCheckBox membersCheckBox;

    public GUI() {
        // Set action listeners

        // headerPanel
        setConfigComboBox();
        setSaveButton();

        // mainPane > paramsPanel;
        setPricingComboBox();

        // mainPane > selectionsPanel
        setAddButton();
        setResetButton();
        setSelectionsTable();
        setGenerateButton();

        // startButton
        setStartButton();

        // If default is auto, select items
        if (Main.rm.config.getAuto()) {
            Main.rm.config.setSelections(new LinkedHashSet<Integer>());
            Main.trading.Select();
        }

        populateObjectsFromConfig();
        configureUI();
    }

    // Method called by `saveButton`, `generateButton`, and `startButton`
    private void setConfigFromObjects() {
        // Miscellaneous
        Main.rm.config.setTimeout(Float.parseFloat(timeoutField.getText()));
        Main.rm.config.setProfitCutOff(Integer.parseInt(profitCutOffField.getText()));
        Main.rm.config.setSysExit(sysExitCheckBox.isSelected());
        Main.rm.config.setMaxBidValue(Integer.parseInt(maxBidValueField.getText()));
        Main.rm.config.setMaxBidVol(Integer.parseInt(maxBidVolField.getText()));

        // Bid Priority
        Main.rm.config.setPriorityProfit(priorityProfitSlider.getValue());
        Main.rm.config.setPriorityVol(priorityVolSlider.getValue());
        Main.rm.config.setPriorityCapitalBinding(priorityCapitalBindingSlider.getValue());

        // Pricing set by event listener since it affects Selections (i.e. before `saveButton` or `startButton` are clicked

        // Selections set by `addButton` and `generateButton`

        // Selections Auto
        Main.rm.config.setAuto(selectionsPane.getSelectedIndex() == 1);
        Main.rm.config.setMinVol(Integer.parseInt(minVolField.getText()));
        Main.rm.config.setMinMargin(Integer.parseInt(minMarginField.getText()));
        Main.rm.config.setMaxBidPrice(Integer.parseInt(maxBidPriceField.getText()));
        Main.rm.config.setMaxBidAskVolRatio(Float.parseFloat(maxBidAskVolRatioField.getText()));
        Main.rm.config.setMembers(membersCheckBox.isSelected());
        Main.rm.config.setTradeRestricted(tradeRestrictedCheckBox.isSelected());
        Main.rm.config.setNumToSelect((Integer) numToSelectSpinner.getValue());
    }

    private void setConfigComboBox() {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxModel.addElement("Default");
        comboBoxModel.addElement("Custom 1");
        comboBoxModel.addElement("Custom 2");
        comboBoxModel.addElement("Custom 3");
        comboBoxModel.addElement("Custom 4");
        comboBoxModel.addElement("Custom 5");
        comboBoxModel.addElement("Custom 6");
        comboBoxModel.addElement("Custom 7");
        comboBoxModel.addElement("Custom 8");
        comboBoxModel.addElement("Custom 9");
        comboBoxModel.addElement("Custom 10");
        configComboBox.setModel(comboBoxModel);
        configComboBox.addActionListener(e -> {
            String fileName = (String) configComboBox.getSelectedItem();
            if (fileName == "Default") {
                Main.rm.config = new Config();
                saveButton.setEnabled(false);
            } else {
                Main.rm.loadConfig(fileName);
                saveButton.setEnabled(true);
            }
            if (Main.rm.config.getAuto()) {
                Main.rm.config.setSelections(new LinkedHashSet<Integer>());
                Main.trading.Select();
            }
            populateObjectsFromConfig();
        });
    }

    private void populateObjectsFromConfig() {
        // Miscellaneous
        timeoutField.setText(String.valueOf(Main.rm.config.getTimeout()));
        profitCutOffField.setText(String.valueOf(Main.rm.config.getProfitCutOff()));
        sysExitCheckBox.setSelected(Main.rm.config.getSysExit());
        maxBidValueField.setText(String.valueOf(Main.rm.config.getMaxBidValue()));
        maxBidVolField.setText(String.valueOf(Main.rm.config.getMaxBidPrice()));

        // Bid Priority
        priorityProfitSlider.setValue(Main.rm.config.getPriorityProfit());
        priorityVolSlider.setValue(Main.rm.config.getPriorityVol());
        priorityCapitalBindingSlider.setValue(Main.rm.config.getPriorityCapitalBinding());

        // Pricing
        switch (Main.rm.config.getPricing()) {
            case "latest":
                pricingComboBox.setSelectedItem("Latest");
                break;
            case "fiveMinute":
                pricingComboBox.setSelectedItem("5 Minute Average");
                break;
            case "oneHour":
                pricingComboBox.setSelectedItem("1 Hour Average");
                break;
            case "bestOfLatestFiveMinute":
                pricingComboBox.setSelectedItem("Best of Latest and 5 Minute Average");
                break;
            case "latestPmOne":
                pricingComboBox.setSelectedItem("Latest Bid Plus 1, Latest Ask Minus 1");
                break;
            case "fiveMinutePmOne":
                pricingComboBox.setSelectedItem("5 Minute Average Bid Plus 1, 5 Minute Average Ask Minus 1");
                break;
            case "bestOfLatestPmOneFiveMinutePmOne":
                pricingComboBox.setSelectedItem("Best of Latest ±1 and 5 Minute Average ±1");
                break;
        }

        // Selections
        setSelectionsTable();

        // Selections Auto
        selectionsPane.setSelectedIndex(Main.rm.config.getAuto() ? 1 : 0);
        minVolField.setText(String.valueOf(Main.rm.config.getMinVol()));
        minMarginField.setText(String.valueOf(Main.rm.config.getMinMargin()));
        maxBidPriceField.setText(String.valueOf(Main.rm.config.getMaxBidPrice()));
        maxBidAskVolRatioField.setText(String.valueOf(Main.rm.config.getMaxBidAskVolRatio()));
        membersCheckBox.setSelected(Main.rm.config.getMembers());
        tradeRestrictedCheckBox.setSelected(Main.rm.config.getTradeRestricted());
        numToSelectSpinner.setValue(Main.rm.config.getNumToSelect());
    }

    private void setSaveButton() {
        saveButton.setEnabled(!"Default".equals((String) configComboBox.getSelectedItem()));
        saveButton.addActionListener(e -> {
            setConfigFromObjects();
            String fileName = (String) configComboBox.getSelectedItem();
            Main.rm.saveConfig(fileName);
        });
    }

    private void setPricingComboBox() {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxModel.addElement("Latest");
        comboBoxModel.addElement("5 Minute Average");
        comboBoxModel.addElement("1 Hour Average");
        comboBoxModel.addElement("Best of Latest and 5 Minute Average");
        comboBoxModel.addElement("Latest Bid Plus 1, Latest Ask Minus 1");
        comboBoxModel.addElement("5 Minute Average Bid Plus 1, 5 Minute Average Ask Minus 1");
        comboBoxModel.addElement("Best of Latest ±1 and 5 Minute Average ±1");
        pricingComboBox.setModel(comboBoxModel);
        pricingComboBox.addActionListener(e -> {
            String selectionString = (String) pricingComboBox.getSelectedItem();
            switch (selectionString) {
                case "Latest":
                    Main.rm.config.setPricing("latest");
                    break;
                case "5 Minute Average":
                    Main.rm.config.setPricing("fiveMinute");
                    break;
                case "1 Hour Average":
                    Main.rm.config.setPricing("oneHour");
                    break;
                case "Best of Latest and 5 Minute Average":
                    Main.rm.config.setPricing("bestOfLatestFiveMinute");
                    break;
                case "Latest Bid Plus 1, Latest Ask Minus 1":
                    Main.rm.config.setPricing("latestPmOne");
                    break;
                case "5 Minute Average Bid Plus 1, 5 Minute Average Ask Minus 1":
                    Main.rm.config.setPricing("fiveMinutePmOne");
                    break;
                case "Best of Latest ±1 and 5 Minute Average ±1":
                    Main.rm.config.setPricing("bestOfLatestPmOneFiveMinutePmOne");
                    break;
            }
            Main.rm.items.setAllPricing();
            if (Main.rm.config.getAuto()) {
                Main.rm.config.setSelections(new LinkedHashSet<Integer>());
                Main.trading.Select();
            }
            setSelectionsTable();
        });
    }

    private void setSelectionsTable() {
        String[] columnNames = {"Name", "ID", "Members", "Restricted", "Bid", "Margin", "1hr Vol", "Target", "4hr Refresh"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Only allow editing for target vol
            }
        };
        for (int ID : Main.rm.config.getSelections()) {
            Items.Item item = Main.rm.items.get(ID);
            tableModel.addRow(new Object[]{
                    item.getMapping().getName(),
                    item.getId(),
                    item.getMapping().getMembers(),
                    Trading.getRestrictedIdSet().contains(item.getId()),
                    item.getBid(),
                    (int) Math.ceil(0.99 * item.getAsk() - item.getBid()),
                    item.getOneHour().getLowPriceVolume() + item.getOneHour().getHighPriceVolume(),
                    item.getTargetVol(),
                    (item.getFourHourLimit().getUsedLimit() == 0) ? "N/A" : (int) (240 - Duration.between(item.getFourHourLimit().getRefreshTime(), LocalDateTime.now()).toMinutes()) + " mins"}
            );
        }
        selectionsTable.setModel(tableModel);
    }

    private void setAddButton() {
        addButton.addActionListener(e -> {
            int ID = Main.rm.getIdFromString(addItemField.getText());
            if (ID != -1) {
                Main.rm.config.incrementSelections(ID);
                addItemField.setText("");
                setSelectionsTable();
            }
        });
    }

    private void setResetButton() {
        resetButton.addActionListener(e -> {
            Main.rm.config.setSelections(new LinkedHashSet<Integer>());
            setSelectionsTable();
        });
    }

    private void setGenerateButton() {
        generateButton.addActionListener(e -> {
            setConfigFromObjects();
            Main.rm.config.setSelections(new LinkedHashSet<Integer>());
            Main.trading.Select();
            Main.rm.items.updateAllFourLimit();
            setSelectionsTable();
        });
    }

    private void setStartButton() {
        startButton.addActionListener(e -> {
            setConfigFromObjects();
            Main.rm.session.setRunning(true);
            Main.rm.session.setTimer(new Timer());
            dispose();
        });
    }

    private void configureUI() {
        setTitle("humblePhlipper");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(contentPanel);
        pack();
        setLocationRelativeTo(Client.getCanvas());
        setVisible(true);
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.setMinimumSize(new Dimension(100, 100));
        contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        headerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        saveButton = new JButton();
        saveButton.setText("Save");
        headerPanel.add(saveButton, BorderLayout.EAST);
        configComboBox = new JComboBox();
        configComboBox.setEditable(false);
        configComboBox.setEnabled(true);
        headerPanel.add(configComboBox, BorderLayout.CENTER);
        mainPane = new JTabbedPane();
        mainPane.setEnabled(true);
        mainPane.setMinimumSize(new Dimension(69, 100));
        contentPanel.add(mainPane, BorderLayout.CENTER);
        paramsPanel = new JPanel();
        paramsPanel.setLayout(new GridBagLayout());
        paramsPanel.setToolTipText("");
        mainPane.addTab("Params", paramsPanel);
        miscellaneousPanel = new JPanel();
        miscellaneousPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        paramsPanel.add(miscellaneousPanel, gbc);
        miscellaneousPanel.setBorder(BorderFactory.createTitledBorder(null, "Miscellaneous", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        miscellaneousPanel.add(panel1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        maxBidVolField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(maxBidVolField, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        timeoutField = new JTextField();
        timeoutField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(timeoutField, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel4, gbc);
        sysExitCheckBox = new JCheckBox();
        sysExitCheckBox.setText("True");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(sysExitCheckBox, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel5, gbc);
        profitCutOffField = new JTextField();
        profitCutOffField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(profitCutOffField, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel6, gbc);
        maxBidValueField = new JTextField();
        maxBidValueField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(maxBidValueField, gbc);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        miscellaneousPanel.add(panel7, gbc);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel8, gbc);
        timeoutMinutesTextField = new JTextField();
        timeoutMinutesTextField.setEditable(false);
        timeoutMinutesTextField.setText("Timeout (Minutes)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel8.add(timeoutMinutesTextField, gbc);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel9, gbc);
        profitCutoffTextField = new JTextField();
        profitCutoffTextField.setEditable(false);
        profitCutoffTextField.setText("Profit Cutoff");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel9.add(profitCutoffTextField, gbc);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel10, gbc);
        maxVolumeOfAnyBidTextField = new JTextField();
        maxVolumeOfAnyBidTextField.setEditable(false);
        maxVolumeOfAnyBidTextField.setText("Max Volume of any Bid");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(maxVolumeOfAnyBidTextField, gbc);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel11, gbc);
        maxValueOfAnyBidTextField = new JTextField();
        maxValueOfAnyBidTextField.setEditable(false);
        maxValueOfAnyBidTextField.setText("Max Value of any Bid");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel11.add(maxValueOfAnyBidTextField, gbc);
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel12, gbc);
        closeClientOnStopTextField = new JTextField();
        closeClientOnStopTextField.setEditable(false);
        closeClientOnStopTextField.setText("Close Client on Stop");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel12.add(closeClientOnStopTextField, gbc);
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        paramsPanel.add(panel13, gbc);
        panel13.setBorder(BorderFactory.createTitledBorder(null, "Pricing", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        pricingComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel13.add(pricingComboBox, gbc);
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        paramsPanel.add(panel14, gbc);
        panel14.setBorder(BorderFactory.createTitledBorder(null, "Bid Priority", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel14.add(panel15, gbc);
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel15.add(panel16, gbc);
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel16.add(panel17, gbc);
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel17.add(panel18, gbc);
        priorityCapitalBindingSlider = new JSlider();
        priorityCapitalBindingSlider.setValue(50);
        panel18.add(priorityCapitalBindingSlider, BorderLayout.CENTER);
        final JPanel panel19 = new JPanel();
        panel19.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel17.add(panel19, gbc);
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setText("Weight");
        label1.setVerticalAlignment(0);
        label1.setVerticalTextPosition(0);
        panel19.add(label1, BorderLayout.CENTER);
        final JPanel panel20 = new JPanel();
        panel20.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel17.add(panel20, gbc);
        priorityVolSlider = new JSlider();
        priorityVolSlider.setValue(50);
        panel20.add(priorityVolSlider, BorderLayout.CENTER);
        final JPanel panel21 = new JPanel();
        panel21.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel17.add(panel21, gbc);
        priorityProfitSlider = new JSlider();
        priorityProfitSlider.setValue(50);
        panel21.add(priorityProfitSlider, BorderLayout.CENTER);
        final JPanel panel22 = new JPanel();
        panel22.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel16.add(panel22, gbc);
        final JPanel panel23 = new JPanel();
        panel23.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel22.add(panel23, gbc);
        capitalBindingVeTextField = new JTextField();
        capitalBindingVeTextField.setEditable(false);
        capitalBindingVeTextField.setText("Capital Binding (-ve)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel23.add(capitalBindingVeTextField, gbc);
        final JPanel panel24 = new JPanel();
        panel24.setLayout(new CardLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel22.add(panel24, gbc);
        final JLabel label2 = new JLabel();
        label2.setHorizontalAlignment(0);
        label2.setText("Ordinal Ranking");
        panel24.add(label2, "Card1");
        final JPanel panel25 = new JPanel();
        panel25.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel22.add(panel25, gbc);
        a1HourVolumeVeTextField = new JTextField();
        a1HourVolumeVeTextField.setEditable(false);
        a1HourVolumeVeTextField.setText("1 Hour Volume (+ve)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel25.add(a1HourVolumeVeTextField, gbc);
        final JPanel panel26 = new JPanel();
        panel26.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel22.add(panel26, gbc);
        profitMarginVeTextField = new JTextField();
        profitMarginVeTextField.setEditable(false);
        profitMarginVeTextField.setText("Profit Margin (+ve)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel26.add(profitMarginVeTextField, gbc);
        selectionsPanel = new JPanel();
        selectionsPanel.setLayout(new BorderLayout(0, 0));
        mainPane.addTab("Selections", selectionsPanel);
        selectionsPane = new JTabbedPane();
        selectionsPanel.add(selectionsPane, BorderLayout.NORTH);
        selectionsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        manualPanel = new JPanel();
        manualPanel.setLayout(new GridBagLayout());
        selectionsPane.addTab("Manual", manualPanel);
        manualPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel27 = new JPanel();
        panel27.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        manualPanel.add(panel27, gbc);
        resetButton = new JButton();
        resetButton.setText("Reset");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel27.add(resetButton, gbc);
        addButton = new JButton();
        addButton.setText("Add by Name or ID");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel27.add(addButton, gbc);
        final JPanel panel28 = new JPanel();
        panel28.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        manualPanel.add(panel28, gbc);
        addItemField = new JTextField();
        addItemField.setText("");
        panel28.add(addItemField, BorderLayout.CENTER);
        autoPanel = new JPanel();
        autoPanel.setLayout(new GridBagLayout());
        selectionsPane.addTab("Auto", autoPanel);
        final JPanel panel29 = new JPanel();
        panel29.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        autoPanel.add(panel29, gbc);
        final JPanel panel30 = new JPanel();
        panel30.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel29.add(panel30, gbc);
        panel30.setBorder(BorderFactory.createTitledBorder(null, "Max 1 Hour Bid/Vol Volume Ratio", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        maxBidAskVolRatioField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel30.add(maxBidAskVolRatioField, gbc);
        final JPanel panel31 = new JPanel();
        panel31.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel29.add(panel31, gbc);
        panel31.setBorder(BorderFactory.createTitledBorder(null, "Number of Items to Select", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        numToSelectSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel31.add(numToSelectSpinner, gbc);
        generateButton = new JButton();
        generateButton.setText("Generate");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel31.add(generateButton, gbc);
        final JPanel panel32 = new JPanel();
        panel32.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel29.add(panel32, gbc);
        panel32.setBorder(BorderFactory.createTitledBorder(null, "Max Bid Price", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        maxBidPriceField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel32.add(maxBidPriceField, gbc);
        final JPanel panel33 = new JPanel();
        panel33.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        autoPanel.add(panel33, gbc);
        final JPanel panel34 = new JPanel();
        panel34.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel33.add(panel34, gbc);
        panel34.setBorder(BorderFactory.createTitledBorder(null, "Min 1 Hour Volume", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        minVolField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel34.add(minVolField, gbc);
        final JPanel panel35 = new JPanel();
        panel35.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel33.add(panel35, gbc);
        panel35.setBorder(BorderFactory.createTitledBorder(null, "Min Margin", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        minMarginField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel35.add(minMarginField, gbc);
        final JPanel panel36 = new JPanel();
        panel36.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel33.add(panel36, gbc);
        panel36.setBorder(BorderFactory.createTitledBorder(null, "Restrictions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        membersCheckBox = new JCheckBox();
        membersCheckBox.setText("Members Account");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel36.add(membersCheckBox, gbc);
        tradeRestrictedCheckBox = new JCheckBox();
        tradeRestrictedCheckBox.setText("Trade Restricted");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel36.add(tradeRestrictedCheckBox, gbc);
        selectionsTableScrollPane = new JScrollPane();
        selectionsPanel.add(selectionsTableScrollPane, BorderLayout.CENTER);
        selectionsTable = new JTable();
        selectionsTableScrollPane.setViewportView(selectionsTable);
        savePanel = new JPanel();
        savePanel.setLayout(new BorderLayout(0, 0));
        contentPanel.add(savePanel, BorderLayout.SOUTH);
        startButton = new JButton();
        startButton.setText("Start");
        savePanel.add(startButton, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

    public void Dispose() {
        dispose();
    }

}


package humblePhlipper;

import humblePhlipper.Resources.SavedData.Config;
import humblePhlipper.Resources.SavedData.Trade;
import org.dreambot.api.Client;
import org.dreambot.api.settings.ScriptSettings;
import org.dreambot.api.utilities.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EndGUI extends JFrame {
    private JTabbedPane tabbedPane1;
    private JPanel contentPanel;
    private JComboBox historyComboBox;
    private JTextArea tradesTextArea;
    private JTextArea configTextArea;
    private JTextField profitPerHourTextField;
    private JTextField profitTextField;
    private JTextField tradingTimeField;
    private JScrollPane barChartScrollPane;
    private JTextArea selectionsTextArea;

    public EndGUI() {
        setHistoryComboBox();
        populateObjectsFromHistoryFile();
        configureUI();
    }

    private void setHistoryComboBox() {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();

        String historyPath = System.getProperty("scripts.path") + File.separator + "humblePhlipper" + File.separator + "History";
        File historyDirectory = new File(historyPath);
        File[] files = historyDirectory.listFiles();

        if (files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            for (File file : files) {
                // Add each file name to the JComboBox
                comboBoxModel.addElement(file.getName());
            }
        }
        historyComboBox.setModel(comboBoxModel);
        historyComboBox.addActionListener(e -> {
            populateObjectsFromHistoryFile();
        });
    }

    private void populateObjectsFromHistoryFile() {
        String file = (String) historyComboBox.getSelectedItem();
        Map sessionHistory = ScriptSettings.load(Map.class, "humblePhlipper", "History", file);
        String selectionsCSV = (String) sessionHistory.get("selectionsCSV");
        String tradesCSV = (String) sessionHistory.get("tradesCSV");
        String configJSON = (String) sessionHistory.get("configJSON");

        selectionsTextArea.setText(selectionsCSV);
        tradesTextArea.setText(tradesCSV);
        configTextArea.setText(configJSON);

        List<LocalDateTime> timeList = new ArrayList<>();
        Map<String, Double> itemProfitMap = new HashMap<>();
        Map<String, Integer> itemVolMap = new HashMap<>();
        Config config = Main.rm.gson.fromJson(configJSON, Config.class);
        for (int ID : config.getSelections()) {
            itemProfitMap.put(Main.rm.mappingMap.get(ID).getName(), 0.0);
            itemVolMap.put(Main.rm.mappingMap.get(ID).getName(), 0);
        }

        String[] trades = tradesCSV.split("\\n");
        for (int i = 1; i < trades.length; i++) {
            Trade trade = new Trade(trades[i]);
            itemProfitMap.merge(trade.getName(), trade.getPrice() * trade.getVol(), Double::sum);
            itemVolMap.merge(trade.getName(), trade.getVol(), Integer::sum);
            timeList.add(trade.getTime());
        }

        long runtime = (!timeList.isEmpty()) ? Duration.between(Collections.min(timeList), Collections.max(timeList)).toMinutes() : 0;
        double profit = (!itemProfitMap.isEmpty()) ? itemProfitMap.values().stream().mapToDouble(Double::doubleValue).sum() : 0;
        double profitPerHour = (runtime != 0) ? 60 * profit / runtime : 0;

        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        tradingTimeField.setText(runtime + " minutes");
        profitTextField.setText(decimalFormat.format(Math.round(profit)));
        profitPerHourTextField.setText(decimalFormat.format(Math.round(profitPerHour)));

        drawBarChart(itemProfitMap, itemVolMap);
    }

    private void drawBarChart(Map<String, Double> itemProfitMap, Map<String, Integer> itemVolMap) {
        Map<String, Double> sortedItemProfitMap = itemProfitMap.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                int barHeight = 20;
                int maxBarWidth = getWidth() - 200 - 10;
                int zeroProfitX = (int) (200 + maxBarWidth * (0 - Collections.min(sortedItemProfitMap.values())) / (Collections.max(sortedItemProfitMap.values()) - Collections.min(sortedItemProfitMap.values())));

                int y = 10;
                for (Map.Entry<String, Double> entry : sortedItemProfitMap.entrySet()) {
                    double profit = entry.getValue();
                    int barWidth = (int) (maxBarWidth * Math.abs(profit) / (Collections.max(sortedItemProfitMap.values()) - Collections.min(sortedItemProfitMap.values())));
                    int x = (int) ((profit > 0) ? zeroProfitX : (zeroProfitX - barWidth));


                    g2d.setColor((profit < 0) ? Color.RED : Color.GREEN);
                    g2d.fillRect(x, y, barWidth, barHeight);

                    DecimalFormat decimalFormat = new DecimalFormat("#,###");
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(entry.getKey() + ": " + itemVolMap.get(entry.getKey()) / 2 + " for " + decimalFormat.format(Math.round(entry.getValue())), 10, y + barHeight / 2);

                    y += barHeight + 10; // Adjust the space between bars
                }

                g2d.drawLine(zeroProfitX, 5, zeroProfitX, 10 + itemProfitMap.size() * (10 + 20) - 5);
            }
        };
        chartPanel.setPreferredSize(new Dimension(0, 10 + itemProfitMap.size() * (10 + 20)));
        barChartScrollPane.setViewportView(chartPanel);
    }

    private void configureUI() {
        setTitle("humblePhlipper");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        contentPanel.setPreferredSize(new Dimension(400, 700));
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
        tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Inspect", panel1);
        historyComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        historyComboBox.setModel(defaultComboBoxModel1);
        panel1.add(historyComboBox, BorderLayout.NORTH);
        final JTabbedPane tabbedPane2 = new JTabbedPane();
        panel1.add(tabbedPane2, BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        tabbedPane2.addTab("Summary", panel2);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        panel3.setEnabled(true);
        panel2.add(panel3, BorderLayout.NORTH);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel4, gbc);
        panel4.setBorder(BorderFactory.createTitledBorder(null, "Profit Per Hour", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        profitPerHourTextField = new JTextField();
        profitPerHourTextField.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(profitPerHourTextField, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel5, gbc);
        panel5.setBorder(BorderFactory.createTitledBorder(null, "Trading Time", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tradingTimeField = new JTextField();
        tradingTimeField.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(tradingTimeField, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel6, gbc);
        panel6.setBorder(BorderFactory.createTitledBorder(null, "Profit", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        profitTextField = new JTextField();
        profitTextField.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(profitTextField, gbc);
        barChartScrollPane = new JScrollPane();
        panel2.add(barChartScrollPane, BorderLayout.CENTER);
        final JScrollPane scrollPane1 = new JScrollPane();
        tabbedPane2.addTab("Config JSON", scrollPane1);
        configTextArea = new JTextArea();
        configTextArea.setEditable(false);
        scrollPane1.setViewportView(configTextArea);
        final JScrollPane scrollPane2 = new JScrollPane();
        tabbedPane2.addTab("Trades CSV", scrollPane2);
        tradesTextArea = new JTextArea();
        tradesTextArea.setEditable(false);
        scrollPane2.setViewportView(tradesTextArea);
        final JScrollPane scrollPane3 = new JScrollPane();
        tabbedPane2.addTab("Selections CSV", scrollPane3);
        selectionsTextArea = new JTextArea();
        selectionsTextArea.setEditable(false);
        scrollPane3.setViewportView(selectionsTextArea);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Analyse", panel7);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
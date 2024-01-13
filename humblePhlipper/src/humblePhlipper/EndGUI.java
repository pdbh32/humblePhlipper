package humblePhlipper;

import Jama.Matrix;

import humblePhlipper.regression.LinearRegression;
import humblePhlipper.regression.io.CSV;
import humblePhlipper.regression.io.DM;
import humblePhlipper.regression.io.Models;
import humblePhlipper.regression.io.Regressors;
import humblePhlipper.resources.savedData.Config;
import humblePhlipper.resources.savedData.Trade;
import org.dreambot.api.Client;
import org.dreambot.api.settings.ScriptSettings;

import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EndGUI extends JFrame {
    private List<Integer> modelsList = Models.getList();
    private String[] regressorsArray = Regressors.getArray();
    private String regressorsCSV = Regressors.getCSV();
    private DecimalFormat commaFormat = new DecimalFormat("#,###");
    private DecimalFormat fourDpFormat = new DecimalFormat("#.####");
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
    private JTable regressionTable;
    private JTextField totalProfitTextField;
    private JTextField totalRuntimeTextField;
    private JTextField totalProfitPerHourTextField;
    private JTextField numberOfSessionsTextField;
    private JComboBox seComboBox;
    private JTabbedPane tabbedPane2;
    private JTextArea yTextArea;
    private JTextArea xTextArea;
    private JTextArea rCodeTextArea;
    private JTabbedPane tabbedPane3;
    private JTextField OLSTextField;
    private JCheckBox finiteCorrectionCheckbox;
    private JTabbedPane tabbedPane4;
    private JTextArea stataCodeTextArea;

    public EndGUI() {
        setHistoryComboBox();
        populateInspectionObjectsFromHistoryFile();
        populateAnalysis();
        setSeComboBox();
        setFiniteCorrectionCheckbox();
        runRegressions();
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
                comboBoxModel.addElement(file.getName());
            }
        }
        historyComboBox.setModel(comboBoxModel);
        historyComboBox.addActionListener(e -> {
            populateInspectionObjectsFromHistoryFile();
        });
    }

    private void populateInspectionObjectsFromHistoryFile() {
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

        tradingTimeField.setText(runtime + " minutes");
        profitTextField.setText(commaFormat.format(Math.round(profit)));
        profitPerHourTextField.setText(commaFormat.format(Math.round(profitPerHour)));

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

    private void populateAnalysis() {
        try {
            DM dm = new DM(Collections.max(modelsList));
            totalProfitTextField.setText(commaFormat.format(Math.round(dm.cumProfit)));
            totalRuntimeTextField.setText(commaFormat.format(Math.round(dm.cumRuntimeHours)));
            totalProfitPerHourTextField.setText(commaFormat.format(Math.round(dm.cumProfit / dm.cumRuntimeHours)));
            numberOfSessionsTextField.setText(commaFormat.format(dm.Y.getRowDimension()));
            yTextArea.setText(CSV.toCSV(dm.Y, false, false, true));
            xTextArea.setText(CSV.toCSV(dm.X, false, true, false));
            rCodeTextArea.setText("Y <- c(\n" + CSV.toCSV(dm.Y, true, false, false) + ")" +
                    "\n" +
                    "\nX <- matrix(c(\n" + CSV.toCSV(dm.X, true, false, false) + ")," +
                    "\nnrow = " + dm.X.getRowDimension() + ", ncol = " + dm.X.getColumnDimension() + ", byrow = TRUE," +
                    "\ndimnames = list(NULL, c(" + regressorsCSV + ")))" +
                    "\n" +
                    "\nmodel <- lm(Y ~ X[, 2:5])" +
                    "\nsummary(model)");
        } catch (Exception ignored) {
        }
    }

    private void runRegressions() {
        boolean white = ("Assume Heteroskedasticity (White, 1980)".equals(seComboBox.getSelectedItem())) ? true : false;
        boolean finiteCorrection = finiteCorrectionCheckbox.isSelected();

        List<LinearRegression> listLr = new ArrayList<>();

        try {
            for (int k : modelsList) {
                DM dm = new DM(k);
                listLr.add(new LinearRegression(dm.Y, dm.X, finiteCorrection));
            }
        } catch (Exception ignored) {
            return;
        }

        DefaultTableModel model = new DefaultTableModel(0, 1 + modelsList.size()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Rows `k` are regressorsArray
        // Columns `m` are regressor and modelsList
        for (int k = 0; k < Collections.max(modelsList) + 1; k++) {
            List<Object> row = new ArrayList<>();
            row.add(regressorsArray[k]);
            for (int m = 0; m < modelsList.size(); m++) {
                LinearRegression lr = listLr.get(m);

                if (k > lr.k) {
                    row.add("");
                    continue;
                }

                if (lr.BetaHat == null) {
                    row.add("Singular X");
                    continue;
                }

                Matrix B = lr.BetaHat;
                Matrix O = (white) ? lr.WhiteOmegaHat : lr.OmegaHat;

                String bSe = commaFormat.format(B.get(k, 0)) + lr.calcSigStar(k, white);
                bSe += " (" + commaFormat.format(Math.sqrt(O.get(k, k))) + ")";
                row.add(bSe);
            }
            model.addRow(row.toArray());
        }

        List<Object> R2row = new ArrayList<>();
        R2row.add("R^2");
        for (int m = 0; m < modelsList.size(); m++) {
            LinearRegression lr = listLr.get(m);

            if (lr.BetaHat == null) {
                R2row.add("Singular X");
                continue;
            }

            R2row.add(fourDpFormat.format(lr.R2));
        }
        model.addRow(R2row.toArray());

        List<Object> AdjR2row = new ArrayList<>();
        AdjR2row.add("Adjusted R^2");
        for (int m = 0; m < modelsList.size(); m++) {
            LinearRegression lr = listLr.get(m);

            if (lr.BetaHat == null) {
                AdjR2row.add("Singular X");
                continue;
            }

            AdjR2row.add(fourDpFormat.format(lr.AdjR2));
        }
        model.addRow(AdjR2row.toArray());

        List<Object> FstatRow = new ArrayList<>();
        FstatRow.add("F-statistic");
        FstatRow.add("N/A");
        for (int m = 1; m < modelsList.size(); m++) {
            LinearRegression lr = listLr.get(m);

            if (lr.BetaHat == null) {
                AdjR2row.add("Singular X");
                continue;
            }

            FstatRow.add(fourDpFormat.format(lr.calcFstat()));
        }
        model.addRow(FstatRow.toArray());

        regressionTable.setModel(model);
    }

    private void setSeComboBox() {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxModel.addElement("Assume Homosckedasticity");
        comboBoxModel.addElement("Assume Heteroskedasticity (White, 1980)");
        seComboBox.setModel(comboBoxModel);
        seComboBox.addActionListener(e -> {
            runRegressions();
        });
    }

    private void setFiniteCorrectionCheckbox() {
        finiteCorrectionCheckbox.setSelected(true);
        finiteCorrectionCheckbox.addActionListener(e -> {
            runRegressions();
        });
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
        final JTabbedPane tabbedPane5 = new JTabbedPane();
        panel1.add(tabbedPane5, BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        tabbedPane5.addTab("Summary", panel2);
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
        tabbedPane5.addTab("Config JSON", scrollPane1);
        configTextArea = new JTextArea();
        configTextArea.setEditable(false);
        scrollPane1.setViewportView(configTextArea);
        final JScrollPane scrollPane2 = new JScrollPane();
        tabbedPane5.addTab("Trades CSV", scrollPane2);
        tradesTextArea = new JTextArea();
        tradesTextArea.setEditable(false);
        scrollPane2.setViewportView(tradesTextArea);
        final JScrollPane scrollPane3 = new JScrollPane();
        tabbedPane5.addTab("Selections CSV", scrollPane3);
        selectionsTextArea = new JTextArea();
        selectionsTextArea.setEditable(false);
        scrollPane3.setViewportView(selectionsTextArea);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Analyse", panel7);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridBagLayout());
        panel8.setEnabled(true);
        panel7.add(panel8, BorderLayout.NORTH);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(panel9, gbc);
        panel9.setBorder(BorderFactory.createTitledBorder(null, "Profit", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalProfitTextField = new JTextField();
        totalProfitTextField.setEditable(false);
        panel9.add(totalProfitTextField, BorderLayout.CENTER);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(panel10, gbc);
        panel10.setBorder(BorderFactory.createTitledBorder(null, "Number of Sessions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        numberOfSessionsTextField = new JTextField();
        numberOfSessionsTextField.setEditable(false);
        panel10.add(numberOfSessionsTextField, BorderLayout.CENTER);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(panel11, gbc);
        panel11.setBorder(BorderFactory.createTitledBorder(null, "Runtime (Hours)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalRuntimeTextField = new JTextField();
        totalRuntimeTextField.setEditable(false);
        panel11.add(totalRuntimeTextField, BorderLayout.CENTER);
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(panel12, gbc);
        panel12.setBorder(BorderFactory.createTitledBorder(null, "Profit per Hour", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalProfitPerHourTextField = new JTextField();
        totalProfitPerHourTextField.setEditable(false);
        panel12.add(totalProfitPerHourTextField, BorderLayout.CENTER);
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new BorderLayout(0, 0));
        panel7.add(panel13, BorderLayout.CENTER);
        tabbedPane3 = new JTabbedPane();
        panel13.add(tabbedPane3, BorderLayout.CENTER);
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new BorderLayout(0, 0));
        tabbedPane3.addTab("Regression", panel14);
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridBagLayout());
        panel14.add(panel15, BorderLayout.NORTH);
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel15.add(panel16, gbc);
        panel16.setBorder(BorderFactory.createTitledBorder(null, "Model", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        OLSTextField = new JTextField();
        OLSTextField.setEditable(false);
        OLSTextField.setText("OLS");
        panel16.add(OLSTextField, BorderLayout.CENTER);
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel15.add(panel17, gbc);
        panel17.setBorder(BorderFactory.createTitledBorder(null, "Finite Sample Correction", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        finiteCorrectionCheckbox = new JCheckBox();
        finiteCorrectionCheckbox.setText("True");
        panel17.add(finiteCorrectionCheckbox, BorderLayout.CENTER);
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel15.add(panel18, gbc);
        panel18.setBorder(BorderFactory.createTitledBorder(null, "Standard Errors", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        seComboBox = new JComboBox();
        panel18.add(seComboBox, BorderLayout.CENTER);
        regressionTable = new JTable();
        panel14.add(regressionTable, BorderLayout.CENTER);
        tabbedPane2 = new JTabbedPane();
        tabbedPane3.addTab("Inputs", tabbedPane2);
        final JScrollPane scrollPane4 = new JScrollPane();
        tabbedPane2.addTab("Y CSV", scrollPane4);
        yTextArea = new JTextArea();
        yTextArea.setEditable(false);
        scrollPane4.setViewportView(yTextArea);
        final JScrollPane scrollPane5 = new JScrollPane();
        tabbedPane2.addTab("X CSV", scrollPane5);
        xTextArea = new JTextArea();
        xTextArea.setEditable(false);
        scrollPane5.setViewportView(xTextArea);
        tabbedPane4 = new JTabbedPane();
        tabbedPane3.addTab("Replication", tabbedPane4);
        final JScrollPane scrollPane6 = new JScrollPane();
        tabbedPane4.addTab("R (4.2.2) Code", scrollPane6);
        rCodeTextArea = new JTextArea();
        rCodeTextArea.setEditable(false);
        scrollPane6.setViewportView(rCodeTextArea);
        final JPanel panel19 = new JPanel();
        panel19.setLayout(new BorderLayout(0, 0));
        tabbedPane4.addTab("Stata (13) Code", panel19);
        stataCodeTextArea = new JTextArea();
        stataCodeTextArea.setEditable(false);
        stataCodeTextArea.setText("");
        panel19.add(stataCodeTextArea, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
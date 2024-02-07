package humblePhlipper;

import Jama.Matrix;

import humblePhlipper.regression.LinearRegression;
import humblePhlipper.regression.distributions.F;
import humblePhlipper.regression.distributions.T;
import humblePhlipper.regression.io.CSV;
import humblePhlipper.regression.io.DM;
import humblePhlipper.regression.io.Models;
import humblePhlipper.regression.io.Regressors;
import humblePhlipper.resources.data.Config;
import humblePhlipper.resources.data.Trades;
import org.dreambot.api.Client;
import org.dreambot.api.settings.ScriptSettings;

import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EndGUI extends JFrame {
    private final File historyDirectory = new File(System.getProperty("scripts.path") + File.separator + "humblePhlipper" + File.separator + "History");
    private final List<Integer> modelsList = Models.getList();
    private final String[] regressorsArray = Regressors.getArray();
    private final String regressorsCSV = Regressors.getCSV();
    private final DecimalFormat commaFormat = new DecimalFormat("#,###");
    private final DecimalFormat fourDpFormat = new DecimalFormat("#.####");
    private DM dm; // Design matrices including *all coefficients*
    private JTabbedPane tabbedPane1;
    private JPanel contentPanel;
    private JComboBox historyComboBox;
    private JTextArea tradesTextArea;
    private JTextArea configTextArea;
    private JTextField profitPerHourTextField;
    private JTextField profitTextField;
    private JTextField runtimeTextField;
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
    private JCheckBox finiteCorrectionCheckbox;
    private JTabbedPane tabbedPane4;
    private JTextArea stataCodeTextArea;
    private JTextArea omissionsTextArea;
    private JTextField errorsTextField;
    private JTextField totalTaxTextField;
    private JTextField totalTradesTextField;
    private JComboBox modelComboBox;

    public EndGUI() {
        // Inspection
        setHistoryComboBox();
        populateInspectionObjectsFromHistoryFile();

        // Analysis
        assembleData();
        populateAnalysis();
        setModelComboBox();
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

        Trades trades = new Trades(tradesCSV);
        Trades.Summary allSummary = trades.summarise();

        errorsTextField.setText(trades.getError());
        runtimeTextField.setText(Math.round(allSummary.runtimeHours * 60) + " minutes");
        profitTextField.setText(commaFormat.format(Math.round(allSummary.profit)));
        profitPerHourTextField.setText(commaFormat.format(Math.round(allSummary.profit / allSummary.runtimeHours)));

        Map<String, Trades> tradesMap = trades.splitByName();
        Map<String, Double> itemProfitMap = new HashMap<>();
        Map<String, Integer> itemVolMap = new HashMap<>();

        Config config = Main.rm.gson.fromJson(configJSON, Config.class);
        for (int ID : config.getSelections()) {
            itemProfitMap.put(Main.rm.mappingMap.get(ID).getName(), 0.0);
            itemVolMap.put(Main.rm.mappingMap.get(ID).getName(), 0);
        }

        for (Map.Entry<String, Trades> entry : tradesMap.entrySet()) {
            Trades.Summary itemSummary = entry.getValue().summarise();
            itemProfitMap.merge(entry.getKey(), itemSummary.profit, Double::sum);
            itemVolMap.merge(entry.getKey(), itemSummary.vol, Integer::sum);
        }

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

    private void assembleData() {
        File[] files = historyDirectory.listFiles();
        if (files == null) {
            return;
        }
        dm = new DM(files, Collections.max(modelsList));
    }

    private void populateAnalysis() {
        if (dm == null) {
            return;
        }
        totalTaxTextField.setText(commaFormat.format(dm.cumTax));
        totalTradesTextField.setText(commaFormat.format(dm.cumTrades));
        totalProfitTextField.setText(commaFormat.format(Math.round(dm.cumProfit)));
        totalRuntimeTextField.setText(commaFormat.format(Math.round(dm.cumRuntimeHours)));
        totalProfitPerHourTextField.setText(commaFormat.format(Math.round(dm.cumProfit / dm.cumRuntimeHours)));
        numberOfSessionsTextField.setText(commaFormat.format(dm.Y.getRowDimension()));
        omissionsTextArea.setText(dm.errors);
        yTextArea.setText(CSV.toCSV(dm.Y, false, false, true));
        xTextArea.setText(CSV.toCSV(dm.X, false, true, false));
        rCodeTextArea.setText("Y <- c(\n" + CSV.toCSV(dm.Y, true, false, false) + ")" +
                "\n" +
                "\nX <- matrix(c(\n" + CSV.toCSV(dm.X, true, false, false) + ")," +
                "\nnrow = " + dm.X.getRowDimension() + ", ncol = " + dm.X.getColumnDimension() + ", byrow = TRUE," +
                "\ndimnames = list(NULL, c(" + regressorsCSV + ")))" +
                "\n" +
                "\n#OLS assuming homoskedasticity with finite sample correction" +
                "\nols <- lm(Y ~ X[, 2:5])" +
                "\nsummary(ols)" +
                "\n" +
                "\n#WLS assuming homoskedasticity with finite sample correction" +
                "\nwls <- lm(Y ~ X[, 2:5], weights=X[, 2])" +
                "\nsummary(wls)" +
                "\n" +
                "\n#OLS assuming heteroskedasticity (White, 1980) without finite sample correction" +
                "\nlibrary(lmtest)" +
                "\nlibrary(sandwich)" +
                "\ncoeftest(ols, vcov = vcovHC(ols, type = 'HC0'))");
    }

    private void runRegressions() {
        boolean WLS = "(Runtime) WLS".equals(modelComboBox.getSelectedItem());
        boolean white = "Assume Heteroskedasticity (White, 1980)".equals(seComboBox.getSelectedItem());
        boolean finiteCorrection = finiteCorrectionCheckbox.isSelected();

        List<LinearRegression> listLr = new ArrayList<>();

        try {
            for (int k : modelsList) {
                Matrix I = Matrix.identity(dm.X.getColumnDimension(), k + 1);
                Matrix X = dm.X.times(I);
                LinearRegression lr = new LinearRegression(dm.Y, X);
                if (WLS) lr.WLS(dm.Weights);
                else lr.OLS();
                if (white) lr.WhiteOmegaHat();
                else lr.OmegaHat();
                if (finiteCorrection) lr.FiniteCorrection();
                listLr.add(lr);
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

        // Rows `k` are regressors
        // Columns `m` are models (k = 0, k = 2, ...)
        for (int k = 0; k < Collections.max(modelsList) + 1; k++) {
            List<Object> row = new ArrayList<>();
            row.add(regressorsArray[k]);
            for (int m = 0; m < modelsList.size(); m++) {
                LinearRegression lr = listLr.get(m);

                if (k > lr.k) {
                    row.add("");
                    continue;
                }

                if (lr.n < lr.k + 1) {
                    row.add("n < k + 1");
                    continue;
                }

                if (lr.B == null) {
                    row.add("Singular X");
                    continue;
                }

                double b = lr.B.get(k, 0);
                double se = Math.sqrt(lr.O.get(k, k));
                double t = b / se;

                String bSe = commaFormat.format(b) + T.calcSigStar(t, lr.n - 1);
                bSe += " (" + commaFormat.format(se) + ")";
                row.add(bSe);
            }
            model.addRow(row.toArray());
        }

        List<Object> R2row = new ArrayList<>();
        R2row.add("R^2");
        for (int m = 0; m < modelsList.size(); m++) {
            LinearRegression lr = listLr.get(m);

            if (lr.n < lr.k + 1) {
                R2row.add("n < k + 1");
                continue;
            }

            if (lr.B == null) {
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

            if (lr.n < lr.k + 1) {
                AdjR2row.add("n < k + 1");
                continue;
            }

            if (lr.B == null) {
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

            if (lr.n < lr.k + 1) {
                FstatRow.add("n < k + 1");
                continue;
            }

            if (lr.B == null) {
                FstatRow.add("Singular X");
                continue;
            }

            FstatRow.add(fourDpFormat.format(lr.F) + F.calcSigStar(lr.F, lr.k, lr.n - lr.k - 1));
        }
        model.addRow(FstatRow.toArray());

        regressionTable.setModel(model);
    }

    private void setModelComboBox() {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxModel.addElement("OLS");
        comboBoxModel.addElement("(Runtime) WLS");
        modelComboBox.setModel(comboBoxModel);
        modelComboBox.addActionListener(e -> {
            runRegressions();
        });
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
        contentPanel.setPreferredSize(new Dimension(500, 700));
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
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel3, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel4, gbc);
        panel4.setBorder(BorderFactory.createTitledBorder(null, "Errors", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        errorsTextField = new JTextField();
        errorsTextField.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(errorsTextField, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel5, gbc);
        panel5.setBorder(BorderFactory.createTitledBorder(null, "History File", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        historyComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        historyComboBox.setModel(defaultComboBoxModel1);
        panel5.add(historyComboBox, BorderLayout.CENTER);
        final JTabbedPane tabbedPane5 = new JTabbedPane();
        panel1.add(tabbedPane5, BorderLayout.CENTER);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(0, 0));
        tabbedPane5.addTab("Summary", panel6);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        panel7.setEnabled(true);
        panel6.add(panel7, BorderLayout.NORTH);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel8, gbc);
        panel8.setBorder(BorderFactory.createTitledBorder(null, "Profit Per Hour", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        profitPerHourTextField = new JTextField();
        profitPerHourTextField.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel8.add(profitPerHourTextField, gbc);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel9, gbc);
        panel9.setBorder(BorderFactory.createTitledBorder(null, "Runtime (Minutes)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        runtimeTextField = new JTextField();
        runtimeTextField.setEditable(false);
        runtimeTextField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel9.add(runtimeTextField, gbc);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel10, gbc);
        panel10.setBorder(BorderFactory.createTitledBorder(null, "Profit", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        profitTextField = new JTextField();
        profitTextField.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(profitTextField, gbc);
        barChartScrollPane = new JScrollPane();
        panel6.add(barChartScrollPane, BorderLayout.CENTER);
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
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Analyse", panel11);
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridBagLayout());
        panel12.setEnabled(true);
        panel11.add(panel12, BorderLayout.NORTH);
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel12.add(panel13, gbc);
        panel13.setBorder(BorderFactory.createTitledBorder(null, "Profit", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalProfitTextField = new JTextField();
        totalProfitTextField.setEditable(false);
        panel13.add(totalProfitTextField, BorderLayout.CENTER);
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel12.add(panel14, gbc);
        panel14.setBorder(BorderFactory.createTitledBorder(null, "Sessions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        numberOfSessionsTextField = new JTextField();
        numberOfSessionsTextField.setEditable(false);
        panel14.add(numberOfSessionsTextField, BorderLayout.CENTER);
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel12.add(panel15, gbc);
        panel15.setBorder(BorderFactory.createTitledBorder(null, "Hours", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalRuntimeTextField = new JTextField();
        totalRuntimeTextField.setEditable(false);
        panel15.add(totalRuntimeTextField, BorderLayout.CENTER);
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel12.add(panel16, gbc);
        panel16.setBorder(BorderFactory.createTitledBorder(null, "Profit/Hour", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalProfitPerHourTextField = new JTextField();
        totalProfitPerHourTextField.setEditable(false);
        panel16.add(totalProfitPerHourTextField, BorderLayout.CENTER);
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel12.add(panel17, gbc);
        panel17.setBorder(BorderFactory.createTitledBorder(null, "Tax", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalTaxTextField = new JTextField();
        totalTaxTextField.setEditable(false);
        panel17.add(totalTaxTextField, BorderLayout.CENTER);
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel12.add(panel18, gbc);
        panel18.setBorder(BorderFactory.createTitledBorder(null, "Trades", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        totalTradesTextField = new JTextField();
        totalTradesTextField.setEditable(false);
        panel18.add(totalTradesTextField, BorderLayout.CENTER);
        final JPanel panel19 = new JPanel();
        panel19.setLayout(new BorderLayout(0, 0));
        panel11.add(panel19, BorderLayout.CENTER);
        tabbedPane3 = new JTabbedPane();
        panel19.add(tabbedPane3, BorderLayout.CENTER);
        final JPanel panel20 = new JPanel();
        panel20.setLayout(new BorderLayout(0, 0));
        tabbedPane3.addTab("Regression", panel20);
        final JPanel panel21 = new JPanel();
        panel21.setLayout(new GridBagLayout());
        panel20.add(panel21, BorderLayout.NORTH);
        final JPanel panel22 = new JPanel();
        panel22.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel21.add(panel22, gbc);
        panel22.setBorder(BorderFactory.createTitledBorder(null, "Model", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        modelComboBox = new JComboBox();
        panel22.add(modelComboBox, BorderLayout.CENTER);
        final JPanel panel23 = new JPanel();
        panel23.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel21.add(panel23, gbc);
        panel23.setBorder(BorderFactory.createTitledBorder(null, "Finite Sample Correction", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        finiteCorrectionCheckbox = new JCheckBox();
        finiteCorrectionCheckbox.setText("True");
        panel23.add(finiteCorrectionCheckbox, BorderLayout.CENTER);
        final JPanel panel24 = new JPanel();
        panel24.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel21.add(panel24, gbc);
        panel24.setBorder(BorderFactory.createTitledBorder(null, "Standard Errors", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        seComboBox = new JComboBox();
        panel24.add(seComboBox, BorderLayout.CENTER);
        regressionTable = new JTable();
        panel20.add(regressionTable, BorderLayout.CENTER);
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
        final JScrollPane scrollPane6 = new JScrollPane();
        tabbedPane2.addTab("Omissions", scrollPane6);
        omissionsTextArea = new JTextArea();
        omissionsTextArea.setEditable(false);
        scrollPane6.setViewportView(omissionsTextArea);
        tabbedPane4 = new JTabbedPane();
        tabbedPane3.addTab("Replication", tabbedPane4);
        final JScrollPane scrollPane7 = new JScrollPane();
        tabbedPane4.addTab("R (4.2.2) Code", scrollPane7);
        rCodeTextArea = new JTextArea();
        rCodeTextArea.setEditable(false);
        scrollPane7.setViewportView(rCodeTextArea);
        final JPanel panel25 = new JPanel();
        panel25.setLayout(new BorderLayout(0, 0));
        tabbedPane4.addTab("Stata (13) Code", panel25);
        stataCodeTextArea = new JTextArea();
        stataCodeTextArea.setEditable(false);
        stataCodeTextArea.setText("");
        panel25.add(stataCodeTextArea, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
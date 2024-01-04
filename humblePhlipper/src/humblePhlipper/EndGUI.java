package humblePhlipper;

import org.dreambot.api.Client;
import org.dreambot.api.utilities.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Paint;
import java.text.DecimalFormat;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;


public class EndGUI extends JFrame {
    private JTabbedPane tabbedPane1;
    private JPanel contentPanel;
    private JTextArea csvTextArea;
    private JTextArea configTextArea;
    private JPanel lineGraphPanel;
    private JScrollPane breakdownScrollPane;
    private JTextField runtimeTextField;
    private JTextField startingGpTextField;
    private JTextField profitTextField;
    private JTextField profitPerHourTextField;

    public EndGUI() {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        runtimeTextField.setText(String.valueOf(Main.rm.session.getTimer().elapsed() / 60000) + " minutes");
        startingGpTextField.setText(String.valueOf((Main.rm.session.getStartingGp() != null) ? decimalFormat.format(Main.rm.session.getStartingGp()) : "unknown"));
        profitTextField.setText(String.valueOf(decimalFormat.format(Main.rm.session.getProfit())));
        profitPerHourTextField.setText(String.valueOf(decimalFormat.format(100 * Main.rm.session.getProfit() / Main.rm.session.getTimer().elapsed())));
        lineGraphPanel.add(createLineGraph(), BorderLayout.CENTER);
        breakdownScrollPane.setViewportView(createBarChart());
        configTextArea.setText(Main.rm.getConfigString());
        csvTextArea.setText(Main.rm.session.getHistoryCSV());
        configureUI();
    }

    private ChartPanel createLineGraph() {
        XYSeries series = new XYSeries("profit");
        for (Map.Entry<Long, Double> entry : Main.rm.session.getTimeCumProfitMap().entrySet()) {
            series.add((double) entry.getKey() / 60000, entry.getValue());
        }
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Cumulative Profit over Time",
                "Minutes",
                "Profit",
                new XYSeriesCollection(series)
        );

        long maxTime = Main.rm.session.getTimeCumProfitMap().lastKey();

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainAxis(new NumberAxis());
        plot.getDomainAxis().setRange(0, (double) Main.rm.session.getTimeCumProfitMap().lastKey() / 60000);
        plot.setRangeAxis(new NumberAxis());
        plot.getRangeAxis().setRange(Collections.min(Main.rm.session.getTimeCumProfitMap().values()), Collections.max(Main.rm.session.getTimeCumProfitMap().values()));

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);

        chart.getLegend().setVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));

        return chartPanel;
    }

    private ChartPanel createBarChart() {
        Map<String, Integer> itemProfitMap = new HashMap<>();
        for (Integer ID : Main.rm.config.getSelections()) {
            itemProfitMap.put(Main.rm.items.get(ID).getMapping().getName(), (int) Main.rm.items.get(ID).getProfit());
        }
        Logger.log(itemProfitMap);

        // Create a list of entries and sort it based on values
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(itemProfitMap.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            dataset.addValue(entry.getValue(), "Profit", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Profit by Item",
                "Item",
                "Profit",
                dataset,
                PlotOrientation.HORIZONTAL, // Set the orientation to vertical
                false,
                false,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setDomainAxis(new CategoryAxis());
        plot.setRangeAxis(new NumberAxis());
        plot.getRangeAxis().setRange(Collections.min(itemProfitMap.values()), Collections.max(itemProfitMap.values()));

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                double value = dataset.getValue(row, column).doubleValue();
                return (value > 0) ? Color.GREEN : Color.RED;
            }
        };
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(300, 400));

        return chartPanel;
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
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setEnabled(true);
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Summary", panel1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel3, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel4, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Profit/hour");
        panel4.add(label1, BorderLayout.CENTER);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel5, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Runtime");
        panel5.add(label2, BorderLayout.WEST);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel6, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Profit");
        panel6.add(label3, BorderLayout.CENTER);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel7, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Starting gp");
        panel7.add(label4, BorderLayout.CENTER);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel8, gbc);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(panel9, gbc);
        profitPerHourTextField = new JTextField();
        profitPerHourTextField.setEditable(false);
        panel9.add(profitPerHourTextField, BorderLayout.CENTER);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(panel10, gbc);
        runtimeTextField = new JTextField();
        runtimeTextField.setEditable(false);
        panel10.add(runtimeTextField, BorderLayout.CENTER);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(panel11, gbc);
        profitTextField = new JTextField();
        profitTextField.setEditable(false);
        panel11.add(profitTextField, BorderLayout.CENTER);
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(panel12, gbc);
        startingGpTextField = new JTextField();
        startingGpTextField.setEditable(false);
        panel12.add(startingGpTextField, BorderLayout.CENTER);
        lineGraphPanel = new JPanel();
        lineGraphPanel.setLayout(new BorderLayout(0, 0));
        panel1.add(lineGraphPanel, BorderLayout.CENTER);
        breakdownScrollPane = new JScrollPane();
        tabbedPane1.addTab("Breakdown", breakdownScrollPane);
        final JScrollPane scrollPane1 = new JScrollPane();
        tabbedPane1.addTab("Config", scrollPane1);
        configTextArea = new JTextArea();
        configTextArea.setEditable(false);
        scrollPane1.setViewportView(configTextArea);
        final JScrollPane scrollPane2 = new JScrollPane();
        tabbedPane1.addTab("CSV", scrollPane2);
        csvTextArea = new JTextArea();
        csvTextArea.setEditable(false);
        scrollPane2.setViewportView(csvTextArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}

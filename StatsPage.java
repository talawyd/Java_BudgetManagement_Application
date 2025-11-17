import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class StatsPage extends JFrame {
    private String loggedInUsername;
    private int userId;
    private Connection conn;
    private JComboBox<String> filterComboBox;
    private JComboBox<String> goalsComboBox;
    private JPanel chartPanel;
    private static final Color[] CHART_COLORS = {
        new Color(65, 105, 225),  // Royal Blue
        new Color(220, 20, 60),    // Crimson Red
        new Color(34, 139, 34),    // Forest Green
        new Color(255, 140, 0)     // Dark Orange
    };

    public StatsPage(String username, int userId) {
        this.loggedInUsername = username;
        this.userId = userId;
        initializeDatabaseConnection();
        setupUI();
    }

    private void initializeDatabaseConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            dispose();
        }
    }

    private void setupUI() {
        setTitle("Financial Dashboard - " + loggedInUsername);
        setSize(1100, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createControlPanel(), BorderLayout.CENTER);
        loadDefaultView();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton backButton = new JButton("← Back to Dashboard");
        backButton.addActionListener(e -> {
            new DashboardPage(loggedInUsername, userId).setVisible(true);
            dispose();
        });

        JLabel title = new JLabel("📊 Financial Analytics Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        header.add(backButton, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        return header;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Data"));

        filterComboBox = new JComboBox<>(new String[]{"All", "Expenses", "Goals", "Yearly Savings"});
        filterComboBox.setPreferredSize(new Dimension(150, 30));
        filterComboBox.addActionListener(e -> updateChartView());

        goalsComboBox = new JComboBox<>();
        goalsComboBox.setPreferredSize(new Dimension(200, 30));
        goalsComboBox.setVisible(false);
        goalsComboBox.addActionListener(e -> updateGoalChart());

        filterPanel.add(new JLabel("View:"));
        filterPanel.add(filterComboBox);
        filterPanel.add(new JLabel("Select Goal:"));
        filterPanel.add(goalsComboBox);

        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }

    private void loadDefaultView() {
        chartPanel.removeAll();
        chartPanel.add(new SummaryPieChartPanel(), BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void updateChartView() {
        String selected = (String) filterComboBox.getSelectedItem();
        goalsComboBox.setVisible("Goals".equals(selected));

        chartPanel.removeAll();

        switch (selected) {
            case "All":
                chartPanel.add(new SummaryPieChartPanel(), BorderLayout.CENTER);
                break;
            case "Expenses":
                chartPanel.add(new ExpensesPieChartPanel(), BorderLayout.CENTER);
                break;
            case "Goals":
                loadGoalsComboBox();
                if (goalsComboBox.getItemCount() > 0) {
                    updateGoalChart();
                } else {
                    chartPanel.add(new JLabel("No goals available", SwingConstants.CENTER), BorderLayout.CENTER);
                }
                break;
            case "Yearly Savings":
                chartPanel.add(new YearlySavingsChartPanel(), BorderLayout.CENTER);
                break;
        }

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void loadGoalsComboBox() {
        goalsComboBox.removeAllItems();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT goalname FROM goals WHERE id = ? ORDER BY goalname");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                goalsComboBox.addItem(rs.getString("goalname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateGoalChart() {
        String selectedGoal = (String) goalsComboBox.getSelectedItem();
        if (selectedGoal != null) {
            chartPanel.removeAll();
            chartPanel.add(new GoalProgressPieChartPanel(selectedGoal), BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
        }
    }

    class SummaryPieChartPanel extends JPanel {
        private Map<String, Double> data = new LinkedHashMap<>();

        public SummaryPieChartPanel() {
            setBackground(Color.WHITE);
            loadData();
        }

        private void loadData() {
            try {
                double expenses = getTotalExpenses();
                double goalsPaid = getGoalsPaid();
                double salary = getSalary(); // Get salary from userinfo table

                data.put("Expenses", expenses);
                data.put("Goals Paid", goalsPaid);
                data.put("Monthly Salary", salary); // Updated label

            } catch (SQLException e) {
                data.put("Error", 1.0);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total <= 0) return;

            int diameter = Math.min(getWidth(), getHeight()) - 150;
            int x = (getWidth() - diameter) / 2;
            int y = 50;

            int startAngle = 0;
            int colorIndex = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double value = entry.getValue();
                int arcAngle = (int) Math.round(value / total * 360);

                g2.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                g2.fillArc(x, y, diameter, diameter, startAngle, arcAngle);
                startAngle += arcAngle;
                colorIndex++;
            }

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("Financial Overview", getWidth()/2 - 60, 30);

            int legendY = y + diameter + 30;
            int legendX = 50;
            colorIndex = 0;

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                g2.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                g2.fillRect(legendX, legendY, 20, 20);
                g2.setColor(Color.BLACK);

                String label = String.format("%s: N$%,.2f (%.1f%%)",
                    entry.getKey(), entry.getValue(), (entry.getValue()/total)*100);

                g2.drawString(label, legendX + 30, legendY + 15);
                legendY += 30;
                colorIndex++;
            }
        }
    }

    class ExpensesPieChartPanel extends JPanel {
        private Map<String, Double> expenseCategories = new LinkedHashMap<>();

        public ExpensesPieChartPanel() {
            setBackground(Color.WHITE);
            loadData();
        }

        private void loadData() {
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM expense WHERE id = ?");
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    for (int i = 1; i <= 10; i++) {
                        String name = rs.getString("exp" + i + "_name");
                        double amount = rs.getDouble("exp" + i + "_amount");
                        if (name != null && amount > 0) {
                            expenseCategories.put(name, amount);
                        }
                    }
                }
            } catch (SQLException e) {
                expenseCategories.put("Error", 1.0);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double total = expenseCategories.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total <= 0) return;

            int diameter = Math.min(getWidth(), getHeight()) - 150;
            int x = (getWidth() - diameter) / 2;
            int y = 50;

            int startAngle = 0;
            int colorIndex = 0;
            for (Map.Entry<String, Double> entry : expenseCategories.entrySet()) {
                double value = entry.getValue();
                int arcAngle = (int) Math.round(value / total * 360);

                g2.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                g2.fillArc(x, y, diameter, diameter, startAngle, arcAngle);
                startAngle += arcAngle;
                colorIndex++;
            }

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("Expense Categories Breakdown", getWidth()/2 - 120, 30);

            int legendY = y + diameter + 30;
            int legendX = 50;
            colorIndex = 0;

            for (Map.Entry<String, Double> entry : expenseCategories.entrySet()) {
                g2.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                g2.fillRect(legendX, legendY, 20, 20);
                g2.setColor(Color.BLACK);

                String label = String.format("%s: N$%,.2f (%.1f%%)",
                    entry.getKey(), entry.getValue(), (entry.getValue()/total)*100);

                g2.drawString(label, legendX + 30, legendY + 15);
                legendY += 30;
                colorIndex++;
            }
        }
    }

    class GoalProgressPieChartPanel extends JPanel {
        private String goalName;
        private double totalAmount;
        private double amountLeft;

        public GoalProgressPieChartPanel(String goalName) {
            this.goalName = goalName;
            setBackground(Color.WHITE);
            loadData();
        }

        private void loadData() {
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT goalamount, amount_left FROM goals WHERE id = ? AND goalname = ?");
                ps.setInt(1, userId);
                ps.setString(2, goalName);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    totalAmount = rs.getDouble("goalamount");
                    amountLeft = rs.getDouble("amount_left");
                }
            } catch (SQLException e) {
                totalAmount = 1;
                amountLeft = 1;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (totalAmount <= 0) return;

            int diameter = Math.min(getWidth(), getHeight()) - 150;
            int x = (getWidth() - diameter) / 2;
            int y = 50;
            double amountPaid = totalAmount - amountLeft;
            int paidAngle = (int) (amountPaid / totalAmount * 360);

            g2.setColor(CHART_COLORS[0]);
            g2.fillArc(x, y, diameter, diameter, 0, paidAngle);
            g2.setColor(CHART_COLORS[1]);
            g2.fillArc(x, y, diameter, diameter, paidAngle, 360 - paidAngle);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString(goalName + " Progress", getWidth()/2 - 80, 30);

            String progressText = String.format("%.1f%%\n(N$%,.2f of N$%,.2f)",
                (amountPaid/totalAmount)*100, amountPaid, totalAmount);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            drawCenteredString(g2, progressText, x, y, diameter, diameter);

            int legendY = y + diameter + 30;
            int legendX = 50;

            g2.setColor(CHART_COLORS[0]);
            g2.fillRect(legendX, legendY, 20, 20);
            g2.setColor(Color.BLACK);
            g2.drawString("Achieved: N$" + String.format("%,.2f", amountPaid), legendX + 30, legendY + 15);

            legendY += 30;
            g2.setColor(CHART_COLORS[1]);
            g2.fillRect(legendX, legendY, 20, 20);
            g2.setColor(Color.BLACK);
            g2.drawString("Remaining: N$" + String.format("%,.2f", amountLeft), legendX + 30, legendY + 15);
        }

        private void drawCenteredString(Graphics g, String text, int x, int y, int width, int height) {
            FontMetrics metrics = g.getFontMetrics();
            int textX = x + (width - metrics.stringWidth(text)) / 2;
            int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();

            String[] lines = text.split("\n");
            for (int i = 0; i < lines.length; i++) {
                int lineWidth = metrics.stringWidth(lines[i]);
                g.drawString(lines[i], x + (width - lineWidth) / 2, textY + (i * metrics.getHeight()));
            }
        }
    }

    class YearlySavingsChartPanel extends JPanel {
        private Map<String, Double> monthlySavings = new LinkedHashMap<>();

        public YearlySavingsChartPanel() {
            setBackground(Color.WHITE);
            loadData();
        }

        private void loadData() {
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dece FROM year WHERE id = ?");
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    monthlySavings.put("Jan", rs.getDouble("Jan"));
                    monthlySavings.put("Feb", rs.getDouble("Feb"));
                    monthlySavings.put("Mar", rs.getDouble("Mar"));
                    monthlySavings.put("Apr", rs.getDouble("Apr"));
                    monthlySavings.put("May", rs.getDouble("May"));
                    monthlySavings.put("Jun", rs.getDouble("Jun"));
                    monthlySavings.put("Jul", rs.getDouble("Jul"));
                    monthlySavings.put("Aug", rs.getDouble("Aug"));
                    monthlySavings.put("Sep", rs.getDouble("Sep"));
                    monthlySavings.put("Oct", rs.getDouble("Oct"));
                    monthlySavings.put("Nov", rs.getDouble("Nov"));
                    monthlySavings.put("Dec", rs.getDouble("Dece"));
                }
            } catch (SQLException e) {
                monthlySavings.put("Error", 0.0);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int barWidth = 40;
            int startX = 50;
            int baseY = getHeight() - 100;
            double maxValue = Collections.max(monthlySavings.values());
            if (maxValue <= 0) maxValue = 1;

            g2.drawLine(startX - 10, 50, startX - 10, baseY);
            for (int i = 0; i <= 5; i++) {
                int y = baseY - (i * (baseY - 50) / 5);
                g2.drawString(String.format("N$%,d", (int)(i * maxValue / 5)), startX - 45, y + 5);
                g2.drawLine(startX - 15, y, startX - 10, y);
            }

            int monthIndex = 0;
            for (Map.Entry<String, Double> entry : monthlySavings.entrySet()) {
                int x = startX + monthIndex * (barWidth + 20);
                double value = entry.getValue();
                int height = (int) ((value / maxValue) * (baseY - 50));

                g2.setColor(CHART_COLORS[monthIndex % CHART_COLORS.length]);
                g2.fillRect(x, baseY - height, barWidth, height);

                if (height > 20) {
                    g2.setColor(Color.BLACK);
                    String valueLabel = String.format("N$%,.0f", value);
                    int labelWidth = g2.getFontMetrics().stringWidth(valueLabel);
                    g2.drawString(valueLabel, x + (barWidth - labelWidth)/2, baseY - height - 5);
                }

                g2.drawString(entry.getKey(), x + barWidth/2 - 10, baseY + 20);
                monthIndex++;
            }

            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("Monthly Savings History", getWidth()/2 - 80, 30);
        }
    }

    private double getTotalExpenses() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM expense WHERE id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            double total = 0;
            if (rs.next()) {
                for (int i = 1; i <= 10; i++) {
                    total += rs.getDouble("exp" + i + "_amount");
                }
            }
            return total;
        }
    }

    private double getGoalsPaid() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT SUM(paid) AS totalPaid FROM goals WHERE id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("totalPaid") : 0;
        }
    }

    private double getSalary() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT salary FROM userinfo WHERE id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("salary") : 0;
        }
    }

    private double getYearlySavingsTotal() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT (Jan + Feb + Mar + Apr + May + Jun + Jul + Aug + Sep + Oct + Nov + Dece) AS total FROM year WHERE id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StatsPage("TestUser", 1).setVisible(true);
        });
    }
}
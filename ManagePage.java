import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManagePage extends JFrame {
    private JTextArea goalsArea, expensesArea;
    private String loggedInUsername;
    private int userId;
    private List<String> goalNames, expenseNames;
    private DefaultComboBoxModel<String> goalsComboBoxModel, expensesComboBoxModel;
    private Timer goalUpdateTimer;
    private AppTimer appTimer;
    private static final int UPDATE_INTERVAL = 120000; // 2 minutes in milliseconds

    public ManagePage(String username, int userId) {
        this.loggedInUsername = username;
        this.userId = userId;
        this.appTimer = AppTimer.getInstance();
        goalNames = new ArrayList<>();
        expenseNames = new ArrayList<>();
        goalsComboBoxModel = new DefaultComboBoxModel<>();
        expensesComboBoxModel = new DefaultComboBoxModel<>();

        setupUI();
        initGoalUpdateTimer();
        loadGoals();
        loadExpenses();
    }

    private void setupUI() {
        setTitle("Manage Goals & Expenses - " + loggedInUsername);
        setSize(900, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));

        JButton backButton = new JButton("← Back to Dashboard");
        backButton.addActionListener(e -> {
            new DashboardPage(loggedInUsername, userId).setVisible(true);
            dispose();
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(backButton, BorderLayout.WEST);
        JLabel titleLabel = new JLabel("Manage Goals & Expenses", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel goalsSetPanel = new JPanel(new BorderLayout());
        goalsSetPanel.setBorder(BorderFactory.createTitledBorder("Allocate Funds to Goals"));

        JPanel goalsSetInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel goalSelectLabel = new JLabel("Select Goal:");
        JComboBox<String> goalsComboBox = new JComboBox<>(goalsComboBoxModel);
        JLabel setAmountLabel = new JLabel("Amount to allocate (N$):");
        JTextField setAmountField = new JTextField(10);
        JButton setAmountBtn = new JButton("Allocate");

        setAmountBtn.addActionListener(e -> {
            try {
                String selectedGoal = (String) goalsComboBox.getSelectedItem();
                double amount = Double.parseDouble(setAmountField.getText());
                if (selectedGoal != null) {
                    allocateGoalAmount(selectedGoal, amount);
                    setAmountField.setText("");
                } else {
                    goalsArea.append("Please select a goal\n");
                }
            } catch (NumberFormatException ex) {
                goalsArea.append("Please enter a valid number\n");
            }
        });

        goalsSetInputPanel.add(goalSelectLabel);
        goalsSetInputPanel.add(goalsComboBox);
        goalsSetInputPanel.add(setAmountLabel);
        goalsSetInputPanel.add(setAmountField);
        goalsSetInputPanel.add(setAmountBtn);
        goalsSetPanel.add(goalsSetInputPanel, BorderLayout.CENTER);

        contentPanel.add(goalsSetPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel goalsPanel = new JPanel(new BorderLayout());
        goalsPanel.setBorder(BorderFactory.createTitledBorder("Goals"));

        goalsArea = new JTextArea(8, 60);
        goalsArea.setEditable(false);
        goalsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane goalsScroll = new JScrollPane(goalsArea);
        goalsPanel.add(goalsScroll, BorderLayout.CENTER);

        JPanel goalsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addGoalBtn = new JButton("Add Goal");
        addGoalBtn.addActionListener(e -> addGoal());
        goalsButtonPanel.add(addGoalBtn);

        JButton editGoalBtn = new JButton("Edit Goal");
        editGoalBtn.addActionListener(e -> editGoal());
        goalsButtonPanel.add(editGoalBtn);

        JButton deleteGoalBtn = new JButton("Delete Goal");
        deleteGoalBtn.addActionListener(e -> deleteGoal());
        goalsButtonPanel.add(deleteGoalBtn);

        goalsPanel.add(goalsButtonPanel, BorderLayout.SOUTH);
        contentPanel.add(goalsPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel expensesPanel = new JPanel(new BorderLayout());
        expensesPanel.setBorder(BorderFactory.createTitledBorder("Expenses"));

        expensesArea = new JTextArea(5, 60);
        expensesArea.setEditable(false);
        expensesArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane expensesScroll = new JScrollPane(expensesArea);
        expensesPanel.add(expensesScroll, BorderLayout.CENTER);

        JPanel expensesButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addExpenseBtn = new JButton("Add Expense");
        addExpenseBtn.addActionListener(e -> addExpense());
        expensesButtonPanel.add(addExpenseBtn);

        JButton editExpenseBtn = new JButton("Edit Expense");
        editExpenseBtn.addActionListener(e -> editExpense());
        expensesButtonPanel.add(editExpenseBtn);

        JButton deleteExpenseBtn = new JButton("Delete Expense");
        deleteExpenseBtn.addActionListener(e -> deleteExpense());
        expensesButtonPanel.add(deleteExpenseBtn);

        expensesPanel.add(expensesButtonPanel, BorderLayout.SOUTH);
        contentPanel.add(expensesPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void initGoalUpdateTimer() {
        goalUpdateTimer = new Timer(UPDATE_INTERVAL, e -> {
            loadGoals();
            loadExpenses();
        });
        goalUpdateTimer.start();
    }

    private void loadGoals() {
        goalsArea.setText("");
        goalNames.clear();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
            String query = "SELECT goalname, goalamount, paid, amount_left, status FROM goals WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.isBeforeFirst()) {
                    goalsArea.setText("No goals set yet.");
                } else {
                    goalsArea.append(String.format("%-25s %-15s %-15s %-15s %-10s\n",
                        "Goal Name", "Total Amount", "Paid", "Remaining", "Status"));
                    goalsArea.append("----------------------------------------------------------------------------\n");

                    while (rs.next()) {
                        String goalName = rs.getString("goalname");
                        double goalAmount = rs.getDouble("goalamount");
                        double paid = rs.getDouble("paid");
                        double amountLeft = rs.getDouble("amount_left");
                        String status = rs.getString("status");

                        String goalText = String.format("%-25s N$%-14.2f N$%-14.2f N$%-14.2f %-10s\n",
                            goalName, goalAmount, paid, amountLeft, status);
                        goalsArea.append(goalText);
                        goalNames.add(goalName);
                    }
                }
            }
        } catch (SQLException e) {
            goalsArea.append("Error loading goals\n");
        }
        updateGoalsComboBox();
    }

    private void updateGoalsComboBox() {
        goalsComboBoxModel.removeAllElements();
        for (String goal : goalNames) {
            goalsComboBoxModel.addElement(goal);
        }
    }

    private void allocateGoalAmount(String goalName, double amount) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
            String getQuery = "SELECT goalamount, paid FROM goals WHERE id = ? AND goalname = ?";
            double goalAmount = 0;
            double currentPaid = 0;

            try (PreparedStatement getStmt = conn.prepareStatement(getQuery)) {
                getStmt.setInt(1, userId);
                getStmt.setString(2, goalName);
                ResultSet rs = getStmt.executeQuery();
                if (rs.next()) {
                    goalAmount = rs.getDouble("goalamount");
                    currentPaid = rs.getDouble("paid");
                }
            }

            double newPaid = currentPaid + amount;
            double newAmountLeft = goalAmount - newPaid;
            String status = newAmountLeft <= 0 ? "REACHED" : "In Progress";

            String updateQuery = "UPDATE goals SET paid = ?, amount_left = ?, status = ? " +
                               "WHERE id = ? AND goalname = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setDouble(1, newPaid);
                updateStmt.setDouble(2, newAmountLeft);
                updateStmt.setString(3, status);
                updateStmt.setInt(4, userId);
                updateStmt.setString(5, goalName);
                updateStmt.executeUpdate();
                loadGoals();
            }
        } catch (SQLException e) {
            goalsArea.append("Error allocating goal amount\n");
        }
    }

    private void addGoal() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField amountField = new JTextField();

        panel.add(new JLabel("Goal Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Amount (N$):"));
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Goal",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double amount = Double.parseDouble(amountField.getText());

                if (name.isEmpty()) {
                    goalsArea.append("Goal name cannot be empty\n");
                    return;
                }

                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                    String query = "INSERT INTO goals (id, username, goalname, goalamount, paid, amount_left, status) " +
                                 "VALUES (?, ?, ?, ?, 0, ?, 'In Progress')";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, loggedInUsername);
                        stmt.setString(3, name);
                        stmt.setDouble(4, amount);
                        stmt.setDouble(5, amount);
                        stmt.executeUpdate();
                        loadGoals();
                    }
                }
            } catch (NumberFormatException e) {
                goalsArea.append("Please enter a valid number for amount\n");
            } catch (SQLException e) {
                goalsArea.append("Error adding goal\n");
            }
        }
    }

    private void editGoal() {
        if (goalNames.isEmpty()) {
            goalsArea.append("No goals available to edit\n");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(this,
            "Select goal to edit:", "Edit Goal",
            JOptionPane.PLAIN_MESSAGE, null,
            goalNames.toArray(), goalNames.get(0));

        if (selected != null) {
            JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
            JTextField amountField = new JTextField();

            panel.add(new JLabel("New Amount (N$):"));
            panel.add(amountField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Goal",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    double amount = Double.parseDouble(amountField.getText());

                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                        String getQuery = "SELECT paid FROM goals WHERE id = ? AND goalname = ?";
                        double paid = 0;
                        try (PreparedStatement getStmt = conn.prepareStatement(getQuery)) {
                            getStmt.setInt(1, userId);
                            getStmt.setString(2, selected);
                            ResultSet rs = getStmt.executeQuery();
                            if (rs.next()) {
                                paid = rs.getDouble("paid");
                            }
                        }

                        String query = "UPDATE goals SET goalamount = ?, amount_left = ? " +
                                     "WHERE id = ? AND goalname = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(query)) {
                            stmt.setDouble(1, amount);
                            stmt.setDouble(2, amount - paid);
                            stmt.setInt(3, userId);
                            stmt.setString(4, selected);
                            stmt.executeUpdate();
                            loadGoals();
                        }
                    }
                } catch (NumberFormatException e) {
                    goalsArea.append("Please enter a valid number for amount\n");
                } catch (SQLException e) {
                    goalsArea.append("Error updating goal\n");
                }
            }
        }
    }

    private void deleteGoal() {
        if (goalNames.isEmpty()) {
            goalsArea.append("No goals available to delete\n");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(this,
            "Select goal to delete:", "Delete Goal",
            JOptionPane.PLAIN_MESSAGE, null,
            goalNames.toArray(), goalNames.get(0));

        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the goal: " + selected + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                    String query = "DELETE FROM goals WHERE id = ? AND goalname = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, selected);
                        stmt.executeUpdate();
                        loadGoals();
                    }
                } catch (SQLException e) {
                    goalsArea.append("Error deleting goal\n");
                }
            }
        }
    }

    private void loadExpenses() {
        expensesArea.setText("");
        expenseNames.clear();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
            String query = "SELECT * FROM expense WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    boolean hasExpenses = false;
                    expensesArea.append(String.format("%-25s %-15s\n", "Expense Name", "Amount"));
                    expensesArea.append("----------------------------------------\n");

                    for (int i = 1; i <= 10; i++) {
                        String name = rs.getString("exp" + i + "_name");
                        Double amount = rs.getDouble("exp" + i + "_amount");
                        if (name != null && !name.isEmpty() && amount != 0) {
                            expensesArea.append(String.format("%-25s N$%-14.2f\n", name, amount));
                            expenseNames.add(name);
                            hasExpenses = true;
                        }
                    }
                    if (!hasExpenses) {
                        expensesArea.setText("No expenses recorded yet.");
                    }
                } else {
                    expensesArea.setText("No expenses recorded yet.");
                }
            }
        } catch (SQLException e) {
            expensesArea.append("Error loading expenses\n");
        }
        updateExpensesComboBox();
    }

    private void updateExpensesComboBox() {
        expensesComboBoxModel.removeAllElements();
        for (String expense : expenseNames) {
            expensesComboBoxModel.addElement(expense);
        }
    }

    private void addExpense() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField amountField = new JTextField();

        panel.add(new JLabel("Expense Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Amount (N$):"));
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Expense",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double amount = Double.parseDouble(amountField.getText());

                if (name.isEmpty()) {
                    expensesArea.append("Expense name cannot be empty\n");
                    return;
                }

                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                    String checkQuery = "SELECT * FROM expense WHERE id = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                        checkStmt.setInt(1, userId);
                        ResultSet rs = checkStmt.executeQuery();

                        if (rs.next()) {
                            for (int i = 1; i <= 10; i++) {
                                if (rs.getString("exp" + i + "_name") == null) {
                                    String updateQuery = "UPDATE expense SET exp" + i + "_name = ?, exp" + i + "_amount = ? " +
                                                      "WHERE id = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                        updateStmt.setString(1, name);
                                        updateStmt.setDouble(2, amount);
                                        updateStmt.setInt(3, userId);
                                        updateStmt.executeUpdate();
                                        break;
                                    }
                                }
                            }
                        }
                        loadExpenses();
                    }
                }
            } catch (NumberFormatException e) {
                expensesArea.append("Please enter a valid number for amount\n");
            } catch (SQLException e) {
                expensesArea.append("Error adding expense\n");
            }
        }
    }

    private void editExpense() {
        if (expenseNames.isEmpty()) {
            expensesArea.append("No expenses available to edit\n");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(this,
            "Select expense to edit:", "Edit Expense",
            JOptionPane.PLAIN_MESSAGE, null,
            expenseNames.toArray(), expenseNames.get(0));

        if (selected != null) {
            JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
            JTextField amountField = new JTextField();

            panel.add(new JLabel("New Amount (N$):"));
            panel.add(amountField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Expense",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    double amount = Double.parseDouble(amountField.getText());

                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                        String findQuery = "SELECT * FROM expense WHERE id = ?";
                        try (PreparedStatement findStmt = conn.prepareStatement(findQuery)) {
                            findStmt.setInt(1, userId);
                            ResultSet rs = findStmt.executeQuery();

                            if (rs.next()) {
                                for (int i = 1; i <= 10; i++) {
                                    if (selected.equals(rs.getString("exp" + i + "_name"))) {
                                        String updateQuery = "UPDATE expense SET exp" + i + "_amount = ? " +
                                                            "WHERE id = ?";
                                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                            updateStmt.setDouble(1, amount);
                                            updateStmt.setInt(2, userId);
                                            updateStmt.executeUpdate();
                                            loadExpenses();
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    expensesArea.append("Please enter a valid number for amount\n");
                } catch (SQLException e) {
                    expensesArea.append("Error updating expense\n");
                }
            }
        }
    }

    private void deleteExpense() {
        if (expenseNames.isEmpty()) {
            expensesArea.append("No expenses available to delete\n");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(this,
            "Select expense to delete:", "Delete Expense",
            JOptionPane.PLAIN_MESSAGE, null,
            expenseNames.toArray(), expenseNames.get(0));

        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the expense: " + selected + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                    String findQuery = "SELECT * FROM expense WHERE id = ?";
                    try (PreparedStatement findStmt = conn.prepareStatement(findQuery)) {
                        findStmt.setInt(1, userId);
                        ResultSet rs = findStmt.executeQuery();

                        if (rs.next()) {
                            for (int i = 1; i <= 10; i++) {
                                if (selected.equals(rs.getString("exp" + i + "_name"))) {
                                    String updateQuery = "UPDATE expense SET exp" + i + "_name = NULL, exp" + i + "_amount = 0 " +
                                                        "WHERE id = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                        updateStmt.setInt(1, userId);
                                        updateStmt.executeUpdate();
                                        loadExpenses();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    expensesArea.append("Error deleting expense\n");
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (goalUpdateTimer != null) {
            goalUpdateTimer.stop();
        }
        super.dispose();
    }
}
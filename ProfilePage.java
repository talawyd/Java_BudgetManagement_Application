import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProfilePage extends JFrame {
    private JLabel nameLabel, jobLabel, salaryLabel, bankBalanceLabel;
    private String loggedInUsername;
    private int userId;
    private AppTimer appTimer;

    public ProfilePage(String username, int userId) {
        this.loggedInUsername = username;
        this.userId = userId;
        this.appTimer = AppTimer.getInstance();

        setTitle("Profile - " + username);
        setSize(600, 400);
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
        JLabel titleLabel = new JLabel("User Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel userInfoPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        userInfoPanel.setBorder(BorderFactory.createTitledBorder("Personal Info"));

        userInfoPanel.add(new JLabel("Username:"));
        nameLabel = new JLabel();
        userInfoPanel.add(nameLabel);
        JButton editNameBtn = new JButton("Edit");
        editNameBtn.addActionListener(e -> editField("Username", nameLabel.getText(), "username"));
        userInfoPanel.add(editNameBtn);

        userInfoPanel.add(new JLabel("Job:"));
        jobLabel = new JLabel();
        userInfoPanel.add(jobLabel);
        JButton editJobBtn = new JButton("Edit");
        editJobBtn.addActionListener(e -> editField("Job", jobLabel.getText(), "job"));
        userInfoPanel.add(editJobBtn);

        userInfoPanel.add(new JLabel("Salary (N$):"));
        salaryLabel = new JLabel();
        userInfoPanel.add(salaryLabel);
        JButton editSalaryBtn = new JButton("Edit");
        editSalaryBtn.addActionListener(e -> editField("Salary", salaryLabel.getText(), "salary"));
        userInfoPanel.add(editSalaryBtn);

        userInfoPanel.add(new JLabel("Bank Balance (N$):"));
        bankBalanceLabel = new JLabel();
        userInfoPanel.add(bankBalanceLabel);
        userInfoPanel.add(new JLabel());

        contentPanel.add(userInfoPanel);
        add(contentPanel, BorderLayout.CENTER);

        loadUserData();
    }

    private void loadUserData() {
        loadUserInfo();
        loadBankBalance();
    }

    private void loadUserInfo() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
            String query = "SELECT username, job, salary FROM userinfo WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    nameLabel.setText(rs.getString("username"));
                    jobLabel.setText(rs.getString("job") != null ? rs.getString("job") : "Not specified");
                    salaryLabel.setText(rs.getDouble("salary") != 0 ?
                        "N$" + String.format("%.2f", rs.getDouble("salary")) : "Not specified");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading user info: " + e.getMessage());
        }
    }

    private void loadBankBalance() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
            String query = "SELECT balance FROM bank_account WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    bankBalanceLabel.setText("N$" + String.format("%.2f", balance));
                } else {
                    bankBalanceLabel.setText("N$0.00");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bank balance: " + e.getMessage());
            bankBalanceLabel.setText("Error loading balance");
        }
    }

    private void editField(String fieldName, String currentValue, String columnName) {
        String newValue = JOptionPane.showInputDialog(this,
            "Enter new " + fieldName + ":", currentValue);

        if (newValue != null && !newValue.trim().isEmpty()) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                String query = "UPDATE userinfo SET " + columnName + " = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    if (columnName.equals("salary")) {
                        try {
                            stmt.setDouble(1, Double.parseDouble(newValue));
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(this, "Please enter a valid number");
                            return;
                        }
                    } else {
                        stmt.setString(1, newValue);
                    }
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                    loadUserInfo();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating " + fieldName + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
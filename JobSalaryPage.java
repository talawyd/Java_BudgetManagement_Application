import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class JobSalaryPage extends JFrame {
    private String username;
    private int userId;

    public JobSalaryPage(String username, int userId) {
        this.username = username;
        this.userId = userId;

        setTitle("Enter Job, Salary and Bank Balance");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel jobLabel = new JLabel("Current Job:");
        JTextField jobField = new JTextField();

        JLabel salaryLabel = new JLabel("Monthly Salary (N$):");
        JTextField salaryField = new JTextField();

        JLabel balanceLabel = new JLabel("Current Bank Balance (N$):");
        JTextField balanceField = new JTextField();

        JButton saveBtn = new JButton("Save");

        panel.add(jobLabel);
        panel.add(jobField);
        panel.add(salaryLabel);
        panel.add(salaryField);
        panel.add(balanceLabel);
        panel.add(balanceField);
        panel.add(new JLabel()); // Empty cell for layout
        panel.add(saveBtn);

        add(panel);

        saveBtn.addActionListener(e -> {
            String job = jobField.getText();
            double salary;
            double balance;

            try {
                salary = Double.parseDouble(salaryField.getText());
                balance = Double.parseDouble(balanceField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for salary and balance.");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                String userInfoQuery = "UPDATE userinfo SET job = ?, salary = ? WHERE id = ?";
                try (PreparedStatement userInfoStmt = conn.prepareStatement(userInfoQuery)) {
                    userInfoStmt.setString(1, job);
                    userInfoStmt.setDouble(2, salary);
                    userInfoStmt.setInt(3, userId);
                    userInfoStmt.executeUpdate();
                }

                String bankQuery = "UPDATE bank_account SET balance = ? WHERE id = ?";
                try (PreparedStatement bankStmt = conn.prepareStatement(bankQuery)) {
                    bankStmt.setDouble(1, balance);
                    bankStmt.setInt(2, userId);
                    bankStmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Information saved successfully! Redirecting to dashboard.");
                dispose();
                new DashboardPage(username, userId).setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error saving information: " + ex.getMessage());
            }
        });
    }
}
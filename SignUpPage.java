import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SignUpPage extends JFrame {
    public SignUpPage() {
        setTitle("Sign Up");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton signUpButton = new JButton("Sign Up");
        JButton goToLogin = new JButton("Go to Sign In");

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(signUpButton);
        panel.add(goToLogin);

        add(panel);

        signUpButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
                String userQuery = "INSERT INTO users_login (username, userEmail, usersPw) VALUES (?, ?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, name);
                userStmt.setString(2, email);
                userStmt.setString(3, password);
                userStmt.executeUpdate();

                ResultSet rs = userStmt.getGeneratedKeys();
                if (!rs.next()) {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
                int userId = rs.getInt(1);

                String infoQuery = "INSERT INTO userinfo (id, username) VALUES (?, ?)";
                PreparedStatement infoStmt = conn.prepareStatement(infoQuery);
                infoStmt.setInt(1, userId);
                infoStmt.setString(2, name);
                infoStmt.executeUpdate();

                String expenseQuery = "INSERT INTO expense (id, username) VALUES (?, ?)";
                PreparedStatement expenseStmt = conn.prepareStatement(expenseQuery);
                expenseStmt.setInt(1, userId);
                expenseStmt.setString(2, name);
                expenseStmt.executeUpdate();

                String bankAccountQuery = "INSERT INTO bank_account (id, balance) VALUES (?, 0)";
                PreparedStatement bankAccountStmt = conn.prepareStatement(bankAccountQuery);
                bankAccountStmt.setInt(1, userId);
                bankAccountStmt.executeUpdate();

                String yearQuery = "INSERT INTO year (id, Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dece) " +
                                 "VALUES (?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)";
                PreparedStatement yearStmt = conn.prepareStatement(yearQuery);
                yearStmt.setInt(1, userId);
                yearStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Account created successfully!");
                dispose();
                new JobSalaryPage(name, userId).setVisible(true);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        goToLogin.addActionListener(e -> {
            dispose();
            new SignInPage().setVisible(true);
        });
    }
}
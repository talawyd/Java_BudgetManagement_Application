import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignInPage extends JFrame {
    public SignInPage() {
        setTitle("Sign In");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton signInButton = new JButton("Sign In");
        panel.add(signInButton);

        JButton goToSignUp = new JButton("Go to Sign Up");
        panel.add(goToSignUp);

        add(panel, BorderLayout.CENTER);

        signInButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123");
                 PreparedStatement stmt = conn.prepareStatement("SELECT id, username FROM users_login WHERE userEmail = ? AND usersPw = ?")) {
                stmt.setString(1, email);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String username = rs.getString("username");
                    int userId = rs.getInt("id");
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose();
                    new DashboardPage(username, userId).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        goToSignUp.addActionListener(e -> {
            dispose();
            new SignUpPage().setVisible(true);
        });
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LandingPage extends JFrame {
    public LandingPage() {
        setTitle("Finance Tracker");
        setSize(300, 200);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JButton signUpBtn = new JButton("Sign Up");
        JButton signInBtn = new JButton("Sign In");

        add(new JLabel("Welcome to the Finance Tracker"));
        add(signUpBtn);
        add(signInBtn);

        signUpBtn.addActionListener(e -> {
            dispose();
            new SignUpPage().setVisible(true);
        });

        signInBtn.addActionListener(e -> {
            dispose();
            new SignInPage().setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppTimer.getInstance();
            new LandingPage().setVisible(true);
        });
    }
}
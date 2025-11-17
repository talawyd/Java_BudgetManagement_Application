import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardPage extends JFrame {
    private JPanel mainPanel;
    private String username;
    private int userId;

    public DashboardPage(String username, int userId) {
        this.username = username;
        this.userId = userId;

        setTitle("Dashboard - " + username);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLayout(new BorderLayout());

        mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JButton profileBtn = createCenterButton("Profile");
        JButton statsBtn = createCenterButton("Stats");
        JButton manageBtn = createCenterButton("Manage Goals & Expenses");
        JButton logoutBtn = createCenterButton("Log Out");

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(profileBtn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(statsBtn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(manageBtn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(logoutBtn);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel, BorderLayout.CENTER);

        profileBtn.addActionListener(e -> {
            new ProfilePage(username, userId).setVisible(true);
            dispose();
        });

        statsBtn.addActionListener(e -> {
            new StatsPage(username, userId).setVisible(true);
            dispose();
        });

        manageBtn.addActionListener(e -> {
            new ManagePage(username, userId).setVisible(true);
            dispose();
        });

        logoutBtn.addActionListener(e -> {
            new LandingPage().setVisible(true);
            dispose();
        });
    }

    private JButton createCenterButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        return button;
    }
}
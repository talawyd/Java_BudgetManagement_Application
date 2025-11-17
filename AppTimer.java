import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class AppTimer {
    private static AppTimer instance;
    private Timer timer;
    private int currentMonthIndex = 0;
    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                         "Jul", "Aug", "Sep", "Oct", "Nov", "Dece"};

    private AppTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentMonthIndex = (currentMonthIndex + 1) % 12;
                System.out.println("[MONTHLY UPDATE] Current month: " + MONTHS[currentMonthIndex]);
                updateAllUsers();
            }
        }, 120000, 120000);
    }

    public static AppTimer getInstance() {
        if (instance == null) {
            instance = new AppTimer();
        }
        return instance;
    }

    public String getCurrentMonth() {
        return MONTHS[currentMonthIndex];
    }

    public int getCurrentMonthIndex() {
        return currentMonthIndex;
    }

    private void updateAllUsers() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sign_up", "root", "@PRG123")) {
            String query = "SELECT id FROM users_login";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    updateUserFinances(conn, rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating users: " + e.getMessage());
        }
    }

    private void updateUserFinances(Connection conn, int userId) throws SQLException {
        double salary = getSalary(conn, userId);
        if (salary == 0) return;

        double expenses = getTotalExpenses(conn, userId);

        double goalsPaid = getTotalGoalsPaid(conn, userId);

        double leftover = salary - expenses - goalsPaid;

        resetGoalsPaid(conn, userId);

        System.out.printf("[USER %d] Salary: %.2f | Expenses: %.2f | Goals Paid: %.2f | Leftover: %.2f%n",
                         userId, salary, expenses, goalsPaid, leftover);
    }

    private double getSalary(Connection conn, int userId) throws SQLException {
        String query = "SELECT salary FROM userinfo WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("salary") : 0;
        }
    }

    private double getTotalExpenses(Connection conn, int userId) throws SQLException {
        String query = "SELECT exp1_amount + exp2_amount + exp3_amount + exp4_amount + " +
                      "exp5_amount + exp6_amount + exp7_amount + exp8_amount + " +
                      "exp9_amount + exp10_amount AS total FROM expense WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }

    private double getTotalGoalsPaid(Connection conn, int userId) throws SQLException {
        String query = "SELECT SUM(paid) FROM goals WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private void resetGoalsPaid(Connection conn, int userId) throws SQLException {
        String query = "UPDATE goals SET paid = 0 WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            instance = null;
        }
    }
}
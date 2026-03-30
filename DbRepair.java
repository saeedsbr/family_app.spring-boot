
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbRepair {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:file:/media/talha/Data/development/vehicle management system/backend/vmsdb;MODE=PostgreSQL;AUTO_SERVER=TRUE";
        String user = "sa";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (Statement stmt = conn.createStatement()) {
                int count = stmt.executeUpdate("UPDATE users SET email = LOWER(email)");
                System.out.println("Lowercased " + count + " emails.");
            }

            // Check for the user again
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt
                            .executeQuery("SELECT id, email FROM users WHERE email = 'italha.saeedsbr@gmail.com'")) {
                if (rs.next()) {
                    System.out.println("Found user: " + rs.getString("email") + " with ID: " + rs.getString("id"));
                } else {
                    System.out.println("User italha.saeedsbr@gmail.com NOT found after update!");
                }
            }
        }
    }
}

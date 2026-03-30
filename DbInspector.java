
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DbInspector {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:file:/media/talha/Data/development/vehicle management system/backend/vmsdb;MODE=PostgreSQL;AUTO_SERVER=TRUE";
        String user = "sa";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("--- ALL USERS ---");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                while (rs.next()) {
                    StringBuilder sb = new StringBuilder("User: ");
                    for (int i = 1; i <= cols; i++) {
                        sb.append(md.getColumnName(i)).append("=").append(rs.getString(i)).append(", ");
                    }
                    System.out.println(sb.toString());
                }
            } catch (Exception e) {
                System.out.println("Error reading users: " + e.getMessage());
            }

            System.out.println("\n--- VEHICLES ---");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, brand, model, license_plate, user_id FROM vehicles")) {
                while (rs.next()) {
                    System.out.printf("Vehicle: %s %s [%s] (Owner ID: %s)\n", rs.getString("brand"),
                            rs.getString("model"), rs.getString("license_plate"), rs.getString("user_id"));
                }
            } catch (Exception e) {
                System.out.println("Error reading vehicles: " + e.getMessage());
            }

            System.out.println("\n--- METERS ---");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, name, identifier FROM meters")) {
                while (rs.next()) {
                    System.out.printf("Meter: %s [%s]\n", rs.getString("name"), rs.getString("identifier"));
                }
            } catch (Exception e) {
                System.out.println("Error reading meters: " + e.getMessage());
            }
        }
    }
}

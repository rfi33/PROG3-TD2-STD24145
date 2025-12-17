package dish.com;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String JDBC_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    public static Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection connection = getDBConnection()) {
            System.out.println("Connection successful");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


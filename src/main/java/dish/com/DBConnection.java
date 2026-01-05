package dish.com;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final Dotenv dotenv = Dotenv.load();

    private String URL= dotenv.get("DB_URL");
    private String USER = dotenv.get("DB_USER");
    private String PASSWORD = dotenv.get("DB_PASSWORD");

    public Connection getDBConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Error connection");
        }
    }
}


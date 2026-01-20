package dish.com;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private final Dotenv dotenv;
    private final String URL;
    private final String USER;
    private final String PASSWORD;

    public DBConnection() {
        this.dotenv = Dotenv.load();
        this.URL = dotenv.get("DB_URL");
        this.USER = dotenv.get("DB_USER");
        this.PASSWORD = dotenv.get("DB_PASSWORD");
    }

    public Connection getDBConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Error connection to database", e);
        }
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Error closing connection", e);
            }
        }
    }
}

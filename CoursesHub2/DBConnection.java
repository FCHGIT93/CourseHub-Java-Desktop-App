
package CoursesHub2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=EduConnectDB2;encrypt=false;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Fatima@5";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

/*
 * Handles all sqlite-related things
 * */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {

    // connects to database
    public Connection connect(String path) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:C:/authentication.db"); // need to sub in path
    }
}

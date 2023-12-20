/*
 * Handles all sqlite-related things
 *
 * Sources used:
 * https://www.javatpoint.com/java-sqlite
 * https://www.codejava.net/java-se/jdbc/connect-to-sqlite-via-jdbc
 */

import java.sql.*;
import java.util.List;

public class DatabaseHandler {

    Connection connection;

    public DatabaseHandler(String path) {
        this.connection = this.connect(path);
    }

    // connects to database
    public Connection connect(String path) {
        Connection connection;

        // try-catch instead of adding exceptions to method signature
        // ClassNotFoundException, SQLException
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            System.out.println("Successfully connected to " + path);
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // method for selection
    // selects all records from chosen table - only for viewing purposes
    public void selectAll(String tableName) {
        try {
            String sql = "SELECT * FROM " + tableName;
            Statement statement = this.connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            // getting column count
            // https://www.ibm.com/docs/en/db2-for-zos/11?topic=applications-learning-about-resultset-using-resultsetmetadata-methods
            ResultSetMetaData resultsMetaData = results.getMetaData();
            int columnCount = resultsMetaData.getColumnCount();

            StringBuilder result = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                result.append(results.getString(i)).append(" | ");
            }

            while (results.next()) {
                System.out.println("| " + result);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // selects specific columns based on argument


    // method for insertion
    // multiple data types storage - https://stackoverflow.com/questions/26162183/java-multiple-type-data-structure
    public void insert(String tableName, String columns, List<Object> values) throws SQLException {

        // values list
        StringBuilder valueList = new StringBuilder();
        valueList.append('?');
        for (int i = 1, n = values.size(); i < n; i++) {
            valueList.append(", ?");
        }

        String sql = "INSERT INTO " + tableName + "(" + columns + ") VALUES(" + valueList + ")";
        System.out.println(sql);

        PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
    }

    // method for deletion

    // method for update

}

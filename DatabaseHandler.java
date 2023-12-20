/*
 * Handles all sqlite-related things
 *
 * Sources used:
 * https://www.javatpoint.com/java-sqlite
 * https://www.codejava.net/java-se/jdbc/connect-to-sqlite-via-jdbc
 */

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler {

    Connection connection;

    public DatabaseHandler(String path) {
        this.connection = this.connect(path);
    }

    // connects to database
    public Connection connect(String path) {
        Connection connection;

        // try-catch instead of adding exceptions to method signature
        // no need for try-catch in Main
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

        String sql = "SELECT * FROM " + tableName;

        try {
            ResultSet results = this.connection.createStatement().executeQuery(sql);

            // getting column count
            // https://www.ibm.com/docs/en/db2-for-zos/11?topic=applications-learning-about-resultset-using-resultsetmetadata-methods
            int columnCount = results.getMetaData().getColumnCount();

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

    // selects specific columns based on argument and
    public Map[] select(String tableName, String[] columns) {

        // build column argument for sql statement
        StringBuilder columnList = new StringBuilder();
        columnList.append(columns[0]);
        for (int i = 1, n = columns.length; i < n; i++) {
            columnList.append(", ").append(columns[i]);
        }

        String sql = "SELECT " + columnList + " FROM " + tableName;

        try {
            ResultSet resultSet = this.connection.createStatement().executeQuery(sql);
            Map[] records = new Map[resultSet.getMetaData().getColumnCount()];

            for (int i = 0; resultSet.next(); i++) {
                Map<String, String> results = new HashMap<>();

                // enhanced for loop
                for (String column : columns) {
                    results.put(column, resultSet.getString(column));
                }
                records[i] = results;
            }
            return records;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for insertion
    // multiple data types storage - https://stackoverflow.com/questions/26162183/java-multiple-type-data-structure
    public void insert(String tableName, String columns, List<Object> values) {

        // values list
        StringBuilder valueList = new StringBuilder();
        valueList.append('?');
        for (int i = 1, n = values.size(); i < n; i++) {
            valueList.append(", ?");
        }

        String sql = "INSERT INTO " + tableName + "(" + columns + ") VALUES(" + valueList + ")";
        System.out.println(sql);

        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for deletion

    // method for update

}

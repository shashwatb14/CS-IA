/*
 * Handles all sqlite-related things
 *
 * Sources used:
 * https://www.javatpoint.com/java-sqlite
 * https://www.codejava.net/java-se/jdbc/connect-to-sqlite-via-jdbc
 * https://www.tutorialspoint.com/sqlite/sqlite_using_autoincrement.htm
 *
 * SQL Commands:
 * CREATE TABLE authentication(ID int NOT NULL PRIMARY KEY AUTOINCREMENT, encryptedText text NOT NULL);
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler {

    private Connection connection;

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
    public ResultSet select(String tableName, boolean display) {

        String sql = "SELECT * FROM " + tableName;

        try {
            ResultSet results = this.connection.createStatement().executeQuery(sql);

            // getting column count
            // https://www.ibm.com/docs/en/db2-for-zos/11?topic=applications-learning-about-resultset-using-resultsetmetadata-methods
            int columnCount = results.getMetaData().getColumnCount();

            // to avoid duplicate printing when using internally
            if (display) {

                while (results.next()) {
                    // rebuild string for each record
                    StringBuilder result = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        result.append(results.getString(i)).append(" | ");
                    }
                    System.out.println("| " + result);
                }
            }

            return results;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // selects specific columns based on argument
    // https://stackoverflow.com/questions/13543457/how-do-you-create-a-dictionary-in-java
    public List<Map> select(String tableName, String[] columns) {

        // build column argument for sql statement
        StringBuilder columnList = new StringBuilder();
        columnList.append(columns[0]);
        for (int i = 1, n = columns.length; i < n; i++) {
            columnList.append(", ").append(columns[i]);
        }

        String sql = "SELECT " + columnList + " FROM " + tableName;

        // returning hashmap = easier to manage
        try {
            ResultSet resultSet = this.connection.createStatement().executeQuery(sql);

            // using just list for initialization gives more flexibility
            List<Map> records = new ArrayList<>(); // using dynamic arraylist - no need to specify length before usage

            for (int i = 0; resultSet.next(); i++) {
                Map<String, String> results = new HashMap<>();

                // enhanced for loop
                for (String column : columns) {
                    results.put(column, resultSet.getString(column));
                }
                records.add(i, results);
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
            for (int i = 0, n = values.size(); i < n; i++) {

                // check class: cwe.mitre.org/data/definitions/486.html
                // can only be either string for this prototype
                if (values.get(i).getClass().getName().equals("java.lang.String")) {
                    preparedStatement.setString(i + 1, values.get(i).toString());
                }
                // if double - else preparedStatement.setDouble(i + 1, (double) values.get(i));

            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for deletion
    // deletes rows/records instance-wise
    public void delete(String tableName, String content) {
        ResultSet results = this.select(tableName, false);

        try {
            int columnCount = results.getMetaData().getColumnCount();

            while (results.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    if (results.getString(i).equals(content)) {
                        String statement = "DELETE FROM " + tableName + " WHERE " + results.getMetaData().getColumnName(i) + " = \"" + content + "\"";
                        System.out.println(statement);
                        this.connection.createStatement().execute(statement);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for deletion but index-wise
    public void delete(String tableName, int index) {

        try {
            String statement = "DELETE FROM " + tableName + " WHERE id = " + index;
            System.out.println(statement);
            this.connection.createStatement().execute(statement);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for update/replacement instance-wise
    // replaces all instances

    // to reset autoincrement: https://stackoverflow.com/questions/1601697/sqlite-reset-primary-key-field
    //this.connection.createStatement().execute("UPDATE sqlite_sequence SET seq = 0 WHERE name = \"" + tableName + "\"");

}

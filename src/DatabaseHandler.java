/*
 * Handles all sqlite-related things
 *
 * Sources used:
 * https://www.javatpoint.com/java-sqlite
 * https://www.codejava.net/java-se/jdbc/connect-to-sqlite-via-jdbc
 * https://www.tutorialspoint.com/sqlite/sqlite_using_autoincrement.htm
 * https://www.sqlite.org/foreignkeys.html
 *
 * SQL Commands:
 * CREATE TABLE authentication(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, counter INTEGER, encryptedText TEXT NOT NULL);
 * CREATE TABLE sections(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, counter INTEGER, sectionTitle TEXT NOT NULL, isLocked TEXT NOT NULL);
 * CREATE TABLE notes(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, counter INTEGER, section_id INTEGER, content TEXT NOT NULL, FOREIGN KEY(section_id) REFERENCES sections(id));
 * CREATE TABLE archive(id INTEGER NOT NULL, counter INTEGER, sectionTitle TEXT NOT NULL, isLocked TEXT NOT NULL);
   INSERT INTO sections(sectionTitle, isLocked) VALUES('Personal Information', 'TRUE');
   INSERT INTO sections(sectionTitle, isLocked) VALUES('Individual Productivity', 'FALSE');
   INSERT INTO sections(sectionTitle, isLocked) VALUES('Office Workspace', 'FALSE');
   INSERT INTO sections(sectionTitle, isLocked) VALUES('Self-Learning', 'FALSE');
   INSERT INTO sections(sectionTitle, isLocked) VALUES('Family Travel', 'FALSE');
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler {

    private final Connection connection;

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
        ResultSet results;
        try {
            results = this.connection.createStatement().executeQuery(sql);

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
            // using dynamic arraylist - no need to specify length before usage
            List<Map> records = new ArrayList<>();

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

    // method for updating counter column for deletion/insertion
    private void updateCounter(String tableName) {

        try {
            ResultSet results = this.select(tableName, false);

            for (int i = 1; results.next(); i++) {
                String sql = "UPDATE " + tableName + " SET counter = " + i +
                        " WHERE id = " + results.getInt("id");
                this.connection.createStatement().execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for insertion
    // multiple data types storage
    // https://stackoverflow.com/questions/26162183/java-multiple-type-data-structure
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
                // can only be either string or double for this prototype
                if (values.get(i).getClass().getName().equals("java.lang.String")) {
                    preparedStatement.setString(i + 1, values.get(i).toString());
                } else preparedStatement.setDouble(i + 1, (int) values.get(i));

            }
            preparedStatement.executeUpdate();
            updateCounter(tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for deletion
    // deletes rows/records instance-wise
    public void delete(String tableName, String content) {

        try {

            ResultSet results = this.select(tableName, false);
            int columnCount = results.getMetaData().getColumnCount();

            while (results.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    if (results.getString(i).equals(content)) {
                        String statement = "DELETE FROM " + tableName + " WHERE " +
                                results.getMetaData().getColumnName(i) + " = \"" + content + "\"";
                        System.out.println(statement);
                        this.connection.createStatement().execute(statement);
                    }
                }
            }

            updateCounter(tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for deletion but index-wise (primary key)
    public void delete(String tableName, int index) {

        try {
            String statement = "DELETE FROM " + tableName + " WHERE id = " + index;
            System.out.println(statement);
            this.connection.createStatement().execute(statement);
            updateCounter(tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method for update/replacement index-wise only (primary key)
    public void update(String tableName, int index, String updatedContent) {

        try {
            String statement = "UPDATE " + tableName + " SET " + updatedContent + " WHERE id = " + index;
            System.out.println(statement);
            this.connection.createStatement().execute(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // to reset autoincrement: https://stackoverflow.com/questions/1601697/sqlite-reset-primary-key-field
    public void reset(String tableName) {

        try {
            this.connection.createStatement().execute("DELETE FROM " + tableName);
            this.connection.createStatement().execute("UPDATE sqlite_sequence SET seq = 0 WHERE name = \"" + tableName + "\"");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // method for custom sql
    public ResultSet customStatement(String tableName, String statement) {
        ResultSet resultSet;

        try {
            resultSet = this.connection.createStatement().executeQuery(statement);
            updateCounter(tableName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultSet;
    }

    public void customStatementVoid(String tableName, String statement) {

        try {
            this.connection.createStatement().execute(statement);
            updateCounter(tableName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

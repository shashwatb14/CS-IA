/*
    Links used:
    https://www.javatpoint.com/java-swing
    https://www.geeksforgeeks.org/how-to-add-external-jar-file-to-an-intellij-idea-project/
 */

// Swing for GUI

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        // [create databases] and set password for first-time use

        // connect to database
        DatabaseHandler database = new DatabaseHandler("database.db");
        database.select("authentication", true);

        // Authentication test
        Authentication loginEntry = new Authentication("Login");
        while (!loginEntry.isSuccess()) {
            // wtf is happening here
            System.out.print("");
        }

        if (loginEntry.isSuccess()) System.out.println("WORKS!"); // debugging

        // main window
        JFrame main = new JFrame();

        // tabs/categories
        JPanel tabs = new JPanel();

        // size of 1200 width, 800 height
        main.setSize(1200, 800);
        main.setVisible(true);
        main.setLayout(new BorderLayout());
        main.add(tabs, BorderLayout.NORTH);
        main.setLocationRelativeTo(null);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
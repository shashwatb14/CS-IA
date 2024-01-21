/*
 * Links used:
 * https://www.javatpoint.com/java-swing
 * https://www.geeksforgeeks.org/how-to-add-external-jar-file-to-an-intellij-idea-project/
 */

// Swing for GUI
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        // [create databases] and set password for first-time use
        // TODO

        // connect to database
        DatabaseHandler database = new DatabaseHandler("database.db");
        database.select("authentication", true);

        // Authentication test
        // polymorphism
        new Authentication("Login", () -> {

            System.out.println("Giving access to main..."); // debugging

            JFrame main = buildMain();
            buildSections(main);
        });
    }

    public static JFrame buildMain() {

        // main window
        JFrame main = new JFrame();

        // size of 1200 width, 800 height
        main.setSize(1200, 800);
        main.setVisible(true);
        main.setLayout(new BorderLayout());
        main.setLocationRelativeTo(null);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        return main;
    }

    public static void buildSections(JFrame main) {
        // tabs/categories
        JPanel tabs = new JPanel();
        tabs.setLayout(new BoxLayout(tabs, BoxLayout.Y_AXIS));

        // border - https://stackoverflow.com/questions/46572625/how-to-change-width-size-of-jpanels-in-borderlayout
        // spacing - https://stackoverflow.com/questions/8335997/how-can-i-add-a-space-in-between-two-buttons-in-a-boxlayout
        tabs.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tabs.setBackground(Color.LIGHT_GRAY);

        // pre-adding default sections
        new Section("Personal Information", true, tabs);
        new Section("Individual Productivity", false, tabs);
        new Section("Office Workspace", false, tabs);
        new Section("Self-Learning", false, tabs);
        new Section("Family Travel", false, tabs);

        JButton addButton = new JButton("Add new space");
        tabs.add(addButton);

        main.add(tabs, BorderLayout.WEST);
    }
}
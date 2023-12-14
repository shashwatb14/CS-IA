/*
    Links used:
    https://www.javatpoint.com/java-swing

    Execution:
    javac .\Main.java
    java -classpath ".;sqlite-jdbc-3.44.1.0.jar" Main

    SQL Commands:

 */

// Swing for GUI

public class Main {
    public static void main(String[] args) {

        // create databases and set password for first-time use

        /* DatabaseHandler databaseHandler = new DatabaseHandler();
        try {
            databaseHandler.connect("C:\\Shashwat\\school\\IB (2022 - 2024)\\CS\\CS-IA\\authentication.db");
            System.out.println("Successfully connected to path!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } */

        // Authentication test
        Authentication passwordEntry = new Authentication();
        while (!passwordEntry.isSuccess()) {
            if(passwordEntry.isSuccess()) break;
        }
        System.out.println("TEST");


        /*
        // main window
        JFrame main = new JFrame();

        // tabs/categories
        JPanel tabs = new JPanel();

        // size of 800 wide, 600 tall
        main.setSize(800, 600);
        main.setVisible(true);
        main.setLayout(new BorderLayout());
        main.add(tabs, BorderLayout.NORTH);
        main.setLocationRelativeTo(null);*/
    }
}
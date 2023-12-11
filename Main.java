/*
    Links used:
    https://www.javatpoint.com/java-swing

    Execution:
    javac .\Main.java
    java -classpath ".;sqlite-jdbc.jar" Main
 */

// Swing for GUI
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        // main window
        JFrame main = new JFrame();

        // tabs/categories
        JPanel tabs = new JPanel();

        // size of 800 wide, 600 tall
        main.setSize(800, 600);
        main.setVisible(true);
        main.setLayout(new BorderLayout());
        main.add(tabs, BorderLayout.NORTH);
        main.setLocationRelativeTo(null);
    }
}
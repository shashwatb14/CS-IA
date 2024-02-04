/*
 * Links used:
 * https://www.javatpoint.com/java-swing
 * https://www.geeksforgeeks.org/how-to-add-external-jar-file-to-an-intellij-idea-project/
 */

// Swing for GUI
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static JFrame mainFrame;
    private static DatabaseHandler database;
    private static JPanel cards;

    public static void main(String[] args) {

        // connect to database
        database = new DatabaseHandler("database.db");
        database.select("authentication", true);

        // to reset application
        database.reset("authentication");
        database.reset("sections");
        database.reset("notes");

        // Authentication test
        // polymorphism
        new Authentication("Login", () -> {
            System.out.println("Giving access to main..."); // debugging
            buildApplication();
        }, database);
    }

    public static void buildApplication() {
        cards = new JPanel(new CardLayout());
        cards.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<Map> records = database.select("sections", new String[] {"sectionTitle"});

        // automated creation based on sections in database
        for (int i = 0, n = records.size(); i < n; i++) {
            String sectionTitle = records.get(i).get("sectionTitle").toString();
            JPanel card = new Editor(i + 1, sectionTitle, database).getPanel();
            cards.add(card, sectionTitle);
        }

        mainFrame = buildFrame();
        buildMain();
    }

    private static JFrame buildFrame() {

        // main window
        mainFrame = new JFrame();

        // size of 1200 width, 800 height
        mainFrame.setSize(1200, 800);
        mainFrame.setVisible(true);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        return mainFrame;
    }

    private static void buildMain() {
        // tabs/categories
        JPanel tabs = new JPanel();
        tabs.setLayout(new BoxLayout(tabs, BoxLayout.Y_AXIS));

        // border - https://stackoverflow.com/questions/46572625/how-to-change-width-size-of-jpanels-in-borderlayout
        // spacing - https://stackoverflow.com/questions/8335997/how-can-i-add-a-space-in-between-two-buttons-in-a-boxlayout
        tabs.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tabs.setBackground(Color.LIGHT_GRAY);

        // read sections from database
        for (int i = 0, n = database.select("sections", new String[] {"sectionTitle"}).size(); i < n; i++) {

            Map record = database.select(
                    "sections", new String[] {"sectionTitle", "isLocked"}
            ).get(i);
            new Section(
                    (String) record.get("sectionTitle"),
                    record.get("isLocked").equals("TRUE"),
                    tabs,
                    cards,
                    database
            );
        }

        JButton addButton = getjButton();
        tabs.add(addButton);

        JScrollPane menu = new JScrollPane(tabs);
        menu.setLayout(new ScrollPaneLayout());
        menu.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menu.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        mainFrame.add(menu, BorderLayout.WEST);
        mainFrame.add(cards, BorderLayout.CENTER);
    }

    private static JButton getjButton() {
        JButton addButton = new JButton("Add new section");
        changeCursor(addButton, new Cursor(Cursor.HAND_CURSOR));

        addButton.addActionListener(e -> {
            System.out.println("Adding new space...");
            createSection(newSection("Create new section", "Create section", false, null, null), true, 0);
        });

        return addButton;
    }

    public static Map<String, Object> newSection(String title, String buttonLabel, boolean editing, String sectionTitle, Boolean locked) {
        // create GUI
        JFrame newSectionFrame = new JFrame("Section");

        // main password field
        JPanel mainPanel = new JPanel();

        // title panel
        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel(title));

        JPanel sectionTitlePanel = new JPanel();
        JTextField titleField = new JTextField(20);
        if (editing) titleField.setText(sectionTitle);
        sectionTitlePanel.add(new JLabel("Enter section title: "));
        sectionTitlePanel.add(titleField);

        JPanel checkBoxPanel = new JPanel();
        JCheckBox isLocked = new JCheckBox();
        if (editing && locked) isLocked.setSelected(true);
        changeCursor(isLocked, new Cursor(Cursor.HAND_CURSOR));
        checkBoxPanel.add(new JLabel("Is Locked: "));
        checkBoxPanel.add(isLocked);

        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton(buttonLabel);
        changeCursor(createButton, new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(createButton);

        JPanel resultPanel = new JPanel();
        JLabel newTitlePanel = new JLabel("");
        resultPanel.add(newTitlePanel);

        mainPanel.add(titlePanel);
        buildPanel(newSectionFrame, mainPanel, sectionTitlePanel, checkBoxPanel, buttonPanel, resultPanel);

        Map<String, Object> components = new HashMap<>();
        components.put("createButton", createButton);
        components.put("titleField", titleField);
        components.put("newTitlePanel", newTitlePanel);
        components.put("isLocked", isLocked);
        components.put("newSectionFrame", newSectionFrame);
        return components;
    }

    public static void createSection(Map<String, Object> components, boolean creating, Integer index) {

        JButton createButton = (JButton) components.get("createButton");
        JTextField titleField = (JTextField) components.get("titleField");
        JLabel newTitlePanel = (JLabel) components.get("newTitlePanel");
        JCheckBox isLocked = (JCheckBox) components.get("isLocked");
        JFrame newSectionFrame = (JFrame) components.get("newSectionFrame");

        createButton.addActionListener(e -> {
            String title = titleField.getText();

            List<Map> records = database.select("sections", new String[] {"sectionTitle"});
            boolean isValid = true;

            // ensure no duplicates when creating
            for (int i = 0, n = records.size(); i < n; i++) {
                if (i != (index - 1) && records.get(i).get("sectionTitle").toString().equalsIgnoreCase(title.strip())) {
                    isValid = false;
                    newTitlePanel.setText("Section title already exists");
                    newTitlePanel.setForeground(Color.RED);
                    break;
                }
            }

            // user might want to only lock section
            if ( creating && title.isBlank()) {
                newTitlePanel.setText("Section title is blank");
                newTitlePanel.setForeground(Color.RED);
                isValid = false;
            }

            if (title.length() > 40) {
                newTitlePanel.setText("Section title is too long (" + (title.length() - 40) + " character(s) longer)");
                newTitlePanel.setForeground(Color.RED);
                isValid = false;
            }

            if (isValid) {

                List<Object> values = new ArrayList<>();

                // System.out.println(title); // debugging
                values.add(title);

                // checking checkboxes: https://www.javatpoint.com/java-jcheckbox
                if (isLocked.isSelected()) values.add("TRUE");
                else values.add("FALSE");

                if (creating) {
                    // create new section
                    database.insert("sections", "sectionTitle, isLocked", values);
                    newTitlePanel.setText("Creating new section...");
                    newTitlePanel.setForeground(Color.GREEN);
                }

                // otherwise editing
                else {
                    database.update("sections", index, "sectionTitle = \"" + title +
                                    "\", isLocked = \"" + values.get(1) + "\"");
                    newTitlePanel.setText("Saving changes...");
                    newTitlePanel.setForeground(Color.GREEN);
                }

                // rebuild
                buildApplication();

                newSectionFrame.dispatchEvent(new WindowEvent(newSectionFrame, WindowEvent.WINDOW_CLOSING));

            }
        });
    }

    public static void changeCursor(JComponent jComponent, Cursor cursor) {
        jComponent.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                jComponent.setCursor(cursor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                jComponent.setCursor(cursor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                jComponent.setCursor(cursor);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                jComponent.setCursor(cursor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                jComponent.setCursor(cursor);
            }
        });
    }

    public static void buildPanel(JFrame newSectionFrame, JPanel mainPanel, JPanel sectionTitlePanel, JPanel checkBoxPanel, JPanel buttonPanel, JPanel resultPanel) {
        mainPanel.add(sectionTitlePanel);
        mainPanel.add(checkBoxPanel);
        mainPanel.add(buttonPanel);
        mainPanel.add(resultPanel);

        buildDialogBox(newSectionFrame, mainPanel);
    }

    static void buildDialogBox(JFrame newSectionFrame, JPanel mainPanel) {
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        newSectionFrame.setResizable(false); // disables maximize button
        newSectionFrame.setVisible(true);
        newSectionFrame.setLayout(new FlowLayout());
        newSectionFrame.add(mainPanel);
        newSectionFrame.pack();
        newSectionFrame.setLocationRelativeTo(null); // puts frame in the middle
        newSectionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
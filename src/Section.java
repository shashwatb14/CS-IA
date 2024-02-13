/*
 * Class for different sections/categories
 *
 * Links used:
 * differentiating buttons - https://stackoverflow.com/questions/17143871/how-do-i-differentiate-between-two-different-jbuttons-in-actionperformed
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Section implements ActionListener {

    private final boolean LOCKED;

    private final JButton EDIT_BUTTON = new JButton("Edit");
    private final JButton ARCHIVE_BUTTON = new JButton("Archive");
    private final DatabaseHandler DATABASE;

    // section title
    private final String SECTION_NAME;

    // access to cards Panel
    private final JPanel CARDS;

    private final int SECTION_ID;


    public Section(String title, boolean locked, JPanel panel, JPanel cards, DatabaseHandler database) {

        this.LOCKED = locked;
        this.SECTION_NAME = title;
        this.CARDS = cards;
        this.DATABASE = database;

        try {
            this.SECTION_ID = DATABASE.customStatement(
                    "sections",
                    "SELECT id FROM sections WHERE sectionTitle = \"" + this.SECTION_NAME + "\""
            ).getInt("id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // styling buttons: https://stackoverflow.com/questions/4898584/java-using-an-image-as-a-button
        // clickable panels
        JButton MAIN_BUTTON = new JButton();
        MAIN_BUTTON.setContentAreaFilled(false);
        MAIN_BUTTON.setBorderPainted(false);

        JPanel buttonsPanel = new JPanel();
        JPanel titlePanel = new JPanel();
        JPanel lockPanel = new JPanel();

        JLabel lockStatus;
        if (this.isLocked()) {
            lockStatus = new JLabel("Locked");
            lockStatus.setForeground(Color.BLUE);
        } else {
            lockStatus = new JLabel("Unlocked");
            lockStatus.setForeground(Color.RED);
        }

        JLabel sectionTitle = new JLabel("  " + title + "  ");
        titlePanel.add(sectionTitle);
        lockPanel.add(lockStatus);
        MAIN_BUTTON.add(titlePanel);
        MAIN_BUTTON.add(lockPanel);

        EDIT_BUTTON.addActionListener(this);
        ARCHIVE_BUTTON.addActionListener(this);
        MAIN_BUTTON.addActionListener(this);

        // update cursor to pointer
        Main.changeCursor(MAIN_BUTTON, new Cursor(Cursor.HAND_CURSOR));

        buttonsPanel.add(EDIT_BUTTON);
        buttonsPanel.add(ARCHIVE_BUTTON);
        MAIN_BUTTON.add(buttonsPanel);

        MAIN_BUTTON.setLayout(new BoxLayout(MAIN_BUTTON, BoxLayout.Y_AXIS));
        panel.add(MAIN_BUTTON); // add mainButton to panel in frame
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    public boolean isLocked() {
        return LOCKED;
    }

    private void archive() {
        // GUI frame
        JFrame archiveFrame = new JFrame("Archive");

        // main panel
        JPanel mainPanel = new JPanel();

        // panel for warnings
        JPanel warnings = new JPanel();
        warnings.setLayout(new BoxLayout(warnings, BoxLayout.Y_AXIS));

        JPanel warningTitle = new JPanel();
        JPanel warningPanel1 = new JPanel();
        JPanel warningPanel2 = new JPanel();
        JPanel warningPanel3 = new JPanel();

        JLabel warning = new JLabel("WARNING");
        warning.setForeground(Color.RED);
        warningTitle.add(warning);

        warningPanel1.add(new JLabel("Are you sure you want to archive this section?"));
        warningPanel2.add(new JLabel("Sections cannot be restored through the application."));
        warningPanel3.add(new JLabel("All notes will become inaccessible."));

        warnings.add(warningTitle);
        warnings.add(warningPanel1);
        warnings.add(warningPanel2);
        warnings.add(warningPanel3);

        // panel for choice
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        JButton confirmArchive = new JButton("Archive");
        JButton cancelArchive = new JButton("Cancel");

        confirmArchive.addActionListener(e -> {
            List<Object> values = new ArrayList<>();
            values.add(this.SECTION_ID);
            values.add(this.SECTION_NAME);
            values.add((this.LOCKED) ? "TRUE" : "FALSE");

            DATABASE.insert("archive", "id, sectionTitle, isLocked", values);
            DATABASE.delete("sections", this.SECTION_NAME);

            Main.buildApplication();
            archiveFrame.dispatchEvent(new WindowEvent(archiveFrame, WindowEvent.WINDOW_CLOSING));
        });

        cancelArchive.addActionListener(e -> archiveFrame.dispatchEvent(new WindowEvent(archiveFrame, WindowEvent.WINDOW_CLOSING)));

        Main.changeCursor(confirmArchive, new Cursor(Cursor.HAND_CURSOR));
        Main.changeCursor(cancelArchive, new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(confirmArchive, BorderLayout.WEST);
        buttonPanel.add(cancelArchive, BorderLayout.EAST);

        mainPanel.add(warnings);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(buttonPanel);

        Main.buildDialogBox(archiveFrame, mainPanel);
    }

    // edit title and locked status of section
    private void edit() {
        Main.createSection(
                Main.newSection(
                        "Edit section",
                        "Save changes",
                        true,
                        this.SECTION_NAME,
                        this.LOCKED
                ),
                false,
                this.SECTION_ID
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // first check if locked
        if (this.isLocked()) {
            // lambda function
            new Authentication("Access " + SECTION_NAME, () -> {
                System.out.println("Giving access to " + SECTION_NAME + " ...");

                if (e.getSource() == EDIT_BUTTON) {
                    System.out.println("Editing " + this.SECTION_NAME);
                    edit();
                }

                // archive section
                else if (e.getSource() == ARCHIVE_BUTTON) {
                    System.out.println("Archiving " + this.SECTION_NAME);
                    archive();
                }

                // change view and give access to notes regardless if authentication is successful
                // card layout and switching: https://docs.oracle.com/javase%2Ftutorial%2Fuiswing%2F%2F/layout/card.html
                CardLayout cardLayout = (CardLayout) (CARDS.getLayout());
                cardLayout.show(CARDS, SECTION_NAME);
            }, DATABASE);
        } else {

            // edit section title
            if (e.getSource() == EDIT_BUTTON) {
                System.out.println("Editing " + this.SECTION_NAME);
                edit();
            }

            // archive section
            else if (e.getSource() == ARCHIVE_BUTTON) {
                System.out.println("Archiving " + this.SECTION_NAME);
                archive();
            }

            // change view using CardLayout
            else {
                System.out.println(SECTION_NAME);
                CardLayout cardLayout = (CardLayout) (CARDS.getLayout());
                cardLayout.show(CARDS, SECTION_NAME);
            }
        }
    }
}

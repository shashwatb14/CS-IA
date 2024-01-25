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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Section implements ActionListener {

    private final boolean LOCKED;

    private final JButton MAIN_BUTTON = new JButton(); // clickable panels
    private final JButton EDIT_BUTTON = new JButton("Edit");
    private final JButton ARCHIVE_BUTTON = new JButton("Archive");
    private final DatabaseHandler DATABASE;

    // section title
    private final String SECTION_NAME;

    // access to cards Panel
    private final JPanel CARDS;


    public Section(String title, boolean locked, JPanel panel, JPanel cards, DatabaseHandler database) {

        this.LOCKED = locked;
        this.SECTION_NAME = title;
        this.CARDS = cards;
        this.DATABASE = database;

        // styling buttons: https://stackoverflow.com/questions/4898584/java-using-an-image-as-a-button
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
        MAIN_BUTTON.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MAIN_BUTTON.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                MAIN_BUTTON.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                MAIN_BUTTON.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                MAIN_BUTTON.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                MAIN_BUTTON.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

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

    @Override
    public void actionPerformed(ActionEvent e) {

        // first check if locked
        if (this.isLocked()) {
            // lambda function
            new Authentication("Access " + SECTION_NAME, () -> {
                System.out.println("Giving access to " + SECTION_NAME + " ...");

                if (e.getSource() == EDIT_BUTTON) {
                    System.out.println("Editing " + this.SECTION_NAME);
                }

                // delete section
                else if (e.getSource() == ARCHIVE_BUTTON) {
                    System.out.println("Deleting " + this.SECTION_NAME);
                }


                // change view and give access to notes regardless if authentication is successful
                // card layout and switching: https://docs.oracle.com/javase%2Ftutorial%2Fuiswing%2F%2F/layout/card.html
                CardLayout cardLayout = (CardLayout) (CARDS.getLayout());
                cardLayout.show(CARDS, SECTION_NAME);
            }, DATABASE);
        }

        else {

            // edit section title
            if (e.getSource() == EDIT_BUTTON) {
                System.out.println("Editing " + this.SECTION_NAME);
            }

            // delete section
            else if (e.getSource() == ARCHIVE_BUTTON) {
                System.out.println("Deleting " + this.SECTION_NAME);
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

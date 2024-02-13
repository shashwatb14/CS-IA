/*
 * Editor class for each section
 *
 * Sources:
 * https://stackoverflow.com/questions/14170041/is-it-possible-to-create-programs-in-java-that-create-text-to-link-in-chrome
 */

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// key listener: https://sandeepdass003.wordpress.com/2017/03/04/how-to-use-textfield-textarea-and-keylistener-keyevent/
public class Editor implements KeyListener, ActionListener {

    private final JPanel CONTENT = new JPanel();
    private final JEditorPane EDITOR_PANE = new JEditorPane();
    private final DatabaseHandler DATABASE;
    private final int SECTION_ID;
    private final JButton EDIT_BUTTON;
    private final JPanel COPY_BUTTONS = new JPanel();

    public Editor(int index, String sectionTitle, DatabaseHandler database) {

        this.DATABASE = database;

        try {
            this.SECTION_ID = DATABASE.customStatement(
                    "sections",
                    "SELECT id FROM sections WHERE sectionTitle = \"" + sectionTitle + "\""
            ).getInt("id");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        CONTENT.setLayout(new BorderLayout());
        Main.changeCursor(EDITOR_PANE, new Cursor(Cursor.TEXT_CURSOR));

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel((index + ". " + sectionTitle)));

        JPanel editor = new JPanel();
        editor.setLayout(new BorderLayout());

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = createScrollPane();
        row.add(scrollPane);
        row.add(Box.createRigidArea(new Dimension(0, 10)));
        editor.add(row, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JPanel buttonsLabel = new JPanel();
        buttonsLabel.setLayout(new BoxLayout(buttonsLabel, BoxLayout.Y_AXIS));
        buttonsLabel.add(new JLabel("Click to copy to clipboard:"));
        buttonsLabel.add(Box.createRigidArea(new Dimension(0, 5)));

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(buttonsLabel, BorderLayout.NORTH);

        COPY_BUTTONS.setLayout(new FlowLayout());
        COPY_BUTTONS.add(Box.createRigidArea(new Dimension(5, 0)));

        JPanel copyButtonsPanel = new JPanel();
        copyButtonsPanel.setLayout(new BoxLayout(copyButtonsPanel, BoxLayout.Y_AXIS));
        copyButtonsPanel.add(COPY_BUTTONS);

        JScrollPane buttonScrollPane = new JScrollPane(copyButtonsPanel);
        buttonScrollPane.setLayout(new ScrollPaneLayout());
        buttonScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        buttonScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        buttonPanel.add(buttonScrollPane, BorderLayout.CENTER);

        editor.add(buttonPanel, BorderLayout.SOUTH);

        EDIT_BUTTON = new JButton("Edit");
        EDIT_BUTTON.addActionListener(this);
        Main.changeCursor(EDIT_BUTTON, new Cursor(Cursor.HAND_CURSOR));

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
        editPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        editPanel.add(EDIT_BUTTON);

        CONTENT.add(titlePanel, BorderLayout.NORTH);
        CONTENT.add(editor, BorderLayout.CENTER);
        CONTENT.add(editPanel, BorderLayout.SOUTH);
    }

    private JScrollPane createScrollPane() {

        String content = getContent();

        EDITOR_PANE.setContentType("text/html");

        // hyperlinks: https://stackoverflow.com/questions/14170041/is-it-possible-to-create-programs-in-java-that-create-text-to-link-in-chrome
        EDITOR_PANE.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        EDITOR_PANE.setFont(new Font(Font.SERIF, Font.PLAIN, 14));
        EDITOR_PANE.setText(convertTextToHtml(content)); // https://www.tutorialspoint.com/how-to-set-font-for-text-in-jtextpane-with-javaw
        EDITOR_PANE.addKeyListener(this);
        EDITOR_PANE.setEditable(false);
        EDITOR_PANE.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // scroll pane: https://docs.oracle.com/javase%2Ftutorial%2Fuiswing%2F%2F/components/textarea.html
        return getjScrollPane();
    }

    private JScrollPane getjScrollPane() {
        JScrollPane scrollPane = new JScrollPane(EDITOR_PANE);
        scrollPane.setLayout(new ScrollPaneLayout());

        // scroll pane policy: https://stackoverflow.com/questions/66641158/scrollpane-with-textarea-inside-container-doesnt-show-up
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    public JPanel getPanel() {
        return this.CONTENT;
    }

    @Override
    public void keyTyped(KeyEvent e) {

        if (e.getKeyChar() == '/') {
            System.out.println("FORMULA");
        }
        // get text: https://stackoverflow.com/questions/40855387/how-to-get-the-text-of-a-textarea-when-button-is-clicked-in-java
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // System.out.println(e.getKeyChar());
        // auto complete bracket functionality: https://stackoverflow.com/questions/11442471/press-a-key-with-java
        switch (e.getKeyChar()) {
            case '(' -> {
                Robot r;
                try {
                    r = new Robot();
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                }
                r.keyPress(KeyEvent.VK_SHIFT);
                r.keyPress(KeyEvent.VK_0);
                r.keyRelease(KeyEvent.VK_SHIFT);
                r.keyRelease(KeyEvent.VK_0);
                r.keyPress(KeyEvent.VK_LEFT);
                r.keyRelease(KeyEvent.VK_LEFT);
            }
            case '{' -> {
                Robot r;
                try {
                    r = new Robot();
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                }
                r.keyPress(KeyEvent.VK_SHIFT);
                r.keyPress(KeyEvent.VK_CLOSE_BRACKET);
                r.keyRelease(KeyEvent.VK_SHIFT);
                r.keyRelease(KeyEvent.VK_CLOSE_BRACKET);
                r.keyPress(KeyEvent.VK_LEFT);
                r.keyRelease(KeyEvent.VK_LEFT);
            }
            case '[' -> {
                Robot r;
                try {
                    r = new Robot();
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                }
                r.keyPress(KeyEvent.VK_CLOSE_BRACKET);
                r.keyRelease(KeyEvent.VK_CLOSE_BRACKET);
                r.keyPress(KeyEvent.VK_LEFT);
                r.keyRelease(KeyEvent.VK_LEFT);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // System.out.println(e.getKeyChar());

        if (EDITOR_PANE.isEditable()) {

            System.out.println("Saving...");
            String content = EDITOR_PANE.getText();

            saveContent(content);
        }
    }

    // allow for editing and saving content so client can view/write notes
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == EDIT_BUTTON) {
            if (!EDITOR_PANE.isEditable()) {
                // reset copiable buttons
                COPY_BUTTONS.removeAll();

                EDITOR_PANE.setContentType("text");
                EDITOR_PANE.setText(getContent());
                System.out.println("Editing...");
                EDIT_BUTTON.setText("Save");
                EDITOR_PANE.setEditable(true);
            } else {
                saveContent(EDITOR_PANE.getText());
                EDITOR_PANE.setContentType("text/html");
                EDITOR_PANE.setText(convertTextToHtml(getContent()));
                EDITOR_PANE.setEditable(false);
                EDIT_BUTTON.setText("Edit");
                System.out.println("Returning...");
            }
        } else {
            // get button text: https://stackoverflow.com/questions/7867834/get-button-name-from-actionlistener
            JButton jButton = (JButton) e.getSource();
            String toCopy = jButton.getText();

            // copy to clipboard: https://stackoverflow.com/questions/6710350/copying-text-to-the-clipboard-using-java
            StringSelection stringSelection = new StringSelection(toCopy);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }

    private void saveContent(String content) {
        List<Object> record = new ArrayList<>();
        record.add(this.SECTION_ID);
        record.add(content);

        try {
            if (DATABASE.customStatement(
                    "notes",
                    "SELECT * FROM notes WHERE section_id = " + this.SECTION_ID
            ).getString("content") == null) {
                DATABASE.insert("notes", "section_id, content", record);
            } else {
                DATABASE.customStatementVoid("notes", "UPDATE notes SET content = \"" + content +
                        "\" WHERE section_id = " + this.SECTION_ID);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getContent() {
        String content;
        try {
            content = DATABASE.customStatement(
                    "notes",
                    "SELECT content FROM notes WHERE section_id = " + this.SECTION_ID
            ).getString("content");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    private String convertFormulas(String content) {
        if (content == null) return "";
        StringBuilder html = new StringBuilder();
        char[] chars = content.toCharArray();
        int n = chars.length;
        final int LEN = 5;
        int end = 0;

        // fix glitch
        if (n <= LEN) return content;

        for (int i = 0; i < n - LEN; i++) {

            // simple "formulas"
            // convert to hyperlinks
            switch (content.substring(i, i + LEN)) {
                case "/lnk{" -> {
                    end = findEnd(i, n, chars);
                    if (end == 0) {
                        html.append(chars[i]);
                        continue;
                    }
                    html.append("<a href=\"").append(content, i + LEN, end).append("\">").append(content, i + LEN, end).append("<a>");
                    i = end;
                }
                case "/hlt{" -> {
                    end = findEnd(i, n, chars);
                    if (end == 0) {
                        html.append(chars[i]);
                        continue;
                    }
                    html.append("<span style=\"background-color: #FFF200;\">").append(content, i + LEN, end).append("</span>");
                    i = end;
                }
                case "/bld{" -> {
                    end = findEnd(i, n, chars);
                    if (end == 0) {
                        html.append(chars[i]);
                        continue;
                    }
                    html.append("<span style=\"font-weight: bold;\">").append(content, i + LEN, end).append("</span>");
                    i = end;
                }
                case "/und{" -> {
                    end = findEnd(i, n, chars);
                    if (end == 0) {
                        html.append(chars[i]);
                        continue;
                    }
                    html.append("<span style=\"text-decoration: underline;\">").append(content, i + LEN, end).append("</span>");
                    i = end;
                }
                case "/cpy{" -> {
                    end = findEnd(i, n, chars);
                    if (end == 0) {
                        html.append(chars[i]);
                        continue;
                    }
                    JButton jButton = new JButton(content.substring(i + LEN, end));
                    jButton.addActionListener(this);
                    Main.changeCursor(jButton, new Cursor(Cursor.HAND_CURSOR));

                    COPY_BUTTONS.add(jButton);
                    html.append("<code><span style=\"background-color: #DDDDDD;\">")
                            .append(content, i + LEN, end).append("</span></code>");
                    i = end;
                }
                default -> html.append(chars[i]);
            }
        }

        if (end < n - LEN) html.append(content, n - LEN, n);
        else if (end > n - LEN && end < n) html.append(content, end + 1, n);

        return html.toString();
    }

    private int findEnd(int i, int n, char[] chars) {
        int end = 0;
        for (int j = i + 5; j < n; j++) {
            if (chars[j] == '}') {
                end = j;
                break;
            }
        }
        if (end == 0) System.out.println("ERROR - FORMULA NOT CLOSED");
        return end;
    }

    private String convertTextToHtml(String content) {
        StringBuilder html = new StringBuilder();
        content = convertFormulas(content);
        char[] chars = content.toCharArray();

        for (char aChar : chars) {
            try {
                if (aChar == '\n') html.append("<br>");
                    // spaces in html: blog.hubspot.com/website/html-space
                else if (aChar == '\t') html.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                else html.append(aChar);
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
            }
        }

        return html.toString();
    }
}

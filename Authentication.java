/*
 * Authentication class for password protection
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Authentication implements ActionListener {

    // global password field attribute for actionPerformed method
    private JPasswordField passwordField = new JPasswordField(20);
    private String password = "";

    // constructor
    public Authentication() {

        // main frame
        JFrame frame = new JFrame("Authentication");

        // main panel
        JPanel panel = new JPanel();

        JPanel resultPanel = new JPanel();

        // password label, and button
        JLabel passwordLabel = new JLabel("Password: ");
        JButton submitButton = new JButton("Submit");

        // passwordLabel.setFont(new Font("Verdana", Font.PLAIN, 12));

        // add action listeners to password field and button
        passwordField.addActionListener(this);
        submitButton.addActionListener(this);

        // add password, label and button to panel
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(submitButton);

        // configure frame
        frame.setResizable(false); // disables maximize button
        frame.setVisible(true);
        frame.setLayout(new CardLayout());
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // validate password
    private boolean validatePassword(char[] password) {
        if (password.length < 8 || password.length > 20) return false;
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        char[] input = passwordField.getPassword();

        if (!validatePassword(input)) {
            System.out.println("ERROR - invalid password length");

            // update screen
        } else {
            // give access
        }


        // resetting every time action is performed
        password = "";

        // constructing password as a string
        for (char c : input) {
            password += c;
        }
        System.out.println(password);
        if (password.equals("test")) System.out.println("YES");
    }

    public static void main(String[] args)   {
        Authentication passwordEntry = new Authentication();
    }

}

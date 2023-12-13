/*
 * Authentication class for password protection
 *
 * Specific snippets from sources
 * Closing a JFrame - https://stackoverflow.com/questions/1234912/how-to-programmatically-close-a-jframe
 */

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Authentication implements ActionListener {

    // global password field attribute for actionPerformed method
    private final JPasswordField passwordField;

    // global result label field for updates
    private final JLabel result;

    // determines access
    private boolean success = false;

    // encoded actual password
    private final static String correct = "password"; // get password from database

    // main frame
    JFrame frame = new JFrame("Authentication");

    // constructor
    public Authentication() {

        // main panel
        JPanel mainPanel = new JPanel();

        // panel for input
        JPanel panel = new JPanel();

        // panel to inform user on status
        JPanel resultPanel = new JPanel();

        // password label, result label and button
        JLabel passwordLabel = new JLabel("Password: ");
        JButton submitButton = new JButton("Submit");

        // add action listeners to password field and button
        passwordField = new JPasswordField(20);
        passwordField.addActionListener(this);
        submitButton.addActionListener(this);

        // add password, label and button to panel
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(submitButton);

        result = new JLabel("Enter Password");
        resultPanel.add(result);

        // merge two panels vertically
        mainPanel.add(panel);
        mainPanel.add(resultPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // configure frame
        frame.setResizable(false); // disables maximize button
        frame.setVisible(true);
        frame.setLayout(new FlowLayout());
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null); // puts frame in the middle
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // validate password
    private boolean checkPassword(String password) {
        return password.equals(correct);
    }

    // encrypt password
    // need the same key for decryption - key only changes when password changes
    // Raised exceptions include: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
    // IllegalBlockSizeException, BadPaddingException
    private String encryptPassword(String password, SecretKey secretKey, IvParameterSpec ivParameterSpec) throws Exception {

        // most of the code from Bard for encryption
        // given sources:
        // https://www.baeldung.com/java-aes-encryption-decryption,
        // https://www.tutorialspoint.com/java_cryptography/index.htm,
        // https://jenkov.com/tutorials/java-cryptography/index.html

        // convert password to byte array
        byte[] plainText = password.getBytes(StandardCharsets.UTF_8);

        // initialize cipher object
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec); // int, key, iv

        // encrypt and encode
        byte[] encryptedText = cipher.doFinal(plainText);
        String encodedText = Base64.getEncoder().encodeToString(encryptedText);

        System.out.println("Encrypted: " + encodedText);
        return encodedText;
    }

    // painstakingly reverse-engineered
    private String decryptPassword(String encodedText, SecretKey secretKey, IvParameterSpec ivParameterSpec) throws Exception {
        // initializing cipher object
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec); // initialization vector required

        byte[] encryptedText = Base64.getDecoder().decode(encodedText);
        byte[] decryptedText = cipher.doFinal(encryptedText);
        String result = new String(decryptedText, StandardCharsets.UTF_8);

        System.out.println("Decrypted: " + result);
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        char[] input = passwordField.getPassword();

        // resetting every time action is performed
        StringBuilder password = new StringBuilder();

        // constructing password as a string
        for (char c : input) {
            password.append(c);
        }

        try {
            // generate secure key for AES encryption
            // source - https://stackoverflow.com/questions/51770704/java-aes-decryption-code-is-not-working-invalidexception-1234444
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecretKey secretKey = keyGenerator.generateKey();

            byte[] iv = new byte[16]; // 16 bytes long
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv); // set up iv

            // System.out.println(secretKey);
            System.out.println("What you typed: " + password);
            String encrypted = encryptPassword(password.toString(), secretKey, ivParameterSpec);
            decryptPassword(encrypted, secretKey, ivParameterSpec);

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }


        if (!checkPassword(password.toString())) {
            // update text
            result.setText("Wrong Password");
        } else {
            // give access and close window
            result.setText("Correct Password");
            this.success = true;
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); // close window after giving access
        }
    }

    public boolean isSuccess() {
        return this.success;
    }
}
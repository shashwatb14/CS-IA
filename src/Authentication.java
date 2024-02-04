/*
 * Authentication class for password protection
 *
 * Specific snippets from sources
 * Closing a JFrame - https://stackoverflow.com/questions/1234912/how-to-programmatically-close-a-jframe
 */

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Map;

public class Authentication implements ActionListener {

    // global password field attribute for actionPerformed method
    private final JPasswordField PASSWORD_FIELD;

    // for action listener to create new password
    private JPasswordField passwordField;
    private JPasswordField confirmPassword;
    private JButton createButton;
    private JLabel newPasswordResult;
    private JFrame setPasswordFrame;

    private static final int COLUMN_SIZE = 20;

    // global result label field for updates
    private final JLabel RESULT;

    // iv
    private static final byte[] IV_BYTE = new byte[16];
    private static final IvParameterSpec IV_PARAMETER_SPEC = new IvParameterSpec(IV_BYTE);

    // database handler
    private static DatabaseHandler authApp;

    // encoded actual password
    private final String CORRECT;

    // main frame - (try static for fun)
    private final JFrame FRAME = new JFrame("Authentication");

    // callback interface for authentication purposes
    private final AuthenticationCallback CALLBACK;

    // constructor
    public Authentication(String title, AuthenticationCallback callback, DatabaseHandler database) {

        this.CALLBACK = callback;
        authApp = database;
        this.CORRECT = decryptPassword(); // get password from database

        // main panel
        JPanel mainPanel = new JPanel();

        // panel for title
        JPanel titlePanel = new JPanel();

        // panel for input
        JPanel panel = new JPanel();

        // panel to inform user on status
        JPanel resultPanel = new JPanel();

        // password label, result label and button
        JLabel passwordLabel = new JLabel("Password: ");
        JButton submitButton = new JButton("Submit");

        // add action listeners to password field and button
        PASSWORD_FIELD = new JPasswordField(COLUMN_SIZE);
        PASSWORD_FIELD.addActionListener(this);
        submitButton.addActionListener(this);

        // change mouse to pointer - https://stackoverflow.com/questions/7359189/how-to-change-the-mouse-cursor-in-java
        Main.changeCursor(submitButton, new Cursor(Cursor.HAND_CURSOR));

        // add title to frame
        titlePanel.add(new JLabel(title));

        // add password, label and button to panel
        panel.add(passwordLabel);
        panel.add(PASSWORD_FIELD);
        panel.add(submitButton);

        RESULT = new JLabel("Enter Password");
        resultPanel.add(RESULT);

        // merge panels vertically
        mainPanel.add(titlePanel);
        mainPanel.add(panel);
        mainPanel.add(resultPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // configure frame
        FRAME.setResizable(false); // disables maximize button
        FRAME.setVisible(!authApp.select("authentication", new String[]{"encryptedText"}).isEmpty()); // genius intellij
        FRAME.setLayout(new FlowLayout());
        FRAME.add(mainPanel);
        FRAME.pack();
        FRAME.setLocationRelativeTo(null); // puts frame in the middle
        FRAME.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // validate password
    private boolean checkPassword(String password) {
        return password.equals(CORRECT);
    }

    // encrypt password
    // need the same key for decryption - key only changes when password changes
    // Raised exceptions include: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
    // IllegalBlockSizeException, BadPaddingException
    private String encryptPassword(String password, SecretKey secretKey) {

        // most of the code from Bard for encryption
        // given sources:
        // https://www.baeldung.com/java-aes-encryption-decryption,
        // https://www.tutorialspoint.com/java_cryptography/index.htm,
        // https://jenkov.com/tutorials/java-cryptography/index.html

        // convert password to byte array
        byte[] plainText = password.getBytes(StandardCharsets.UTF_8);

        // initialize cipher object
        byte[] encryptedText;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV_PARAMETER_SPEC); // int, key, iv

            // encrypt and encode
            encryptedText = cipher.doFinal(plainText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /* debugging
        System.out.println(encodedText);
        System.out.println(secretKey);
        System.out.println(IV_PARAMETER_SPEC);*/

        return Base64.getEncoder().encodeToString(encryptedText);
    }

    // painstakingly reverse-engineered
    private String decryptPassword() {
        // initializing cipher object
        byte[] decryptedText = new byte[0];

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // database handler to retrieve password
            Map intel = authApp.select("authentication", new String[]{"encryptedText"}).get(0);

            // reconstruct secret key
            // decipher from file
            // file to byte: https://stackoverflow.com/questions/858980/file-to-byte-in-java
            byte[] fileByte = Files.readAllBytes(Path.of("C:\\Shashwat\\school\\IB (2022 - 2024)\\CS\\CS-IA\\src\\SecretFile.key"));

            // reconstructing secret key: https://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa
            SecretKey newKey = new SecretKeySpec(fileByte, 0, fileByte.length, "AES");

            cipher.init(Cipher.DECRYPT_MODE, newKey, IV_PARAMETER_SPEC); // initialization vector required

            byte[] encryptedText = Base64.getDecoder().decode(intel.get("encryptedText").toString());
            decryptedText = cipher.doFinal(encryptedText);
        } catch (IndexOutOfBoundsException error) {
            // first time password setup
            setNewPassword();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new String(decryptedText, StandardCharsets.UTF_8);
    }

    private void setNewPassword() {

        // create GUI
        setPasswordFrame = new JFrame();

        // main password field
        JPanel mainPanel = new JPanel();

        // title panel
        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Create new password"));

        // inner password fields
        JPanel passwordPanel = new JPanel();
        JPanel confirmPanel = new JPanel();

        passwordField = new JPasswordField(COLUMN_SIZE);
        confirmPassword = new JPasswordField(COLUMN_SIZE);

        passwordPanel.add(new JLabel("Enter password: "));
        passwordPanel.add(passwordField);

        confirmPanel.add(new JLabel("Confirm password: "));
        confirmPanel.add(confirmPassword);

        JPanel buttonPanel = new JPanel();
        createButton = new JButton("Create password");
        Main.changeCursor(createButton, new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(createButton);

        JPanel resultPanel = new JPanel();
        newPasswordResult = new JLabel("Create new password");
        resultPanel.add(newPasswordResult);

        // decrypt and setNewPassword non-static because of action listeners
        confirmPassword.addActionListener(this);
        createButton.addActionListener(this);

        mainPanel.add(titlePanel);
        Main.buildPanel(setPasswordFrame, mainPanel, passwordPanel, confirmPanel, buttonPanel, resultPanel);
    }

    private SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecretKey secretKey = keyGenerator.generateKey();

            // write key to file: https://stackoverflow.com/questions/54665348/how-to-store-secretkey-and-iv-in-a-single-file-for-aes-encryption-and-decryption
            FileOutputStream outFile = new FileOutputStream("C:\\Shashwat\\school\\IB (2022 - 2024)\\CS\\CS-IA\\src\\SecretFile.key");
            byte[] key = secretKey.getEncoded();
            outFile.write(key);
            outFile.close();

            return secretKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == confirmPassword || e.getSource() == createButton) {
            System.out.println("Creating new password..."); // debugging

            // constructing string
            char[] password1 = passwordField.getPassword();
            char[] password2 = confirmPassword.getPassword();

            // resetting every time action is performed
            StringBuilder firstPassword = new StringBuilder();
            StringBuilder secondPassword = new StringBuilder();

            // constructing password as a string
            for (int i = 0, n = Math.min(password1.length, password2.length); i < n; i++) {
                firstPassword.append(password1[i]);
                secondPassword.append(password2[i]);
            }

            System.out.println("1st: " + firstPassword);
            System.out.println("2nd: " + secondPassword);

            // validation and confirmation checks
            if (firstPassword.length() < 3 || firstPassword.length() > 20 ||
                    secondPassword.length() < 3 || secondPassword.length() > 20) {

                System.out.println("Invalid length"); // debugging

                newPasswordResult.setText("Invalid Length");
                newPasswordResult.setForeground(Color.RED);

            } else if (firstPassword.toString().length() != secondPassword.toString().length()) {
                System.out.println("Different lengths for password...");

                System.out.println(firstPassword.toString().length());
                System.out.println(secondPassword.toString().length());

                newPasswordResult.setText("Passwords do not match");
                newPasswordResult.setForeground(Color.RED);

            } else if (firstPassword.toString().contentEquals(secondPassword)) { // string builders need to be converted to string
                System.out.println("Success! Creating new password..."); // debugging

                // update database
                SecretKey secretKey = generateKey();
                String encryptedText = encryptPassword(String.valueOf(firstPassword), secretKey);

                List<Object> encryptedPassword = new ArrayList<>();
                encryptedPassword.add(encryptedText);

                authApp.insert("authentication", "encryptedText", encryptedPassword);

                // add default sections since setting up for the first time
                authApp.customStatementVoid("sections", "INSERT INTO sections(sectionTitle, isLocked) VALUES('Personal Information', 'TRUE');");
                authApp.customStatementVoid("sections", "INSERT INTO sections(sectionTitle, isLocked) VALUES('Individual Productivity', 'FALSE');");
                authApp.customStatementVoid("sections", "INSERT INTO sections(sectionTitle, isLocked) VALUES('Office Workspace', 'FALSE');");
                authApp.customStatementVoid("sections", "INSERT INTO sections(sectionTitle, isLocked) VALUES('Self-Learning', 'FALSE');");
                authApp.customStatementVoid("sections", "INSERT INTO sections(sectionTitle, isLocked) VALUES('Family Travel', 'FALSE');");

                newPasswordResult.setText("Success");
                newPasswordResult.setForeground(Color.GREEN);

                CALLBACK.onAuthenticationSuccess();
                setPasswordFrame.dispatchEvent(new WindowEvent(setPasswordFrame, WindowEvent.WINDOW_CLOSING));

            } else {
                newPasswordResult.setText("Passwords do not match");
                newPasswordResult.setForeground(Color.RED);
            }

        } else {
            char[] input = PASSWORD_FIELD.getPassword();

            // resetting every time action is performed
            StringBuilder password = new StringBuilder();

            // constructing password as a string
            for (char c : input) {
                password.append(c);
            }

            System.out.println("What you typed: " + password);

            if (!checkPassword(password.toString())) {
                // update text
                RESULT.setText("Wrong Password");
                RESULT.setForeground(Color.RED);
            } else {
                // give access and close window
                RESULT.setText("Correct Password");
                RESULT.setForeground(Color.GREEN);

                // write new secretKey and encodedText to database
                // generate secure key for AES encryption
                // source - https://stackoverflow.com/questions/51770704/java-aes-decryption-code-is-not-working-invalidexception-1234444

                SecretKey secretKey = generateKey();

                String encryptedText = encryptPassword(String.valueOf(password), secretKey);

                /* for reset/debugging
                List<Object> encryptedPassword = new ArrayList<>();
                encryptedPassword.add(encryptedText);
                AUTH_APP.insert("authentication", "encryptedText", encryptedPassword);*/

                // update database
                authApp.update("authentication", 1, "encryptedText = \"" + encryptedText + "\"");

                // call method on success
                CALLBACK.onAuthenticationSuccess();
                FRAME.dispatchEvent(new WindowEvent(FRAME, WindowEvent.WINDOW_CLOSING)); // close window after giving access
            }
        }
    }
}
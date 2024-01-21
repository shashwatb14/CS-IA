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
import java.util.Base64;
import java.util.Map;

public class Authentication implements ActionListener {

    // global password field attribute for actionPerformed method
    private final JPasswordField PASSWORD_FIELD;

    // global result label field for updates
    private final JLabel RESULT;

    // iv
    private static final byte[] IV_BYTE = new byte[16];
    private static final IvParameterSpec IV_PARAMETER_SPEC = new IvParameterSpec(IV_BYTE);

    // database handler
    private static final DatabaseHandler AUTH_APP = new DatabaseHandler("database.db");

    // encoded actual password
    private static final String CORRECT = decryptPassword(); // get password from database

    // main frame
    private final JFrame FRAME = new JFrame("Authentication");

    // callback interface for authentication purposes
    private final AuthenticationCallback CALLBACK;

    // constructor
    public Authentication(String title, AuthenticationCallback callback) {

        this.CALLBACK = callback;

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
        PASSWORD_FIELD = new JPasswordField(20);
        PASSWORD_FIELD.addActionListener(this);
        submitButton.addActionListener(this);

        // change mouse to pointer - https://stackoverflow.com/questions/7359189/how-to-change-the-mouse-cursor-in-java
        submitButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

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
        FRAME.setVisible(true);
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
    private String encryptPassword(String password, SecretKey secretKey) throws Exception {

        // most of the code from Bard for encryption
        // given sources:
        // https://www.baeldung.com/java-aes-encryption-decryption,
        // https://www.tutorialspoint.com/java_cryptography/index.htm,
        // https://jenkov.com/tutorials/java-cryptography/index.html

        // convert password to byte array
        byte[] plainText = password.getBytes(StandardCharsets.UTF_8);

        // initialize cipher object
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV_PARAMETER_SPEC); // int, key, iv

        // encrypt and encode
        byte[] encryptedText = cipher.doFinal(plainText);

        /* debugging
        System.out.println(encodedText);
        System.out.println(secretKey);
        System.out.println(IV_PARAMETER_SPEC);*/

        return Base64.getEncoder().encodeToString(encryptedText);
    }

    // painstakingly reverse-engineered
    private static String decryptPassword() {
        // initializing cipher object
        byte[] decryptedText;

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // database handler to retrieve password
            Map<String, String> intel = AUTH_APP.select("authentication", new String[] {"encryptedText"}).get(0);

            // reconstruct secret key
            // decipher from file
            // file to byte: https://stackoverflow.com/questions/858980/file-to-byte-in-java
            byte[] fileByte = Files.readAllBytes(Path.of("C:\\Shashwat\\school\\IB (2022 - 2024)\\CS\\CS-IA\\SecretFile.key"));

            // reconstructing secret key: https://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa
            SecretKey newKey = new SecretKeySpec(fileByte, 0, fileByte.length, "AES");

            cipher.init(Cipher.DECRYPT_MODE, newKey, IV_PARAMETER_SPEC); // initialization vector required

            byte[] encryptedText = Base64.getDecoder().decode(intel.get("encryptedText"));
            decryptedText = cipher.doFinal(encryptedText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new String(decryptedText, StandardCharsets.UTF_8);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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

            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

                SecretKey secretKey = keyGenerator.generateKey();

                // write key to file: https://stackoverflow.com/questions/54665348/how-to-store-secretkey-and-iv-in-a-single-file-for-aes-encryption-and-decryption
                FileOutputStream outFile = new FileOutputStream("C:\\Shashwat\\school\\IB (2022 - 2024)\\CS\\CS-IA\\SecretFile.key");
                byte[] key = secretKey.getEncoded();
                outFile.write(key);
                outFile.close();

                String encryptedText = encryptPassword(String.valueOf(password), secretKey);

                // update database
                AUTH_APP.update("authentication", 1, "encryptedText = \"" + encryptedText + "\"");

            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }

            // call method on success
            CALLBACK.onAuthenticationSuccess();
            FRAME.dispatchEvent(new WindowEvent(FRAME, WindowEvent.WINDOW_CLOSING)); // close window after giving access
        }
    }
}
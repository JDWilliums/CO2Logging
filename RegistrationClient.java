import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class RegistrationClient {
    private static final String SERVER_IP = "127.0.0.1";  // localhost ip
    private static final int SERVER_PORT = 12345;         // Need to update to fix this, need to have custom port

    public RegistrationClient() {
        createRegistrationPage();
    }

    private void createRegistrationPage() {
        JFrame frame = new JFrame("Register");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(10, 10));

        // Logo Panel
        JPanel logoPanel = new JPanel(new FlowLayout());
        ImageIcon originalIcon = new ImageIcon("logo.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoPanel.add(logoLabel);
        frame.setIconImage(originalIcon.getImage());
        frame.add(logoPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel userIdLabel = new JLabel("User ID:");
        JTextField userIdField = new JTextField(20);

        JLabel emailLabel = new JLabel("Email:"); // we do not save email any where, might need to change or remove
        JTextField emailField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPasswordField = new JPasswordField(20);

        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);

        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");

        // Register Button Logic
        registerButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

            // basic checks
            if (userId.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setText("All fields are required.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match.");
                return;
            }

            // Build the REGISTER command - needed to send info to server 
            String command = "REGISTER," + userId + "," + email + "," + password;

            // One Command per Connection:
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                // Send the command
                writer.println(command);

                // Read the response
                String response = reader.readLine();
                if (response == null) {
                    messageLabel.setText("No response from server.");
                    return;
                }

                if ("REGISTER_SUCCESS".equalsIgnoreCase(response)) {
                    JOptionPane.showMessageDialog(frame, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    new LoginClient(); // if registered successful we navigate back to the login client
                } else {
                    // Could be "User already exists" or other error
                    messageLabel.setText(response);
                }

            } catch (IOException ex1) {
                messageLabel.setText("Connection error: " + ex1.getMessage());
            }
        });

        // login button stuff
        loginButton.addActionListener(e -> {
            frame.dispose();
            new LoginClient();
        });

        // Layout form fields - don't touch
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(userIdLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(emailLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(confirmPasswordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(registerButton, gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(loginButton, gbc);
        gbc.gridx = 1; gbc.gridy = 6; formPanel.add(messageLabel, gbc);

        frame.add(formPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegistrationClient::new);
    }
}

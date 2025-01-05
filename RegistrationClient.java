import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class RegistrationClient {
    private String serverIp = "127.0.0.1"; // Default IP
    private int serverPort = 12345;       // Default port

    public static void main(String[] args) {
        
        String ip = "127.0.0.1";
        int port = 12345;

        if (args.length >= 2) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        } else if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        // Make final copies for use in the lambda - fixes compile error
        final String finalIp = ip;
        final int finalPort = port;
        SwingUtilities.invokeLater(() -> new RegistrationClient(finalIp, finalPort));
    }

    public RegistrationClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        createRegistrationPage();
    }

    private void createRegistrationPage() {
        JFrame frame = new JFrame("Register");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
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

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPasswordField = new JPasswordField(20);

        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);

        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");

        registerButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

            if (userId.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setText("All fields are required.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match.");
                return;
            }

            // Build the REGISTER command
            String command = "REGISTER," + userId + "," + email + "," + password;

            // One Command per Connection
            try (Socket socket = new Socket(serverIp, serverPort);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                writer.println(command);
                String response = reader.readLine();

                if (response == null) {
                    messageLabel.setText("No response from server.");
                    return;
                }

                if ("REGISTER_SUCCESS".equalsIgnoreCase(response)) {
                    JOptionPane.showMessageDialog(frame, "Registration successful!", 
                                                  "Success", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    // Go to login page with the same IP/port
                    SwingUtilities.invokeLater(() -> new LoginClient(serverIp, serverPort));
                } else {
                    messageLabel.setText(response);
                }

            } catch (IOException ex1) {
                messageLabel.setText("Connection error: " + ex1.getMessage());
            }
        });

        loginButton.addActionListener(e -> {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new LoginClient(serverIp, serverPort));
        });

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
}

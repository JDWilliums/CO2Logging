import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class LoginClient {
    private static final String SERVER_IP = "127.0.0.1"; // local host ip
    private static final int SERVER_PORT = 12345;        // change this

    public LoginClient() {
        createLoginPage();
    }

    private void createLoginPage() {
        JFrame frame = new JFrame("Login");
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

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // login button - sends command to server with login information to check for verification
        loginButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (userId.isEmpty() || password.isEmpty()) {
                messageLabel.setText("User ID and Password cannot be empty.");
                return;
            }

            // Build LOGIN command
            String command = "LOGIN," + userId + "," + password;

            // One command per connection:- maybe change to persistant client model so we can have infinite commands per connection? 
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

                if ("Login successful!".equalsIgnoreCase(response)) {
                    JOptionPane.showMessageDialog(frame, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    new MainPage(userId); // if login is successful we open main page sending through the userid the user logged in with
                } else {
                    messageLabel.setText(response);
                }

            } catch (IOException ex) {
                messageLabel.setText("Connection error: " + ex.getMessage());
            }
        });

        // register button
        registerButton.addActionListener(e -> {
            frame.dispose();
            new RegistrationClient(); 
        });

        // Layout fields
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(userIdLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(loginButton, gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(registerButton, gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(messageLabel, gbc);

        frame.add(formPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginClient::new);
    }
}

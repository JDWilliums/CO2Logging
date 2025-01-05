import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class LoginClient {
    private String serverIp = "127.0.0.1"; // Default IP
    private int serverPort = 12345;       // Default port

    public static void main(String[] args) {
        // If arguments are provided, parse them:
        // Usage examples:
        //   java LoginClient 127.0.0.1 54321  (IP and port)
        //   java LoginClient 54321          (just port, IP defaults to localhost)
        //   java LoginClient                (defaults to localhost:12345)
        String ip = "127.0.0.1";
        int port = 12345;

        if (args.length >= 2) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        } else if (args.length == 1) {
            // If only one argument, assume it's the port
            port = Integer.parseInt(args[0]);
        }

        // Make final copies for use in the lambda - fixes error that requires variables accessed from an enclosing scope be final or effectively final.
        final String finalIp = ip;
        final int finalPort = port;

        // Create the LoginClient with the parsed IP and port
        SwingUtilities.invokeLater(() -> new LoginClient(finalIp, finalPort));
    }

    public LoginClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        createLoginPage();
    }

    private void createLoginPage() {
        JFrame frame = new JFrame("Login");
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

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userIdField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (userId.isEmpty() || password.isEmpty()) {
                    messageLabel.setText("User ID and Password cannot be empty.");
                    return;
                }

                // Build the LOGIN command
                String command = "LOGIN," + userId + "," + password;

                // One command per connection
                try (Socket socket = new Socket(serverIp, serverPort);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                    writer.println(command);
                    String response = reader.readLine();

                    if (response == null) {
                        messageLabel.setText("No response from server.");
                        return;
                    }

                    if ("Login successful!".equalsIgnoreCase(response)) {
                        JOptionPane.showMessageDialog(frame, "Login successful!", 
                                                      "Success", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                        new MainPage(userId);
                    } else {
                        messageLabel.setText(response);
                    }
                } catch (IOException ex) {
                    messageLabel.setText("Connection error: " + ex.getMessage());
                }
            }
        });

        registerButton.addActionListener(e -> {
            frame.dispose();
            // Pass the same IP/port to RegistrationClient so it matches the server
            SwingUtilities.invokeLater(() -> new RegistrationClient(serverIp, serverPort));
        });

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
}

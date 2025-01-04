import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class LoginClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginClient::new);
    }

    public LoginClient() {
        connectToServer();
        createLoginPage();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createLoginPage() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout(10, 10));

        // Logo Panel
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setLayout(new FlowLayout());
        ImageIcon originalIcon = new ImageIcon("logo.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoPanel.add(logoLabel);

        // Sets window icon
        frame.setIconImage(originalIcon.getImage());

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel userIdLabel = new JLabel("User ID:");
        userIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField userIdField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPasswordField passwordField = new JPasswordField(20);

        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(59, 89, 182));
        loginButton.setForeground(Color.WHITE);

        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(new Color(59, 89, 182));
        registerButton.setForeground(Color.WHITE);

        loginButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (userId.isEmpty() || password.isEmpty()) {
                messageLabel.setText("User ID and Password cannot be empty.");
                return;
            }

            writer.println(userId + "," + password);

            try {
                String response = reader.readLine();
                if (response == null) {
                    messageLabel.setText("Connection closed by server.");
                    socket.close();
                    return;
                }

                if (response.equalsIgnoreCase("Login successful!")) {
                    JOptionPane.showMessageDialog(frame, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    new MainPage(userId); // Go to mainpage storing user id
                } else if (response.equalsIgnoreCase("Goodbye!")) {
                    JOptionPane.showMessageDialog(frame, "Server closed the connection.", "Disconnected", JOptionPane.WARNING_MESSAGE);
                    socket.close();
                    System.exit(0);
                } else {
                    messageLabel.setText(response);
                }
            } catch (IOException ex) {
                messageLabel.setText("Connection error. Please try again.");
            }
        });

        registerButton.addActionListener(e -> {
            frame.dispose();
            new RegistrationClient(); // Open Registration page
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userIdLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(userIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(registerButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        formPanel.add(messageLabel, gbc);

        // Add Panels to Frame
        frame.add(logoPanel, BorderLayout.NORTH);
        frame.add(formPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}

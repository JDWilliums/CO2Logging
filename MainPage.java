import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainPage {
    private final String userId; // the user id of the logged in user
    private final JFrame frame; // GUI stuff - dont touch
    private final JTable table;
    private final DefaultTableModel tableModel;
    private static final String CSV_FILE = "co2_readings.csv";

    public MainPage(String userId) {
        this.userId = userId;

        // Create frame
        frame = new JFrame("Main Page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        // Set layout
        frame.setLayout(new BorderLayout(10, 10));
        // Add logo
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(Color.WHITE);
        ImageIcon originalIcon = new ImageIcon("logo.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoPanel.add(logoLabel);
        frame.add(logoPanel, BorderLayout.NORTH);
        // Sets window icon
        frame.setIconImage(originalIcon.getImage());
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(10, 10));

        // Add "Create Reading" form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2, 10, 10));
        JLabel postcodeLabel = new JLabel("Postcode:");
        JTextField postcodeField = new JTextField();
        JLabel co2Label = new JLabel("CO2 Concentration:");
        JTextField co2Field = new JTextField();
        JButton submitButton = new JButton("Submit Reading");

        formPanel.add(postcodeLabel);
        formPanel.add(postcodeField);
        formPanel.add(co2Label);
        formPanel.add(co2Field);
        formPanel.add(new JLabel()); // Spacer
        formPanel.add(submitButton);

        contentPanel.add(formPanel, BorderLayout.NORTH);

        // Add "View Readings" table
        tableModel = new DefaultTableModel(new String[]{"Timestamp", "User ID", "Postcode", "CO2 ppm"}, 0);
        table = new JTable(tableModel);

        JScrollPane tableScrollPane = new JScrollPane(table);
        JButton refreshButton = new JButton("Refresh Readings");

        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        tablePanel.add(refreshButton, BorderLayout.SOUTH);

        contentPanel.add(tablePanel, BorderLayout.CENTER);

        frame.add(contentPanel, BorderLayout.CENTER);

        // action listeners
        submitButton.addActionListener(e -> createReading(postcodeField.getText().trim(), co2Field.getText().trim()));
        refreshButton.addActionListener(e -> loadCSVData());

        // Load initial data
        loadCSVData();

        // Show frame
        frame.setVisible(true);
    }

    private synchronized void createReading(String postcode, String co2ppm) {
        if (postcode.isEmpty() || co2ppm.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Both fields are required!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double co2Value = Double.parseDouble(co2ppm);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            try (FileWriter writer = new FileWriter(CSV_FILE, true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                bufferedWriter.write(String.format("%s,%s,%s,%.2f\n", timestamp, userId, postcode, co2Value));
            }

            JOptionPane.showMessageDialog(frame, "Reading successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "CO2 Concentration must be a valid number!", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving data to CSV.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private synchronized void loadCSVData() {
        tableModel.setRowCount(0); // Clears the table
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                tableModel.addRow(parts);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading CSV data.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int MAX_CLIENTS = 4;
    private static final String CSV_FILE = "co2_readings.csv";
    private final ServerSocket serverSocket;
    private final ExecutorService executor;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executor = Executors.newFixedThreadPool(MAX_CLIENTS);
        initializeCSV();
    }

    public void start() {
        System.out.println("Server started, waiting for connections...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");
                executor.execute(new ClientHandler(clientSocket));
            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
            }
        }
    }

    private synchronized void writeToCSV(String timestamp, String userId, String postcode, double co2ppm) {
        try (FileWriter writer = new FileWriter(CSV_FILE, true)) {
            writer.append(timestamp).append(",").append(userId).append(",")
                    .append(postcode).append(",").append(String.valueOf(co2ppm)).append("\n");
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    private void initializeCSV() {
        try (FileWriter writer = new FileWriter(CSV_FILE, true)) {
            writer.append("Timestamp,UserID,Postcode,CO2ppm\n");
        } catch (IOException e) {
            System.err.println("Error initializing CSV: " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                 
                writer.println("Welcome! Please enter your UserID:");
                String userId = reader.readLine();
                
                writer.println("Enter postcode:");
                String postcode = reader.readLine();
                
                writer.println("Enter CO2 concentration in ppm:");
                double co2ppm = Double.parseDouble(reader.readLine());

                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                synchronized (Server.this) {
                    writeToCSV(timestamp, userId, postcode, co2ppm);
                }

                writer.println("Data saved. Goodbye!");
            } catch (IOException e) {
                System.err.println("Client connection error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        try {
            Server server = new Server(port);
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}

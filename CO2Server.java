import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CO2Server {
    private static final int MAX_CLIENTS = 8;
    private static final HashMap<String, String> users = new HashMap<>();
    private final ServerSocket serverSocket;
    private final ExecutorService executor;

    public CO2Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executor = Executors.newFixedThreadPool(MAX_CLIENTS);
        initializeUsers();
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

    private void initializeUsers() {
        users.put("user1", "password123");
        users.put("user2", "password456");
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

                boolean authenticated = false;

                while (!authenticated) {

                    String input = reader.readLine(); // Read input from client
                    if (input == null || input.trim().equalsIgnoreCase("exit")) {
                        writer.println("Goodbye!"); // Notify client before closing
                        break;
                    }

                    // Validate input format
                    String[] credentials = input.split(",");
                    if (credentials.length != 2) {
                        writer.println("Invalid input format. Please use the format: userID,password.");
                        continue;
                    }

                    String userId = credentials[0].trim();
                    String password = credentials[1].trim();

                    // Authenticate user
                    if (authenticateUser(userId, password)) {
                        writer.println("Login successful!");
                        authenticated = true;
                    } else {
                        writer.println("Invalid User ID or Password.");
                    }
                }
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

        private boolean authenticateUser(String userId, String password) {
            return users.containsKey(userId) && users.get(userId).equals(password);
        }
    }


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java CO2Server <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        try {
            CO2Server server = new CO2Server(port);
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}

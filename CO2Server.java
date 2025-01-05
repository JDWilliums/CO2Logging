import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CO2Server {
    private static final int MAX_CLIENTS = 8; // can increase
    private static final HashMap<String, String> users = new HashMap<>(); // user+pass hash map for login

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

    // These are default users - login with these if need be
    private void initializeUsers() {
        users.put("user1", "password123");
        users.put("user2", "password456");
    }

    // Handles each client in seperate thread 
    private class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // Read exactly one line (one command) from the client
                String line = reader.readLine();
                if (line != null) {
                    // Split the command
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        String command = parts[0].trim().toUpperCase();

                        switch (command) {
                            case "REGISTER":
                                // Format: REGISTER,userId,email,password
                                handleRegister(parts, writer);
                                break;
                            case "LOGIN":
                                // Format: LOGIN,userId,password
                                handleLogin(parts, writer);
                                break;
                            default:
                                writer.println("Unrecognised command."); // is called if something is broken = format is wrong
                                break;
                        }
                    } else {
                        writer.println("Invalid command format.");
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

        private void handleRegister(String[] parts, PrintWriter writer) {
            // Expected: REGISTER,userId,email,password
            if (parts.length != 4) {
                writer.println("Invalid registration format. Expected: REGISTER,userId,email,password");
                return;
            }
            String userId = parts[1].trim();
            // we dont store email but if we did: String email = parts[2].trim();
            String password = parts[3].trim();

            synchronized (users) {
                if (users.containsKey(userId)) {
                    writer.println("User already exists");
                } else {
                    users.put(userId, password);
                    writer.println("REGISTER_SUCCESS");
                }
            }
        }

        private void handleLogin(String[] parts, PrintWriter writer) {
            // Expected: LOGIN,userId,password
            if (parts.length != 3) {
                writer.println("Invalid login format. Expected: LOGIN,userId,password");
                return;
            }
            String userId = parts[1].trim();
            String password = parts[2].trim();

            synchronized (users) {
                if (users.containsKey(userId) && users.get(userId).equals(password)) {
                    writer.println("Login successful!");
                } else {
                    writer.println("Invalid User ID or Password.");
                }
            }
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

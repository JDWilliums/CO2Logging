import java.io.*;
import java.net.Socket;

public class Client {
    private final String serverAddress;
    private final int port;

    public Client(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public void start() {
        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println(reader.readLine());  // Welcome message

            System.out.print("UserID: ");
            String userId = consoleReader.readLine();
            writer.println(userId);

            System.out.print("Postcode: ");
            String postcode = consoleReader.readLine();
            writer.println(postcode);

            System.out.print("CO2 ppm: ");
            String co2ppm = consoleReader.readLine();
            writer.println(co2ppm);

            System.out.println(reader.readLine());  // Confirmation message

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Client <server_address> <port>");
            return;
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(serverAddress, port);
        client.start();
    }
}

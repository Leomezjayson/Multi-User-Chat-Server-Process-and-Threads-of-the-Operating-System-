import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * ChatClient
 * ----------
 * Connects to the multi-user ChatServer. Prompts for a username first,
 * then sends/receives messages concurrently, same pattern as the
 * 2-user chat client (a listener thread for incoming messages, the
 * main thread for sending).
 */
public class ChatClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             Scanner scanner = new Scanner(System.in)) {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            out.println(username);

            System.out.println("Connected as " + username
                    + ". Type a message and press Enter. Type 'exit' to quit.\n");

            // Listener thread: continuously prints whatever the server
            // broadcasts, independent of the main thread below.
            Thread listenerThread = new Thread(() -> {
                try {
                    String incoming;
                    while ((incoming = in.readLine()) != null) {
                        System.out.println("\n" + incoming);
                        System.out.print("You: ");
                    }
                } catch (IOException e) {
                    System.out.println("\nConnection to server lost.");
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();

            while (true) {
                System.out.print("You: ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Closing connection.");
                    break;
                }
                out.println(message);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}

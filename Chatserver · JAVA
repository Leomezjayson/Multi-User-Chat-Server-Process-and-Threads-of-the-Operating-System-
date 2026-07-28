import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ChatServer
 * ----------
 * Accepts an UNLIMITED number of clients (unlike the previous 2-user
 * project) and broadcasts every message to everyone else connected.
 *
 * OS concept demonstrated: Process and Threads.
 * Each connected client is handled by its own THREAD, not a separate
 * OS PROCESS. Threads share the same memory space (here, the shared
 * `clients` list), which is what makes broadcasting simple — every
 * thread can directly access the same list of connected handlers.
 * A process-based approach (like older Unix servers using fork())
 * would require explicit inter-process communication (pipes, shared
 * memory, sockets) just to let one client's handler notify another's,
 * since separate processes do NOT share memory by default.
 */
public class ChatServer {

    private static final int PORT = 6000;

    // Thread-safe list: multiple threads add/remove/iterate concurrently.
    // CopyOnWriteArrayList is safe for this because reads (broadcasting)
    // happen far more often than writes (clients joining/leaving).
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("========================================");
            System.out.println("  Multi-User Chat Server");
            System.out.println("  Listening on port " + PORT);
            System.out.println("========================================");

            // Runs forever, accepting any number of clients.
            // Each accepted connection gets its OWN thread immediately,
            // so the server can go straight back to accept() and wait
            // for the next client without being blocked by this one.
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    /**
     * Sends a message to every connected client except the sender.
     * Called from whichever client's thread received the message —
     * so this method runs concurrently from multiple threads and
     * must be safe to call that way (CopyOnWriteArrayList handles this).
     */
    private static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.send(message);
            }
        }
    }

    /**
     * Handles one client's entire lifecycle on its own thread:
     * username registration, reading messages, broadcasting them,
     * and cleanup on disconnect.
     */
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String username = "Unknown";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        void send(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {

                out = new PrintWriter(socket.getOutputStream(), true);

                // First line from the client is treated as their username.
                String firstLine = in.readLine();
                if (firstLine != null && !firstLine.isBlank()) {
                    username = firstLine.trim();
                }

                System.out.println(username + " joined from "
                        + socket.getRemoteSocketAddress());
                broadcast(username + " has joined the chat.", this);

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(username + ": " + line);
                    broadcast(username + ": " + line, this);
                }

            } catch (IOException e) {
                System.out.println(username + " disconnected unexpectedly.");
            } finally {
                clients.remove(this);
                broadcast(username + " has left the chat.", this);
                System.out.println(username + " left.");
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}

package connect6.network;

import java.io.*;
import java.net.Socket;

public class NetworkClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private MessageListener messageListener;
    private volatile boolean connected = false;

    public NetworkClient(MessageListener listener) {
        this.messageListener = listener;
    }

    public void connect(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;

            if (messageListener != null) {
                messageListener.onConnectionStatusChanged(true);
            }

            new Thread(this::readMessages).start();

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            if (messageListener != null) {
                messageListener.onConnectionStatusChanged(false);
            }
        }
    }

    private void readMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (messageListener != null) {
                    messageListener.onMessageReceived(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();

            if (messageListener != null) {
                messageListener.onConnectionStatusChanged(false);
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
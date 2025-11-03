package connect6.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String playerRole;
    public int clientId;
    private GameServer gameServer;

    public ClientHandler(Socket socket, int clientId, GameServer gameServer) {
        this.socket = socket;
        this.clientId = clientId;
        this.gameServer = gameServer;
    }

    public void setPlayerRole(String role) {
        this.playerRole = role;
    }

    public String getPlayerRole() {
        return playerRole;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(ServerConstants.CMD_WELCOME + " Player " + clientId);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith(ServerConstants.CMD_MOVE)) {
                    String[] parts = message.split(" ");
                    if (parts.length == 3) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        gameServer.processMove(this, x, y);
                    }
                } else if (message.equals(ServerConstants.CMD_EXIT)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client " + clientId + " disconnected");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            gameServer.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
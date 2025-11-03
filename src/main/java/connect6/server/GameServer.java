package connect6.server;

import connect6.game.Connect6Game;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private Connect6Game game;
    private boolean gameStarted = false;

    public static void main(String[] args) {
        new GameServer().startServer();
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(ServerConstants.PORT);
            System.out.println("Connect6 Server started on port " + ServerConstants.PORT);
            System.out.println("Waiting for 2 players...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size() + 1, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                if (clients.size() == 2 && !gameStarted) {
                    startGame();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame() {
        game = new Connect6Game();
        gameStarted = true;

        Collections.shuffle(clients);

        clients.get(0).setPlayerRole("BLACK");
        clients.get(1).setPlayerRole("WHITE");

        broadcast(ServerConstants.CMD_GAME_START);
        clients.get(0).sendMessage(ServerConstants.CMD_ROLE + " BLACK");
        clients.get(1).sendMessage(ServerConstants.CMD_ROLE + " WHITE");
        broadcast(ServerConstants.CMD_TURN + " BLACK");

        System.out.println("Game started! Black: " + clients.get(0).clientId + ", White: " + clients.get(1).clientId);
    }

    public void processMove(ClientHandler client, int x, int y) {
        char expectedPlayer = (client.getPlayerRole().equals("BLACK")) ? 'B' : 'W';
        if (game.getCurrentPlayer() != expectedPlayer) {
            client.sendMessage(ServerConstants.CMD_ERROR + " Not your turn");
            return;
        }

        if (game.makeMove(x, y)) {
            broadcastBoard();

            if (game.isGameOver()) {
                broadcast(ServerConstants.CMD_GAME_OVER + " " + game.getWinner());
                System.out.println("Game over! Winner: " + game.getWinner());
                gameStarted = false;
                clients.clear();
            } else {
                String currentPlayer = (game.getCurrentPlayer() == 'B') ? "BLACK" : "WHITE";
                broadcast(ServerConstants.CMD_TURN + " " + currentPlayer);
            }
        } else {
            client.sendMessage(ServerConstants.CMD_ERROR + " Invalid move");
        }
    }

    private void broadcastBoard() {
        StringBuilder boardState = new StringBuilder();
        char[][] board = game.getBoard();

        for (int i = 0; i < game.getBoardSize(); i++) {
            for (int j = 0; j < game.getBoardSize(); j++) {
                boardState.append(board[i][j]);
            }
        }

        broadcast(ServerConstants.CMD_BOARD + " " + boardState.toString());
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        if (clients.size() < 2 && gameStarted) {
            broadcast(ServerConstants.CMD_GAME_OVER + " DISCONNECT");
            gameStarted = false;
        }
    }
}
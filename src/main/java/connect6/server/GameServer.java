package connect6.server;

import connect6.game.Connect6Game;
import connect6.rmi.RemoteGameInterface;
import connect6.rmi.RemoteClientInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameServer implements RemoteGameInterface {
    private Connect6Game game;
    // Используем LinkedHashMap — порядок вставки сохраняется
    private Map<String, RemoteClientInterface> clients = new LinkedHashMap<>();
    private boolean gameStarted = false;
    private String currentPlayer;

    public static void main(String[] args) {
        try {
            System.setProperty("java.security.policy", "server.policy");

            GameServer server = new GameServer();
            RemoteGameInterface stub = (RemoteGameInterface) UnicastRemoteObject.exportObject(server, 0);

            Registry registry = LocateRegistry.createRegistry(ServerConstants.RMI_PORT);
            registry.rebind(ServerConstants.GAME_SERVER_NAME, stub);

            System.out.println("Connect6 RMI Server started on port " + ServerConstants.RMI_PORT);
            System.out.println("Server bound to: " + ServerConstants.GAME_SERVER_NAME);
            System.out.println("Waiting for players...");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void registerClient(RemoteClientInterface client, String playerName) throws RemoteException {
        if (clients.size() >= 2) {
            client.showError("Server is full. Maximum 2 players allowed.");
            return;
        }

        clients.put(playerName, client);
        System.out.println("Player connected: " + playerName);

        if (clients.size() == 2 && !gameStarted) {
            startGame();
        } else {
            client.showError("Waiting for another player...");
        }
    }

    private void startGame() throws RemoteException {
        game = new Connect6Game();
        gameStarted = true;

        String[] playerNames = clients.keySet().toArray(new String[0]);
        String blackPlayer = playerNames[0];
        String whitePlayer = playerNames[1];

        clients.get(blackPlayer).setPlayerRole("BLACK");
        clients.get(whitePlayer).setPlayerRole("WHITE");

        currentPlayer = blackPlayer;

        for (RemoteClientInterface client : clients.values()) {
            client.gameStarted();
        }

        // оповестить текущего игрока о ходе
        clients.get(currentPlayer).setCurrentTurn(currentPlayer);
        broadcastBoard();

        System.out.println("Game started! Black: " + blackPlayer + ", White: " + whitePlayer);
    }

    @Override
    public synchronized void makeMove(String playerName, int x, int y) throws RemoteException {
        if (!gameStarted) {
            clients.get(playerName).showError("Game not started yet");
            return;
        }

        if (!playerName.equals(currentPlayer)) {
            clients.get(playerName).showError("Not your turn");
            return;
        }

        // expected player char
        String[] players = clients.keySet().toArray(new String[0]);
        char expectedPlayer = playerName.equals(players[0]) ? 'B' : 'W';
        if (game.getCurrentPlayer() != expectedPlayer) {
            clients.get(playerName).showError("Not your turn");
            return;
        }

        if (game.makeMove(x, y)) {
            broadcastBoard();

            if (game.isGameOver()) {
                String winner = game.getWinner();
                for (RemoteClientInterface client : clients.values()) {
                    client.gameOver(winner);
                }
                System.out.println("Game over! Winner: " + winner);
                resetGame();
            } else {
                // смена текущего игрока: у нас clients - LinkedHashMap, players в порядке регистрации
                currentPlayer = currentPlayer.equals(players[0]) ? players[1] : players[0];
                clients.get(currentPlayer).setCurrentTurn(currentPlayer);
            }
        } else {
            clients.get(playerName).showError("Invalid move");
        }
    }

    @Override
    public synchronized void disconnect(String playerName) throws RemoteException {
        clients.remove(playerName);
        System.out.println("Player disconnected: " + playerName);

        if (gameStarted && clients.size() < 2) {
            for (RemoteClientInterface client : clients.values()) {
                client.gameOver("DISCONNECT");
            }
            resetGame();
        }
    }

    private void broadcastBoard() throws RemoteException {
        char[][] board = game.getBoard();
        for (RemoteClientInterface client : clients.values()) {
            client.updateBoard(board);
        }
    }

    private void resetGame() {
        gameStarted = false;
        clients.clear();
        currentPlayer = null;
        System.out.println("Game reset. Waiting for new players...");
    }
}

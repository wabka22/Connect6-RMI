package connect6.server;

import connect6.game.Connect6Game;
import connect6.rmi.RemoteClientInterface;
import connect6.rmi.RemoteGameInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameServer implements RemoteGameInterface {
    private Connect6Game game;
    private final Map<String, RemoteClientInterface> clients = new LinkedHashMap<>();
    private boolean gameStarted = false;
    private String currentPlayer;
    private final Map<String, Boolean> rematchRequests = new LinkedHashMap<>();

    public static void main(String[] args) {
        try {
            System.setProperty("java.security.policy", "server.policy");
            GameServer server = new GameServer();
            RemoteGameInterface stub = (RemoteGameInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(ServerConstants.RMI_PORT);
            registry.rebind(ServerConstants.GAME_SERVER_NAME, stub);
            System.out.println("✅ Connect6 RMI Server started on port " + ServerConstants.RMI_PORT);
        } catch (Exception e) {
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

        if (clients.size() == 2) {
            // Очистить сообщение ожидания для обоих
            for (RemoteClientInterface c : clients.values()) {
                c.showError("");
            }
        }
    }

    private void startGame() throws RemoteException {
        game = new Connect6Game();
        gameStarted = true;
        rematchRequests.clear();

        String[] playerNames = clients.keySet().toArray(new String[0]);
        currentPlayer = playerNames[0];

        clients.get(playerNames[0]).setPlayerRole("BLACK");
        clients.get(playerNames[1]).setPlayerRole("WHITE");

        for (RemoteClientInterface client : clients.values()) {
            client.gameStarted();
        }

        clients.get(currentPlayer).setCurrentTurn(currentPlayer);
        broadcastBoard();
    }

    @Override
    public synchronized void makeMove(String playerName, int x, int y) throws RemoteException {
        if (!gameStarted || game == null) return;

        RemoteClientInterface client = clients.get(playerName);
        if (client == null) return;

        if (!playerName.equals(currentPlayer)) {
            client.showError("Not your turn");
            return;
        }

        boolean moveSuccess = game.placeStone(x, y);
        if (!moveSuccess) {
            client.showError("Invalid move");
            return;
        }

        broadcastBoard();

        if (game.isGameOver()) {
            String winner = game.getWinner();
            for (RemoteClientInterface c : clients.values()) {
                c.gameOver(winner);
            }
            gameStarted = false;
            return;
        }

        if (game.shouldSwitchPlayer()) {
            String[] players = clients.keySet().toArray(new String[0]);
            currentPlayer = currentPlayer.equals(players[0]) ? players[1] : players[0];
            game.switchPlayer();
        }

        for (Map.Entry<String, RemoteClientInterface> entry : clients.entrySet()) {
            entry.getValue().setCurrentTurn(currentPlayer);
        }
    }

    @Override
    public synchronized void disconnect(String playerName) throws RemoteException {
        clients.remove(playerName);
        rematchRequests.remove(playerName);

        if (gameStarted && clients.size() < 2) {
            for (RemoteClientInterface client : clients.values()) {
                client.gameOver("DISCONNECT");
            }
            gameStarted = false;
            currentPlayer = null;
        }
    }

    private void broadcastBoard() throws RemoteException {
        if (game == null) return;
        char[][] board = game.getBoard();
        for (RemoteClientInterface client : clients.values()) {
            client.updateBoard(board);
        }
    }

    @Override
    public synchronized void requestRematch(String playerName) throws RemoteException {
        if (!clients.containsKey(playerName)) return;
        rematchRequests.put(playerName, true);

        if (rematchRequests.size() == 2 && rematchRequests.values().stream().allMatch(b -> b)) {
            System.out.println("Starting rematch...");
            startGame();
        }
    }
}

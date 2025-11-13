package connect6.server;

import connect6.game.Connect6Game;
import connect6.game.PlaceResult;
import connect6.game.PlayerType;
import connect6.rmi.RemoteClientInterface;
import connect6.rmi.RemoteGameInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameServer implements RemoteGameInterface {

    private static final Logger LOG = Logger.getLogger(GameServer.class.getName());

    private Connect6Game game;
    private final Map<String, RemoteClientInterface> clients = new LinkedHashMap<>();
    private boolean gameStarted = false;
    private String currentPlayer;
    private final Map<String, Boolean> rematchRequests = new LinkedHashMap<>();
    private static Registry registry;

    public static void main(String[] args) {
        System.setProperty("java.security.policy", "server.policy");
        try {
            GameServer server = new GameServer();
            RemoteGameInterface stub = (RemoteGameInterface) UnicastRemoteObject.exportObject(server, 0);
            registry = LocateRegistry.createRegistry(ServerConfig.INSTANCE.RMI_PORT);
            registry.rebind(ServerConfig.INSTANCE.GAME_SERVER_NAME, stub);
            LOG.info(" Server started on port " + ServerConfig.INSTANCE.RMI_PORT);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Server error", e);
        }
    }

    @Override
    public synchronized void registerClient(RemoteClientInterface client, String playerName)
            throws RemoteException {
        if (clients.containsKey(playerName)) {
            client.showError("Name already in use. Choose another.");
            return;
        }

        if (clients.size() >= ServerConfig.INSTANCE.MAX_PLAYERS) {
            client.showError(ServerConfig.INSTANCE.MSG_SERVER_FULL);
            return;
        }

        clients.put(playerName, client);
        LOG.info("Player connected: " + playerName);

        if (clients.size() == 2) {
            for (RemoteClientInterface c : clients.values()) {
                try {
                    c.showError("");
                } catch (RemoteException ignored) {}
            }
            if (!gameStarted) startGame();
        } else {
            client.showError(ServerConfig.INSTANCE.MSG_WAITING_PLAYER);
        }
    }

    private void startGame() throws RemoteException {
        game = new Connect6Game();
        gameStarted = true;
        rematchRequests.clear();

        String[] players = clients.keySet().toArray(new String[0]);
        if (players.length < 2) return;

        currentPlayer = players[0];

        clients.get(players[0]).setPlayerRole(PlayerType.BLACK.name());
        clients.get(players[1]).setPlayerRole(PlayerType.WHITE.name());

        for (RemoteClientInterface c : clients.values()) {
            try {
                c.gameStarted();
            } catch (RemoteException e) {
                LOG.log(Level.WARNING, "Failed to start game", e);
            }
        }

        clients.get(currentPlayer).setCurrentTurn(currentPlayer);
        broadcastBoard();

        LOG.info("New game started between " + players[0] + " and " + players[1]);
    }

    @Override
    public synchronized void makeMove(String playerName, int x, int y) throws RemoteException {
        if (!gameStarted || !playerName.equals(currentPlayer)) return;

        RemoteClientInterface client = clients.get(playerName);
        PlaceResult result = game.placeStone(x, y);

        if (result != PlaceResult.OK) {
            client.showError("Invalid move: " + result);
            return;
        }

        broadcastBoard();

        if (game.isGameOver()) {
            for (RemoteClientInterface c : clients.values()) {
                try {
                    c.gameOver(game.getWinner());
                } catch (RemoteException e) {
                    LOG.log(Level.WARNING, "Failed to notify client", e);
                }
            }
            gameStarted = false;
            return;
        }

        if (game.shouldSwitchPlayer()) {
            switchCurrentPlayer();
            game.switchPlayer();
        }

        for (RemoteClientInterface c : clients.values()) {
            try {
                c.setCurrentTurn(currentPlayer);
            } catch (RemoteException e) {
                LOG.log(Level.WARNING, "Failed to update turn", e);
            }
        }
    }

    @Override
    public synchronized void disconnect(String playerName) throws RemoteException {
        if (!clients.containsKey(playerName)) return;

        clients.remove(playerName);
        rematchRequests.remove(playerName);

        LOG.info("Player disconnected: " + playerName);

        if (gameStarted && clients.size() == 1) {
            String remaining = clients.keySet().iterator().next();
            LOG.info("Player " + playerName + " disconnected. " + remaining + " wins by default.");

            try {
                RemoteClientInterface winner = clients.get(remaining);
                winner.showError(ServerConfig.INSTANCE.MSG_PLAYER_DISCONNECTED);
                winner.gameOver(remaining);
            } catch (RemoteException e) {
                LOG.log(Level.WARNING, "Failed to notify remaining player", e);
            }

            gameStarted = false;
            currentPlayer = null;
        }

        if (clients.isEmpty()) {
            LOG.info("No active players. Waiting for new connections...");
        }
    }

    @Override
    public synchronized void requestRematch(String playerName) throws RemoteException {
        if (!clients.containsKey(playerName)) return;

        rematchRequests.put(playerName, true);
        if (rematchRequests.size() == 2 && rematchRequests.values().stream().allMatch(b -> b)) {
            LOG.info("Starting rematch...");
            startGame();
        }
    }

    private void broadcastBoard() {
        char[][] boardCopy = game.getBoard();
        for (Map.Entry<String, RemoteClientInterface> entry : clients.entrySet()) {
            try {
                entry.getValue().updateBoard(boardCopy);
            } catch (RemoteException e) {
                LOG.log(Level.WARNING, "Client unreachable: " + entry.getKey(), e);
            }
        }
    }

    private void switchCurrentPlayer() {
        String[] players = clients.keySet().toArray(new String[0]);
        currentPlayer = currentPlayer.equals(players[0]) ? players[1] : players[0];
    }
}

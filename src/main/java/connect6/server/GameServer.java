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

  public static void main(String[] args) {
    try {
      System.setProperty("java.security.policy", "server.policy");
      GameServer server = new GameServer();
      RemoteGameInterface stub = (RemoteGameInterface) UnicastRemoteObject.exportObject(server, 0);
      Registry registry = LocateRegistry.createRegistry(ServerConstants.RMI_PORT);
      registry.rebind(ServerConstants.GAME_SERVER_NAME, stub);
      LOG.info("Connect6 RMI Server started on port " + ServerConstants.RMI_PORT);

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      registry.unbind(ServerConstants.GAME_SERVER_NAME);
                      LOG.info("Server shut down.");
                    } catch (Exception ignored) {
                    }
                  }));
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Server error", e);
    }
  }

  @Override
  public synchronized void registerClient(RemoteClientInterface client, String playerName)
      throws RemoteException {
    if (clients.size() >= 2) {
      client.showError("Server is full. Maximum 2 players allowed.");
      return;
    }

    clients.put(playerName, client);
    LOG.info("Player connected: " + playerName);

    if (clients.size() == 2 && !gameStarted) {
      startGame();
    } else {
      client.showError("Waiting for another player...");
    }

    if (clients.size() == 2) {
      for (RemoteClientInterface c : clients.values()) {
        c.showError("");
      }
    }
  }

  private void startGame() throws RemoteException {
    game = new Connect6Game();
    gameStarted = true;
    rematchRequests.clear();

    String[] players = clients.keySet().toArray(new String[0]);
    currentPlayer = players[0];

    clients.get(players[0]).setPlayerRole(PlayerType.BLACK.name());
    clients.get(players[1]).setPlayerRole(PlayerType.WHITE.name());

    for (RemoteClientInterface c : clients.values()) {
      c.gameStarted();
    }

    clients.get(currentPlayer).setCurrentTurn(currentPlayer);
    broadcastBoard();
  }

  @Override
  public synchronized void makeMove(String playerName, int x, int y) throws RemoteException {
    if (!gameStarted || game == null) {
      return;
    }

    RemoteClientInterface client = clients.get(playerName);
    if (client == null) {
      return;
    }

    if (!playerName.equals(currentPlayer)) {
      client.showError("Not your turn");
      return;
    }

    PlaceResult result = game.placeStone(x, y);
    if (result != PlaceResult.OK) {
      client.showError("Invalid move: " + result);
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
      try {
        entry.getValue().setCurrentTurn(currentPlayer);
      } catch (RemoteException e) {
        LOG.log(Level.WARNING, "Failed to update turn for client " + entry.getKey(), e);
      }
    }
  }

  @Override
  public synchronized void disconnect(String playerName) throws RemoteException {
    clients.remove(playerName);
    rematchRequests.remove(playerName);

    if (gameStarted && clients.size() < 2) {
      for (RemoteClientInterface c : clients.values()) {
        c.gameOver("DISCONNECT");
      }
      gameStarted = false;
      currentPlayer = null;
    }

    LOG.info("Player disconnected: " + playerName);
  }

  private void broadcastBoard() {
    if (game == null) {
      return;
    }

    char[][] boardCopy = game.getBoard();
    clients
        .entrySet()
        .removeIf(
            entry -> {
              try {
                entry.getValue().updateBoard(boardCopy);
                return false;
              } catch (RemoteException e) {
                LOG.log(Level.WARNING, "Client unreachable: " + entry.getKey());
                return true;
              }
            });
  }

  @Override
  public synchronized void requestRematch(String playerName) throws RemoteException {
    if (!clients.containsKey(playerName)) {
      return;
    }

    rematchRequests.put(playerName, true);

    if (rematchRequests.size() == 2 && rematchRequests.values().stream().allMatch(b -> b)) {
      LOG.info("Starting rematch...");
      startGame();
    }
  }
}

package connect6.client;

import connect6.client.ui.GameBoardPanel;
import connect6.game.PlayerType;
import connect6.rmi.RemoteClientInterface;
import connect6.rmi.RemoteGameInterface;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.*;

public class GameClient extends JFrame implements RemoteClientInterface {

  private RemoteGameInterface gameServer;
  private String playerName;
  private PlayerType playerRole;
  private boolean myTurn = false;
  private boolean gameActive = false;

  private JLabel statusLabel;
  private JLabel turnLabel;
  private JLabel roleLabel;
  private JLabel scoreLabel;
  private GameBoardPanel boardPanel;

  private int playerWins = 0;
  private int opponentWins = 0;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new GameClient().setVisible(true));
  }

  public GameClient() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (Exception ignored) {
    }

    initializeGUI();
  }

  private void initializeGUI() {
    setTitle("Connect6 - RMI Client");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLayout(new BorderLayout(8, 8));

    // Верхняя панель
    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JTextField nameField = new JTextField("Player" + (System.currentTimeMillis() % 1000), 15);
    JButton connectBtn = new JButton("Connect");
    JButton disconnectBtn = new JButton("Disconnect");
    disconnectBtn.setEnabled(false);

    top.add(new JLabel("Player Name:"));
    top.add(nameField);
    top.add(connectBtn);
    top.add(disconnectBtn);
    add(top, BorderLayout.NORTH);

    // Правая панель
    JPanel right = new JPanel();
    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
    right.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    statusLabel = new JLabel("Not connected");
    roleLabel = new JLabel("Role: -");
    turnLabel = new JLabel("Turn: -");
    scoreLabel = new JLabel(getScoreText());

    right.add(statusLabel);
    right.add(Box.createVerticalStrut(8));
    right.add(roleLabel);
    right.add(Box.createVerticalStrut(8));
    right.add(turnLabel);
    right.add(Box.createVerticalStrut(8));
    right.add(scoreLabel);

    add(right, BorderLayout.EAST);

    // Панель доски
    boardPanel = new GameBoardPanel(19);
    boardPanel.setClickListener(this::handleBoardClick);
    add(boardPanel, BorderLayout.CENTER);

    // Кнопки
    connectBtn.addActionListener(
        e -> {
          playerName = nameField.getText().trim();
          if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter player name");
            return;
          }
          connectToServer(playerName);
          if (gameServer != null) {
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
          }
        });

    disconnectBtn.addActionListener(e -> dispose());

    setSize(900, 850);
    setLocationRelativeTo(null);
  }

  private void connectToServer(String name) {
    try {
      System.setProperty("java.security.policy", "client.policy");

      Registry registry =
          LocateRegistry.getRegistry(ClientConfig.CFG.SERVER_HOST, ClientConfig.CFG.RMI_PORT);
      gameServer = (RemoteGameInterface) registry.lookup(ClientConfig.CFG.GAME_SERVER_NAME);

      RemoteClientInterface stub =
          (RemoteClientInterface) UnicastRemoteObject.exportObject(this, 0);
      gameServer.registerClient(stub, name);

      statusLabel.setText("Connected as: " + name);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Connection failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void handleBoardClick(int x, int y) {
    if (!gameActive) {
      JOptionPane.showMessageDialog(this, "Game not started yet");
      return;
    }
    if (!myTurn) {
      JOptionPane.showMessageDialog(this, "Not your turn");
      return;
    }
    try {
      gameServer.makeMove(playerName, x, y);
    } catch (RemoteException e) {
      JOptionPane.showMessageDialog(this, "Move failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void updateBoard(char[][] board) {
    SwingUtilities.invokeLater(() -> boardPanel.setBoard(board));
  }

  @Override
  public void setPlayerRole(String role) {
    SwingUtilities.invokeLater(
        () -> {
          playerRole = PlayerType.valueOf(role);
          roleLabel.setText("Role: " + playerRole);
          statusLabel.setText("Connected as: " + playerName + " (" + playerRole + ")");
        });
  }

  @Override
  public void setCurrentTurn(String player) {
    SwingUtilities.invokeLater(
        () -> {
          myTurn = player.equals(playerName);
          turnLabel.setText(myTurn ? "Your turn (" + playerRole + ")" : "Opponent's turn");
        });
  }

  @Override
  public void gameStarted() {
    SwingUtilities.invokeLater(
        () -> {
          gameActive = true;
          statusLabel.setText("Game started!");
        });
  }

  @Override
  public void gameOver(String winner) {
    SwingUtilities.invokeLater(
        () -> {
          gameActive = false;
          myTurn = false;

          if (winner.equals(playerRole.name()) || winner.equals("OPPONENT_DISCONNECTED"))
            playerWins++;
          else opponentWins++;

          scoreLabel.setText(getScoreText());

          int option =
              JOptionPane.showConfirmDialog(
                  this,
                  "Game Over! Winner: " + winner + "\nDo you want to play again?",
                  "Game Over",
                  JOptionPane.YES_NO_OPTION);

          if (option == JOptionPane.YES_OPTION) {
            try {
              gameServer.requestRematch(playerName);
            } catch (RemoteException e) {
              showError("Rematch request failed: " + e.getMessage());
            }
          }
        });
  }

  private String getScoreText() {
    return "<html>Score<br>Your wins: "
        + playerWins
        + "<br>Opponent wins: "
        + opponentWins
        + "</html>";
  }

  @Override
  public void showError(String message) {
    SwingUtilities.invokeLater(
        () -> {
          if (message.isEmpty())
            statusLabel.setText("Connected as: " + playerName + " (" + playerRole + ")");
          else statusLabel.setText(message);
        });
  }

  @Override
  public void dispose() {
    try {
      if (gameServer != null && playerName != null) gameServer.disconnect(playerName);
    } catch (Exception ignored) {
    }

    super.dispose();
    System.exit(0);
  }
}

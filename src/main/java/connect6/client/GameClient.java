package connect6.client;

import connect6.rmi.RemoteClientInterface;
import connect6.rmi.RemoteGameInterface;
import connect6.client.ui.BoardRenderer;
import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameClient extends JFrame implements RemoteClientInterface {
    private RemoteGameInterface gameServer;
    private String playerName;
    private String playerRole;
    private boolean myTurn = false;
    private boolean gameActive = false;

    private JLabel statusLabel;
    private JLabel turnInfoLabel;
    private JLabel roleLabel;
    private JLabel scoreLabel;
    private GameBoardPanel boardPanel;
    private JButton rematchButton;

    private int playerWins = 0;
    private int opponentWins = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameClient().setVisible(true));
    }

    public GameClient() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Connect6 - RMI Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        // Верхняя панель подключения
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JTextField nameField = new JTextField("Player" + (System.currentTimeMillis() % 1000), 15);
        JButton connectButton = new JButton("Connect");
        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        top.add(new JLabel("Player Name:"));
        top.add(nameField);
        top.add(connectButton);
        top.add(disconnectButton);
        add(top, BorderLayout.NORTH);

        // Правая панель статуса
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        statusLabel = new JLabel("Not connected");
        roleLabel = new JLabel("Role: -");
        turnInfoLabel = new JLabel("Turn: -");
        scoreLabel = new JLabel("<html>Score<br>Your wins: 0<br>Opponent wins: 0</html>");

        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        turnInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        right.add(statusLabel);
        right.add(Box.createVerticalStrut(8));
        right.add(roleLabel);
        right.add(Box.createVerticalStrut(8));
        right.add(turnInfoLabel);
        right.add(Box.createVerticalStrut(8));
        right.add(scoreLabel);

        rematchButton = new JButton("Try Again");
        rematchButton.setEnabled(false);
        rematchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        rematchButton.addActionListener(e -> {
            if (gameServer != null && playerName != null) {
                try {
                    gameServer.requestRematch(playerName);
                    rematchButton.setEnabled(false);
                } catch (RemoteException ex) {
                    showError("Rematch request failed: " + ex.getMessage());
                }
            }
        });
        right.add(Box.createVerticalStrut(8));
        right.add(rematchButton);

        add(right, BorderLayout.EAST);

        // Центральная панель — доска
        boardPanel = new GameBoardPanel(19);
        boardPanel.setClickListener(this::handleBoardClick);
        add(boardPanel, BorderLayout.CENTER);

        connectButton.addActionListener(e -> {
            playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter player name");
                return;
            }
            connectToServer(playerName);
            if (gameServer != null) {
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
            }
        });

        disconnectButton.addActionListener(e -> dispose());

        setSize(900, 850);
        setLocationRelativeTo(null);
    }

    private void connectToServer(String name) {
        try {
            System.setProperty("java.security.policy", "client.policy");
            Registry registry = LocateRegistry.getRegistry(ClientConstants.SERVER_HOST, ClientConstants.RMI_PORT);
            gameServer = (RemoteGameInterface) registry.lookup(ClientConstants.GAME_SERVER_NAME);

            RemoteClientInterface stub = (RemoteClientInterface) UnicastRemoteObject.exportObject(this, 0);
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

    // --- RMI CALLBACKS ---
    @Override
    public void updateBoard(char[][] board) {
        SwingUtilities.invokeLater(() -> boardPanel.setBoard(board));
    }

    @Override
    public void setPlayerRole(String role) {
        SwingUtilities.invokeLater(() -> {
            playerRole = role;
            roleLabel.setText("Role: " + role);
            statusLabel.setText("Connected as: " + playerName + " (" + role + ")");
        });
    }

    @Override
    public void setCurrentTurn(String player) {
        SwingUtilities.invokeLater(() -> {
            myTurn = player.equals(playerName);
            turnInfoLabel.setText(myTurn ? "Your turn (" + playerRole + ")" : "Opponent's turn");
        });
    }

    @Override
    public void gameStarted() {
        SwingUtilities.invokeLater(() -> {
            gameActive = true;
            rematchButton.setEnabled(false);
            statusLabel.setText("Game started!");
        });
    }

    @Override
    public void gameOver(String winner) {
        SwingUtilities.invokeLater(() -> {
            gameActive = false;
            myTurn = false;

            if (winner.equals(playerRole)) playerWins++;
            else if (!winner.equals("DISCONNECT")) opponentWins++;

            updateScore();

            JOptionPane.showMessageDialog(this, "Game Over! Winner: " + winner);
            rematchButton.setEnabled(true);
        });
    }

    private void updateScore() {
        scoreLabel.setText("<html>Score<br>Your wins: " + playerWins + "<br>Opponent wins: " + opponentWins + "</html>");
    }

    @Override
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.isEmpty()) statusLabel.setText("Connected as: " + playerName + " (" + playerRole + ")");
            else JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    @Override
    public void dispose() {
        try {
            if (gameServer != null && playerName != null) {
                gameServer.disconnect(playerName);
            }
        } catch (Exception ignored) {}
        super.dispose();
        System.exit(0);
    }
}

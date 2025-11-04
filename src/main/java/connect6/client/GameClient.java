package connect6.client;

import connect6.rmi.RemoteGameInterface;
import connect6.rmi.RemoteClientInterface;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameClient extends JFrame implements RemoteClientInterface {
    private RemoteGameInterface gameServer;
    private String playerName;

    private JLabel statusLabel;
    private JLabel turnInfoLabel;
    private GameBoardPanel boardPanel;

    private String playerRole;
    private boolean myTurn = false;
    private boolean gameActive = false;

    public GameClient() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Connect6 Game - RMI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель ввода имени
        JPanel namePanel = new JPanel(new FlowLayout());
        namePanel.add(new JLabel("Player Name:"));
        JTextField nameField = new JTextField(15);
        nameField.setText("Player" + (System.currentTimeMillis() % 1000));
        namePanel.add(nameField);

        JButton connectButton = new JButton("Connect");
        namePanel.add(connectButton);
        add(namePanel, BorderLayout.NORTH);

        statusLabel = new JLabel("Enter name and connect to server");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.CENTER);

        turnInfoLabel = new JLabel("Waiting for connection...");
        turnInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(turnInfoLabel, BorderLayout.SOUTH);

        boardPanel = new GameBoardPanel(19);
        boardPanel.setClickListener(this::handleBoardClick);
        add(new JScrollPane(boardPanel), BorderLayout.CENTER);

        connectButton.addActionListener(e -> {
            playerName = nameField.getText().trim();
            if (!playerName.isEmpty()) {
                connectToServer(playerName);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter player name");
            }
        });

        pack();
        setLocationRelativeTo(null);
        setSize(700, 700);
    }

    private void connectToServer(String playerName) {
        try {
            // Установка политики безопасности для RMI
            System.setProperty("java.security.policy", "client.policy");

            Registry registry = LocateRegistry.getRegistry(
                    ClientConstants.SERVER_HOST,
                    ClientConstants.RMI_PORT
            );

            gameServer = (RemoteGameInterface) registry.lookup(ClientConstants.GAME_SERVER_NAME);

            // Экспортируем клиентский объект для callback'ов
            RemoteClientInterface clientStub = (RemoteClientInterface)
                    UnicastRemoteObject.exportObject(this, 0);

            gameServer.registerClient(clientStub, playerName);

            statusLabel.setText("Connected to server as: " + playerName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Connection failed: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void handleBoardClick(int x, int y) {
        if (gameActive && myTurn && gameServer != null) {
            try {
                gameServer.makeMove(playerName, x, y);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Move failed: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // Реализация RemoteClientInterface methods
    @Override
    public void updateBoard(char[][] board) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            boardPanel.setBoard(board);
        });
    }

    @Override
    public void setPlayerRole(String role) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            playerRole = role;
            statusLabel.setText("You are: " + role + " - " + playerName);
        });
    }

    @Override
    public void setCurrentTurn(String player) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            myTurn = player.equals(playerName);
            turnInfoLabel.setText(myTurn ?
                    "Your turn! Make a move (" + playerRole + ")" :
                    "Opponent's turn - waiting..."
            );
        });
    }

    @Override
    public void gameStarted() throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            gameActive = true;
            turnInfoLabel.setText("Game started! Waiting for turn...");
        });
    }

    @Override
    public void gameOver(String winner) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            gameActive = false;
            myTurn = false;

            if (winner.equals("DISCONNECT")) {
                JOptionPane.showMessageDialog(this,
                        "Opponent disconnected!",
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE
                );
                turnInfoLabel.setText("Game Over - Opponent disconnected");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Game Over! Winner: " + winner,
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE
                );
                turnInfoLabel.setText("Game Over - Winner: " + winner);
            }
        });
    }

    @Override
    public void showError(String message) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }

    @Override
    public void dispose() {
        if (gameServer != null && playerName != null) {
            try {
                gameServer.disconnect(playerName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameClient().setVisible(true);
        });
    }
}
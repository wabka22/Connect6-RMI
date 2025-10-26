package com.connect6.server;

import com.connect6.common.GameService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameServer implements GameService {
    private final int[][] board = new int[19][19];
    private int currentPlayer = 1;

    @Override
    public synchronized boolean makeMove(int x, int y, int player) {
        if (board[x][y] == 0 && player == currentPlayer) {
            board[x][y] = player;
            currentPlayer = 3 - player;
            return true;
        }
        return false;
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    @Override
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            GameService stub = (GameService) UnicastRemoteObject.exportObject(server, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("GameService", stub);

            System.out.println("✅ GameServer запущен и ждёт подключения клиентов...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

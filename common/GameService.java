package com.connect6.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameService extends Remote {
    boolean makeMove(int x, int y, int player) throws RemoteException;
    int[][] getBoard() throws RemoteException;
    int getCurrentPlayer() throws RemoteException;
}

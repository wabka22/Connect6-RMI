package connect6.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteGameInterface extends Remote {
    void registerClient(RemoteClientInterface client, String playerName) throws RemoteException;
    void makeMove(String playerName, int x, int y) throws RemoteException;
    void disconnect(String playerName) throws RemoteException;
}
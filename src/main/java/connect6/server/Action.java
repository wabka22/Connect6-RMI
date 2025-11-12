package connect6.server;

import connect6.rmi.RemoteClientInterface;
import java.rmi.RemoteException;

@FunctionalInterface
public interface Action {
  void apply(RemoteClientInterface client) throws RemoteException;
}

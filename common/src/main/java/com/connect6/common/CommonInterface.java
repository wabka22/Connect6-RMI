package com.connect6.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CommonInterface extends Remote {
    String sayHello(String name) throws RemoteException;
}

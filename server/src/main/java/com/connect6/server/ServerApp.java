package com.connect6.server;

import com.connect6.common.CommonInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class ServerApp extends UnicastRemoteObject implements CommonInterface {

    protected ServerApp() throws RemoteException {
        super();
    }

    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "! (from RMI server)";
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry started on port 1099");

            ServerApp server = new ServerApp();
            Naming.rebind("rmi://localhost:1099/HelloService", server);

            System.out.println("RMI Server started successfully.");
        } catch (RemoteException re) {
            System.err.println("RMI Registry error: " + re.getMessage());
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

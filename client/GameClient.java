package com.connect6.client;

import com.connect6.common.GameService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            GameService service = (GameService) registry.lookup("GameService");

            service.makeMove(3, 3, 1);
            System.out.println("Текущий игрок: " + service.getCurrentPlayer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

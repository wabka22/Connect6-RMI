package com.connect6.client;

import com.connect6.common.CommonInterface;
import java.rmi.Naming;

public class ClientApp {
    public static void main(String[] args) {
        try {
            CommonInterface service = (CommonInterface) Naming.lookup("rmi://localhost:1099/HelloService");
            String response = service.sayHello("Alexey");
            System.out.println("Response from server: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

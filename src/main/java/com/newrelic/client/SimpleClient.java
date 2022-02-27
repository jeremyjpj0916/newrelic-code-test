package com.newrelic.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class SimpleClient {
    private Socket clientSocket;
    private PrintWriter out;

    public boolean initializeConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println("***Connection successful!***");
            return true;
        } catch (IOException e) {
            System.err.println("Error when initializing connection: " + e.getMessage());
            return false;
        }
    }

    public boolean sendPayload(String payload) {
        try {
            out.println(payload);
            return true;
        } catch (Exception e) {
            System.err.println("Error sending payload: " + e.getMessage());
            return false;
        }
    }

    public void sendValidNumbers(int numbers) {
        try {
            Random random = new Random();
            for (int i = 0; i < numbers; i++) {
                out.println(String.format("%09d", random.nextInt(1000000000)));
            }
        } catch (Exception e) {
            System.err.println("Error sending numbers: " + e.getMessage());
        }
    }

    public boolean terminateConnection() {
        try {
            out.close();
            clientSocket.close();
            return true;
        } catch (IOException e) {
            System.err.println("Could not terminate connection: " + e.getMessage());
            return false;
        }

    }
}

package com.newrelic;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

public final class Application {

    private static final int clientLimit = 5;
    private static final int port = 4000;
    private static final ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(clientLimit + 1);
    private static ServerSocket serverSocket;

    public static void init() {
        try {
            serverSocket = new ServerSocket(port, clientLimit);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        OutputManager outputManager = new OutputManager(concurrentLinkedQueue);
        executorService.submit(outputManager);

        while (true) {
            try {
                ProcessRequests processRequests = new ProcessRequests(
                        serverSocket.accept(), concurrentLinkedQueue);
                executorService.submit(processRequests);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("***Starting Application***");
        init();
    }
}
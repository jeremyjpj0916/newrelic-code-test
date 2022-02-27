package com.newrelic;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProcessRequests implements Runnable {

    private final ConcurrentLinkedQueue<Integer> concurrentLinkedQueue;
    private final Socket clientSocket;
    private BufferedReader clientReader;

    public ProcessRequests(Socket socket, ConcurrentLinkedQueue<Integer> concurrentLinkedQueue) {
        clientSocket = socket;
        this.concurrentLinkedQueue = concurrentLinkedQueue;
    }

    @Override
    public void run() {
        System.out.println("***New client connection initiated!***");

        try {
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (true) {
                String inputLine = clientReader.readLine();

                if (inputLine.equals("terminate")) {
                    System.exit(0);
                }
                else if (!isNineDigitInteger(inputLine)) {
                    break;
                }
                else {
                    int intInput = Integer.parseInt(trimLeadingZeros(inputLine));
                    concurrentLinkedQueue.add(intInput);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientReader.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Trim prior to storing for better mem/perf
    private static String trimLeadingZeros(String source) {
        for (int i = 0; i < source.length(); ++i) {
            char c = source.charAt(i);
            if (c != '0') {
                return source.substring(i);
            }
        }
        return "0"; // If all  ints are 0's return 0.
    }

    // Faster than using regex matching like Pattern.compile("\\d{9}")
    private static boolean isNineDigitInteger(String s) {
        if(s.length() != 9) {
            return false;
        }

        for(int i = 0; i < s.length(); i++) {
            if(!isZeroToNine(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // Sadly standard lib java.lang.Character.isDigit(char ch) includes more than 0 to 9.
    private static boolean isZeroToNine(char c) {
        return (c >= '0' && c <= '9');
    }
}

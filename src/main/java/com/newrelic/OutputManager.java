package com.newrelic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class OutputManager implements Runnable {

    private final ConcurrentLinkedQueue<Integer> concurrentLinkedQueue;
    private static final String outputFile = "numbers.log";
    private final BitSet bitSet = new BitSet(1000000000);
    private static final int waitPeriod = 10000;
    private int uniqueTotal = 0;
    private int uniqueIntervalCount = 0;
    private int duplicateIntervalCount = 0;

    public OutputManager(ConcurrentLinkedQueue<Integer> concurrentLinkedQueue) {
        this.concurrentLinkedQueue = concurrentLinkedQueue;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new PrintSummary(), 0, waitPeriod);

        File file = new File(outputFile);
        if (file.exists()) {
            if(!file.delete()) {
                System.out.println(outputFile + " lacks accessible file permissions. Application terminating!");
                System.exit(0);
            }
        }
    }

    @Override
    public void run() {

        try (FileWriter fileWriter = new FileWriter(outputFile, true);
             BufferedWriter outputWriter = new BufferedWriter(fileWriter)) {

            while (true) {
                try {
                    int number = concurrentLinkedQueue.remove();
                    if (bitSet.get(number)) {
                        duplicateIntervalCount++;
                        continue;
                    }
                    bitSet.set(number);
                    uniqueTotal++;
                    uniqueIntervalCount++;
                    try {
                        outputWriter.write(number + "");
                        outputWriter.newLine();
                        outputWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchElementException e) { //No element is okay if no client input.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final class PrintSummary extends TimerTask {
        @Override
        public void run() {
                System.out.printf("Received %d unique numbers, %d duplicates. Unique total: %d\n",
                        uniqueIntervalCount, duplicateIntervalCount, uniqueTotal);
            uniqueIntervalCount = 0;
            duplicateIntervalCount = 0;

        }
    }
}

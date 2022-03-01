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
import java.util.concurrent.atomic.AtomicInteger;

public final class OutputManager implements Runnable {

    private final ConcurrentLinkedQueue<Integer> concurrentLinkedQueue;
    private static final String outputFile = "numbers.log";
    private final BitSet bitSet = new BitSet(1000000000);
    private static final int waitPeriod = 10000;
    private final AtomicInteger uniqueTotal = new AtomicInteger(0);
    private final AtomicInteger uniqueIntervalCount = new AtomicInteger(0);
    private final AtomicInteger duplicateIntervalCount = new AtomicInteger(0);

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
                        duplicateIntervalCount.getAndIncrement();
                        continue;
                    }
                    bitSet.set(number);
                    uniqueTotal.getAndIncrement();
                    uniqueIntervalCount.getAndIncrement();
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
                        uniqueIntervalCount.get(), duplicateIntervalCount.get(), uniqueTotal.get());
            uniqueIntervalCount.set(0);
            duplicateIntervalCount.set(0);

        }
    }
}

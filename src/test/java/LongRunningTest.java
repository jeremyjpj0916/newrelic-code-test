import com.newrelic.Application;
import com.newrelic.client.SimpleClient;
import org.junit.jupiter.api.*;
import java.io.*;
import java.util.*;

import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LongRunningTest {
    private static final String localHost = "127.0.0.1";
    private static final int correctPort = 4000;
    private static final String outputfile = "numbers.log";
    private static final int standardRequestCount = 5000000;

    @BeforeAll
    static void startServer() {
        Thread serverThread = new Thread(Application::init);
        serverThread.start();
        try {
            Thread.sleep(1000);
            try {
                BufferedReader br = new BufferedReader(new FileReader(outputfile));
                if (br.readLine() == null) {
                    System.out.println(outputfile + " was detected empty. Pre-req test passed!");
                }
            } catch (IOException e) {
                assertTrue(FALSE);
            }
        } catch (InterruptedException e) {
            assertTrue(FALSE);
        }
    }

    @DisplayName("Test five clients")
    @Order(6)
    @Test
    public void testFiveClientValidNumbers() {
        SimpleClient simpleClient1 = new SimpleClient();
        simpleClient1.initializeConnection(localHost, correctPort);

        SimpleClient simpleClient2 = new SimpleClient();
        simpleClient2.initializeConnection(localHost, correctPort);

        SimpleClient simpleClient3 = new SimpleClient();
        simpleClient3.initializeConnection(localHost, correctPort);

        SimpleClient simpleClient4 = new SimpleClient();
        simpleClient4.initializeConnection(localHost, correctPort);

        SimpleClient simpleClient5 = new SimpleClient();
        simpleClient5.initializeConnection(localHost, correctPort);

        //Send 25 million numbers
        Thread serverThread1 = new Thread(() -> {
            simpleClient1.sendValidNumbers(standardRequestCount);
            simpleClient1.terminateConnection();
        });

        Thread serverThread2 = new Thread(() -> {
            simpleClient2.sendValidNumbers(standardRequestCount);
            simpleClient2.terminateConnection();
        });

        Thread serverThread3 = new Thread(() -> {
            simpleClient3.sendValidNumbers(standardRequestCount);
            simpleClient3.terminateConnection();
        });

        Thread serverThread4 = new Thread(() -> {
            simpleClient4.sendValidNumbers(standardRequestCount);
            simpleClient4.terminateConnection();
        });

        Thread serverThread5 = new Thread(() -> {
            simpleClient5.sendValidNumbers(standardRequestCount);
            simpleClient5.terminateConnection();
        });

        serverThread1.start();
        serverThread2.start();
        serverThread3.start();
        serverThread4.start();
        serverThread5.start();

        try {
            //Sleep main thread 50 second for processing evaluation and output to take place
            Thread.sleep(50000);
            BufferedReader br = new BufferedReader(new FileReader(outputfile));
            String line;
            int fileLineCount = 0;
            Set<String> uniqueEntries = new HashSet<>(25000000);
            while((line=br.readLine())!=null) {
                Integer.parseInt(line);
                fileLineCount++;
                uniqueEntries.add(line);
            }
            br.close();
            System.out.println("Lines counted in the file: " + fileLineCount);
            System.out.println("Entries counted in the hashset: " + uniqueEntries.size());
            assertEquals(fileLineCount, uniqueEntries.size());
        } catch (NumberFormatException | IOException | InterruptedException e) {
            assertTrue(FALSE);
        }
    }
}

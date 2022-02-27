import com.newrelic.Application;
import com.newrelic.client.SimpleClient;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConsiderationTests {
    private static final String localHost = "127.0.0.1";
    private static final int correctPort = 4000;
    private static final int invalidPort = 4001;
    private static final String outputfile = "numbers.log";
    private static Thread serverThread;
    private int standardRequestCount = 500000;


    @BeforeAll
    static void startServer() {
        Application application = new Application();
        serverThread = new Thread(() -> {
            application.init();
        });
        serverThread.start();
        try {
            Thread.sleep(1000);
            try {
                BufferedReader br = new BufferedReader(new FileReader(outputfile));
                if (br.readLine() == null) {
                    System.out.println(outputfile + " was detected empty. Pre-req test passed!");
                }
            } catch (FileNotFoundException e) {
                assertTrue(FALSE);
            } catch (IOException e) {
                assertTrue(FALSE);
            }
        } catch (InterruptedException e) {
            assertTrue(FALSE);
        }
    }

    @DisplayName("Test valid connection")
    @Test
    @Order(1)
    void testValidConnection() {
        SimpleClient simpleClient = new SimpleClient();
        assertTrue(simpleClient.initializeConnection(localHost, correctPort));
    }

    @DisplayName("Test invalid connection")
    @Test
    @Order(2)
    void testInvalidConnection() {
        SimpleClient simpleClient = new SimpleClient();
        assertEquals(false, simpleClient.initializeConnection(localHost, invalidPort));
    }

    @DisplayName("Test invalid client input")
    @Test
    @Order(3)
    public void testInvalidInput() {
        SimpleClient simpleClient = new SimpleClient();
        simpleClient.initializeConnection(localHost, correctPort);
        assertTrue(simpleClient.sendPayload("invalid"));
        assertTrue(simpleClient.sendPayload("00000000i"));
        assertTrue(simpleClient.sendPayload("00000000i"));
        assertTrue(simpleClient.sendPayload("12345"));

        try {
            BufferedReader br = new BufferedReader(new FileReader(outputfile));
            if (br.readLine() == null) {
                //Success case. Empty file means we did not handle junk input.
            }
        } catch (FileNotFoundException e) {
            assertTrue(FALSE);
        } catch (IOException e) {
            assertTrue(FALSE);
        }
        simpleClient.terminateConnection();
    }

    @DisplayName("Test duplicate client input")
    @Test
    @Order(4)
    public void testDuplicateInput() {
        SimpleClient simpleClient = new SimpleClient();
        simpleClient.initializeConnection(localHost, correctPort);
        assertTrue(simpleClient.sendPayload("123456789"));
        assertTrue(simpleClient.sendPayload("123456789"));

        try {
            Thread.sleep(10);
            BufferedReader br = new BufferedReader(new FileReader(outputfile));
            String line;
            int fileLineCount = 0;
            Set<String> uniqueEntries = new HashSet<String>(2);
            while((line=br.readLine())!=null) {
                Integer.parseInt(line);
                fileLineCount++;
                uniqueEntries.add(line);
            }
            br.close();
            System.out.println("Lines counted in the file: " + fileLineCount);
            System.out.println("Entries counted in the hashset: " + uniqueEntries.size());
            assertEquals(fileLineCount, uniqueEntries.size());
        } catch (IOException | InterruptedException e) {
            assertTrue(FALSE);
        }
        simpleClient.terminateConnection();
    }

    @DisplayName("Test single client")
    @Test
    @Order(5)
    public void testSingleClientValidNumbers() {
        SimpleClient simpleClient = new SimpleClient();
        simpleClient.initializeConnection(localHost, correctPort);
        simpleClient.sendValidNumbers(standardRequestCount);
        try {
            Thread.sleep(10000); //Sleep thread so I get some output from Server before test finishes
            BufferedReader br = new BufferedReader(new FileReader(outputfile));
            String line;
            int fileLineCount = 0;
            Set<String> uniqueEntries = new HashSet<String>(1000000);
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
        simpleClient.terminateConnection();
    }

    @DisplayName("Test five clients")
    @Test
    @Order(6)
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
            Thread.sleep(10000); //Sleep thread so I get some output from Server before test finishes
            BufferedReader br = new BufferedReader(new FileReader(outputfile));
            String line;
            int fileLineCount = 0;
            Set<String> uniqueEntries = new HashSet<String>(5000000);
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

    @DisplayName("Test six clients to ensure limit is working")
    @Test
    @Order(7)
    public void testSixClientValidNumbers() {
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

        Thread serverThread1 = new Thread(() -> {
            simpleClient1.sendValidNumbers(standardRequestCount);
        });

        Thread serverThread2 = new Thread(() -> {
            simpleClient2.sendValidNumbers(standardRequestCount);
        });

        Thread serverThread3 = new Thread(() -> {
            simpleClient3.sendValidNumbers(standardRequestCount);
        });

        Thread serverThread4 = new Thread(() -> {
            simpleClient4.sendValidNumbers(standardRequestCount);
        });

        Thread serverThread5 = new Thread(() -> {
            simpleClient5.sendValidNumbers(standardRequestCount);
        });

        serverThread1.start();
        serverThread2.start();
        serverThread3.start();
        serverThread4.start();
        serverThread5.start();

        try {
            Thread.sleep(10000); //Sleep thread so I get some output from Server before test finishes
            BufferedReader br1 = new BufferedReader(new FileReader(outputfile));
            String line;
            Set<String> uniqueEntries = new HashSet<String>(8000000);
            int fileLineCount1 = 0;
            while((line=br1.readLine())!=null) {
                Integer.parseInt(line);
                uniqueEntries.add(line);
                fileLineCount1++;
            }
            br1.close();

            SimpleClient simpleClient6 = new SimpleClient();
            simpleClient6.initializeConnection(localHost, correctPort);
            Random random = new Random();
            String newUniqueValue = String.format("%09d", random.nextInt(1000000000));
            while(uniqueEntries.contains(newUniqueValue)){
                newUniqueValue = String.format("%09d", random.nextInt(1000000000));
            }
            simpleClient6.sendPayload(newUniqueValue);
            Thread.sleep(1000);
            BufferedReader br2 = new BufferedReader(new FileReader(outputfile));
            int fileLineCount2 = 0;
            while(br2.readLine()!=null) {
                fileLineCount2++;
            }
            br2.close();
            assertEquals(fileLineCount1, fileLineCount2);
            simpleClient6.terminateConnection();
            simpleClient5.terminateConnection();
            simpleClient4.terminateConnection();
            simpleClient3.terminateConnection();
            simpleClient2.terminateConnection();
            simpleClient1.terminateConnection();
        } catch (NumberFormatException | IOException | InterruptedException e) {
            assertTrue(FALSE);
        }

    }

/*    @DisplayName("Test termination call")
    @Order(9)
    @Test
    public void testTermination() {
        System.out.println("Testing terminate command");
        SimpleClient simpleClient = new SimpleClient();
        simpleClient.initializeConnection(localHost, correctPort);
        simpleClient.sendPayload("terminate");
    }*/
}

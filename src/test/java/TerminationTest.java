import com.newrelic.Application;
import com.newrelic.client.SimpleClient;
import org.junit.jupiter.api.*;
import java.io.*;

import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TerminationTest {
    private static final String localHost = "127.0.0.1";
    private static final int correctPort = 4000;
    private static final String outputfile = "numbers.log";

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

    @DisplayName("Test termination call")
    @Order(1)
    @Test
    public void testTermination() {
        System.out.println("Testing terminate command");
        SimpleClient simpleClient = new SimpleClient();
        simpleClient.initializeConnection(localHost, correctPort);
        simpleClient.sendPayload("terminate");
    }

}

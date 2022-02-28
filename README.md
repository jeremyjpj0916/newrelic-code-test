# Table of Contents
1. [Solution Approach](README.md#solution-approach)
2. [Tests](README.md#tests)
3. [Repo structure](README.md#repo-structure)
4. [Challenge Guidelines](README.md#challenge-guidelines)

# Solution Approach
- Java 11 solution to the New Relic Coding Challenge described below 
- Maven for minor build/test dependency management
- Written all up in Intellij IDE Community edition as my favorite java IDE

The Main script is contained in `./src/main/java/com/newrelic/Application.java`.
The solution uses `java.io.*` `java.net.*` `java.util.*` No Dependencies other than maven tools to help with integrated 
build tests using ```org.junit.jupiter.``` and ```maven-jar-plugin``` for jar creation.

The server is limited to 5 inbound active client socket connections at a time each handled via multithreading, 
1 thread per client. For this I used a:
```
private static final ExecutorService executorService = Executors.newFixedThreadPool(clientLimit + 1);
```

Why the +1 you might ask? I decided to keep a singular thread dedicated to backend just handling the logging + writing
to number.log file via the OutputManager class.

<b>Some assumptions</b>
- I assume the disk where the java application will execute has proper permissions to write to the locical volume mount with proper disk space available.
- I assume no other application is currently running on localhost port 4000 and you have proper permissions to bind on that port as well.
- I assume the computer has a proper amount of RAM for runtime JVM execution to store and process data at runtime.

## Future Enhancements
I could add a batch and do file i/o less often than every time here with singular thread writes. Or rewrite
the writer to be multithreaded as well in the background and safely find a way to write to file across parallel threads.
Would have to drop my ```BitSet``` usage in the OutputManager to do so as its not multi-thread safe. 
But actually was able to reach desired perf without it though seemingly!

# Tests
A ```SimpleClient.java``` client is provided in the ```com.newrelic.client``` package folder used for my integration tests. 

```ConsiderationTests.java``` covered the main baseline of application requirements and is required to pass upon build. 

```LongRunningTest.java``` validates the performance over a longer time with the application and ensures no degradation 
of throughput. 

Output with my ```ConsiderationTests```(all must pass for build to complete!):

    numbers.log cleared/created new             : Successful (validated in the BeforeAll step)
    Test valid connection                       : Successful
    Test invalid connection                     : Successful
    Test invalid client input                   : Successful
    Test duplicate client input                 : Successful
    Test single client input                    : Successful
    Test five client input                      : Successful
    Test six clients to ensure limit is working : Successful

The ```TerminationTest.java``` I validated in isolate the client termination command 
as it kills the JVM. Ran out of time on ways to mock that one up and override the default behavior as 
I do see ways to get around that hurdle to be able to include it in my ```ConsiderationTests``` as a future
cleanup to my integrated testing.

## Repo structure

    .
    ├── README.md
    ├── pom.xml
    ├── src
    │   └── main/java/com/newrelic
    │       └── Application.java
    │       └── OutputManager.java
    │       └── ProcessRequests.java
    │   └── main/java/com/newrelic/client
    │       └── SimpleClient.java
    │   └── test/java
    │       └── ConsiderationTests.java
    │       └── LongRunningTest.java
    │       └── TerminationTest.java
    ├── target
    │   └── Application-1.0.0-SNAPSHOT.jar

The jar I simply provided here for convenience as long as you have a compatible JVM.

# Execution of the jar

![JarExecutionStandalone](https://user-images.githubusercontent.com/31913027/155912580-67c8d01e-1f24-4903-a882-414d4bea8f0a.png)

To execute JAR yourself, ensure you have your ```JAVA_HOME``` and ```PATH``` set correctly for java and hopefully your IDE, Windows ex:
```
C:\Users\jerem>echo %JAVA_HOME%
C:\Program Files\Eclipse Adoptium\jdk-11.0.14.101-hotspot\
```

```
C:\Users\jerem>echo %PATH%
C:\Program Files\Eclipse Adoptium\jdk-11.0.14.101-hotspot\bin;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2021.3.2\bin
```

If you wish to build from source and run the ```ConsiderationTests``` simultaneously, you can use the ```mvn``` command line but I would recommend using your IDE like so:

![image](https://user-images.githubusercontent.com/31913027/155913738-bf89fb52-fbf5-45c5-bf0c-57e3cb3330c4.png)

This will also produce your own local JAR in the ```target``` directory ready for use!

![image](https://user-images.githubusercontent.com/31913027/155913906-ced69aed-4b45-438f-9f7a-f3583502b296.png)

# Performance Testing Results

With 5 multi-thread clients in a 10-second interval each sending 500k requests(5 separate tests), derived from my ```ConsiderationTests.testFiveClientValidNumbers()```:
```
Received 2496905 unique numbers, 3095 duplicates. Unique total: 2496905
Received 2496927 unique numbers, 3073 duplicates. Unique total: 2496927
Received 2496856 unique numbers, 3144 duplicates. Unique total: 2496856
Received 2496883 unique numbers, 3117 duplicates. Unique total: 2496883
Received 2496934 unique numbers, 3066 duplicates. Unique total: 2496934
```
We can see that I am handling all 2.5 million requests here per 10 second.

![FiveClientValidInputTest](https://user-images.githubusercontent.com/31913027/155912608-ef2bbe61-f94d-4231-9801-cc431189f7f5.png)


With 5 multi-thread clients in a 50-second interval each sending 5mil requests(5 separate tests), derived from my ```LongRunningTest.testFiveClientValidNumbers()```:

![FiveClientLongRunningValidInputTest](https://user-images.githubusercontent.com/31913027/155912634-8a6be775-5507-41e9-8a3f-e26093f14562.png)

And even running the JAR in isolate along with the long running test within my IDE:

![JarExecutionwithLongRunningTest](https://user-images.githubusercontent.com/31913027/155912690-0c962dbf-c4e9-49dc-8e8a-27c05dbbd048.png)


All tests were performed on my personal windows PC w following specs:
```
System Model:	                        MSI GL65 Leopard 10SFKV
Processor:	                        Intel(R) Core(TM) i7-10750H CPU @ 2.60GHz, 2592 Mhz, 6 Core(s), 12 Logical Processor(s)
Installed Physical Memory (RAM):	16.0 GB
```

# Challenge Guidelines

![Guidelines](https://user-images.githubusercontent.com/31913027/155912531-df285d64-d883-45e2-965f-11eeb21cef9a.png)



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

## Future Enhancements
I could add a batch and do file i/o less often than every time here with singular thread writes. Or rewrite
the writer to be multithreaded as well in the background and safely find a way to write to file across parallel threads.
Would have to drop my ```BitSet``` usage in the OutputManager to do so as its not multi thread safe. 
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

PIC HERE

With 5 multi-thread clients in a 50-second interval each sending 5mil requests(5 separate tests), derived from my ```LongRunningTest.testFiveClientValidNumbers()```:

PICS HERE

# Challenge Guidelines

PIC GOES HERE


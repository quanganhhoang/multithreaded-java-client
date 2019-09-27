package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.client.RequestThread;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MultithreadedClient {
  private static final Logger logger = LogManager.getLogger(Main.class.getName());

  private static final int MIN_JITTER_TIME = 250;             // milliseconds
  private static final int MAX_JITTER_TIME = 1000;            // milliseconds
  private static final int MAX_TERMINATION_WAIT_TIME = 60;    // seconds

  private static final int PHASE1_START_TIME = 1;
  private static final int PHASE1_END_TIME = 90;
  private static final int PHASE2_START_TIME = 91;
  private static final int PHASE2_END_TIME = 360;
  private static final int PHASE3_START_TIME = 361;
  private static final int PHASE3_END_TIME = 420;

  private Integer numThreads;
  private Integer numSkiers; // effectively the skier's ID
  private Integer numSkiLifts; // default 40, range 5-60
  private Integer numRuns; // numRuns: default 10, max 20

  private String ipAddress = "";
  private String port = "";

  public MultithreadedClient(Integer numThreads, Integer numSkiers, Integer numSkiLifts, Integer numRuns,
      String ipAddress, String port) {
    this.numThreads = numThreads;
    this.numSkiers = numSkiers;
    this.numSkiLifts = numSkiLifts;
    this.numRuns = numRuns;
    this.ipAddress = ipAddress;
    this.port = port;
  }

  public void run(int phase) {
    logger.info("Starting phase " + phase + "...");
    logger.info("====================================");
    // calculate number of threads to run for each phase
    int numPoolThreads = getNumThreads(numThreads, phase);
    int numRequestPerThread = numSkiers / numPoolThreads;
    if (numSkiers % numPoolThreads != 0) numPoolThreads++;

    ExecutorService executor = Executors.newFixedThreadPool(numPoolThreads);
    int numThreadTrigger = numPoolThreads / 10; // num of completed threads to trigger next phase
    CountDownLatch countDownLatch = new CountDownLatch(numThreadTrigger);

    int threadCount = 0, startSkierID = 1, remainingNumIds = numSkiers;

    int startTime = getStartEndTimeByPhase(phase, true);
    int endTime = getStartEndTimeByPhase(phase, false);

    Runnable thread;
    for (int i = 0; i < numPoolThreads; i++) {
      String name = "Thread #" + threadCount;
      int range = Math.min(numRequestPerThread, remainingNumIds);
      ThreadInfo info = new ThreadInfo(name, ipAddress, port, startSkierID, startSkierID + range,
          PHASE1_START_TIME, PHASE2_END_TIME, numRuns, numSkiLifts);

      thread = new Thread(new RequestThread(phase, info, countDownLatch));

      threadCount++;
      remainingNumIds -= numRequestPerThread;
      startSkierID += numThreads;

      executor.execute(thread);
    }

    logger.info("Spawned threads for phase " + phase);

    try {
      countDownLatch.await();
      logger.info("10% of threads from phase " + phase + " finished");
      if (phase + 1 <= 3) run(phase + 1);
    } catch (InterruptedException e) {
      logger.error("ERROR: CountdownLatch failed to work");
    }

    try {
      int tries = 0;
      executor.shutdown();
      while (!executor.awaitTermination(MAX_TERMINATION_WAIT_TIME, TimeUnit.SECONDS)) {
        executor.shutdownNow();
        if (++tries > 3) {
          logger.fatal("Unable to shutdown thread executor. Calling System.exit()...");
          System.exit(0);
        }
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }

//    logger.info("Phase " + phase + " complete!");
//    logger.info("====================================");
//    logger.info("Threads spawned: " + threadCount);
//    logger.info("Done!");
  }

  public int getStartEndTimeByPhase(int phase, boolean isStart) throws IllegalArgumentException {
    switch (phase) {
      case 1:
        return isStart ? PHASE1_START_TIME : PHASE1_END_TIME;
      case 2:
        return isStart ? PHASE2_START_TIME : PHASE2_END_TIME;
      case 3:
        return isStart ? PHASE3_START_TIME : PHASE3_END_TIME;
      default:
        throw new IllegalArgumentException();
    }
  }

//  public void runPhase1() {
//    // Phase 1: launch numThreads/4 threads
//    int numPoolThreads = getNumThreads(numThreads, 1);
//    int numRequestPerThread = numSkiers / numPoolThreads;
//    if (numSkiers % numPoolThreads != 0) numPoolThreads++;
//
//    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
//    int numThreadTrigger = numPoolThreads / 10; // num of completed threads to trigger phase 2
//    CountDownLatch countDownLatch = new CountDownLatch(numThreadTrigger);
//
//    int threadCount = 0, startSkierID = 1, remainingNumIds = numSkiers;
//    Runnable thread;
//    for (int i = 0; i < numPoolThreads; i++) {
//      String name = "Thread #" + threadCount;
//      int range = Math.min(numRequestPerThread, remainingNumIds);
//      ThreadInfo info = new ThreadInfo(name, ipAddress, port, startSkierID, startSkierID + range,
//          PHASE1_START_TIME, PHASE2_END_TIME, numRuns, numSkiLifts);
//
//      thread = new Thread(new RequestThread(info, countDownLatch));
//
//      threadCount++;
//      remainingNumIds -= numRequestPerThread;
//      startSkierID += numThreads;
//
//      executor.execute(thread);
//    }
//
//    logger.info("Spawned threads for phase 1");
//
//    try {
//      countDownLatch.await();
//      logger.info("10% of threads from phase 1 finished");
//      runPhase2();
//    } catch (InterruptedException e) {
//      logger.error("ERROR: CountdownLatch failed to work");
//    }
//
//    try {
//      int tries = 0;
//      executor.shutdown();
//      while (!executor.awaitTermination(MAX_TERMINATION_WAIT_TIME, TimeUnit.SECONDS)) {
//        executor.shutdownNow();
//        if (++tries > 3) {
//          logger.fatal("Unable to shutdown thread executor. Calling System.exit()...");
//          System.exit(0);
//        }
//      }
//    } catch (InterruptedException e) {
//      executor.shutdownNow();
//      Thread.currentThread().interrupt();
//    }
//
//    logger.info("Run complete!");
//    logger.info("====================================");
//    logger.info("Threads spawned: " + threadCount);
//    logger.info("Done!");
//  }

  public void runPhase2() {

  }

  public void runPhase3() {

  }


  public Integer getNumThreads(Integer numThreads, int phase) {
    switch (phase) {
      case 1:
      case 3:
        return numThreads / 4;
      case 2:
        return numThreads;
      default:
        return 0;
    }
  }
}

package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.logging.StopWatch;
import edu.neu.ccs.cs6650.model.LatencyStat;
import edu.neu.ccs.cs6650.model.ThreadInfo;
import edu.neu.ccs.cs6650.model.ThreadStat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultithreadedCallable {
  private static final Logger logger = LogManager.getLogger(Main.class.getName());

  private static final int MAX_TERMINATION_WAIT_TIME = 20;    // seconds

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

  private String ipAddress;
  private String port;

  private long totalRequestSuccess;
  private long totalRequestFail;
  private List<LatencyStat> latencyStats = new ArrayList<>();
  private CountDownLatch countDownLatch;

//  private StopWatch sw = new StopWatch();

  public MultithreadedCallable(Integer numThreads, Integer numSkiers, Integer numSkiLifts, Integer numRuns,
      String ipAddress, String port) {
    this.numThreads = numThreads;
    this.numSkiers = numSkiers;
    this.numSkiLifts = numSkiLifts;
    this.numRuns = numRuns;
    this.ipAddress = ipAddress;
    this.port = port;
  }

  public void run() {
//    sw.start();
    // phase 1:
    logger.info("Starting phase 1...");
//    sw.addNamedSplit("Preparing phase 1 threads");
    List<RequestCallable> threadList1 = prepareThreads(1);
//    sw.addNamedSplit("Finished preparing phase 1 threads");
    ExecutorService executor1 = Executors.newFixedThreadPool(threadList1.size());

    logger.info(threadList1.size() + " threads spawned for phase 1");
//    sw.addNamedSplit("Invoking phase 1 threads");
    List<Future<ThreadStat>> resultPhase1 = new ArrayList<>();
    try {
      resultPhase1 = executor1.invokeAll(threadList1);
    } catch (InterruptedException e) {
      logger.info(e);
    }

    countDown();
    logger.info("Starting phase 2...");

    // phase 2
//    sw.addNamedSplit("Preparing phase 2 threads");
    List<RequestCallable> threadList2 = prepareThreads(2);
//    sw.addNamedSplit("Finished preparing phase 2 threads");
    ExecutorService executor2 = Executors.newFixedThreadPool(threadList2.size());
    logger.info(threadList2.size() + " threads spawned for phase 2");
//    sw.addNamedSplit("Invoking phase 2 threads");
    List<Future<ThreadStat>> resultPhase2 = new ArrayList<>();
    try {
      resultPhase2 = executor2.invokeAll(threadList2);
    } catch (InterruptedException e) {
      logger.info(e);
    }

    countDown();

    logger.info("Starting phase 3...");
//    sw.addNamedSplit("Preparing phase 3 threads");
    List<RequestCallable> threadList3 = prepareThreads(3);
    ExecutorService executor3 = Executors.newFixedThreadPool(threadList3.size());
    logger.info(threadList3.size() + " threads spawned for phase 3");
    List<Future<ThreadStat>> resultPhase3 = new ArrayList<>();
//    sw.addNamedSplit("Invoking phase 3 threads");
    try {
      resultPhase3 = executor3.invokeAll(threadList3);
    } catch (InterruptedException e) {
      logger.info(e);
    }

//    sw.addNamedSplit("Collecting data...");
    List<Future<ThreadStat>> totalResult =
        Stream.of(resultPhase1, resultPhase2, resultPhase3)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());

    logger.info("Number of threads result: " + totalResult.size());
    for (Future<ThreadStat> res : totalResult) {
      try {
//        logger.info("Num success: " + res.get().getNumRequestSuccess());
//        logger.info("Num failures: " + res.get().getNumRequestFail());
        totalRequestSuccess += res.get().getNumRequestSuccess();
        totalRequestFail += res.get().getNumRequestFail();

        latencyStats.addAll(res.get().getStatList());
      } catch (InterruptedException | ExecutionException | CancellationException e) {

        logger.error("ERROR: Exception returning number of request sent.");
        logger.error(e);
        e.printStackTrace();
      }
    }
//    sw.addNamedSplit("Shutting down executors");
    shutdownExecutor(executor1);
    shutdownExecutor(executor2);
    shutdownExecutor(executor3);
//    sw.addNamedSplit("Finished!");
//    System.out.println(sw.toString());
  }

  private void countDown() {
    try {
      countDownLatch.await(3L, TimeUnit.SECONDS);
      logger.info("10% of threads from current phase finished");
    } catch (InterruptedException e) {
      logger.error("ERROR: CountdownLatch failed to work");
    }
  }

  private void shutdownExecutor(ExecutorService executor) {
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
  }

  private List<RequestCallable> prepareThreads(int phase) {
    int numPoolThreads = getNumThreads(numThreads, phase);

    int skierIdRangePerThread = (int) (numSkiers * 1.0 / numPoolThreads);
//    System.out.println("num skiers per thread: " + skierIdRangePerThread);
    int startSkierID = 1, remainingNumIds = numSkiers;

    int startTime = getStartEndTimeByPhase(phase, true);
    int endTime = getStartEndTimeByPhase(phase, false);

    int numRequestPerThread = getNumRequestPerThread(numPoolThreads, phase);

    // num of completed threads to trigger next phase
    countDownLatch = new CountDownLatch(numPoolThreads / 10);

    List<RequestCallable> threadList = new ArrayList<>();
    for (int i = 0; i < numPoolThreads; i++) {
//      System.out.println("startSkierId: " + startSkierID);
//      System.out.println("remaining: " + remainingNumIds);
      int range = (i == numPoolThreads - 1) ? remainingNumIds
                                            : skierIdRangePerThread;
      if (range == 0) break;
      ThreadInfo info = new ThreadInfo(ipAddress, port, startSkierID, startSkierID + range,
          startTime, endTime, numRuns, numSkiLifts, numRequestPerThread, phase);

      threadList.add(new RequestCallable(info, countDownLatch));

      remainingNumIds -= range;
      startSkierID += range;
    }

    return threadList;
  }

  private int getStartEndTimeByPhase(int phase, boolean isStart) throws IllegalArgumentException {
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

  private Integer getNumThreads(Integer numThreads, int phase) {
    switch (phase) {
      case 1:
      case 3:
        return (int) Math.ceil(numThreads / 4.0);
      case 2:
        return numThreads;
      default:
        return 0;
    }
  }

  private Integer getNumRequestPerThread(int numPoolThreads, int phase) throws IllegalArgumentException {
    switch (phase) {
      case 1:
      case 3:
        return this.numRuns / 10 * this.numSkiers / numPoolThreads;
      case 2:
        return this.numRuns * 8 / 10 * this.numSkiers / numPoolThreads;
      default:
        throw new IllegalArgumentException();
    }
  }

  public long getTotalRequestFail() {
    return totalRequestFail;
  }

  public long getTotalRequestSuccess() {
    return totalRequestSuccess;
  }

  public List<LatencyStat> getLatencyStats() {
    return latencyStats;
  }
}

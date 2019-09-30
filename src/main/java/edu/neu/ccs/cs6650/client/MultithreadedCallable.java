package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.model.LatencyStat;
import edu.neu.ccs.cs6650.model.ThreadStat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("Duplicates")
public class MultithreadedCallable {
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

  private AtomicInteger totalRequestSuccess = new AtomicInteger(0);
  private AtomicInteger totalRequestFail = new AtomicInteger(0);
  private List<LatencyStat> latencyStats = new ArrayList<>();

  public MultithreadedCallable(Integer numThreads, Integer numSkiers, Integer numSkiLifts, Integer numRuns,
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
    System.out.println("num pool threads: " + numPoolThreads);
    int skierIdRangePerThread = (int) Math.ceil(numSkiers * 1.0 / numPoolThreads);

    ExecutorService executor = Executors.newFixedThreadPool(numPoolThreads);
    // num of completed threads to trigger next phase
    CountDownLatch countDownLatch = new CountDownLatch(numPoolThreads / 10);

    int threadCount = 0, startSkierID = 1, remainingNumIds = numSkiers;

    int startTime = getStartEndTimeByPhase(phase, true);
    int endTime = getStartEndTimeByPhase(phase, false);

    int numRequestPerThread = getNumRequestPerThread(numPoolThreads, phase);
    System.out.println("Num skiers per thread: " + skierIdRangePerThread);

    Callable thread;
    for (int i = 0; i < numPoolThreads; i++) {
      String name = "Thread #" + threadCount;
//      System.out.println("startSkierId: " + startSkierID);
//      System.out.println("remaining: " + remainingNumIds);
      int range = Math.min(skierIdRangePerThread, remainingNumIds);
      ThreadInfo info = new ThreadInfo(name, ipAddress, port, startSkierID, startSkierID + range,
          startTime, endTime, numRuns, numSkiLifts, numRequestPerThread);

      thread = new RequestCallable(phase, info, countDownLatch);

      threadCount++;
      remainingNumIds -= range;
      startSkierID += range;

      Future<ThreadStat> future = executor.submit(thread);
      try {
        totalRequestSuccess.addAndGet(future.get().getNumRequestSuccess());
        totalRequestFail.addAndGet(future.get().getNumRequestFail());
        // how to concatenate all latency result here!?!?
        latencyStats.addAll(future.get().getStatList());
      } catch (InterruptedException | ExecutionException e) {
        logger.error("ERROR: Exception returning number of request sent.");
        logger.error(e);
      }
    }

    logger.info(threadCount + " threads spawned for phase " + phase);

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
      logger.info("Phase " + phase + " complete!");
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

  public Integer getNumThreads(Integer numThreads, int phase) {
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

  public Integer getNumRequestPerThread(int numPoolThreads, int phase) throws IllegalArgumentException {
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

  public AtomicInteger getTotalRequestFail() {
    return totalRequestFail;
  }

  public AtomicInteger getTotalRequestSuccess() {
    return totalRequestSuccess;
  }

  public List<LatencyStat> getLatencyStats() {
    return latencyStats;
  }
}

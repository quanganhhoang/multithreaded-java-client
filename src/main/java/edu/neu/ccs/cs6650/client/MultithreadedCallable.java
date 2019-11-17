package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.model.LatencyStat;
import edu.neu.ccs.cs6650.model.ThreadInfo;
import edu.neu.ccs.cs6650.model.ThreadStat;

import java.util.ArrayList;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultithreadedCallable {
  private static final Logger logger = LogManager.getLogger(Main.class.getName());

  private static final int MAX_TERMINATION_WAIT_TIME = 10;    // seconds

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

  private ExecutorService executor = Executors.newFixedThreadPool(Config.MAX_THREAD_POOL);
  private CompletionService<ThreadStat> completionService = new ExecutorCompletionService<>(executor);
  private CountDownLatch countDownLatch;

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
    logger.info("Starting phase 1...");
    prepareThreads(1);

    countDown(); // blocking call

    logger.info("Starting phase 2...");
    prepareThreads(2);

    countDown(); // blocking call

    logger.info("Starting phase 3...");
    prepareThreads(3);

    retrieveResult(completionService);

    // shutting down executors
    shutdownExecutor();
  }

  private void retrieveResult(CompletionService cs) {
    int n = getNumThreads(0); // get total number of threads submitted
    logger.info("Retrieving results from " + n + " threads...");
    try {
      for (int i = 0; i < n; i++) {
        Future f = cs.take();
        ThreadStat stat = (ThreadStat) f.get();
        totalRequestSuccess += stat.getNumRequestSuccess();
        totalRequestFail += stat.getNumRequestFail();

        latencyStats.addAll(stat.getStatList());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException | CancellationException e) {
      logger.error("ERROR: Exception returning number of request sent.", e);
    }
  }

  private void countDown() {
    try {
      countDownLatch.await(3L, TimeUnit.SECONDS);
      logger.info("10% of threads from current phase finished");
    } catch (InterruptedException e) {
      logger.error("ERROR: CountdownLatch failed to work");
    }
  }

  private void shutdownExecutor() {
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

  private void prepareThreads(int phase) {
    int numPoolThreads = getNumThreads(phase);

    int skierIdRangePerThread = (int) (numSkiers * 1.0 / numPoolThreads);
    int startSkierID = 1, remainingNumIds = numSkiers;
    int startTime = getStartEndTimeByPhase(phase, true);
    int endTime = getStartEndTimeByPhase(phase, false);

    int numRequestPerThread = getNumRequestPerThread(numPoolThreads, phase);

    // num of completed threads to trigger next phase
    countDownLatch = new CountDownLatch(numPoolThreads / 10);

    for (int i = 0; i < numPoolThreads; i++) {
      int range = (i == numPoolThreads - 1) ? remainingNumIds
                                            : skierIdRangePerThread;
      if (range == 0) break;
      ThreadInfo info = new ThreadInfo(ipAddress, port, startSkierID, startSkierID + range,
          startTime, endTime, numRuns, numSkiLifts, numRequestPerThread, phase);

      completionService.submit(new RequestCallable(info, countDownLatch));

      remainingNumIds -= range;
      startSkierID += range;
    }
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

  private Integer getNumThreads(int phase) {
    switch (phase) {
      case 1:
      case 3:
        return (int) Math.ceil(numThreads / 4.0);
      case 2:
        return numThreads;
      case 0:
        return numThreads * 3 / 2;
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
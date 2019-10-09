package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.logging.StopWatch;
import edu.neu.ccs.cs6650.model.LatencyStat;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Main {
  private static final Logger logger = LogManager.getLogger(Main.class.getName());

  private static final StopWatch sw = new StopWatch();
//  private static final Integer DAY_LENGTH_MIN = 420; // each ski day is of length 420 minutes

  private static final int MIN_NUM_LIFTS = 5;
  private static final int MAX_NUM_LIFTS = 60;

  private static final String CSV_PATH = "/Users/qa/Documents/Github/cs6650/stats.csv";

  public static void main(String[] args) {
    // TODO: Performance testing for assignment 2 (32, 64, 128, 256 threads) -> expect database deadlocks
//    System.setProperty("java.net.preferIPv4Stack", "true");

    Integer numThreads = 256; // max = 256
    Integer numSkiers = 20000; // effectively the skier's ID | max = 50000
    Integer numSkiLifts = 40; // default 40, range 5-60
    Integer numRuns = 20; // numRuns: default 10, max 20

    String ipAddress = "";
    String port = "";

    if (args.length != 0) {
      numThreads = Integer.valueOf(args[0]);
      numSkiers = Integer.valueOf(args[1]);
      numSkiLifts = Integer.valueOf(args[2]);
      numRuns = Integer.valueOf(args[3]);
      ipAddress = args[4];
      port = args[5];
    }

    logger.info("Number of threads: " + numThreads);
    logger.info("Number of skiers: " + numSkiers);
    logger.info("Number of ski lifts: " + numSkiLifts);
    logger.info("Average number of runs per skier: " + numRuns);
    logger.info("Program starting...");

//    MultithreadedClient client = new MultithreadedClient(numThreads, numSkiers, numSkiLifts, numRuns, ipAddress, port);
    MultithreadedCallable client = new MultithreadedCallable(numThreads, numSkiers, numSkiLifts, numRuns, ipAddress, port);
    sw.start();
    client.run();
    sw.stop();

    double totalElapsedTime = sw.getElapsedTime() / 1000;
    System.out.println();
    logger.info("Number of requests sent: " + client.getTotalRequestSuccess());
    logger.info("Number of requests failed: " + client.getTotalRequestFail());
    logger.info("Total run time: " + totalElapsedTime + " seconds");

    // adjust start time to system' start time then output results to csv
    List<LatencyStat> latencyStats = Util.adjustLatencyStartTime(client.getLatencyStats(), (long) sw.getStartTime());
    latencyStats.sort(Comparator.comparingLong(LatencyStat::getStartTime));

    try {
      Util.writeToCSV(latencyStats, CSV_PATH);
      logger.info("Results written to CSV!\n");
    } catch (IOException e) {
      logger.info("ERROR: IOException writing to file.");
    }

    logger.info("============= STATS =================");
    logger.info("Mean response time: " + Util.findMeanResponseTime(latencyStats)  + " ms");
    logger.info("Median response time: " + Util.findMedianResponseTime(latencyStats)  + " ms");
    logger.info("Max response time: " + Util.findMaxResponseTime(latencyStats)  + " ms");
    logger.info("99th percentile response time: " + Util.find99percentileResponseTime(latencyStats)  + " ms");
    logger.info("Throughput: " + Util.findThroughput(totalElapsedTime, client.getTotalRequestSuccess().longValue()) + " requests/second");
    logger.info("============= DONE =================");
  }



}

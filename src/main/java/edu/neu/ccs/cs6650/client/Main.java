package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.logging.StopWatch;
import edu.neu.ccs.cs6650.model.LatencyStat;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Main {
  private static final Logger logger = LogManager.getLogger(Main.class.getName());

  private static final StopWatch sw = new StopWatch();
//  private static final Integer DAY_LENGTH_MIN = 420; // each ski day is of length 420 minutes

  private static final int MIN_NUM_LIFTS = 5;
  private static final int MAX_NUM_LIFTS = 60;

  private static final String csvPath = "/Users/qa/Documents/Github/cs6650/stats.csv";

  public static void main(String[] args) {
//    System.setProperty("java.net.preferIPv4Stack", "true");

    Integer numThreads = 64; // max = 256
    Integer numSkiers = 2000; // effectively the skier's ID | max = 50000
    Integer numSkiLifts = 40; // default 40, range 5-60
    Integer numRuns = 10; // numRuns: default 10, max 20

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
    client.run(1);
    sw.stop();

    double totalElapsedTime = sw.getElapsedTime() / 1000;
    System.out.println();
    logger.info("Number of requests sent: " + client.getTotalRequestSuccess());
    logger.info("Number of requests failed: " + client.getTotalRequestFail());
    logger.info("Total run time: " + totalElapsedTime);

    // TODO: output client's latency result to csv
    List<LatencyStat> latencyStats = client.getLatencyStats();
    try {
      Util.writeToCSV(latencyStats, csvPath);
      logger.info("Results written to CSV!\n");
    } catch (IOException e) {
      logger.info("ERROR: IOException writing to file.");
    }

    logger.info("============= STATS =================");
    logger.info("Mean response time: " + Util.findMeanResponseTime(latencyStats)  + " ms");
    logger.info("Median response time: " + Util.findMedianResponseTime(latencyStats)  + " ms");
    logger.info("Max response time: " + Util.findMaxResponseTime(latencyStats)  + " ms");
    logger.info("99th percentile response time: " + Util.find99percentileResponseTime(latencyStats)  + " ms");
    logger.info("Throughput: " + Util.findThroughput(totalElapsedTime, client.getTotalRequestSuccess().longValue()) + " request/s");
    logger.info("============= DONE =================");
  }



}

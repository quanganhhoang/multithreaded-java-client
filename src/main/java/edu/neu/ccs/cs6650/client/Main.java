package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.logging.StopWatch;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Main {
  private static final Logger logger = LogManager.getLogger(Main.class.getName());

//  private static final StopWatch sw = new StopWatch();
  private static final Integer DAY_LENGTH_MIN = 420; // each ski day is of length 420 minutes


  private static final int MIN_NUM_LIFTS = 5;
  private static final int MAX_NUM_LIFTS = 60;

  public static void main(String[] args) {
    Integer numThreads = 256;
    Integer numSkiers = 50000; // effectively the skier's ID
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

    MultithreadedClient client = new MultithreadedClient(numThreads, numSkiers, numSkiLifts, numRuns, ipAddress, port);

    client.runPhase1();
  }



}

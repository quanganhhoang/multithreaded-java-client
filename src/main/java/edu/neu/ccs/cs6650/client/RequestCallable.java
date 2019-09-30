package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.model.LatencyStat;
import edu.neu.ccs.cs6650.model.RequestType;
import edu.neu.ccs.cs6650.model.ThreadStat;
import edu.neu.ccs.cs6650.net.RestRequest;
import edu.neu.ccs.cs6650.net.RestResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@SuppressWarnings("Duplicates")
public class RequestCallable implements Callable<ThreadStat> {
  private static final Logger logger = LogManager.getLogger(RequestThread.class.getName());
  private static final boolean IS_LOCAL = true;

  private static final String API_ENDPOINT =
      IS_LOCAL ? "http://localhost:8080/skier-api/"
          : "http://ec2-54-234-184-116.compute-1.amazonaws.com:8080/cs6650";

  private int numPhase;
  private ThreadInfo info;
  private CountDownLatch countDownLatch;
  private List<LatencyStat> statList;

  public RequestCallable(int numPhase, ThreadInfo info, CountDownLatch countDownLatch) {
    this.numPhase = numPhase;
    this.info = info;
    this.countDownLatch = countDownLatch;
    this.statList = new ArrayList<>();
  }

  @Override
  public ThreadStat call() throws ExecutionException, InterruptedException {
    int rangeIdStart = info.getStartSkierId(), rangeIdEnd = info.getEndSkierId();
//    System.out.println("startId: " + rangeIdStart);
//    System.out.println("endId: " + rangeIdEnd);
//    System.out.println("numLifts: " + info.getNumLifts());

    int resortId = 1;
    int seasonId = 2019;
    int dayId = 25;

    int totalNumRequestSent = 0, totalFailures = 0;

    for (int i = 0, n = info.getNumRequest(); i < n; i++) {
      int skierId = ThreadLocalRandom.current().nextInt(rangeIdEnd - rangeIdStart) + rangeIdStart;
      int liftId = ThreadLocalRandom.current().nextInt(info.getNumLifts());
      int time = info.getStartTime() + ThreadLocalRandom.current().nextInt(info.getEndTime() - info.getStartTime());

      try {
        sendDummyRequest(resortId, seasonId, dayId, skierId, time, liftId);
        totalNumRequestSent++;
      } catch (IOException e) {
        System.out.println("ERROR: Failed to call the API");
        logger.info(e);
        totalFailures++;
      }
    }

    // finished work
    countDownLatch.countDown();
    return new ThreadStat(totalNumRequestSent, totalFailures, statList);
  }

  public void sendDummyRequest(int resortId, int seasonId, int dayId, int skierId, int time, int liftId) throws IOException {
    StringBuilder sb = new StringBuilder(API_ENDPOINT);
    sb.append("skiers/");
    sb.append(resortId);
    sb.append("/seasons/");
    sb.append(seasonId);
    sb.append("/days/");
    sb.append(dayId);
    sb.append("/skiers/");
    sb.append(skierId);
    String url = sb.toString();

    long startTime = System.currentTimeMillis();
    RestResponse response = new RestRequest(url)
      .addHeader("accept", "application/json")
      .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
      .addHeader("Host", "localhost:8080")
      .addHeader("Accept-Encoding", "gzip, deflate")
      .addHeader("Accept", "application/json")
      .addHeader("Connection", "keep-alive")
      .addHeader("Content-Type", "application/json;charset=UTF-8")
      .addField("time", String.valueOf(time))
      .addField("liftID", String.valueOf(liftId))
      .doPost();

    long endTime = System.currentTimeMillis();

    statList.add(new LatencyStat(response.statusCode(), RequestType.POST, startTime, endTime - startTime));
    String json = response == null ? "" : response.getBody();
//    System.out.println(this.numPhase + ": " + json);
  }

  public void sendPostRequest() {

  }
}
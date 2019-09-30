package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.net.RestRequest;
import edu.neu.ccs.cs6650.net.RestResponse;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class RequestThread implements Runnable {
  private static final Logger logger = LogManager.getLogger(RequestThread.class.getName());
  private static final boolean IS_LOCAL = true;

  private static final String API_ENDPOINT =
      IS_LOCAL ? "http://localhost:8080/skier-api/"
               : "http://ec2-54-234-184-116.compute-1.amazonaws.com:8080/cs6650";

  private int numPhase;
  private ThreadInfo info;
  private CountDownLatch countDownLatch;

  public RequestThread(int numPhase, ThreadInfo info, CountDownLatch countDownLatch) {
    this.numPhase = numPhase;
    this.info = info;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    int rangeIdStart = info.getStartSkierId(), rangeIdEnd = info.getEndSkierId();

    Random rand = new Random();
    int resortId = 1;
    int seasonId = 2019;
    int dayId = 25;

    for (int i = 0, n = info.getNumRequest(); i < n; i++) {
      int skierId = rand.nextInt(rangeIdEnd - rangeIdStart) + rangeIdStart;
      int liftId = rand.nextInt(info.getNumLifts());
      int time = info.getStartTime() + rand.nextInt(info.getEndTime() - info.getStartTime());

      sendDummyGetRequest(resortId, seasonId, dayId, skierId, time, liftId);
    }

    // finished work
    countDownLatch.countDown();
  }

  public void sendDummyGetRequest(int resortId, int seasonId, int dayId, int skierId, int time, int liftId) {
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

    RestResponse response = null;
    try {
      response = new RestRequest(url)
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

    } catch (IOException e) {
      logger.error("Failed to call API");
    }

    String json = response == null ? "" : response.getBody();
    System.out.println(this.numPhase + ": " + json);
  }

  public void sendPostRequest() {

  }
}
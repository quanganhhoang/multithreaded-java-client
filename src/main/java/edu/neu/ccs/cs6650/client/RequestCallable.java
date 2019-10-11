package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.model.LatencyStat;
import edu.neu.ccs.cs6650.model.RequestType;
import edu.neu.ccs.cs6650.model.ThreadInfo;
import edu.neu.ccs.cs6650.model.ThreadStat;
import edu.neu.ccs.cs6650.net.RestRequest;
import edu.neu.ccs.cs6650.net.RestResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class RequestCallable implements Callable<ThreadStat> {
  private static final Logger logger = LogManager.getLogger(RequestCallable.class.getName());

  // public DNS (IPv4)
//  private static final String EC2_ENDPOINT = "ec2-34-221-182-197.us-west-2.compute.amazonaws.com:8080";

  private String apiEndpoint;
  private ThreadInfo info;
  private CountDownLatch countDownLatch;
  private List<LatencyStat> statList;

  public RequestCallable(ThreadInfo info, CountDownLatch countDownLatch) {
    this.info = info;
    this.countDownLatch = countDownLatch;
    this.statList = new ArrayList<>();
    this.apiEndpoint = "http://" + this.info.getIpAddress() + ":" + this.info.getPort() + "/skier_api/";
  }

  @Override
  public ThreadStat call() throws ExecutionException, InterruptedException {
    int rangeIdStart = info.getStartSkierId(), rangeIdEnd = info.getEndSkierId();
//    System.out.println("startId: " + rangeIdStart);
//    System.out.println("endId: " + rangeIdEnd);
//    System.out.println("num requests: " + info.getNumRequest());

    int resortId = 1;
    int seasonId = 2019;
    int dayId = 25;

    int totalNumRequestSent = 0, totalFailures = 0;

    for (int i = 0, n = info.getNumRequest(); i < n; i++) {
      int skierId = ThreadLocalRandom.current().nextInt(rangeIdEnd - rangeIdStart) + rangeIdStart;
      int liftId = ThreadLocalRandom.current().nextInt(info.getNumLifts());
      int time = info.getStartTime() + ThreadLocalRandom.current().nextInt(info.getEndTime() - info.getStartTime());

      if (sendDummyRequest(resortId, seasonId, dayId, skierId, time, liftId)) {
        totalNumRequestSent++;
      } else {
        totalFailures++;
      }
    }

    // finished work
    countDownLatch.countDown();
    return new ThreadStat(totalNumRequestSent, totalFailures, statList);
  }

  public boolean sendDummyRequest(int resortId, int seasonId, int dayId, int skierId, int time, int liftId) {
    StringBuilder sb = new StringBuilder(this.apiEndpoint);
    sb.append("skiers/");
    sb.append(resortId);
    sb.append("/seasons/");
    sb.append(seasonId);
    sb.append("/days/");
    sb.append(dayId);
    sb.append("/skiers/");
    sb.append(skierId);
    String url = sb.toString();
//    System.out.println(url);

    long startTime = System.currentTimeMillis();

    RestResponse response = null;
    for (int i = 0; i < 3; i++) {
      try {
        response = new RestRequest(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
            .addHeader("Host", this.info.getIpAddress())
//      .addHeader("Accept-Encoding", "gzip, deflate")
            .addHeader("Accept", "application/json")
//      .addHeader("Connection", "keep-alive")
            .addHeader("Content-Type", "application/json;charset=UTF-8")
            .addField("time", String.valueOf(time))
            .addField("liftID", String.valueOf(liftId))
            .doPost();
//        logger.info("success");
        break;
      } catch (IOException e) {
//        logger.info("Failed to call API");
        logger.info(e);
        sleep(500);
      }
    }

    long endTime = System.currentTimeMillis();

    if (response != null) {
      statList.add(new LatencyStat(response.statusCode(), RequestType.POST, startTime, endTime - startTime));
      return true;
    } else {
      return false;
    }
//    String json = response == null ? "" : response.getBody();
//    System.out.println(this.numPhase + ": " + json);
  }

  public void sendPostRequest() {

  }

  public static void sleep(long time) {
    try {
      Thread.sleep(time);
    } catch(InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}
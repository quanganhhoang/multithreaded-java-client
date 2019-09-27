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
      IS_LOCAL ? "http://localhost:8080/skier-api"
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
    int rangeStart = info.getStartSkierId(), rangeEnd = info.getEndSkierId();
    int numPostRequests = info.getNumRuns() / 10 *  (rangeEnd - rangeStart);

    Random rand = new Random();

    for (int i = 0; i < numPostRequests; i++) {
//      int skierId = rand.nextInt(rangeEnd - rangeStart) + rangeStart;
//      int liftId = rand.nextInt()

      sendDummyGetRequest();
    }

    // finished work
    countDownLatch.countDown();
  }

  public void sendDummyGetRequest() {
    String url = API_ENDPOINT + "/resorts";

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
          .doGet();
    } catch (IOException e) {
      logger.error("Failed to call API");
    }

    String json = response.getBody();
    System.out.println(this.numPhase + ": " + json);
  }

  public void sendPostRequest() {

  }
}
package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.net.RestRequest;
import edu.neu.ccs.cs6650.net.RestResponse;
import java.io.IOException;
import java.util.Random;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class RequestThread implements Runnable {
  private static final Logger logger = LogManager.getLogger(RequestThread.class.getName());
  private static final boolean IS_LOCAL = false;

  private static final String API_ENDPOINT =
      IS_LOCAL ? "localhost:8080/cs6650"
               : "http://ec2-54-234-184-116.compute-1.amazonaws.com:8080/cs6650";

  ThreadInfo info;

  public RequestThread(ThreadInfo info) {
    this.info = info;
  }

  @Override
  public void run() {
    int rangeStart = info.getStartSkierId(), rangeEnd = info.getEndSkierId();
    int numPostRequests = info.getNumRuns() / 10 *  (rangeEnd - rangeStart);

    Random rand = new Random();

    for (int i = 0; i < numPostRequests; i++) {
      int skierId = rand.nextInt(rangeEnd - rangeStart) + rangeStart;
//      int liftId = rand.nextInt()

      sendGetRequest();
    }
  }

  public void sendGetRequest() {
    String url = API_ENDPOINT + "/resorts";
    RestResponse response = null;
    try {
      response = new RestRequest(API_ENDPOINT)
          .addHeader("accept", "application/json")
          .addHeader("", "")
          .doGet();
    } catch (IOException e) {
      logger.error("Failed to call API");
    }

    String json = response.getBody();
    System.out.println(json);
  }

  public void sendPostRequest() {

  }
}
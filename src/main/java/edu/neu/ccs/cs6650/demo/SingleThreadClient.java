package edu.neu.ccs.cs6650.demo;

import edu.neu.ccs.cs6650.client.RequestThread;
import edu.neu.ccs.cs6650.net.RestRequest;
import edu.neu.ccs.cs6650.net.RestResponse;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SingleThreadClient {

  private static final Logger logger = LogManager.getLogger(RequestThread.class.getName());
  private static final boolean IS_LOCAL = true;

  private static final String API_ENDPOINT =
      IS_LOCAL ? "http://localhost:8080/skier-api"
               : "http://ec2-54-234-184-116.compute-1.amazonaws.com:8080/cs6650";

  public static void main(String[] args) {
    sendGetRequest();
    sendPostRequest();
  }

  public static void sendGetRequest() {
    // http://localhost:8080/skier-api/skiers/12/seasons/2019/day/1/skier/123
    String url = API_ENDPOINT + "/resorts";
//    System.out.println(url);
    RestResponse response = null;
    try {
      response = new RestRequest(url)
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
    System.out.println(json);
  }

  public static void sendPostRequest() {
    // http://localhost:8080/resorts/{resortID}/seasons
    // { "year": 2019 }
    int resortId = 1;
    String url = API_ENDPOINT + "/resorts/" + resortId + "/seasons";

    RestResponse response = null;
    try {
      response = new RestRequest(url)
          .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
          .addHeader("Host", "localhost:8080")
          .addHeader("Accept-Encoding", "gzip, deflate")
          .addHeader("Accept", "application/json")
          .addHeader("Connection", "keep-alive")
          .addHeader("Content-Type", "application/json;charset=UTF-8")
          .addField("year", "2019")
          .doPost();

    } catch (IOException e) {
      logger.error("Failed to call API");
    }

    String json = response.getBody();
    System.out.println(json);
  }


}

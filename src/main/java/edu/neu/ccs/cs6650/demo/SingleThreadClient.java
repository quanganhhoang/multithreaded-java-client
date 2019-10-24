package edu.neu.ccs.cs6650.demo;

import edu.neu.ccs.cs6650.net.RestRequest;
import edu.neu.ccs.cs6650.net.RestResponse;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SingleThreadClient {

  private static final Logger logger = LogManager.getLogger(SingleThreadClient.class.getName());
  private static final String LOCAL_HOST = "localhost:8080";

  private static final String EC2_ENDPOINT = "ec2-34-221-182-197.us-west-2.compute.amazonaws.com:8080";
//  private static final String EC2_ENDPOINT = "52.12.97.44:8080";
  private static final boolean IS_LOCAL = false;

  private static final String API_ENDPOINT = "http://" + (IS_LOCAL ? LOCAL_HOST : EC2_ENDPOINT) + "/skier_api";

  public static void main(String[] args) {
    sendGetRequest();
//    addNewSeason();
//    addNewRide();
  }

  public static void sendGetRequest() {
//    http://ec2-34-221-182-197.us-west-2.compute.amazonaws.com:8080/skier_api/resorts
    String url = API_ENDPOINT + "/resorts";
    System.out.println(url);
    RestResponse response = null;
    try {
      response = new RestRequest(url)
//          .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
//          .addHeader("Host", IS_LOCAL ? LOCAL_HOST : EC2_ENDPOINT)
//          .addHeader("Accept-Encoding", "gzip, deflate")
          .addHeader("Accept", "application/json")
//          .addHeader("Connection", "keep-alive")
          .addHeader("Content-Type", "application/json;charset=UTF-8")
          .doGet();
    } catch (IOException e) {
      logger.error("Failed to call API");
    }

    String json = response.getBody();
    System.out.println(json);
  }

  public static void addNewSeason() {
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

  public static void addNewRide() {

    // http://localhost:8080/skier-api/skiers/{resortId}/seasons/{season}/days/{dayId}/skiers/{skierId}
    String url = API_ENDPOINT + "/skiers/1/seasons/2019/days/4/skiers/1";
    System.out.println(url);
    RestResponse response = null;
    try {
      response = new RestRequest(url)
          .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
          .addHeader("Host", "localhost:8080")
          .addHeader("Accept-Encoding", "gzip, deflate")
          .addHeader("Accept", "application/json")
          .addHeader("Connection", "keep-alive")
          .addHeader("Content-Type", "application/json;charset=UTF-8")
          .addField("time", "300")
          .addField("liftID", "5")
          .doPost();

    } catch (IOException e) {
      logger.error("Failed to call API");
    }

    String json = response.getBody();
    System.out.println(json);
  }


}

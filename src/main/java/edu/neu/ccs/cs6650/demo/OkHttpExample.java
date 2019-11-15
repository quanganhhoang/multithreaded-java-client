package edu.neu.ccs.cs6650.demo;

import java.sql.SQLException;
import okhttp3.*;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OkHttpExample {
  private static final Logger logger = LogManager.getLogger(OkHttpExample.class.getName());
  private static final String LOCAL_HOST = "localhost:8080";
//  private static final String EC2_ENDPOINT = "ec2-54-191-195-103.us-west-2.compute.amazonaws.com";
  private static final String EC2_ENDPOINT = "cs6650-loadbalancer-1302090979.us-west-2.elb.amazonaws.com";

  private static final boolean IS_LOCAL = false;

  private static final String API_ENDPOINT = "http://" + (IS_LOCAL ? LOCAL_HOST : EC2_ENDPOINT) + "/skier-api";

  // one instance, reuse
  private final OkHttpClient httpClient = new OkHttpClient.Builder().eventListener(new PrintingEventListener()).build();

  public static void main(String[] args) throws Exception {

    OkHttpExample obj = new OkHttpExample();

    System.out.println("Testing 1 - Send Http GET request");
    obj.sendGet();
//    obj.sendGetSkier();
//    obj.addNewLiftRide();

//    System.out.println("Testing 2 - Send Http POST request");
//    obj.sendPost();
  }

  private void sendGet() throws Exception {
    String url = API_ENDPOINT + "/resorts";
    System.out.println(url);
    Request request = new Request.Builder()
        .url(url)
//        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
        .addHeader("Host", IS_LOCAL ? LOCAL_HOST : EC2_ENDPOINT)
//        .addHeader("Accept-Encoding", "gzip, deflate")
        .addHeader("Accept", "application/json")
//        .addHeader("Connection", "keep-alive")
        .addHeader("Content-Type", "application/json;charset=UTF-8")
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
      // Get response body
      System.out.println(response.body().string());
    }
  }

  private void sendGetSkier() throws Exception {
    StringBuilder url = new StringBuilder(API_ENDPOINT)
        .append("/skiers/")
        .append(1)
        .append("/seasons/")
        .append(2019)
        .append("/days/")
        .append(1)
        .append("/skiers/")
        .append(1);
    logger.info("Url: " + url.toString());
    Request request = new Request.Builder()
        .url(url.toString())
//        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
        .addHeader("Host", IS_LOCAL ? LOCAL_HOST : EC2_ENDPOINT)
        .addHeader("Accept-Encoding", "gzip, deflate")
        .addHeader("Accept", "application/json")
//        .addHeader("Connection", "keep-alive")
        .addHeader("Content-Type", "application/json;charset=UTF-8")
        .build();

    try (Response response = httpClient.newCall(request).execute()) {

      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
      // Get response body
      System.out.println(response.body().string());
    }
  }

  private void addNewLiftRide() throws Exception {
    StringBuilder sb = new StringBuilder(API_ENDPOINT)
        .append("/skiers/")
        .append(2)
        .append("/seasons/")
        .append(2019)
        .append("/days/")
        .append(1)
        .append("/skiers/")
        .append(40);
    String url = sb.toString();

    String json = "{\"time\":611,\"liftID\":\"30\"}";

    RequestBody requestBody = RequestBody.create(json,
        MediaType.parse("application/json; charset=utf-8"));

    Request request = new Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Host", IS_LOCAL ? LOCAL_HOST : EC2_ENDPOINT)
//        .addHeader("Host", this.info.getIpAddress())
//        .addHeader("Accept-Encoding", "gzip, deflate")
        .addHeader("Accept", "application/json")
//        .addHeader("Connection", "keep-alive")
        .addHeader("Content-Type", "application/json;charset=UTF-8")
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      } else {
        System.out.println(response.body().string());
      }
    } catch (IOException e) {
      logger.info(e);
    }
  }
}
package edu.neu.ccs.cs6650.demo;

import okhttp3.*;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OkHttpExample {

  private static final Logger logger = LogManager.getLogger(SingleThreadClient.class.getName());
  private static final String LOCAL_HOST = "localhost:8080";

  private static final String EC2_ENDPOINT = "ec2-34-221-182-197.us-west-2.compute.amazonaws.com:8080";
  //  private static final String EC2_ENDPOINT = "52.12.97.44:8080";
  private static final boolean IS_LOCAL = true;

  private static final String API_ENDPOINT = "http://" + (IS_LOCAL ? LOCAL_HOST : EC2_ENDPOINT) + "/skier-api";

  // one instance, reuse
  private final OkHttpClient httpClient = new OkHttpClient.Builder().eventListener(new PrintingEventListener()).build();

  public static void main(String[] args) throws Exception {

    OkHttpExample obj = new OkHttpExample();

    System.out.println("Testing 1 - Send Http GET request");
    obj.sendGet();

//    System.out.println("Testing 2 - Send Http POST request");
//    obj.sendPost();

  }

  private void sendGet() throws Exception {
    String url = API_ENDPOINT + "/resorts";
    System.out.println(url);
    Request request = new Request.Builder()
        .url(url)
        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
        .addHeader("Host", IS_LOCAL ? LOCAL_HOST : EC2_ENDPOINT)
        .addHeader("Accept-Encoding", "gzip, deflate")
        .addHeader("Accept", "application/json")
//        .addHeader("Connection", "keep-alive")
        .addHeader("Content-Type", "application/json;charset=UTF-8")
        .build();

    try (Response response = httpClient.newCall(request).execute()) {

      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//      response.body().source().readByteString();
      // Get response body
      System.out.println(response.body().string());
    }

  }

  private void sendPost() throws Exception {

    // form parameters
    RequestBody formBody = new FormBody.Builder()
        .add("username", "abc")
        .add("password", "123")
        .add("custom", "secret")
        .build();

    Request request = new Request.Builder()
        .url("https://httpbin.org/post")
        .addHeader("User-Agent", "OkHttp Bot")
        .post(formBody)
        .build();

    try (Response response = httpClient.newCall(request).execute()) {

      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

      // Get response body
      System.out.println(response.body().string());
    }

  }

}
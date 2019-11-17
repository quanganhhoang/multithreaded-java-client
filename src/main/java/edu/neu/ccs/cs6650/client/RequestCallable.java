package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.model.LatencyStat;
import edu.neu.ccs.cs6650.model.RequestType;
import edu.neu.ccs.cs6650.model.ThreadInfo;
import edu.neu.ccs.cs6650.model.ThreadStat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class RequestCallable implements Callable<ThreadStat> {
//  private static final Logger logger = LogManager.getLogger(RequestCallable.class.getName());
  private String apiEndpoint;
  private ThreadInfo info;
  private CountDownLatch countDownLatch;
  private List<LatencyStat> statList;

  private final OkHttpClient httpClient = new OkHttpClient.Builder()
      .connectTimeout(Config.CONNECT_TIMEOUT, TimeUnit.SECONDS)
      .readTimeout(Config.READ_TIMEOUT, TimeUnit.SECONDS)
//      .retryOnConnectionFailure(false)
      .build();

  public RequestCallable(ThreadInfo info, CountDownLatch countDownLatch) {
    this.info = info;
    this.countDownLatch = countDownLatch;
    this.statList = new ArrayList<>();
    this.apiEndpoint = (Config.IS_AWS ? "http://" : "https://")
        + this.info.getIpAddress()
        + (Config.IS_AWS ? ":" + this.info.getPort() + "/skier-api/" : "/");
  }

  @Override
  public ThreadStat call() {
    int rangeIdStart = info.getStartSkierId(), rangeIdEnd = info.getEndSkierId();

    int resortId = 1;
    int seasonId = 2019;
    int dayId = ThreadLocalRandom.current().nextInt(365);

    int totalNumRequestSent = 0, totalFailures = 0;

    for (int i = 0, n = info.getNumRequest(); i < n; i++) {
      int skierId = ThreadLocalRandom.current().nextInt(rangeIdEnd - rangeIdStart) + rangeIdStart;
      int liftId = ThreadLocalRandom.current().nextInt(info.getNumLifts());
      int time = info.getStartTime() + ThreadLocalRandom.current().nextInt(info.getEndTime() - info.getStartTime());

      String url = buildSqlStmt(resortId, seasonId, dayId, skierId);
      if (sendPostApache(url, time, liftId)) {
        totalNumRequestSent++;
      } else {
        totalFailures++;
      }

      // only applies to phase 3
      if (this.info.getPhase() == 3) {
        if (sendGetApache(url)) {
          totalNumRequestSent++;
        } else {
          totalFailures++;
        }
      }
    }

    // finished work
    countDownLatch.countDown();
    return new ThreadStat(totalNumRequestSent, totalFailures, statList);
  }

  private boolean sendPostRequest(String url, int time, int liftId) {
    String json = "{\"time\":" + time + ",\"liftID\":\"" + liftId + "\"}";

    RequestBody requestBody = RequestBody.create(json,
        MediaType.parse("application/json; charset=utf-8"));

    Request request = new Request.Builder()
        .url(url)
        .post(requestBody)
//        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
//        .addHeader("Host", this.info.getIpAddress())
//        .addHeader("Accept-Encoding", "gzip, deflate")
        .addHeader("Accept", "application/json")
        .addHeader("Connection", "keep-alive")
//        .addHeader("Content-Type", "application/json;charset=UTF-8")
        .build();

    long startTime = System.currentTimeMillis();
    try (Response response = httpClient.newCall(request).execute()) {
//      if (!response.isSuccessful()) {
//        throw new IOException("Unexpected code " + response);
//      } else {
//
//      }
      statList.add(new LatencyStat(response.code(), RequestType.POST, startTime, System.currentTimeMillis() - startTime));
      return true;
      // Get response body
//      System.out.println(response.body().string());
    } catch (IOException e) {
//      logger.info(e);
      return false;
    }
  }

  private boolean sendPostApache(String url, int time, int liftId) {
    HttpPost post = new HttpPost(url);
    post.addHeader("Accept", "application/json");
    String json = "{\"time\":" + time + ",\"liftID\":\"" + liftId + "\"}";

    StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
    post.setEntity(requestEntity);

    long startTime = System.currentTimeMillis();
    try (CloseableHttpClient httpClient = HttpClients.createDefault();
         CloseableHttpResponse response = httpClient.execute(post)) {

      statList.add(new LatencyStat(response.getStatusLine().getStatusCode(), RequestType.POST, startTime, System.currentTimeMillis() - startTime));
      return true;
    } catch (IOException e) {
      System.out.println(e);
      return false;
    }
  }

  private boolean sendGetApache(String url) {
    HttpGet request = new HttpGet(url);

    long startTime = System.currentTimeMillis();
    try (CloseableHttpClient httpClient = HttpClients.createDefault();
         CloseableHttpResponse response = httpClient.execute(request)) {

      statList.add(new LatencyStat(response.getStatusLine().getStatusCode(), RequestType.GET, startTime, System.currentTimeMillis() - startTime));
      return true;
    } catch (IOException e) {
      System.out.println(e);
      return false;
    }

  }

  private boolean sendGetRequest(String url) {
    Request request = new Request.Builder()
        .url(url)
//        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
//        .addHeader("Host", this.info.getIpAddress())
//        .addHeader("Accept-Encoding", "gzip, deflate")
//        .addHeader("Accept", "application/json")
//        .addHeader("Connection", "keep-alive")
//        .addHeader("Content-Type", "application/json;charset=UTF-8")
        .build();

    long startTime = System.currentTimeMillis();
    try (Response response = httpClient.newCall(request).execute()) {
      statList.add(new LatencyStat(response.code(), RequestType.GET, startTime, System.currentTimeMillis() - startTime));
      return true;
    } catch (IOException e) {
//      logger.info(e);
      return false;
    }
  }

  private String buildSqlStmt(int resortId, int seasonId, int dayId, int skierId) {
    StringBuilder sb = new StringBuilder(this.apiEndpoint)
        .append("skiers/")
        .append(resortId)
        .append("/seasons/")
        .append(seasonId)
        .append("/days/")
        .append(dayId)
        .append("/skiers/")
        .append(skierId);
    return sb.toString();
  }

//  public static void sleep(long time) {
//    try {
//      Thread.sleep(time);
//    } catch(InterruptedException ex) {
//      Thread.currentThread().interrupt();
//    }
//  }
}
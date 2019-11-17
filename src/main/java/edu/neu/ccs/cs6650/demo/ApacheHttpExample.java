package edu.neu.ccs.cs6650.demo;

import edu.neu.ccs.cs6650.model.LatencyStat;
import edu.neu.ccs.cs6650.model.RequestType;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ApacheHttpExample {
  private static final String API_ENDPOINT = "http://cs6650-loadbalancer-1302090979.us-west-2.elb.amazonaws.com/skier-api/";

  public static void main(String[] args) {
      String url = buildSqlStmt(1, 2019, 1, 1);

      sendPostApache(url, 19, 20);
      sendGetApache(url);
  }

  private static String buildSqlStmt(int resortId, int seasonId, int dayId, int skierId) {
    StringBuilder sb = new StringBuilder(API_ENDPOINT)
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

  private static boolean sendPostApache(String url, int time, int liftId) {
    HttpPost post = new HttpPost(url);
    post.addHeader("Accept", "application/json");
    post.addHeader("Host", "cs6650-loadbalancer-1302090979.us-west-2.elb.amazonaws.com");
    String json = "{\"time\":" + time + ",\"liftID\":\"" + liftId + "\"}";

    StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
    post.setEntity(requestEntity);

    System.out.println(post.getURI());
    try (CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(post)) {

      System.out.println(response.getStatusLine().getReasonPhrase());
      System.out.println(response.getStatusLine().getStatusCode());
      return true;
    } catch (IOException e) {
      System.out.println(e);
      return false;
    }
  }

  private static boolean sendGetApache(String url) {
    HttpGet request = new HttpGet(url);

    try (CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request)) {
      System.out.println(response.getStatusLine().getReasonPhrase());
      System.out.println(response.getStatusLine().getStatusCode());
      return true;
    } catch (IOException e) {
      System.out.println(e);
      return false;
    }

  }
}

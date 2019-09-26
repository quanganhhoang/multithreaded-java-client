package edu.neu.ccs.cs6650.net;


import java.nio.charset.StandardCharsets;
import java.util.*;

public class RestResponse {

  private RestRequest request;
  private int statusCode;
  private String statusMessage;
  private Map<String, List<String>> headers;
  private byte[] body;

  public RestResponse(RestRequest request, int statusCode, String statusMessage, Map<String, List<String>> headers, byte[] body) {
    this.request = request;
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.headers = headers;
    this.body = body;
  }

  public int statusCode() {
    return statusCode;
  }

  public String statusMessage() {
    return statusMessage;
  }

  public Map<String, List<String>> headers() {
    return Collections.unmodifiableMap(headers);
  }

  public String getBody() {
    String str = null;
    try {
      str = new String(body, StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return str;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(0)
        .append(statusCode).append(' ').append(statusMessage)
        .append(" [").append(request.getAddress()).append("]\n\n");

    for (String key : headers.keySet()) {
      if (key != null) {
        buf.append(String.format("%s: %s\n", key, headers.get(key)));
      }
    }
    buf.append('\n').append(body);
    return buf.toString();
  }

}

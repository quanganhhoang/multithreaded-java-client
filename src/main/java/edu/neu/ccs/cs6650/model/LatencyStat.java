package edu.neu.ccs.cs6650.model;

public class LatencyStat {
  private int responseCode;
  private RequestType requestType;
  private long startTime;
  private long latency;

  public LatencyStat(int responseCode, RequestType requestType, long startTime, long latency) {
    this.responseCode = responseCode;
    this.requestType = requestType;
    this.startTime = startTime;
    this.latency = latency;
  }

  public RequestType getRequestType() {
    return requestType;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getLatency() {
    return latency;
  }

  @Override
  public String toString() {
    return startTime + "," + requestType.toString() + "," + latency + "," + responseCode;
  }
}

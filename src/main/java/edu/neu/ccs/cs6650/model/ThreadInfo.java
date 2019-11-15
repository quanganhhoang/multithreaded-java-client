package edu.neu.ccs.cs6650.model;

public class ThreadInfo {
  private String ipAddress;
  private String port;
  private Integer startSkierId;
  private Integer endSkierId;
  private Integer startTime;
  private Integer endTime;
  private Integer numRuns;
  private Integer numLifts;
  private Integer numRequest;
  private Integer phase;

  public ThreadInfo(String ipAddress, String port, Integer startSkierId, Integer endSkierId,
      Integer startTime, Integer endTime, Integer numRuns, Integer numLifts, Integer numRequest, Integer phase) {
    this.ipAddress = ipAddress;
    this.port = port;
    this.startSkierId = startSkierId;
    this.endSkierId = endSkierId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numRuns = numRuns;
    this.numLifts = numLifts;
    this.numRequest = numRequest;
    this.phase = phase;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getPort() {
    return port;
  }

  public Integer getStartSkierId() {
    return startSkierId;
  }

  public Integer getEndSkierId() {
    return endSkierId;
  }

  public Integer getStartTime() {
    return startTime;
  }

  public Integer getEndTime() {
    return endTime;
  }

  public Integer getNumRuns() {
    return numRuns;
  }

  public Integer getNumLifts() {
    return numLifts;
  }

  public Integer getNumRequest() {
    return numRequest;
  }

  public Integer getPhase() {
    return phase;
  }
}

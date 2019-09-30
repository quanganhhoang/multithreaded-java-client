package edu.neu.ccs.cs6650.model;

import java.util.List;

public class ThreadStat {
  private int numRequestSuccess;
  private int numRequestFail;
  private List<LatencyStat> statList;

  public ThreadStat(int numRequestSuccess, int numRequestFail, List<LatencyStat> statList) {
    this.numRequestSuccess = numRequestSuccess;
    this.numRequestFail = numRequestFail;
    this.statList = statList;
  }

  public int getNumRequestSuccess() {
    return numRequestSuccess;
  }

  public int getNumRequestFail() {
    return numRequestFail;
  }

  public List<LatencyStat> getStatList() {
    return statList;
  }
}

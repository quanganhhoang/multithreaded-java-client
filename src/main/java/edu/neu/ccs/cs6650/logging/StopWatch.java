package edu.neu.ccs.cs6650.logging;

import java.util.*;
//import org.json.*;

public class StopWatch {
  private double startTime;       // Time when stopwatch was started or reset
  private double splitTime;       // Time when the last split was recorded
  private Map<String, Timing> namedSplits;     // Named times and intervals

  public StopWatch() {
    start();
  }

  public void reset() {
    start();
  }

  public void start() {
    namedSplits = new LinkedHashMap<>();   // LinkedHashMap keeps items in the order in which they were inserted
    startTime = now();
    splitTime = startTime;
    namedSplits.put("Started", timing());
  }

  public void stop() {
    namedSplits.put("Stopped", timing());
  }

  public double getElapsedTime() {
    return now() - startTime;
  }

  public double getSplitTime() {
    double t = now() - splitTime;
    splitTime = now();
    return t;
  }

  public void addNamedSplit(String splitName) {
    namedSplits.put(splitName, timing());
    splitTime = now();
  }

  public double getNamedSplitTime(String splitName) {
    return namedSplits.get(splitName).time;
  }

  public double getNamedSplitInterval(String splitName) {
    return namedSplits.get(splitName).interval;
  }

  @Override
  public String toString() {
    String s = "";

    for (String key : namedSplits.keySet()) {
      Timing timing = namedSplits.get(key);
      s += String.format("%20s: %dms\t(%dms)\n", key, (long)timing.getInterval(), (long)timing.getTime());
    }

    return s;
  }

  public double getStartTime() {
    return startTime;
  }

  //  public String toJson() {
//    JSONObject jo = new JSONObject(namedSplits);
//    return jo.toString();
//  }
//
//  public String toJson(int indent) {
//    JSONObject jo = new JSONObject(namedSplits);
//    return jo.toString(indent);
//  }

  // -------------------------------------------------------------------------

  private double now() {
//    double now = System.nanoTime() / 1e6;       // milliseconds
    double now = System.currentTimeMillis();    // CA: alternate approach - unclear which is better

    return now;
  }

  private Timing timing() {
    double now = now();
    return new Timing(now-startTime, now-splitTime);
  }


  // =========================================================================

  public class Timing {

    private final double time;
    private final double interval;

    Timing(double time, double interval) {
      this.time = time;
      this.interval = interval;
    }

    public double getTime() {
      return time;
    }

    public double getInterval() {
      return interval;
    }

    @Override
    public String toString() {
      return "(" + time + "," + interval + ")";
    }

  }

}

package edu.neu.ccs.cs6650.client;

import edu.neu.ccs.cs6650.model.LatencyStat;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.DoubleStream;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;


public class Util {

  public static void writeToCSV(List<LatencyStat> latencyStats, String filePath) throws IOException {
    File outputFile = new File(filePath);
    try (PrintWriter pw = new PrintWriter(outputFile)) {
      pw.println("start_time,request_type, latency, response_code");
      latencyStats.stream()
          .map(LatencyStat::toString)
          .forEach(pw::println);
    }
  }

  public static double findMeanResponseTime(List<LatencyStat> latencyStats) {
    return latencyStats.stream().mapToDouble(LatencyStat::getLatency).average().orElse(0d);
  }

  public static double findMedianResponseTime(List<LatencyStat> latencyStats) {
    // DoubleStream skip(long n) returns a stream consisting of the remaining elements
    // of this stream after discarding the first n elements of the stream.
    // If this stream contains fewer than n elements then an empty stream will be returned.
    DoubleStream sortedLatency = latencyStats.stream().mapToDouble(LatencyStat::getLatency).sorted();
    return latencyStats.size() % 2 == 0 ?
        sortedLatency.skip(latencyStats.size()/2 - 1).limit(2).average().getAsDouble():
        sortedLatency.skip(latencyStats.size()/2).findFirst().getAsDouble();
  }

  public static double findThroughput(double totalTime, long numRequests) {
    return numRequests / totalTime;
  }

  public static double findMaxResponseTime(List<LatencyStat> latencyStats) {
    return latencyStats.stream().mapToDouble(LatencyStat::getLatency).max().orElse(0d);
  }

  public static double find99percentileResponseTime(List<LatencyStat> latencyStats) {
//    public static long[] percentiles(long[] numbers, double... percentiles) {
//      Arrays.sort(numbers, 0, numbers.length);
//      long[] values = new long[percentiles.length];
//      for (int i = 0; i < percentiles.length; i++) {
//        int index = (int) (percentiles[i] * numbers.length);
//        values[i] = numbers[index];
//      }
//      return values;
//    }

    Percentile p99 = new Percentile();
    DoubleStream sortedLatency = latencyStats.stream().mapToDouble(LatencyStat::getLatency).sorted();

    return p99.evaluate(sortedLatency.toArray(), 99);

//    double[] sortedLatency = latencyStats.stream().mapToDouble(LatencyStat::getLatency).sorted().toArray();
//    return sortedLatency[sortedLatency.length * 99 / 100];
  }
}

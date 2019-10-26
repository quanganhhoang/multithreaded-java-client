package edu.neu.ccs.cs6650.demo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public final class PrintingEventListener extends EventListener {
  private long callStartNanos;

  private void printEvent(String name) {
    long nowNanos = System.nanoTime();
    if (name.equals("callStart")) {
      callStartNanos = nowNanos;
    }
    long elapsedNanos = nowNanos - callStartNanos;
    System.out.printf("%.3fms %s%n", elapsedNanos / 1000000d, name);
  }

  @Override public void callStart(@NotNull Call call) {
    printEvent("callStart");
  }

  @Override public void proxySelectStart(@NotNull Call call, HttpUrl url) {
    printEvent("proxySelectStart");
  }

  @Override public void proxySelectEnd(Call call, HttpUrl url, List<Proxy> proxies) {
    printEvent("proxySelectEnd");
  }

  @Override public void dnsStart(Call call, String domainName) {
    printEvent("dnsStart");
  }

  @Override public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
    printEvent("dnsEnd");
  }

  @Override public void connectStart(
      Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
    printEvent("connectStart");
  }

  @Override public void secureConnectStart(Call call) {
    printEvent("secureConnectStart");
  }

  @Override public void secureConnectEnd(Call call, Handshake handshake) {
    printEvent("secureConnectEnd");
  }

  @Override public void connectEnd(
      Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
    printEvent("connectEnd");
  }

  @Override public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy,
      Protocol protocol, IOException ioe) {
    printEvent("connectFailed");
  }

  @Override public void connectionAcquired(Call call, Connection connection) {
    printEvent("connectionAcquired");
  }

  @Override public void connectionReleased(Call call, Connection connection) {
    printEvent("connectionReleased");
  }

  @Override public void requestHeadersStart(Call call) {
    printEvent("requestHeadersStart");
  }

  @Override public void requestHeadersEnd(Call call, Request request) {
    printEvent("requestHeadersEnd");
  }

  @Override public void requestBodyStart(Call call) {
    printEvent("requestBodyStart");
  }

  @Override public void requestBodyEnd(Call call, long byteCount) {
    printEvent("requestBodyEnd");
  }

  @Override public void requestFailed(Call call, IOException ioe) {
    printEvent("requestFailed");
  }

  @Override public void responseHeadersStart(Call call) {
    printEvent("responseHeadersStart");
  }

  @Override public void responseHeadersEnd(Call call, Response response) {
    printEvent("responseHeadersEnd");
  }

  @Override public void responseBodyStart(Call call) {
    printEvent("responseBodyStart");
  }

  @Override public void responseBodyEnd(Call call, long byteCount) {
    printEvent("responseBodyEnd");
  }

  @Override public void responseFailed(Call call, IOException ioe) {
    printEvent("responseFailed");
  }

  @Override public void callEnd(Call call) {
    printEvent("callEnd");
  }

  @Override public void callFailed(Call call, IOException ioe) {
    printEvent("callFailed");
  }
}

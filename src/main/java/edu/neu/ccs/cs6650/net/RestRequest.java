package edu.neu.ccs.cs6650.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * RestRequest - gets data from RESTful services (JSON, etc.)
 *
 * Example of use:
 *      RestResponse response = new RestRequest("https://data.seattle.gov/resource/33kz-ixgy.json")
 *          .addHeader("accept", "application/json")
 *          .addHeader("", "")
 *          .doGet();
 *
 *      String json = response.getBody();
 *
 */

public class RestRequest {

  private static final int CONNECTION_TIMEOUT_DEFAULT = 1000; // ms
  private static final int READ_TIMEOUT_DEFAULT = 10000; // ms

  private String address;
  private Map<String, String> headers;
  private String body;
  private String authorization;
  private int connectionTimeout;
  private int readTimeout;

  public RestRequest(String address) {
    this.address = address;
    this.headers = new HashMap<>();
    this.body = "";
    this.authorization = "";
    this.connectionTimeout = CONNECTION_TIMEOUT_DEFAULT;
    this.readTimeout = READ_TIMEOUT_DEFAULT;
  }

  @Override
  public String toString() {
    String s = address;
    if (body.length() > 0) s += "?" + body;
    if (headers.size() > 0) s += " | " + headers.toString();
    return s;
  }

  // Getters
  public String getAddress() {
    return address;
  }

  public String getBody() {
    return body;
  }

  public String getFields() {
    return body;
  }

  public Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  public String getHeader(String name) {
    return headers.get(name);
  }

  // Setters

  public RestRequest addField(String field, String value) {
    if (body.length() > 0) body += "&";
    body += field + "=" + value;
    return this;
  }


  public RestRequest addHeader(String header, String value) {
    headers.put(header, value);
    return this;
  }

  public RestRequest addAuthorization(String authorization) {
    this.authorization = authorization;
    return this;
  }

  public RestRequest setConnectionTimeout(int timeout) {
    connectionTimeout = timeout;
    return this;
  }

  public RestRequest setReadTimeout(int timeout) {
    readTimeout = timeout;
    return this;
  }

  // =========================================================

  public RestResponse doGet() throws IOException {
    return send("GET", address+"?"+body, connectionTimeout, readTimeout);
  }

  public RestResponse doPost() throws IOException {
    return send("POST", address, connectionTimeout, readTimeout);
  }

  public RestResponse doPut() throws IOException {
    return send("PUT", address, connectionTimeout, readTimeout);
  }

  public RestResponse doDelete() throws IOException {
    return send("DELETE", address, connectionTimeout, readTimeout);
  }

  // =========================================================

  private RestResponse send(String method, String address, int connectionTimeout, int readTimeout) throws IOException {
    RestResponse response;

    URL url = new URL(address);

    HttpURLConnection conn = null;

    try {
      conn = (HttpURLConnection)url.openConnection();
      conn.setConnectTimeout(connectionTimeout);
      conn.setReadTimeout(readTimeout);
      conn.setRequestMethod(method);
      conn.setUseCaches(false);
      conn.setInstanceFollowRedirects(false);

      // ensure we have a set user-agent
      ensureUserAgent();

      // Sets the general request property. If a property with the key already exists, overwrite its value with the new value.
      for (String name : headers.keySet()) {
        conn.addRequestProperty(name, headers.get(name));
      }

      if (authorization != null && !authorization.isEmpty()) {
        conn.setRequestProperty("Authorization", authorization);
      }

      if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
        // A URL connection can be used for input and/or output.
        // Set the DoOutput flag to true if you intend to use the URL connection for output, false if not. The default is false.
        conn.setDoOutput(true);
        try (OutputStream output = conn.getOutputStream()) {
          output.write(body.getBytes());
        }
      }

      // if getting an error code >= 400
      InputStream inStream = (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) ? conn.getErrorStream() : conn.getInputStream();

      // if we have received a compressed response, decompress it
      String encoding = conn.getContentEncoding();
      if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
        inStream = new GZIPInputStream(inStream);
      }

      byte[] responseBytes = readFully(inStream);

      response = new RestResponse(this,
          conn.getResponseCode(),
          conn.getResponseMessage(),
          conn.getHeaderFields(),
          responseBytes
      );

    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return response;
  }

  private void ensureUserAgent() {
    for (String key : headers.keySet()) {
      if (key.equalsIgnoreCase("user-agent")) {
        return;
      }
    }

    addHeader("User-Agent", "");
  }

  private static byte[] readFully(InputStream input) throws IOException {
    // data is written into a byte array. The buffer automatically grows as data is written to it.
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    // read 1 kb at a time
    byte[] buffer = new byte[8 * 1024];         // a buffer to store the bytes from each read operation
    int nBytes;                                 // number of bytes read in one read operation
    while ((nBytes = input.read(buffer)) != -1) {
      // Writes nBytes from the buffer starting from beginning of buffer
      output.write(buffer, 0, nBytes);
    }
    return output.toByteArray();
  }

}

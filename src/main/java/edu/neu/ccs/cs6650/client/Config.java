package edu.neu.ccs.cs6650.client;

public class Config {
  static final boolean IS_LOCAL = false;
  static final boolean IS_AWS = false;
  static final boolean IS_LOAD_BALANCED = true;
  static final int MAX_THREAD_POOL = 320;

  static final int CONNECT_TIMEOUT = 10;
  static final int READ_TIMEOUT = 5;
}

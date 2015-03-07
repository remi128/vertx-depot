package io.vertx.example.util;

import com.baldmountain.depot.DepotVerticle;


/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Runner {

  private static final String DEPOT_JAVA_DIR = "src/main/java/";

  public static void runClusteredExample(Class<DepotVerticle> clazz) {
    ExampleRunner.runJavaExample(DEPOT_JAVA_DIR, clazz, true);
  }

  public static void runExample(Class<DepotVerticle> clazz) {
    ExampleRunner.runJavaExample(DEPOT_JAVA_DIR, clazz, false);
  }


}

package com.baldmountain.depot;

import com.baldmountain.depot.models.Product;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.math.BigDecimal;

/**
 * Created by gclements on 3/8/15.
 */
public class DbSeed {
    static boolean done = false;

    static void setDone() {
        done = true;
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        JsonObject config = new JsonObject().put("db_name", "depot_development");
        MongoService mongoService = MongoService.create(vertx, config);
        mongoService.start();

        mongoService.dropCollection("products", event1 -> {
            if (event1.succeeded()) {
                new Product("Herland", "A feminist novel", "", new BigDecimal(2.35))
                        .save(mongoService, event2 -> {
                            if (event2.succeeded()) {
                                System.out.println("All set: " + event2.result());
                            } else {
                                System.out.println(event2.cause().getMessage());
                            }
                            mongoService.stop();
                            synchronized (config) {
                                setDone();
                                config.notify();
                            }
                        });
            } else {
                System.out.println("Trouble dropping products");
                synchronized (config) {
                    setDone();
                    config.notify();
                }
            }
        });

        try {
            synchronized (config) {
                while (!done)
                    config.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        vertx.close();
    }
}

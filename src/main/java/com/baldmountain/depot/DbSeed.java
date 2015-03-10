package com.baldmountain.depot;

import com.baldmountain.depot.models.Product;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gclements on 3/8/15.
 *
 */
public class DbSeed {
    static boolean done = false;
    static Object lock = new Object();
    static MongoService mongoService;

    static void setDone(String msg) {
        System.out.println(msg);
        mongoService.stop();
        done = true;
        synchronized (lock) {
            lock.notify();
        }
    }

    static void saveAProduct(List<Product> products) {
        if (!products.isEmpty()) {
            Product product = products.remove(0);
            product.save(mongoService, res -> {
                if (res.succeeded()) {
                    saveAProduct(products);
                } else {
                    setDone(res.cause().getMessage());
                }
            });
        } else {
            setDone("Finished!");
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        JsonObject config = new JsonObject().put("db_name", "depot_development");

        mongoService = MongoService.create(vertx, config);
        mongoService.start();

        mongoService.dropCollection("products", event1 -> {
            if (event1.succeeded()) {
                LinkedList<Product> products = new LinkedList<>();
                products.add(new Product("CoffeeScript", "<p>\n" +
                        "        CoffeeScript is JavaScript done right. It provides all of JavaScript's " +
                        "functionality wrapped in a cleaner, more succinct syntax. In the first " +
                        "book on this exciting new language, CoffeeScript guru Trevor Burnham " +
                        "shows you how to hold onto all the power and flexibility of JavaScript " +
                        "while writing clearer, cleaner, and safer code.</p>", "/images/cs.jpg", new BigDecimal(36.00)));
                products.add(new Product("Programming Ruby 1.9 & 2.0", "<p>Ruby is the fastest growing and most exciting dynamic language " +
                        "out there. If you need to get working programs delivered fast, " +
                        "you should add Ruby to your toolbox. </p>", "/images/ruby.jpg", new BigDecimal(49.95)));
                products.add(new Product("Rails Test Prescriptions", "<p><em>Rails Test Prescriptions</em> is a comprehensive guide to testing " +
                        "Rails applications, covering Test-Driven Development from both a theoretical perspective (why to test) and from a practical perspective " +
                        "(how to test effectively). It covers the core Rails testing tools and procedures for Rails 2 and Rails 3, and introduces popular add-ons, " +
                        "including Cucumber, Shoulda, Machinist, Mocha, and Rcov.</p>", "/images/rtp.jpg", new BigDecimal(34.95)));
                saveAProduct(products);
            } else {
                setDone("Trouble dropping products");
            }
        });

        try {
            synchronized (lock) {
                while (!done)
                    lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        vertx.close();
    }
}

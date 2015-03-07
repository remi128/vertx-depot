package com.baldmountain.depot;

import java.math.BigDecimal;

import io.vertx.codetrans.annotations.CodeTranslate;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.example.util.Runner;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.*;
import io.vertx.ext.apex.sstore.LocalSessionStore;
import io.vertx.ext.auth.AuthService;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.auth.shiro.ShiroAuthService;
import io.vertx.ext.jdbc.JdbcService;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SqlConnection;

/**
 * @author Geoffrey Clements
 */
public class DepotVerticle extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(DepotVerticle.class);
    }
    
    private JdbcService service;
    private static final Logger log = LoggerFactory.getLogger(DepotVerticle.class);
    
    @CodeTranslate
    @Override
    public void start() throws Exception {

        JsonObject config = new JsonObject()
                .put("url", "jdbc:sqlite:test.db")
                .put("driver_class", "org.sqlite.JDBC")
                .put("max_pool_size", 30);

        service = JdbcService.create(vertx, config);
        service.start();
        checkDatabase();
        
        final Router router = Router.router(vertx);

        // We need cookies, sessions and request bodies
        router.route().handler(CookieHandler.create());
        router.route().handler(
                SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(BodyHandler.create());

        // Simple auth service which uses a properties file for user/role info
        final AuthService authService = ShiroAuthService.create(vertx,
                ShiroAuthRealmType.PROPERTIES, new JsonObject());

        // Any requests to URI starting '/private/' require login
        router.route("/private/*").handler(
                RedirectAuthHandler.create(authService, "/loginpage.html"));

        // Serve the static private pages from directory 'private'
        router.route("/private/*").handler(
                StaticHandler.create().setCachingEnabled(false)
                        .setWebRoot("private"));

        // Handles the actual login
        router.route("/loginhandler").handler(
                FormLoginHandler.create(authService));

        // Implement logout
        router.route("/logout").handler(context -> {
            context.session().logout();
            // Redirect back to the index page
                context.response().putHeader("location", "/")
                        .setStatusCode(302).end();
            });

        // Serve the non private static pages
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
    
    private void checkDatabase() {
        service.getConnection(res -> {
            if (res.succeeded()) {

              SqlConnection connection = res.result();
              connection.execute("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY ASC, title STRING, description TEXT, imageUrl STRING, price DECIMAL);", result -> {
                 if (result.succeeded()) {
                     connection.query("SELECT * FROM products", res2 -> {
                         if (res2.succeeded()) {
                             ResultSet rs = res2.result();
                             log.info("We got " + rs.getNumRows() + " rows");
                             if (rs.getNumRows() == 0) {
                                 connection.execute("INSERT INTO products (title,description,price) VALUES ('Herland', 'A feminist novel', 2.00 );" , res3 -> {
                                     if (res3.succeeded()) {
                                         log.info(">> inserted");
                                     } else {
                                         log.error(">> didn't insert "+res3.cause().getMessage());
                                     }
                                 });
                             } else {
                                 rs.getResults().stream().forEach(row -> {
                                     log.info(">> id: "+row.getInteger(0));
                                     log.info(">> title: "+row.getString(1));
                                     log.info(">> description: "+row.getString(2));
                                     log.info(">> price: "+new BigDecimal(row.getDouble(4)));
                                 });
                             }
                         }
                     });
                     
                 } else {
                     log.error("Error creating missing table"+result.cause().getMessage());
                     connection.close(null);
                     service.stop();
                     try {
                        stop();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                 }
              });
              // Got a connection

            } else {
              // Failed to get connection - deal with it
                log.error("Can't get a connection to the DB");
                service.stop();
                try {
                    stop();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
          });
        }
}

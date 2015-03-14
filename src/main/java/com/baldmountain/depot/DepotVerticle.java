package com.baldmountain.depot;

import io.vertx.codetrans.annotations.CodeTranslate;
import io.vertx.core.AbstractVerticle;
//import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.example.util.Runner;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.*;
import io.vertx.ext.apex.sstore.LocalSessionStore;
// import io.vertx.ext.auth.AuthService;
// import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
// import io.vertx.ext.auth.shiro.ShiroAuthService;
import io.vertx.ext.mongo.MongoService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffrey Clements
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Geoffrey Clements
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
public class DepotVerticle extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(DepotVerticle.class);
    }

    private static final Logger log = LoggerFactory.getLogger(DepotVerticle.class);
    private MongoService mongoService;
    // make sure the Controllers don't get GCed.
    private List<AbstractController> controllers = new ArrayList<>();

    @CodeTranslate
    @Override
    public void start() throws Exception {

        JsonObject config = new JsonObject().put("db_name", "depot_development");
        mongoService = MongoService.create(vertx, config);
        mongoService.start();

// Now do stuff with it:

//        mongoService.count("products", new JsonObject(), res -> {
//
//            // ...
//            if (res.succeeded()) {
//                log.error("win: "+res.result());
//            } else {
//                log.error("fail: "+res.cause().getMessage());
//            }
//        });
        final Router router = Router.router(vertx);

        // We need cookies, sessions and request bodies
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(BodyHandler.create());

        // Simple auth service which uses a properties file for user/role info
//        final AuthService authService = ShiroAuthService.create(vertx,
//                ShiroAuthRealmType.PROPERTIES, new JsonObject());

        // Any requests to URI starting '/private/' require login
//        router.route("/private/*").handler(
//                RedirectAuthHandler.create(authService, "/loginpage.html"));

        // Serve the static private pages from directory 'private'
//        router.route("/private/*").handler(
//                StaticHandler.create().setCachingEnabled(false)
//                        .setWebRoot("private"));

        // Handles the actual login
//        router.route("/loginhandler").handler(
//                FormLoginHandler.create(authService));

        // Implement logout
//        router.route("/logout").handler(context -> {
//            context.session().logout();
//            // Redirect back to the index page
//            context.response().putHeader("location", "/")
//                    .setStatusCode(302).end();
//        });

        controllers.add(new StoreController(router, mongoService).setupRoutes());
        controllers.add(new ProductsController(router, mongoService).setupRoutes());
        controllers.add(new LineItemsController(router, mongoService).setupRoutes());
        controllers.add(new CartsController(router, mongoService).setupRoutes());

        router.route("/").handler(context -> {
            HttpServerResponse response = context.response();
            response.putHeader("location", "/store");
            response.setStatusCode(302);
            response.end();
        });

//        router.route("/").handler(context -> {
//            Product.all(mongoService, result -> {
//                if (result.succeeded()) {
//                    log.info("product count: " + result.result().size());
//                } else {
//                    context.response().end("Hello world");
//                }
//            });
//        });

        // Serve the non private static pages
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}

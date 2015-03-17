package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.LineItem;
import com.baldmountain.depot.models.Product;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
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

    private List<AbstractController> controllers = new ArrayList<>();
    private Dao<Product, String> productDao;
    private Dao<Cart, String> cartDao;
    private Dao<LineItem, String> lineItemDao;

    @CodeTranslate
    @Override
    public void start() throws Exception {

        String databaseUrl = "jdbc:sqlite:test.db";
        // create a connection source to our database
        ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
        // instantiate the dao
        productDao = DaoManager.createDao(connectionSource, Product.class);
        cartDao = DaoManager.createDao(connectionSource, Cart.class);
        lineItemDao = DaoManager.createDao(connectionSource, LineItem.class);

        TableUtils.createTable(connectionSource, Product.class);    // make sure the Controllers don't get GCed.
        TableUtils.createTable(connectionSource, Cart.class);    // make sure the Controllers don't get GCed.
        TableUtils.createTable(connectionSource, LineItem.class);    // make sure the Controllers don't get GCed.

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

        controllers.add(new StoreController(router, productDao).setupRoutes());
        controllers.add(new ProductsController(router, productDao).setupRoutes());
        controllers.add(new LineItemsController(router, productDao).setupRoutes());
        controllers.add(new CartsController(router, productDao).setupRoutes());

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

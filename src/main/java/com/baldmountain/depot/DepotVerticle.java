package com.baldmountain.depot;

import com.baldmountain.depot.models.Product;
import io.vertx.codetrans.annotations.CodeTranslate;
import io.vertx.core.AbstractVerticle;
//import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.example.util.Runner;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.*;
import io.vertx.ext.apex.sstore.LocalSessionStore;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;
// import io.vertx.ext.auth.AuthService;
// import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
// import io.vertx.ext.auth.shiro.ShiroAuthService;
import io.vertx.ext.mongo.MongoService;

/**
 * @author Geoffrey Clements
 */
public class DepotVerticle extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(DepotVerticle.class);
    }

    private static final Logger log = LoggerFactory.getLogger(DepotVerticle.class);
    private MongoService mongoService;

    private void getProductAndShowNext(RoutingContext context) {
        String productID = context.request().getParam("productid");
        Product.find(mongoService, productID, res -> {
            if (res.succeeded()) {
                context.put("product", res.result());
                context.next();
            } else {
                context.fail(res.cause());
            }
        });
    }

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
//        router.route().handler(BodyHandler.create());

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

        router.get("/products/edit/:productId").handler(context -> {
            getProductAndShowNext(context);
        });

        router.get("/products/show/:productId").handler(context -> {
            getProductAndShowNext(context);
        });

        router.getWithRegex("/products|/products/|/products/index.html").handler(context -> {
            Product.all(mongoService, res -> {
                if (res.succeeded()) {
                    context.put("products", res.result());
                    context.next();
                } else {
                    context.fail(res.cause());
                }
            });
        });

        router.route("/products/*").handler(
                new DepotTemplateHandler(ThymeleafTemplateEngine.create().setMode("HTML5"),
                        "templates/products", "text/html", "/products/"));

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

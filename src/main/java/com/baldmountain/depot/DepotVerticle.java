package com.baldmountain.depot;

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

/**
 * @author Geoffrey Clements
 */
public class DepotVerticle extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(DepotVerticle.class);
    }

    private static final Logger log = LoggerFactory.getLogger(DepotVerticle.class);

    @CodeTranslate
    @Override
    public void start() throws Exception {

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

}

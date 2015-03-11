package com.baldmountain.depot;

import com.baldmountain.depot.models.Product;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;
import io.vertx.ext.mongo.MongoService;

import java.util.Collections;

/**
 * Created by gclements on 3/11/15.
 *
 */
public class StoreController extends AbstractContoller {
    private final ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create().setMode("HTML5");
    private final DepotTemplateHandler templateHandler = new DepotTemplateHandler(engine, "templates/store", "text/html", "/store/");

    public StoreController(final Router router, final MongoService mongoService) {
        super(router, mongoService);
    }

    public AbstractContoller setupRoutes() {
        router.get("/store/show/:productId").handler(context -> {
            getProductAndShowNext(context);
        });

        router.getWithRegex("/store|/store/|/store/index.html").handler(context -> {
            Product.all(mongoService, res -> {
                if (res.succeeded()) {
                    context.put("products", res.result());
                    context.next();
                } else {
                    context.fail(res.cause());
                }
            });
        });

        router.route("/store/*").handler(templateHandler);

        return this;
    }
}

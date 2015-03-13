package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;
import io.vertx.ext.mongo.MongoService;

/**
 * Created by gclements on 3/12/15.
 *
 */
public class CartsController extends AbstractController{
    private final ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create().setMode("HTML5");
    private final DepotTemplateHandler templateHandler = new DepotTemplateHandler(engine, "templates/carts", "text/html", "/carts/");

    public CartsController (final Router router, final MongoService mongoService) {
        super(router, mongoService);
    }

    public AbstractController setupRoutes() {
        router.get("/carts/show/:cartId").handler(context -> {
            String cartId = context.request().getParam("cartId");
            Cart.find(mongoService, cartId, res -> {
                if (res.succeeded()) {
                    res.result().getLineItems(mongoService, res2 -> {
                        if (res2.succeeded()) {
                            context.put("cart", res.result());
                            context.put("lineItems", res2.result());
                            context.next();
                        } else {
                            context.fail(res.cause());
                        }
                    });
                } else {
                    context.fail(res.cause());
                }
            });
        });

        router.route("/carts/*").handler(templateHandler);
        return this;
    }

}

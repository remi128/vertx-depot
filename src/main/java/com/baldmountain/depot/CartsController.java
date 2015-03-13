package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.LineItem;
import com.baldmountain.depot.models.Product;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;
import io.vertx.ext.mongo.MongoService;

import java.util.List;
import java.util.Map;

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
        router.get("/carts/show").handler(context -> {
            getCart(context, res -> {
                if (res.succeeded()) {
                    Cart cart = res.result();
                    context.put("cart", cart);
                    cart.getLineItems(mongoService, res2 -> {
                        if (res2.succeeded()) {
                            List<LineItem> lineItems = res2.result();
                            context.put("lineItems", lineItems);
                            cart.getProductMapForCart(mongoService, res3 -> {
                                if (res2.succeeded()) {
                                    Map<String, Product> productMap = res3.result();
                                    context.put("productMap", productMap);
                                    context.next();
                                } else {
                                    context.fail(res3.cause());
                                }
                            });
                        } else {
                            context.fail(res2.cause());
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

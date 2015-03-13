package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.Product;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.Router;
import io.vertx.ext.mongo.MongoService;

/**
 * Created by gclements on 3/12/15.
 *
 */
public class LineItemsController extends AbstractController {

    public LineItemsController (final Router router, final MongoService mongoService) {
        super(router, mongoService);
    }

    public AbstractController setupRoutes() {
        router.get("/line_items/create/:productId").handler(context -> {
            String productID = context.request().getParam("productid");
            Product.find(mongoService, productID, res -> {
                if (res.succeeded()) {
                    getCart(context, res2 ->{
                        if (res2.succeeded()) {
                            Cart cart = res2.result();
                            cart.addProduct(res.result(), mongoService, res3 -> {
                                if (res3.succeeded()) {
                                    cart.save(mongoService, res4 -> {
                                        if (res4.succeeded()) {
                                            HttpServerResponse response = context.response();
                                            response.putHeader("location", "/carts/show");
                                            response.setStatusCode(302);
                                            response.end();
                                        } else {
                                            context.fail(res4.cause());
                                        }
                                    });
                                }
                                else {
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

        return this;
    }
}

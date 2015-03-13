package com.baldmountain.depot;

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
                            res2.result().addProduct(res.result(), mongoService, res3 -> {
                                if (res3.succeeded()) {
                                    HttpServerResponse response = context.response();
                                    response.putHeader("location", "/carts/show/"+res2.result().getId());
                                    response.setStatusCode(302);
                                    response.end();
                                }
                                else {
                                    context.fail(res.cause());
                                }
                            });
                        } else {
                            context.fail(res.cause());
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

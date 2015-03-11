package com.baldmountain.depot;

import com.baldmountain.depot.models.Product;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.mongo.MongoService;

/**
 * Created by gclements on 3/11/15.
 *
 */
public abstract class AbstractContoller {
    protected final Router router;
    protected final MongoService mongoService;

    protected void getProductAndShowNext(RoutingContext context) {
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

    public AbstractContoller(final Router router, final MongoService mongoService) {
        this.router = router;
        this.mongoService = mongoService;
    }

    public abstract AbstractContoller setupRoutes();
}

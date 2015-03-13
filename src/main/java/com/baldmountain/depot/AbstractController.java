package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.Product;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.mongo.MongoService;

/**
 * Created by gclements on 3/11/15.
 *
 */
public abstract class AbstractController {
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

    public AbstractController(final Router router, final MongoService mongoService) {
        this.router = router;
        this.mongoService = mongoService;
    }

    public abstract AbstractController setupRoutes();

    public void getCart(RoutingContext context, Handler<AsyncResult<Cart>> requestHandler) {
        Session session = context.session();
        String cartId = session.get("cart");
        if (cartId != null) {
            Cart.find(mongoService, cartId, requestHandler);
        } else {
            Cart cart = new Cart();
            cart.save(mongoService, res -> {
                if (res.succeeded()) {
                    cart.setId(res.result());
                    requestHandler.handle(new ConcreteAsyncResult<>(cart));
                } else {
                    requestHandler.handle(new ConcreteAsyncResult<>(res.cause()));
                }
            });
        }
    }
}

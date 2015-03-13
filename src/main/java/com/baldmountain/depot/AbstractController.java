package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.Product;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.apex.Cookie;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.impl.CookieImpl;
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
            // get the existing one
            Cart.find(mongoService, cartId, res -> {
                Cart cart = res.result();
                // make sure the cart we are loading has it's line items
                cart.getLineItems(mongoService, res2 -> {
                    if (res2.succeeded()) {
                        requestHandler.handle(new ConcreteAsyncResult<>(cart));
                    } else {
                        requestHandler.handle(new ConcreteAsyncResult<>(res2.cause()));
                    }
                });
            });
        } else {
            // make a new cart
            Cart cart = new Cart();
            cart.save(mongoService, res -> {
                if (res.succeeded()) {
                    String newCartId = res.result();
                    cart.setId(newCartId);
                    // make sure it goes into the session
                    session.put("cart", newCartId);
                    requestHandler.handle(new ConcreteAsyncResult<>(cart));
                } else {
                    requestHandler.handle(new ConcreteAsyncResult<>(res.cause()));
                }
            });
        }
    }

    protected void moveNoticeToContext(RoutingContext context) {
        Cookie notice = context.getCookie("depot_notice");
        if(notice != null) {
            context.removeCookie("notice");
            String value = notice.getValue();
            if (value != null && !value.isEmpty()) {
                context.put("notice", notice);
            }
        }
    }

    protected void setNoticeInCookie(RoutingContext context, String notice) {
        context.addCookie(new CookieImpl("notice", notice));
    }
}

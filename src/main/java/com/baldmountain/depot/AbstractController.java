package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.Product;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.Cookie;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.mongo.MongoService;

/**
 * @author Geoffrey Clements
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Geoffrey Clements
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

    public AbstractController getCart(RoutingContext context, Handler<AsyncResult<Cart>> requestHandler) {
        Session session = context.session();
        String cartId = session.get("cart");
        if (cartId != null) {
            // get the existing one
            Cart.find(mongoService, cartId, res -> {
                if (res.succeeded()) {
                    Cart cart = res.result();
                    // make sure the cart we are loading has it's line items
                    cart.getLineItems(mongoService, res2 -> {
                        if (res2.succeeded()) {
                            requestHandler.handle(new ConcreteAsyncResult<>(cart));
                        } else {
                            requestHandler.handle(new ConcreteAsyncResult<>(res2.cause()));
                        }
                    });
                } else {
                    setNoticeInCookie(context, "Invaid cart");

                }
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
        return this;
    }

    protected AbstractController moveNoticeToContext(RoutingContext context) {
        Cookie notice = context.getCookie("depot_notice");
        if(notice != null) {
            String value = notice.getValue();
            context.removeCookie("depot_notice");
            if (value != null && !value.isEmpty()) {
                context.put("notice", value);
            }
        }
        return this;
    }

    protected AbstractController  setNoticeInCookie(RoutingContext context, String notice) {
        context.addCookie(Cookie.cookie("depot_notice", notice));
        return this;
    }

    protected AbstractController redirectTo(RoutingContext context, String route) {
        HttpServerResponse response = context.response();
        response.putHeader("location", route);
        response.setStatusCode(302);
        response.end();
        return this;
    }

    protected String getRestilizerMethod(RoutingContext context) {
        String method = context.request().getParam("_method");
        if (method != null) {
            return method.toLowerCase();
        }
        return method;
    }
}

package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.LineItem;
import com.baldmountain.depot.models.Product;
import com.j256.ormlite.dao.Dao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.Cookie;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;

import java.sql.SQLException;

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
    protected final Dao<Product, String> productDao;
    protected final Dao<Cart, String> cartDao;

    protected void getProductAndShowNext(RoutingContext context) {
        String productID = context.request().getParam("productid");
        try {
            Product product = productDao.queryForId(productID);
            context.put("product", product);
        } catch (SQLException ex) {
            context.fail(ex);
        }
    }

    public AbstractController(final Router router, final Dao<Product, String> productDao,
            final Dao<Cart, String> cartDao) {
        this.router = router;
        this.productDao = productDao;
        this.cartDao = cartDao;
    }

    public abstract AbstractController setupRoutes();

    public AbstractController getCart(RoutingContext context, Handler<AsyncResult<Cart>> requestHandler) {
        Session session = context.session();
        String cartId = session.get("cart");
        if (cartId != null) {
            try {
                Cart cart = cartDao.queryForId(cartId);
                requestHandler.handle(new ConcreteAsyncResult<>(cart));
            } catch (SQLException ex) {
                requestHandler.handle(new ConcreteAsyncResult<>(ex));
            }
        } else {
            // make a new cart
            Cart cart = new Cart();
            try {
                cartDao.create(cart);
                requestHandler.handle(new ConcreteAsyncResult<>(cart));
            } catch (SQLException ex) {
                requestHandler.handle(new ConcreteAsyncResult<>(ex));
            }
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
        return null;
    }
}

package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.LineItem;
import com.baldmountain.depot.models.Product;
import io.vertx.ext.apex.Cookie;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;
import io.vertx.ext.mongo.MongoService;

import java.util.List;
import java.util.Map;

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
public class CartsController extends AbstractController{
    private final ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create().setMode("HTML5");
    private final DepotTemplateHandler templateHandler = new DepotTemplateHandler(engine, "templates/carts", "text/html", "/carts/");

    public CartsController (final Router router, final MongoService mongoService) {
        super(router, mongoService);
    }

    public AbstractController setupRoutes() {
        router.get("/carts/show").handler(context -> {
            moveNoticeToContext(context);
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

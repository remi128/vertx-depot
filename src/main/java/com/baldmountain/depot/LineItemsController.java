package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.Product;
import io.vertx.ext.apex.Router;
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
                                            setNoticeInCookie(context, "Line item was successfully created.")
                                                .redirectTo(context, "/carts/show");
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

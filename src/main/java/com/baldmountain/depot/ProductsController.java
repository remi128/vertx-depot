package com.baldmountain.depot;

import com.baldmountain.depot.models.Product;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;
import io.vertx.ext.mongo.MongoService;

import java.util.Collections;

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
public class ProductsController extends AbstractController {
    private final ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create().setMode("HTML5");
    private final DepotTemplateHandler templateHandler = new DepotTemplateHandler(engine, "templates/products", "text/html", "/products/");

    public ProductsController (final Router router, final MongoService mongoService) {
        super(router, mongoService);
    }

    public AbstractController setupRoutes() {
        router.get("/products/edit/:productId").handler(context -> {
            getProductAndShowNext(context);
        });

        router.get("/products/show/:productId").handler(context -> {
            getProductAndShowNext(context);
        });

        router.post("/products/:productId").handler(context -> {
            String productID = context.request().getParam("productid");
            String method = getRestilizerMethod(context);
            switch(method) {
                case "delete":
                    Product.find(mongoService, productID, res -> {
                        if (res.succeeded()) {
                            Product product = res.result();
                            product.delete(mongoService, res2 -> {
                                setNoticeInCookie(context, "'" + product.getTitle() + "' was deleted.")
                                        .redirectTo(context, "/products");
                            });
                        } else {
                            context.fail(res.cause());
                        }
                    });
                    break;
                case "put":
                    if (!"0".equals(productID)) {
                        Product.find(mongoService, productID, res -> {
                            if (res.succeeded()) {
                                Product product = res.result();
                                try {
                                    product.update(context.request().formAttributes(), true);
                                    product.save(mongoService, res2 -> {
                                        if (res2.succeeded()) {
                                            Product.all(mongoService, res3 -> {
                                                if (res3.succeeded()) {
                                                    setNoticeInCookie(context, "'"+product.getTitle()+"' was successfully saved.")
                                                            .redirectTo(context, "/products");
                                                } else {
                                                    context.fail(res2.cause());
                                                }
                                            });
                                        } else {
                                            context.fail(res2.cause());
                                        }
                                    });
                                } catch (Exception e) {
                                    product.update(context.request().formAttributes(), false);
                                    context.put("product", new Product(context.request().formAttributes(), false));
                                    context.put("errors", Collections.singletonList(e.getMessage()));
                                    templateHandler.renderSpecificPath(context, "/edit.html");
                                }
                            } else {
                                context.put("product", new Product(context.request().formAttributes(), false));
                                context.put("errors", Collections.singletonList(res.cause().getMessage()));
                                templateHandler.renderSpecificPath(context, "/new.html");
                            }
                        });
                    } else {
                        try {
                            Product product = new Product(context.request().formAttributes(), true);
                            product.save(mongoService, res -> {
                                if (res.succeeded()) {
                                    Product.all(mongoService, res2 -> {
                                        if (res.succeeded()) {
                                            setNoticeInCookie(context, "'"+product.getTitle()+"' was successfully created.")
                                                    .redirectTo(context, "/products");
                                        } else {
                                            context.fail(res2.cause());
                                        }
                                    });
                                } else {
                                    context.put("product", new Product(context.request().formAttributes(), false));
                                    context.put("errors", Collections.singletonList(res.cause().getMessage()));
                                    templateHandler.renderSpecificPath(context, "/new.html");
                                }
                            });
                        } catch (Exception e) {
                            context.put("product", new Product(context.request().formAttributes(), false));
                            context.put("errors", Collections.singletonList(e.getMessage()));
                            templateHandler.renderSpecificPath(context, "/new.html");
                        }
                    }
                    break;
                default:
                    context.fail(new IllegalArgumentException("Unknown post method specified."));
            }
        });

        router.get("/products/new").handler(context -> {
            context.put("product", new Product());
            context.next();
        });

        router.getWithRegex("/products|/products/|/products/index.html").handler(context -> {
            moveNoticeToContext(context);
            Product.all(mongoService, res -> {
                if (res.succeeded()) {
                    context.put("products", res.result());
                    context.next();
                } else {
                    context.fail(res.cause());
                }
            });
        });

        router.route("/products/*").handler(templateHandler);

        return this;
    }
}

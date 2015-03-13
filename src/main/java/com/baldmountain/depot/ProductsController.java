package com.baldmountain.depot;

import com.baldmountain.depot.models.Product;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;
import io.vertx.ext.mongo.MongoService;

import java.util.Collections;

/**
 * Created by gclements on 3/11/15.
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

        router.post("/products/delete/:productId").handler(context -> {
            String productID = context.request().getParam("productid");
            Product.find(mongoService, productID, res -> {
                if (res.succeeded()) {
                    res.result().delete(mongoService, res2 -> {
                        HttpServerResponse response = context.response();
                        response.putHeader("location", "/products");
                        response.setStatusCode(302);
                        response.end();
                    });
                } else {
                    context.fail(res.cause());
                }
            });
        });

        router.get("/products/new").handler(context -> {
            context.put("product", new Product());
            context.next();
        });

        router.post("/products/save").handler(context -> {
            String id = context.request().formAttributes().get("_id");
            if (id != null && !id.isEmpty()) {
                Product.find(mongoService, id, res -> {
                    if (res.succeeded()) {
                        Product product = res.result();
                        try {
                            product.update(context.request().formAttributes(), true);
                            product.save(mongoService, res2 -> {
                                if (res2.succeeded()) {
                                    Product.all(mongoService, res3 -> {
                                        if (res3.succeeded()) {
                                            HttpServerResponse response = context.response();
                                            response.putHeader("location", "/products");
                                            response.setStatusCode(302);
                                            response.end();
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
                                    HttpServerResponse response = context.response();
                                    response.putHeader("location", "/products");
                                    response.setStatusCode(302);
                                    response.end();
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
        });

        router.getWithRegex("/products|/products/|/products/index.html").handler(context -> {
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

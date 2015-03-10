package com.baldmountain.depot;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.TemplateHandler;
import io.vertx.ext.apex.impl.Utils;
import io.vertx.ext.apex.templ.TemplateEngine;

/**
 * Created by gclements on 3/10/15.
 *
 */
public class ProductsTemplateHandler implements TemplateHandler {
    private final TemplateEngine engine;
    private final String templateDirectory;
    private final String contentType;

    public ProductsTemplateHandler(TemplateEngine engine, String templateDirectory, String contentType) {
        this.engine = engine;
        this.templateDirectory = templateDirectory;
        this.contentType = contentType;
    }

    @Override
    public void handle(RoutingContext context) {
        // /products/:operation/:id where operation and id are optional
        String path = context.normalisedPath().substring(1);
        String[] parts = path.split("/");
        switch(parts.length) {
            case 3:
            case 2:
                path = "/products/"+parts[1]+".html";
                break;
            default:
                path = "/products/index.html";
        }
        String file = templateDirectory + Utils.pathOffset(path, context);
        engine.render(context, file, res -> {
            if (res.succeeded()) {
                context.response().putHeader(HttpHeaders.CONTENT_TYPE, contentType).end(res.result());
            } else {
                context.fail(res.cause());
            }
        });
    }
}

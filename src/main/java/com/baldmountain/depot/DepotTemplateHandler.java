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
public class DepotTemplateHandler implements TemplateHandler {
    private final TemplateEngine engine;
    private final String templateDirectory;
    private final String contentType;
    private final String pathPrefix;

    // The format of pathPrefix is "/foo/" so we don't have to append/prepend /
    public DepotTemplateHandler(TemplateEngine engine, String templateDirectory, String contentType, String pathPrefix) {
        this.engine = engine;
        this.templateDirectory = templateDirectory;
        this.contentType = contentType;
        this.pathPrefix = pathPrefix;
    }

    @Override
    public void handle(RoutingContext context) {
        // /products/:operation/:id where operation and id are optional, remove first /
        String path = context.normalisedPath().substring(1);
        String[] parts = path.split("/");
        switch(parts.length) {
            case 3:
                path = pathPrefix+parts[1]+".html";
            case 2:
                if (parts[1].isEmpty() || "index.html".equals(parts[1])) {
                    path = pathPrefix+"index.html";
                } else {
                    path = pathPrefix+parts[1]+".html";
                }
                break;
            default:
                path = pathPrefix+"index.html";
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

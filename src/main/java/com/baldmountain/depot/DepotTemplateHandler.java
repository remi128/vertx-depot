package com.baldmountain.depot;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.TemplateHandler;
import io.vertx.ext.apex.impl.Utils;
import io.vertx.ext.apex.templ.TemplateEngine;

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
        renderSpecificPath(context, Utils.pathOffset(path, context));
    }

    public void renderSpecificPath(RoutingContext context, String path) {
        String file = templateDirectory + path;
        engine.render(context, file, res -> {
            if (res.succeeded()) {
                context.response().putHeader(HttpHeaders.CONTENT_TYPE, contentType).end(res.result());
            } else {
                context.fail(res.cause());
            }
        });
    }
}

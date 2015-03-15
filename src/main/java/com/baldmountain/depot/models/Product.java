package com.baldmountain.depot.models;

import com.baldmountain.depot.ConcreteAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

public class Product extends BaseModel {
    private String title;
    private String description;
    private String imageUrl;
    private BigDecimal price;

    public Product(JsonObject json) {
        super("products", json);
        title = json.getString("title");
        description = json.getString("description");
        imageUrl = json.getString("imageUrl");
        if (json.containsKey("price")) {
            price = new BigDecimal(json.getDouble("price")).setScale(2, RoundingMode.CEILING);
        } else {
            price = BigDecimal.ZERO.setScale(2, RoundingMode.CEILING);
        }
    }

    public Product(String title, String description, String imageUrl, BigDecimal price) {
        super("products");
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public Product(MultiMap formParams, boolean validate) {
        this();
        update(formParams, validate);
    }

    // empty Product for /products/new
    public Product() {
        super("products");
        title = "";
        description = "";
        imageUrl = "";
        price = BigDecimal.ZERO.setScale(2, RoundingMode.CEILING);
    }


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Product update(MultiMap formParams, boolean validate) {
        String s = formParams.get("description");
        if (s != null && !description.equals(s)) {
            description = s;
            dirty = true;
        }
        s = formParams.get("imageUrl");
        if (s != null && !imageUrl.equals(s)) {
            imageUrl = s;
            dirty = true;
        }
        if (validate) {
            s = formParams.get("title");
            if (s == null || s.isEmpty()) {
                throw new IllegalArgumentException("Product must have a title");
            }
            if (!title.equals(s)) {
                title = s;
                dirty = true;
            }
            BigDecimal p = new BigDecimal(formParams.get("price")).setScale(2, RoundingMode.CEILING);
            if (!price.equals(p)) {
                price = p;
                dirty = true;
            }
        } else {
            s = formParams.get("title");
            if (s != null && !title.equals(s)) {
                title = s;
                dirty = true;
            }
            try {
                BigDecimal p = new BigDecimal(formParams.get("price")).setScale(2, RoundingMode.CEILING);
                if (!price.equals(p)) {
                    price = p;
                    dirty = true;
                }
            } catch(Exception e) {
                // ignore
            }
        }
        return this;
    }

    public Product save(MongoService service, Handler<AsyncResult<String>> resultHandler) {
        if (!"0".equals(id)) {
            // update existing
            if (dirty) {
                JsonObject json = new JsonObject()
                        .put("title", title)
                        .put("description", description)
                        .put("imageUrl", imageUrl)
                        .put("price", price.doubleValue());
                setDates(json);
                service.replace("products",
                        new JsonObject().put("_id", id),
                        json, (Void) -> resultHandler.handle(new ConcreteAsyncResult<>(id)));
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(id));
            }
        } else {
            JsonObject json = new JsonObject()
                    .put("title", title)
                    .put("description", description)
                    .put("imageUrl", imageUrl)
                    .put("price", price.doubleValue());
            setDates(json);
            service.save("products", json, resultHandler);
        }
        return this;
    }

    public static void find(MongoService service, String id, Handler<AsyncResult<Product>> resultHandler) {
        assert !"0".equals(id);
        JsonObject query = new JsonObject().put("_id", id);
        service.findOne("products", query, null, res -> {
            if (res.succeeded()) {
                resultHandler.handle(new ConcreteAsyncResult<>(new Product(res.result())));
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
            }
        });
    }

    public static void all(MongoService service, Handler<AsyncResult<List<Product>>> resultHandler) {
        service.find("products", new JsonObject(), res -> {
            if (res.succeeded()) {
                List<Product> products = res.result().stream().map(Product::new).collect(Collectors.toList());
                products.sort((p1, p2) -> p1.getTitle().compareTo(p2.getTitle()));
                resultHandler.handle(new ConcreteAsyncResult<>(products));
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
            }
        });
    }
}

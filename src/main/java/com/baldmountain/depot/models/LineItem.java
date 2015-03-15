package com.baldmountain.depot.models;

import com.baldmountain.depot.ConcreteAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.util.HashMap;
import java.util.List;
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
public class LineItem extends BaseModel {
    private String cartId;
    private String productId;
    private int count;

    public LineItem(JsonObject json) {
        super("line_items", json);
        cartId = json.getString("cart");
        productId = json.getString("product");
        count = json.getInteger("count", 1);
    }

    public LineItem(String cart, String product) {
        super("line_items");
        cartId = cart;
        productId = product;
        count = 1;
    }

    public String getCartId() {
        return cartId;
    }

    public String getProductId() {
        return productId;
    }

    public int getCount() {
        return count;
    }

    public int incrementCount(int num) {
        count += num;
        dirty = true;
        return count;
    }

    public LineItem save(MongoService service, Handler<AsyncResult<String>> resultHandler) {
        JsonObject json = new JsonObject()
                .put("cart", cartId)
                .put("product", productId)
                .put("count", count);
        if (!"0".equals(id)) {
            // update existing
            if (dirty) {
                setDates(json);
                service.replace(collection,
                        new JsonObject().put("_id", id),
                        json, (Void) -> resultHandler.handle(new ConcreteAsyncResult<>(id)));
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(id));
            }
        } else {
            setDates(json);
            service.save(collection, json, resultHandler);
        }
        return this;
    }

    public static void findForCart(MongoService service, String cart, Handler<AsyncResult<List<LineItem>>> resultHandler) {
        assert !"0".equals(cart);
        JsonObject query = new JsonObject().put("cart", cart);
        service.find("line_items", query, res -> {
            if (res.succeeded()) {
                List<LineItem> lineItems = res.result().stream().map(LineItem::new).collect(Collectors.toList());
                resultHandler.handle(new ConcreteAsyncResult<>(lineItems));
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
            }
        });
    }
}

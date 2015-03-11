package com.baldmountain.depot.models;

import com.baldmountain.depot.ConcreteAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by gclements on 3/11/15.
 *
 */
public class Cart extends BaseModel {
    private List<LineItem> lineItems;

    public Cart(JsonObject json, MongoService mongoService, Handler<AsyncResult<String>> resultHandler) {
        super(json);
        LineItem.findForCart(mongoService, id, res -> {
            if (res.succeeded()) {
                lineItems = res.result();
                resultHandler.handle(new ConcreteAsyncResult<>(id));
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
            }
        });
    }

    // empty Cart
    public Cart(MongoService mongoService, Handler<AsyncResult<String>> resultHandler) {
        lineItems = new ArrayList<>();
        JsonObject json = new JsonObject();
        setDates(json);
        mongoService.save("carts", json, res -> {
            if (res.succeeded()) {
                id = res.result();
                resultHandler.handle(new ConcreteAsyncResult<>(res.result()));
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
            }
        });
    }

    public Cart addProduct(Product product) {
        Optional<LineItem> lineItem = lineItems.stream().filter(item -> item.getProductId().equals(product.getId())).findFirst();
        if (lineItem.isPresent()) {
            lineItem.get().incrementCount(1);
        } else {
            lineItems.add(new LineItem(id, product.getId()));
        }
        return this;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }
}

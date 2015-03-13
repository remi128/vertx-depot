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
    private List<LineItem> lineItems = null;

    public Cart(JsonObject json) {
        super(json);
    }

    // empty Cart
    public Cart() {
        lineItems = new ArrayList<>();
    }

    // we need this in AbstracController.getCart()
    public Cart setId(String id) {
        this.id = id;
        return this;
    }

    public Cart addProduct(Product product, MongoService mongoService, Handler<AsyncResult<String>> resultHandler) {
        Optional<LineItem> lineItem = lineItems.stream().filter(item -> item.getProductId().equals(product.getId())).findFirst();
        if (lineItem.isPresent()) {
            LineItem li = lineItem.get();
            li.incrementCount(1);
            resultHandler.handle(new ConcreteAsyncResult<>(li.getId()));
        } else {
            LineItem newLineItem = new LineItem(id, product.getId()).save(mongoService, res -> {
                if (res.succeeded()) {
                    // since it is saved, we have an id
                    this.id = res.result();
                    resultHandler.handle(new ConcreteAsyncResult<>(this.id));
                } else {
                    resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
                }
            });
            lineItems.add(newLineItem);
        }
        return this;
    }

    public Cart getLineItems(MongoService mongoService, Handler<AsyncResult<List<LineItem>>> resultHandler) {
        if (lineItems != null) {
            resultHandler.handle(new ConcreteAsyncResult<>(lineItems));
        } else {
            LineItem.findForCart(mongoService, id, res -> {
                if (res.succeeded()) {
                    lineItems = res.result();
                    resultHandler.handle(new ConcreteAsyncResult<>(lineItems));
                } else {
                    resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
                }
            });
        }
        return this;
    }

    public static void find(MongoService mongoService, String id, Handler<AsyncResult<Cart>> resultHandler) {
        JsonObject query = new JsonObject().put("_id", id);
        mongoService.findOne("carts", query, null, res -> {
            if (res.succeeded()) {
                if (res.result() == null) {
                    // it's null?
                    resultHandler.handle(new ConcreteAsyncResult<>(new Cart(new JsonObject().put("_id", id))));
                } else {
                    resultHandler.handle(new ConcreteAsyncResult<>(new Cart(res.result())));
                }
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
            }
        });
    }

    public Cart save(MongoService mongoService, Handler<AsyncResult<String>> resultHandler) {
        JsonObject json = new JsonObject();
        setDates(json);
        if (id != null) {
            // update existing
            mongoService.replace("carts",
                    new JsonObject().put("_id", id),
                    json, (Void) -> resultHandler.handle(new ConcreteAsyncResult<>(id)));
        } else {
            mongoService.save("carts", json, resultHandler);
        }
        lineItems.stream().forEach(lineItem -> {
            // we should probably wait until this is done.
            lineItem.save(mongoService, s -> {});
        });
        return this;
    }
}

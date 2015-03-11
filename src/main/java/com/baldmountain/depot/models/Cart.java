package com.baldmountain.depot.models;

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
                resultHandler.handle(new AsyncResult<String>() {
                    @Override
                    public String result() {
                        return id;
                    }

                    @Override
                    public Throwable cause() {
                        return null;
                    }

                    @Override
                    public boolean succeeded() {
                        return true;
                    }

                    @Override
                    public boolean failed() {
                        return false;
                    }
                });
            } else {
                resultHandler.handle(new AsyncResult<String>() {
                    @Override
                    public String result() {
                        return null;
                    }

                    @Override
                    public Throwable cause() {
                        return res.cause();
                    }

                    @Override
                    public boolean succeeded() {
                        return false;
                    }

                    @Override
                    public boolean failed() {
                        return true;
                    }
                });
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
                resultHandler.handle(new AsyncResult<String>() {
                    @Override
                    public String result() {
                        return res.result();
                    }

                    @Override
                    public Throwable cause() {
                        return null;
                    }

                    @Override
                    public boolean succeeded() {
                        return false;
                    }

                    @Override
                    public boolean failed() {
                        return false;
                    }
                });
            } else {
                resultHandler.handle(new AsyncResult<String>() {
                    @Override
                    public String result() {
                        return null;
                    }

                    @Override
                    public Throwable cause() {
                        return res.cause();
                    }

                    @Override
                    public boolean succeeded() {
                        return false;
                    }

                    @Override
                    public boolean failed() {
                        return false;
                    }
                });
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

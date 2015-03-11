package com.baldmountain.depot.models;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by gclements on 3/11/15.
 *
 */
public class LineItem extends BaseModel {
    private String cartId;
    private String productId;
    private int count;

    public LineItem(JsonObject json) {
        super(json);
        cartId = json.getString("cart");
        productId = json.getString("product");
        count = json.getInteger("count", 1);
    }

    public LineItem(String cart, String product) {
        cartId = cart;
        productId = product;
        count = 1;
    }

    public LineItem(String cart, String product, int cnt) {
        cartId = cart;
        productId = product;
        count = cnt;
    }

    public String getCartId() {
        return cartId;
    }

    public String getProductId() {
        return cartId;
    }

    public int getCount() {
        return count;
    }

    public int incrementCount(int num) {
        count += num;
        return count;
    }

    public void save(MongoService service, Handler<AsyncResult<String>> resultHandler) {
        JsonObject json = new JsonObject()
                .put("cart", cartId)
                .put("product", productId)
                .put("count", count);
        setDates(json);
        if (id != null) {
            // update existing
            service.replace("line_items",
                    new JsonObject().put("_id", id),
                    json, (Void) -> resultHandler.handle(new AsyncResult<String>() {
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
                    }));
        } else {
            service.save("line_items", json, resultHandler);
        }
    }

    public static void findForCart(MongoService service, String cart, Handler<AsyncResult<List<LineItem>>> resultHandler) {
        JsonObject query = new JsonObject().put("cart", cart);
        service.find("line_items", query, res -> {
            if (res.succeeded()) {
                List<LineItem> lineItems = res.result().stream().map(LineItem::new).collect(Collectors.toList());
                resultHandler.handle(new AsyncResult<List<LineItem>>() {
                    @Override
                    public List<LineItem> result() {
                        return lineItems;
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
                resultHandler.handle(new AsyncResult<List<LineItem>>() {
                    @Override
                    public List<LineItem> result() {
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
}

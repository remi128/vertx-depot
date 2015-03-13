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
        if (id != null) {
            // update existing
            if (dirty) {
                setDates(json);
                service.replace("line_items",
                        new JsonObject().put("_id", id),
                        json, (Void) -> resultHandler.handle(new ConcreteAsyncResult<>(id)));
            }
        } else {
            setDates(json);
            service.save("line_items", json, resultHandler);
        }
        return this;
    }

    public static void findForCart(MongoService service, String cart, Handler<AsyncResult<List<LineItem>>> resultHandler) {
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

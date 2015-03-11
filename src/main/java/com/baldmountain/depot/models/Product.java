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
import java.util.List;
import java.util.stream.Collectors;

public class Product extends BaseModel {
    private String title;
    private String description;
    private String imageUrl;
    private BigDecimal price;

    public Product(JsonObject json) {
        super(json);
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
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public Product(MultiMap formParams, boolean validate) {
        update(formParams, validate);
    }

    // empty Product for /products/new
    public Product() {
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

    public void update(MultiMap formParams, boolean validate) {
        if (validate) {
            if (formParams.get("title").isEmpty()) {
                throw new IllegalArgumentException("Product must have a title");
            }
            title = formParams.get("title");
            description = formParams.get("description");
            imageUrl = formParams.get("imageUrl");
            price = new BigDecimal(formParams.get("price")).setScale(2, RoundingMode.CEILING);
        } else {
            title = formParams.get("title");
            description = formParams.get("description");
            imageUrl = formParams.get("imageUrl");
            try {
                price = new BigDecimal(formParams.get("price")).setScale(2, RoundingMode.CEILING);
            } catch(Exception e) {
                // ignore
            }
        }
    }

    public void save(MongoService service, Handler<AsyncResult<String>> resultHandler) {
        JsonObject json = new JsonObject()
                .put("title", title)
                .put("description", description)
                .put("imageUrl", imageUrl)
                .put("price", price.doubleValue());
        setDates(json);
        if (id != null) {
            // update existing
            service.replace("products",
                    new JsonObject().put("_id", id),
                    json, (Void) -> resultHandler.handle(new ConcreteAsyncResult<>(id)));
        } else {
            service.save("products", json, resultHandler);
        }
    }

    public static void find(MongoService service, String id, Handler<AsyncResult<Product>> resultHandler) {
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
    public void delete(MongoService service, Handler<AsyncResult<Void>> resultHandler) {
        service.remove("products", new JsonObject().put("_id", id), resultHandler);
    }
}

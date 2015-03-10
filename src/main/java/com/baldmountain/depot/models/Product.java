package com.baldmountain.depot.models;

import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.math.BigDecimal;
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
            price = new BigDecimal(json.getDouble("price"));
        } else {
            price = BigDecimal.ZERO;
        }
    }

    public Product(String title, String description, String imageUrl, BigDecimal price) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
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

    public void save(MongoService service, Handler<AsyncResult<String>> resultHandler) {
        JsonObject json = new JsonObject()
                .put("title", title)
                .put("description", description)
                .put("imageUrl", imageUrl)
                .put("price", price.doubleValue());
        setDates(json);
        if (id != null) {
            // update existing
            service.update("products",
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
            service.save("products", json, resultHandler);
        }
    }

    public static void find(MongoService service, String id, Handler<AsyncResult<Product>> resultHandler) {
        JsonObject query = new JsonObject().put("_id", id);
        service.findOne("products", query, null, res -> {
            if (res.succeeded()) {
                resultHandler.handle(new AsyncResult<Product>() {
                    @Override
                    public Product result() {
                        return new Product(res.result());
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
                resultHandler.handle(new AsyncResult<Product>() {
                    @Override
                    public Product result() {
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

    public static void all(MongoService service, Handler<AsyncResult<List<Product>>> resultHandler) {
        service.find("products", new JsonObject(), res -> {
            if (res.succeeded()) {
                List<Product> products = res.result().stream().map(Product::new).collect(Collectors.toList());
                resultHandler.handle(new AsyncResult<List<Product>>() {
                    @Override
                    public List<Product> result() {
                        return products;
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
                resultHandler.handle(new AsyncResult<List<Product>>() {
                    @Override
                    public List<Product> result() {
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
    public void delete(MongoService service, Handler<AsyncResult<Void>> resultHandler) {
        service.remove("products", new JsonObject().put("_id", id), resultHandler);
    }
}

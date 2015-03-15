package com.baldmountain.depot.models;

import com.baldmountain.depot.ConcreteAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.util.*;

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
public class Cart extends BaseModel {
    private static final HashMap<String, Cart> cache = new HashMap<>();
    private List<LineItem> lineItems = null;


    public Cart(JsonObject json) {
        super("carts", json);
    }

    // empty Cart
    public Cart() {
        super("carts");
    }

    // we need this in AbstracController.getCart()
    public Cart setId(String id) {
        assert !"0".equals(id);
        this.id = id;
        return this;
    }

    public Cart addProduct(Product product, MongoService mongoService, Handler<AsyncResult<String>> resultHandler) {
        if (lineItems == null) {
            lineItems = new ArrayList<>();
        }
        dirty = true;
        Optional<LineItem> lineItem = lineItems.stream().filter(item -> item.getProductId().equals(product.getId())).findFirst();
        if (lineItem.isPresent()) {
            LineItem li = lineItem.get();
            li.incrementCount(1);
            resultHandler.handle(new ConcreteAsyncResult<>(li.getId()));
        } else {
            assert !"0".equals(id);
            LineItem newLineItem = new LineItem(id, product.getId());
            newLineItem.save(mongoService, res -> {
                if (res.succeeded()) {
                    // since it is saved, we have an id
                    newLineItem.id = res.result();
                    lineItems.add(newLineItem);
                    resultHandler.handle(new ConcreteAsyncResult<>(newLineItem.id));
                } else {
                    resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
                }
            });
        }
        return this;
    }

    public Cart getLineItems(MongoService mongoService, Handler<AsyncResult<List<LineItem>>> resultHandler) {
        if (lineItems != null) {
            resultHandler.handle(new ConcreteAsyncResult<>(lineItems));
        } else {
            assert !"0".equals(id);
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
        assert !"0".equals(id);
        Cart cart = cache.get(id);
        if (cart != null) {
            resultHandler.handle(new ConcreteAsyncResult<>(cart));
        } else {
            JsonObject query = new JsonObject().put("_id", id);
            mongoService.findOne("carts", query, null, res -> {
                if (res.succeeded()) {
                    Cart newCart;
                    if (res.result() == null) {
                        // it's null?
                        newCart = new Cart(new JsonObject().put("_id", id));
                    } else {
                        newCart = new Cart(res.result());
                    }
                    assert !"0".equals(id);
                    cache.put(id, newCart);
                    resultHandler.handle(new ConcreteAsyncResult<>(newCart));
                } else {
                    resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
                }
            });
        }
    }

    private void saveLineItems(MongoService mongoService, Handler<AsyncResult<String>> resultHandler) {
        if (lineItems != null) {
            ArrayList<String> savedItems = new ArrayList<>(lineItems.size());
            if (lineItems.size() > 0) {
                lineItems.stream().forEach(lineItem -> {
                    // we should probably wait until this is done. But because carts are cached
                    // we'll get this one back. There is still a race condition here. :(
                    lineItem.save(mongoService, res -> {
                        if (res.succeeded()) {
                            savedItems.add(res.result());
                            if (savedItems.size() == lineItems.size()) {
                                resultHandler.handle(new ConcreteAsyncResult<>(id));
                            }
                        } else {
                            resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
                        }
                    });
                });
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(id));
            }
        } else {
            resultHandler.handle(new ConcreteAsyncResult<>(this.id));
        }
    }

    public Cart save(MongoService mongoService, Handler<AsyncResult<String>> resultHandler) {
        JsonObject json = new JsonObject();
        if (!"0".equals(id)) {
            if (dirty) {
                setDates(json);
                // update existing
                mongoService.replace(collection,
                        new JsonObject().put("_id", id),
                        json, (Void) -> {
                            dirty = false;
                            saveLineItems(mongoService, resultHandler);
                        });
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(id));
            }
        } else {
            setDates(json);
            mongoService.save(collection, json, res -> {
                if (res.succeeded()) {
                    this.id = res.result();
                    dirty = false;
                    saveLineItems(mongoService, resultHandler);
                } else {
                    resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
                }
            });
        }
        return this;
    }

    public Cart getProductMapForCart(MongoService mongoService, Handler<AsyncResult<Map<String, Product>>> resultHandler) {
        getLineItems(mongoService, res -> {
            if (res.succeeded()) {
                List<LineItem> lineItems = res.result();
                Map<String, Product> map = new HashMap<>();
                if (lineItems.size() > 0) {
                    lineItems.stream().forEach(lineItem -> {
                        Product.find(mongoService, lineItem.getProductId(), res2 -> {
                            if (res2.succeeded()) {
                                Product p = res2.result();
                                map.put(p.getId(), p);
                                if (map.size() == lineItems.size()) {
                                    resultHandler.handle(new ConcreteAsyncResult<>(map));
                                } else {
                                    res2.cause();
                                }
                            }
                        });
                    });
                } else {
                    resultHandler.handle(new ConcreteAsyncResult<>(map));
                }
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
            }
        });
        return this;
    }

    @Override
    public BaseModel delete(MongoService service, Handler<AsyncResult<Void>> resultHandler) {
        assert !"0".equals(id);
        cache.remove(id);
        service.remove(collection, new JsonObject().put("_id", id), res -> {
            if (res.succeeded()) {
                id = "0";
                resultHandler.handle(new ConcreteAsyncResult<>((Void) null));
            } else {
                resultHandler.handle(new ConcreteAsyncResult<>(res.cause()));
            }
        });
        return this;
    }
}

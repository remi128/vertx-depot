package com.baldmountain.depot;

import com.baldmountain.depot.models.Cart;
import com.baldmountain.depot.models.LineItem;
import com.baldmountain.depot.models.Product;
import com.github.jknack.handlebars.Options;
import io.vertx.ext.apex.Router;
import io.vertx.ext.mongo.MongoService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffrey Clements
 *         <p>
 *         The MIT License (MIT)
 *         <p>
 *         Copyright (c) 2015 Geoffrey Clements
 *         <p>
 *         Permission is hereby granted, free of charge, to any person obtaining a copy
 *         of this software and associated documentation files (the "Software"), to deal
 *         in the Software without restriction, including without limitation the rights
 *         to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *         copies of the Software, and to permit persons to whom the Software is
 *         furnished to do so, subject to the following conditions:
 *         <p>
 *         The above copyright notice and this permission notice shall be included in all
 *         copies or substantial portions of the Software.
 *         <p>
 *         THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *         IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *         FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *         AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *         LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *         OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *         SOFTWARE.
 */
public class CartsController extends AbstractController {
  private final DepotHandlebarsTemplateEngine engine = DepotHandlebarsTemplateEngine.create();
  private final DepotTemplateHandler templateHandler = new DepotTemplateHandler(engine, "templates/carts", "text/html", "/carts/");

  public CartsController(final Router router, final MongoService mongoService) {
    super(router, mongoService);
    engine.getHandlebars().registerHelper("lineItemTotal", (Integer count, Options options) -> {
      String productId = options.param(0);
      Map<String, Product> productMap = options.param(1);
      return new BigDecimal(count).multiply(productMap.get(productId).getPrice()).toString();
    }).registerHelper("productTitle", (String productId, Options options) -> {
      Map<String, Product> productMap = options.param(0);
      return productMap.get(productId).getTitle();
    });

  }

  public AbstractController setupRoutes() {
    router.get("/carts/show").handler(context -> {
      moveNoticeToContext(context);
      getCart(context, res -> {
        if (res.succeeded()) {
          Cart cart = res.result();
          context.put("cart", cart);
          cart.getLineItems(mongoService, res2 -> {
            if (res2.succeeded()) {
              List<LineItem> lineItems = res2.result();
              context.put("lineItems", lineItems);
              cart.getProductMapForCart(mongoService, res3 -> {
                if (res2.succeeded()) {
                  Map<String, Product> productMap = res3.result();
                  context.put("productMap", productMap);
                  BigDecimal totalPrice = lineItems.stream()
                      .map(li -> productMap.get(li.getProductId()).getPrice().multiply(new BigDecimal(li.getCount())))
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
                  context.put("totalPrice", totalPrice);
                  context.next();
                } else {
                  context.fail(res3.cause());
                }
              });
            } else {
              context.fail(res2.cause());
            }
          });
        } else {
          context.fail(res.cause());
        }
      });
    });

    router.post("/carts").handler(context -> {
      // get cart
      String method = getRestilizerMethod(context);
      switch (method) {
        case "delete":
          getCart(context, res -> {
            if (res.succeeded()) {
              Cart cart = res.result();
              // get car items
              cart.getLineItems(mongoService, res2 -> {
                if (res2.succeeded()) {
                  List<LineItem> lineItems = res2.result();
                  if (lineItems.size() > 0) {
                    // if there are any delete them
                    ArrayList<LineItem> deletedLineItems = new ArrayList<>(lineItems.size());
                    lineItems.stream().forEach(li -> {
                      li.delete(mongoService, (Void) -> {
                        deletedLineItems.add(li);
                        if (deletedLineItems.size() == lineItems.size()) {
                          // once we're don deleting line items, delete the cart.
                          cart.delete(mongoService, res3 -> {
                            if (res3.succeeded()) {
                              setNoticeInCookie(context, "Cart successfully emptied.");
                              redirectTo(context, "/store");
                            } else {
                              context.fail(res3.cause());
                            }
                          });
                        }
                      });
                    });
                  } else {
                    cart.delete(mongoService, res3 -> {
                      if (res3.succeeded()) {
                        setNoticeInCookie(context, "Cart successfully emptied.");
                        redirectTo(context, "/store");
                      } else {
                        context.fail(res3.cause());
                      }
                    });
                  }
                } else {
                  context.fail(res2.cause());
                }
              });
            } else {
              context.fail(res.cause());
            }
          });
          break;
      }
    });

    router.route("/carts/*").handler(templateHandler);
    return this;
  }
}

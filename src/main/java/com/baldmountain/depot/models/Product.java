package com.baldmountain.depot.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.vertx.core.MultiMap;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

@DatabaseTable(tableName = "carts")
public class Product {
    @DatabaseField(id = true, generatedId = true)
    private int id;
    @DatabaseField
    private String title;
    @DatabaseField
    private String description;
    @DatabaseField
    private String imageUrl;
    @DatabaseField
    private BigDecimal price;
    @DatabaseField
    private String created_at;
    @DatabaseField
    private String updated_at;

    public Product() {
    }

    public int getId() {
        return id;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getUpdatedAt() {
        return updated_at;
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

    public Product update(MultiMap formParams, boolean validate) {
        description = formParams.get("description");
        imageUrl = formParams.get("imageUrl");
        if (validate) {
            String s = formParams.get("title");
            if (s == null || s.isEmpty()) {
                throw new IllegalArgumentException("Product must have a title");
            }
            title = s;
            price = new BigDecimal(formParams.get("price")).setScale(2, RoundingMode.CEILING);
        } else {
            title = formParams.get("title");
            try {
                price = new BigDecimal(formParams.get("price")).setScale(2, RoundingMode.CEILING);
            } catch(Exception e) {
                // ignore
            }
        }
        return this;
    }
}

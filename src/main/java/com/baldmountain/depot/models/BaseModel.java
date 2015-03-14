package com.baldmountain.depot.models;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

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
class BaseModel {
    private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    protected boolean dirty = false;

    protected String id;
    protected Date createdOn;
    protected Date updatedOn;

    BaseModel() {
        createdOn = new Date();
    }

    BaseModel(JsonObject json) {
        id = json.getString("_id");
        String s = json.getString("createdOn");
        if (s != null) {
            try {
                createdOn = dateFormat.parse(s);
            } catch (ParseException e) {
                // bad date, update to now
                createdOn = new Date();
            } catch (NumberFormatException e) {
                // bug in the date parser
                createdOn = new Date();
            }
        } else {
            createdOn = new Date();
        }
        s = json.getString("updatedOn");
        if (s != null && !s.isEmpty()) {
            try {
                updatedOn = dateFormat.parse(s);
            } catch (ParseException e) {
                // just ignore
            } catch (NumberFormatException e) {
                // just ignore
            }
        }
    }

    // only call this if the item is actually dirty
    void setDates(JsonObject json) {
        json.put("createdOn", dateFormat.format(createdOn));
        setUpdatedOn();
        if (updatedOn != null) {
            json.put("updatedOn", dateFormat.format(updatedOn));
        } else {
            json.put("updatedOn", "");
        }
    }

    public String getId() {
        return id;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn() {
        updatedOn = new Date();
        dirty = true;
    }


}

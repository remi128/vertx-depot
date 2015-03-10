package com.baldmountain.depot.models;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by gclements on 3/7/15.
 *
 */
class BaseModel {
    private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

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
            }
        } else {
            createdOn = new Date();
        }
        s = json.getString("updatedOn");
        if (s != null) {
            try {
                updatedOn = dateFormat.parse(s);
            } catch (ParseException e) {
                // just ignore
            }
        }
    }

    void setDates(JsonObject json) {
        json.put("createdOn", dateFormat.format(createdOn));
        updatedOn = new Date();
        json.put("updatedOn", dateFormat.format(updatedOn));
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


}

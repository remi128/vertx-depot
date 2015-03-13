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

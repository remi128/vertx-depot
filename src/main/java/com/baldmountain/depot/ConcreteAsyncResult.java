package com.baldmountain.depot;

import io.vertx.core.AsyncResult;

/**
 * Created by gclements on 3/11/15.
 */
public class ConcreteAsyncResult<T> implements AsyncResult<T> {
    private final T result;
    private final boolean succeeded;

    public ConcreteAsyncResult(T t) {
        result = t;
        this.succeeded = true;
    }

    public ConcreteAsyncResult(Throwable t) {
        result = null;
        this.succeeded = false;
    }


    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return null;
    }

    @Override
    public boolean succeeded() {
        return succeeded;
    }

    @Override
    public boolean failed() {
        return !succeeded();
    }
}

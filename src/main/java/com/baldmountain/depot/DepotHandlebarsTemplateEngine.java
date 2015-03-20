/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package com.baldmountain.depot;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.impl.Utils;
import io.vertx.ext.apex.templ.HandlebarsTemplateEngine;
import io.vertx.ext.apex.templ.impl.CachingTemplateEngine;

import java.io.IOException;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class DepotHandlebarsTemplateEngine extends CachingTemplateEngine<Template> implements HandlebarsTemplateEngine {

  private final Handlebars handlebars;
  private final Loader loader = new Loader();

  static DepotHandlebarsTemplateEngine create() {
    return new DepotHandlebarsTemplateEngine();
  }

  public DepotHandlebarsTemplateEngine() {
    super(HandlebarsTemplateEngine.DEFAULT_TEMPLATE_EXTENSION, HandlebarsTemplateEngine.DEFAULT_MAX_CACHE_SIZE);
    handlebars = new Handlebars(loader);
  }

  @Override
  public DepotHandlebarsTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public DepotHandlebarsTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return null;
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    try {
      Template template = cache.get(templateFileName);
      if (template == null) {
        synchronized (this) {
          loader.setVertx(context.vertx());
          template = handlebars.compile(templateFileName);
          cache.put(templateFileName, template);
        }
      }
      handler.handle(Future.succeededFuture(Buffer.buffer(template.apply(context.data()))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  public Handlebars getHandlebars() {
    return handlebars;
  }

  private class Loader implements TemplateLoader {

    private Vertx vertx;

    void setVertx(Vertx vertx) {
      this.vertx = vertx;
    }

    @Override
    public TemplateSource sourceAt(String location) throws IOException {

      String loc = adjustLocation(location);
      String templ = Utils.readFileToString(vertx, loc);

      if (templ == null) {
        throw new IllegalArgumentException("Cannot find resource " + loc);
      }

      long lastMod = System.currentTimeMillis();

      return new TemplateSource() {
        @Override
        public String content() throws IOException {
          // load from the file system
          return templ;
        }

        @Override
        public String filename() {
          return loc;
        }

        @Override
        public long lastModified() {
          return lastMod;
        }
      };
    }

    @Override
    public String resolve(String location) {
      return location;
    }

    @Override
    public String getPrefix() {
      return null;
    }

    @Override
    public String getSuffix() {
      return extension;
    }

    @Override
    public void setPrefix(String prefix) {

    }

    @Override
    public void setSuffix(String suffix) {
      extension = suffix;
    }
  }
}

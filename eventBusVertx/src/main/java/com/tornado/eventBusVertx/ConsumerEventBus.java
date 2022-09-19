package com.tornado.eventBusVertx;

import io.vertx.core.AbstractVerticle;

public class ConsumerEventBus extends AbstractVerticle {

  private String name = null;

  public ConsumerEventBus(String name) {
    this.name = name;
  }

  @Override
  public void start() throws Exception {

    vertx.eventBus().consumer("hello.vertx.addr", msg -> {
      msg.reply("Consumer Name: "+this.name+". Hello Vert.x World");
    });

    vertx.eventBus().consumer("hello.named.addr", msg -> {
      msg.reply("Consumer Name: "+this.name+". Hello Vert.x World ... " + msg.body());
    });

    vertx.eventBus().consumer("hello.named.publishMessage", msg -> {
      System.out.println("Consumer Publish with Consumer Name: "+this.name+". Hello Vert.x World ... " + msg.body() );
      msg.reply("Consumer Publish with Consumer Name: "+this.name+". Hello Vert.x World ... " + msg.body() );
    });

    vertx.eventBus().consumer("hello.named.sendMessage", msg -> {
      System.out.println("Consumer Send with Consumer Name: "+this.name+". Hello Vert.x World ... " + msg.body());
    });
  }


}

package com.tornado.eventBusVertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainRouteVerticle extends AbstractVerticle{

  @Override
  public void start() throws Exception {

    Router router = Router.router(vertx);
    router.get("/api/v1/hello").handler(this::helloWorld);
    router.get("/api/v1/hello/:name").handler(this::helloWorldName);

    router.get("/api/v1/hello/send/:name").handler(this::sendMessage);
//    publisher work right but with error.
    router.get("/api/v1/hello/publish/:name").handler(this::publishMessage);

    vertx.createHttpServer().requestHandler(router).listen(8888);
  }


  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    /*
    * Creating 2 instance of ConsumerEventBus to test publish and send
    * The publish method sends the message to all verticles listening on a given address.
    * The send() method sends the message to just one of the listening verticles
    */
    vertx.deployVerticle(new ConsumerEventBus("C1"));
    vertx.deployVerticle(new ConsumerEventBus("C2"));

    vertx.deployVerticle(new MainRouteVerticle())
      .onSuccess((res) -> System.out.println("successfully deployed"));
  }


  private void helloWorld(RoutingContext routingContext) {
    vertx.eventBus().request("hello.vertx.addr", "", reply -> {
      routingContext.request().response().end((String) reply.result().body());
    });
  }

  private void helloWorldName(RoutingContext routingContext) {
    vertx.eventBus().request("hello.named.addr", routingContext.pathParam("name"), reply -> {
      routingContext.request().response().end((String) reply.result().body());
    });
  }

  private void sendMessage(RoutingContext routingContext) {
    vertx.eventBus().send("hello.named.sendMessage", routingContext.pathParam("name"));
  }


  private void publishMessage(RoutingContext routingContext) {
    vertx.eventBus().publish("hello.named.publishMessage", routingContext.pathParam("name"));
  }


}

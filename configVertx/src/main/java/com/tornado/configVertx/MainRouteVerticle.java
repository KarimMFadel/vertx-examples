package com.tornado.configVertx;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainRouteVerticle extends AbstractVerticle {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		vertx.deployVerticle(new MainRouteVerticle()).onSuccess((res) -> System.out.println("successfully deployed"));
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Router router = Router.router(vertx);
		router.get("/api/v1/hello").handler(this::helloWorld);
		router.get("/api/v1/hello/:name").handler(this::helloWorldName);

		ConfigStoreOptions defaultConfig = new ConfigStoreOptions().setType("file").setFormat("json")
				.setConfig(new JsonObject().put("path", "config.json"));

		ConfigRetrieverOptions opts = new ConfigRetrieverOptions().addStore(defaultConfig);

		ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);

		Promise<JsonObject> configPromise = Promise.promise();
		cfgRetriever.getConfig(configPromise);

		configPromise.future().onSuccess(configResult -> {

			vertx.createHttpServer().requestHandler(router).listen(getPort(configResult));
			startPromise.complete();

		}).onFailure(exception -> startPromise.fail("Failed to load Configuration + " + exception.getMessage()));
	}

	private void helloWorld(RoutingContext routingContext) {
		routingContext.request().response().end("Hello Vert.x World");
	}

	private void helloWorldName(RoutingContext routingContext) {
		routingContext.request().response().end("Hello Vert.x World ... " + routingContext.pathParam("name"));
	}

	private int getPort(JsonObject configResult) {
		JsonObject http = configResult.getJsonObject("http");
		return http.getInteger("port");
	}

}

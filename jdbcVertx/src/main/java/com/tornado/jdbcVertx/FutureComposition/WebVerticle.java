package com.tornado.jdbcVertx.FutureComposition;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class WebVerticle extends AbstractVerticle {

	private final static String SECRET_AUTH_TOKEN = "mySuperSecretAuthToken";

	@Override
	public void start() throws Exception {
//		configureRouter()
//		.compose(this::startHttpServer)
//		.onSuccess(event -> System.out.println("WebVerticle is working good"))
//		.onFailure(exception -> System.err.println("WebVerticle - There is an exception happended :" + exception));
		

		vertx.createHttpServer().requestHandler(configureRouter()).listen(getPort(config()));
	}

	private Future<HttpServer> startHttpServer(Router router) {
		// java.lang.IllegalStateException: Set request or WebSocket handler first
		HttpServer httpServer = vertx.createHttpServer().requestHandler(router);
		return Future.<HttpServer>future(promise -> httpServer.listen(getPort(config()), promise));
	}

	private Router configureRouter() {
		Router router = Router.router(vertx);
		setChainedRoutes(router);
		router.get("/api/v1/hello").handler(this::helloWorld);
		router.get("/api/v1/hello/:name").handler(this::helloWorldName);
		setStaticRoutes(router);

		// Promise.succeededFuture(router).future();
//		return Future.succeededFuture(router);
		return router;
	}

	/**
	 * Make sure that every request has a auth_token with the headers
	 * 
	 * @param router
	 */
	private void setChainedRoutes(Router router) {
		// chained routes
		router.route().handler(ctx -> {
			String authToken = ctx.request().getHeader("AUTH_TOKEN");
			if (authToken != null && SECRET_AUTH_TOKEN.equals(authToken))
				ctx.next();
			else {
				HttpResponseStatus unauthorizedStatus = HttpResponseStatus.UNAUTHORIZED;
				ctx.response().setStatusCode(unauthorizedStatus.code())
						.setStatusMessage(unauthorizedStatus.reasonPhrase()).end();
			}
		});
	}

	/**
	 * Set Static Route, if there is no matching with any path then will redirect to
	 * index.html page
	 * 
	 * @param router
	 */
	private void setStaticRoutes(Router router) {
		router.route().handler(StaticHandler.create("web")); // .setIndexPage("index.html")
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

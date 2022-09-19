package com.tornado.jdbcVertx;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class MainJdbcVerticle extends AbstractVerticle {

	private final static String SECRET_AUTH_TOKEN = "mySuperSecretAuthToken";

	SQLClient client;

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MainJdbcVerticle()).onSuccess((res) -> System.out.println("successfully deployed"));
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		vertx.executeBlocking(this::doDatabaseMigrations, result -> {
			if (result.failed())
				startPromise.fail(result.cause());
			if (result.succeeded())
				System.out.println("connected to DB");
		});

		Router router = Router.router(vertx);

		setChainedRoutes(router);

		router.get("/api/v1/hello").handler(this::helloWorld);
		router.get("/api/v1/hello/:name").handler(this::helloWorldName);

		setStaticRoutes(router);

		doConfig(startPromise, router);

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

	void doConfig(Promise<Void> startPromise, Router router) {
		ConfigStoreOptions defaultConfig = new ConfigStoreOptions().setType("file").setFormat("json")
				.setConfig(new JsonObject().put("path", "config.json"));
//    ConfigStoreOptions cliConfig = new ConfigStoreOptions()
//      .setType("json")
//      .setConfig(config());

		ConfigRetrieverOptions opts = new ConfigRetrieverOptions().addStore(defaultConfig);
//      .addStore(cliConfig);

		ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);

		Promise<JsonObject> configPromise = Promise.promise();
		cfgRetriever.getConfig(configPromise);

		configPromise.future().onSuccess(configResult -> {

			vertx.createHttpServer().requestHandler(router).listen(getPort(configResult));
			startPromise.complete();

		}).onFailure(exception -> startPromise.fail("Failed to load Configuration + " + exception.getMessage()));

//    return Future.<JsonObject>future(promise -> cfgRetriever.getConfig(promise));
	}

	void doDatabaseMigrations(Promise<Void> startPromise) {
		Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://127.0.0.1:5432/todo", "postgres", "Pa$$w0rd")
				.load();

		try {
			flyway.migrate();
			startPromise.complete();
		} catch (FlywayException e) {
			startPromise.fail(e);
		}
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

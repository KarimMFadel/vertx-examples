package com.tornado.jdbcVertx.FutureComposition;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;

public class MainJdbcVerticleSeqComp extends AbstractVerticle {

	// to store the configuration and avoid path it with all methods.
	final JsonObject loadedConfig = new JsonObject();

	SQLClient client;

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MainJdbcVerticleSeqComp())
				.onSuccess((res) -> System.out.println("successfully deployed"));
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		/**
		 * Asynchronous/Future coordination
		 * Coordination of multiple futures can be achieved with Vert.x futures. 
		 * It supports concurrent composition (run several async operations in parallel)
		 *  and sequential composition (chain async operations).
		 * 
		 * # Sequential Composition - Do A, then B, then C . . . . Handle errors
		 * # Concurrent Composition - Do A, B, C and D once all/any complete - Do something else...
		 */
		
		// Sequential Composition
		doConfig()
			.compose(this::storeConfig)
			.compose(this::deployOtherVerticles)
			.onSuccess(event -> System.out.println("all Operation are working good"))
			.onFailure(exception -> System.err.println("There is an exception happended :" + exception));

	}

	private Future<Void> deployOtherVerticles(Void unused) {
		DeploymentOptions opts = new DeploymentOptions().setConfig(loadedConfig);
		
		// loading other verticles (Groovy/JS/...)
		Future<String> webVerticle = Future.future(promise -> vertx.deployVerticle(new WebVerticle(), opts, promise));
		Future<String> dbVerticle = Future.future(promise -> vertx.deployVerticle(new DatabaseVerticle(), opts, promise));
		Future<String> helloJs = Future.future(promise -> vertx.deployVerticle("Hello.js", opts, promise));
		
		// # Concurrent Composition 
		return CompositeFuture.all(helloJs, dbVerticle, webVerticle).mapEmpty();
	}

	

	private Future<Void> storeConfig(JsonObject future) {
		loadedConfig.mergeIn(future);
		return Future.succeededFuture();
	}

	Future<JsonObject> doConfig() {
		ConfigStoreOptions defaultConfig = new ConfigStoreOptions().setType("file").setFormat("json")
				.setConfig(new JsonObject().put("path", "config.json"));
		ConfigStoreOptions cliConfig = new ConfigStoreOptions().setType("json").setConfig(config());
		ConfigRetrieverOptions opts = new ConfigRetrieverOptions().addStore(defaultConfig).addStore(cliConfig);
		ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);

		return Future.future(promise -> cfgRetriever.getConfig(promise));
	}

}

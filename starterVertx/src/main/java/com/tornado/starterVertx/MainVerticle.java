package com.tornado.starterVertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		
		vertx.deployVerticle(new MainVerticle()).onSuccess((res) -> System.out.println("successfully deployed"));
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		vertx.createHttpServer().requestHandler(req -> {
			if (req.path().startsWith("/api/v1/hello"))
				req.response().putHeader("content-type", "text/plain").end("Special Page from Vert.x!");
			else
				req.response().putHeader("content-type", "text/plain").end("Main Hello Page from Vert.x!");
		}).listen(8888, http -> {
			if (http.succeeded()) {
				startPromise.complete();
				System.out.println("HTTP server started on port 8888");
			} else {
				startPromise.fail(http.cause());
			}
		});
	}
}

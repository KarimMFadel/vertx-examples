package com.tornado.jdbcVertx.FutureComposition;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class DatabaseVerticle extends AbstractVerticle{

	@Override
	public void start() throws Exception {
		doDatabaseMigrations()
			.onSuccess(event -> System.out.println("DatabaseVerticle - connected to DB"))
			.onFailure(exception -> System.err.println("DatabaseVerticle - Couldn't connected to DB"));
	}
	


	Future<Void> doDatabaseMigrations() {
		JsonObject dbConfig = config().getJsonObject("db", new JsonObject());
		String url = dbConfig.getString("url", "jdbc:postgresql://127.0.0.1:5432/todo");
		String adminUser = dbConfig.getString("admin_user", "postgres");
		String adminPass = dbConfig.getString("admin_password", "Pa$$w0rd");
		Flyway flyway = Flyway.configure().dataSource(url, adminUser, adminPass).load();

		try {
			flyway.migrate();
			return Future.succeededFuture();
		} catch (FlywayException e) {
			return Future.failedFuture(e);
		}
	}
}

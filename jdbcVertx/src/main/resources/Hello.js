vertx.eventBus().consumer("hello.vertx.addr", function(msg) {
	msg.reply("Consumer Name: " + this.name + ". Hello Vert.x World");
});
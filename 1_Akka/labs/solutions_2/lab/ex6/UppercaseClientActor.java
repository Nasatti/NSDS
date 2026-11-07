package com.lab.ex6;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class UppercaseClientActor extends AbstractActor {

	private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
	private static final int NUM_REQUESTS = 4;

	private ActorRef server;
	private int replies = 0;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ConfigMsg.class, this::configure).build();
	}

	void configure(ConfigMsg msg) {
		System.out.println("CLIENT: Received configuration message!");
		server = msg.getServerRef();
		startTest();
	}

	void startTest() {
		// Issue several requests without waiting for their responses
		// The server can process only one; it must stash the remaining ones
		askUppercase("hello " + self() );
		askUppercase("akka " + self() );
		askUppercase("actor model " + self() );
		askUppercase("last request " + self() );
	}

	void askUppercase(String text) {
		System.out.println("CLIENT: Asking to uppercase: " + text);

		scala.concurrent.Future<Object> waitingForReply = ask(server, new TextMsg(text), 5000);

		try {
			ReplyMsg reply = (ReplyMsg) waitingForReply.result(timeout, null);
			System.out.println("CLIENT: Received reply: " + reply.getText());
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static Props props() {
		return Props.create(UppercaseClientActor.class);
	}
}

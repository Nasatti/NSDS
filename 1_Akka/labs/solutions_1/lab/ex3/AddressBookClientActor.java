package com.lab.ex3;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeoutException;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class AddressBookClientActor extends AbstractActor {

	private ActorRef server;
	private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ReplyMsg.class, this::processReply).match(ConfigMsg.class, this::configure).match(PutMsg.class, this::putEntry).match(GetMsg.class, this::query).build();
	}

	void putEntry(PutMsg msg) {
		System.out.println("CLIENT: Sending new entry " + msg.getName() + " - " + msg.getEmail());
		server.tell(msg, self());
	}

	void query(GetMsg msg) {
		System.out.println("CLIENT: Issuing query for " + msg.getName());
		scala.concurrent.Future<Object> waitingForReply = ask(server, msg, 5000);
		try {
			ReplyMsg reply = (ReplyMsg) waitingForReply.result(timeout, null);
			if (reply.getEmail() != null) {
				System.out.println("CLIENT: Received reply, email is " + reply.getEmail());
			} else {
				System.out.println("CLIENT: Received reply, no email found!");
			}
		} catch (TimeoutException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void startTest() {
		server.tell(new PutMsg("Luca", "luca.mottola@polimi.it"), self());
		server.tell(new PutMsg("Alessandro", "alessandro.margara@polimi.it"), self());

		self().tell(new GetMsg("Alessandro"), self());
		self().tell(new GetMsg("Gianpaolo"), self());

	}

	void processReply(ReplyMsg msg) {
		System.out.println("CLIENT: Unsolicited reply, this should not happen!");
	}

	void configure(ConfigMsg msg) {
		System.out.println("CLIENT: Received configuration message!");
		server = msg.getServerRef();
		startTest();
	}

	static Props props() {
		return Props.create(AddressBookClientActor.class);
	}

}

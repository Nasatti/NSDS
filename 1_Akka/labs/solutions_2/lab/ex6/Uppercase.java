package com.lab.ex6;

import java.io.IOException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Uppercase {

    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef server = sys.actorOf(UppercaseServerActor.props(), "server");
        final ActorRef client1 = sys.actorOf(UppercaseClientActor.props(), "client1");
        final ActorRef client2 = sys.actorOf(UppercaseClientActor.props(), "client2");

        // Tell the client who the server is, and start the test
        client1.tell(new ConfigMsg(server), ActorRef.noSender());
        client2.tell(new ConfigMsg(server), ActorRef.noSender());

    	// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sys.terminate();
    }
}

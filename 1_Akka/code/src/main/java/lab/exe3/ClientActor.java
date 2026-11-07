package lab.exe3;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ClientActor extends AbstractActor {


	public ClientActor() {

	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ReplyMsg.class, this::onReply).build();
	}

	void onReply(ReplyMsg msg) {
	    if (msg.getEmail() != null) {
	        System.out.println("CLIENT: Ricevuta email dal server -> " + msg.getEmail());
	    } else {
	        System.out.println("CLIENT: Il server ha risposto che il contatto non esiste!");
	    }
	}

	static Props props() {
		return Props.create(ClientActor.class);
	}

}

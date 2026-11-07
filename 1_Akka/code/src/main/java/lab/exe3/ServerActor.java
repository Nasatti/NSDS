package lab.exe3;

import java.util.HashMap;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ServerActor extends AbstractActor{

	private HashMap<String, String> contacts;

	public ServerActor() {
		this.contacts = new HashMap<>();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(PutMsg.class, this::onPut).match(GetMsg.class, this::onGet).build();
	}

	void onPut(PutMsg msg) {
		contacts.put(msg.getName(), msg.getEmail());
		System.out.println("SERVER: Added contact: " + msg.getName() + " with address: " + msg.getEmail());
	}

	void onGet(GetMsg msg) {
		System.out.println("SERVER: Received request for contact: " + msg.getName());
		String address = contacts.get(msg.getName());
		ReplyMsg reply = new ReplyMsg(address);
		getSender().tell(reply, self());
	}

	static Props props() {
		return Props.create(ServerActor.class);
	}

}

package lab.exe5;

import java.util.HashMap;
import akka.actor.AbstractActor;
import akka.actor.Props;

public class AddressBookServerActor extends AbstractActor {

	private final HashMap<String, String> addresses;

	public AddressBookServerActor() {
		System.out.println("SERVER: Constructor called (state initialized).");
		this.addresses = new HashMap<>();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GetMsg.class, this::generateReply)
				.match(PutMsg.class, this::storeEntry)
				.build();
	}

	private void generateReply(GetMsg msg) {
		System.out.println("SERVER: Received query for name: " + msg.getName());
		String email = addresses.get(msg.getName());
		sender().tell(new ReplyMsg(email), self());
	}

	private void storeEntry(PutMsg msg) throws Exception {
		// Iniezione del guasto richiesta dalla consegna
		if ("Fail!".equals(msg.getName())) {
			System.out.println("SERVER: Simulating a crash on Fail! message...");
			throw new Exception("Server artificial crash!");
		} else {
			System.out.println("SERVER: Storing entry: " + msg.getName() + " -> " + msg.getEmail());
			addresses.put(msg.getName(), msg.getEmail());
		}
	}

	public static Props props() {
		return Props.create(AddressBookServerActor.class);
	}
}
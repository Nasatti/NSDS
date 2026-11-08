package lab.exe5;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeoutException;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class AddressBookClientActor extends AbstractActor {

	private ActorRef server;
	private final Duration timeout = Duration.create(5, SECONDS);

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ConfigMsg.class, this::configure)
				.match(PutMsg.class, this::putEntry)
				.match(GetMsg.class, this::query)
				.match(ReplyMsg.class, this::processReply)
				.build();
	}

	private void configure(ConfigMsg msg) {
		System.out.println("CLIENT: Received server reference.");
		this.server = msg.getServerRef();
	}

	private void putEntry(PutMsg msg) {
		System.out.println("CLIENT: Sending PUT: " + msg.getName() + " - " + msg.getEmail());
		server.tell(msg, self());
	}

	private void query(GetMsg msg) {
		System.out.println("CLIENT: Issuing GET for " + msg.getName());
		// Pattern ASK: attende la risposta via Future
		Future<Object> waitingForReply = ask(server, msg, 5000);
		try {
			ReplyMsg reply = (ReplyMsg) waitingForReply.result(timeout, null);
			if (reply.getEmail() != null) {
				System.out.println("CLIENT: Reply received! Email for " + msg.getName() + " is: " + reply.getEmail());
			} else {
				System.out.println("CLIENT: Reply received! No email found for " + msg.getName());
			}
		} catch (TimeoutException | InterruptedException e) {
			System.err.println("CLIENT: Timeout or interruption waiting for reply!");
		}
	}

	private void processReply(ReplyMsg msg) {
		System.out.println("CLIENT: Unsolicited reply received.");
	}

	public static Props props() {
		return Props.create(AddressBookClientActor.class);
	}
}
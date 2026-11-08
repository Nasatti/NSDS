package lab.exe6;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeoutException;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class UppercaseClientActor extends AbstractActor {

	private final Duration timeout = Duration.create(10, SECONDS);
	private ActorRef server;

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ConfigMsg.class, this::configure)
				.build();
	}

	private void configure(ConfigMsg msg) {
		this.server = msg.getServerRef();
		System.out.println("CLIENT " + self().path().name() + ": Configured. Starting requests!");
		startTest();
	}

	private void startTest() {
		// Invia 4 richieste in sequenza
		askUppercase("hello " + self().path().name());
		askUppercase("akka " + self().path().name());
		askUppercase("actor model " + self().path().name());
		askUppercase("last request " + self().path().name());
	}

	private void askUppercase(String text) {
		System.out.println("CLIENT " + self().path().name() + ": Asking: " + text);

		Future<Object> waitingForReply = ask(server, new TextMsg(text), 10000);

		try {
			ReplyMsg reply = (ReplyMsg) waitingForReply.result(timeout, null);
			System.out.println("CLIENT " + self().path().name() + ": Got reply: " + reply.getText());
		} catch (TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Props props() {
		return Props.create(UppercaseClientActor.class);
	}
}
package lab.exe4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class EchoClientActor extends AbstractActor {

	private ActorRef server;

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ConfigMsg.class, this::configure)
				.match(TextMsg.class, this::sendText)
				.match(SleepMsg.class, this::reroute)
				.match(WakeupMsg.class, this::reroute)
				.build();
	}

	/**
	 * client sa con che server comunicare
	 * @param msg
	 */
	private void configure(ConfigMsg msg) {
		System.out.println("CLIENT: Configured with server reference.");
		this.server = msg.getServerRef();
	}

	/**
	 * inoltra i messaggi di controllo (SleepMsg, WakeupMsg) al server
	 * @param msg
	 */
	private void reroute(Msg msg) {
		System.out.println("CLIENT: Rerouting control message " + msg.getClass().getSimpleName() + " to server");
		server.tell(msg, self());
	}

	/**
	 * se sender = null o noSender, msg inviato dal main --> inoltra al server
	 * altrimenti msg di risposta dal server --> stampa a video
	 * @param msg
	 */
	private void sendText(TextMsg msg) {
		if (msg.getSender() == ActorRef.noSender() || msg.getSender() == null) {
			// Messaggio inviato dall'esterno (main), inoltriamolo al server
			System.out.println("CLIENT: Sending text to server: " + msg.getText());
			msg.setSender(self());
			server.tell(msg, self());
		} else {
			// Risposta di echo tornata dal server
			System.out.println("CLIENT: Received reply from server: " + msg.getText());
		}
	}

	public static Props props() {
		return Props.create(EchoClientActor.class);
	}
}
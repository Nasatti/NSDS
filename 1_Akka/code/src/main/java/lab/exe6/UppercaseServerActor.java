package lab.exe6;

import java.time.Duration;
import java.util.Locale;
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class UppercaseServerActor extends AbstractActorWithStash {

	// Ritardo simulato per ogni richiesta
	private static final Duration PROCESSING_DELAY = Duration.ofMillis(1000);

	@Override
	public Receive createReceive() {
		return ready();
	}

	// STATO 1: PRONTO A RICEVERE
	private Receive ready() {
		return receiveBuilder()
				.match(TextMsg.class, this::startProcessing)
				.build();
	}

	// STATO 2: OCCUPATO AD ELABORARE
	private Receive busy() {
		return receiveBuilder()
				.match(TextMsg.class, this::putAside)
				.match(ProcessingDoneMsg.class, this::finishProcessing)
				.build();
	}

	private void startProcessing(TextMsg msg) {
		System.out.println("SERVER: Starting processing for: \"" + msg.getText() + "\"");

		// Salviamo il richiedente originale (perché in ask() e' un attore temporaneo)
		ProcessingDoneMsg done = new ProcessingDoneMsg(msg.getText(), sender());

		// Cambiamo stato in busy: da ora le nuove richieste verranno messe in stash
		getContext().become(busy());

		// Programmiamo il completamento asincrono tra 1 secondo senza bloccare il thread
		getContext().system().scheduler().scheduleOnce(
				PROCESSING_DELAY,
				self(),
				done,
				getContext().dispatcher(),
				self());
		/*getContext().system().scheduler().scheduleOnce(
		 * 		delay,
		 * 		receiver,
		 * 		message,
		 * 		executor,getContext().dispatcher(), 
		 * 		sender);
		 */
	}

	private void putAside(TextMsg msg) {
		System.out.println("SERVER: Busy! Stashing request: \"" + msg.getText() + "\"");
		stash();
	}

	private void finishProcessing(ProcessingDoneMsg msg) {
		String uppercase = msg.getText().toUpperCase(Locale.ROOT);
		System.out.println("SERVER: Completed! Result: \"" + uppercase + "\"");

		// Rispondiamo al client originario
		msg.getRequester().tell(new ReplyMsg(uppercase), self());

		// Torniamo pronti e scongeliamo le richieste in coda
		getContext().become(ready());
		unstashAll();
	}

	public static Props props() {
		return Props.create(UppercaseServerActor.class);
	}
}
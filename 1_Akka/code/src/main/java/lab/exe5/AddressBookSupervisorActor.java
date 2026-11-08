package lab.exe5;

import java.time.Duration;
import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class AddressBookSupervisorActor extends AbstractActor {

	// Definiamo la strategia di supervisione applicata ai figli
	// SupervisorStrategy.resume() || SupervisorStrategy.restart()
	private static final SupervisorStrategy strategy = new OneForOneStrategy(
			1, // Numero massimo di tentativi di riavvio
			Duration.ofMinutes(1), // Finestra temporale
			DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart())
					.build());

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	@Override
	public Receive createReceive() {
		// Quando riceve i Props, crea il figlio all'interno del proprio contesto di supervisore
		return receiveBuilder()
				.match(Props.class, props -> {
					getSender().tell(getContext().actorOf(props), getSelf());
				})
				.build();
	}

	public static Props props() {
		return Props.create(AddressBookSupervisorActor.class);
	}
}
package lab.exe5;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class AddressBook {

	public static void main(String[] args) {
		final ActorSystem sys = ActorSystem.create("AddressBookSystem");
		final ActorRef client = sys.actorOf(AddressBookClientActor.props(), "client");

		Duration timeout = Duration.create(5, SECONDS);

		// 1. Creiamo prima il Supervisore
		final ActorRef supervisor = sys.actorOf(AddressBookSupervisorActor.props(), "supervisor");

		// 2. Chiediamo al supervisore di creare il Server come suo figlio
		Future<Object> waitingForServer = ask(supervisor, AddressBookServerActor.props(), 5000);
		ActorRef server = null;
		try {
			server = (ActorRef) waitingForServer.result(timeout, null);
		} catch (TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}

		// 3. Colleghiamo il Client al Server appena creato
		client.tell(new ConfigMsg(server), ActorRef.noSender());

		// 4. Inseriamo i contatti iniziali
		client.tell(new PutMsg("Luca", "luca.mottola@polimi.it"), ActorRef.noSender());
		client.tell(new PutMsg("Alessandro", "alessandro.margara@polimi.it"), ActorRef.noSender());

		client.tell(new GetMsg("Alessandro"), ActorRef.noSender());
		client.tell(new GetMsg("Gianpaolo"), ActorRef.noSender());

		// 5. ORA INIETTIAMO IL GUASTO!
		client.tell(new PutMsg("Fail!", "none"), ActorRef.noSender());

		/* 6. 
		 Se la strategia e' RESUME: trova luca.mottola@polimi.it (lo stato non e' stato azzerato)
		 Se la strategia e' RESTART: stampa "no email found" perche' la mappa e' stata resettata!
		 */
		client.tell(new GetMsg("Luca"), ActorRef.noSender());

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sys.terminate();
	}
}
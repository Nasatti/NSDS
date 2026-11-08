package lab.exe6;

import java.io.IOException;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Uppercase {

	public static void main(String[] args) {
		final ActorSystem sys = ActorSystem.create("UppercaseSystem");

		final ActorRef server = sys.actorOf(UppercaseServerActor.props(), "server");

		// Creiamo due client distinti per testare la concorrenza
		final ActorRef client1 = sys.actorOf(UppercaseClientActor.props(), "client1");
		final ActorRef client2 = sys.actorOf(UppercaseClientActor.props(), "client2");

		// Avviamo entrambi i client passandogli il server
		client1.tell(new ConfigMsg(server), ActorRef.noSender());
		client2.tell(new ConfigMsg(server), ActorRef.noSender());

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sys.terminate();
	}
}
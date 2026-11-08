package lab.exe4;

import java.io.IOException;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Echo {

	public static void main(String[] args) {
		//creazione sistema
		final ActorSystem sys = ActorSystem.create("EchoSystem");

		//creazione attori
		final ActorRef server = sys.actorOf(SleepyEchoServerActor.props(), "server");
		final ActorRef client = sys.actorOf(EchoClientActor.props(), "client");

		//configura il client indicandogli chi e' il server via ConfigMsg
		client.tell(new ConfigMsg(server), ActorRef.noSender());

		//inviamsg a server sveglio
		client.tell(new TextMsg("Hello Luca!", ActorRef.noSender()), ActorRef.noSender());
		client.tell(new TextMsg("Hello Alessandro!", ActorRef.noSender()), ActorRef.noSender());

		//manda a dormire il server
		client.tell(new SleepMsg(), ActorRef.noSender());

		//invia msg mentre il server dorme ( accantonati via stash)
		client.tell(new TextMsg("You should be sleeping now 1!", ActorRef.noSender()), ActorRef.noSender());
		client.tell(new TextMsg("You should be sleeping now 2!", ActorRef.noSender()), ActorRef.noSender());

		//risveglia il server (msg accantonati verranno processati in ordine)
		client.tell(new WakeupMsg(), ActorRef.noSender());

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sys.terminate();
	}
}
package lab.exe3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class ContactList {


	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef client = sys.actorOf(ClientActor.props(), "client");
		final ActorRef server = sys.actorOf(ServerActor.props(), "server");
		
		server.tell(new PutMsg("Alice", "alice@gmail.com"), client);
		System.out.println("CLIENT: Inviata richiesta di aggiunta contatto Alice al server");
		
		server.tell(new GetMsg("Alice"), client);
		System.out.println("CLIENT: Inviata richiesta di recupero contatto Alice al server");
		
		server.tell(new GetMsg("Bob"), client);
		System.out.println("CLIENT: Inviata richiesta di recupero contatto Bob al server");
		
		try {
	        System.in.read(); // Premi Invio per terminare
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		
		sys.terminate();

	}

}

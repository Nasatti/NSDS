package lab.exe2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Counter {


	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef counter = sys.actorOf(CounterActor.props(), "counter");

		try {
	        counter.tell(new DecrementMessage(), ActorRef.noSender());
	        Thread.sleep(100);
	        counter.tell(new DecrementMessage(), ActorRef.noSender());
	        Thread.sleep(100);
	        counter.tell(new IncrementMessage(), ActorRef.noSender());
	        Thread.sleep(100);
	        counter.tell(new DecrementMessage(), ActorRef.noSender());
	        Thread.sleep(100);
	        counter.tell(new IncrementMessage(), ActorRef.noSender());
	        Thread.sleep(100);
	        counter.tell(new IncrementMessage(), ActorRef.noSender());
	        Thread.sleep(100);
	        System.in.read(); // Premi Invio per terminare
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		
		sys.terminate();

	}

}

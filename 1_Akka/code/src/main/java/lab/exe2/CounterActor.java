package lab.exe2;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class CounterActor extends AbstractActorWithStash {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
        return receiveBuilder()
            .match(IncrementMessage.class, this::onIncrement)
            .match(DecrementMessage.class, this::onDecrement)
            .build();
    }
	void onIncrement(IncrementMessage msg) {
		++counter;
		unstashAll();
		System.out.println("Counter increased to " + counter);
	}
	
	void onDecrement(DecrementMessage msg) {
	    if (counter == 0) {
	        System.out.println("Counter a 0! Decremento accantonato nello Stash...");
	        stash();
	    } else {
	        --counter;
	        System.out.println("Counter decreased to " + counter);
	    }
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}

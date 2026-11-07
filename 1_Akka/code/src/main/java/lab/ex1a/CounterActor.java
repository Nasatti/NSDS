package lab.ex1a;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class CounterActor extends AbstractActor {

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
		System.out.println("Counter increased to " + counter);
	}
	
	void onDecrement(DecrementMessage msg) {
		--counter;
		System.out.println("Counter decreased to " + counter);
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}

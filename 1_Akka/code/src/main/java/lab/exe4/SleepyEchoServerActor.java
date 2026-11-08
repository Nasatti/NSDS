package lab.exe4;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class SleepyEchoServerActor extends AbstractActorWithStash {

	@Override
	public Receive createReceive() {
		return awake();
	}

	/**
	 *  Comportamento quando e' SVEGLIO
	 * @return
	 */
	private Receive awake() {
		return receiveBuilder()
				.match(TextMsg.class, this::echo)
				.match(SleepMsg.class, this::goToSleep)
				.build();
	}

	/**
	 *  Comportamento quando e' ADDORMENTATO
	 * @return
	 */
	private Receive sleepy() {
		return receiveBuilder()
				.match(TextMsg.class, this::putAside)
				.match(WakeupMsg.class, this::wakeup)
				.build();
	}

	/**
	 * AWAKE
	 *  Echo del messaggio ricevuto
	 * @param msg
	 */
	private void echo(TextMsg msg) {
		System.out.println("SERVER: Echo-ing back msg to " + msg.getSender() + " with text: " + msg.getText());
		msg.getSender().tell(msg, self());
	}

	/**
	 * AWAKE
	 *  Passa a stato ADDORMENTATO
	 * @param msg
	 */
	private void goToSleep(SleepMsg msg) {
		System.out.println("SERVER: Going to sleep...");
		getContext().become(sleepy());
	}
	
	
	/**
	 * SLEEPY
	 *  Accantona il messaggio ricevuto
	 * @param msg
	 */
	private void putAside(TextMsg msg) {
		System.out.println("SERVER: Setting aside msg: " + msg.getText());
		stash();
	}

	
	/**
	 * SLEEPY
	 *  Risveglia il server, processa i messaggi accantonati
	 * @param msg
	 */
	private void wakeup(WakeupMsg msg) {
		System.out.println("SERVER: Waking up! Processing stashed messages...");
		unstashAll();
		getContext().become(awake());
	}

	public static Props props() {
		return Props.create(SleepyEchoServerActor.class);
	}
}
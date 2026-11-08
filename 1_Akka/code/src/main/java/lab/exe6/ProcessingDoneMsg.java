package lab.exe6;

import java.io.Serializable;
import akka.actor.ActorRef;

public class ProcessingDoneMsg implements Serializable {
	private final String text;
	private final ActorRef requester;

	public ProcessingDoneMsg(String text, ActorRef requester) {
		this.text = text;
		this.requester = requester;
	}

	public String getText() {
		return text;
	}

	public ActorRef getRequester() {
		return requester;
	}
}
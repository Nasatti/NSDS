package lab.exe4;

import akka.actor.ActorRef;

public class ConfigMsg extends Msg {
	private static final long serialVersionUID = 1L;

	private final ActorRef serverRef;

	public ConfigMsg(ActorRef serverRef) {
		this.serverRef = serverRef;
	}

	public ActorRef getServerRef() {
		return serverRef;
	}
}
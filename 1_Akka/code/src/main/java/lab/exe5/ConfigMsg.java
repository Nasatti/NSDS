package lab.exe5;

import java.io.Serializable;
import akka.actor.ActorRef;

public class ConfigMsg implements Serializable {
	private final ActorRef serverRef;

	public ConfigMsg(ActorRef serverRef) {
		this.serverRef = serverRef;
	}

	public ActorRef getServerRef() {
		return serverRef;
	}
}
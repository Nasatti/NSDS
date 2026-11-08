package lab.exe5;

import java.io.Serializable;

public class ReplyMsg implements Serializable {
	private final String email;

	public ReplyMsg(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
}
package lab.exe6;

import java.io.Serializable;

public class ReplyMsg implements Serializable {
	private final String text;

	public ReplyMsg(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
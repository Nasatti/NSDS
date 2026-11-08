package lab.exe6;

import java.io.Serializable;

public class TextMsg implements Serializable {
	private final String text;

	public TextMsg(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
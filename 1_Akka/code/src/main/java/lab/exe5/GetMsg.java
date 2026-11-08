package lab.exe5;

import java.io.Serializable;

public class GetMsg implements Serializable {
	private final String name;

	public GetMsg(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
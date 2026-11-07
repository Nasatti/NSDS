package lab.exe3;

public class PutMsg {
// add a new contact in the list
	private final String name;
	private final String email;
	
	public PutMsg(String name, String email) {
		this.name = name;
		this.email = email;
	}
	
	public String getName() {
		return name;
	}
	
	public String getEmail() {
		return email;
	}	
}

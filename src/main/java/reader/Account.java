package reader;

public class Account {
	private String hierID;
	private String accID;
	
	public Account(String hierID, String accID) {
		this.hierID = hierID;
		this.accID = accID;
	}
	
	public String getHierID() {
		return hierID;
	}
	public void setHierID(String hierID) {
		this.hierID = hierID;
	}
	public String getAccID() {
		return accID;
	}
	public void setAccID(String accID) {
		this.accID = accID;
	}

}

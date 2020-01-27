package hierarchy;

public class HierRecord {
	private String PARRENT_ACC_ID;
	private String ACC_ID;
	private String ACCTYPE_CATEGORY;
	private String ACC_NAME;
	private String ACC_NUMBER;
	
	public HierRecord(String PARRENT_ACC_ID, String ACC_ID, String ACCTYPE_CATEGORY, String ACC_NAME, String ACC_NUMBER) {
		this.PARRENT_ACC_ID = PARRENT_ACC_ID;
		this.ACC_ID = ACC_ID;
		this.ACCTYPE_CATEGORY = ACCTYPE_CATEGORY;
		this.ACC_NAME = ACC_NAME;
		this.ACC_NUMBER = ACC_NUMBER;
	}
	
	public String getPARRENT_ACC_ID() {
		return PARRENT_ACC_ID;
	}
	public String getACC_ID() {
		return ACC_ID;
	}
	public String getACCTYPE_CATEGORY() {
		return ACCTYPE_CATEGORY;
	}
	public String getACC_NAME() {
		return ACC_NAME;
	}
	public String getACC_NUMBER() {
		return ACC_NUMBER;
	}
}

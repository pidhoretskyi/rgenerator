package reader;

import java.sql.Date;

public class Interest implements Cloneable{
	
	private int accID;
	private String accName;
	private String ownerName;
	
	private String settlemNum;
	private String settleName;
	private String settleOwner;
	private String bankID;
	private String calcuType;
	private int calcuNum;
	private Date toDate;
	private String currency;
	private double interestCredit;
	private double interestDebit;
	private double bearingBalance;
	private double rate;
	private String basis;
	private String settleType;
	
	public Object clone(){
		Interest inter=null;
		try {
			inter = (Interest) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inter;
	}
	
	public int getAccID() {
		return accID;
	}
	public void setAccID(int accID) {
		this.accID = accID;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public String getAccName() {
		return accName;
	}
	public void setAccName(String accName) {
		this.accName = accName;
	}
	
	public String getSettlemNum() {
		return settlemNum;
	}
	public void setSettlemNum(String settlemNum) {
		this.settlemNum = settlemNum;
	}
	
	public String getSettleOwner() {
		return settleOwner;
	}
	public void setSettleOwner(String settleOwner) {
		this.settleOwner = settleOwner;
	}
	
	public String getSettleName() {
		return settleName;
	}
	public void setSettleName(String settleName) {
		this.settleName = settleName;
	}
	
	public String getBankID() {
		return bankID;
	}
	public void setBankID(String bankID) {
		this.bankID = bankID;
	}
	
	public String getCalcuType() {
		return calcuType;
	}
	public void setCalcuType(String calcuType) {
		this.calcuType = calcuType;
	}
	
	public int getCalcuNum() {
		return calcuNum;
	}
	public void setCalcuNum(int calcuNum) {
		this.calcuNum = calcuNum;
	}
	
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	
	public double getInterestCredit() {
		return interestCredit;
	}
	public void setInterestCredit(double interestCredit) {
		this.interestCredit = interestCredit;
	}
	
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public double getInterestDebit() {
		return interestDebit;
	}
	public void setInterestDebit(double interestDebit) {
		this.interestDebit = interestDebit;
	}
	
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	
	public double getBearingBalance() {
		return bearingBalance;
	}
	public void setBearingBalance(double bearingBalance) {
		this.bearingBalance = bearingBalance;
	}
	
	public String getBasis() {
		return basis;
	}
	public void setBasis(String basis) {
		this.basis = basis;
	}
	
	public String getSettleType() {
		return settleType;
	}
	public void setSettleType(String settleType) {
		this.settleType = settleType;
	}
	

}

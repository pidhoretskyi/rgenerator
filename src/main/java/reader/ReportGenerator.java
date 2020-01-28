package reader;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import com.ibm.db2.jcc.am.Connection;
import com.rgenerator.db.DbConnProvider;
import com.rgenerator.db.DbDataProvider;

public class ReportGenerator {

	private DbConnProvider serverACCT;
	private Connection connectionACCT;
	private DbDataProvider dataProviderACCT;
	private DbConnProvider serverCUST;
	private Connection connectionCUST;
	private DbDataProvider dataProviderCUST;

	public ArrayList<Interest> data;

	private String colum_accID = "Account Number";
	private String colum_accName = "Account Name";
	private String colum_ownerName = "Account Owner Name";
	private String colum_settlemNum = "Settlement Account Number";
	private String colum_settleName = "Settlement Account Name";
	private String colum_settleOwner = "Settlement Account Owner";
	private String colum_bankID = "Bank ID";
	private String colum_calcuType = "Interest type";
	private String colum_calcuNum = "Number of days";
	private String colum_toDate = "Calculation date";
	private String colum_currency = "Currency";
	private String colum_interestCredit = "Calculated Interest Credit";
	private String colum_interestDebit = "Calculated Interest Debit";
	private String colum_bearingBalance = "Interest Bearing Balance";
	private String colum_rate = "Total Interest Rate (%)";
	private String colum_basis = "Interest Basis";
	private String colum_settleType = "Settlement Type";

	public String getColum_accID() {
		return colum_accID;
	}

	public String getColum_accName() {
		return colum_accName;
	}

	public String getColum_ownerName() {
		return colum_ownerName;
	}

	public String getColum_settlemNum() {
		return colum_settlemNum;
	}

	public String getColum_settleOwner() {
		return colum_settleOwner;
	}

	public String getColum_bankID() {
		return colum_bankID;
	}

	public String getColum_settleName() {
		return colum_settleName;
	}

	public String getColum_calcuNum() {
		return colum_calcuNum;
	}

	public String getColum_calcuType() {
		return colum_calcuType;
	}

	public String getColum_currency() {
		return colum_currency;
	}

	public String getColum_toDate() {
		return colum_toDate;
	}

	public String getColum_interestCredit() {
		return colum_interestCredit;
	}

	public String getColum_bearingBalance() {
		return colum_bearingBalance;
	}

	public String getColum_interestDebit() {
		return colum_interestDebit;
	}

	public String getColum_rate() {
		return colum_rate;
	}

	public String getColum_basis() {
		return colum_basis;
	}

	public String getColum_settleType() {
		return colum_settleType;
	}

	private String getOwner(String account) throws SQLException {
		ResultSet owner = dataProviderACCT.getOwner(account);
		ResultSet ownerName;
		String name = "Empty";
		if (owner.next()) {
			String ownerID = owner.getString(1);
			ownerName = dataProviderCUST.getOwnerName(ownerID);
			if (ownerName.next())
				name = ownerName.getString(2);
		}
		return name;
	}

	private ArrayList<Interest> getRecords(String account, LocalDate fromDate, LocalDate toDate) {
		serverACCT = new DbConnProvider();
		serverACCT.connectionToFirstDB();
		connectionACCT = serverACCT.openConn();
		dataProviderACCT = new DbDataProvider(connectionACCT);

		serverCUST = new DbConnProvider();
		serverCUST.connectionToSecondtDB();
		connectionCUST = serverCUST.openConn();
		dataProviderCUST = new DbDataProvider(connectionCUST);

		String sSettlementNum = "Empty";
		String sSettlementName = "Empty";
		String settleOwner = "Empty";

		ArrayList<Interest> result = new ArrayList<Interest>();
		String currency = "";
		try {

			String ownerName = getOwner(account);
			ResultSet settlement;
			while (true) {
				settlement = dataProviderACCT.getSettlementAcc(account);

				if (settlement.next()) {
					sSettlementNum = settlement.getString(2);
					sSettlementName = settlement.getString(3);
					settleOwner = getOwner(settlement.getString(1));
					break;
				} else {
					ResultSet parent = dataProviderACCT.getParent(account);
					if (parent.next()) {
						account = parent.getString(1);
					} else {
						break;
					}
				}

			}

			ResultSet curr = dataProviderACCT.getCurrency(account);
			if (curr.next())
				currency = curr.getString(2);

			ResultSet data = dataProviderACCT.getDetailsData(account, fromDate.toString(), toDate.toString());
			while (data.next()) {
				Interest record = new Interest();
				record.setAccID(data.getInt(1));
				record.setAccName(data.getString(2));
				record.setBankID(data.getString(3));
				record.setCalcuType(data.getString(4));
				record.setCalcuNum(data.getInt(5));
				record.setToDate(data.getDate(6));
				record.setCurrency(currency);
				System.out.println(data.getString(12));
				if (data.getString(12).equals("BCRB1")) {
					
					if (currency == "JPY")
						record.setInterestCredit(Double.valueOf(data.getInt(7)) / 100);
					else
						record.setInterestCredit(Double.valueOf(data.getInt(7)) / 10000);
					record.setInterestDebit(0);
				} else if (data.getString(12).equals("BDEB1") || data.getString(12).equals("CRED1")
						|| data.getString(12).equals("BUOD1")) {
					if (currency == "JPY")
						record.setInterestDebit(Double.valueOf(data.getInt(7)) / 100);
					else
						record.setInterestDebit(Double.valueOf(data.getInt(7)) / 10000);
					record.setInterestCredit(0);
				} else {
					record.setInterestCredit(0);
					record.setInterestDebit(0);
				}
				if (currency == "JPY")
					record.setBearingBalance(Double.valueOf(data.getInt(8)));
				else
					record.setBearingBalance(Double.valueOf(data.getInt(8)) / 100);

				record.setRate(Double.valueOf(data.getInt(9)) / 1000000000);
				record.setBasis(data.getString(10));
				record.setSettleType(data.getString(11));

				record.setSettlemNum(sSettlementNum);
				record.setSettleName(sSettlementName);

				record.setOwnerName(ownerName);

				record.setSettleOwner(settleOwner);

				result.add(record);
			}
		} catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());
				System.err.println("SQLSTATE: " + ex.getSQLState());
				System.err.println("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained exceptions
			}
		}

		serverACCT.endConn(connectionACCT);
		serverCUST.endConn(connectionCUST);

		return result;

	}

	private ArrayList<Interest> getDayRecords(ArrayList<Interest> full, LocalDate date) {
		ArrayList<Interest> dayrecords = new ArrayList<Interest>();

		for (int i = 0; i < full.size(); i++) {
			if (full.get(i).getToDate().toString().equals(date.toString())) {
				dayrecords.add((Interest) full.get(i).clone());
			}
		}

		return dayrecords;
	}

	private Interest getEmptyRecord(String account, LocalDate date) {
		serverACCT = new DbConnProvider();
		serverACCT.connectionToFirstDB();
		connectionACCT = serverACCT.openConn();
		dataProviderACCT = new DbDataProvider(connectionACCT);

		serverCUST = new DbConnProvider();
		serverCUST.connectionToSecondtDB();
		connectionCUST = serverCUST.openConn();
		dataProviderCUST = new DbDataProvider(connectionCUST);

		Interest empty = null;
		String currency = "";
		String sSettlementNum = "Empty";
		String sSettlementName = "Empty";
		String settleOwner = "Empty";

		try {
			ResultSet curr = dataProviderACCT.getCurrency(account);
			if (curr.next())
				currency = curr.getString(2);

			String ownerName = getOwner(account);
			ResultSet settlement;
			while (true) {
				settlement = dataProviderACCT.getSettlementAcc(account);

				if (settlement.next()) {
					sSettlementNum = settlement.getString(2);
					sSettlementName = settlement.getString(3);
					settleOwner = getOwner(settlement.getString(1));
					break;
				} else {
					ResultSet parent = dataProviderACCT.getParent(account);
					if (parent.next()) {
						account = parent.getString(1);
					} else {
						break;
					}
				}

			}
			ResultSet record = dataProviderACCT.findDetailsData(account);// get the previous one

			if (record.next()) {
				empty = new Interest();
				empty.setCurrency(currency);
				empty.setAccID(record.getInt(1));
				empty.setAccName(record.getString(2));
				empty.setBankID(record.getString(3));
				empty.setCalcuType(record.getString(4));
				empty.setCalcuNum(0);
				empty.setToDate(Date.valueOf(date));
				empty.setInterestCredit(0);
				empty.setInterestDebit(0);
				empty.setBearingBalance(0);
				empty.setRate(0);
				empty.setBasis(record.getString(10));
				empty.setSettleType(record.getString(11));
				empty.setSettlemNum(sSettlementNum);
				empty.setSettleName(sSettlementName);

				empty.setOwnerName(ownerName);

				empty.setSettleOwner(settleOwner);
			}
		} catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());
				System.err.println("SQLSTATE: " + ex.getSQLState());
				System.err.println("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained exceptions
			}
		}
		serverACCT.endConn(connectionACCT);
		serverCUST.endConn(connectionCUST);
		return empty;
	}

	public ArrayList<Interest> getReport(String account, LocalDate fromDate, LocalDate toDate) {

		ArrayList<Interest> report = new ArrayList<Interest>();
		try {
			ArrayList<Interest> records = getRecords(account, fromDate, toDate);
			while (toDate.compareTo(fromDate) >= 0) {
				ArrayList<Interest> dayrecords = getDayRecords(records, fromDate);// select records for current day

				if (dayrecords.size() == 0) { // if there`s no record for today
					if (report.size() == 0) {// if we can`t find first record -> create zero-one
						Interest first = getEmptyRecord(account, fromDate);
						if (first != null)
							report.add(first);
						else
							return report;
					} else {// copy previous record
						report.add((Interest) report.get(report.size() - 1).clone());
						report.get(report.size() - 1).setToDate(Date.valueOf(fromDate));
					}
				} else {
					for (int i = 0; i < dayrecords.size(); i++) { // get all records for current day
						report.add((Interest) dayrecords.get(i).clone());
						// System.out.println("Here");
					}

				}
				fromDate = fromDate.plusDays(1);// next day
			}

		} catch (NullPointerException ex) {
			System.err.println("Error msg: " + ex.getMessage());

			ex.printStackTrace();
			// ex = ex.getNextException();
		}
		return report;
	}
	/*
	 * public ArrayList<Interest> getReport(String account, LocalDate fromDate,
	 * LocalDate toDate) { serverACCT = new DbConnProvider();
	 * serverACCT.connectionToFirstDB(); connectionACCT = serverACCT.openConn();
	 * dataProviderACCT = new DbDataProvider(connectionACCT);
	 * 
	 * serverCUST = new DbConnProvider(); serverCUST.connectionToSecondtDB();
	 * connectionCUST = serverCUST.openConn(); dataProviderCUST = new
	 * DbDataProvider(connectionCUST); data = new ArrayList<Interest>(); String
	 * currency = ""; Interest row; int bdAccessCount = 0; try { ResultSet curr =
	 * dataProviderACCT.getCurrency(account); if (curr.next()) currency =
	 * curr.getString(2);
	 * 
	 * String sSettlementNum = "Empty"; String sSettlementName = "Empty"; String
	 * settleOwner = "Empty"; String ownerName = getOwner(account); ResultSet
	 * settlement; while (true) { settlement =
	 * dataProviderACCT.getSettlementAcc(account);
	 * 
	 * if (settlement.next()) { sSettlementNum = settlement.getString(2);
	 * sSettlementName = settlement.getString(3); settleOwner =
	 * getOwner(settlement.getString(1)); break; } else { ResultSet parent =
	 * dataProviderACCT.getParent(account); if (parent.next()) { account =
	 * parent.getString(1); } else { break; } }
	 * 
	 * }
	 * 
	 * bdAccessCount += 2;
	 * 
	 * while (toDate.compareTo(fromDate) >= 0) { ResultSet record =
	 * dataProviderACCT.getDetailsData(account, fromDate.toString(),
	 * fromDate.toString()); bdAccessCount++;
	 * 
	 * if (!record.next()) { // if there`s no record for today if (data.size() == 0)
	 * {// if we can`t find first record -> create zero-one
	 * 
	 * record = dataProviderACCT.findDetailsData(account, toDate.toString());// get
	 * the previous one bdAccessCount++; if (record.next()) { row = new Interest();
	 * row.setCurrency(currency); row.setAccID(record.getInt(1));
	 * row.setAccName(record.getString(2)); row.setBankID(record.getString(3));
	 * row.setCalcuType(record.getString(4)); row.setCalcuNum(0);
	 * row.setToDate(Date.valueOf(fromDate)); row.setInterestCredit(0);
	 * row.setInterestDebit(0); row.setBearingBalance(0); row.setRate(0);
	 * row.setBasis(record.getString(10)); row.setSettleType(record.getString(11));
	 * row.setSettlemNum(sSettlementNum); row.setSettleName(sSettlementName);
	 * 
	 * row.setOwnerName(ownerName);
	 * 
	 * row.setSettleOwner(settleOwner);
	 * 
	 * data.add(row); } else return new ArrayList<Interest>(); } else {// copy
	 * previous record data.add((Interest) data.get(data.size() - 1).clone());
	 * data.get(data.size() - 1).setToDate(Date.valueOf(fromDate)); //
	 * System.out.println("Record not found");
	 * 
	 * } } else {
	 * 
	 * do { row = new Interest(); // System.out.println(bdAccessCount);
	 * row.setCurrency(currency); row.setAccID(record.getInt(1));
	 * row.setAccName(record.getString(2)); row.setBankID(record.getString(3));
	 * row.setCalcuType(record.getString(4)); row.setCalcuNum(record.getInt(5));
	 * row.setToDate(record.getDate(6)); if (record.getInt(7) < 0) { if (currency ==
	 * "JPY") row.setInterestCredit(Double.valueOf(record.getInt(7)) / 100); else
	 * row.setInterestCredit(Double.valueOf(record.getInt(7)) / 10000);
	 * row.setInterestDebit(0); } else if (record.getInt(7) > 0) { if (currency ==
	 * "JPY") row.setInterestDebit(Double.valueOf(record.getInt(7)) / 100); else
	 * row.setInterestDebit(Double.valueOf(record.getInt(7)) / 10000);
	 * row.setInterestCredit(0); } else { row.setInterestCredit(0);
	 * row.setInterestDebit(0); } if (currency == "JPY")
	 * row.setBearingBalance(Double.valueOf(record.getInt(8))); else
	 * row.setBearingBalance(Double.valueOf(record.getInt(8)) / 100);
	 * 
	 * row.setRate(Double.valueOf(record.getInt(9)) / 1000000000);
	 * row.setBasis(record.getString(10)); row.setSettleType(record.getString(11));
	 * 
	 * row.setSettlemNum(sSettlementNum); row.setSettleName(sSettlementName);
	 * 
	 * row.setOwnerName(ownerName);
	 * 
	 * row.setSettleOwner(settleOwner);
	 * 
	 * data.add(row); if (bdAccessCount > 200) { System.out.println("Reconnect");
	 * serverACCT.endConn(connectionACCT); serverCUST.endConn(connectionCUST);
	 * connectionACCT = serverACCT.openConn(); connectionCUST =
	 * serverCUST.openConn(); dataProviderACCT = new DbDataProvider(connectionACCT);
	 * dataProviderCUST = new DbDataProvider(connectionCUST); bdAccessCount = 0; } }
	 * while (record.next());
	 * 
	 * // System.out.println(fromDate.toString());
	 * 
	 * } // System.out.println(fromDate); fromDate = fromDate.plusDays(1);
	 * 
	 * }
	 * 
	 * } catch (SQLException ex) { System.err.println("SQLException information");
	 * while (ex != null) { System.err.println("Error msg: " + ex.getMessage());
	 * System.err.println("SQLSTATE: " + ex.getSQLState());
	 * System.err.println("Error code: " + ex.getErrorCode()); ex.printStackTrace();
	 * ex = ex.getNextException(); // For drivers that support chained exceptions }
	 * }
	 * 
	 * serverACCT.endConn(connectionACCT); serverCUST.endConn(connectionCUST);
	 * return data; }
	 */

}

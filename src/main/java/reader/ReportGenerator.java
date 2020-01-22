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

	private ResultSet getSettlement(String account) throws SQLException {
		ResultSet settlement = null;
		DbConnProvider server= new DbConnProvider();
		server.connectionToSecondtDB();
		Connection connection = server.openConn(); 
		DbDataProvider dataProvider= new DbDataProvider(connection);

		while (account != null) {
			settlement = dataProviderACCT.getSettlementAcc(account);

			if (settlement.next()) {
				return settlement;
			} else {
				ResultSet parent = dataProviderACCT.getParent(account);
				if (parent.next()) {
					account = parent.getString(1);
				} else {

					settlement = null;
					account = null;
				}
			}

		}
		server.endConn(connection);
		return settlement;
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

	public ArrayList<Interest> getReport(String account, LocalDate fromDate, LocalDate toDate) {
		serverACCT = new DbConnProvider();
		serverACCT.connectionToFirstDB();
		connectionACCT = serverACCT.openConn();
		dataProviderACCT = new DbDataProvider(connectionACCT);

		serverCUST = new DbConnProvider();
		serverCUST.connectionToSecondtDB();
		connectionCUST = serverCUST.openConn();
		dataProviderCUST = new DbDataProvider(connectionCUST);
		data = new ArrayList<Interest>();
		String currency = "";
		Interest row;
		int bdAccessCount = 0;
		try {
			ResultSet curr = dataProviderACCT.getCurrency(account);
			if (curr.next())
				currency = curr.getString(2);

			ResultSet settlement = getSettlement(account);
			String sSettlementNum = "Empty";
			String sSettlementName = "Empty";
			String settleOwner = "Empty";
			String ownerName = getOwner(account);
			
			sSettlementNum = settlement.getString(2);
			sSettlementName = settlement.getString(3);
			settleOwner = getOwner(settlement.getString(1));
			bdAccessCount += 2;

			while (toDate.compareTo(fromDate) >= 0) {
				ResultSet record = dataProviderACCT.getDetailsData(account, fromDate.toString(), fromDate.toString());
				bdAccessCount++;

				if (!record.next()) { // if there`s no record for today
					if (data.size() == 0) {// if we can`t find first record -> create zero-one
						
						record = dataProviderACCT.findDetailsData(account, fromDate.toString());// get the previous one
						bdAccessCount++;
						if (record.next()) {
							row = new Interest();
							row.setCurrency(currency);
							row.setAccID(record.getInt(1));
							row.setAccName(record.getString(2));
							row.setBankID(record.getString(3));
							row.setCalcuType(record.getString(4));
							row.setCalcuNum(0);
							row.setToDate(Date.valueOf(fromDate));
							row.setInterestCredit(0);
							row.setInterestDebit(0);
							row.setBearingBalance(0);
							row.setRate(0);
							row.setBasis(record.getString(10));
							row.setSettleType(record.getString(11));
							row.setSettlemNum(sSettlementNum);
							row.setSettleName(sSettlementName);

							row.setOwnerName(ownerName);

							row.setSettleOwner(settleOwner);

							data.add(row);
						} else
							return new ArrayList<Interest>();
					} else {// copy previous record
						data.add((Interest)data.get(data.size() - 1).clone());
						data.get(data.size() - 1).setToDate(Date.valueOf(fromDate));
						System.out.println("Record not found");
						for(int i=0; i<data.size(); i++) {
							System.out.println("Date "+ data.get(i).getToDate());
						}
						
						
					}
				} else {

					do {
						row = new Interest();
						// System.out.println(bdAccessCount);
						row.setCurrency(currency);
						row.setAccID(record.getInt(1));
						row.setAccName(record.getString(2));
						row.setBankID(record.getString(3));
						row.setCalcuType(record.getString(4));
						row.setCalcuNum(record.getInt(5));
						row.setToDate(record.getDate(6));
						if (record.getInt(7) < 0) {
							if (currency == "JPY")
								row.setInterestCredit(Double.valueOf(record.getInt(7)) / 100);
							else
								row.setInterestCredit(Double.valueOf(record.getInt(7)) / 10000);
							row.setInterestDebit(0);
						} else if (record.getInt(7) > 0) {
							if (currency == "JPY")
								row.setInterestDebit(Double.valueOf(record.getInt(7)) / 100);
							else
								row.setInterestDebit(Double.valueOf(record.getInt(7)) / 10000);
							row.setInterestCredit(0);
						} else {
							row.setInterestCredit(0);
							row.setInterestDebit(0);
						}
						if (currency == "JPY")
							row.setBearingBalance(Double.valueOf(record.getInt(8)));
						else
							row.setBearingBalance(Double.valueOf(record.getInt(8)) / 100);

						row.setRate(Double.valueOf(record.getInt(9)) / 1000000000);
						row.setBasis(record.getString(10));
						row.setSettleType(record.getString(11));

						row.setSettlemNum(sSettlementNum);
						row.setSettleName(sSettlementName);

						row.setOwnerName(ownerName);

						row.setSettleOwner(settleOwner);

						data.add(row);
						if (bdAccessCount > 200) {
							System.out.println("Reconnect");
							serverACCT.endConn(connectionACCT);
							serverCUST.endConn(connectionCUST);
							connectionACCT = serverACCT.openConn();
							connectionCUST = serverCUST.openConn();
							dataProviderACCT = new DbDataProvider(connectionACCT);
							dataProviderCUST = new DbDataProvider(connectionCUST);
							bdAccessCount = 0;
						}
					} while (record.next());

					// System.out.println(fromDate.toString());

				}
				// System.out.println(fromDate);
				fromDate = fromDate.plusDays(1);

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
		return data;
	}

}

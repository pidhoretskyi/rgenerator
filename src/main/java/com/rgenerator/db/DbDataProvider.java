package com.rgenerator.db;


import java.sql.SQLException;
import java.sql.Statement;

import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.ResultSet;



public class DbDataProvider {
	
	Connection connection;
	
	public DbDataProvider(Connection connection) {
		this.connection = connection;
	}
	

	public ResultSet getAccountNumberForReport(String accountID) {
		return dataProvider("SELECT a.ACC_NAME, a.ACC_NUMBER FROM ACCT.ACCOUNT a\r\n" + 
				"WHERE a.ACC_ID = "+accountID);
	}
	
	public ResultSet getTopAccountNumber(String hierID) {
		return dataProvider("SELECT a_top.ACC_NUMBER\r\n" + 
				"     , a_top.ACC_ID\r\n" + 
				"     , a_top.ACC_NAME\r\n" + 
				"FROM ACCT.ACCOUNT a_top\r\n" + 
				"INNER JOIN ACCT.EDGE e_top ON e_top.HIER_ID = "+hierID+" AND a_top.ACC_ID = e_top.CHILD_ACC_ID AND e_top.PARENT_ACC_ID IS NULL");
	}
	
	public ResultSet getCurrency(String account) {
		return dataProvider("SELECT ACC_ID, CURRENCY_CODE FROM acct.ACCOUNT a2\r\n" + 
				"WHERE ACC_ID="+account);
	}
	
	public ResultSet getDetailsData(String account, String dateFrom, String dateTo) {
		return dataProvider("SELECT\r\n" + 
				"	ard.ACC_ID AS \"Account number\",\r\n" + 
				"	a.ACC_NAME AS \"Account Name\",\r\n" + 
				"	ard.BANK_ID AS \"Bank_ID\",\r\n" + 
				"	ard.CALCU_TYPE AS \"Interest type\",\r\n" + 
				"	ard.CALCU_NUMBER_OF_DAYS AS \"Number of days\",\r\n" + 
				"	ard.CALCU_TO_DATE AS \"Calculation date\",\r\n" + 
				"	ard.CALCU_AMOUNT AS \"Calculated Interest\",\r\n" + 
				"	ard.CALCU_CONSUMED_AMOUNT AS \"Interest Bearing Balance\",\r\n" + 
				"	ard.CALCU_RATE_PERCENT AS \"Total Interest Rate (%)\",\r\n" + 
				"	ard.FCOND_NAME AS \"Interest Basis\",\r\n" + 
				"	ard.SETTLEMENT_TYPE AS \"Settlement type\",\r\n" + 
				"   ard.FUNCTION_ID\r\n" +
				"FROM\r\n" + 
				"	ACCT.ACCOUNT_RESULT_DETAILS ard\r\n" + 
				"INNER JOIN ACCT.ACCOUNT a ON\r\n" + 
				"	a.ACC_ID = ard.ACC_ID\r\n" + 
				"WHERE\r\n" + 
				"	ard.ACC_ID = "+account+"\r\n" + 
				"		AND CALCU_TO_DATE BETWEEN date('"+dateFrom+"') AND date('"+dateTo+"')\r\n" + 
				"	AND ard.CALCU_TYPE = 'INT'\r\n" + 
				"ORDER BY\r\n" + 
				"	ard.CALCU_TO_DATE ASC");
	}
	
	public ResultSet findDetailsData(String account) {
		return dataProvider("SELECT\r\n" + 
				"	ard.ACC_ID AS \"Account number\",\r\n" + 
				"	a.ACC_NAME AS \"Account Name\",\r\n" + 
				"	ard.BANK_ID AS \"Bank_ID\",\r\n" + 
				"	ard.CALCU_TYPE AS \"Interest type\",\r\n" + 
				"	ard.CALCU_NUMBER_OF_DAYS AS \"Number of days\",\r\n" + 
				"	ard.CALCU_TO_DATE AS \"Calculation date\",\r\n" + 
				"	ard.CALCU_AMOUNT AS \"Calculated Interest\",\r\n" + 
				"	ard.CALCU_CONSUMED_AMOUNT AS \"Interest Bearing Balance\",\r\n" + 
				"	ard.CALCU_RATE_PERCENT AS \"Total Interest Rate (%)\",\r\n" + 
				"	ard.FCOND_NAME AS \"Interest Basis\",\r\n" + 
				"	ard.SETTLEMENT_TYPE AS \"Settlement type\"\r\n" + 
				"FROM\r\n" + 
				"	ACCT.ACCOUNT_RESULT_DETAILS ard\r\n" + 
				"INNER JOIN ACCT.ACCOUNT a ON\r\n" + 
				"	a.ACC_ID = ard.ACC_ID\r\n" + 
				"WHERE\r\n" + 
				"	ard.ACC_ID = "+account+"\r\n" + 
				"	AND ard.CALCU_TYPE = 'INT'\r\n" + 
				"ORDER BY\r\n" + 
				"	ard.CALCU_TO_DATE DESC\r\n"+
				"LIMIT 1");
	}
	
	
	
	public ResultSet weeklyEntriesData(String account, String date) {
		return dataProvider("SELECT \r\n" + 
				"       e.HIER_ID -- group into workbook files by HIER_ID\r\n" + 
				"     , ar.ACC_ID -- split into worksheets by ACC_ID\r\n" + 
				"     , ar.RESUL_FROM_DATE\r\n" + 
				"     , ar.RESUL_TO_DATE\r\n" +  
				"  FROM ACCT.ACCOUNT_RESULT ar\r\n" + 
				"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND '"+date+"' BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
				"WHERE ar.ACC_ID = "+account+" AND \r\n" + 
				"      '"+date+"' BETWEEN ar.RESUL_FROM_DATE AND ar.RESUL_TO_DATE\r\n" + 
				"  AND DAYOFWEEK('"+date+"') = 1 -- Sunday\r\n" + 
				"ORDER BY e.HIER_ID, ar.ACC_ID, ar.RESUL_FROM_DATE");
	}
	
	
	
	public ResultSet monthlyEntriesData(String account, String date) {
		return dataProvider("-- Monthly\r\n" + 
				"-- for each row of this resultset\r\n" + 
				"SELECT \r\n" + 
				"       e.HIER_ID -- group into workbook files by HIER_ID\r\n" + 
				"     , ar.ACC_ID -- split into worksheets by ACC_ID\r\n" + 
				"     , ar.RESUL_FROM_DATE\r\n" +
				"     , ar.RESUL_TO_DATE\r\n" + 
				"     , ar.FUNCTION_ID, ar.COND_CODE\r\n" + 
				"FROM ACCT.ACCOUNT_RESULT ar\r\n" + 
				"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND '"+date+"' BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
				"WHERE ar.ACC_ID="+account+" AND (\r\n" + 
				"    -- criteria to select records that need to be reported :today\r\n" + 
				"    ar.RESUL_TO_DATE = '"+date+"'\r\n" + 
				"    OR YEAR(ar.RESUL_TO_DATE) > YEAR('"+date+"')\r\n" + 
				"    OR YEAR(ar.RESUL_TO_DATE) = YEAR('"+date+"') AND MONTH(ar.RESUL_TO_DATE) > MONTH('"+date+"'))\r\n" + 
				"    --\r\n" + 
				"ORDER BY e.HIER_ID, ar.ACC_ID, ar.RESUL_FROM_DATE\r\n");
	}
	
	public ResultSet quarterEntriesData(String account, String from, String to) {
		return dataProvider("-- Monthly\r\n" + 
				"-- for each row of this resultset\r\n" + 
				"SELECT \r\n" + 
				"       e.HIER_ID -- group into workbook files by HIER_ID\r\n" + 
				"     , ar.ACC_ID -- split into worksheets by ACC_ID\r\n" + 
				"     , ar.RESUL_FROM_DATE\r\n" +
				"     , ar.RESUL_TO_DATE\r\n" + 
				"     , ar.FUNCTION_ID, ar.COND_CODE\r\n" + 
				"FROM ACCT.ACCOUNT_RESULT ar\r\n" + 
				"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND '"+to+"' BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
				"WHERE ar.ACC_ID="+account+" AND \r\n" + 
				"ar.RESUL_TO_DATE BETWEEN '"+from+"' AND '"+to+"' \r\n"+
				"ORDER BY e.HIER_ID, ar.ACC_ID, ar.RESUL_FROM_DATE\r\n");
	}
	
	public ResultSet getMonthlyHierarchies(String date) {
		return dataProvider("SELECT DISTINCT\r\n" + 
				"       e.HIER_ID \r\n" + 
				"     FROM ACCT.ACCOUNT_RESULT ar\r\n" + 
				"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND '"+date+"' BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
				"WHERE 1=0\r\n" + 
				"    -- criteria to select records that need to be reported :today\r\n" + 
				"    OR ar.RESUL_TO_DATE = '"+date+"'\r\n" + 
				"    OR YEAR(ar.RESUL_TO_DATE) > YEAR('"+date+"')\r\n" + 
				"    OR YEAR(ar.RESUL_TO_DATE) = YEAR('"+date+"') AND MONTH(ar.RESUL_TO_DATE) > MONTH('"+date+"')\r\n" + 
				"    --\r\n" + 
				"ORDER BY e.HIER_ID");
	}
	
	public ResultSet getWeeklyHierarchies(String date) {
		return dataProvider("SELECT DISTINCT\r\n" + 
				"       e.HIER_ID -- group into workbook files by HIER_ID\r\n" + 
				"     FROM ACCT.ACCOUNT_RESULT ar\r\n" + 
				"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND date('"+date+"') BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
				"WHERE \r\n" + 
				"      date('"+date+"') BETWEEN ar.RESUL_FROM_DATE AND ar.RESUL_TO_DATE\r\n" + 
				"  AND DAYOFWEEK(date('"+date+"')) = 1 -- Sunday\r\n" + 
				"ORDER BY e.HIER_ID");
	}
	
	public ResultSet getQuarterHierarchies(String from, String to) {
		return dataProvider("SELECT DISTINCT\r\n" + 
				"       e.HIER_ID \r\n" + 
				"     FROM ACCT.ACCOUNT_RESULT ar\r\n" + 
				"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND date('"+to+"') BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
				"WHERE \r\n" + 
				"	ar.RESUL_TO_DATE BETWEEN date('"+from+"') AND date('"+to+"')\r\n" + 
				"ORDER BY e.HIER_ID");
	}
	
	public ResultSet getHierarchy(String account) {
		return dataProvider("SELECT CHILD_ACC_ID, HIER_ID FROM ACCT.EDGE e2 \r\n" + 
				"WHERE HIER_ID IN (SELECT HIER_ID FROM ACCT.EDGE e\r\n" + 
				"WHERE CHILD_ACC_ID IN (SELECT ACC_ID FROM ACCT.ACCOUNT a WHERE ACC_NUMBER LIKE '%"+account+"%')) ");
	}
	
	public ResultSet dailyEntriesACC(String date) {
		return dataProvider(
				"SELECT\r\n"+ 
			    "   e.HIER_ID\r\n"+
			    " , ar.ACC_ID\r\n"+
			    " , ar.RESUL_TO_DATE\r\n"+
			    " , ar.FUNCTION_ID, ar.COND_CODE\r\n"+
			  "FROM ACCT.ACCOUNT_RESULT ar\r\n"+
			"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND date('"+date+"') BETWEEN e.FROM_DATE AND e.TO_DATE\r\n"+
			"WHERE 1=0\r\n"+
			    "OR ar.RESUL_TO_DATE = date('"+date+"')\r\n"+
			"ORDER BY e.HIER_ID, ar.ACC_ID");
	}
	
	
	public ResultSet getSettlementAcc(String account) {
		return dataProvider("SELECT a.ACC_ID, a.ACC_NUMBER, a.ACC_NAME\r\n" + 
				"  FROM ACCT.EDGE e\r\n" + 
				"INNER JOIN ACCT.EDGE es ON es.PARENT_ACC_ID = e.PARENT_ACC_ID AND es.HIER_ID = e.HIER_ID\r\n" + 
				"INNER JOIN ACCT.ACCOUNT_ACCOUNTTYPE aat ON aat.ACC_ID = es.CHILD_ACC_ID AND aat.ACCTYPE_CATEGORY = 'TRAN'\r\n" + 
				"INNER JOIN ACCT.ACCOUNT a ON a.ACC_ID = es.CHILD_ACC_ID\r\n" + 
				"WHERE e.CHILD_ACC_ID = "+account+"\r\n" + 
				"   AND a.RCA_ACC = 'Y' ");
	}
	
	
	public ResultSet getParent(String account) {
		return dataProvider("SELECT PARENT_ACC_ID\r\n" + 
				"  FROM ACCT.EDGE \r\n" + 
				" WHERE CHILD_ACC_ID = "+account);
	}
	
	public ResultSet getOwner(String account) {
		return dataProvider("SELECT r.CUST_ID\r\n" + 
				"FROM ACCT.ACCOUNT_CUSTOMER_ROLE r\r\n" + 
				"WHERE r.ACC_ID ="+account+" AND r.ROLE_TYPE = 'PAHR'");
	}
	
	public ResultSet getOwnerName(String customer) {
		return dataProvider("\r\n" + 
				"SELECT cu.CUST_ID,\r\n" + 
				"--- this might work:\r\n" + 
				"      CASE cu.CUST_TYPE WHEN 'C' \r\n" + 
				"                         THEN cu.CUST_COMPANY_NAME \r\n" + 
				"                         ELSE COALESCE(cu.CUST_FIRST_NAME, '')\r\n" + 
				"                              || CASE WHEN LENGTH(COALESCE(cu.CUST_FIRST_NAME, '')) > 0 AND LENGTH(COALESCE(cu.CUST_LAST_NAME, '')) > 0 THEN ' ' ELSE '' END \r\n" + 
				"                              || COALESCE(cu.CUST_LAST_NAME, '') \r\n" + 
				"                              || CASE WHEN LENGTH(COALESCE(cu.CUST_NAME_SUFFIX, '')) > 0 THEN ' ' ELSE '' END\r\n" + 
				"                              || COALESCE(cu.CUST_NAME_SUFFIX, '')\r\n" + 
				"                         END AS ENTITY_NAME\r\n" + 
				"--\r\n" + 
				" FROM CUST.CUSTOMER cu WHERE cu.CUST_ID = "+customer);
	}
	
	public ResultSet getAllAggrAccInHierarchy(String date, String hierarchiID) {
        return dataProvider("SELECT\r\n" + 
                     "e.PARENT_ACC_ID,\r\n" + 
                      "a.ACC_ID,\r\n" + 
                     "aat.ACCTYPE_CATEGORY,\r\n" + 
                     "e.FROM_DATE,\r\n" + 
                     "e.TO_DATE,\r\n" + 
                     "a.ACC_NAME,\r\n" + 
                     "a.ACC_NUMBER\r\n" + 
                     "FROM ACCT.EDGE e\r\n" + 
                     "INNER JOIN ACCT.ACCOUNT_ACCOUNTTYPE aat ON aat.ACC_ID = e.CHILD_ACC_ID\r\n" + 
                     "INNER JOIN ACCT.ACCOUNT a ON a.ACC_ID = e.CHILD_ACC_ID\r\n" + 
                     "WHERE e.HIER_ID = " + hierarchiID +"\r\n" + 
                     "AND '" + date + "' BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
                     "ORDER BY e.PARENT_ACC_ID DESC");
	}


	
	private ResultSet dataProvider(String qwery) {
		ResultSet rs = null;
		try {
		// Create the Statement
		Statement stmt = connection.createStatement();
		

		// Execute a query and generate a ResultSet instance
		rs = (ResultSet) stmt.executeQuery(qwery);
		
		}catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());
				System.err.println("SQLSTATE: " + ex.getSQLState());
				System.err.println("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained exceptions
			}
		}
		
		return rs;	
	}
	

}

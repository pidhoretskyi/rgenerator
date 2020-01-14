package com.rgenerator.db;

import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.ResultSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

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
	
	public ResultSet getDetailsData(String account) {
		return dataProvider("SELECT\r\n" + 
				"                             ACC_ID AS \"Account number\", \r\n" + 
				"                                BANK_ID AS \"Bank_ID\",\r\n" + 
				"                                CALCU_TYPE AS \"Interest type\",\r\n" + 
				"                                CALCU_NUMBER_OF_DAYS AS \"Number of days\", \r\n" + 
				"                                CALCU_TO_DATE AS \"Calculation date\",\r\n" + 
				"                                CALCU_AMOUNT AS \"Calculated Interest\",\r\n" + 
				"                                CALCU_CONSUMED_AMOUNT AS \"Interest Bearing Balance\", \r\n" +
				"                                CALCU_RATE_PERCENT AS \"Total Interest Rate (%)\", \r\n" + 
				"                                FCOND_NAME AS \"Interest Basis\",\r\n" + 
				"                               SETTLEMENT_TYPE AS \"Settlement type\"\r\n" + 
				"                          FROM ACCT.ACCOUNT_RESULT_DETAILS WHERE ACC_ID = "+account+"\r\n" + 
				"                          AND CALCU_TYPE = 'INT'\r\n" + 
				"ORDER BY CALCU_REG_TIMESTAMP DESC");
	}
	
	public ResultSet weeklyEntriesData(String date) {
		return dataProvider(
				"-- Weekly\r\n" + 
				"SELECT \r\n" + 
				"       e.HIER_ID -- group into workbook files by HIER_ID\r\n" + 
				"     , ar.ACC_ID -- split into worksheets by ACC_ID\r\n" + 
				"     , ar.RESUL_TO_DATE\r\n" + 
				"     , ar.FUNCTION_ID, ar.COND_CODE\r\n" + 
				"     , date('"+date+"') + CASE DAYOFWEEK(date('"+date+"')) WHEN 1 THEN -6 ELSE - DAYOFWEEK(date('"+date+"')) + 2 END DAYS AS details_from\r\n" + 
				"     , date('"+date+"') + CASE DAYOFWEEK(date('"+date+"')) WHEN 1 THEN 0 ELSE 8 - DAYOFWEEK(date('"+date+"')) END DAYS AS details_to\r\n" + 
				"  FROM ACCT.ACCOUNT_RESULT ar\r\n" + 
				"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND date('"+date+"') BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
				"--INNER JOIN ACCOUNT a ON a.ACC_ID = ar.ACC_ID\r\n" + 
				"--LEFT OUTER JOIN TERMCOND_FUNCTION fa ON fa.FUNCTION_ID = ar.FUNCTION_ID AND fa.COND_CODE = ar.COND_CODE AND fa.BANK_ID = a.BANK_ID\r\n" + 
				"WHERE -- criteria to select records that need to be reported :today\r\n" + 
				"      date('"+date+"') BETWEEN ar.RESUL_FROM_DATE AND ar.RESUL_TO_DATE\r\n" + 
				"  AND DAYOFWEEK(date('"+date+"')) = 1 -- Sunday\r\n" + 
				"ORDER BY e.HIER_ID, ar.ACC_ID, ar.RESUL_FROM_DATE\r\n" + 
				"-- then extract account_result_details based on data in account_result");
	}
	
	public ResultSet monthlyEntriesData(String date) {
		return dataProvider("-- Monthly\r\n" + 
				"-- for each row of this resultset\r\n" + 
				"SELECT \r\n" + 
				"       e.HIER_ID -- group into workbook files by HIER_ID\r\n" + 
				"     , ar.ACC_ID -- split into worksheets by ACC_ID\r\n" + 
				"     , ar.RESUL_TO_DATE\r\n" + 
				"     , ar.FUNCTION_ID, ar.COND_CODE\r\n" + 
				"     , date(TO_DATE(TO_CHAR(YEAR('"+date+"'),'0000')||'-'||TO_CHAR(MONTH('"+date+"'),'00')||'-01', 'YYYY-MM-DD')) AS details_from\r\n" + 
				"     , date(TO_DATE(TO_CHAR(YEAR('"+date+"') + CASE MONTH('"+date+"') WHEN 12 THEN 1 ELSE 0 END,'0000')||'-'||TO_CHAR(MONTH('"+date+"') + CASE MONTH('"+date+"') WHEN 12 THEN -11 ELSE 1 END,'00')||'-01', 'YYYY-MM-DD') - 1 DAY) AS details_to\r\n" + 
				"FROM ACCT.ACCOUNT_RESULT ar\r\n" + 
				"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND '"+date+"' BETWEEN e.FROM_DATE AND e.TO_DATE\r\n" + 
				"WHERE 1=0\r\n" + 
				"    -- criteria to select records that need to be reported :today\r\n" + 
				"    OR ar.RESUL_TO_DATE = '"+date+"'\r\n" + 
				"    OR YEAR(ar.RESUL_TO_DATE) > YEAR('"+date+"')\r\n" + 
				"    OR YEAR(ar.RESUL_TO_DATE) = YEAR('"+date+"') AND MONTH(ar.RESUL_TO_DATE) > MONTH('"+date+"')\r\n" + 
				"    --\r\n" + 
				"ORDER BY e.HIER_ID, ar.ACC_ID, ar.RESUL_FROM_DATE");
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

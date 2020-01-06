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
	
	public ResultSet MonthEntriesData(String account) {
		return DataProvider("SELECT * FROM ACCT.ACCOUNT_RESULT\r\n" + 
				"WHERE ACC_ID IN (SELECT ACC_ID FROM ACCT.ACCOUNT a WHERE ACC_NUMBER LIKE '%"+account+"%')\r\n" + 
				"  AND SETTLEMENT_TYPE = 'INT'\r\n");
		
	}
	
	public ResultSet DailyEntriesData(String account) {
		return DataProvider("SELECT * FROM ACCT.ACCOUNT_RESULT_DETAILS\r\n" +
				"WHERE ACC_ID = " +account+"\r\n" +
				  "AND CALCU_TYPE = 'INT'\r\n" +
				  "ORDER BY CALCU_REG_TIMESTAMP DESC");
	}
	
	public ResultSet WeeklyEntriesData(String account) {
		return DataProvider("SELECT * FROM ACCT.ACCOUNT_RESULT_DETAILS\r\n" +
				"WHERE ACC_ID IN (SELECT ACC_ID FROM ACCT.ACCOUNT a WHERE ACC_NUMBER LIKE '%"+account+"%')\r\n" +
				  "AND CALCU_TYPE = 'INT'\r\n" +
				  "ORDER BY CALCU_REG_TIMESTAMP DESC");
	}
	
	public ResultSet GetHierarchy(String account) {
		return DataProvider("SELECT CHILD_ACC_ID, HIER_ID FROM ACCT.EDGE e2 \r\n" + 
				"WHERE HIER_ID IN (SELECT HIER_ID FROM ACCT.EDGE e\r\n" + 
				"WHERE CHILD_ACC_ID IN (SELECT ACC_ID FROM ACCT.ACCOUNT a WHERE ACC_NUMBER LIKE '%"+account+"%')) ");
	}
	
	public ResultSet DailyEntriesACC(String date) {
		return DataProvider(
				"SELECT\r\n"+ 
			    "   e.HIER_ID\r\n"+
			    " , ar.ACC_ID\r\n"+
			    " , ar.RESUL_TO_DATE\r\n"+
			    " , ar.FUNCTION_ID, ar.COND_CODE\r\n"+
			  "FROM ACCT.ACCOUNT_RESULT ar\r\n"+
			"INNER JOIN ACCT.EDGE e ON e.CHILD_ACC_ID = ar.ACC_ID AND date('"+date+"') BETWEEN e.FROM_DATE AND e.TO_DATE\r\n"+
			"WHERE 1=0\r\n"+
			    "OR ar.RESUL_TO_DATE = date('"+date+"')\r\n"+
			    "OR YEAR(ar.RESUL_TO_DATE) > YEAR(date('"+date+"'))\r\n"+
			    "OR YEAR(ar.RESUL_TO_DATE) = YEAR(date('"+date+"')) AND MONTH(ar.RESUL_TO_DATE) > MONTH(date('"+date+"'))\r\n"+
			"ORDER BY e.HIER_ID, ar.ACC_ID");
	}
	
	private ResultSet DataProvider(String qwery) {
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

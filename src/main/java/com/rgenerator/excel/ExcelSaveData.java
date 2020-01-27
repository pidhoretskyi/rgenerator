package com.rgenerator.excel;


import java.sql.SQLException;

import java.time.LocalDate;

import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.ResultSet;
import com.rgenerator.db.DbConnProvider;
import com.rgenerator.db.DbDataProvider;

import hierarchy.Hierarchy;





public class ExcelSaveData {


	private DbConnProvider server;
	private Connection connection;
	private DbDataProvider dataProvider;
		
	public void startMonthlyReporting(LocalDate fromDate, LocalDate toDate) {
		server = new DbConnProvider();
		server.connectionToFirstDB();
		connection = server.openConn();
		dataProvider = new DbDataProvider(connection);
		if(connection!=null) {
			ResultSet monthlyHier = dataProvider.getMonthlyHierarchies(toDate.toString());
			Hierarchy monthly = new Hierarchy();
			try {
				while(monthlyHier.next()) {
					monthly.getHierarchyReport(monthlyHier.getString(1), fromDate, toDate, 2);
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
		}
		server.endConn(connection);
		
	}
	
	public void startWeeklyReporting(LocalDate fromDate, LocalDate toDate) {
		server = new DbConnProvider();
		server.connectionToFirstDB();
		connection = server.openConn();
		dataProvider = new DbDataProvider(connection);
		if(connection!=null) {
			ResultSet weeklyHier = dataProvider.getWeeklyHierarchies(toDate.toString());
			Hierarchy weekly = new Hierarchy();
			try {
				while(weeklyHier.next()) {
					weekly.getHierarchyReport(weeklyHier.getString(1), fromDate, toDate, 1);
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
		}
		server.endConn(connection);
	}

	

	
}

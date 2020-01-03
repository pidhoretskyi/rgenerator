package com.rgenerator.db;

import java.sql.DriverManager;
import java.sql.SQLException;
import com.ibm.db2.jcc.am.Connection;


public class DbConnProvider {
	
	String URL;
	String USER;
	String PASSWORD;
	
	public DbConnProvider() {
		URL = "jdbc:db2://10.254.187.216:50000/ACCTDB";
		USER = "root";
		PASSWORD = "15Asennu51";
	}
	
	public DbConnProvider(String URL, String USER, String PASSWORD) {
		this.URL = URL;
		this.USER = USER;
		this.PASSWORD = PASSWORD;
	}
	
	public Connection openConn()  {
		Connection connection = null;
		try {
			// loading DB2 driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");
			
			// create connection
			connection = (Connection) DriverManager.getConnection(URL, USER, PASSWORD);

			// Commit changes manually
			connection.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");
			
			
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load JDBC driver");
			System.out.println("Exception: " + e);
			e.printStackTrace();
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
		return connection;
	}
	
	public void endConn(Connection connection) {
		try {
			// Connection must be on a unit-of-work boundary to allow close
			connection.close();
			
			if (connection.isClosed()) {
				System.out.println("Connection is closed");
			}
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
			
	}
	
}

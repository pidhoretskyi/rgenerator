package com.rgenerator.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import com.ibm.db2.jcc.am.Connection;

public class DbConnProvider {

	private static final String fileName = "META-INF/application.properties";

	private static Scanner scanner = new Scanner(System.in);
	public static Properties properties = new Properties();
	public static InputStream inputStream = DbConnProvider.class.getClassLoader().getResourceAsStream(fileName);

	private String URL;
	private String USER;
	private String PASSWORD;
	private String PORT;
	private String DB_NAME;

	public DbConnProvider() {
	}

	public DbConnProvider(String URL, String USER, String PASSWORD) {
		this.URL = URL;
		this.USER = USER;
		this.PASSWORD = PASSWORD;

	}

	public void connectionToFirstDB() {

		try {
			inputStream = DbConnProvider.class.getClassLoader().getResourceAsStream(fileName);
			if (inputStream == null) {
				System.out.println("Sorry, unable to find " + fileName);
				return;
			}
			properties.load(inputStream);

			// Get data from application.properties file provide connection with First DB
			DB_NAME = properties.getProperty("dbNameForFirstConn");
			URL = properties.getProperty("dbUrlForFirstConn");
			USER = properties.getProperty("dbUserForFirstConn");
			//System.out.println("ENTER PASSWORD for " + DB_NAME );
			PASSWORD = "15Asennu51";//scanner.next();
//			PASSWORD = properties.getProperty("dbPassForFirstConn");
			PORT = properties.getProperty("dbPortForFirstConn");

			URL = URL + ":" + PORT + "/" + DB_NAME;
			new DbConnProvider(URL, USER, PASSWORD);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connectionToSecondtDB() {

		try {
			inputStream = DbConnProvider.class.getClassLoader().getResourceAsStream(fileName);
			if (inputStream == null) {
				System.out.println("Sorry, unable to find " + fileName);
				return;
			}
			properties.load(inputStream);

			// Get data from application.properties file provide connection with Second DB
			DB_NAME = properties.getProperty("dbNameForSecondConn");
			URL = properties.getProperty("dbUrlForSecondConn");
			USER = properties.getProperty("dbUserForSecondConn");
			//System.out.println("ENTER PASSWORD for " + DB_NAME );
			PASSWORD = "15Asennu51";//scanner.next();
//			PASSWORD = properties.getProperty("dbPassForFirstConn");
			PORT = properties.getProperty("dbPortForSecondConn");

			URL = URL + ":" + PORT + "/" + DB_NAME;
			new DbConnProvider(URL, USER, PASSWORD);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public Connection openConn() {
		Connection connection = null;
		try {
			// loading DB2 driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			// System.out.println("**** Loaded the JDBC driver");

			// create connection
			connection = (Connection) DriverManager.getConnection(URL, USER, PASSWORD);

			// Commit changes manually
			connection.setAutoCommit(false);

			// System.out.println("**** Created a JDBC connection to the data source");

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
			connection.commit();
			// Connection must be on a unit-of-work boundary to allow close
			connection.close();

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

}

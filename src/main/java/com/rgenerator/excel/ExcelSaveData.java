package com.rgenerator.excel;

import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.ResultSet;
import com.rgenerator.db.DbConnProvider;
import com.rgenerator.db.DbDataProvider;

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

public class ExcelSaveData {
	
	public void SaveACC(Workbook workbook, ResultSet ACCdata, String ACCname) {
		try {
			ResultSetMetaData data = ACCdata.getMetaData();
			int columnCount = data.getColumnCount();
			int currentRow = 0;
		
		
			Sheet sheet = workbook.createSheet(ACCname);
			//Header font
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 12);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
        
        
			System.out.println("**** Writing to file");
			Row row = sheet.createRow(currentRow++);
			for(int i=1; i<=columnCount; i++) {
				Cell cell = row.createCell(i-1);
				cell.setCellValue(data.getColumnName(i));
				cell.setCellStyle(headerCellStyle);
			}
		  
			boolean isDataExist = false;
			while(ACCdata.next()) {
				isDataExist = true;
				row = sheet.createRow(currentRow++);
				for(int i=1; i<=columnCount; i++) {
					row.createCell(i-1).setCellValue(ACCdata.getString(i));
				}
			}
			if(!isDataExist) {
				workbook.removeSheetAt(workbook.getSheetIndex(sheet));
				return;
			}
			for(int i = 0; i < columnCount; i++) {
				sheet.autoSizeColumn(i);
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
	
	public void SaveHierarchy(String account) {
		DbConnProvider server = new DbConnProvider();
		Connection connection = server.openConn();
		DbDataProvider dataProvider = new DbDataProvider(connection);
		System.err.println("**** Saving hierarchy");
		if(connection != null) {
			try {
				ResultSet hier = dataProvider.GetHierarchy(account);
				Workbook workbook = new HSSFWorkbook();
				while(hier.next()) {
					if(workbook.getSheet(hier.getString(1))!=null) continue;
					ResultSet acc = dataProvider.MonthEntriesData(hier.getString(1));
					SaveACC(workbook, acc, hier.getString(1));
				}
				FileOutputStream fileOut = new FileOutputStream(account+"_"+LocalDate.now()+".xls");
				workbook.write(fileOut);
				fileOut.close();
			}catch(IOException ex) {
	        	System.err.println("IOException information");
	        	while (ex != null) {
					System.err.println("Error msg: " + ex.getMessage());
					
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
	
	public void DailyEnries(String date) {
		DbConnProvider server = new DbConnProvider();
		Connection connection = server.openConn();
		DbDataProvider dataProvider = new DbDataProvider(connection);
		System.out.println("**** 1");
		
		if(connection != null) {
			
			try {
				
				ResultSet accountsForDailyReport = dataProvider.DailyEntriesACC(date);
				Workbook workbook = new HSSFWorkbook();
				FileOutputStream fileOut=null;
				int prevACChier = -1;
				
				while(accountsForDailyReport.next()) {
					ResultSet accNumberData = dataProvider.getAccountNumberForDailyReport(accountsForDailyReport.getString(2));
					
					accNumberData.next();
					String accNumber = accNumberData.getString(2);
					System.out.println("**** "+accNumber);
					if(accNumber==null) continue;
					if(workbook.getSheet(accNumber)!=null) continue; // if we already have sheet with current acc name when skip
					if(prevACChier==accountsForDailyReport.getInt(1)) { //write data to the same hierarchy
						ResultSet accData = dataProvider.DailyEntriesData(accountsForDailyReport.getString(2));
						SaveACC(workbook, accData, accNumber);
						prevACChier=accountsForDailyReport.getInt(1);
					}else { //create new file for other hierarchy
						if(workbook.getNumberOfSheets()!=0) {
						fileOut = new FileOutputStream(workbook.getSheetName(0)+"_"+date+".xls");
						workbook.write(fileOut);
						fileOut.close();
						}
					
						workbook = new HSSFWorkbook();
						ResultSet AccData = dataProvider.DailyEntriesData(accountsForDailyReport.getString(2));
						SaveACC(workbook, AccData, accNumber);
						prevACChier=accountsForDailyReport.getInt(1);
						//System.out.println("**** Working");
					}
				}
			}catch(IOException ex) {
	        	System.err.println("IOException information");
	        	while (ex != null) {
					System.err.println("Error msg: " + ex.getMessage());
					
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
		
	}
	
	
	



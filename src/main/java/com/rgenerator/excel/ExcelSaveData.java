package com.rgenerator.excel;

import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.ResultSet;
import com.rgenerator.db.DbConnProvider;
import com.rgenerator.db.DbDataProvider;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelSaveData {

	MoveFile moveFile = new MoveFile();
	private String directoryName = moveFile.createDirectory(); 
	
	
	public void saveAccToSheet(Workbook workbook, ResultSet ACCdata, String ACCname, String reportName,
			String reportPeriod) {
		try {
			ResultSetMetaData data = ACCdata.getMetaData();
			int columnCount = data.getColumnCount();
			int currentRow = 4;

			Sheet sheet = workbook.createSheet(ACCname);

			Row rowReportName = sheet.createRow(0);
			Row rowReportPeriod = sheet.createRow(1);
			Row rowReportedASOn = sheet.createRow(2);

			Cell cell1 = rowReportName.createCell(0);
			Cell cell2 = rowReportPeriod.createCell(0);
			Cell cell3 = rowReportedASOn.createCell(0);

			HSSFRichTextString richTextStringRow1 = new HSSFRichTextString("Report Name : " + reportName);
			HSSFRichTextString richTextStringRow2 = new HSSFRichTextString(
					"Report Period (from-date, to-date): " + reportPeriod);
			HSSFRichTextString richTextStringRow3 = new HSSFRichTextString("Reported As On : "
					+ new SimpleDateFormat("dd.MM.yyyy HH:mm").format(Calendar.getInstance().getTime()));

			Font fontBolt = workbook.createFont();
			fontBolt.setBold(true);

			richTextStringRow1.applyFont(0, 12, fontBolt);
			richTextStringRow2.applyFont(0, 35, fontBolt);
			richTextStringRow3.applyFont(0, 17, fontBolt);

			cell1.setCellValue(richTextStringRow1);
			cell2.setCellValue(richTextStringRow2);
			cell3.setCellValue(richTextStringRow3);

			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
			sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));

			// Header style
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setColor(IndexedColors.BLACK.getIndex());
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			headerCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
			headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			// Date style
			CellStyle dateCellStyle = workbook.createCellStyle();
			CreationHelper createHelper = workbook.getCreationHelper();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("DD.MM.YYYY"));
			dateCellStyle.setAlignment(HorizontalAlignment.CENTER);
			dateCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			// Datestamp style
			CellStyle datestampCellStyle = workbook.createCellStyle();
			datestampCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("DD.MM.YYYY HH:mm:ss.000"));
			datestampCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			datestampCellStyle.setAlignment(HorizontalAlignment.CENTER);

			// Text style
			CellStyle textCellStyle = workbook.createCellStyle();
			textCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			textCellStyle.setAlignment(HorizontalAlignment.CENTER);

			// System.out.println("**** Writing to file");
			Row row = sheet.createRow(currentRow++);
			for (int i = 1; i <= columnCount; i++) {
				Cell cell = row.createCell(i - 1);
				cell.setCellValue(data.getColumnLabel(i));
				
				cell.setCellStyle(headerCellStyle);
			}

			boolean isDataExist = false;
			while (ACCdata.next()) {
				isDataExist = true;
				row = sheet.createRow(currentRow++);
				for (int i = 1; i <= columnCount; i++) {
					Cell cell = row.createCell(i - 1);

					if (data.getColumnType(i) == java.sql.Types.DATE) {
						cell.setCellStyle(dateCellStyle);
						cell.setCellValue(ACCdata.getDate(i));

					} else if (data.getColumnType(i) == java.sql.Types.TIMESTAMP) {
						cell.setCellStyle(datestampCellStyle);
						cell.setCellValue(ACCdata.getTimestamp(i));

					} else {
						cell.setCellValue(ACCdata.getString(i));
						cell.setCellStyle(textCellStyle);
					}
				}
			}
			if (!isDataExist) {
				workbook.removeSheetAt(workbook.getSheetIndex(sheet));
				return;
			}
			for (int i = 0; i < columnCount; i++) {
				sheet.autoSizeColumn(i);
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

	/*
	 * public void saveHierarchy(String account) { DbConnProvider server = new
	 * DbConnProvider(); Connection connection = server.openConn(); DbDataProvider
	 * dataProvider = new DbDataProvider(connection);
	 * System.err.println("**** Saving hierarchy"); if (connection != null) { try {
	 * ResultSet hier = dataProvider.getHierarchy(account); Workbook workbook = new
	 * HSSFWorkbook(); while (hier.next()) { ResultSet accNumberData =
	 * dataProvider.getAccountNumberForReport(hier.getString(1));
	 * accNumberData.next(); String accNumber = accNumberData.getString(1);
	 * System.out.println("**** " + accNumber);
	 * 
	 * ResultSet accData = dataProvider.getDetailsData(hier.getString(1));
	 * saveAccToSheet(workbook, accData, accNumber, "Daily");
	 * 
	 * } FileOutputStream fileOut = new FileOutputStream(account + "_" +
	 * LocalDate.now() + ".xls"); workbook.write(fileOut); fileOut.close(); } catch
	 * (IOException ex) { System.err.println("IOException information"); while (ex
	 * != null) { System.err.println("Error msg: " + ex.getMessage());
	 * 
	 * } } catch (SQLException ex) { System.err.println("SQLException information");
	 * while (ex != null) { System.err.println("Error msg: " + ex.getMessage());
	 * System.err.println("SQLSTATE: " + ex.getSQLState());
	 * System.err.println("Error code: " + ex.getErrorCode()); ex.printStackTrace();
	 * ex = ex.getNextException(); // For drivers that support chained exceptions }
	 * } }
	 * 
	 * }
	 */

	public void dailyEnries(String date) {
		System.out.println("--------------Daily-------------");
		DbConnProvider server = new DbConnProvider();
		Connection connection = server.openConn();
		DbDataProvider dataProvider = new DbDataProvider(connection);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY");
		
		String reportFolderDailyName = "DAILY";
		String folderForDailyRepotrs = moveFile.createFolder(reportFolderDailyName, directoryName); 

		if (connection != null) {
			ResultSet accountsForDailyReport = dataProvider.dailyEntriesACC(date);
			getDailyReport(dataProvider, accountsForDailyReport, "Daily",
					formatter.format(LocalDate.now()) + "-" + formatter.format(LocalDate.now()), folderForDailyRepotrs);
		}
		
		server.endConn(connection);
	}

	public void weeklyEntries(String date) {
		System.out.println("--------------Weekly-------------");
		DbConnProvider server = new DbConnProvider();
		Connection connection = server.openConn();
		DbDataProvider dataProvider = new DbDataProvider(connection);

		String reportFolderWeeklyName = "WEEKLY";
		String folderForWeeklyRepotrs = moveFile.createFolder(reportFolderWeeklyName, directoryName); 
		
		if (connection != null) {

			ResultSet accountsForWeeklyReport = dataProvider.weeklyEntriesData(date);
			
			getScheduledReport(dataProvider, accountsForWeeklyReport, "Weekly", folderForWeeklyRepotrs);
		}
		server.endConn(connection);
	}
	
	public void monthlyEntries(String date) {
		System.out.println("--------------Monthly-------------");
		DbConnProvider server = new DbConnProvider();
		Connection connection = server.openConn();
		DbDataProvider dataProvider = new DbDataProvider(connection);

		String reportFolderMonthlyName = "MONTHLY";
		String folderForMonthlyRepotrs = moveFile.createFolder(reportFolderMonthlyName, directoryName); 
		
		if (connection != null) {

			ResultSet accountsForWeeklyReport = dataProvider.monthlyEntriesData(date);
			
			getScheduledReport(dataProvider, accountsForWeeklyReport, "Monthly", folderForMonthlyRepotrs);
		}
		
		server.endConn(connection);
	}
	
	private void getScheduledReport(DbDataProvider dataProvider, ResultSet accountsForReport, String reportName, String filePath) {
		
		SimpleDateFormat dateFormat=new SimpleDateFormat("dd.MM.yyyy");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY");
		Workbook workbook = new HSSFWorkbook();
		FileOutputStream fileOut = null;
		int prevACChier = -1;

		try {
			while (accountsForReport.next()) {
				
				
				
				ResultSet accNumberData = dataProvider
						.getAccountNumberForReport(accountsForReport.getString(2));

				accNumberData.next();
				String accNumber = accNumberData.getString(2);

				if (accNumber == null)
					continue;
				if (workbook.getSheet(accNumber) != null)
					continue; // if we already have sheet with current acc name when skip
				if (prevACChier == accountsForReport.getInt(1)) { // write data to the same hierarchy
					System.out.println("**** " + accNumber);
					ResultSet accData = dataProvider.getDetailsData(accountsForReport.getString(2));
					saveAccToSheet(workbook, accData, accNumber, reportName,
							dateFormat.format(accountsForReport.getDate(6)) + " - "
									+ formatter.format(LocalDate.now()) );
					prevACChier = accountsForReport.getInt(1);
				} else { // create new file for other hierarchy
					System.out.println("**** " + accNumber);
					if (workbook.getNumberOfSheets() != 0) {
						ResultSet topAcc = dataProvider.getTopAccountNumber(accountsForReport.getString(1));
						if(topAcc.next()) {
							String fileName;
						if(topAcc.getString(1)==null)
							fileName = topAcc.getString(3);
						else
							fileName = topAcc.getString(1);
						
							fileOut = new FileOutputStream(filePath+"/"+fileName + "_" + dateFormat.format(accountsForReport.getDate(6)) + "-"
									+  formatter.format(LocalDate.now())+ "("+reportName+").xls");

						workbook.write(fileOut);
						}
						fileOut.close();
					}

					workbook = new HSSFWorkbook();
					ResultSet AccData = dataProvider.getDetailsData(accountsForReport.getString(2));
					saveAccToSheet(workbook, AccData, accNumber, reportName,
							dateFormat.format(accountsForReport.getDate(6)) + " - "
									+ formatter.format(LocalDate.now()));
					prevACChier = accountsForReport.getInt(1);
				    
				}
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
		} catch (IOException ex) {
			System.err.println("IOException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());

			}
		}
	}

	private void getDailyReport(DbDataProvider dataProvider, ResultSet accountsForReport, String reportName,
			String reportPeriod, String filePath) {

		Workbook workbook = new HSSFWorkbook();
		FileOutputStream fileOut = null;
		int prevACChier = -1;

		try {
			while (accountsForReport.next()) {
				ResultSet accNumberData = dataProvider.getAccountNumberForReport(accountsForReport.getString(2));

				accNumberData.next();
				String accNumber = accNumberData.getString(2);

				if (accNumber == null)
					continue;
				if (workbook.getSheet(accNumber) != null)
					continue; // if we already have sheet with current acc name when skip
				if (prevACChier == accountsForReport.getInt(1)) { // write data to the same hierarchy
					System.out.println("**** " + accNumber);
					ResultSet accData = dataProvider.getDetailsData(accountsForReport.getString(2));
					saveAccToSheet(workbook, accData, accNumber, reportName, reportPeriod);
					prevACChier = accountsForReport.getInt(1);
				} else { // create new file for other hierarchy
					System.out.println("**** " + accNumber);
					if (workbook.getNumberOfSheets() != 0) {
						ResultSet topAcc = dataProvider.getTopAccountNumber(accountsForReport.getString(1));
						if(topAcc.next()) {
							String fileName;
						if(topAcc.getString(1)==null)
							fileName = topAcc.getString(3);
						else
							fileName = topAcc.getString(1);
							fileOut = new FileOutputStream(filePath+"/"+fileName + "_" + reportPeriod + "(" + reportName + ").xls");
						workbook.write(fileOut);
						
						}
						fileOut.close();
					}

					workbook = new HSSFWorkbook();
					ResultSet AccData = dataProvider.getDetailsData(accountsForReport.getString(2));
					saveAccToSheet(workbook, AccData, accNumber, reportName, reportPeriod);
					prevACChier = accountsForReport.getInt(1);
					// System.out.println("**** Working");
				}
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
		} catch (IOException ex) {
			System.err.println("IOException information");
			while (ex != null) {
				System.err.println("Error msg: " + ex.getMessage());

			}
		}

	}

	
}

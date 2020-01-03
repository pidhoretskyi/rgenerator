package com.rgenerator.excel;

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
		
			while(ACCdata.next()) {
				row = sheet.createRow(currentRow++);
				for(int i=1; i<=columnCount; i++) {
					row.createCell(i-1).setCellValue(ACCdata.getString(i));
				}
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
	
	public void SaveHierarchy() {
		
	}
	
	
	

}

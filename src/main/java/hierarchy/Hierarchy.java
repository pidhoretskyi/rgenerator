package hierarchy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

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

import com.ibm.db2.jcc.am.Connection;
import com.rgenerator.db.DbConnProvider;
import com.rgenerator.db.DbDataProvider;
import com.rgenerator.excel.MoveFile;

import reader.Interest;
import reader.ReportGenerator;

public class Hierarchy {
	ArrayList<HierRecord> hier;

	private DbConnProvider server;
	private Connection connection;
	private DbDataProvider dataProvider;

	private LocalDate fromDate;
	private LocalDate toDate;
	private String reportName;
	private DateTimeFormatter formatter;

	private String directoryName;
	private String folderForMonthlyRepotrs;
	private String folderForWeeklyRepotrs;
	private String folderForQuarterReports;
	private String saveFolder;

	private int reportType;

	public Hierarchy() {
		MoveFile moveFile = new MoveFile();
		String directoryName = moveFile.createDirectory();
		folderForMonthlyRepotrs = moveFile.createFolder("MONTHLY", directoryName);
		folderForWeeklyRepotrs = moveFile.createFolder("WEEKLY", directoryName);
		folderForQuarterReports = moveFile.createFolder("QUARTER", directoryName);

	}

	private String getType(String accID) {
		String type = "";
		for (int i = 0; i < hier.size(); i++)
			if (hier.get(i).getACC_ID().equals(accID)) {
				type = hier.get(i).getACCTYPE_CATEGORY();
				break;
			}
		return type;
	}

	private String getNumber(String accID) {
		String number = "";
		for (int i = 0; i < hier.size(); i++)
			if (hier.get(i).getACC_ID().equals(accID)) {
				if (hier.get(i).getACC_NUMBER() == null)
					number = hier.get(i).getACC_NAME();
				else
					number = hier.get(i).getACC_NUMBER();
				break;
			}
		return number;
	}

	private ArrayList<String> getChildren(String accID) {
		ArrayList<String> children = new ArrayList<String>();

		for (int i = 0; i < hier.size(); i++) {
			if (hier.get(i).getPARRENT_ACC_ID() == null)
				continue;
			if (hier.get(i).getPARRENT_ACC_ID().equals(accID))
				children.add(hier.get(i).getACC_ID());
		}
		return children;
	}

	public void getHierarchyReport(String hierID, LocalDate fromDate, LocalDate toDate, int reportType) {
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.reportType = reportType;
		if (reportType == 1) {// Weekly
			this.reportName = "Weekly";
			saveFolder = folderForWeeklyRepotrs;
		} else if (reportType == 2) {// Monthly
			this.reportName = "Monthly";
			saveFolder = folderForMonthlyRepotrs;
		} else { // Quarter
			this.reportName = "Quarterly";
			saveFolder = folderForQuarterReports;
		}
		formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		server = new DbConnProvider();
		server.connectionToFirstDB();
		connection = server.openConn();
		dataProvider = new DbDataProvider(connection);
		if (connection != null) {
			ResultSet set = dataProvider.getAllAggrAccInHierarchy(toDate.toString(), hierID);
			hier = new ArrayList<HierRecord>();
			try {
				while (set.next()) {
					hier.add(new HierRecord(set.getString(1), set.getString(2), set.getString(3), set.getString(6),
							set.getString(7)));
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

			generateReport("", hier.get(0).getACC_ID());
			server.endConn(connection);
		} else {
			// TODO
			// Log problem with server connection
		}
	}

	private ArrayList<AccountCollection> generateReport(String prephix, String accID) {
		ArrayList<AccountCollection> data = new ArrayList<Hierarchy.AccountCollection>();
		String fileName = prephix + "(" + getNumber(accID) + ")";
		System.out.println(">>>" + accID);
		if (getType(accID).equals("TRAN")) {
			System.out.println("Get tran data");
			ArrayList<Interest> buf = getReport(accID);
			if (buf.size() != 0)
				data.add(new AccountCollection(buf, accID));
			// saveACC(book, ACCdata, getNumber(accID), reportName,
			// formatter.format(fromDate) + " - " + formatter.format(toDate));
		} else {
			System.out.println("Find children");
			ArrayList<String> children = getChildren(accID);
			for (int i = 0; i < children.size(); i++) {
				ArrayList<AccountCollection> report = generateReport(fileName, children.get(i));
				for (int j = 0; j < report.size(); j++) {
					data.add(report.get(j));
				}
			}
			Workbook book = new HSSFWorkbook();
			if (data.size() != 0) {

				for (int i = 0; i < data.size(); i++) {
					saveACC(book, data.get(i).interest, getNumber(data.get(i).accID), fileName);
				}
			}
			if (book.getNumberOfSheets() != 0) {
				try {
					System.out.println("File name: " + fileName);
					FileOutputStream fileOut = new FileOutputStream(
							saveFolder + "/" + fileName + "_" + formatter.format(toDate) + "(" + reportName + ").xls");

					book.write(fileOut);
					fileOut.close();
				} catch (IOException ex) {
					System.err.println("IOException information");

					System.err.println("Error msg: " + ex.getMessage());

				}
			}
		}
		return data;
	}

	private ArrayList<Interest> getReport(String accID) {
		LocalDate from = fromDate;
		LocalDate to = toDate;
		try {
			ResultSet acc_result;
			if (reportType == 1)
				acc_result = dataProvider.weeklyEntriesData(accID, to.toString());
			else if (reportType == 2)
				acc_result = dataProvider.monthlyEntriesData(accID, to.toString());
			else
				acc_result = dataProvider.quarterEntriesData(accID, from.toString(), to.toString());

			if (acc_result.next()) {
				if (acc_result.getString(3).compareTo(toDate.toString()) > 0) {
					return new ArrayList<Interest>(); // no interest were found
				} else {

					if (acc_result.getString(3).compareTo(fromDate.toString()) > 0) {
						from = Date.valueOf(acc_result.getString(3)).toLocalDate();
						// System.out.println("<<<<<<" + from.toString());
					}

				}
			} else
				return new ArrayList<Interest>();
			if (reportType == 3) {
				to = Date.valueOf(acc_result.getString(4)).toLocalDate();
				while (acc_result.next()) {
					if (acc_result.getString(4).compareTo(to.toString()) > 0) {
						from = Date.valueOf(acc_result.getString(3)).toLocalDate();
					}
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
		}
		ReportGenerator generator = new ReportGenerator();
		return generator.getReport(accID, from, to);
	}

	private void saveACC(Workbook workbook, ArrayList<Interest> ACCdata, String ACCname, String reportName) {

		Sheet sheet = workbook.createSheet(ACCname);
		// Create header information
		String reportPeriod = formatter.format(fromDate) + " - " + formatter.format(toDate);

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

		// Date style
		CellStyle dateCellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("DD.MM.YYYY"));
		dateCellStyle.setAlignment(HorizontalAlignment.CENTER);
		dateCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		dateCellStyle.setAlignment(HorizontalAlignment.CENTER);
		dateCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

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

		// Percentage style 45.548%
		CellStyle perStyle = workbook.createCellStyle();
		perStyle.setDataFormat(workbook.createDataFormat().getFormat("0.000%"));
		perStyle.setAlignment(HorizontalAlignment.CENTER);
		perStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		// Calcu style 154,456.1531
		CellStyle calcuStyle = workbook.createCellStyle();
		calcuStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.0000"));
		calcuStyle.setAlignment(HorizontalAlignment.CENTER);
		calcuStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		// Money style
		CellStyle moneyStyle = workbook.createCellStyle();
		moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
		moneyStyle.setAlignment(HorizontalAlignment.CENTER);
		moneyStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		// Text style
		CellStyle textCellStyle = workbook.createCellStyle();
		textCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		textCellStyle.setAlignment(HorizontalAlignment.CENTER);

		ReportGenerator report = new ReportGenerator();
		int cellcount = 0;
		int currentRow = 4;
		Row row = sheet.createRow(currentRow++);
		Cell cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_accID());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_accName());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_ownerName());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_settlemNum());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_settleName());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_settleOwner());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_bankID());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_calcuType());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_calcuNum());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_toDate());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_currency());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_interestCredit());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_interestDebit());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_bearingBalance());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_rate());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_basis());
		cell.setCellStyle(headerCellStyle);
		cell = row.createCell(cellcount++);
		cell.setCellValue(report.getColum_settleType());
		cell.setCellStyle(headerCellStyle);

		for (int i = 0; i < ACCdata.size(); i++) {
			row = sheet.createRow(currentRow++);
			cellcount = 0;
			Interest record = ACCdata.get(i);
			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getAccID());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getAccName());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getOwnerName());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getSettlemNum());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getSettleName());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getSettleOwner());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getBankID());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getCalcuType());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getCalcuNum());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(dateCellStyle);
			cell.setCellValue(record.getToDate());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getCurrency());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(calcuStyle);
			cell.setCellValue(record.getInterestCredit());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(calcuStyle);
			cell.setCellValue(record.getInterestDebit());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(moneyStyle);
			cell.setCellValue(record.getBearingBalance());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(perStyle);
			cell.setCellValue(record.getRate());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getBasis());

			cell = row.createCell(cellcount++);
			cell.setCellStyle(textCellStyle);
			cell.setCellValue(record.getSettleType());
		}

		for (int i = 0; i < cellcount; i++) { // Autosize columns
			sheet.autoSizeColumn(i);
		}

	}

	private class AccountCollection {
		ArrayList<Interest> interest;
		String accID;

		public AccountCollection(ArrayList<Interest> interest, String accID) {
			this.interest = interest;
			this.accID = accID;
		}
	}

}

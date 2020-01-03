import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.ResultSet;
import com.rgenerator.db.DbConnProvider;
import com.rgenerator.db.DbDataProvider;
import com.rgenerator.excel.ExcelSaveData;

public class Test {
	
	public static void main(String[] args) {
		DbConnProvider server = new DbConnProvider();
		Connection connection = server.openConn();
		DbDataProvider dataProvider = new DbDataProvider(connection);
		LocalDate date = LocalDate.now();
		
		if(connection!=null) {
			try {
			ExcelSaveData ex = new ExcelSaveData();
			Workbook workbook = new HSSFWorkbook();
			ex.SaveACC(workbook, dataProvider.DailyEntriesData("LT542140030002190972"), "LT542140030002190972");
			FileOutputStream fileOut = new FileOutputStream("Hierarchy "+date+" (Daily).xls");
	        workbook.write(fileOut);
	        fileOut.close();
	        workbook.close();
	        
	        ex = new ExcelSaveData();
			workbook = new HSSFWorkbook();
			ex.SaveACC(workbook, dataProvider.MonthEntriesData("LT542140030002190972"), "LT542140030002190972");
			fileOut = new FileOutputStream("Hierarchy "+date+"(Monthly).xls");
	        workbook.write(fileOut);
	        fileOut.close();
	        workbook.close();

	        }catch(IOException ex) {
	        	System.err.println("IOException information");
	        	while (ex != null) {
					System.err.println("Error msg: " + ex.getMessage());
					
				}
	        }
			
		}
		

	}

}

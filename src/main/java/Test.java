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
		ExcelSaveData ex = new ExcelSaveData();
		ex.DailyEnries(LocalDate.now().toString());	
		//ex.SaveHierarchy("LT542140030002190972");
		}
}



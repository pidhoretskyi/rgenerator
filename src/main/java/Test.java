import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.ResultSet;
import com.rgenerator.db.DbConnProvider;
import com.rgenerator.db.DbDataProvider;
import com.rgenerator.excel.ExcelSaveData;

public class Test {
	
	public static void main(String[] args) throws ParseException {
		ExcelSaveData ex = new ExcelSaveData();
		
		//ex.dailyEnries("2020-01-12");
		
		//ex.weeklyEntries("2020-01-12");	
		ex.monthlyEntries("2020-01-14");
		
		//	ex.getWeeksForEntries(ex.getWeeksDates(2020, 0), "2020-01-27");
		
//		for(int i=0; i<dates.size(); i++) {
//			System.out.println("Week "+(i+1)+": from "+dates.get(i).get(0)+", to "+dates.get(i).get(1));
//		}
		//ex.dailyEnries(LocalDate.now().toString());	
		//ex.saveHierarchy("LT372140030001497825");
		}
}



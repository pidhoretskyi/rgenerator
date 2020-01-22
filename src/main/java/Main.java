import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;

import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.ResultSet;
import com.rgenerator.db.DbConnProvider;
import com.rgenerator.db.DbDataProvider;
import com.rgenerator.excel.ExcelSaveData;

public class Main {
	
	public static void main(String[] args)  {
		ExcelSaveData ex = new ExcelSaveData();

		//ex.monthlyEntries(LocalDate.now().minusDays(1).toString());
		//ex.weeklyEntries(LocalDate.now().minusDays(1).toString());
		//System.out.println(Date.valueOf(LocalDate.of(2019, 12, 5)));
		//ex.monthlyReport(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1), LocalDate.now());
		//long start = System.nanoTime();
		//ex.monthlyReport(LocalDate.of(2019, 12, 1), LocalDate.of(2019, 12, 31));
		ex.getReport(2, LocalDate.of(2019, 12, 30), LocalDate.of(2020, 1, 5));
		//ex.getReport(3, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 5));

		//long finish = System.nanoTime();
		//long time = finish-start;
		//System.out.println("Time: "+time/1000000000);
		
}
}


import java.text.ParseException;

import java.time.LocalDate;

import com.rgenerator.excel.ExcelSaveData;

public class Test {

	public static void main(String[] args) throws ParseException {
		ExcelSaveData ex = new ExcelSaveData();
		long time = System.currentTimeMillis();
		ex.startMonthlyReporting(LocalDate.of(2019, 12, 1), LocalDate.of(2019, 12, 31));
		ex.startWeeklyReporting(LocalDate.of(2019, 12, 23), LocalDate.of(2019, 12, 29));
		time = System.currentTimeMillis()-time;
		System.out.println(time/1000);
	}

}

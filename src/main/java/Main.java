
import java.time.DayOfWeek;
import java.time.LocalDate;

import com.rgenerator.excel.ExcelSaveData;

public class Main {

	public static void main(String[] args) {
		ExcelSaveData ex = new ExcelSaveData();
		ex.startMonthlyReporting(LocalDate.now().minusDays(1).withDayOfMonth(1), LocalDate.now().minusDays(1));
		ex.startWeeklyReporting(LocalDate.now().minusDays(1).with(DayOfWeek.MONDAY), LocalDate.now().minusDays(1));

	}
}

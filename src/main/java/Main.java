import java.text.ParseException;
import java.time.LocalDate;

import com.rgenerator.excel.ExcelSaveData;

public class Main {
	public static void main(String[] args) throws ParseException {
		ExcelSaveData ex = new ExcelSaveData();

		ex.monthlyEntries(LocalDate.now().toString());
		ex.weeklyEntries(LocalDate.now().toString());

	}
}

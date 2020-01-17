
import java.text.ParseException;

import com.rgenerator.excel.ExcelSaveData;

public class Test {

	public static void main(String[] args) throws ParseException {
		ExcelSaveData ex = new ExcelSaveData();

		// ex.dailyEnries("2020-01-12");

		//ex.weeklyEntries("2019-12-31");
		ex.monthlyEntries("2020-01-16");

	}

	
}

package reader;

import java.sql.SQLException;
import java.util.ArrayList;

import com.ibm.db2.jcc.am.ResultSet;

public class AccountsForReport {
	
	public ArrayList<Account> accounts;
	
	public AccountsForReport(ResultSet set) throws SQLException {
		accounts = new ArrayList<Account>();
		while(set.next()) {
			accounts.add(new Account(set.getString(1), set.getString(2)));
		}
	}

}

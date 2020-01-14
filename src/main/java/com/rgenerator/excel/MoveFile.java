package com.rgenerator.excel;

import java.io.File;

public class MoveFile {



	public String createFolder(String foldername, String rootDir) {

		File reportFolder = new File(rootDir + "/" + foldername);
		reportFolder.mkdir();

		return reportFolder.toString();
	}

	public String createDirectory() {

		File reportFolder = null;
		String reportFolderName = "REPORTS";
		String osname = System.getProperty("os.name", "").toLowerCase();

		if (osname.startsWith("windows")) {
			System.out.println("Hello "+ osname);
			reportFolder = new File("C:/" + reportFolderName);
			reportFolder.mkdir();
			
		} else if (osname.startsWith("linux")) {
			System.out.println("Hello "+ osname);
			reportFolder = new File("/root/" + reportFolderName);
			reportFolder.mkdir();
			
		} else {
			System.out.println("Sorry, your operating system is different");
		}

		return reportFolder.toString();
	}

}

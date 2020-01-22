package com.rgenerator.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import com.rgenerator.db.DbConnProvider;

public class MoveFile {

	private String fileName = "META-INF/application.properties";
	private InputStream inputStream = DbConnProvider.class.getClassLoader().getResourceAsStream(fileName);
	private Properties properties = new Properties();

	String windowsDirectory = "";
	String unixDirectory = "";

	public String createFolder(String foldername, String rootDir) {

		File reportFolder = new File(rootDir + "/" + foldername);
		reportFolder.mkdir();

		return reportFolder.toString();
	}

	public String createDirectory() {

		try {
			inputStream = DbConnProvider.class.getClassLoader().getResourceAsStream(fileName);
			if (inputStream == null) {
				System.out.println("Sorry, unable to find " + fileName);
//				return;
			}
			properties.load(inputStream);
			windowsDirectory = properties.getProperty("windowsDirectory");
			unixDirectory = properties.getProperty("unixDirectory");
		} catch (IOException e) {
			e.printStackTrace();
		}

		File reportFolder = null;
		String reportFolderName = "REPORTS";
		String osname = System.getProperty("os.name", "").toLowerCase();

		if (osname.startsWith("windows")) {
			System.out.println("Hello " + osname);

			reportFolder = new File(windowsDirectory + reportFolderName);
			reportFolder.exists();
			reportFolder.mkdir();

		} else if (osname.startsWith("linux")) {
			System.out.println("Hello " + osname);
			unixDirectory = properties.getProperty("unixDirectory");
			reportFolder = new File(unixDirectory + reportFolderName);
			reportFolder.exists();
			reportFolder.mkdir();

		} else {
			System.out.println("Sorry, your operating system is different");
		}

		return reportFolder.toString();
	}

}

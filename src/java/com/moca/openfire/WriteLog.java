package com.moca.openfire;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

public class WriteLog {
	
	public void createNewLog(String fileName) throws IOException {
		File oldFile = new File(fileName);
		if (oldFile.exists()) {
			String rootPath = oldFile.getParent();
			if(oldFile.length() > 30000000) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				File newFile = new File(rootPath + File.separator + oldFile.getName() + "-" + format.format(new Date()) + ".log");
				if (oldFile.renameTo(newFile)) {
					new WriteLog().fileWriter(fileName, oldFile.getName() + ": ------------------------- 创建新日志  ----- " + format.format(new Date()));
				}
			}
		}
	}

	public void fileWriter(String fileName, String logContent) throws IOException {
		FileWriter writer = new FileWriter(fileName, true);
		writer.write(logContent);
		writer.write("\r\n");
		writer.flush();
		writer.close();
	}

	public void fileWriter(String fileName, TreeSet<String> logList) throws IOException {
		FileWriter writer = new FileWriter(fileName, true);
		for (String log : logList) {
			writer.write(log);
			writer.write("\r\n");
		}
		writer.flush();
		writer.close();
	}

	public TreeSet<String> readFileByLines(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String tempString = null;
		TreeSet<String> nums = new TreeSet<String>();
		while ((tempString = reader.readLine()) != null) {
			nums.add(tempString);
		}
		reader.close();
		return nums;
	}
	
}

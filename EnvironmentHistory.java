package com.kmichaelfox.agents.sl;

import java.io.File;
import java.util.ArrayList;

import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.EvaluationInfo;

public class EnvironmentHistory {
	private ArrayList<String> history;
	private DataWriter out;
	
	private String filename;
	private String path;
	
	public EnvironmentHistory() {
		filename = "EnvironmentHistory_"+System.currentTimeMillis()+".arff";
		path = "~/Documents/Class Documents/2016_Spring/GameAI/SL Assignment Data";
		
		if (path.startsWith("~" + File.separator)) {
		    path = System.getProperty("user.home") + path.substring(1);
		}
		
		if (!(new File(path)).exists()) {
			System.out.println("Supplied path is not valid. Defaulting to Desktop.");
			path = System.getProperty("user.home");
		}
		
		//path += ("/"+filename);
		
		System.out.println("Logging environment history to: ["+path+"/"+filename+"]");
		out = new DataWriter(path+"/"+filename);
		history = new ArrayList<String>();
	}
	
	public void logHistory(Environment e) {
		EvaluationInfo info = e.getEvaluationInfo();
		history.add(info.toStringSingleLine());
	}
	
	public void logHistory(String s) {
		//history.add(System.currentTimeMillis() + " " + s);
		history.add(s);
	}
	
	public void writeHistoryToFile() {
		for (int i = 0; i < history.size(); i++) {
			out.println(history.get(i));
		}
		out.closeFile();
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getFilePath() {
		return path;
	}
	
	public String getFilenameAndPath() {
		return path+"/"+filename;
	}
	
	// alternative method - custom struct for POD-based environment history
	private class EnvironmentData {
		
	}
	
	
}
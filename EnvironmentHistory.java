package com.kmichaelfox.agents.sl;

import java.io.File;
import java.util.ArrayList;

import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.EvaluationInfo;

public class EnvironmentHistory {
	private ArrayList<String> history;
	private DataWriter out;
	
	public EnvironmentHistory() {
		String path = "~/Documents/Class Documents/2016_Spring/GameAI/SL Assignment Data/EnvironmentHistory_"+System.currentTimeMillis()+".txt";
		if (path.startsWith("~" + File.separator)) {
		    path = System.getProperty("user.home") + path.substring(1);
		}
		System.out.println("Logging environment history to: ["+path+"]");
		out = new DataWriter(path);
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
	
	// alternative method - custom struct for POD-based environment history
	private class EnvironmentData {
		
	}
}
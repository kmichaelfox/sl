package com.kmichaelfox.agents.sl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

public class ClassifierRun {
	public static void main(String[] args) throws Exception {
		//		ObjectInputStream ois = new ObjectInputStream(
		//				new FileInputStream("/Users/kellyfox/Documents/Class Documents/2016_Spring/GameAI/SL Assignment Data/good_single_level_low_generations_data/large_identifiers1.model"));
		//		Classifier cls = (Classifier)ois.readObject();
		//		ois.close();
		File folder = new File("/Users/kellyfox/Documents/Class Documents/2016_Spring/GameAI/SL Assignment Data/1000_gen_large_id");
		File[] listOfDirs = folder.listFiles();

		DataWriter out = new DataWriter(folder.getAbsolutePath()+"/ClassifierAgentEvaluations.txt");
		for (int i = 0; i < listOfDirs.length; i++) {
			if (listOfDirs[i].isDirectory()) {
				File[] listOfFiles = listOfDirs[i].listFiles();
				for (int j = 0; j < listOfFiles.length; j++) {
					//System.out.println("found file "+listOfFiles[j].getName());
					String extension = "";

					int index = listOfFiles[j].getName().lastIndexOf('.');
					if (index > 0) {
						extension = listOfFiles[j].getName().substring(index+1);
					}
					//System.out.println(extension);
					//System.out.println((extension.equals("model"))?"found model file":"");
					if (extension.equals("model")) {
						System.out.println("Running ClassifierAgent for ["+listOfFiles[j]+"]");
						//Object[] inputData = SerializationHelper.readAll("/Users/kellyfox/Documents/Class Documents/2016_Spring/GameAI/SL Assignment Data/good_single_level_low_generations_data/large_identifiers1.model");
						Object[] inputData = SerializationHelper.readAll(listOfFiles[j].getAbsolutePath());
						//Classifier cls = (Classifier)SerializationHelper.read("/Users/kellyfox/Documents/Class Documents/2016_Spring/GameAI/SL Assignment Data/good_single_level_low_generations_data/large_identifiers1.model");

						MarioAIOptions marioAIOptions = new MarioAIOptions("");
						marioAIOptions.setVisualization(false);
						marioAIOptions.setFPS(200);
						Agent agent = new ClassifierAgent((Classifier)inputData[0], (DataSource)inputData[1]);
						marioAIOptions.setAgent(agent);
						BasicTask basicTask = new BasicTask(marioAIOptions);

						basicTask.runSingleEpisode(1);
						//System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
						out.println("\n\n\n\n"+listOfFiles[j].getName());
						out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
						//basicTask.get
					}
				}
			}
		}
		System.out.println("Classifier Run Completed");
		System.exit(0);
	}
}

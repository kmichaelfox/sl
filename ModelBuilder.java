package com.kmichaelfox.agents.sl;

import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import weka.core.Attribute;
import weka.core.Instances;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;

public class ModelBuilder {
	
//	public ModelBuilder() {
//		
//	}
	
	public static void main(String[] args) throws Exception {
		String path = "~/Documents/GameAI/training_data/small_identifiers_data";
		String filename = "small_identifiers_data1.arff";
		if (path.startsWith("~" + File.separator)) {
		    path = System.getProperty("user.home") + path.substring(1);
		}
		
		System.out.println("Opening data file: ["+(path+"/"+filename)+"]");
		
		// read in data
		DataSource source = new DataSource(path+"/"+filename);
		Instances data = source.getDataSet();
		
		if (data.classIndex() == -1)
			data.setClassIndex(data.numAttributes() - 1);
		
		// create classifier
		//String[] options = new String[1];
		//options[0] = "-U";
		RandomForest tree = new RandomForest();
		//tree.setOptions(options);
		tree.buildClassifier(data);
		
		// cross-validate
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(tree, data, 10, new Random(1));
		System.out.println("Error rate: "+eval.errorRate());
		System.out.println("Correct: "+eval.correct());
		System.out.println("Percent Correct: "+eval.pctCorrect());
		System.out.println("Root mean squared error: "+eval.rootMeanSquaredError());
		System.out.println("Summary:\n"+eval.toSummaryString());
		
		// write model to file
		System.out.println("Writing model to file: ["+path+"/small_identifier.model"+"]");
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(path+"/small_identifiers.model"));
		
		oos.writeObject(tree);
		oos.flush();
		oos.close();
	}
}

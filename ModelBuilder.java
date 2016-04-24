package com.kmichaelfox.agents.sl;

import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Date;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesSimple;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

public class ModelBuilder {

	//	public ModelBuilder() {
	//		
	//	}

	public static void main(String[] args) throws Exception {
		// verify argument provided
		if (args.length != 2) throw new IllegalArgumentException("missing argument: path to an arff file");

		// verify file exists
		File f = new File(args[0]);
		if (!f.isFile()) throw new IllegalArgumentException("file \""+f.getName()+"\" does not exist.");

		//if (path.startsWith("~" + File.separator)) {
		//    path = System.getProperty("user.home") + path.substring(1);
		//}

		System.out.println("Opening data file: ["+f.getPath()+"]");

		// read in data
		DataSource source = new DataSource(f.getPath());
		Instances data = source.getDataSet();

		if (data.classIndex() == -1)
			data.setClassIndex(data.numAttributes() - 1);

		String[] classifierList = {
				"RandomForest",
				"MultilayerPerceptron_H5_N50",
				"MultilayerPerceptron_H10_N50",
				"MultilayerPerceptron_H15_N50",
				"MultilayerPerceptron_H5_N100",
				"MultilayerPerceptron_H10_N100",
				"MultilayerPerceptron_H15_N100",
				"MultilayerPerceptron_H5_N200",
				"MultilayerPerceptron_H10_N200",
				"MultilayerPerceptron_H15_N200",
				"NaiveBayes",
				"IBk_K1",
				"IBk_K5",
				"IBk_K9",
				"J48_C0.01",
				"J48_C0.1",
				"J48_C0.5",
				"SMO_C0.01_P1",
				"SMO_C0.1_P1",
				"SMO_C1_P1",
				"SMO_C10_P1",
				"SMO_C50_P1",
				"SMO_C0.01_P2",
				"SMO_C0.1_P2",
				"SMO_C1_P2",
				"SMO_C10_P2",
				"SMO_C50_P2",
				"SMO_C0.01_P3",
				"SMO_C0.1_P3",
				"SMO_C1_P3",
				"SMO_C10_P3",
				"SMO_C50_P3",
				"SMO_C0.01_RBF1",
				"SMO_C0.1_RBF1",
				"SMO_C1_RBF1",
				"SMO_C10_RBF1",
				"SMO_C50_RBF1",
				"SMO_C0.01_RBF5",
				"SMO_C0.1_RBF5",
				"SMO_C1_RBF5",
				"SMO_C10_RBF5",
				"SMO_C50_RBF5",
				"SMO_C0.01_RBF10",
				"SMO_C0.1_RBF10",
				"SMO_C1_RBF10",
				"SMO_C10_RBF10",
				"SMO_C50_RBF10"
		};

		String summaryPath = args[1];
		if (summaryPath.startsWith("~" + File.separator)) {
			summaryPath = System.getProperty("user.home") + summaryPath.substring(1);
		} else if (!summaryPath.startsWith(File.separator)) {
			summaryPath = f.getParent() + "/evaluationSummaryStatistics.csv";
		}

		// create summary file
		DataWriter evalSummary = new DataWriter(summaryPath);
		
		// write headers
		evalSummary.print("Classifier,");
		evalSummary.print("Pct Correct,");
		evalSummary.print("Kappa,");
		evalSummary.print("Mean Abs. Error,");
		evalSummary.print("Root Mean Sq. Error,");
		evalSummary.print("Relative Absolute Error,");
		evalSummary.println("Rool Rel. Sq. Error");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		for (int i = 0; i < classifierList.length; i++) {
			//report classifier progress
		    Date now = new Date();
		    String strDate = sdf.format(now);
		    System.out.println("[Starting build of "+classifierList[i]+" at "+strDate+"]");
			
			
			// create classifier
			//RandomForest classifier = new RandomForest();
			Classifier classifier = getClassifier(i);
			classifier.buildClassifier(data);

			// cross-validate
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(classifier, data, 10, new Random(1));

			// write model to file
			String savePath = args[1];
			if (savePath.startsWith("~" + File.separator)) {
				savePath = System.getProperty("user.home") + savePath.substring(1);
			} else if (!savePath.startsWith(File.separator)) {
				savePath = f.getParent() + "/" + classifierList[i] + "_" + savePath;
			}

			System.out.println("Writing model to file: ["+savePath+"]");
			Object[] output = {classifier, source};
			SerializationHelper.writeAll(savePath, output);


			// log evaluation data
			DataWriter evalInfo = new DataWriter(savePath+".eval.txt");
			evalInfo.println("Error rate: "+eval.errorRate());
			evalInfo.println("Correct: "+eval.correct());
			evalInfo.println("Percent Correct: "+eval.pctCorrect());
			evalInfo.println("Root mean squared error: "+eval.rootMeanSquaredError());
			evalInfo.println("Summary:\n"+eval.toSummaryString());
			evalInfo.closeFile();
			
			evalSummary.print(classifierList[i]+",");
			evalSummary.print(eval.pctCorrect()+",");
			evalSummary.print(eval.kappa()+",");
			evalSummary.print(eval.meanAbsoluteError()+",");
			evalSummary.print(eval.rootMeanSquaredError()+",");
			evalSummary.print(eval.relativeAbsoluteError()+",");
			evalSummary.println(eval.rootRelativeSquaredError()+"");

			//report classifier progress
		    now = new Date();
		    strDate = sdf.format(now);
		    System.out.println("[Completed build of "+classifierList[i]+" at "+strDate+"]");
			
		}
		evalSummary.closeFile();

		System.out.println("ModelBuilder Completed");
	}

	private static Classifier getClassifier(int id) throws Exception {
		switch (id) {
		//"RandomForest",
		case 0: {
			return new RandomForest();
		}

		//"MultilayerPerceptrion_H5_N50",
		case 1: {
			String[] options = weka.core.Utils.splitOptions("-H 5 -N 50");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}

		//"MultilayerPerceptron_H10_N50",
		case 2: {
			String[] options = weka.core.Utils.splitOptions("-H 10 -N 50");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}
		//"MultilayerPerceptron_H15_N50",
		case 3: {
			String[] options = weka.core.Utils.splitOptions("-H 15 -N 50");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}

		//"MultilayerPerceptron_H5_N100",
		case 4: {
			String[] options = weka.core.Utils.splitOptions("-H 5 -N 100");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}

		//"MultilayerPerceptron_H10_N100",
		case 5: {
			String[] options = weka.core.Utils.splitOptions("-H 10 -N 100");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}

		//"MultilayerPerceptron_H15_N100",
		case 6: {
			String[] options = weka.core.Utils.splitOptions("-H 15 -N 100");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}

		//"MultilayerPerceptron_H5_N200",
		case 7: {
			String[] options = weka.core.Utils.splitOptions("-H 5 -N 200");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}

		//"MultilayerPerceptron_H10_N200",
		case 8: {
			String[] options = weka.core.Utils.splitOptions("-H 10 -N 200");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}

		//"MultilayerPerceptron_H15_N200",
		case 9: {
			String[] options = weka.core.Utils.splitOptions("-H 15 -N 200");
			MultilayerPerceptron c = new MultilayerPerceptron();
			c.setOptions(options);
			return c;
		}

		//"NaiveBayes",
		case 10: {
			return new NaiveBayes();
		}

		//"IBk_K1",
		case 11: {
			String[] options = weka.core.Utils.splitOptions("-K 1");
			IBk c = new IBk();
			c.setOptions(options);
			return c;
		}

		//"IBk_K5",
		case 12: {
			String[] options = weka.core.Utils.splitOptions("-K 5");
			IBk c = new IBk();
			c.setOptions(options);
			return c;
		}

		//"IBk_K9",
		case 13: {
			String[] options = weka.core.Utils.splitOptions("-K 9");
			IBk c = new IBk();
			c.setOptions(options);
			return c;
		}

		//"J48_C0.01",
		case 14: {
			String[] options = weka.core.Utils.splitOptions("-C 0.01");
			J48 c = new J48();
			c.setOptions(options);
			return c;
		}

		//"J48_C0.1",
		case 15: {
			String[] options = weka.core.Utils.splitOptions("-C 0.1");
			J48 c = new J48();
			c.setOptions(options);
			return c;
		}

		//"J48_C0.5",
		case 16: {
			String[] options = weka.core.Utils.splitOptions("-C 0.5");
			J48 c = new J48();
			c.setOptions(options);
			return c;
		}

		//"SMO_C0.01_P1",
		case 17: {
			String[] options;
			PolyKernel k = new PolyKernel();
			options = weka.core.Utils.splitOptions("-C 0.01");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.1_P1",
		case 18: {
			String[] options;
			PolyKernel k = new PolyKernel();
			options = weka.core.Utils.splitOptions("-C 0.1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C1_P1",
		case 19: {
			String[] options;
			PolyKernel k = new PolyKernel();
			options = weka.core.Utils.splitOptions("-C 1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C10_P1",
		case 20: {
			String[] options;
			PolyKernel k = new PolyKernel();
			options = weka.core.Utils.splitOptions("-C 10");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C50_P1",
		case 21: {
			String[] options;
			PolyKernel k = new PolyKernel();
			options = weka.core.Utils.splitOptions("-C 50");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.01_P2",
		case 22: {
			String[] options = weka.core.Utils.splitOptions("-E 2");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.01");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.1_P2",
		case 23: {
			String[] options = weka.core.Utils.splitOptions("-E 2");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C1_P2",
		case 24: {
			String[] options = weka.core.Utils.splitOptions("-E 2");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C10_P2",
		case 25: {
			String[] options = weka.core.Utils.splitOptions("-E 2");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 10");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C50_P2",
		case 26: {
			String[] options = weka.core.Utils.splitOptions("-E 2");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 50");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.01_P3",
		case 27: {
			String[] options = weka.core.Utils.splitOptions("-E 3");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.01");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.1_P3",
		case 28: {
			String[] options = weka.core.Utils.splitOptions("-E 3");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C1_P3",
		case 29: {
			String[] options = weka.core.Utils.splitOptions("-E 3");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C10_P3",
		case 30: {
			String[] options = weka.core.Utils.splitOptions("-E 3");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 10");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C50_P3",
		case 31: {
			String[] options = weka.core.Utils.splitOptions("-E 3");
			PolyKernel k = new PolyKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 50");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.01_RBF1",
		case 32: {
			String[] options = weka.core.Utils.splitOptions("-G 1");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.01");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.1_RBF1",
		case 33: {
			String[] options = weka.core.Utils.splitOptions("-G 1");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C1_RBF1",
		case 34: {
			String[] options = weka.core.Utils.splitOptions("-G 1");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C10_RBF1",
		case 35: {
			String[] options = weka.core.Utils.splitOptions("-G 1");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 10");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C50_RBF1",
		case 36: {
			String[] options = weka.core.Utils.splitOptions("-G 1");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 50");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.01_RBF5",
		case 37: {
			String[] options = weka.core.Utils.splitOptions("-G 5");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.01");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.1_RBF5",
		case 38: {
			String[] options = weka.core.Utils.splitOptions("-G 5");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C1_RBF5",
		case 39: {
			String[] options = weka.core.Utils.splitOptions("-G 5");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C10_RBF5",
		case 40: {
			String[] options = weka.core.Utils.splitOptions("-G 5");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 10");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C50_RBF5",
		case 41: {
			String[] options = weka.core.Utils.splitOptions("-G 5");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 50");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.01_RBF10",
		case 42: {
			String[] options = weka.core.Utils.splitOptions("-G 10");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.01");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C0.1_RBF10",
		case 43: {
			String[] options = weka.core.Utils.splitOptions("-G 10");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 0.1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C1_RBF10",
		case 44: {
			String[] options = weka.core.Utils.splitOptions("-G 10");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 1");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C10_RBF10",
		case 45: {
			String[] options = weka.core.Utils.splitOptions("-G 10");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 10");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}

		//"SMO_C50_RBF10",
		case 46: 
		default: {
			String[] options = weka.core.Utils.splitOptions("-G 10");
			RBFKernel k = new RBFKernel();
			k.setOptions(options);
			options = weka.core.Utils.splitOptions("-C 50");
			SMO c = new SMO();
			c.setOptions(options);
			c.setKernel(k);
			return c;
		}
		}
	}
}

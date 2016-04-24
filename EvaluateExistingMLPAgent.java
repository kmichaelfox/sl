package com.kmichaelfox.agents.sl;

import java.io.File;
import java.io.IOException;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;

import com.kmichaelfox.agents.sl.automation.LearningAgent;
import com.kmichaelfox.agents.sl.automation.MLP;
import com.kmichaelfox.agents.sl.automation.OldMLPAgent;

public class EvaluateExistingMLPAgent {
	public static void main(String[] args) {
		MLP mlp = new MLP(13,15,Environment.numberOfKeys);
		try {
			mlp.setWeightsArray(FileArrayProvider.readLines("/Users/kellyfox/Documents/Class Documents/2016_Spring/GameAI/SL Assignment Data/MLP_weights_1461459170003.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Agent agent = new OldMLPAgent(mlp);
		MarioAIOptions marioAIOptions = new MarioAIOptions();
		marioAIOptions.setVisualization(true);
		System.out.println("LearningTrack best agent = " + agent);
		marioAIOptions.setAgent(agent);
		BasicTask basicTask = new BasicTask(marioAIOptions);
		basicTask.setOptionsAndReset(marioAIOptions);
		System.out.println("basicTask = " + basicTask);
		System.out.println("agent = " + agent);

		boolean verbose = true;

		EvaluationInfo evaluationInfo = new EvaluationInfo();

		String filename = "agent_evaluation"+System.currentTimeMillis()+".txt";
		String path = "~/Documents/Class Documents/2016_Spring/GameAI/SL Assignment Data";

		if (path.startsWith("~" + File.separator)) {
			path = System.getProperty("user.home") + path.substring(1);
		}

		if (!(new File(path)).exists()) {
			System.out.println("Supplied path is not valid. Defaulting to Desktop.");
			path = System.getProperty("user.home");
		}

		System.out.println("Logging evaluation history to: ["+path+"/"+filename+"]");
		DataWriter out = new DataWriter(path+"/"+filename);

		// test all-time best
		for (int i = 0; i < 1; i++) {
			// create file writer and begin logging samples
			((OldMLPAgent)agent).startEnvironmentReporting();
			if (!basicTask.runSingleEpisode(1))  // make evaluation on the same episode once
			{
				System.out.println("MarioAI: out of computational time per action! Agent disqualified!");
			}
			out.println("all-time best:");
			out.print(((OldMLPAgent)agent).getFilename());
			out.print("        "+((((OldMLPAgent)agent).getMarioStatus()==Environment.MARIO_STATUS_WIN) ? " win" : "loss"));
			out.println("        "+basicTask.getEvaluationInfo().computeWeightedFitness());
			
			
			// close file and destroy file writer
			((OldMLPAgent)agent).stopEnvironmentReporting();

			evaluationInfo = basicTask.getEvaluationInfo();
			System.out.println(evaluationInfo.toString());
		}
		out.closeFile();


//		f = evaluationInfo.computeWeightedFitness();
//		if (verbose)
//		{
//			System.out.println("Intermediate SCORE = " + f + ";\n Details: " + evaluationInfo.toString());
//		}
	}
}

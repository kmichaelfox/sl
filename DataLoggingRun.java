package com.kmichaelfox.agents.sl;

import ch.idsia.benchmark.mario.environments.*;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.MarioCustomSystemOfValues;
import ch.idsia.tools.MarioAIOptions;
import ch.idsia.agents.*;
import ch.idsia.agents.controllers.*;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey at idsia dot ch Date: Mar 17, 2010 Time: 8:28:00 AM
 * Package: ch.idsia.scenarios
 */
public final class DataLoggingRun
{
	public static void main(String[] args)
	{
		final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
		final Agent agent = new DataLoggingAgent();
		marioAIOptions.setAgent(agent);
		final BasicTask basicTask = new BasicTask(marioAIOptions);
		marioAIOptions.setVisualization(true);
//        basicTask.reset(marioAIOptions);
		final MarioCustomSystemOfValues m = new MarioCustomSystemOfValues();
//        basicTask.runSingleEpisode();
		// run 1 episode with same options, each time giving  output of Evaluation info.
		// verbose = false
		basicTask.doEpisodes(1, false, 1);
		((DataLoggingAgent)agent).closeHistoryBuffer();
		System.out.println("\nEvaluationInfo: \n" + basicTask.getEnvironment().getEvaluationInfoAsString());
		System.out.println("\nCustom : \n" + basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(m));
		System.exit(0);
	}

}
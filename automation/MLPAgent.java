package com.kmichaelfox.agents.sl.automation;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;
import com.kmichaelfox.agents.es.MLP;

public class MLPAgent extends BasicMarioAIAgent implements Agent, Evolvable {
	private MLP mlp;
	final int numberOfOutputs = Environment.numberOfKeys;
	//final int numberOfInputs = 56;
	final int numberOfInputs = 24;
	static private final String name = "MLPAgent";
	
	public MLPAgent() {
		super(name);
		mlp = new MLP(numberOfInputs, 10, numberOfOutputs);
	}

	private MLPAgent(MLP mlp) {
	    super(name);
	    this.mlp = mlp;
	}
		
	public Evolvable getNewInstance() {
	    return new MLPAgent(mlp.getNewInstance());
	}
	
	public Evolvable copy() {
	    return new MLPAgent(mlp.copy());
	}
	
	public void reset() {
	    mlp.reset();
	}
	
	public void mutate() {
	    mlp.mutate();
	}
	
	public MLPAgent recombine(MLPAgent mate) {
		return new MLPAgent(mlp.recombine(mate.mlp));
	}
	
	public void psoRecombine(MLPAgent last, MLPAgent pBest, MLPAgent gBest) {
		mlp.psoRecombine(last.mlp, pBest.mlp, gBest.mlp);
	}
	
	public boolean[] getAction() {
	//        byte[][] scene = observation.getLevelSceneObservation(/*1*/);
	//        byte[][] enemies = observation.getEnemiesObservation(/*0*/);
	    byte[][] scene = levelScene;
	    
	    /* INPUTS FOR ORIGINAL EXAMPLE AGENT (plus marioMode) */
	    double[] inputs = new double[]{
	    		probe(-1, -1, scene), probe(0, -1, scene), probe(1, -1, scene),
	            probe(-1, 0, scene), probe(0, 0, scene), probe(1, 0, scene),
	            probe(-1, 1, scene), probe(0, 1, scene), probe(1, 1, scene),
	            probe(-1, -1, enemies), probe(0, -1, enemies), probe(1, -1, enemies),
	            probe(-1, 0, enemies), probe(0, 0, enemies), probe(1, 0, enemies),
	            probe(-1, 1, enemies), probe(0, 1, enemies), probe(1, 1, enemies),
	            isMarioOnGround ? 1 : 0, 
	            isMarioAbleToJump ? 1 : 0,
	            (marioMode == 2) ? 1 : 0,
	            (marioMode == 1) ? 1 : 0,
	            (marioMode == 0) ? 1 : 0,
	            1};
	    
	    /* INPUTS FOR EXPANDED GRID EXAMPLE AGENT (plus marioMode) */
//	    double[] inputs = new double[]{
//	    		probe(-2, -2, scene), probe(-1, -2, scene), probe(0, -2, scene), probe(1, -2, scene), probe(2, -2, scene),
//	    		probe(-2, -1, scene), probe(-1, -1, scene), probe(0, -1, scene), probe(1, -1, scene), probe(2, -1, scene),
//	            probe(-2, 0, scene),  probe(-1, 0, scene),  probe(0, 0, scene),  probe(1, 0, scene),  probe(2, 0, scene),
//	            probe(-2, 1, scene),  probe(-1, 1, scene),  probe(0, 1, scene),  probe(1, 1, scene),  probe(2, 1, scene),
//	            probe(-2, 2, scene),  probe(-2, 2, scene),  probe(0, 2, scene),  probe(1, 2, scene),  probe(2, 2, scene),
//	            probe(-2, -2, enemies), probe(-1, -2, enemies), probe(0, -2, enemies), probe(1, -2, enemies), probe(2, -2, enemies),
//	    		probe(-2, -1, enemies), probe(-1, -1, enemies), probe(0, -1, enemies), probe(1, -1, enemies), probe(2, -1, enemies),
//	            probe(-2, 0, enemies),  probe(-1, 0, enemies),  probe(0, 0, enemies),  probe(1, 0, enemies),  probe(2, 0, enemies),
//	            probe(-2, 1, enemies),  probe(-1, 1, enemies),  probe(0, 1, enemies),  probe(1, 1, enemies),  probe(2, 1, enemies),
//	            probe(-2, 2, enemies),  probe(-2, 2, enemies),  probe(0, 2, enemies),  probe(1, 2, enemies),  probe(2, 2, enemies),
//	            isMarioOnGround ? 1 : 0, 
//	            isMarioAbleToJump ? 1 : 0,
//	            (marioMode == 2) ? 1 : 0,
//	            (marioMode == 1) ? 1 : 0,
//	            (marioMode == 0) ? 1 : 0,
//	            1};
	    
	    /* INPUTS FOR REVISED RULE AGENT */
//	    double[] inputs = new double[]{
//	    		isEnemyAhead(1) ? 1 : 0,
//	    		isEnemyAhead(2) ? 1 : 0,
//	    		isEnemyAhead(-1) ? 1 : 0,
//	    		isEnemyAhead(-2) ? 1 : 0,
//	    		isObstacleAhead() ? 1 : 0,
//	    		isObstacleAbove() ? 1 : 0,
//	    		isGapAhead() ? 1 : 0,
//	    		isMarioOnGround ? 1 : 0,
//	    		isMarioAbleToJump ? 1 : 0,
//	    		(marioMode == 2) ? 1 : 0,
//	    		(marioMode == 1) ? 1 : 0,
//	    		(marioMode == 0) ? 1 : 0,
//	    		1
//	    };
	    
	    double[] outputs = mlp.propagate(inputs);
	    boolean[] action = new boolean[numberOfOutputs];
	    for (int i = 0; i < action.length; i++)
	    {
	        action[i] = outputs[i] > 0;
	    }
	    return action;
	}
	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
		// NAME IS FINAL
	}
	
	private double probe(int x, int y, byte[][] scene) {
	    int realX = x + 11;
	    int realY = y + 11;
	    return (scene[realX][realY] != 0) ? 1 : 0;
	}
	
	private boolean isEnemyAhead(int stepsAhead) {
		//if (getEnemiesCellValue(marioEgoRow, marioEgoCol + stepsAhead) != 0) {
			//System.out.println(getEnemiesCellValue(marioEgoRow, marioEgoCol + 1));
		//}
		return getEnemiesCellValue(marioEgoRow, marioEgoCol + stepsAhead) != 0 ||
				getEnemiesCellValue(marioEgoRow - 1, marioEgoCol + stepsAhead) != 0;
//		if (getEnemiesCellValue(marioEgoRow, marioEgoCol + 1) != 2) {
//			System.out.println(getEnemiesCellValue(marioEgoRow, marioEgoCol + 1));
//		}
//		return (getEnemiesCellValue(marioEgoRow, marioEgoCol + stepsAhead) != 0 &&
//				getEnemiesCellValue(marioEgoRow, marioEgoCol + stepsAhead) != 20) ||
//				(getEnemiesCellValue(marioEgoRow - 1, marioEgoCol + stepsAhead) != 0 &&
//				getEnemiesCellValue(marioEgoRow - 1, marioEgoCol + stepsAhead) != 20) ||
//				(getEnemiesCellValue(marioEgoRow + 1, marioEgoCol + stepsAhead) != 0 &&
//				getEnemiesCellValue(marioEgoRow + 1, marioEgoCol + stepsAhead) != 20);
	}
	
	private boolean isObstacleAbove() {
		return getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol) != 0 &&
				getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol) != -24 ||
				getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol) != 0 &&
				getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol) != -24;
	}
	
	private boolean isObstacleAhead() {
//		return ((getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol + 1) == 0 &&
//	            getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol + 1) == 0) ||
//	            (getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 2) != 0) &&
//	            getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 2) != -24 &&
//	            getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != 0 &&
//	            getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != -24);
//		return getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != 0;
		return (getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != 0 ||
				getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol + 1) != 0);// &&
				//getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol + 1) != -24) &&
				//(getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + 1) != 0 &&
				//getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + 1) != -24);
	}
	
	private boolean isGapAhead() {
		return getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + 1) == 0;
	}
}

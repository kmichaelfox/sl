package com.kmichaelfox.agents.sl.automation;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;

import com.kmichaelfox.agents.sl.EnvironmentHistory;
import com.kmichaelfox.agents.sl.automation.MLP;

public class OldMLPAgent extends BasicMarioAIAgent implements Agent, Evolvable {
	private MLP mlp;
	final int numberOfOutputs = Environment.numberOfKeys;
	//final int numberOfInputs = 56;
	//final int numberOfInputs = 24;
	final int numberOfInputs = 13;
	final int numberOfHiddenNodes = 15;
	static private final String name = "MLPAgent";
	
	private boolean[] action;
	private EnvironmentHistory hist = null;
	private String currentState;
	
	private double currentProgress = 0;
	private int stuckCounter = 0;
	
	public OldMLPAgent() {
		super(name);
		mlp = new MLP(numberOfInputs, numberOfHiddenNodes, numberOfOutputs);
		action = new boolean[Environment.numberOfKeys];
	}

	public OldMLPAgent(MLP mlp) {
	    super(name);
	    this.mlp = mlp;
	    action = new boolean[Environment.numberOfKeys];
	}
		
	public Evolvable getNewInstance() {
	    return new OldMLPAgent(mlp.getNewInstance());
	}
	
	public Evolvable copy() {
	    return new OldMLPAgent(mlp.copy());
	}
	
	public void reset() {
	    mlp.reset();
	    action = new boolean[Environment.numberOfKeys];
	    currentProgress = 0;
	    stuckCounter = 0;
	}
	
	public void mutate() {
	    mlp.mutate();
	}
	
	public OldMLPAgent recombine(OldMLPAgent mate) {
		return new OldMLPAgent(mlp.recombine(mate.mlp));
	}
	
	public void psoRecombine(OldMLPAgent last, OldMLPAgent pBest, OldMLPAgent gBest) {
		mlp.psoRecombine(last.mlp, pBest.mlp, gBest.mlp);
	}
	
	@Override
	public void integrateObservation(Environment environment) {
		
	    levelScene = environment.getLevelSceneObservationZ(0);
	    enemies = environment.getEnemiesObservationZ(0);
	    mergedObservation = environment.getMergedObservationZZ(1, 0);

	    this.marioFloatPos = environment.getMarioFloatPos();
	    this.enemiesFloatPos = environment.getEnemiesFloatPos();
	    this.marioState = environment.getMarioState();

	    receptiveFieldWidth = environment.getReceptiveFieldWidth();
	    receptiveFieldHeight = environment.getReceptiveFieldHeight();

	    // It also possible to use direct methods from Environment interface.
	    //
	    marioStatus = marioState[0];
	    marioMode = marioState[1];
	    isMarioOnGround = marioState[2] == 1;
	    isMarioAbleToJump = marioState[3] == 1;
	    isMarioAbleToShoot = marioState[4] == 1;
	    isMarioCarrying = marioState[5] == 1;
	    getKillsTotal = marioState[6];
	    getKillsByFire = marioState[7];
	    getKillsByStomp = marioState[8];
	    getKillsByShell = marioState[9];
	}
	
	public boolean[] getAction() {
	//        byte[][] scene = observation.getLevelSceneObservation(/*1*/);
	//        byte[][] enemies = observation.getEnemiesObservation(/*0*/);
	    byte[][] scene = levelScene;
	    tickStuckCounter();
	    
	    /* INPUTS FOR ORIGINAL EXAMPLE AGENT (plus marioMode) */
//	    double[] inputs = new double[]{
//	    		probe(-1, -1, scene), probe(0, -1, scene), probe(1, -1, scene),
//	            probe(-1, 0, scene), probe(0, 0, scene), probe(1, 0, scene),
//	            probe(-1, 1, scene), probe(0, 1, scene), probe(1, 1, scene),
//	            probe(-1, -1, enemies), probe(0, -1, enemies), probe(1, -1, enemies),
//	            probe(-1, 0, enemies), probe(0, 0, enemies), probe(1, 0, enemies),
//	            probe(-1, 1, enemies), probe(0, 1, enemies), probe(1, 1, enemies),
//	            isMarioOnGround ? 1 : 0, 
//	            isMarioAbleToJump ? 1 : 0,
//	            (marioMode == 2) ? 1 : 0,
//	            (marioMode == 1) ? 1 : 0,
//	            (marioMode == 0) ? 1 : 0,
//	            1};
	    
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
	    double[] inputs = new double[]{
	    		isEnemyAhead(1) ? 1 : 0,
	    		isEnemyAhead(2) ? 1 : 0,
	    		isEnemyAhead(-1) ? 1 : 0,
	    		isEnemyAhead(-2) ? 1 : 0,
	    		isObstacleAhead(1) ? 1 : 0,
	    		isObstacleAbove() ? 1 : 0,
	    		isGapAhead(1) ? 1 : 0,
	    		isMarioOnGround ? 1 : 0,
	    		isMarioAbleToJump ? 1 : 0,
	    		(marioMode == 2) ? 1 : 0,
	    		(marioMode == 1) ? 1 : 0,
	    		(marioMode == 0) ? 1 : 0,
	    		1
	    };
	    
	    double[] outputs = mlp.propagate(inputs);
	    //boolean[] action = new boolean[numberOfOutputs];
	    for (int i = 0; i < action.length; i++)
	    {
	        action[i] = outputs[i] > 0;
	    }

	    // log current state
	    if (hist != null) {
	    	findCurrentState();
			hist.logHistory(currentState);
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
	
	public double[] getWeightsArray() {
		return mlp.getWeightsArray();
	}
	
	public void startEnvironmentReporting() {
		currentState = "";
		hist = new EnvironmentHistory();
		printARFFHeaderToHistory();
	}
	
	public void stopEnvironmentReporting() {
		hist.writeHistoryToFile();
		hist = null;
	}
	
	public int getEnemiesCellValue(int x, int y)
	{
		if (x < 0 || x >= levelScene.length || y < 0 || y >= levelScene[0].length)
			return 0;

		return enemies[x][y];
	}

	public int getReceptiveFieldCellValue(int x, int y)
	{
		if (x < 0 || x >= levelScene.length || y < 0 || y >= levelScene[0].length)
			return 0;

		return levelScene[x][y];
	}

	private void printARFFHeaderToHistory() {
		if (hist == null) {
			throw new RuntimeException("EnvironmentHistory failed to initialize");
		}

		hist.logHistory("@RELATION action");
		hist.logHistory("");
		hist.logHistory("@ATTRIBUTE mario_mode NUMERIC");
		hist.logHistory("@ATTRIBUTE enemies_right_close NUMERIC");
		hist.logHistory("@ATTRIBUTE enemies_right_far NUMERIC");
		hist.logHistory("@ATTRIBUTE enemies_left_close NUMERIC");
		hist.logHistory("@ATTRIBUTE enemies_left_far NUMERIC");
		hist.logHistory("@ATTRIBUTE obstacle_right NUMERIC");
		hist.logHistory("@ATTRIBUTE obstacle_left NUMERIC");
		hist.logHistory("@ATTRIBUTE obstacle_above NUMERIC");
		hist.logHistory("@ATTRIBUTE gap_ahead NUMERIC");
		hist.logHistory("@ATTRIBUTE mario_on_ground NUMERIC");
		hist.logHistory("@ATTRIBUTE mario_can_jump NUMERIC");
		hist.logHistory("@ATTRIBUTE class {NONE,L,R,D,L_JUMP,R_JUMP,JUMP,L_FIRE,R_FIRE,FIRE,L_JUMP_FIRE,R_JUMP_FIRE,JUMP_FIRE}");
		hist.logHistory("");
		hist.logHistory("@DATA");
	}

	private void findCurrentState() {
		currentState = "";
		//hist.logHistory("@ATTRIBUTE mario_mode NUMERIC");
		currentState += (marioMode+",");
		//hist.logHistory("@ATTRIBUTE enemies_right_close NUMERIC");
		currentState += ((isEnemyRightClose() ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE enemies_right_far NUMERIC");
		currentState += ((isEnemyRightFar() ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE enemies_left_close NUMERIC");
		currentState += ((isEnemyLeftClose() ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE enemies_left_far NUMERIC");
		currentState += ((isEnemyLeftFar() ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE obstacle_right NUMERIC");
		currentState += ((isObstacleAhead(1) ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE obstacle_left NUMERIC");
		currentState += ((isObstacleAhead(-1) ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE obstacle_above NUMERIC");
		currentState += ((isObstacleAbove() ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE gap_ahead NUMERIC");
		currentState += ((isGapAhead(1) ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE mario_on_ground NUMERIC");
		currentState += ((isMarioOnGround ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE mario_can_jump NUMERIC");
		currentState += ((isMarioAbleToJump ? 1 : 0)+",");
		//hist.logHistory("@ATTRIBUTE class {NONE,L,R,D,L_JUMP,R_JUMP,JUMP,L_FIRE,R_FIRE,FIRE,L_JUMP_FIRE,R_JUMP_FIRE,JUMP_FIRE}");
		currentState += (getActionType());
	}
	
	private String getActionType() {
		String actionType = "";
		if (action[Mario.KEY_RIGHT]) {
			actionType += "R";
		} else if (action[Mario.KEY_LEFT]) {
			actionType += "L";
		} else if (action[Mario.KEY_DOWN]) {
			return "D";
		}
		
		if (action[Mario.KEY_JUMP]) {
			if (!actionType.isEmpty()) {
				actionType += "_";
			}
			
			actionType += "JUMP";
		}
		
		if (action[Mario.KEY_SPEED]) {
			if (!actionType.isEmpty()) {
				actionType += "_";
			}
			
			actionType += "FIRE";
		}
		
		if (actionType.isEmpty()) {
			actionType = "NONE";
		}
		return actionType;
	}
	
	private void tickStuckCounter() {
		if (Math.abs(marioFloatPos[0] - currentProgress) > 20) {
			stuckCounter = 0;
			currentProgress = (int)marioFloatPos[0];
		} else {
			stuckCounter++;
		}
	}
	
	public boolean isMarioStuck() {
		return stuckCounter > 35;
	}

	public boolean isEnemyRightClose() {
		return isEnemyAhead(1) || isEnemyAhead(2);
	}

	public boolean isEnemyRightFar() {
		return isEnemyAhead(3) || isEnemyAhead(4);
	}

	public boolean isEnemyLeftClose() {
		return isEnemyAhead(-1) || isEnemyAhead(-2);
	}

	public boolean isEnemyLeftFar() {
		return isEnemyAhead(-3) || isEnemyAhead(-4);
	}

	private boolean isEnemyAhead(int stepsAhead) {
		return getEnemiesCellValue(marioEgoRow, marioEgoCol + stepsAhead) != 0 ||
				getEnemiesCellValue(marioEgoRow - 1, marioEgoCol + stepsAhead) != 0 ||
				getEnemiesCellValue(marioEgoRow - 2, marioEgoCol + stepsAhead) != 0;
	}

	public boolean isObstacleAbove() {
		return getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol) != 0 &&
				getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol) != 2 &&
				getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol) != -62 ||
				getReceptiveFieldCellValue(marioEgoRow - 3, marioEgoCol) != 0 &&
				getReceptiveFieldCellValue(marioEgoRow - 3, marioEgoCol) != 2 &&
				getReceptiveFieldCellValue(marioEgoRow - 3, marioEgoCol) != -62;
	}

	public boolean isObstacleAhead(int stepsAhead) {
		return (getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + stepsAhead) != 0 ||
				getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol + stepsAhead) != 0) &&
				getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol + stepsAhead) != 0;
	}
	
	public boolean isQuestionBlockAbove() {
		return getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol) == -22 ||
				getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol) == -11;
	}

	public boolean isGapAhead(int directionMult) {
		return (getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + (1 * directionMult)) == 0 || 
				getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + (2 * directionMult)) == 0) && 
				isMarioOnGround;
		//return (getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + (1 * directionMult)) == 0) &&
		//(getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + (2 * directionMult)) == 0);
	}
	
	public boolean isPowerUpAhead(int distance) {
		boolean present = false;
		int sign = distance/Math.abs(distance);
		int abs_dist = Math.abs(distance)+1;
		for (int height = -5; height < 5; height++) {
			for (int width = 1; width < abs_dist; width++) {
				present = present || 
						(getEnemiesCellValue(marioEgoRow+height, marioEgoCol+(width*sign)) == 2 || // Mushroom 
						getEnemiesCellValue(marioEgoRow+height, marioEgoCol+(width*sign)) == 3);   // Fire Flower
			}
		}
		return present;
	}
	
	public boolean isPowerUpAbove(int distance) {
		boolean present = false;
		for (int height = 0; height < distance; height++) {
			for (int width = -(receptiveFieldWidth/2); width < (int)(receptiveFieldWidth/2); width++) {
				present = present || 
						(getEnemiesCellValue(marioEgoRow-height-2, marioEgoCol+width) == 2 || // Mushroom 
						getEnemiesCellValue(marioEgoRow-height-2, marioEgoCol+width) == 3);   // Fire Flower
			}
		}
		return present;
	}
	
	public int getMarioStatus() {
		return marioStatus;
	}
	
	public String getFilename() {
		if (hist == null) {
			return null;
		}
		
		return hist.getFilename();
	}
}

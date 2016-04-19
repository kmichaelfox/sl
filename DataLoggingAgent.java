/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.kmichaelfox.agents.sl;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import com.kmichaelfox.agents.rl.RLAgent;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 29, 2009
 * Time: 12:19:49 AM
 * Package: ch.idsia.controllers.agents.controllers;
 */
public class DataLoggingAgent extends KeyAdapter implements Agent {
	List<boolean[]> history = new ArrayList<boolean[]>();
	private boolean[] action = null;
	private String Name = "DataLoggingAgent";

	/*final*/
	protected byte[][] levelScene;
	/*final */
	protected byte[][] enemies;
	protected byte[][] mergedObservation;

	protected float[] marioFloatPos = null;
	protected float[] enemiesFloatPos = null;

	protected int[] marioState = null;

	protected int marioStatus;
	protected int marioMode;
	protected boolean isMarioOnGround;
	protected boolean isMarioAbleToJump;
	protected boolean isMarioAbleToShoot;
	protected boolean isMarioCarrying;
	protected int getKillsTotal;
	protected int getKillsByFire;
	protected int getKillsByStomp;
	protected int getKillsByShell;

	protected int receptiveFieldWidth;
	protected int receptiveFieldHeight;
	protected int marioEgoRow;
	protected int marioEgoCol;
	// values of these variables could be changed during the Agent-Environment interaction.
	// Use them to get more detailed or less detailed description of the level.
	// for information see documentation for the benchmark <link: marioai.org/marioaibenchmark/zLevels
	int zLevelScene = 1;
	int zLevelEnemies = 0;

	EnvironmentHistory hist = null;
	String currentState;
	double currentProgress = 0;
	int stuckCounter = 0;


	public DataLoggingAgent() {
		this.reset();
		//hist = new EnvironmentHistory();
		currentState = "";
		//printARFFHeaderToHistory();
		//        RegisterableAgent.registerAgent(this);
	}

	public boolean[] getAction() {
		tickStuckCounter();
		findCurrentState();
		if (hist != null) {
			hist.logHistory(currentState);
		}
		return action;
	}
	
	private void tickStuckCounter() {
		if (Math.abs(marioFloatPos[0] - currentProgress) > 20) {
			stuckCounter = 0;
			currentProgress = marioFloatPos[0];
		} else {
			stuckCounter++;
		}
	}
	
	public boolean isMarioStuck() {
		return stuckCounter > 50;
	}

	public void integrateObservation(Environment environment)
	{
		levelScene = environment.getLevelSceneObservationZ(zLevelScene);
		enemies = environment.getEnemiesObservationZ(zLevelEnemies);
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

	public void giveIntermediateReward(float intermediateReward) {

	}

	public void reset() {
		// Just check you keyboard. Especially arrow buttons and 'A' and 'S'!
		action = new boolean[Environment.numberOfKeys];
	}



	public void setObservationDetails(final int rfWidth, final int rfHeight, final int egoRow, final int egoCol) {
		receptiveFieldWidth = rfWidth;
		receptiveFieldHeight = rfHeight;

		marioEgoRow = egoRow;
		marioEgoCol = egoCol;
	}

	public boolean[] getAction(Environment observation) {
		float[] enemiesPos = observation.getEnemiesFloatPos();
		return action;
	}

	public String getName() { return Name; }

	public void setName(String name) { Name = name; }


	public void keyPressed(KeyEvent e) {
		toggleKey(e.getKeyCode(), true);
	}

	public void keyReleased(KeyEvent e) {
		toggleKey(e.getKeyCode(), false);
	}


	private void toggleKey(int keyCode, boolean isPressed) {
		switch (keyCode)
		{
		case KeyEvent.VK_LEFT:
			action[Mario.KEY_LEFT] = isPressed;
			break;
		case KeyEvent.VK_RIGHT:
			action[Mario.KEY_RIGHT] = isPressed;
			break;
		case KeyEvent.VK_DOWN:
			action[Mario.KEY_DOWN] = isPressed;
			break;
		case KeyEvent.VK_UP:
			action[Mario.KEY_UP] = isPressed;
			break;

		case KeyEvent.VK_S:
			action[Mario.KEY_JUMP] = isPressed;
			break;
		case KeyEvent.VK_A:
			action[Mario.KEY_SPEED] = isPressed;
			break;
		}
	}

	public List<boolean[]> getHistory() {
		return history;
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
		//hist.logHistory("@ATTRIBUTE gap_above NUMERIC");
		currentState += ((isGapAhead() ? 1 : 0)+",");
		currentState += ((isPowerUpAhead(-3) ? 1 : 0)+","); // added
		currentState += ((isPowerUpAhead(3) ? 1 : 0)+",");  // added
		currentState += ((isPowerUpAbove(3) ? 1 : 0)+",");  // added
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
				getEnemiesCellValue(marioEgoRow - 1, marioEgoCol + stepsAhead) != 0;
	}

	public boolean isObstacleAbove() {
		
		return getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol) != 0 &&
				getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol) != 2 ||
				getReceptiveFieldCellValue(marioEgoRow - 3, marioEgoCol) != 0 &&
				getReceptiveFieldCellValue(marioEgoRow - 3, marioEgoCol) != 2;
	}

	public boolean isObstacleAhead(int stepsAhead) {
		return (getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + stepsAhead) != 0 ||
				getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol + stepsAhead) != 0);
	}

	public boolean isGapAhead() {
		if (getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + 1) == 0 && isMarioOnGround) {
			System.out.println("gap ahead");
		}
		return getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + 1) == 0 && isMarioOnGround;
	}
	
	public void closeHistoryBuffer() {
		if (hist != null) {
			hist.writeHistoryToFile();
		}
	}
}

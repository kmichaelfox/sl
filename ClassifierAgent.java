package com.kmichaelfox.agents.sl;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class ClassifierAgent extends BasicMarioAIAgent implements Agent {
	static private final String name = "ClassifierAgent::";
	Classifier classifier;
	//DataSource trainingData;
	Instances dataset;
	//FastVector attributes;
	
	double currentProgress = 0;
	int stuckCounter = 0;
	
	public ClassifierAgent (Classifier c, DataSource inputData) {
		super(name+getClassifierName(c));
		classifier = c;
		//trainingData = inputData;
		//attributes = setAttributes();
		try {
			dataset = inputData.getDataSet();
			dataset.setClassIndex(dataset.numAttributes() - 1);
			//System.out.println(dataset.lastInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(dataset);
	}
	
	private static String getClassifierName(Classifier c) {
		String _class = c.getClass().getName();
		int index = _class.lastIndexOf((int)'.');
		
		return _class.substring(index+1);
	}
	
	private static FastVector setAttributes() {
		FastVector vec = new FastVector(19);
		vec.addElement(new Attribute("mario_mode")); //@ATTRIBUTE mario_mode NUMERIC
		vec.addElement(new Attribute("enemies_right_close")); //@ATTRIBUTE enemies_right_close NUMERIC
		vec.addElement(new Attribute("enemies_right_far")); //@ATTRIBUTE enemies_right_far NUMERIC
		vec.addElement(new Attribute("enemies_left_close")); //@ATTRIBUTE enemies_left_close NUMERIC
		vec.addElement(new Attribute("enemies_left_far")); //@ATTRIBUTE enemies_left_far NUMERIC
		vec.addElement(new Attribute("obstacle_right")); //@ATTRIBUTE obstacle_right NUMERIC
		vec.addElement(new Attribute("obstacle_left")); //@ATTRIBUTE obstacle_left NUMERIC
		vec.addElement(new Attribute("obstacle_above")); //@ATTRIBUTE obstacle_above NUMERIC
		vec.addElement(new Attribute("gap_ahead")); //@ATTRIBUTE gap_ahead NUMERIC
		vec.addElement(new Attribute("gap_behind")); //@ATTRIBUTE gap_behind NUMERIC
		vec.addElement(new Attribute("mario_stuck")); //@ATTRIBUTE mario_stuck NUMERIC
		vec.addElement(new Attribute("powerup_ahead")); //@ATTRIBUTE powerup_ahead NUMERIC
		vec.addElement(new Attribute("powerup_behind")); //@ATTRIBUTE powerup_behind NUMERIC
		vec.addElement(new Attribute("powerup_above")); //@ATTRIBUTE powerup_above NUMERIC
		vec.addElement(new Attribute("mario_map_height")); //@ATTRIBUTE mario_map_height NUMERIC
		vec.addElement(new Attribute("question_block_above")); //@ATTRIBUTE question_block_above NUMERIC
		vec.addElement(new Attribute("mario_on_ground")); //@ATTRIBUTE mario_on_ground NUMERIC
		vec.addElement(new Attribute("mario_can_jump")); //@ATTRIBUTE mario_can_jump NUMERIC
		FastVector classAttribute = new FastVector(13); //@ATTRIBUTE class {NONE,L,R,D,L_JUMP,R_JUMP,JUMP,L_FIRE,R_FIRE,FIRE,L_JUMP_FIRE,R_JUMP_FIRE,JUMP_FIRE}
		vec.addElement(new Attribute("class", classAttribute));
		
		return vec;
	}
	
	public boolean[] getAction() {
		tickStuckCounter();
		boolean[] action = new boolean[Environment.numberOfKeys];
		
		Instance obs = new Instance(dataset.numAttributes());
		obs.setDataset(dataset);
		obs.setValue((Attribute)obs.attribute(0), marioMode);
		obs.setValue((Attribute)obs.attribute(1), isEnemyRightClose()?1:0); // enemies_right_close
		obs.setValue((Attribute)obs.attribute(2), isEnemyRightFar()?1:0); // enemies_right_far
		obs.setValue((Attribute)obs.attribute(3), isEnemyLeftClose()?1:0); // enemies_left_close
		obs.setValue((Attribute)obs.attribute(4), isEnemyLeftFar()?1:0); // enemies_left_far
		obs.setValue((Attribute)obs.attribute(5), isObstacleAhead(1)?1:0); // obstacle_right
		obs.setValue((Attribute)obs.attribute(6), isObstacleAhead(-1)?1:0); // obstacle_left
		obs.setValue((Attribute)obs.attribute(7), isObstacleAbove()?1:0); // obstacle_above
		obs.setValue((Attribute)obs.attribute(8), isGapAhead(1)?1:0); // gap_ahead
		obs.setValue((Attribute)obs.attribute(9), isGapAhead(-1)?1:0); // gap_behind
		obs.setValue((Attribute)obs.attribute(10), isMarioStuck()?1:0); // mario_stuck
		obs.setValue((Attribute)obs.attribute(11), isPowerUpAhead(3)?1:0); // powerup_ahead
		obs.setValue((Attribute)obs.attribute(12), isPowerUpAhead(-3)?1:0); // powerup_behind
		obs.setValue((Attribute)obs.attribute(13), isPowerUpAbove(3)?1:0); // powerup_above
		obs.setValue((Attribute)obs.attribute(14), marioFloatPos[1] / 250); // mario_map_height 
		obs.setValue((Attribute)obs.attribute(15), isQuestionBlockAbove() ? 1 : 0); // question_block_above
		obs.setValue((Attribute)obs.attribute(16), isMarioOnGround?1:0); // mario_on_ground
		obs.setValue((Attribute)obs.attribute(17), isMarioAbleToJump?1:0); // mario_can_jump
		
		try {
			setAction(classifier.classifyInstance(obs), action);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		
		return action;
	}
	
	private void setAction(double classification, boolean[] action) {
		
		switch ((int)classification) {
		case 0: // NONE
			break;
		case 1: // L
			action[Mario.KEY_LEFT] = true;
			break;
		case 2: // R
			action[Mario.KEY_RIGHT] = true;
			break;
		case 3: // D
			action[Mario.KEY_DOWN] = true;
			break;
		case 4: // L_JUMP
			action[Mario.KEY_LEFT] = true;
			action[Mario.KEY_JUMP] = true;
			break;
		case 5: // R_JUMP
			action[Mario.KEY_RIGHT] = true;
			action[Mario.KEY_JUMP] = true;
			break;
		case 6: // JUMP
			action[Mario.KEY_JUMP] = true;
			break;
		case 7: // L_FIRE
			action[Mario.KEY_LEFT] = true;
			action[Mario.KEY_SPEED] = true;
			break;
		case 8: // R_FIRE
			action[Mario.KEY_RIGHT] = true;
			action[Mario.KEY_SPEED] = true;
			break;
		case 9: // FIRE
			action[Mario.KEY_SPEED] = true;
			break;
		case 10: // L_JUMP_FIRE
			action[Mario.KEY_LEFT] = true;
			action[Mario.KEY_JUMP] = true;
			action[Mario.KEY_SPEED] = true;
			break;
		case 11: // R_JUMP_FIRE
			action[Mario.KEY_RIGHT] = true;
			action[Mario.KEY_JUMP] = true;
			action[Mario.KEY_SPEED] = true;
			break;
		case 12: // JUMP_FIRE
			action[Mario.KEY_JUMP] = true;
			action[Mario.KEY_SPEED] = true;
			break;
		}
		
		//return action;
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
				getReceptiveFieldCellValue(marioEgoRow - 2, marioEgoCol) != 2 ||
				getReceptiveFieldCellValue(marioEgoRow - 3, marioEgoCol) != 0 &&
				getReceptiveFieldCellValue(marioEgoRow - 3, marioEgoCol) != 2;
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
}

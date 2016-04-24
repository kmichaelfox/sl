# MarioAI Supervised Learning package

com.kmichaelfox.agents.sl.automation contains most of the code (some of it updated) from the origin GA agent.

OldMLPAgent.java contains an agent that is functionally identical to the original evolutionary strategy agent, while MLPAgent is the update version that contains 7 new observations.

These can be trained by executing com.kmichaelfox.agents.sl.automation.LearningTrack.java, and the results will be logged to various data files.

One of the files resulting from this run is a formatted ARFF file that can be provided to com.kmichaelfox.agents.sl.ModelBuilder.java, which will automatically construct a model for each of the classifiers and command line options required for this assignment.

com.kmichaelfox.agents.sl.ClassifierRun.java can then be executed to run a ClassifierAgent through a map one time and log the level's EvaluationInfo to a file.
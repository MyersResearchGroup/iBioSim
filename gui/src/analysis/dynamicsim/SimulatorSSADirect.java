package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.Gui;
import main.util.MutableBoolean;
import odk.lang.FastMath;

public class SimulatorSSADirect extends Simulator {
	
	private static Long initializationTime = new Long(0);
	
	MutableBoolean eventsFlag = new MutableBoolean(false);
	MutableBoolean rulesFlag = new MutableBoolean(false);
	MutableBoolean constraintsFlag = new MutableBoolean(false);

	public SimulatorSSADirect(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType) 
	throws IOException, XMLStreamException {
		
		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, randomSeed,
				progress, printInterval, initializationTime, stoichAmpValue, running, interestingSpecies, quantityType);
	
		try {
			initialize(randomSeed, 1);
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (XMLStreamException e2) {
			e2.printStackTrace();
		}
	}

	public void simulate() {
		
		if (sbmlHasErrorsFlag == true)
			return;
		
		long initTime2 = System.nanoTime();
		
		final boolean noEventsFlag = (Boolean) eventsFlag.getValue();
		final boolean noAssignmentRulesFlag = (Boolean) rulesFlag.getValue();
		final boolean noConstraintsFlag = (Boolean) constraintsFlag.getValue();
		
		initializationTime += System.nanoTime() - initTime2;
		long initTime3 = System.nanoTime() - initTime2;
		
		//System.err.println("initialization time: " + initializationTime / 1e9f);
		
		//SIMULATION LOOP
		//simulate until the time limit is reached
		
		long step1Time = 0;
		long step2Time = 0;
		long step3Time = 0;
		long step4Time = 0;
		long step5Time = 0;
		
		currentTime = 0.0;
		double printTime = -0.00001;
		
		//add events to queue if they trigger
		if (noEventsFlag == false)
			handleEvents(noAssignmentRulesFlag, noConstraintsFlag);
		
		while (currentTime < timeLimit && cancelFlag == false) {
			
			//if a constraint fails
			if (constraintFailureFlag == true) {
				
				JOptionPane.showMessageDialog(Gui.frame, "Simulation Canceled Due To Constraint Failure",
						"Constraint Failure", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			//EVENT HANDLING
			//trigger and/or fire events, etc.
			if (noEventsFlag == false) {
				
				HashSet<String> affectedReactionSet = 
					fireEvents(noAssignmentRulesFlag, noConstraintsFlag);				
				
				//recalculate propensties/groups for affected reactions
				if (affectedReactionSet.size() > 0)
					updatePropensities(affectedReactionSet);
			}
			
			//TSD PRINTING
			//print to TSD if the next print interval arrives
			//this obviously prints the previous timestep's data
			if (currentTime >= printTime) {
				
				if (printTime < 0)
					printTime = 0.0;
					
				try {
					printToTSD(printTime);
					bufferedTSDWriter.write(",\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				printTime += printInterval;
			}			
			
			//update progress bar			
			progress.setValue((int)((currentTime / timeLimit) * 100.0));
			
			
			//STEP 1: generate random numbers
			
			//long step1Initial = System.nanoTime();
			
			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();
			
			//step1Time += System.nanoTime() - step1Initial;
			
			
			
			//STEP 2: calculate delta_t, the time till the next reaction execution
			
			//long step2Initial = System.nanoTime();
			 
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			
			//step2Time += System.nanoTime() - step2Initial;
			
			
			
			//STEP 3: select a reaction
			
			//long step3Initial = System.nanoTime();
			
			String selectedReactionID = selectReaction(r2);
			
			//step3Time += System.nanoTime() - step3Initial;
			
		
			
			//STEP 4: perform selected reaction and update species counts
			
			//long step4Initial = System.nanoTime();
			
			performReaction(selectedReactionID, noAssignmentRulesFlag, noConstraintsFlag);
			
			//step4Time += System.nanoTime() - step4Initial;
			
			
			
			//STEP 5: compute affected reactions' new propensities and update total propensity
			
			//long step5Initial = System.nanoTime();
			
			//create a set (precludes duplicates) of reactions that the selected reaction's species affect
			HashSet<String> affectedReactionSet = getAffectedReactionSet(selectedReactionID, noAssignmentRulesFlag);
			
			updatePropensities(affectedReactionSet);
			
			//step5Time += System.nanoTime() - step5Initial;
			
			
			//update time for next iteration
			currentTime += delta_t;
			
			if (variableToIsInAssignmentRuleMap != null &&
					variableToIsInAssignmentRuleMap.containsKey("time"))				
				performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get("time"));
			
			//add events to queue if they trigger
			if (noEventsFlag == false) {
				
				handleEvents(noAssignmentRulesFlag, noConstraintsFlag);
			
				//step to the next event fire time if it comes before the next time step
				if (!triggeredEventQueue.isEmpty() && triggeredEventQueue.peek().fireTime <= currentTime)
					currentTime = triggeredEventQueue.peek().fireTime;
			}
			
			while (currentTime > printTime && printTime < timeLimit) {
				
				try {
					printToTSD(printTime);
					bufferedTSDWriter.write(",\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				printTime += printInterval;
				running.setTitle("Progress (" + (int)((currentTime / timeLimit) * 100.0) + "%)");
			}
			
		} //end simulation loop
		
//		System.err.println("total time: " + String.valueOf((initializationTime + System.nanoTime() - 
//				initTime2 - initTime3) / 1e9f));
//		System.err.println("total step 1 time: " + String.valueOf(step1Time / 1e9f));
//		System.err.println("total step 2 time: " + String.valueOf(step2Time / 1e9f));
//		System.err.println("total step 3 time: " + String.valueOf(step3Time / 1e9f));
//		System.err.println("total step 4 time: " + String.valueOf(step4Time / 1e9f));
//		System.err.println("total step 5 time: " + String.valueOf(step5Time / 1e9f));
		
		if (cancelFlag == false) {
			
			//print the final species counts
			try {
				printToTSD(printTime);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				bufferedTSDWriter.write(')');
				bufferedTSDWriter.flush();
			} 
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * sets up data structures local to the SSA-Direct method
	 * 
	 * @param noEventsFlag
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	private void initialize(long randomSeed, int runNumber) 
	throws IOException, XMLStreamException {	
		
		setupArrays();
		setupSpecies();
		setupParameters();
		setupInitialAssignments();
		setupRules();
		setupConstraints();
		
		eventsFlag = new MutableBoolean(false);
		rulesFlag = new MutableBoolean(false);
		constraintsFlag = new MutableBoolean(false);
		
		if (numEvents == 0)
			eventsFlag.setValue(true);
		else
			eventsFlag.setValue(false);
		
		if (numAssignmentRules == 0)
			rulesFlag.setValue(true);
		else
			rulesFlag.setValue(false);
		
		if (numConstraints == 0)
			constraintsFlag.setValue(true);
		else
			constraintsFlag.setValue(false);
		
		
		//STEP 0: calculate initial propensities (including the total)		
		setupReactions();		
		setupEvents();
		
		setupForOutput(randomSeed, runNumber);
		
		if (dynamicBoolean == true) {
			
			setupGrid();
			createModelCopy();
		}
	}

	protected void eraseComponentFurther(HashSet<String> reactionIDs) {
	}
	
	/**
	 * 
	 */
	protected void updateAfterDynamicChanges() {
		
	}
	
	/**
	 * updates the propensities of the reactions affected by the recently performed reaction
	 * @param affectedReactionSet the set of reactions affected by the recently performed reaction
	 */
	private void updatePropensities(HashSet<String> affectedReactionSet) {
		
		//loop through the affected reactions and update the propensities
		for (String affectedReactionID : affectedReactionSet) {
			
			boolean notEnoughMoleculesFlag = false;
			
			HashSet<StringDoublePair> reactantStoichiometrySet = 
				reactionToReactantStoichiometrySetMap.get(affectedReactionID);
			
			//check for enough molecules for the reaction to occur
			for (StringDoublePair speciesAndStoichiometry : reactantStoichiometrySet) {
				
				String speciesID = speciesAndStoichiometry.string;
				double stoichiometry = speciesAndStoichiometry.doub;
				
				//if there aren't enough molecules to satisfy the stoichiometry
				if (variableToValueMap.get(speciesID) < stoichiometry) {
					notEnoughMoleculesFlag = true;
					break;
				}
			}
			
			double newPropensity = 0.0;
			
			if (notEnoughMoleculesFlag == false) {
				
				newPropensity = evaluateExpressionRecursive(reactionToFormulaMap.get(affectedReactionID));
				//newPropensity = CalculatePropensityIterative(affectedReactionID);
			}
			
			double oldPropensity = reactionToPropensityMap.get(affectedReactionID);
			
			//add the difference of new v. old propensity to the total propensity
			totalPropensity += newPropensity - oldPropensity;
			
			reactionToPropensityMap.put(affectedReactionID, newPropensity);
		}
	}
	
	/**
	 * randomly selects a reaction to perform
	 * 
	 * @param r2 random number
	 * @return the ID of the selected reaction
	 */
	private String selectReaction(double r2) {
		
		double randomPropensity = r2 * (totalPropensity);
		double runningTotalReactionsPropensity = 0.0;
		String selectedReaction = "";
		
		//finds the reaction that the random propensity lies in
		//it keeps adding the next reaction's propensity to a running total
		//until the running total is greater than the random propensity
		for (String currentReaction : reactionToPropensityMap.keySet()) {
			
			runningTotalReactionsPropensity += reactionToPropensityMap.get(currentReaction);
			
			if (randomPropensity < runningTotalReactionsPropensity) {
				selectedReaction = currentReaction;
				break;
			}
		}
		
		return selectedReaction;		
	}

	/**
	 * cancels the current run
	 */
	protected void cancel() {
	
		cancelFlag = true;
	}
	
	/**
	 * clears data structures for new run
	 */
	protected void clear() {
		
		variableToValueMap.clear();
		reactionToPropensityMap.clear();
		
		if (numEvents > 0) {
			
			triggeredEventQueue.clear();
			untriggeredEventSet.clear();
			eventToPriorityMap.clear();
			eventToDelayMap.clear();
		}
		
		reactionToFormulaMap.clear();
		speciesIDSet.clear();
		componentToLocationMap.clear();
		componentToReactionSetMap.clear();
		componentToVariableSetMap.clear();
		componentToEventSetMap.clear();
		compartmentIDSet.clear();
		nonconstantParameterIDSet.clear();
		
		minRow = Integer.MAX_VALUE;
		minCol = Integer.MAX_VALUE;
		maxRow = Integer.MIN_VALUE;
		maxCol = Integer.MIN_VALUE;
		
		//get rid of things that were created dynamically last run
		if (dynamicBoolean == true) {
			resetModel();
		}
	}

	/**
	 * does minimized initalization process to prepare for a new run
	 */
	protected void setupForNewRun(int newRun) {
		
		try {
			setupSpecies();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setupInitialAssignments();
		setupParameters();
		setupRules();
		setupConstraints();
		
		totalPropensity = 0.0;
		minPropensity = Double.MAX_VALUE;
		maxPropensity = Double.MIN_VALUE;
		
		if (numEvents == 0)
			eventsFlag.setValue(true);
		else
			eventsFlag.setValue(false);
		
		if (numAssignmentRules == 0)
			rulesFlag.setValue(true);
		else
			rulesFlag.setValue(false);
		
		if (numConstraints == 0)
			constraintsFlag.setValue(true);
		else
			constraintsFlag.setValue(false);
		
		//STEP 0A: calculate initial propensities (including the total)		
		setupReactions();		
		setupEvents();		
		setupForOutput(0, newRun);
		
		if (dynamicBoolean == true) {
			
			setupGrid();
			
			//print compartment location IDs
			for (String componentLocationID : componentToLocationMap.keySet()) {
				
				String locationX = componentLocationID + "__locationX";
				String locationY = componentLocationID + "__locationY";
				
				try {
					bufferedTSDWriter.write(", \"" + locationX + "\", \"" + locationY + "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			//print compartment IDs (for sizes)
			for (String componentID : compartmentIDSet) {
				
				try {
					bufferedTSDWriter.write(", \"" + componentID + "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				bufferedTSDWriter.write("),\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}

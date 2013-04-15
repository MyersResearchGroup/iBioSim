package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import analysis.dynamicsim.Simulator.StringDoublePair;

import odk.lang.FastMath;

import main.Gui;
import main.util.MutableBoolean;

public class SimulatorSSADirectHierarchical extends HierarchicalSimulator{
	
	private static Long initializationTime = new Long(0);
	
	public SimulatorSSADirectHierarchical(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType) 
	throws IOException, XMLStreamException {

		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed,
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
		
		if (sbmlHasErrorsFlag)
			return;
		
		long initTime2 = System.nanoTime();
		
		initializationTime += System.nanoTime() - initTime2;
		
		//SIMULATION LOOP
		currentTime = 0.0;
		double printTime = -0.00001;
		
		while (currentTime < timeLimit && !cancelFlag) {
			
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
			
			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();
			
			
			//STEP 2: calculate delta_t, the time till the next reaction execution
			 
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			System.out.println("delta t = " + delta_t + ": total propensity = " + totalPropensity);
			
			if (delta_t > maxTimeStep)
				delta_t = maxTimeStep;
			
			if (delta_t < minTimeStep)
				delta_t = minTimeStep;
			
			
			//STEP 3: select a reaction
			
			String selectedReactionID = selectReaction(r2);
			
			//if its length isn't positive then there aren't any reactions
			if (!selectedReactionID.isEmpty()) {
			
			//STEP 4: perform selected reaction and update species counts
				
				performReaction(selectedReactionID, true, true);
				
				//STEP 5: compute affected reactions' new propensities and update total propensity
				
				//create a set (precludes duplicates) of reactions that the selected reaction's species affect
				HashSet<String> affectedReactionSet = getAffectedReactionSet(selectedReactionID, true);
				
				updatePropensities(affectedReactionSet);
			}
			
			//update time for next iteration
			currentTime += delta_t;
			
			if (variableToIsInAssignmentRuleMap != null &&
					variableToIsInAssignmentRuleMap.containsKey("time"))				
				performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get("time"));
			
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
		
		//STEP 0: calculate initial propensities (including the total)		
		setupReactions();		
		
		setupForOutput(randomSeed, runNumber);
		
			
		bufferedTSDWriter.write("(" + "\"" + "time" + "\"");
		
		//if there's an interesting species, only those get printed
		if (interestingSpecies.size() > 0) {
			
			for (String speciesID : interestingSpecies)
				bufferedTSDWriter.write(", \"" + speciesID + "\"");
		}
		else {
		
			for (String speciesID : speciesIDSet) {				
				bufferedTSDWriter.write(", \"" + speciesID + "\"");
			}
			
			//print compartment IDs (for sizes)
			for (String componentID : compartmentIDSet) {
				
				bufferedTSDWriter.write(", \"" + componentID + "\"");
			}		
			
			//print nonconstant parameter IDs
			for (String parameterID : nonconstantParameterIDSet) {
				
				try {
					bufferedTSDWriter.write(", \"" + parameterID + "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}		
		}
		
		bufferedTSDWriter.write("),\n");
		
	}
	

	protected void eraseComponentFurther(HashSet<String> reactionIDs) {}
	
	/**
	 * 
	 */
	protected void updateAfterDynamicChanges() {}
	
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
		
		totalPropensity = 0.0;
		minPropensity = Double.MAX_VALUE;
		maxPropensity = Double.MIN_VALUE;
		
		//STEP 0A: calculate initial propensities (including the total)		
		setupReactions();		
		setupEvents();		
		setupForOutput(0, newRun);

	}
	
}
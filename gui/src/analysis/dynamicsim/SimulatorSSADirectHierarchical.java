package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;

import analysis.dynamicsim.HierarchicalSimulator.ModelState;
import analysis.dynamicsim.Simulator.StringDoublePair;

import odk.lang.FastMath;

import main.Gui;
import main.util.MutableBoolean;

public class SimulatorSSADirectHierarchical extends HierarchicalSimulator{
	
	private static Long initializationTime = new Long(0);
	private int submodelIndex = -1;

	
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
		double printTime = printInterval;
		
		double nextEventTime = handleEvents();
		
		while (currentTime < timeLimit && !cancelFlag && constraintFlag) 
		{
			
			//EVENT HANDLING
			//trigger and/or fire events, etc.
			if (topmodel.noEventsFlag == false) 
			{
				HashSet<String> affectedReactionSet = fireEvents(topmodel, topmodel.noRuleFlag, topmodel.noConstraintsFlag);				
				
				//recalculate propensties/groups for affected reactions
				if (affectedReactionSet.size() > 0)
					updatePropensities(affectedReactionSet, -1);
			}
			
			for(int i = 0; i < numSubmodels; i++)
			{
					if (submodels[i].noEventsFlag == false) {
						HashSet<String> affectedReactionSet = fireEvents(submodels[i], submodels[i].noRuleFlag, submodels[i].noConstraintsFlag);				
						
					//recalculate propensties/groups for affected reactions
					if (affectedReactionSet.size() > 0)
							updatePropensities(affectedReactionSet, i);
					}
			}
			
			//TSD PRINTING
			//print to TSD if the next print interval arrives
			//this obviously prints the previous timestep's data
//			if (currentTime >= printTime) {
//				
//				if (printTime < 0)
//					printTime = 0.0;
//					
//				try {
//					printToTSD(printTime);
//					bufferedTSDWriter.write(",\n");
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
//				printTime += printInterval;
//			}			
			
			//update progress bar			
			//progress.setValue((int)((currentTime / timeLimit) * 100.0));
			
			
			//STEP 1: generate random numbers
			
			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();
			
			
			//STEP 2: calculate delta_t, the time till the next reaction execution
			 
			double totalPropensity = getTotalPropensity();
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			double nextReactionTime = currentTime + delta_t;
			nextEventTime = handleEvents();
	
			if (nextReactionTime < nextEventTime && nextReactionTime < currentTime + maxTimeStep) {
				currentTime = nextReactionTime;
				// perform reaction
			} else if (nextEventTime < currentTime + maxTimeStep) {
				currentTime = nextEventTime;
				// print 
			} else {
				currentTime += maxTimeStep;
				// print
			}
			if (currentTime > timeLimit) {
				currentTime = timeLimit;
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
			if (currentTime == nextEventTime) {
			//STEP 3: select a reaction

				String selectedReactionID = selectReaction(r2);

				//if its length isn't positive then there aren't any reactions
				if (!selectedReactionID.isEmpty()) {

					//STEP 4: perform selected reaction and update species counts

					if(submodelIndex == -1)
					{
						//if its length isn't positive then there aren't any reactions
						if (!selectedReactionID.isEmpty()) {
							performReaction(topmodel, selectedReactionID, topmodel.noRuleFlag, topmodel.noConstraintsFlag);
							HashSet<String> affectedReactionSet = getAffectedReactionSet(topmodel, selectedReactionID, true);

							//STEP 5: compute affected reactions' new propensities and update total propensity
							updatePropensities(affectedReactionSet, submodelIndex);
						}

						//if (topmodel.variableToIsInAssignmentRuleMap != null &&
						//topmodel.variableToIsInAssignmentRuleMap.containsKey("time"))				
						//	performAssignmentRules(topmodel, topmodel.variableToAffectedAssignmentRuleSetMap.get("time"));
						/*
					if (topmodel.noEventsFlag == false) {

						handleEvents(topmodel, topmodel.noRuleFlag, topmodel.noConstraintsFlag);

						//step to the next event fire time if it comes before the next time step
						if (!topmodel.triggeredEventQueue.isEmpty() && topmodel.triggeredEventQueue.peek().fireTime <= currentTime)
							currentTime = topmodel.triggeredEventQueue.peek().fireTime;
					}
						 */
					}
					else
					{
						//if its length isn't positive then there aren't any reactions
						if (!selectedReactionID.isEmpty()) {
							performReaction(submodels[submodelIndex], selectedReactionID, submodels[submodelIndex].noRuleFlag, submodels[submodelIndex].noConstraintsFlag);

							HashSet<String> affectedReactionSet = getAffectedReactionSet(submodels[submodelIndex], selectedReactionID, true);

							//STEP 5: compute affected reactions' new propensities and update total propensity
							updatePropensities(affectedReactionSet, submodelIndex);
						}


						//	if (topmodel.variableToIsInAssignmentRuleMap != null &&
						//	submodels[submodelIndex].variableToIsInAssignmentRuleMap.containsKey("time"))				
						//performAssignmentRules(submodels[submodelIndex], submodels[submodelIndex].variableToAffectedAssignmentRuleSetMap.get("time"));

						/*
					if (submodels[submodelIndex].noEventsFlag == false) {

						handleEvents(submodels[submodelIndex], submodels[submodelIndex].noRuleFlag, submodels[submodelIndex].noConstraintsFlag);

						//step to the next event fire time if it comes before the next time step
						if (!submodels[submodelIndex].triggeredEventQueue.isEmpty() && submodels[submodelIndex].triggeredEventQueue.peek().fireTime <= currentTime)
							currentTime = submodels[submodelIndex].triggeredEventQueue.peek().fireTime;

					}*/
					}
				}
			}
			updateRules();

				//update time for next iteration
				//currentTime += delta_t;

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
		
		
		setupSpecies(topmodel);
		setupParameters(topmodel);	
		setupReactions(topmodel);		
		setupConstraints(topmodel);
		setupEvents(topmodel);
		setupInitialAssignments(topmodel);
		setupRules(topmodel);
		setupForOutput(randomSeed, runNumber);
		
		
		for(ModelState model : submodels)
		{
			setupSpecies(model);
			setupParameters(model);	
			setupReactions(model);		
			setupConstraints(model);
			setupEvents(model);
			setupInitialAssignments(model);
			setupRules(model);
			setupForOutput(randomSeed, runNumber);
		}
		
		bufferedTSDWriter.write("(" + "\"" + "time" + "\"");
		
		for (String speciesID : topmodel.speciesIDSet) {				
			bufferedTSDWriter.write(", \"" + speciesID + "\"");
		}
		for (String noConstantParam : topmodel.nonconstantParameterIDSet) 				
			bufferedTSDWriter.write(", \"" + noConstantParam + "\"");
		
		for(ModelState model : submodels)
		{
			for (String speciesID : model.speciesIDSet) 				
					bufferedTSDWriter.write(", \"" + speciesID + "_" + model.ID + "\"");
			for (String noConstantParam : model.nonconstantParameterIDSet) 				
				bufferedTSDWriter.write(", \"" + noConstantParam + "_" + model.ID + "\"");
		}
		
		
		bufferedTSDWriter.write("),\n");
		
	}
	
	/**
	 * updates the propensities of the reactions affected by the recently performed reaction
	 * @param affectedReactionSet the set of reactions affected by the recently performed reaction
	 */
	private void updatePropensities(HashSet<String> affectedReactionSet, int index) {
		
		//loop through the affected reactions and update the propensities
		for (String affectedReactionID : affectedReactionSet) {
			
			if(submodelIndex == -1)
			{
				HashSet<StringDoublePair> reactantStoichiometrySet = 
				topmodel.reactionToReactantStoichiometrySetMap.get(affectedReactionID);
				updatePropensities(topmodel, affectedReactionSet,affectedReactionID, reactantStoichiometrySet);
			}
			else
			{
				HashSet<StringDoublePair> reactantStoichiometrySet = 
				submodels[index].reactionToReactantStoichiometrySetMap.get(affectedReactionID);
				updatePropensities(submodels[index], affectedReactionSet,affectedReactionID, reactantStoichiometrySet); 
			}		
		}
	}
	
	/**
	 * Helper method
	 */
	private void updatePropensities(ModelState model, HashSet<String> affectedReactionSet, String affectedReactionID, HashSet<StringDoublePair> reactantStoichiometrySet) 
	{
		boolean notEnoughMoleculesFlag = false; 
		
		//check for enough molecules for the reaction to occur
		for (StringDoublePair speciesAndStoichiometry : reactantStoichiometrySet) {
			
			String speciesID = speciesAndStoichiometry.string;
			double stoichiometry = speciesAndStoichiometry.doub;
			
			//if there aren't enough molecules to satisfy the stoichiometry
			if (model.variableToValueMap.get(speciesID) < stoichiometry) {
				notEnoughMoleculesFlag = true;
				break;
			}
		}
		
		double newPropensity = 0.0;
		
		if (notEnoughMoleculesFlag == false) {
			
			newPropensity = evaluateExpressionRecursive(model, model.reactionToFormulaMap.get(affectedReactionID));
			//newPropensity = CalculatePropensityIterative(affectedReactionID);
		}
		
		double oldPropensity = model.reactionToPropensityMap.get(affectedReactionID);
		
		//add the difference of new v. old propensity to the total propensity
		model.propensity += newPropensity - oldPropensity;
		
		//totalPropensity += newPropensity - oldPropensity;
		
		model.reactionToPropensityMap.put(affectedReactionID, newPropensity);
		
		
	}	
	
	
	/**
	 * randomly selects a reaction to perform
	 * 
	 * @param r2 random number
	 * @return the ID of the selected reaction
	 */
	private String selectReaction(double r2) {
		
		double randomPropensity = r2 * (getTotalPropensity());
		double runningTotalReactionsPropensity = 0.0;
		String selectedReaction = "";
		
		//finds the reaction that the random propensity lies in
		//it keeps adding the next reaction's propensity to a running total
		//until the running total is greater than the random propensity
		
		
		for (String currentReaction : topmodel.reactionToPropensityMap.keySet()) {
			
			runningTotalReactionsPropensity += topmodel.reactionToPropensityMap.get(currentReaction);
			
			if (randomPropensity < runningTotalReactionsPropensity) 
			{
				selectedReaction = currentReaction;
				// keep track of submodel index
				submodelIndex = -1;
				return selectedReaction;
			}
		}
		
		for(int i = 0; i < numSubmodels; i ++)
		{
			
		for (String currentReaction : submodels[i].reactionToPropensityMap.keySet()) 
		{
			runningTotalReactionsPropensity += submodels[i].reactionToPropensityMap.get(currentReaction);
			
			if (randomPropensity < runningTotalReactionsPropensity) 
			{
				selectedReaction = currentReaction;
				// keep track of submodel index
				submodelIndex = i;
				return selectedReaction;
			}
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
		topmodel.clear();
		
		for(int i = 0; i < this.numSubmodels; i++)
			submodels[i].clear();
		
		for(String key : replacements.keySet())
			replacements.put(key, initReplacementState.get(key));
		
		

		/*
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = reader.readSBML(SBMLFileName);
		
		topmodel = new ModelState(document.getModel(), true, "");
		
		setupSubmodels(document);
		getComponentPortMap(document);
	*/
	}

	protected double handleEvents()
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (topmodel.noEventsFlag == false)
		{
			handleEvents(topmodel, topmodel.noRuleFlag, topmodel.noConstraintsFlag);
			//step to the next event fire time if it comes before the next time step
			if (!topmodel.triggeredEventQueue.isEmpty() && topmodel.triggeredEventQueue.peek().fireTime <= nextEventTime)
				nextEventTime = topmodel.triggeredEventQueue.peek().fireTime;
		}
		
		for(int i = 0; i < numSubmodels; i++)
			if (submodels[i].noEventsFlag == false){
				handleEvents(submodels[i], submodels[i].noRuleFlag, submodels[i].noConstraintsFlag);
				//step to the next event fire time if it comes before the next time step
				if (!submodels[i].triggeredEventQueue.isEmpty() && submodels[i].triggeredEventQueue.peek().fireTime <= nextEventTime)
					nextEventTime = submodels[i].triggeredEventQueue.peek().fireTime;
			}
		return nextEventTime;
	}
	
	/**
	 * does minimized initalization process to prepare for a new run
	 */
	protected void setupForNewRun(int newRun) {

		try {
			setupSpecies(topmodel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setupParameters(topmodel);		
		setupReactions(topmodel);		
		
		setupForOutput(0, newRun);
		
		for(ModelState model : submodels)
		{
		try {
			setupSpecies(model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setupParameters(model);	
		setupReactions(model);		
		
		setupForOutput(0, newRun);
		
		}
		

		
		for (String speciesID : topmodel.speciesIDSet) {				
			try {
				bufferedTSDWriter.write(", \"" + speciesID + "\"");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(ModelState model : submodels)
		{
			for (String speciesID : model.speciesIDSet) {				
					try {
						bufferedTSDWriter.write(", \"" + speciesID + "_" + model.ID + "\"");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
		}
		
		
		try {
			bufferedTSDWriter.write("),\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
}
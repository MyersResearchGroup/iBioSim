package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import odk.lang.FastMath;

public class SimulatorSSADirectHierarchical extends HierarchicalSimulator{

	private static Long initializationTime = new Long(0);
	private String modelstateID;
	private boolean updateRateRule;


	public SimulatorSSADirectHierarchical(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType) 
					throws IOException, XMLStreamException {

		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, progress,
				printInterval, stoichAmpValue, running, interestingSpecies, quantityType);

		initialize(randomSeed, 1);
		modelstateID = "topmodel";

	}

	@Override
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
				HashSet<String> affectedReactionSet = fireEvents(topmodel, "reaction", topmodel.noRuleFlag, topmodel.noConstraintsFlag);				

				//recalculate propensties/groups for affected reactions
				if (affectedReactionSet.size() > 0)
					updatePropensities(affectedReactionSet, "topmodel");
			}

			for(ModelState models : submodels.values())
			{
				if (models.noEventsFlag == false) {
					HashSet<String> affectedReactionSet = fireEvents(models, "reaction", models.noRuleFlag, models.noConstraintsFlag);				

					//recalculate propensties/groups for affected reactions
					if (affectedReactionSet.size() > 0)
						updatePropensities(affectedReactionSet, models.ID);
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

			//STEP 1: generate random numbers

			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();


			//STEP 2: calculate delta_t, the time till the next reaction execution

			double totalPropensity = getTotalPropensity();
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			double nextReactionTime = currentTime + delta_t;
			nextEventTime = handleEvents();
			updateRateRule = false;
			
			if (nextReactionTime < nextEventTime && nextReactionTime < currentTime + maxTimeStep) 
			{
				currentTime = nextReactionTime;
				// perform reaction
			} 
			else if (nextEventTime < currentTime + maxTimeStep) 
			{
				currentTime = nextEventTime;
				// print 
			} 
			else 
			{
				currentTime += maxTimeStep;
				updateRateRule = true;
				// print
			}
			
			if (currentTime > timeLimit) 
			{
				currentTime = timeLimit;
			}
			
			while (currentTime > printTime && printTime < timeLimit) 
			{

				try {
					printToTSD(printTime);
					bufferedTSDWriter.write(",\n");
				} catch (IOException e) {
					e.printStackTrace();
				}

				printTime += printInterval;
				running.setTitle("Progress (" + (int)((currentTime / timeLimit) * 100.0) + "%)");
				//update progress bar			
				progress.setValue((int)((currentTime / timeLimit) * 100.0));

			}
			
			if (currentTime == nextReactionTime) 
			{
				//STEP 3: select a reaction

				String selectedReactionID = selectReaction(r2);

				//if its length isn't positive then there aren't any reactions
				if (!selectedReactionID.isEmpty()) {

					//STEP 4: perform selected reaction and update species counts

					if(modelstateID.equals("topmodel"))
					{
						//if its length isn't positive then there aren't any reactions
						if (!selectedReactionID.isEmpty()) {
							performReaction(topmodel, selectedReactionID, topmodel.noRuleFlag, topmodel.noConstraintsFlag);
							HashSet<String> affectedReactionSet = getAffectedReactionSet(topmodel, selectedReactionID, true);

							//STEP 5: compute affected reactions' new propensities and update total propensity
							updatePropensities(affectedReactionSet, modelstateID);
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
							performReaction(submodels.get(modelstateID), selectedReactionID, submodels.get(modelstateID).noRuleFlag, submodels.get(modelstateID).noConstraintsFlag);

							HashSet<String> affectedReactionSet = getAffectedReactionSet(submodels.get(modelstateID), selectedReactionID, true);

							//STEP 5: compute affected reactions' new propensities and update total propensity
							updatePropensities(affectedReactionSet, modelstateID);
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
			
			if(updateRateRule)
			{
				//updatePropensities(performRateRules(topmodel, currentTime), "topmodel");
				performRateRules(topmodel, currentTime);
			
				for(ModelState modelstate : submodels.values())
				{
					//updatePropensities(performRateRules(modelstate, currentTime), modelstate.ID);
					performRateRules(modelstate, currentTime);
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
	 * performs every rate rule using the current time step
	 * 
	 * @param delta_t
	 * @return
	 */
	private HashSet<String> performRateRules(ModelState modelstate, double delta_t) {
		
		HashSet<String> affectedVariables = new HashSet<String>();
		
		for (Rule rule : modelstate.rateRulesList) {

				RateRule rateRule = (RateRule) rule;	
				String variable = rateRule.getVariable();
				
				//update the species count (but only if the species isn't constant) (bound cond is fine)
				if (modelstate.variableToIsConstantMap.containsKey(variable) && modelstate.variableToIsConstantMap.get(variable) == false) {
					
					if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
							modelstate.speciesToHasOnlySubstanceUnitsMap.get(variable) == false) {
						
						double currVal = modelstate.getVariableToValue(variable);
						double incr = delta_t * (
								evaluateExpressionRecursive(modelstate, rateRule.getMath()) * 
								modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(variable)));
						modelstate.setvariableToValueMap(variable, currVal + incr);
					}
					else {
						double currVal = modelstate.getVariableToValue(variable);
						double incr = delta_t * evaluateExpressionRecursive(modelstate, rateRule.getMath());
						
						modelstate.setvariableToValueMap(variable, currVal + incr);
					}
					
					affectedVariables.add(variable);
				}
			
		}
		
		return affectedVariables;
	}

	
	
	/**
	 * sets up data structures local to the SSA-Direct method
	 * 
	 * @param noEventsFlag
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 */
	private void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException {	 
		setupNonConstantSpeciesReferences(topmodel);
		setupSpecies(topmodel);
		setupParameters(topmodel);	
		setupReactions(topmodel);		
		setupConstraints(topmodel);
		setupEvents(topmodel);
		setupInitialAssignments(topmodel);
		setupRules(topmodel);
		setupForOutput(randomSeed, runNumber);



			for(ModelState model : submodels.values())
			{
				setupNonConstantSpeciesReferences(model);
				setupSpecies(model);
				setupParameters(model);	
				setupReactions(model);		
				setupConstraints(model);
				setupEvents(model);
				setupInitialAssignments(model);
				setupRules(model);
				setupForOutput(randomSeed, runNumber);
			}
		
		setupReplacingSpecies();



		bufferedTSDWriter.write("(" + "\"" + "time" + "\"");

		if(interestingSpecies.length > 0)
		{
			for(String s : interestingSpecies)
			{

				bufferedTSDWriter.write(",\"" + s + "\"");
				
			}

			bufferedTSDWriter.write("),\n");
			
			return;
		}
		
		for (String speciesID : topmodel.speciesIDSet)
		{	
				bufferedTSDWriter.write(",\"" + speciesID + "\"");
		}
		
		for (String noConstantParam : topmodel.variablesToPrint) 
			{
				bufferedTSDWriter.write(",\"" + noConstantParam + "\"");
			}
		/*
		for (String compartment : topmodel.compartmentIDSet)
		{
			bufferedTSDWriter.write(", \"" + compartment + "\"");
		}
		*/
		for(ModelState model : submodels.values())
		{
			for (String speciesID : model.speciesIDSet) 		
				if(!model.isHierarchical.contains(speciesID))
				{
					bufferedTSDWriter.write(",\"" + model.ID + "__" + speciesID + "\"");
				}
			
			for (String noConstantParam : model.variablesToPrint)
				if(!model.isHierarchical.contains(noConstantParam))
				{
					bufferedTSDWriter.write(",\"" + model.ID + "__" +  noConstantParam + "\"");
				}
		}

		
		bufferedTSDWriter.write("),\n");

	}


	/**
	 * updates the propensities of the reactions affected by the recently performed reaction
	 * @param affectedReactionSet the set of reactions affected by the recently performed reaction
	 */
	private void updatePropensities(HashSet<String> affectedReactionSet, String id) {

		//loop through the affected reactions and update the propensities
		for (String affectedReactionID : affectedReactionSet) {

			if(id.equals("topmodel"))
			{
				if(topmodel.isDeletedByMetaID(affectedReactionID))
					continue;
				
				HashSet<StringDoublePair> reactantStoichiometrySet = 
						topmodel.reactionToReactantStoichiometrySetMap.get(affectedReactionID);
				updatePropensities(topmodel, affectedReactionID,reactantStoichiometrySet);
			}
			else
			{
				if(submodels.get(id).isDeletedByMetaID(affectedReactionID))
					continue;
				HashSet<StringDoublePair> reactantStoichiometrySet = 

						submodels.get(id).reactionToReactantStoichiometrySetMap.get(affectedReactionID);
				updatePropensities(submodels.get(id), affectedReactionID,reactantStoichiometrySet); 
			}		
		}
	}

	/**
	 * Helper method
	 */
	private void updatePropensities(ModelState model, String affectedReactionID, HashSet<StringDoublePair> reactantStoichiometrySet) 
	{

		if(model.reactionToFormulaMap.get(affectedReactionID) == null)
		{

			model.reactionToPropensityMap.put(affectedReactionID, 0.0);
			return;
		}
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
		//model.propensity = newPropensity - oldPropensity;
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
				modelstateID = "topmodel";
				return selectedReaction;
			}
		}

		for(ModelState models : submodels.values())
		{

			for (String currentReaction : models.reactionToPropensityMap.keySet()) 
			{
				runningTotalReactionsPropensity += models.reactionToPropensityMap.get(currentReaction);

				if (randomPropensity < runningTotalReactionsPropensity) 
				{
					selectedReaction = currentReaction;
					// keep track of submodel index
					modelstateID = models.ID;
					return selectedReaction;
				}
			}
		}


		return selectedReaction;		
	}

	/**
	 * cancels the current run
	 */
	@Override
	protected void cancel() {

		cancelFlag = true;
	}

	/**
	 * clears data structures for new run
	 */
	@Override
	protected void clear() {
		topmodel.clear();

		for(ModelState modelstate : submodels.values())
			modelstate.clear();


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

	private double handleEvents()
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (topmodel.noEventsFlag == false)
		{
			handleEvents(topmodel);
			//step to the next event fire time if it comes before the next time step
			if (!topmodel.triggeredEventQueue.isEmpty() && topmodel.triggeredEventQueue.peek().fireTime <= nextEventTime)
				if(topmodel.triggeredEventQueue.peek().fireTime < nextEventTime)
					nextEventTime = topmodel.triggeredEventQueue.peek().fireTime;
		}

		for(ModelState models : submodels.values())
			if (models.noEventsFlag == false){
				handleEvents(models);
				//step to the next event fire time if it comes before the next time step
				if (!models.triggeredEventQueue.isEmpty() && models.triggeredEventQueue.peek().fireTime <= nextEventTime)
					if(models.triggeredEventQueue.peek().fireTime < nextEventTime)
						nextEventTime = models.triggeredEventQueue.peek().fireTime;
			}
		return nextEventTime;
	}

	/**
	 * does minimized initalization process to prepare for a new run
	 */
	@Override
	protected void setupForNewRun(int newRun) {

		try {
			setupNonConstantSpeciesReferences(topmodel);
			setupSpecies(topmodel);
			setupParameters(topmodel);	
			setupReactions(topmodel);		
			setupConstraints(topmodel);
			setupEvents(topmodel);
			setupInitialAssignments(topmodel);
			setupRules(topmodel);
			setupForOutput(0, newRun);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		for(ModelState model : submodels.values())
		{
			try {
				setupNonConstantSpeciesReferences(model);
				setupSpecies(model);
				setupParameters(model);	
				setupReactions(model);		
				setupConstraints(model);
				setupEvents(model);
				setupInitialAssignments(model);
				setupRules(model);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}



		setupReplacingSpecies();

		constraintFlag = true;

		try {
			for (String speciesID : topmodel.speciesIDSet) {				
				bufferedTSDWriter.write(",\"" + speciesID + "\"");
			}
			for (String noConstantParam : topmodel.variablesToPrint) 				
				bufferedTSDWriter.write(",\"" + noConstantParam + "\"");
			/*
			for (String compartment : topmodel.compartmentIDSet)
			{
				bufferedTSDWriter.write(", \"" + compartment + "\"");
			}
			*/
			for(ModelState model : submodels.values())
			{
				for (String speciesID : model.speciesIDSet) 				
					bufferedTSDWriter.write(",\"" + model.ID + "__" + speciesID + "\"");
				for (String noConstantParam : model.variablesToPrint) 				
					bufferedTSDWriter.write(",\"" + model.ID + "__" +  noConstantParam + "\"");
				/*
				for (String compartment : model.compartmentIDSet)
					bufferedTSDWriter.write(", \"" + model.ID + "__" + compartment + "\"");
				*/
			}


			bufferedTSDWriter.write("),\n");

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
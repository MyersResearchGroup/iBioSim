package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import odk.lang.FastMath;

import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import analysis.dynamicsim.hierarchical.HierarchicalSimulator;
import analysis.dynamicsim.hierarchical.util.HierarchicalStringDoublePair;

public class HierarchicalSSADirectSimulator extends HierarchicalSimulator{

	private static Long initializationTime = new Long(0);
	private String modelstateID;
	private boolean updateRateRule;


	public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType) 
					throws IOException, XMLStreamException {

		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep, progress,
				printInterval, stoichAmpValue, running, interestingSpecies, quantityType);

		initialize(randomSeed, 1);
		modelstateID = "topmodel";

	}

	@Override
	public void simulate() {

		if (isSbmlHasErrorsFlag())
			return;

		long initTime2 = System.nanoTime();

		initializationTime += System.nanoTime() - initTime2;

		//SIMULATION LOOP
		setCurrentTime(0.0);
		double printTime = getPrintInterval();

		double nextEventTime = handleEvents();

		while (getCurrentTime() < getTimeLimit() && !isCancelFlag()) 
		{

			//EVENT HANDLING
			//trigger and/or fire events, etc.
			if (getTopmodel().isNoEventsFlag() == false) 
			{
				HashSet<String> affectedReactionSet = fireEvents(getTopmodel(), "reaction", getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag());				

				//recalculate propensties/groups for affected reactions
				if (affectedReactionSet.size() > 0)
					updatePropensities(affectedReactionSet, "topmodel");
			}

			for(ModelState models : getSubmodels().values())
			{
				if (models.isNoEventsFlag() == false) {
					HashSet<String> affectedReactionSet = fireEvents(models, "reaction", models.isNoRuleFlag(), models.isNoConstraintsFlag());				

					//recalculate propensties/groups for affected reactions
					if (affectedReactionSet.size() > 0)
						updatePropensities(affectedReactionSet, models.getID());
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

			double r1 = getRandomNumberGenerator().nextDouble();
			double r2 = getRandomNumberGenerator().nextDouble();


			//STEP 2: calculate delta_t, the time till the next reaction execution

			double totalPropensity = getTotalPropensity();
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			double nextReactionTime = getCurrentTime() + delta_t;
			nextEventTime = handleEvents();
			updateRateRule = false;
			
			if (nextReactionTime < nextEventTime && nextReactionTime < getCurrentTime() + getMaxTimeStep()) 
			{
				setCurrentTime(nextReactionTime);
				// perform reaction
			} 
			else if (nextEventTime < getCurrentTime() + getMaxTimeStep()) 
			{
				setCurrentTime(nextEventTime);
				// print 
			} 
			else 
			{
				setCurrentTime(getCurrentTime() + getMaxTimeStep());
				updateRateRule = true;
				// print
			}
			
			if (getCurrentTime() > getTimeLimit()) 
			{
				setCurrentTime(getTimeLimit());
			}
			
			while (getCurrentTime() > printTime && printTime < getTimeLimit()) 
			{

				try {
					printToTSD(printTime);
					getBufferedTSDWriter().write(",\n");
				} catch (IOException e) {
					e.printStackTrace();
				}

				printTime += getPrintInterval();
				getRunning().setTitle("Progress (" + (int)((getCurrentTime() / getTimeLimit()) * 100.0) + "%)");
				//update progress bar			
				getProgress().setValue((int)((getCurrentTime() / getTimeLimit()) * 100.0));

			}
			
			if (getCurrentTime() == nextReactionTime) 
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
							performReaction(getTopmodel(), selectedReactionID, getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag());
							HashSet<String> affectedReactionSet = getAffectedReactionSet(getTopmodel(), selectedReactionID, true);

							//STEP 5: compute affected reactions' new propensities and update total propensity
							updatePropensities(affectedReactionSet, modelstateID);
						}

						//if (getTopmodel().variableToIsInAssignmentRuleMap != null &&
						//getTopmodel().variableToIsInAssignmentRuleMap.containsKey("time"))				
						//	performAssignmentRules(getTopmodel(), getTopmodel().variableToAffectedAssignmentRuleSetMap.get("time"));
						/*
					if (getTopmodel().isNoEventsFlag() == false) {

						handleEvents(getTopmodel(), getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag());

						//step to the next event fire time if it comes before the next time step
						if (!getTopmodel().getTriggeredEventQueue().isEmpty() && getTopmodel().getTriggeredEventQueue().peek().fireTime <= currentTime)
							currentTime = getTopmodel().getTriggeredEventQueue().peek().fireTime;
					}
						 */
					}
					else
					{
						//if its length isn't positive then there aren't any reactions
						if (!selectedReactionID.isEmpty()) {
							performReaction(getSubmodels().get(modelstateID), selectedReactionID, getSubmodels().get(modelstateID).isNoRuleFlag(), getSubmodels().get(modelstateID).isNoConstraintsFlag());

							HashSet<String> affectedReactionSet = getAffectedReactionSet(getSubmodels().get(modelstateID), selectedReactionID, true);

							//STEP 5: compute affected reactions' new propensities and update total propensity
							updatePropensities(affectedReactionSet, modelstateID);
						}


						//	if (getTopmodel().variableToIsInAssignmentRuleMap != null &&
						//	getSubmodels()[submodelIndex].variableToIsInAssignmentRuleMap.containsKey("time"))				
						//performAssignmentRules(getSubmodels()[submodelIndex], getSubmodels()[submodelIndex].variableToAffectedAssignmentRuleSetMap.get("time"));

						/*
					if (getSubmodels()[submodelIndex].isNoEventsFlag() == false) {

						handleEvents(getSubmodels()[submodelIndex], getSubmodels()[submodelIndex].isNoRuleFlag(), getSubmodels()[submodelIndex].isNoConstraintsFlag());

						//step to the next event fire time if it comes before the next time step
						if (!getSubmodels()[submodelIndex].getTriggeredEventQueue().isEmpty() && getSubmodels()[submodelIndex].getTriggeredEventQueue().peek().fireTime <= currentTime)
							currentTime = getSubmodels()[submodelIndex].getTriggeredEventQueue().peek().fireTime;

					}*/
					}
				}
			}
			
			if(updateRateRule)
			{
				//updatePropensities(performRateRules(getTopmodel(), currentTime), "topmodel");
				performRateRules(getTopmodel(), getCurrentTime());
			
				for(ModelState modelstate : getSubmodels().values())
				{
					//updatePropensities(performRateRules(modelstate, currentTime), modelstate.getID());
					performRateRules(modelstate, getCurrentTime());
				}
			}
			
			updateRules();

			//update time for next iteration
			//currentTime += delta_t;

		} //end simulation loop

		if (isCancelFlag() == false) {

			//print the final species counts
			try {
				printToTSD(printTime);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				getBufferedTSDWriter().write(')');
				getBufferedTSDWriter().flush();
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
		
		for (Rule rule : modelstate.getRateRulesList()) {

				RateRule rateRule = (RateRule) rule;	
				String variable = rateRule.getVariable();
				
				//update the species count (but only if the species isn't constant) (bound cond is fine)
				if (modelstate.getVariableToIsConstantMap().containsKey(variable) && modelstate.getVariableToIsConstantMap().get(variable) == false) {
					
					if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(variable) &&
							modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(variable) == false) {
						
						double currVal = modelstate.getVariableToValue(getReplacements(), variable);
						double incr = delta_t * (
								evaluateExpressionRecursive(modelstate, rateRule.getMath()) * 
								modelstate.getVariableToValue(getReplacements(), modelstate.getSpeciesToCompartmentNameMap().get(variable)));
						modelstate.setvariableToValueMap(getReplacements(), variable, currVal + incr);
					}
					else {
						double currVal = modelstate.getVariableToValue(getReplacements(), variable);
						double incr = delta_t * evaluateExpressionRecursive(modelstate, rateRule.getMath());
						
						modelstate.setvariableToValueMap(getReplacements(), variable, currVal + incr);
					}
					
					affectedVariables.add(variable);
				}
			
		}
		
		return affectedVariables;
	}

	
	
	/**
	 * sets up data structures local to the SSA-Direct method
	 * 
	 * @param isNoEventsFlag()
	 * @param noAssignmentRulesFlag
	 * @param isNoConstraintsFlag()
	 */
	private void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException {	 
		setupNonConstantSpeciesReferences(getTopmodel());
		setupSpecies(getTopmodel());
		setupParameters(getTopmodel());	
		setupReactions(getTopmodel());		
		setupConstraints(getTopmodel());
		setupEvents(getTopmodel());
		setupInitialAssignments(getTopmodel());
		setupRules(getTopmodel());
		setupForOutput(randomSeed, runNumber);



			for(ModelState model : getSubmodels().values())
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


		getBufferedTSDWriter().write("(" + "\"" + "time" + "\"");

		if(getInterestingSpecies().length > 0)
		{
			for(String s : getInterestingSpecies())
			{

				getBufferedTSDWriter().write(",\"" + s + "\"");
				
			}

			getBufferedTSDWriter().write("),\n");
			
			return;
		}
		
		for (String speciesID : getTopmodel().getSpeciesIDSet())
		{	
			getBufferedTSDWriter().write(",\"" + speciesID + "\"");
		}
		
		for (String noConstantParam : getTopmodel().getVariablesToPrint()) 
			{
			getBufferedTSDWriter().write(",\"" + noConstantParam + "\"");
			}
		/*
		for (String compartment : getTopmodel().compartmentIDSet)
		{
			bufferedTSDWriter.write(", \"" + compartment + "\"");
		}
		*/
		for(ModelState model : getSubmodels().values())
		{
			for (String speciesID : model.getSpeciesIDSet()) 		
				if(!model.getIsHierarchical().contains(speciesID))
				{
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" + speciesID + "\"");
				}
			
			for (String noConstantParam : model.getVariablesToPrint())
				if(!model.getIsHierarchical().contains(noConstantParam))
				{
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" +  noConstantParam + "\"");
				}
		}

		
		getBufferedTSDWriter().write("),\n");

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
				if(getTopmodel().isDeletedByMetaID(affectedReactionID))
					continue;
				
				HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet = 
						getTopmodel().getReactionToReactantStoichiometrySetMap().get(affectedReactionID);
				updatePropensities(getTopmodel(), affectedReactionID,reactantStoichiometrySet);
			}
			else
			{
				if(getSubmodels().get(id).isDeletedByMetaID(affectedReactionID))
					continue;
				HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet = 

						getSubmodels().get(id).getReactionToReactantStoichiometrySetMap().get(affectedReactionID);
				updatePropensities(getSubmodels().get(id), affectedReactionID,reactantStoichiometrySet); 
			}		
		}
	}

	/**
	 * Helper method
	 */
	private void updatePropensities(ModelState model, String affectedReactionID, HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet) 
	{

		if(model.getReactionToFormulaMap().get(affectedReactionID) == null)
		{

			model.getReactionToPropensityMap().put(affectedReactionID, 0.0);
			return;
		}
		boolean notEnoughMoleculesFlag = false; 

		//check for enough molecules for the reaction to occur
		for (HierarchicalStringDoublePair speciesAndStoichiometry : reactantStoichiometrySet) {

			String speciesID = speciesAndStoichiometry.string;
			double stoichiometry = speciesAndStoichiometry.doub;

			//if there aren't enough molecules to satisfy the stoichiometry
			if (model.getVariableToValueMap().get(speciesID) < stoichiometry) {
				notEnoughMoleculesFlag = true;
				break;
			}
		}

		double newPropensity = 0.0;

		if (notEnoughMoleculesFlag == false) {

			newPropensity = evaluateExpressionRecursive(model, model.getReactionToFormulaMap().get(affectedReactionID));
			//newPropensity = CalculatePropensityIterative(affectedReactionID);
		}

		double oldPropensity = model.getReactionToPropensityMap().get(affectedReactionID);

		//add the difference of new v. old propensity to the total propensity
		model.setPropensity(model.getPropensity() + newPropensity - oldPropensity);
		//model.propensity = newPropensity - oldPropensity;
		//totalPropensity += newPropensity - oldPropensity;

		model.getReactionToPropensityMap().put(affectedReactionID, newPropensity);


	}	


	/**
	 * randomly selects a reaction to perform
	 * 
	 * @param r2 random number
	 * @return the getID() of the selected reaction
	 */
	private String selectReaction(double r2) {

		double randomPropensity = r2 * (getTotalPropensity());
		double runningTotalReactionsPropensity = 0.0;
		String selectedReaction = "";

		//finds the reaction that the random propensity lies in
		//it keeps adding the next reaction's propensity to a running total
		//until the running total is greater than the random propensity


		for (String currentReaction : getTopmodel().getReactionToPropensityMap().keySet()) {

			runningTotalReactionsPropensity += getTopmodel().getReactionToPropensityMap().get(currentReaction);

			if (randomPropensity < runningTotalReactionsPropensity) 
			{
				selectedReaction = currentReaction;
				// keep track of submodel index
				modelstateID = "topmodel";
				return selectedReaction;
			}
		}

		for(ModelState models : getSubmodels().values())
		{

			for (String currentReaction : models.getReactionToPropensityMap().keySet()) 
			{
				runningTotalReactionsPropensity += models.getReactionToPropensityMap().get(currentReaction);

				if (randomPropensity < runningTotalReactionsPropensity) 
				{
					selectedReaction = currentReaction;
					// keep track of submodel index
					modelstateID = models.getID();
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
	public void cancel() {

		setCancelFlag(true);
	}

	/**
	 * clears data structures for new run
	 */
	@Override
	public void clear() {
		getTopmodel().clear();

		for(ModelState modelstate : getSubmodels().values())
			modelstate.clear();


		for(String key : getReplacements().keySet())
			getReplacements().put(key, getInitReplacementState().get(key));

	}

	private double handleEvents()
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (getTopmodel().isNoEventsFlag() == false)
		{
			handleEvents(getTopmodel());
			//step to the next event fire time if it comes before the next time step
			if (!getTopmodel().getTriggeredEventQueue().isEmpty() && getTopmodel().getTriggeredEventQueue().peek().fireTime <= nextEventTime)
				if(getTopmodel().getTriggeredEventQueue().peek().fireTime < nextEventTime)
					nextEventTime = getTopmodel().getTriggeredEventQueue().peek().fireTime;
		}

		for(ModelState models : getSubmodels().values())
			if (models.isNoEventsFlag() == false){
				handleEvents(models);
				//step to the next event fire time if it comes before the next time step
				if (!models.getTriggeredEventQueue().isEmpty() && models.getTriggeredEventQueue().peek().fireTime <= nextEventTime)
					if(models.getTriggeredEventQueue().peek().fireTime < nextEventTime)
						nextEventTime = models.getTriggeredEventQueue().peek().fireTime;
			}
		return nextEventTime;
	}

	/**
	 * does minimized initalization process to prepare for a new run
	 */
	@Override
	public void setupForNewRun(int newRun) {

		try {
			setupNonConstantSpeciesReferences(getTopmodel());
			setupSpecies(getTopmodel());
			setupParameters(getTopmodel());	
			setupReactions(getTopmodel());		
			setupConstraints(getTopmodel());
			setupEvents(getTopmodel());
			setupInitialAssignments(getTopmodel());
			setupRules(getTopmodel());
			setupForOutput(0, newRun);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		for(ModelState model : getSubmodels().values())
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

		setConstraintFlag(true);

		try {
			for (String speciesID : getTopmodel().getSpeciesIDSet()) {				
				getBufferedTSDWriter().write(",\"" + speciesID + "\"");
			}
			for (String noConstantParam : getTopmodel().getVariablesToPrint()) 				
				getBufferedTSDWriter().write(",\"" + noConstantParam + "\"");
			/*
			for (String compartment : getTopmodel().compartmentIDSet)
			{
				bufferedTSDWriter.write(", \"" + compartment + "\"");
			}
			*/
			for(ModelState model : getSubmodels().values())
			{
				for (String speciesID : model.getSpeciesIDSet()) 				
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" + speciesID + "\"");
				for (String noConstantParam : model.getVariablesToPrint()) 				
					getBufferedTSDWriter().write(",\"" + model.getID() + "__" +  noConstantParam + "\"");
				/*
				for (String compartment : model.compartmentIDSet)
					bufferedTSDWriter.write(", \"" + model.getID() + "__" + compartment + "\"");
				*/
			}


			getBufferedTSDWriter().write("),\n");

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

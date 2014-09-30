package analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.ArrayList;
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
			if (getTopmodel().isNoEventsFlag() == false) 
			{
				HashSet<String> affectedReactionSet = fireEvents(getTopmodel(), "reaction", getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag());				
				if (affectedReactionSet.size() > 0)
				{
					updatePropensities(affectedReactionSet, getTopmodel());
					updatePropensities(getTopmodel());
				}
			}

			for(ModelState models : getSubmodels().values())
			{
				if (models.isNoEventsFlag() == false) {
					HashSet<String> affectedReactionSet = fireEvents(models, "reaction", models.isNoRuleFlag(), models.isNoConstraintsFlag());				
					if (affectedReactionSet.size() > 0)
					{
						updatePropensities(affectedReactionSet, models);
						updatePropensities(models);
					}
				}
			}

			if(getCurrentTime()  > 2000)
			{
				System.out.print("");
			}
			double r1 = getRandomNumberGenerator().nextDouble();
			double r2 = getRandomNumberGenerator().nextDouble();
			double totalPropensity = getTotalPropensity();
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			double nextReactionTime = getCurrentTime() + delta_t;
			nextEventTime = handleEvents();
			updateRateRule = false;

			if (nextReactionTime < nextEventTime && nextReactionTime < getCurrentTime() + getMaxTimeStep()) 
			{
				setCurrentTime(nextReactionTime);
			} 
			else if (nextEventTime <= getCurrentTime() + getMaxTimeStep()) 
			{
				setCurrentTime(nextEventTime);
			} 
			else 
			{
				setCurrentTime(getCurrentTime() + getMaxTimeStep());
				updateRateRule = true;
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
				getProgress().setValue((int)((getCurrentTime() / getTimeLimit()) * 100.0));

			}

			if (getCurrentTime() == nextReactionTime) 
			{
				String selectedReactionID = selectReaction(r2);
				if (!selectedReactionID.isEmpty()) {
					if(modelstateID.equals("topmodel"))
					{
						if (!selectedReactionID.isEmpty()) {
							performReaction(getTopmodel(), selectedReactionID, getTopmodel().isNoRuleFlag(), getTopmodel().isNoConstraintsFlag());
							HashSet<String> affectedReactionSet = getAffectedReactionSet(getTopmodel(), selectedReactionID, true);
							updatePropensities(affectedReactionSet, getTopmodel());
							updatePropensities(getTopmodel());
						}
					}
					else
					{
						if (!selectedReactionID.isEmpty()) {
							performReaction(getSubmodels().get(modelstateID), selectedReactionID, getSubmodels().get(modelstateID).isNoRuleFlag(), getSubmodels().get(modelstateID).isNoConstraintsFlag());
							HashSet<String> affectedReactionSet = getAffectedReactionSet(getSubmodels().get(modelstateID), selectedReactionID, true);
							updatePropensities(affectedReactionSet, getSubmodels().get(modelstateID));
							updatePropensities(getSubmodels().get(modelstateID));
						}
					}
				}
			}

			if(updateRateRule)
			{
				performRateRules(getTopmodel(), getCurrentTime());

				for(ModelState modelstate : getSubmodels().values())
				{
					performRateRules(modelstate, getCurrentTime());
				}
			}

			updateRules();
		}

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


	private void updatePropensities(HashSet<String> affectedReactionSet, ModelState modelstate) {
		for (String affectedReactionID : affectedReactionSet) {

			if(modelstate.isDeletedByMetaID(affectedReactionID))
				continue;

			HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet = 
					modelstate.getReactionToReactantStoichiometrySetMap().get(affectedReactionID);
			updatePropensity(modelstate, affectedReactionID,reactantStoichiometrySet);		
		}
	}

	private void updatePropensities(ModelState modelstate) {
		if(modelstate != getTopmodel())
			for (String reaction : getTopmodel().getHierarchicalReactions())
			{
				if(getTopmodel().isDeletedByMetaID(reaction))
					continue;

				HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet = 
						getTopmodel().getReactionToReactantStoichiometrySetMap().get(reaction);
				updatePropensity(getTopmodel(), reaction, reactantStoichiometrySet);
			}


		for(ModelState model : getSubmodels().values())
		{
			if(modelstate != model)
				for (String reaction : model.getHierarchicalReactions())
				{
					if(model.isDeletedByMetaID(reaction))
						continue;

					HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet = 
							model.getReactionToReactantStoichiometrySetMap().get(reaction);
					updatePropensity(model, reaction, reactantStoichiometrySet);
				}
		}
	}


	private void updatePropensity(ModelState model, String affectedReactionID, HashSet<HierarchicalStringDoublePair> reactantStoichiometrySet) 
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
			if (model.getVariableToValue(getReplacements(), speciesID) < stoichiometry) {
				notEnoughMoleculesFlag = true;
				break;
			}
		}

		double newPropensity = 0.0;
		if (notEnoughMoleculesFlag == false) {
			newPropensity = evaluateExpressionRecursive(model, model.getReactionToFormulaMap().get(affectedReactionID));
		}

		double oldPropensity = model.getReactionToPropensityMap().get(affectedReactionID);
		model.setPropensity(model.getPropensity() + newPropensity - oldPropensity);
		model.updateReactionToPropensityMap(affectedReactionID, newPropensity);
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
			if (!getTopmodel().getTriggeredEventQueue().isEmpty() && getTopmodel().getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
				if(getTopmodel().getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
					nextEventTime = getTopmodel().getTriggeredEventQueue().peek().getFireTime();
		}

		for(ModelState models : getSubmodels().values())
			if (models.isNoEventsFlag() == false){
				handleEvents(models);
				//step to the next event fire time if it comes before the next time step
				if (!models.getTriggeredEventQueue().isEmpty() && models.getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
					if(models.getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
						nextEventTime = models.getTriggeredEventQueue().peek().getFireTime();
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

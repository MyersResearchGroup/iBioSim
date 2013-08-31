package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.util.MutableBoolean;

import odk.lang.FastMath;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.events.EventHandler.Action;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.RateRule;

import analysis.dynamicsim.HierarchicalSimulator.ModelState;
import analysis.dynamicsim.HierarchicalSimulator.StringDoublePair;


public class SimulatorHybridHierarchical  extends HierarchicalSimulator {


	private static Long initializationTime = new Long(0);
	private String modelstateID;
	int numSteps;
	double relativeError, absoluteError , nextReactionStep, nextEventTime, p2;

	DiffEquations[] functions;

	public SimulatorHybridHierarchical(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			double stoichAmpValue, JFrame running, String[] interestingSpecies, int numSteps, double relError, 
			double absError, String quantityType) throws IOException, XMLStreamException 
			{

		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep,
				randomSeed, progress, printInterval, initializationTime,
				stoichAmpValue, running, interestingSpecies, quantityType);

		this.numSteps = numSteps;
		relativeError = relError;
		absoluteError = absError;
		modelstateID = "topmodel";
		functions = new DiffEquations[numSubmodels + 1];

		try 
		{
			initialize(randomSeed, 1);

		} catch (IOException e2) 
		{
			e2.printStackTrace();
		}
			}

	private void initialize(long randomSeed, int runNumber) throws IOException {

		int index = 0;
		setupSpecies(topmodel);
		setupParameters(topmodel);			
		setupConstraints(topmodel);
		setupInitialAssignments(topmodel);
		setupRules(topmodel);
		setupReactions(topmodel);
		setupEvents(topmodel);
		setupFunctionDefinition(topmodel);
		functions[index++] = new DiffEquations(new VariableState(topmodel));
		setupForOutput(randomSeed, runNumber);


		for(ModelState model : submodels.values())
		{
			setupSpecies(model);
			setupParameters(model);		
			setupConstraints(model);
			setupInitialAssignments(model);
			setupRules(model);
			setupReactions(model);	
			setupEvents(model);
			setupFunctionDefinition(model);
			functions[index++] = new DiffEquations(new VariableState(model));
			setupForOutput(randomSeed, runNumber);
		}

		setupReplacingSpecies();

		setupForOutput(randomSeed, runNumber);


		bufferedTSDWriter.write("(" + "\"" + "time" + "\"");

		for (String speciesID : topmodel.speciesIDSet) 
			if(replacements.containsKey(speciesID))
			{

				if(replacementSubModels.get(speciesID).contains("topmodel"))

					bufferedTSDWriter.write(",\"" + speciesID + "\"");
			}
			else
			{
				bufferedTSDWriter.write(",\"" + speciesID + "\"");
			}


		for (String noConstantParam : topmodel.nonconstantParameterIDSet) 
			if(replacements.containsKey(noConstantParam))
			{

				if(replacementSubModels.get(noConstantParam).contains("topmodel"))

					bufferedTSDWriter.write(",\"" + noConstantParam + "\"");
			}
			else
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
				if(replacements.containsKey(speciesID))
				{
					if(!replacementSubModels.get(speciesID).contains(model.ID))
						bufferedTSDWriter.write(",\"" + model.ID + "__" + speciesID + "\"");
				}
				else
				{
					bufferedTSDWriter.write(",\"" + model.ID + "__" + speciesID + "\"");
				}

			for (String noConstantParam : model.nonconstantParameterIDSet)
				if(replacements.containsKey(noConstantParam))
				{
					if(!replacementSubModels.get(noConstantParam).contains(model.ID))
						bufferedTSDWriter.write(",\"" + model.ID + "__" +  noConstantParam + "\"");
				}
				else
				{
					bufferedTSDWriter.write(",\"" + model.ID + "__" +  noConstantParam + "\"");
				}
			/*
			for (String compartment : model.compartmentIDSet)
				bufferedTSDWriter.write(", \"" + model.ID + "__" + compartment + "\"");
			 */

		}


		bufferedTSDWriter.write("),\n");

	}

	@Override
	protected void simulate() {
		// TODO Auto-generated method stub
		if (sbmlHasErrorsFlag)
			return;

		//SIMULATION LOOP
		currentTime = 0.0;
		
		//double printTime = printInterval;
		double printTime = 0.0;
		double nextEndTime = 0.0;
		int currSteps = 0;
		
		//double nextEventTime = handleEvents();
		
		nextEventTime = Double.POSITIVE_INFINITY;
		
		if (absoluteError == 0)
			absoluteError = 1e-9;
		
		if (relativeError == 0)
			relativeError = 1e-6;
		
		if (numSteps == 0)
			numSteps = (int)(timeLimit/printInterval);

		FirstOrderIntegrator odecalc = new HighamHall54Integrator(minTimeStep, maxTimeStep, absoluteError, relativeError);
		odecalc.addEventHandler(new EventHandlerObject(), maxTimeStep, 0, numSteps);
		
		while (currentTime <= timeLimit && !cancelFlag && constraintFlag) 
		{

			/*
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
					HashSet<String> affectedReactionSet = fireEvents(models, "reaction",  models.noRuleFlag, models.noConstraintsFlag);				

					//recalculate propensties/groups for affected reactions
					if (affectedReactionSet.size() > 0)
						updatePropensities(affectedReactionSet, models.ID);
				}
			}	
*/
			//STEP 1: generate random numbers

			double p1 = randomNumberGenerator.nextDouble();
			p2 = randomNumberGenerator.nextDouble();


			//STEP 2: calculate delta_t, the time till the next reaction execution

			double totalPropensity = getTotalPropensity();

			double tau = -FastMath.log(p1)/totalPropensity;

			nextReactionStep = currentTime + tau;
			
			nextEndTime = currentTime + maxTimeStep;
			

			
			if (nextEndTime > printTime) 
			{
				nextEndTime = printTime;
			}
			
			
			for(DiffEquations eq : functions)
			{
				if(currentTime < nextEndTime && Math.abs(currentTime - nextEndTime) > 1e-6)
				{
					odecalc.integrate(eq, currentTime, eq.state.values, nextEndTime, eq.state.values);

					fireEvent(eq, eq.state.modelstate);
					
						
				}
				else
				{
					currentTime = nextEndTime;
				}
			}
			
			updateValuesArray();
			while (currentTime >= printTime && printTime <= timeLimit) {
			{

				try {
					printToTSD(printTime);
					bufferedTSDWriter.write(",\n");
				} catch (IOException e) {
					e.printStackTrace();
				}

				currSteps++;
				printTime = (currSteps*timeLimit/numSteps);
				
				running.setTitle("Progress (" + (int)((currentTime / timeLimit) * 100.0) + "%)");
				//update progress bar			
				progress.setValue((int)((currentTime / timeLimit) * 100.0));

			}

			//updateRules();

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
	}


	private void performStochasticReaction(double p2)
	{

		String selectedReactionID = selectReaction(p2);

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
			}
		}
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
			HashSet<StringDoublePair> reactantStoichiometrySet = 
					topmodel.reactionToReactantStoichiometrySetMap.get(affectedReactionID);
			updatePropensities(topmodel, affectedReactionSet,affectedReactionID, reactantStoichiometrySet);
		}
		else
		{
			HashSet<StringDoublePair> reactantStoichiometrySet = 

					submodels.get(id).reactionToReactantStoichiometrySetMap.get(affectedReactionID);
			updatePropensities(submodels.get(id), affectedReactionSet,affectedReactionID, reactantStoichiometrySet); 
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

@Override
protected void cancel() {
	cancelFlag = true;

}

@Override
protected void clear() {
	topmodel.clear();

	for(int i = 0; i < this.numSubmodels; i++)
		submodels.clear();


	for(String key : replacements.keySet())
		replacements.put(key, initReplacementState.get(key));


}

@Override
protected void setupForNewRun(int newRun) {
	// TODO Auto-generated method stub

}

protected double handleEvents()
{
	double nextEventTime = Double.POSITIVE_INFINITY;
	if (topmodel.noEventsFlag == false)
	{
		handleEvents(topmodel, topmodel.noRuleFlag, topmodel.noConstraintsFlag);
		//step to the next event fire time if it comes before the next time step
		if (!topmodel.triggeredEventQueue.isEmpty() && topmodel.triggeredEventQueue.peek().fireTime <= nextEventTime)
			if(topmodel.triggeredEventQueue.peek().fireTime < nextEventTime)
				nextEventTime = topmodel.triggeredEventQueue.peek().fireTime;
	}

	for(ModelState models : submodels.values())
		if (models.noEventsFlag == false){
			handleEvents(models, models.noRuleFlag, models.noConstraintsFlag);
			//step to the next event fire time if it comes before the next time step
			if (!models.triggeredEventQueue.isEmpty() && models.triggeredEventQueue.peek().fireTime <= nextEventTime)
				if(models.triggeredEventQueue.peek().fireTime < nextEventTime)
					nextEventTime = models.triggeredEventQueue.peek().fireTime;
		}
	return nextEventTime;
}

private class EventHandlerObject implements EventHandler
{
	public EventHandlerObject() {}
	
	@Override
	public Action eventOccurred(double t0, double[] y, boolean t) 
	{
		return EventHandler.Action.RESET_STATE;
	}

	@Override
	public double g(double t, double[] y) {
		
		double t1 = currentTime;
		double t2 = nextEventTime;
	
		if(nextEventTime == Double.POSITIVE_INFINITY)
		{
			return 1;
		}

		else if(t <= nextEventTime)
		{
			//System.out.println(currentTime);
			
			
			return -1;
			
		}
		else if(t <= nextReactionStep)
		{
			return -0.5;
		}
		else
		{
			return 1;
		}
	}
	@Override
	public void init(double t0, double[] y, double t) 
	{
	}

	@Override
	public void resetState(double t, double[] y) {
		
		for(DiffEquations eq : functions)
		{
			
			ModelState modelstate = eq.state.modelstate;
			
			if(modelstate.noEventsFlag == true)
				continue; 
			

			currentTime = t;
			
			if(t == nextReactionStep)
				System.out.println("reaction");
			//performStochasticReaction(p2);
			HashSet<String> variables = fireEvent(eq, modelstate);
			
			
			for(String var : variables)
			{
				int index = eq.state.variableToIndexMap.get(var);
				y[index] = eq.state.values[index];
			}
		}
		
	}
	
	
	
}

private HashSet<String> fireEvent(DiffEquations eq, ModelState modelstate)
{
	HashSet<String> vars = new HashSet<String>();
	if (modelstate.noEventsFlag == false) 
	{

		//if events fired, then the affected species counts need to be updated in the values array
		int sizeBefore = modelstate.triggeredEventQueue.size();
	
		vars = fireEvents(modelstate, "variable", modelstate.noRuleFlag, modelstate.noConstraintsFlag);
	
		int sizeAfter = modelstate.triggeredEventQueue.size();

		if (sizeAfter != sizeBefore)
		{
			for (String var : vars)
			{
				int index = eq.state.variableToIndexMap.get(var);
				eq.state.values[index] = modelstate.getVariableToValue(var);	
			}
		}
		}
	return vars;
	}

private class DiffEquations implements FirstOrderDifferentialEquations
{

	VariableState state;
	public DiffEquations(VariableState state)
	{
		this.state = state;
	}

	@Override
	public void computeDerivatives(double t, double[] y, double[] currValueChanges)
			throws MaxCountExceededException, DimensionMismatchException {

		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();

		for (int i = 0; i < y.length; i++)
			state.modelstate.setvariableToValueMap(state.indexToVariableMap.get(i), y[i]);

		//calculate the current variable values
		//based on the ODE system			
		for (int i = 0; i < currValueChanges.length - 1; i++) {

			String currentVar = state.indexToVariableMap.get(i);

			if ((state.modelstate.speciesIDSet.contains(currentVar) && 
					state.modelstate.speciesToIsBoundaryConditionMap.get(currentVar) == false) &&
					(state.modelstate.variableToValueMap.contains(currentVar)) &&
					state.modelstate.variableToIsConstantMap.get(currentVar) == false) {

				currValueChanges[i] = evaluateExpressionRecursive(state.modelstate, state.dvariablesdtime[i]);
				//if (currValueChanges[i]!=0) {
				//	System.out.println(indexToVariableMap.get(i) + "= " + dvariablesdtime[i].toFormula() + "=" + currValueChanges[i]);
				//}
			}
			else currValueChanges[i] = 0;

			if (state.modelstate.variableToIsInAssignmentRuleMap != null && state.modelstate.variableToIsInAssignmentRuleMap.containsKey(currentVar) &&
					state.modelstate.variableToValueMap.contains(currentVar) && 
					state.modelstate.variableToIsInAssignmentRuleMap.get(currentVar) == true)
				affectedAssignmentRuleSet.addAll(state.modelstate.variableToAffectedAssignmentRuleSetMap.get(currentVar));

			//				if (variableToIsInConstraintMap.get(speciesID) == true)
			//					affectedConstraintSet.addAll(variableToAffectedConstraintSetMap.get(speciesID));
		}
		currValueChanges[currValueChanges.length - 1] = t;
		state.modelstate.setvariableToValueMap("time", t);
		//updatePropensities(performRateRules(topmodel, currentTime), "topmodel");
		performRateRules(state.modelstate, state.variableToIndexMap, currValueChanges);
		
	
					
		//if assignment rules are performed, these changes need to be reflected in the currValueChanges
		//that get passed back
		if (affectedAssignmentRuleSet.size() > 0) {

			HashSet<String> affectedVariables = performAssignmentRules(state.modelstate, affectedAssignmentRuleSet);

			for (String affectedVariable : affectedVariables) {

				int index = state.variableToIndexMap.get(affectedVariable);
				currValueChanges[index] = state.modelstate.getVariableToValue(affectedVariable) - y[index];
			}
		}
		
	
		

		nextEventTime = handleEvents();
	

		currentTime = t;
	}



	@Override
	public int getDimension() {

		return state.values.length;
	}

	/**
	 * performs every rate rule using the current time step
	 * 
	 * @param delta_t
	 * @return
	 */
	protected HashSet<String> performRateRules(ModelState modelstate, HashMap<String, Integer> variableToIndexMap, double[] currValueChanges) {

		HashSet<String> affectedVariables = new HashSet<String>();

		for (RateRule rateRule : modelstate.rateRulesList) {

			String variable = rateRule.getVariable();

			//update the species count (but only if the species isn't constant) (bound cond is fine)
			if (modelstate.variableToIsConstantMap.containsKey(variable) && modelstate.variableToIsConstantMap.get(variable) == false) {

				if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(variable) &&
						modelstate.speciesToHasOnlySubstanceUnitsMap.get(variable) == false) 
				{
					if(!variableToIndexMap.containsKey(variable))
						continue;
					int index = variableToIndexMap.get(variable);

					if(index > currValueChanges.length)
						continue;

					double value = (evaluateExpressionRecursive(modelstate, rateRule.getMath()) *
							modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(variable)));
					currValueChanges[index] = value;
					//modelstate.setvariableToValueMap(variable, value);

				}
				else {
					if(!variableToIndexMap.containsKey(variable))
						continue;
					int index = variableToIndexMap.get(variable);
					if(index > currValueChanges.length)
						continue;
					double value = evaluateExpressionRecursive(modelstate, rateRule.getMath());
					currValueChanges[index] = value;
					//modelstate.setvariableToValueMap(variable, value);
				}

				affectedVariables.add(variable);
			}
		}

		return affectedVariables;
	}

}
private void updateValuesArray()
{
	int index = 0;

	//convert variableToValueMap into two arrays
	//and create a hashmap to find indices
	for(DiffEquations eq : functions)
	{
		for (String variable : eq.state.modelstate.variableToValueMap.keySet()) 
		{
			eq.state.values[index] = eq.state.modelstate.getVariableToValue(variable);
			++index;
		}

		index = 0;
	}
}



protected class VariableState
{
	ModelState modelstate;
	String[] variables;
	double[] values;
	ASTNode[] dvariablesdtime;
	HashMap<String, Integer> variableToIndexMap;
	HashMap<Integer, String> indexToVariableMap;
	MutableBoolean eventsFlag = new MutableBoolean(false);
	MutableBoolean rulesFlag = new MutableBoolean(false);
	MutableBoolean constraintsFlag = new MutableBoolean(false);
	
	protected VariableState(ModelState modelstate)
	{
		this.modelstate = modelstate;
		int size = modelstate.variableToValueMap.size() + 1;
		variables = new String[size];
		values = new double[size];
		dvariablesdtime = new ASTNode[size];
		variableToIndexMap = new HashMap<String, Integer>(size);
		indexToVariableMap = new HashMap<Integer, String>(size);
		
		int index = 0;

		//convert variableToValueMap into two arrays
		//and create a hashmap to find indices
		for (String variable : modelstate.variableToValueMap.keySet()) 
		{
			variables[index] = variable;
			values[index] = modelstate.getVariableToValue(variable);
			variableToIndexMap.put(variable, index);
			dvariablesdtime[index] = new ASTNode();
			dvariablesdtime[index].setValue(0);
			indexToVariableMap.put(index, variable);
			++index;
		}
		

		indexToVariableMap.put(index, "time");

		variableToIndexMap.put("time", index);
		variables[index] = "time";
		values[index] = 0;
		modelstate.variableToValueMap.put("time", 0);
		//create system of ODEs for the change in variables
		for (String reaction : modelstate.reactionToFormulaMap.keySet()) 
		{

			ASTNode formula = modelstate.reactionToFormulaMap.get(reaction);
			//System.out.println("HERE: " + formula.toFormula());
			HashSet<StringDoublePair> reactantAndStoichiometrySet = modelstate.reactionToReactantStoichiometrySetMap.get(reaction);
			HashSet<StringDoublePair> speciesAndStoichiometrySet = modelstate.reactionToSpeciesAndStoichiometrySetMap.get(reaction);
			

			//loop through reactants
			for (StringDoublePair reactantAndStoichiometry : reactantAndStoichiometrySet) {

				String reactant = reactantAndStoichiometry.string;
				double stoichiometry = reactantAndStoichiometry.doub;		
				int varIndex = variableToIndexMap.get(reactant);
				ASTNode stoichNode = new ASTNode();
				stoichNode.setValue(-1 * stoichiometry);
				dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula,stoichNode));
			}
			//loop through products
			for (StringDoublePair speciesAndStoichiometry : speciesAndStoichiometrySet) {

				String species = speciesAndStoichiometry.string;
				double stoichiometry = speciesAndStoichiometry.doub;

				//if it's a product its stoichiometry will be positive
				//(and if it's a reactant it'll be negative)
				if (stoichiometry > 0) 
				{

					int varIndex = variableToIndexMap.get(species);
					ASTNode stoichNode = new ASTNode();
					stoichNode.setValue(stoichiometry);

					//System.out.println("HERE2: " +  ASTNode.times(formula,stoichNode).toFormula());
					dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula,stoichNode));

				}
			}

		}

	}
}
}

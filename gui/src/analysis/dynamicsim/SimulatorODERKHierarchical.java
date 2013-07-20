package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.Gui;
import main.util.MutableBoolean;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;

import analysis.dynamicsim.HierarchicalSimulator.ModelState;

import flanagan.integration.DerivnFunction;
import flanagan.integration.RungeKutta;


public class SimulatorODERKHierarchical  extends HierarchicalSimulator{
	private static Long initializationTime = new Long(0);

	int numSteps;
	DerivnFunc[] functions;
	double relativeError;
	double absoluteError;

	public SimulatorODERKHierarchical(String SBMLFileName, String outputDirectory, double timeLimit, double maxTimeStep, long randomSeed,
			JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, int numSteps, double relError, double absError, String quantityType) 
					throws IOException, XMLStreamException {


		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, 0.0, randomSeed,
				progress, printInterval, initializationTime, stoichAmpValue, running,
				interestingSpecies, quantityType);


		this.numSteps = numSteps;
		relativeError = relError;
		absoluteError = absError;
		functions = new DerivnFunc[numSubmodels + 1];
		
		try {
			initialize(randomSeed, 1);
			
		} catch (IOException e2) {
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
		functions[index++] = new DerivnFunc(new VariableState(topmodel));
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
			functions[index++] = new DerivnFunc(new VariableState(model));
			setupForOutput(randomSeed, runNumber);
		}

		setupReplacingSpecies();

		setupForOutput(randomSeed, runNumber);

		bufferedTSDWriter.write("(" + "\"" + "time" + "\"");
		
		
		for(DerivnFunc der : functions)
		{
			ModelState model = der.state.modelstate;
			for (String speciesID : model.speciesIDSet) 				
				bufferedTSDWriter.write(", \"" + speciesID + "_" + model.ID + "\"");
			for (String noConstantParam : model.nonconstantParameterIDSet) 				
				bufferedTSDWriter.write(", \"" + noConstantParam + "_" + model.ID + "\"");
			for (String compartment : model.compartmentIDSet)
				bufferedTSDWriter.write(", \"" + compartment +  "_" + model.ID + "\"");
			
		}		

		
		bufferedTSDWriter.write("),\n");
		
	}
	
@Override
protected void simulate() {

	int currSteps = 0;

	if (sbmlHasErrorsFlag == true)
		return;

	double printTime = -0.0001;
	double stepSize = 0.0001;
	double nextEndTime = 0.0;
	currentTime = 0.0;

	if (absoluteError == 0)
		absoluteError = 1e-9;
	
	if (relativeError == 0)
		relativeError = 1e-6;
	
	if (stepSize > Double.MAX_VALUE)
		stepSize = 0.01;
	
	if (numSteps == 0)
		numSteps = (int)(timeLimit/printInterval);

	//create runge-kutta instance
	RungeKutta rungeKutta = new RungeKutta();
	rungeKutta.setStepSize(stepSize);

	//absolute error
	rungeKutta.setToleranceAdditionFactor(absoluteError);
	//relative error
	rungeKutta.setToleranceScalingFactor(relativeError);
	//rungeKutta.setMaximumIterations(numSteps);

	//add events to queue if they trigger
	handleEvents();

	while (currentTime < timeLimit && !cancelFlag && constraintFlag) 
	{

		for(DerivnFunc der : functions)
		{
		//EVENT HANDLING
		//trigger and/or fire events, etc.
			
		ModelState modelstate = der.state.modelstate;
		
		if (modelstate.noEventsFlag == false) {

			//if events fired, then the affected species counts need to be updated in the values array
			int sizeBefore = modelstate.triggeredEventQueue.size();
			fireEvents(modelstate, modelstate.noRuleFlag, modelstate.noConstraintsFlag);
			int sizeAfter = modelstate.triggeredEventQueue.size();

			if (sizeAfter != sizeBefore)
				for (int i = 0; i < der.state.values.length; ++i)
					der.state.values[i] = modelstate.getVariableToValue(der.state.indexToVariableMap.get(i));
		}
		

		//prints the initial (time == 0) data				
		if (printTime < 0) 
		{

			printTime = 0.0;

			try {
				printToTSD(printTime);
				bufferedTSDWriter.write(",\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			currSteps++;
			printTime = (currSteps*timeLimit/numSteps);
			//printTime += printInterval;
		}

		nextEndTime = currentTime + maxTimeStep;
		
		if (nextEndTime > printTime) 
		{
			nextEndTime = printTime;
		}

		//set rk values
		rungeKutta.setInitialValueOfX(currentTime);
		rungeKutta.setFinalValueOfX(nextEndTime);
		rungeKutta.setInitialValuesOfY(der.state.values);

		currentTime = nextEndTime;

		//STEP 2B: calculate rate rules using this time step
		/*HashSet<String> affectedVariables = performRateRules(stepSize);

		//update stuff based on the rate rules altering values
		for (String affectedVariable : affectedVariables) {

			if (variableToAffectedAssignmentRuleSetMap != null &&
					variableToAffectedAssignmentRuleSetMap.containsKey(affectedVariable))
				performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get(affectedVariable));

			if (variableToAffectedConstraintSetMap != null &&
					variableToAffectedConstraintSetMap.containsKey(affectedVariable))
				testConstraints(variableToAffectedConstraintSetMap.get(affectedVariable));

			for (int i = 0; i < values.length; ++i) {

				if (affectedVariable.equals(indexToVariableMap.get(i)))
					values[i] = variableToValueMap.get(indexToVariableMap.get(i));
			}
		}

		if (variableToIsInAssignmentRuleMap != null &&
				variableToIsInAssignmentRuleMap.containsKey("time")) {

			performAssignmentRules(variableToAffectedAssignmentRuleSetMap.get("time"));

			for (int i = 0; i < values.length; ++i)
				values[i] = variableToValueMap.get(indexToVariableMap.get(i));
		}
		 */
		//System.err.println(variableToValueMap);

		//call the rk algorithm
		der.state.values = rungeKutta.fehlberg(der);
		}
		
		//TSD PRINTING
		//this prints the previous timestep's data				
		while ((currentTime >= printTime) && (printTime <= timeLimit)) {

			try {
				printToTSD(printTime);

				if (printTime < timeLimit)
					bufferedTSDWriter.write(",\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			currSteps++;
			printTime = (currSteps*timeLimit/numSteps);
			//printTime += printInterval;
			running.setTitle("Progress (" + (int)((currentTime / timeLimit) * 100.0) + "%)");
		}

		//update progress bar			
		progress.setValue((int)((printTime / timeLimit) * 100.0));

		for(DerivnFunc der : functions)
		{
		//EVENT HANDLING
		//trigger and/or fire events, etc.
			
		ModelState modelstate = der.state.modelstate;
		//add events to queue if they trigger
		if (modelstate.noEventsFlag == false) {

			handleEvents();

			//step to the next event fire time if it comes before the next time step
			if (!topmodel.triggeredEventQueue.isEmpty() && topmodel.triggeredEventQueue.peek().fireTime <= currentTime)
				currentTime = topmodel.triggeredEventQueue.peek().fireTime;
		}

		updateValuesArray();
	} //end simulation loop

	
	}
	
	try {
		bufferedTSDWriter.write(')');
		bufferedTSDWriter.flush();
	}
	catch (IOException e) {
		e.printStackTrace();
	}
	
}

@Override
protected void cancel() {
	// TODO Auto-generated method stub
	cancelFlag = true;

}

@Override
protected void clear() {
	// TODO Auto-generated method stub

}

@Override
protected void setupForNewRun(int newRun) {
	// TODO Auto-generated method stub

}


private void updateValuesArray()
{
	int index = 0;

	//convert variableToValueMap into two arrays
	//and create a hashmap to find indices
	for(DerivnFunc der : functions)
	{
		for (String variable : der.state.modelstate.variableToValueMap.keySet()) 
		{
			der.state.values[index] = der.state.modelstate.getVariableToValue(variable);
			++index;
		}
		
		index = 0;
	}
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

private class DerivnFunc implements DerivnFunction {

	VariableState state;
	
	public DerivnFunc(VariableState state)
	{
		this.state = state;
	}
	
	
	/**
	 * in this context, x is the time and y is the values array
	 * this method is called by the rk algorithm and returns the
	 * evaluated derivatives of the ODE system
	 * it needs to return the changes in values for y
	 * (ie, its length is the same)
	 */
	public double[] derivn(double x, double[] y) {

		double[] currValueChanges = new double[y.length];
		HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();

		for (int i = 0; i < y.length; ++i)
			state.modelstate.setvariableToValueMap(state.indexToVariableMap.get(i), y[i]);

		//calculate the current variable values
		//based on the ODE system			
		for (int i = 0; i < currValueChanges.length; ++i) {

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

		//if assignment rules are performed, these changes need to be reflected in the currValueChanges
		//that get passed back
		if (affectedAssignmentRuleSet.size() > 0) {

			HashSet<String> affectedVariables = performAssignmentRules(state.modelstate, affectedAssignmentRuleSet);

			for (String affectedVariable : affectedVariables) {

				int index = state.variableToIndexMap.get(affectedVariable);
				currValueChanges[index] = state.modelstate.getVariableToValue(affectedVariable) - y[index];
			}
		}

		//			for (int i = 0; i < currValueChanges.length; ++i)
		//				System.err.println(currValueChanges[i]);

		return currValueChanges;
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

		variables = new String[modelstate.variableToValueMap.size()];
		values = new double[modelstate.variableToValueMap.size()];
		dvariablesdtime = new ASTNode[modelstate.variableToValueMap.size()];
		variableToIndexMap = new HashMap<String, Integer>(modelstate.variableToValueMap.size());
		indexToVariableMap = new HashMap<Integer, String>(modelstate.variableToValueMap.size());
		
		int index = 0;

		//convert variableToValueMap into two arrays
		//and create a hashmap to find indices
		for (String variable : modelstate.variableToValueMap.keySet()) {

			variables[index] = variable;
			values[index] = modelstate.getVariableToValue(variable);
			variableToIndexMap.put(variable, index);
			dvariablesdtime[index] = new ASTNode();
			dvariablesdtime[index].setValue(0);
			indexToVariableMap.put(index, variable);
			++index;
		}

		//create system of ODEs for the change in variables
		for (String reaction : modelstate.reactionToFormulaMap.keySet()) {

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
				if (stoichiometry > 0) {

					int varIndex = variableToIndexMap.get(species);
					ASTNode stoichNode = new ASTNode();
					stoichNode.setValue(stoichiometry);

					dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula,stoichNode));
				}
			}			
		}

	}
}
}

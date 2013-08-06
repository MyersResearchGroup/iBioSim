package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.util.MutableBoolean;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.RateRule;

import analysis.dynamicsim.HierarchicalSimulator.ModelState;
import analysis.dynamicsim.HierarchicalSimulator.StringDoublePair;



public class SimulatorODERKHierarchical2  extends HierarchicalSimulator{
	
	private static Long initializationTime = new Long(0);
	int numSteps;
	double relativeError;
	double absoluteError;
	DiffEquations[] functions;
	
	public SimulatorODERKHierarchical2(String SBMLFileName, String outputDirectory, double timeLimit, double maxTimeStep, long randomSeed,
			JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, int numSteps, double relError, double absError, String quantityType) 
					throws IOException, XMLStreamException {


		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, 0.0, randomSeed,
				progress, printInterval, initializationTime, stoichAmpValue, running,
				interestingSpecies, quantityType);


		this.numSteps = numSteps;
		relativeError = relError;
		absoluteError = absError;
		functions = new DiffEquations[numSubmodels + 1];
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
				
				bufferedTSDWriter.write(", \"" + speciesID + "\"");
			}
			else
			{
				bufferedTSDWriter.write(", \"" + speciesID + "\"");
			}
				
		
		for (String noConstantParam : topmodel.nonconstantParameterIDSet) 
			if(replacements.containsKey(noConstantParam))
			{

				if(replacementSubModels.get(noConstantParam).contains("topmodel"))
				
					bufferedTSDWriter.write(", \"" + noConstantParam + "\"");
			}
			else
			{
				bufferedTSDWriter.write(", \"" + noConstantParam + "\"");
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
						bufferedTSDWriter.write(", \"" + model.ID + "__" + speciesID + "\"");
				}
				else
				{
					bufferedTSDWriter.write(", \"" + model.ID + "__" + speciesID + "\"");
				}
			
			for (String noConstantParam : model.nonconstantParameterIDSet)
				if(replacements.containsKey(noConstantParam))
				{
					if(!replacementSubModels.get(noConstantParam).contains(model.ID))
						bufferedTSDWriter.write(", \"" + model.ID + "__" +  noConstantParam + "\"");
				}
				else
				{
				bufferedTSDWriter.write(", \"" + model.ID + "__" +  noConstantParam + "\"");
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
		int currSteps = 0;

		if (sbmlHasErrorsFlag == true)
			return;

		double nextEndTime = 0.0;
		
		//SIMULATION LOOP
		//currentTime = printInterval;
		currentTime = 0;
		double printTime = 0;

		if (absoluteError == 0)
			absoluteError = 1e-9;
		
		if (relativeError == 0)
			relativeError = 1e-6;
		
		//if (stepSize > Double.MAX_VALUE)
			//stepSize = 0.01;
		
		if (numSteps == 0)
			numSteps = (int)(timeLimit/printInterval);
		
		HighamHall54Integrator odecalc = new HighamHall54Integrator(0, maxTimeStep, relativeError, absoluteError);
		//odecalc.setMaxEvaluations(numSteps);
		//add events to queue if they trigger
		//odecalc.addEventHandler(handler, maxCheckInterval, convergence, maxIterationCount);
		double nextEventTime;
		
		while (currentTime <= timeLimit && !cancelFlag && constraintFlag) 
		{

			for(DiffEquations eq : functions)
			{
			//EVENT HANDLING
			//trigger and/or fire events, etc.
				
			ModelState modelstate = eq.state.modelstate;

			nextEventTime = handleEvents();
			
			fireEvent(eq, modelstate);

			if(currentTime == 0)
			{
				for(String currentVar : eq.state.modelstate.speciesIDSet)
				{
					HashSet<String> affectedVariables = new HashSet<String>();

					HashSet<AssignmentRule> affectedAssignmentRuleSet = new HashSet<AssignmentRule>();
					if (eq.state.modelstate.variableToIsInAssignmentRuleMap != null && eq.state.modelstate.variableToIsInAssignmentRuleMap.containsKey(currentVar) &&
						eq.state.modelstate.variableToValueMap.contains(currentVar) && 
						eq.state.modelstate.variableToIsInAssignmentRuleMap.get(currentVar) == true)
					
						affectedAssignmentRuleSet.addAll(eq.state.modelstate.variableToAffectedAssignmentRuleSetMap.get(currentVar));
					
					if (affectedAssignmentRuleSet.size() > 0) {

						affectedVariables = performAssignmentRules(eq.state.modelstate, affectedAssignmentRuleSet);

						for (String affectedVariable : affectedVariables) {

							int index = eq.state.variableToIndexMap.get(affectedVariable);
							eq.state.values[index] = eq.state.modelstate.getVariableToValue(affectedVariable) - eq.state.values[index];
						}
					}
				}
				
				
			}
			
			nextEndTime = currentTime + maxTimeStep;
			
			if (nextEndTime > nextEventTime) 
			{
				nextEndTime = nextEventTime;
			}
			
			
			if (nextEndTime > printTime) 
			{
				nextEndTime = printTime;
			}
			
			
			if(currentTime < nextEndTime && Math.abs(currentTime - nextEndTime) > 1e-3)
			{
				double[] temp = new double[eq.state.values.length];
				odecalc.integrate(eq, currentTime, eq.state.values, nextEndTime, temp);
				eq.state.values = temp;
			}
			
			
			updateValuesArray();
			
			
			
			//TSD PRINTING
			//this prints the previous timestep's data				
			while (currentTime >= printTime && printTime <= timeLimit) {
			
				try {
					printToTSD(printTime);
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

			currentTime = nextEndTime;

		/*	
			for(DiffEquations eq : functions)
			{
			//EVENT HANDLING
			//trigger and/or fire events, etc.
				
			ModelState modelstate = eq.state.modelstate;
			

			//add events to queue if they trigger
			if (modelstate.noEventsFlag == false) {

				handleEvents();

				//step to the next event fire time if it comes before the next time step
				if (!topmodel.triggeredEventQueue.isEmpty() && topmodel.triggeredEventQueue.peek().fireTime <= currentTime)
					currentTime = topmodel.triggeredEventQueue.peek().fireTime;
			}

			updateValuesArray();

		} 
			*/
		//end simulation loop

		
		}
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

	
		
	private boolean fireEvent(DiffEquations eq, ModelState modelstate)
	{
		if (modelstate.noEventsFlag == false) 
		{

			//if events fired, then the affected species counts need to be updated in the values array
			int sizeBefore = modelstate.triggeredEventQueue.size();
		
			fireEvents(modelstate, modelstate.noRuleFlag, modelstate.noConstraintsFlag);
		
			int sizeAfter = modelstate.triggeredEventQueue.size();

			if (sizeAfter != sizeBefore)
			{
				for (int i = 0; i < eq.state.values.length; ++i)
					eq.state.values[i] = modelstate.getVariableToValue(eq.state.indexToVariableMap.get(i));
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void setupForNewRun(int newRun) {
		// TODO Auto-generated method stub
		
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
			for (int i = 0; i < currValueChanges.length; i++) {

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
			/*
			for (Rule rule : modelstate.model.getListOfRules()) {
				
				if (rule.isRate()) {
					
					RateRule rateRule = (RateRule) rule;			
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
			}
			*/
			
			for (RateRule rateRule : modelstate.rateRulesList) {
					
					String variable = rateRule.getVariable();
					ASTNode formula = inlineFormula(modelstate, rateRule.getMath());
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
							
							double value = (evaluateExpressionRecursive(modelstate, formula) *
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

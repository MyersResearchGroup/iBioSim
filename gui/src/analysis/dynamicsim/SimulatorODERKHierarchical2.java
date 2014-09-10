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
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.RateRule;



public class SimulatorODERKHierarchical2  extends HierarchicalSimulator{

	//private static Long initializationTime = new Long(0);
	int numSteps;
	double relativeError;
	double absoluteError;
	double eventOccurred;
	double nextEventTime;
	double nextTriggerTime;
	DiffEquations[] functions;

	public SimulatorODERKHierarchical2(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, double maxTimeStep, long randomSeed,
			JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, int numSteps, double relError, double absError, String quantityType)  throws IOException, XMLStreamException {	


		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, 0.0, progress,
				printInterval, stoichAmpValue, running, interestingSpecies, quantityType);


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
		setupNonConstantSpeciesReferences(topmodel);
		setupSpecies(topmodel);
		setupParameters(topmodel);	

		setupConstraints(topmodel);
		setupRules(topmodel);
		setupInitialAssignments(topmodel);


		setupReactions(topmodel);
		setupEvents(topmodel);

		functions[index++] = new DiffEquations(new VariableState(topmodel));
		setupForOutput(randomSeed, runNumber);


		for(ModelState model : submodels.values())
		{
			setupNonConstantSpeciesReferences(model);
			setupSpecies(model);
			setupParameters(model);	

			setupConstraints(model);
			setupRules(model);
			setupInitialAssignments(model);


			setupReactions(model);	
			setupEvents(model);
			functions[index++] = new DiffEquations(new VariableState(model));
			setupForOutput(randomSeed, runNumber);
		}

		setupReplacingSpecies();

		setupForOutput(randomSeed, runNumber);


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

	@Override
	protected void simulate() {
		int currSteps = 0;

		if (sbmlHasErrorsFlag == true)
			return;

		double nextEndTime = 0.0;

		//SIMULATION LOOP
		currentTime = 0;
		double printTime = 0;

		if (absoluteError == 0)
			absoluteError = 1e-9;

		if (relativeError == 0)
			relativeError = 1e-6;

		if (numSteps == 0)
			numSteps = (int)(timeLimit/printInterval);



		HighamHall54Integrator odecalc = new HighamHall54Integrator(minTimeStep, maxTimeStep, absoluteError, relativeError);
		//FirstOrderIntegrator odecalc = new DormandPrince853Integrator(minTimeStep, maxTimeStep, relativeError, absoluteError);

		//odecalc.setMaxEvaluations(numSteps);
		//add events to queue if they trigger
		odecalc.addEventHandler(new EventHandlerObject(), maxTimeStep, 1e-18, 1000000);
		//nextEventTime = Double.POSITIVE_INFINITY;

		nextEventTime = handleEvents();

		for(DiffEquations eq : functions)
		{
			ModelState modelstate = eq.state.modelstate;
			fireEvent(eq, modelstate);

		}

		double initStepTime = 0;
		while (currentTime <= timeLimit && !cancelFlag && constraintFlag) 
		{
			initStepTime = currentTime;

			for(DiffEquations eq : functions)
			{
				//EVENT HANDLING
				//trigger and/or fire events, etc.

				//ModelState modelstate = eq.state.modelstate;

				//nextEventTime = handleEvents();

				currentTime = initStepTime;

				nextEndTime = currentTime + maxTimeStep;



				if (nextEndTime > printTime) 
				{
					nextEndTime = printTime;
				}

				//fireEvent(eq, modelstate);
				nextEventTime = Double.POSITIVE_INFINITY;
				nextTriggerTime = Double.POSITIVE_INFINITY;

				if(currentTime < nextEndTime && Math.abs(currentTime - nextEndTime) > 1e-6)
				{
					odecalc.integrate(eq, currentTime, eq.state.values, nextEndTime, eq.state.values);
					//handleEvents();
					//fireEvent(eq, modelstate);


				}



				updateValuesArray(eq.state.modelstate);
			}

			updateValuesArray();
			currentTime = nextEndTime;



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

			//end simulation loop



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
			for (String variable : eq.state.variables) 
			{
				index = eq.state.variableToIndexMap.get(variable);
				eq.state.values[index] = eq.state.modelstate.getVariableToValue(variable);
			}
		}
	}

	private void updateValuesArray(ModelState modelstate)
	{
		int index = 0;

		//convert variableToValueMap into two arrays
		//and create a hashmap to find indices
		for(DiffEquations eq : functions)
		{
			if(eq.state.modelstate.ID.equals(modelstate.ID))
				continue;

			for (String variable : eq.state.modelstate.isHierarchical) 
			{
				if(eq.state.variableToIndexMap.containsKey(variable)){
					index = eq.state.variableToIndexMap.get(variable);
					eq.state.values[index] = eq.state.modelstate.getVariableToValue(variable);
				}
			}
		}
	}

	protected double handleEvents()
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (topmodel.noEventsFlag == false)
		{
			handleEvents(topmodel);
			//step to the next event fire time if it comes before the next time step
			if (!topmodel.triggeredEventQueue.isEmpty() && topmodel.triggeredEventQueue.peek().fireTime <= nextEventTime)
				if(topmodel.triggeredEventQueue.peek().fireTime < nextEventTime)
				{
					nextEventTime = topmodel.triggeredEventQueue.peek().fireTime;
				}
		}

		for(ModelState models : submodels.values())
			if (models.noEventsFlag == false){
				handleEvents(models);
				//step to the next event fire time if it comes before the next time step
				if (!models.triggeredEventQueue.isEmpty() && models.triggeredEventQueue.peek().fireTime <= nextEventTime)
					if(models.triggeredEventQueue.peek().fireTime < nextEventTime)
					{
						nextEventTime = models.triggeredEventQueue.peek().fireTime;
					}
			}
		return nextEventTime;
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
					if (modelstate.speciesToHasOnlySubstanceUnitsMap.containsKey(var) &&
							modelstate.speciesToHasOnlySubstanceUnitsMap.get(var) == false)
						eq.state.values[index] = modelstate.getVariableToValue(var) * 
						modelstate.variableToValueMap.get(modelstate.speciesToCompartmentNameMap.get(var));
					else
						eq.state.values[index] = modelstate.getVariableToValue(var);	
				}
			}
		}
		return vars;
	}



	@Override
	protected void clear() {
		// TODO Auto-generated method stub

	}
	@Override
	protected void setupForNewRun(int newRun) {


	}

	private class EventHandlerObject implements EventHandler
	{
		//double t0, t1;
		public EventHandlerObject() {}

		@Override
		public Action eventOccurred(double t, double[] y, boolean increasing) 
		{

			currentTime = t;
			nextTriggerTime = t;
			nextEventTime = handleEvents();
			return EventHandler.Action.RESET_STATE;
		}

		@Override
		public double g(double t, double[] y) {

			//double t1 = currentTime;
			//double t2 = nextEventTime;

			for(DiffEquations eq : functions)
				if(isEventTriggered(eq.state.modelstate, t, y, eq.state.variableToIndexMap))
				{
					//System.out.println("Time: " + t + " triggered " + y[1]);
					return -1;
				}

			//System.out.println("Time: " + t + " NOT triggered " + y[1]);

			if(nextEventTime == t)
				return -1;
			else if(nextTriggerTime == t)
				return -1;

			return 1;
		}

		@Override
		public void init(double t0, double[] y, double t) 
		{
			//this.t0 = t0;
			//this.t1 = t;
		}

		@Override
		public void resetState(double t, double[] y) {


			for(DiffEquations eq : functions)
			{

				ModelState modelstate = eq.state.modelstate;

				if(modelstate.noEventsFlag == true)
					continue; 


				HashSet<String> variables = fireEvent(eq, modelstate);


				for(String var : variables)
				{
					int index = eq.state.variableToIndexMap.get(var);
					y[index] = eq.state.values[index];
				}

			}

		}



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

			// Needs to reevaluate some variables that is affected by a nonconstant stoichiometry
			HashSet<String> revaluateVariables = new HashSet<String>();

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
				{
					affectedAssignmentRuleSet.addAll(state.modelstate.variableToAffectedAssignmentRuleSetMap.get(currentVar));
					revaluateVariables.add(currentVar);
				}
				//				if (variableToIsInConstraintMap.get(speciesID) == true)
				//					affectedConstraintSet.addAll(variableToAffectedConstraintSetMap.get(speciesID));
			}
			currValueChanges[currValueChanges.length - 1] = t;
			state.modelstate.setvariableToValueMap("time", t);
			//updatePropensities(performRateRules(topmodel, currentTime), "topmodel");
			revaluateVariables.addAll(performRateRules(state.modelstate, state.variableToIndexMap, currValueChanges));



			//if assignment rules are performed, these changes need to be reflected in the currValueChanges
			//that get passed back
			if (affectedAssignmentRuleSet.size() > 0) {

				HashSet<String> affectedVariables = performAssignmentRules(state.modelstate, affectedAssignmentRuleSet);

				for (String affectedVariable : affectedVariables) 
				{

					int index = state.variableToIndexMap.get(affectedVariable);
					currValueChanges[index] = state.modelstate.getVariableToValue(affectedVariable) - y[index];
					revaluateVariables.add(affectedVariable);

				}
				/*
				//for (String variable : revaluateVariables) {
				//int i = state.variableToIndexMap.get(variable);
				for (int i = 0; i < currValueChanges.length - 1; i++) {

					String variable = state.indexToVariableMap.get(i);

					if ((state.modelstate.speciesIDSet.contains(variable) && 
							state.modelstate.speciesToIsBoundaryConditionMap.get(variable) == false) &&
							(state.modelstate.variableToValueMap.contains(variable)) &&
							state.modelstate.variableToIsConstantMap.get(variable) == false) {

						currValueChanges[i] = evaluateExpressionRecursive(state.modelstate, state.dvariablesdtime[i]);
					}
				}
				 */
			}



			currentTime = t;

			//nextEventTime = handleEvents();


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

						//double oldValue = modelstate.getVariableToValue(variable);
						double value = (evaluateExpressionRecursive(modelstate, formula) *
								modelstate.getVariableToValue(modelstate.speciesToCompartmentNameMap.get(variable)));
						currValueChanges[index] = value;
						//modelstate.setvariableToValueMap(variable, oldValue + value);

					}
					else {
						if(!variableToIndexMap.containsKey(variable))
							continue;
						int index = variableToIndexMap.get(variable);
						if(index > currValueChanges.length)
							continue;
						//double oldValue = modelstate.getVariableToValue(variable);
						double value = evaluateExpressionRecursive(modelstate, formula);
						currValueChanges[index] = value;
						//modelstate.setvariableToValueMap(variable, oldValue + value);
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
				HashSet<StringStringPair> nonConstantStoichiometrySet = modelstate.reactionToNonconstantStoichiometriesSetMap.get(reaction);
				//loop through reactants
				if(reactantAndStoichiometrySet != null)
					for (StringDoublePair reactantAndStoichiometry : reactantAndStoichiometrySet) {

						String reactant = reactantAndStoichiometry.string;
						double stoichiometry = reactantAndStoichiometry.doub;		
						int varIndex = variableToIndexMap.get(reactant);
						ASTNode stoichNode = new ASTNode();
						stoichNode.setValue(-1 * stoichiometry);
						dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula,stoichNode));
					}


				//loop through products
				if(speciesAndStoichiometrySet!=null)
					for (StringDoublePair speciesAndStoichiometry : speciesAndStoichiometrySet) 
					{

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

				if(nonConstantStoichiometrySet != null)
					for (StringStringPair reactantAndStoichiometry : nonConstantStoichiometrySet) {

						String reactant = reactantAndStoichiometry.string1;
						String stoichiometry = reactantAndStoichiometry.string2;
						int varIndex = variableToIndexMap.get(reactant);
						if(stoichiometry.startsWith("-"))
						{
							ASTNode stoichNode = new ASTNode(stoichiometry.substring(1, stoichiometry.length()));
							dvariablesdtime[varIndex] = ASTNode.diff(dvariablesdtime[varIndex], ASTNode.times(formula,stoichNode));
						}
						else
						{
							ASTNode stoichNode = new ASTNode(stoichiometry);
							dvariablesdtime[varIndex] = ASTNode.sum(dvariablesdtime[varIndex], ASTNode.times(formula,stoichNode));
						}
					}
			}

		}
	}

}

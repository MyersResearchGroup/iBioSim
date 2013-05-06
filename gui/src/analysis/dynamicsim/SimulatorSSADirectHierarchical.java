package analysis.dynamicsim;

import java.io.IOException;
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
	private int submodelIndex;
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
			 
			double delta_t = FastMath.log(1 / r1) / getTotalPropensity();
		
			if (delta_t > maxTimeStep)
				delta_t = maxTimeStep;
			
			if (delta_t < minTimeStep)
				delta_t = minTimeStep;
			
			
			//STEP 3: select a reaction
			
			String selectedReactionID = selectReaction(r2);
			
			//if its length isn't positive then there aren't any reactions
			if (!selectedReactionID.isEmpty()) {
			
			//STEP 4: perform selected reaction and update species counts
				
				if(submodelIndex == -1)
				{
					topmodel.performReaction(selectedReactionID, true, true);
					HashSet<String> affectedReactionSet = topmodel.getAffectedReactionSet(selectedReactionID, true);
					
					//STEP 5: compute affected reactions' new propensities and update total propensity
					updatePropensities(affectedReactionSet);
				}
				else
				{
					submodels[submodelIndex].performReaction(selectedReactionID, true, true);
					HashSet<String> affectedReactionSet = submodels[submodelIndex].getAffectedReactionSet(selectedReactionID, true);
					
					//STEP 5: compute affected reactions' new propensities and update total propensity
					updatePropensities(affectedReactionSet);
				}

			}
			
			//update time for next iteration
			currentTime += delta_t;
			
				
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
		
		
		topmodel.setupSpecies();
		topmodel.setupParameters();	
		topmodel.setupReactions();		
		
		setupForOutput(randomSeed, runNumber);
		
		for(ModelState model : submodels)
		{
		model.setupSpecies();
		model.setupParameters();	
		model.setupReactions();		
		
		setupForOutput(randomSeed, runNumber);
		
		}
		
		bufferedTSDWriter.write("(" + "\"" + "time" + "\"");
		
		for (String speciesID : topmodel.speciesIDSet) {				
			bufferedTSDWriter.write(", \"" + speciesID + "\"");
		}
		
		for(ModelState model : submodels)
		{
			for (String speciesID : model.speciesIDSet) {				
				//if(!replacements.containsKey(speciesID))
				//{
					bufferedTSDWriter.write(", \"" + speciesID + "_" + model.ID + "\"");
				//}
			}
			
		}
		
		
		bufferedTSDWriter.write("),\n");
		
	}
	
	
	/**
	 * updates the propensities of the reactions affected by the recently performed reaction
	 * @param affectedReactionSet the set of reactions affected by the recently performed reaction
	 */
	private void updatePropensities(HashSet<String> affectedReactionSet) {
		
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
				submodels[submodelIndex].reactionToReactantStoichiometrySetMap.get(affectedReactionID);
				updatePropensities(submodels[submodelIndex], affectedReactionSet,affectedReactionID, reactantStoichiometrySet); 
				
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
			
			newPropensity = model.evaluateExpressionRecursive(model.reactionToFormulaMap.get(affectedReactionID));
			//newPropensity = CalculatePropensityIterative(affectedReactionID);
		}
		
		double oldPropensity = model.reactionToPropensityMap.get(affectedReactionID);
		
		//add the difference of new v. old propensity to the total propensity
		model.propensity += newPropensity - oldPropensity;
		
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
		
		for(int i = 0; i < numModels - 1; i ++)
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
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = reader.readSBML(SBMLFileName);
		
		topmodel = new ModelState(document.getModel(), true, "");
		setupSubmodels(document);
		getComponentPortMap(document);
	}

	/**
	 * does minimized initalization process to prepare for a new run
	 */
	protected void setupForNewRun(int newRun) {

		try {
			topmodel.setupSpecies();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		topmodel.setupParameters();		
		topmodel.setupReactions();		
		
		setupForOutput(0, newRun);
		
		for(ModelState model : submodels)
		{
		try {
			model.setupSpecies();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.setupParameters();	
		model.setupReactions();		
		
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
package analysis.dynamicsim;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.util.MutableBoolean;




public class SimulatorSSADirectUnflatten extends Simulator{
	private static Long initializationTime = new Long(0);

	MutableBoolean eventsFlag = new MutableBoolean(false);
	MutableBoolean rulesFlag = new MutableBoolean(false);
	MutableBoolean constraintsFlag = new MutableBoolean(false);
	
	public SimulatorSSADirectUnflatten(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			double stoichAmpValue, JFrame running, String[] interestingSpecies, String quantityType) 
	throws IOException, XMLStreamException {
		
		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, minTimeStep, randomSeed,
				progress, printInterval, initializationTime, stoichAmpValue, running, interestingSpecies, quantityType);
		
		System.out.println("test");
	}


	@Override
	protected void cancel() {
		// TODO Auto-generated method stub
		cancelFlag = true;
		
	}

	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		variableToValueMap.clear();
		reactionToPropensityMap.clear();
		
		if (numEvents > 0) {
			
			triggeredEventQueue.clear();
			untriggeredEventSet.clear();
			eventToPriorityMap.clear();
			eventToDelayMap.clear();
		}
		
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
		
		//get rid of things that were created dynamically last run
		if (dynamicBoolean == true) {
			resetModel();
		}
	}

	@Override
	protected void eraseComponentFurther(HashSet<String> reactionIDs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setupForNewRun(int newRun) {
		// TODO Auto-generated method stub
		try {
			setupSpecies();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setupInitialAssignments();
		setupParameters();
		setupRules();
		setupConstraints();
		
		totalPropensity = 0.0;
		minPropensity = Double.MAX_VALUE;
		maxPropensity = Double.MIN_VALUE;
		
		if (numEvents == 0)
			eventsFlag.setValue(true);
		else
			eventsFlag.setValue(false);
		
		if (numAssignmentRules == 0)
			rulesFlag.setValue(true);
		else
			rulesFlag.setValue(false);
		
		if (numConstraints == 0)
			constraintsFlag.setValue(true);
		else
			constraintsFlag.setValue(false);
		
		//STEP 0A: calculate initial propensities (including the total)		
		setupReactions();		
		setupEvents();		
		setupForOutput(0, newRun);
		
		if (dynamicBoolean == true) {
			
			setupGrid();
		}
	}

	@Override
	protected void simulate() {}

	@Override
	protected void updateAfterDynamicChanges() {}

}

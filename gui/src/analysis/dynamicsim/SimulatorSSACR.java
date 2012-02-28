package analysis.dynamicsim;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.Gui;
import main.util.MutableBoolean;
import odk.lang.FastMath;

public class SimulatorSSACR extends Simulator {
	
	//allows for access to a group number from a reaction ID
	private TObjectIntHashMap<String> reactionToGroupMap = null;
	
	//allows for access to a group's min/max propensity from a group ID
	private TIntDoubleHashMap groupToMaxValueMap = null;
	
	//allows for access to the minimum/maximum possible propensity in the group from a group ID
	private TIntDoubleHashMap groupToPropensityFloorMap = null;
	private TIntDoubleHashMap groupToPropensityCeilingMap = null;
	
	//allows for access to the reactionIDs in a group from a group ID
	private ArrayList<HashSet<String> > groupToReactionSetList = null;
	
	//allows for access to the group's total propensity from a group ID
	private TIntDoubleHashMap groupToTotalGroupPropensityMap = null;
	
	//stores group numbers that are nonempty
	private TIntHashSet nonemptyGroupSet = null;
	
	//number of groups including the empty groups and zero-propensity group
	private int numGroups = 0;
	
	private static Long initializationTime = new Long(0);
	
	MutableBoolean eventsFlag = new MutableBoolean(false);
	MutableBoolean rulesFlag = new MutableBoolean(false);
	MutableBoolean constraintsFlag = new MutableBoolean(false);
	
	
	public SimulatorSSACR(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval) 
	throws IOException, XMLStreamException {
		
		super(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, randomSeed,
				progress, printInterval, initializationTime);
		
		try {
			initialize(randomSeed, 1);
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (XMLStreamException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * runs the composition and rejection simulation
	 */
	public void simulate() {
		
		if (sbmlHasErrorsFlag == true)
			return;
		
		long initTime2 = System.nanoTime();
		
		final boolean noEventsFlag = (Boolean) eventsFlag.getValue();
		final boolean noAssignmentRulesFlag = (Boolean) rulesFlag.getValue();
		final boolean noConstraintsFlag = (Boolean) constraintsFlag.getValue();
		
		initializationTime += System.nanoTime() - initTime2;
		long initTime3 = System.nanoTime() - initTime2;
		
		//System.err.println("initialization time: " + initializationTime / 1e9f);
		
		//SIMULATION LOOP
		//simulate until the time limit is reached
		
		long step1Time = 0;
		long step2Time = 0;
		long step3aTime = 0;
		long step3bTime = 0;
		long step4Time = 0;
		long step5Time = 0;
		long step6Time = 0;
		
		TObjectIntHashMap<String> reactionToTimesFired = new TObjectIntHashMap<String>();
		
		currentTime = 0.0;
		double printTime = -0.00001;
		
		//add events to queue if they trigger
		if (noEventsFlag == false)
			handleEvents(noAssignmentRulesFlag, noConstraintsFlag);
		
		while (currentTime < timeLimit && cancelFlag == false) {
			
			//if a constraint fails
			if (constraintFailureFlag == true) {
				
				JOptionPane.showMessageDialog(Gui.frame, "Simulation Canceled Due To Constraint Failure",
						"Constraint Failure", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			//EVENT HANDLING
			//trigger and/or fire events, etc.
			if (noEventsFlag == false) {
				
				HashSet<String> affectedReactionSet = fireEvents(noAssignmentRulesFlag, noConstraintsFlag);
				
				//recalculate propensties/groups for affected reactions
				if (affectedReactionSet.size() > 0) {
					
					boolean newMinPropensityFlag = updatePropensities(affectedReactionSet);
					
					if (newMinPropensityFlag == true)
						reassignAllReactionsToGroups();
					else
						updateGroups(affectedReactionSet);
				}
			}
			
			//prints the initial (time == 0) data
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
			
			//long step1Initial = System.nanoTime();
			
			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();
			double r3 = randomNumberGenerator.nextDouble();
			double r4 = randomNumberGenerator.nextDouble();
			
			//step1Time += System.nanoTime() - step1Initial;
			
			
			
			//STEP 2: calculate delta_t, the time till the next reaction execution
			
			//long step2Initial = System.nanoTime();
			 
			double delta_t = FastMath.log(1 / r1) / totalPropensity;
			
			//step2Time += System.nanoTime() - step2Initial;
			
			
			
			//STEP 3A: select a group
			
			//long step3aInitial = System.nanoTime();
			
			int selectedGroup = selectGroup(r2);
			
			//this happens when there aren't any nonempty groups
			if (selectedGroup == 0) {
				
				currentTime = printTime + printInterval/2;
				continue;
			}
		
			//step3aTime += System.nanoTime() - step3aInitial;
			
			
			
			//STEP 3B: select a reaction within the group
			
			//long step3bInitial = System.nanoTime();
			
			String selectedReactionID = selectReaction(selectedGroup, r3, r4);
			
			//step3bTime += System.nanoTime() - step3bInitial;
			
			
			
			//STEP 4: perform selected reaction and update species counts
			
			//long step4Initial = System.nanoTime();
			
			performReaction(selectedReactionID, noAssignmentRulesFlag, noConstraintsFlag);
			
			//step4Time += System.nanoTime() - step4Initial;
			
			
			
			//STEP 5: compute affected reactions' new propensities and update total propensity
			
			//long step5Initial = System.nanoTime();
			
			//create a set (precludes duplicates) of reactions that the selected reaction's species affect
			HashSet<String> affectedReactionSet = getAffectedReactionSet(selectedReactionID, noAssignmentRulesFlag);
			
			boolean newMinPropensityFlag = updatePropensities(affectedReactionSet);
			
			//step5Time += System.nanoTime() - step5Initial;
			
			
			
			//STEP 6: re-assign affected reactions to appropriate groups
			
			//long step6Initial = System.nanoTime();
			
			//if there's a new minPropensity, then the group boundaries change
			//so re-calculate all groups
			if (newMinPropensityFlag == true)
				reassignAllReactionsToGroups();
			else
				updateGroups(affectedReactionSet);
			
			//step6Time += System.nanoTime() - step6Initial;
			
			
			
			//update time for next iteration
			currentTime += delta_t;
			
			//add events to queue if they trigger
			if (noEventsFlag == false) {
				
				handleEvents(noAssignmentRulesFlag, noConstraintsFlag);
			
				if (!triggeredEventQueue.isEmpty() && (triggeredEventQueue.peek().fireTime <= currentTime))
					currentTime = triggeredEventQueue.peek().fireTime;
			}
			
			while ((currentTime > printTime) && (printTime < timeLimit)) {
				
				try {
					printToTSD(printTime);
					bufferedTSDWriter.write(",\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				printTime += printInterval;			
			}
			
		} //end simulation loop
		
		
//		System.err.println("total time: " + String.valueOf((initializationTime + System.nanoTime() - 
//				initTime2 - initTime3) / 1e9f));
//		System.err.println("total step 1 time: " + String.valueOf(step1Time / 1e9f));
//		System.err.println("total step 2 time: " + String.valueOf(step2Time / 1e9f));
//		System.err.println("total step 3a time: " + String.valueOf(step3aTime / 1e9f));
//		System.err.println("total step 3b time: " + String.valueOf(step3bTime / 1e9f));
//		System.err.println("total step 4 time: " + String.valueOf(step4Time / 1e9f));
//		System.err.println("total step 5 time: " + String.valueOf(step5Time / 1e9f));
//		System.err.println("total step 6 time: " + String.valueOf(step6Time / 1e9f));
		
		if (cancelFlag == false) {
			
			//print the final species counts
			try {
				printToTSD(printTime);
			} 
			catch (IOException e) {
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
	 * initializes data structures local to the SSA-CR method
	 * calculates initial propensities and assigns reactions to groups
	 * 
	 * @param noEventsFlag
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	private void initialize(long randomSeed, int runNumber) 
	throws IOException, XMLStreamException {
		
		reactionToGroupMap = new TObjectIntHashMap<String>((int) (numReactions * 1.5));
		groupToMaxValueMap = new TIntDoubleHashMap();
		groupToPropensityFloorMap = new TIntDoubleHashMap();
		groupToPropensityCeilingMap = new TIntDoubleHashMap();
		groupToReactionSetList = new ArrayList<HashSet<String> >();
		groupToTotalGroupPropensityMap = new TIntDoubleHashMap();
		nonemptyGroupSet = new TIntHashSet();
		
		eventsFlag = new MutableBoolean(false);
		rulesFlag = new MutableBoolean(false);
		constraintsFlag = new MutableBoolean(false);
		
		//setupArrays();
		expandArrays2();
		setupSpecies();
		setupInitialAssignments();
		setupParameters();
		setupRules();
		setupConstraints();
		
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
		
		//STEP OB: create and populate initial groups		
		createAndPopulateInitialGroups();
		
		setupEvents();		
		setupForOutput(randomSeed, runNumber);
		
		if (dynamicBoolean == true) {
			
			setupGrid();
			createModelCopy();
		}
		
		HashSet<String> comps = new HashSet<String>();
		comps.addAll(componentToLocationMap.keySet());
		
		bufferedTSDWriter.write("(" + "\"" + "time" + "\"");
		
		for (String speciesID : speciesIDSet) {
			
			bufferedTSDWriter.write(", \"" + speciesID + "\"");
		}
		
		//print compartment location IDs
		for (String componentLocationID : componentToLocationMap.keySet()) {
			
			String locationX = componentLocationID + "__locationX";
			String locationY = componentLocationID + "__locationY";
			
			bufferedTSDWriter.write(", \"" + locationX + "\", \"" + locationY + "\"");
		}
		
		bufferedTSDWriter.write("),\n");
		
//		for (String reactionID : reactionToPropensityMap.keySet()) {
//			
//			if (reactionToPropensityMap.get(reactionID) > 0) {
//			
//				System.err.println(reactionID);
//				
//				try {
//					System.err.println(ASTNode.formulaToString(reactionToFormulaMap.get(reactionID)));
//				} catch (SBMLException e) {
//					e.printStackTrace();
//				}				
//				
//				System.err.println(reactionToPropensityMap.get(reactionID));
//				
//				System.err.println();
//				System.err.println();
//			}
//		}
//		
//		for (String variableID : variableToValueMap.keySet()) {
//			
//			System.err.println(variableID);
//			System.err.println(variableToValueMap.get(variableID));
//			System.err.println();
//			System.err.println();
//		}
	}
	
	/**
	 * creates the appropriate number of groups and associates reactions with groups
	 */
	private void createAndPopulateInitialGroups() {
		
		//create groups
		int currentGroup = 1;
		double groupPropensityCeiling = 2 * minPropensity;
		
		groupToPropensityFloorMap.put(1, minPropensity);
		
		while (groupPropensityCeiling < maxPropensity) {
			
			groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
			groupToPropensityFloorMap.put(currentGroup + 1, groupPropensityCeiling);
			groupToMaxValueMap.put(currentGroup, 0.0);
			
			groupPropensityCeiling *= 2;
			++currentGroup;
		}
		
		//if there are no non-zero groups
		if (minPropensity == 0) {
			
			numGroups = 1;
			groupToReactionSetList.add(new HashSet<String>(500));
		}
		else {
			
			numGroups = currentGroup + 1;
			
			groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
			groupToMaxValueMap.put(currentGroup, 0.0);
		
			//start at 0 to make a group for zero propensities
			for (int groupNum = 0; groupNum < numGroups; ++groupNum) {
	
				groupToReactionSetList.add(new HashSet<String>(500));
				groupToTotalGroupPropensityMap.put(groupNum, 0.0);
			}
		}
		
		//assign reactions to groups
		for (String reaction : reactionToPropensityMap.keySet()) {
			
			double propensity = reactionToPropensityMap.get(reaction);			
			org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (propensity / minPropensity));
			int group = frexpResult.exponent;
			
			groupToTotalGroupPropensityMap.adjustValue(group, propensity);
			groupToReactionSetList.get(group).add(reaction);
			reactionToGroupMap.put(reaction, group);
			
			if (propensity > groupToMaxValueMap.get(group))
				groupToMaxValueMap.put(group, propensity);
		}
		
		//find out which (if any) groups are empty
		//this is done so that empty groups are never chosen during simulation
		for (int groupNum = 1; groupNum < numGroups; ++groupNum) {
			
			if (groupToReactionSetList.get(groupNum).isEmpty())
				continue;
			
			nonemptyGroupSet.add(groupNum);
		}
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
		
		variableToValueMap.clear();
		reactionToPropensityMap.clear();
		
		if (numEvents > 0) {
			
			triggeredEventQueue.clear();
			untriggeredEventSet.clear();
			eventToPriorityMap.clear();
			eventToDelayMap.clear();
		}
		
		reactionToGroupMap.clear();
		groupToMaxValueMap.clear();
		groupToPropensityFloorMap.clear();
		groupToPropensityCeilingMap.clear();
		groupToReactionSetList.clear();
		groupToTotalGroupPropensityMap.clear();
		nonemptyGroupSet.clear();
		speciesIDSet.clear();
		componentToLocationMap.clear();
		componentToReactionSetMap.clear();
		componentToVariableSetMap.clear();
		componentToEventSetMap.clear();
	}
	
	/**
	 * removes a component's reactions from reactionToGroupMap and groupToReactionSetList
	 */
	protected void eraseComponentFurther(HashSet<String> reactionIDs) {
		
		for (String reactionID : reactionIDs) {
			
			int group = reactionToGroupMap.get(reactionID);
			reactionToGroupMap.remove(reactionID);
			groupToReactionSetList.get(group).remove(reactionID);
		}
	}
	
	/**
	 * assigns all reactions to (possibly new) groups
	 * this is called when the minPropensity changes, which
	 * changes the groups' floor/ceiling propensity values
	 */
	private void reassignAllReactionsToGroups() {
		
		int currentGroup = 1;
		double groupPropensityCeiling = 2 * minPropensity;
		
		//re-calulate and store group propensity floors/ceilings
		groupToPropensityCeilingMap.clear();
		groupToPropensityFloorMap.clear();
		groupToPropensityFloorMap.put(1, minPropensity);
		
		while (groupPropensityCeiling <= maxPropensity) {
			
			groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
			groupToPropensityFloorMap.put(currentGroup + 1, groupPropensityCeiling);
			
			groupPropensityCeiling *= 2;
			++currentGroup;
		}
		
		groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
		int newNumGroups = currentGroup + 1;
		
		//allocate memory if the number of groups expands
		if (newNumGroups > numGroups) {
			
			for (int groupNum = numGroups; groupNum < newNumGroups; ++groupNum)
				groupToReactionSetList.add(new HashSet<String>(500));
		}
		
		//clear the reaction set for each group
		//start at 1, as the zero propensity group isn't going to change
		for (int groupNum = 1; groupNum < newNumGroups; ++groupNum) {
			
			groupToReactionSetList.get(groupNum).clear();
			groupToMaxValueMap.put(groupNum, 0.0);
			groupToTotalGroupPropensityMap.put(groupNum, 0.0);
		}
		
		numGroups = newNumGroups;
		totalPropensity = 0;
		
		//assign reactions to groups
		for (String reaction : reactionToPropensityMap.keySet()) {
			
			double propensity = reactionToPropensityMap.get(reaction);
			
			totalPropensity += propensity;
			
			//the zero-propensity group doesn't need altering
			if (propensity == 0.0) continue;
			
			org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (propensity / minPropensity));
			int group = frexpResult.exponent;
			
			groupToReactionSetList.get(group).add(reaction);
			reactionToGroupMap.put(reaction, group);
			groupToTotalGroupPropensityMap.adjustValue(group, propensity);
			
			if (propensity > groupToMaxValueMap.get(group))
				groupToMaxValueMap.put(group, propensity);
		}
		
		//find out which (if any) groups are empty
		//this is done so that empty groups are never chosen during simulation
		
		nonemptyGroupSet.clear();
		
		for (int groupNum = 1; groupNum < numGroups; ++groupNum) {
			
			if (groupToReactionSetList.get(groupNum).isEmpty())
				continue;
			
			nonemptyGroupSet.add(groupNum);
		}		
	}
	
	/**
	 * chooses a random number between 0 and the total propensity
	 * then it finds which nonempty group this number belongs to
	 * 
	 * @param r2 random number
	 * @return the group selected
	 */
	private int selectGroup(double r2) {
		
		if (nonemptyGroupSet.size() == 0)
			return 0;
		
		double randomPropensity = r2 * (totalPropensity);
		double runningTotalGroupsPropensity = 0.0;
		int selectedGroup = 1;
		
		//finds the group that the random propensity lies in
		//it keeps adding the next group's total propensity to a running total
		//until the running total is greater than the random propensity
		for (; selectedGroup < numGroups; ++selectedGroup) {
			
			runningTotalGroupsPropensity += groupToTotalGroupPropensityMap.get(selectedGroup);
			
			if (randomPropensity < runningTotalGroupsPropensity && nonemptyGroupSet.contains(selectedGroup))
				break;
		}
		
		return selectedGroup;
	}
	
	/**
	 * from the selected group, a reaction is chosen randomly/uniformly
	 * a random number between 0 and the group's max propensity is then chosen
	 * if this number is not less than the chosen reaction's propensity,
	 * the reaction is rejected and the process is repeated until success occurs
	 * 
	 * @param selectedGroup the group to choose a reaction from
	 * @param r3
	 * @param r4
	 * @return the chosen reaction's ID
	 */
	private String selectReaction(int selectedGroup, double r3, double r4) {
		
		HashSet<String> reactionSet = groupToReactionSetList.get(selectedGroup);
		
		double randomIndex = FastMath.floor(r3 * reactionSet.size());
		int indexIter = 0;
		Iterator<String> reactionSetIterator = reactionSet.iterator();
		
		while (reactionSetIterator.hasNext() && indexIter < randomIndex) {
			
			reactionSetIterator.next();
			++indexIter;
		}
			
		String selectedReactionID = reactionSetIterator.next();	
		double reactionPropensity = reactionToPropensityMap.get(selectedReactionID);
		
		//this is choosing a value between 0 and the max propensity in the group
		double randomPropensity = r4 * groupToMaxValueMap.get(selectedGroup);
		
		//loop until there's no reaction rejection
		//if the random propensity is higher than the selected reaction's propensity, another random reaction is chosen
		while (randomPropensity > reactionPropensity) {
			
			r3 = randomNumberGenerator.nextDouble();
			r4 = randomNumberGenerator.nextDouble();
			
			randomIndex = (int) FastMath.floor(r3 * reactionSet.size());
			indexIter = 0;
			reactionSetIterator = reactionSet.iterator();
			
			while (reactionSetIterator.hasNext() && (indexIter < randomIndex)) {
				
				reactionSetIterator.next();
				++indexIter;
			}
				
			selectedReactionID = reactionSetIterator.next();
			reactionPropensity = reactionToPropensityMap.get(selectedReactionID);				
			randomPropensity = r4 * groupToMaxValueMap.get(selectedGroup);
		}		
		
		return selectedReactionID;
	}
	
	/**
	 * does a minimized initialization process to prepare for a new run
	 */
	protected void setupForNewRun(int newRun) {
		
//		if (dynamicBoolean == true) {
//			
//			SBMLReader reader = new SBMLReader();
//			SBMLDocument document = null;
//			
//			try {
//				document = reader.readSBML(SBMLFileName);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//			model = document.getModel();
//			
//			numSpecies = model.getNumSpecies();
//			numParameters = model.getNumParameters();
//			numReactions = model.getNumReactions();
//			numEvents = model.getNumEvents();
//			numRules = model.getNumRules();
//			numConstraints = model.getNumConstraints();
//			numInitialAssignments = model.getNumInitialAssignments();
//			
//			//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
//			speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
//			speciesToIsBoundaryConditionMap = new HashMap<String, Boolean>((int) numSpecies);
//			variableToIsConstantMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
//			speciesToHasOnlySubstanceUnitsMap = new HashMap<String, Boolean>((int) numSpecies);
//			speciesToCompartmentSizeMap = new TObjectDoubleHashMap<String>((int) numSpecies);
//			speciesIDSet = new LinkedHashSet<String>((int) numSpecies);
//			variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);
//			
//			reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
//			reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
//			reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
//			reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));
//			
//			componentToLocationMap = new LinkedHashMap<String, Point>();
//			componentToReactionSetMap = new HashMap<String, HashSet<String> >();
//			componentToVariableSetMap = new HashMap<String, HashSet<String> >();
//			componentToEventSetMap = new HashMap<String, HashSet<String> >();
//			
//			if (numEvents > 0) {
//				
//				triggeredEventQueue = new PriorityQueue<EventToFire>((int) numEvents, eventComparator);
//				untriggeredEventSet = new HashSet<String>((int) numEvents);
//				eventToPriorityMap = new TObjectDoubleHashMap<String>((int) numEvents);
//				eventToDelayMap = new HashMap<String, ASTNode>((int) numEvents);
//				eventToHasDelayMap = new HashMap<String, Boolean>((int) numEvents);
//				eventToTriggerMap = new HashMap<String, ASTNode>((int) numEvents);
//				eventToTriggerInitiallyTrueMap = new HashMap<String, Boolean>((int) numEvents);
//				eventToTriggerPersistenceMap = new HashMap<String, Boolean>((int) numEvents);
//				eventToUseValuesFromTriggerTimeMap = new HashMap<String, Boolean>((int) numEvents);
//				eventToAssignmentSetMap = new HashMap<String, HashSet<Object> >((int) numEvents);
//				eventToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
//				eventToPreviousTriggerValueMap = new HashMap<String, Boolean>((int) numEvents);
//				variableToEventSetMap = new HashMap<String, HashSet<String> >((int) numEvents);
//			}
//			
//			if (numRules > 0) {
//				
//				variableToAffectedAssignmentRuleSetMap = new HashMap<String, HashSet<AssignmentRule> >((int) numRules);
//				variableToIsInAssignmentRuleMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
//			}
//			
//			if (numConstraints > 0) {
//				
//				variableToAffectedConstraintSetMap = new HashMap<String, HashSet<ASTNode> >((int) numConstraints);		
//				variableToIsInConstraintMap = new HashMap<String, Boolean>((int) (numSpecies + numParameters));
//			}
//			
//			totalPropensity = 0.0;
//			numGroups = 0;
//			minPropensity = Double.MAX_VALUE;
//			maxPropensity = Double.MIN_VALUE;
//			
//			try {
//				initialize(0, newRun);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		//if it's not a dynamic model
//		else {
		
		//get rid of things that were created dynamically last run
		if (dynamicBoolean == true) {
			resetModel();
		}
			
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
		numGroups = 0;
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
		
		//STEP OB: create and populate initial groups		
		createAndPopulateInitialGroups();
		
		setupEvents();
		setupForOutput(0, newRun);
		
		if (dynamicBoolean == true)
			setupGrid();
		//}
	}
	
	/**
	 * updates the groups
	 */
	protected void updateAfterDynamicChanges() {
		
		reassignAllReactionsToGroups();
	}
	
	/**
	 * updates the groups of the reactions affected by the recently performed reaction
	 * ReassignAllReactionsToGroups() is called instead when all reactions need changing
	 * 
	 * @param affectedReactionSet the set of reactions affected by the recently performed reaction
	 */
	private void updateGroups(HashSet<String> affectedReactionSet) {
		
		//update the groups for all of the affected reactions
		//their propensities have changed and they may need to go into a different group
		for (String affectedReactionID : affectedReactionSet) {
			
			double newPropensity = reactionToPropensityMap.get(affectedReactionID);
			int oldGroup = reactionToGroupMap.get(affectedReactionID);		
			
			if (newPropensity == 0.0) {
				
				HashSet<String> oldReactionSet = groupToReactionSetList.get(oldGroup);
				
				//update group collections
				//zero propensities go into group 0
				oldReactionSet.remove(affectedReactionID);
				reactionToGroupMap.put(affectedReactionID, 0);
				groupToReactionSetList.get(0).add(affectedReactionID);
				
				if (oldReactionSet.size() == 0)
					nonemptyGroupSet.remove(oldGroup);	
			}
			//if the new propensity != 0.0 (ie, new group != 0)
			else {
				//if it's outside of the old group's boundaries
				if (newPropensity > groupToPropensityCeilingMap.get(oldGroup) ||
						newPropensity < groupToPropensityFloorMap.get(oldGroup)) {
					
					org.openmali.FastMath.FRExpResultf frexpResult = org.openmali.FastMath.frexp((float) (newPropensity / minPropensity));
					int group = frexpResult.exponent;
				
					//if the group is one that currently exists
					if (group < numGroups) {
						
						HashSet<String> newGroupReactionSet = groupToReactionSetList.get(group);
						HashSet<String> oldGroupReactionSet = groupToReactionSetList.get(oldGroup);
						
						//update group collections
						oldGroupReactionSet.remove(affectedReactionID);
						reactionToGroupMap.put(affectedReactionID, group);
						newGroupReactionSet.add(affectedReactionID);
						groupToTotalGroupPropensityMap.adjustValue(group, newPropensity);
						
						//if the group that the reaction was just added to is now nonempty
						if (newGroupReactionSet.size() == 1)
							nonemptyGroupSet.add(group);
						
						if (oldGroupReactionSet.size() == 0)
							nonemptyGroupSet.remove(oldGroup);
						
						if (newPropensity > groupToMaxValueMap.get(group))
							groupToMaxValueMap.put(group, newPropensity);
					}
					//this means the propensity goes into a group that doesn't currently exist
					else {
						
						//groupToReactionSetList is a list, so the group needs to be the index
						for (int iter = numGroups; iter <= group; ++iter) {
							
							if (iter >= groupToReactionSetList.size())
								groupToReactionSetList.add(new HashSet<String>(500));
							
							groupToTotalGroupPropensityMap.put(iter, 0.0);
						}
						
						numGroups = group + 1;
						
						HashSet<String> oldReactionSet = groupToReactionSetList.get(oldGroup);
						
						//update group collections
						groupToTotalGroupPropensityMap.adjustValue(group, newPropensity);
						groupToReactionSetList.get(oldGroup).remove(affectedReactionID);
						reactionToGroupMap.put(affectedReactionID, group);
						groupToReactionSetList.get(group).add(affectedReactionID);						
						nonemptyGroupSet.add(group);
						groupToMaxValueMap.put(group, newPropensity);
						
						if (oldReactionSet.size() == 0)
							nonemptyGroupSet.remove(oldGroup);
					}
				} 
				//if it's within the old group's boundaries (ie, group isn't changing)
				else {

					//maintain current group
					
					if (newPropensity > groupToMaxValueMap.get(oldGroup))
						groupToMaxValueMap.put(oldGroup, newPropensity);
					
					groupToTotalGroupPropensityMap.adjustValue(oldGroup, newPropensity);
				}
			}			
		}
	}
	
	/**
	 * updates the propensities of the reactions affected by the recently performed reaction
	 * @param affectedReactionSet the set of reactions affected by the recently performed reaction
	 * @return whether or not there's a new minPropensity (if there is, all reaction's groups need to change)
	 */
	private boolean updatePropensities(HashSet<String> affectedReactionSet) {
		
		boolean newMinPropensityFlag = false;
		
		//loop through the affected reactions and update the propensities
		for (String affectedReactionID : affectedReactionSet) {
			
			boolean notEnoughMoleculesFlag = false;
			
			HashSet<StringDoublePair> reactantStoichiometrySet = 
				reactionToReactantStoichiometrySetMap.get(affectedReactionID);
			
			if (reactantStoichiometrySet == null)
				continue;
			
			//check for enough molecules for the reaction to occur
			for (StringDoublePair speciesAndStoichiometry : reactantStoichiometrySet) {
				
				String speciesID = speciesAndStoichiometry.string;
				double stoichiometry = speciesAndStoichiometry.doub;
				
				//if there aren't enough molecules to satisfy the stoichiometry
				if (variableToValueMap.get(speciesID) < stoichiometry) {
					notEnoughMoleculesFlag = true;
					break;
				}
			}
			
			double newPropensity = 0.0;
			
			if (notEnoughMoleculesFlag == false)
				newPropensity = evaluateExpressionRecursive(reactionToFormulaMap.get(affectedReactionID));
			
			if (newPropensity > 0.0 && newPropensity < minPropensity) {
				
				minPropensity = newPropensity;
				newMinPropensityFlag = true;
			}
			
			if (newPropensity > maxPropensity)
				maxPropensity = newPropensity;
			
			double oldPropensity = reactionToPropensityMap.get(affectedReactionID);
			int oldGroup = reactionToGroupMap.get(affectedReactionID);
			
			//remove the old propensity from the group's total
			//later on, the new propensity is added to the (possibly new) group's total
			groupToTotalGroupPropensityMap.adjustValue(oldGroup, -oldPropensity);
			
			//add the difference of new v. old propensity to the total propensity
			totalPropensity += newPropensity - oldPropensity;
			
			reactionToPropensityMap.put(affectedReactionID, newPropensity);	
		}
		
		return newMinPropensityFlag;
	}

	
	
}

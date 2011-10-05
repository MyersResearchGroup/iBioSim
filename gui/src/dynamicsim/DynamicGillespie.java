package dynamicsim;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import main.Gui;

import org.openmali.FastMath;
import org.openmali.FastMath.FRExpResultf;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SpeciesReference;


public class DynamicGillespie {
	
	//SBML model
	private Model model = null;
	
	//generates random numbers based on the xorshift method
	XORShiftRandom randomNumberGenerator = null;
	
	private HashMap<String, Reaction> reactionToSBMLReactionMap = null;
	
	//allows for access to a propensity from a reaction ID
	private TObjectDoubleHashMap<String> reactionToPropensityMap = null;
	
	//allows for access to reactant/product speciesID and stoichiometry from a reaction ID
	//note that species and stoichiometries need to be thought of as unique for each reaction
	private HashMap<String, HashSet<StringDoublePair> > reactionToSpeciesAndStoichiometrySetMap = null;
	
	//allows for access to reactant/modifier speciesID and stoichiometry from a reaction ID
	private HashMap<String, HashSet<StringDoublePair> > reactionToReactantStoichiometrySetMap = null;
	
	//allows for access to a kinetic formula tree from a reaction
	private HashMap<String, ASTNode> reactionToFormulaMap = null;
	
	//allows for access to a group number from a reaction ID
	private TObjectIntHashMap<String> reactionToGroupMap = null;
	
	//allows for access to a set of reactions that a species is in (as a reactant or modifier) from a species ID
	private HashMap<String, HashSet<String> > speciesToAffectedReactionSetMap = null;
	
	//allows for access to species and parameter values from a variable ID
	private TObjectDoubleHashMap<String> variableToValueMap = null;
	
	//allows for access to a group's min/max propensity from a group ID
	private TIntDoubleHashMap groupToMaxValueMap = new TIntDoubleHashMap(50);
	
	//allows for access to the minimum/maximum possible propensity in the group from a group ID
	private TIntDoubleHashMap groupToPropensityFloorMap = new TIntDoubleHashMap(50);
	private TIntDoubleHashMap groupToPropensityCeilingMap = new TIntDoubleHashMap(50);
	
	//allows for access to the reactionIDs in a group from a group ID
	private ArrayList<HashSet<String> > groupToReactionSetList = new ArrayList<HashSet<String> >(50);
	
	//stores group numbers that are nonempty
	private TIntHashSet nonemptyGroupSet = new TIntHashSet(50);
	
	//number of groups including the empty groups and zero-propensity group
	private int numGroups = 0;
	
	//propensity variables
	double totalPropensity = 0.0;
	double minPropensity = Double.MAX_VALUE;
	double maxPropensity = Double.MIN_VALUE;
	
	
	public DynamicGillespie() {
	}
	
	/**
	 * simulates the sbml model
	 * 
	 * @param SBMLFileName
	 * @param outputDirectory
	 * @param timeLimit
	 * @param maxTimeStep
	 * @param randomSeed
	 */
	public void Simulate(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed) {
		
		long timeBeforeSim = System.nanoTime();
		
		//initialization will fail if the SBML model has errors
		try {
			if (!Initialize(SBMLFileName, outputDirectory, timeLimit, maxTimeStep, randomSeed))
				return;
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} 
		catch (XMLStreamException e) {
			e.printStackTrace();
			return;
		}
		
		System.err.println("initialization time: " + (System.nanoTime() - timeBeforeSim)/1e9f);
		
		//SIMULATION LOOP
		//simulate until the time limit is reached
		
		long step1Time = 0;
		long step2Time = 0;
		long step3aTime = 0;
		long step3bTime = 0;
		long step4Time = 0;
		long step5Time = 0;
		long step6Time = 0;
		
		double currentTime = 0.0;
		
		while (currentTime <= timeLimit) {
			
			//STEP 1: generate random numbers
			
			long step1Initial = System.nanoTime();
			
			double r1 = randomNumberGenerator.nextDouble();
			double r2 = randomNumberGenerator.nextDouble();
			double r3 = randomNumberGenerator.nextDouble();
			double r4 = randomNumberGenerator.nextDouble();
			
			step1Time += System.nanoTime() - step1Initial;
			
			
			//STEP 2: calculate delta_t, the time till the next reaction execution
			
			long step2Initial = System.nanoTime();
			 
			double delta_t = Math.log(1 / r1) / totalPropensity;
			
			step2Time += System.nanoTime() - step2Initial;
			
			//System.err.println(totalPropensity + " " + currentTime + " " + delta_t + " ");			
			//System.out.println("step 2: time is " + currentTime);
			
			//System.err.println(numGroups);
			
			
			//STEP 3A: select a group
			
			long step3aInitial = System.nanoTime();
			
			//pick a random index, loop through the nonempty groups until that index is reached
			int randomIndex = (int) Math.floor(r2 * (nonemptyGroupSet.size() - 0.0000001));
			int indexIter = 0;
			TIntIterator nonemptyGroupSetIterator = nonemptyGroupSet.iterator();
			
			while (nonemptyGroupSetIterator.hasNext() && (indexIter < randomIndex)) {
				
				//System.out.println("step 3a");
				
				nonemptyGroupSetIterator.next();
				++indexIter;
			}
				
			int selectedGroup = nonemptyGroupSetIterator.next();
			
			//System.err.println(" index: " + randomIndex + " group: " + selectedGroup);
		
			step3aTime += System.nanoTime() - step3aInitial;
			
			
			
			//STEP 3B: select a reaction within the group
			
			long step3bInitial = System.nanoTime();
			
			HashSet<String> reactionSet = groupToReactionSetList.get(selectedGroup);
			
			randomIndex = (int) Math.floor(r3 * reactionSet.size());
			indexIter = 0;
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
				
				//System.out.println("step 3b");
				//System.out.println(randomPropensity + "   " + reactionPropensity);
				
				r4 = randomNumberGenerator.nextDouble();
				
				randomIndex = (int) Math.floor(r4 * reactionSet.size());
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
			
			step3bTime += System.nanoTime() - step3bInitial;
			
			//System.err.println("\nreaction fired: " + selectedReactionID + " propensity: " + reactionPropensity);
			
			
			//STEP 4: perform selected reaction and update species counts
			
			long step4Initial = System.nanoTime();

			//set of all affected reactions that need propensity updating
			HashSet<String> totalAffectedReactionSet = new HashSet<String>(20);
			
			//loop through the reaction's reactants and products and update their amounts
			for (StringDoublePair speciesAndStoichiometry : reactionToSpeciesAndStoichiometrySetMap.get(selectedReactionID)) {
				
				//System.out.println("step 4");
				
				double stoichiometry = speciesAndStoichiometry.doub;
				String speciesID = speciesAndStoichiometry.string;
				
				//System.out.println(selectedReactionID + " " + speciesID + "  " + variableToValueMap.get(speciesID) + "  " + stoichiometry);
				
				//update the species count
				variableToValueMap.adjustValue(speciesID, stoichiometry * delta_t);
				
				//System.out.println(" " + speciesID + "  " + variableToValueMap.get(speciesID));				
				
				totalAffectedReactionSet.addAll(speciesToAffectedReactionSetMap.get(speciesID));
			}
			
			
//			for (String reaction : reactionToFormulaMap.keySet())
//				System.out.println("reactionToFormula Key: " + reaction);
			
//			for (String species : speciesToAffectedReactionSetMap.keySet())
//				for (String reaction : speciesToAffectedReactionSetMap.get(species))
//					System.out.println(species + "   " + reaction);
			
			
			step4Time += System.nanoTime() - step4Initial;
			
			
			//STEP 5: compute affected reactions' new propensities and update total propensity			
			
			//loop through the affected reactions and update the propensities
			for (String affectedReactionID : totalAffectedReactionSet) {
				
				//System.out.println("step 5");
				
				//System.err.println(affectedReactionID + "  " + model.getReaction(affectedReactionID.replace("_fd","").replace("_rv","")).getKineticLaw().getFormula());
				//System.err.println(reactionToPropensityMap.get(affectedReactionID) + " " + totalAffectedReactionSet.size());
				
				long step5Initial = System.nanoTime();
				
				boolean notEnoughMoleculesFlag = false;
				
				HashSet<StringDoublePair> reactantStoichiometrySet = 
					reactionToReactantStoichiometrySetMap.get(affectedReactionID);
				
				//check for enough molecules for the reaction to occur
				for (StringDoublePair speciesAndStoichiometry : reactantStoichiometrySet) {
					
					String speciesID = speciesAndStoichiometry.string;
					double stoichiometry = speciesAndStoichiometry.doub;
					
					//this means there aren't enough molecules to satisfy the stoichiometry
					if (variableToValueMap.get(speciesID) < stoichiometry) {
						notEnoughMoleculesFlag = true;
						break;
					}
				}
				
				double newPropensity = 0.0;
				
				if (notEnoughMoleculesFlag == true)
					newPropensity = 0.0;
				else
					newPropensity = CalculatePropensity(reactionToFormulaMap.get(affectedReactionID));
				
				double oldPropensity = reactionToPropensityMap.get(affectedReactionID);
				
				//add the difference of new v. old propensity to the total propensity
				totalPropensity += newPropensity - oldPropensity;
				
				//System.err.println(String.valueOf(totalPropensity) + " " + String.valueOf(newPropensity - oldPropensity));
				//System.err.println(affectedReactionID + ": " + oldPropensity + " -> " + newPropensity);
				
				reactionToPropensityMap.put(affectedReactionID, newPropensity);				
				
				step5Time += System.nanoTime() - step5Initial;				
				
				
				
				//STEP 6: re-assign affected reactions to appropriate groups
				
				long step6Initial = System.nanoTime();
				
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
				else if (oldPropensity == 0.0) {
					
					int group;
					
					if (newPropensity <= minPropensity) {
						
						group = 1;
						minPropensity = newPropensity;
						ReassignAllReactionsToGroups();
					}
					else {
						
						if (newPropensity > maxPropensity)
							maxPropensity = newPropensity;
						
						FRExpResultf frexpResult = FastMath.frexp((float) (newPropensity / minPropensity));
						group = frexpResult.exponent;
					}
					
					if (group < numGroups) {
						
						HashSet<String> groupReactionSet = groupToReactionSetList.get(group);
						
						//update group collections
						groupToReactionSetList.get(0).remove(affectedReactionID);
						reactionToGroupMap.put(affectedReactionID, group);
						groupReactionSet.add(affectedReactionID);
						
						//if the group that the reaction was just added to is now nonempty
						if (groupReactionSet.size() == 1)
							nonemptyGroupSet.add(group);
						
						if (newPropensity > groupToMaxValueMap.get(group))
							groupToMaxValueMap.put(group, newPropensity);
					}
					//this means the propensity goes into a group that doesn't currently exist
					else {
						
						//groupToReactionSetList is a list, so the group needs to be the index
						for (int iter = numGroups; iter <= group; ++iter)			
							groupToReactionSetList.add(new HashSet<String>(500));
						
						numGroups = group + 1;
						
						//update group collections
						groupToReactionSetList.get(0).remove(affectedReactionID);
						reactionToGroupMap.put(affectedReactionID, group);
						groupToReactionSetList.get(group).add(affectedReactionID);						
						nonemptyGroupSet.add(group);
						groupToMaxValueMap.put(group, newPropensity);
					}					
				}
				else {
					if (newPropensity > groupToPropensityCeilingMap.get(oldGroup) ||
							newPropensity < groupToPropensityFloorMap.get(oldGroup)) {
						
						int group;
						
						if (newPropensity <= minPropensity) {
							
							group = 1;
							minPropensity = newPropensity;
							ReassignAllReactionsToGroups();
						}
						else {
							
							if (newPropensity > maxPropensity)
								maxPropensity = newPropensity;
							
							FRExpResultf frexpResult = FastMath.frexp((float) (newPropensity / minPropensity));
							group = frexpResult.exponent;
						}
						
						if (group < numGroups) {
							
							HashSet<String> newGroupReactionSet = groupToReactionSetList.get(group);
							HashSet<String> oldGroupReactionSet = groupToReactionSetList.get(oldGroup);
							
							//update group collections
							oldGroupReactionSet.remove(affectedReactionID);
							reactionToGroupMap.put(affectedReactionID, group);
							newGroupReactionSet.add(affectedReactionID);
							
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
							for (int iter = numGroups; iter <= group; ++iter)							
								groupToReactionSetList.add(new HashSet<String>(500));
							
							numGroups = group + 1;
							
							HashSet<String> oldReactionSet = groupToReactionSetList.get(oldGroup);
							
							//update group collections
							groupToReactionSetList.get(oldGroup).remove(affectedReactionID);
							reactionToGroupMap.put(affectedReactionID, group);
							groupToReactionSetList.get(group).add(affectedReactionID);						
							nonemptyGroupSet.add(group);
							groupToMaxValueMap.put(group, newPropensity);
							
							if (oldReactionSet.size() == 0)
								nonemptyGroupSet.remove(oldGroup);
						}					
					}
					else {

						//maintain current group; do nothing
					}
				}
				
				step6Time += System.nanoTime() - step6Initial;				
				
			}//end step 5/6 for loop
			
			//update time: choose the smaller of delta_t and the given max timestep
			//by default, delta_t will always be chosen
			if (delta_t <= maxTimeStep)
				currentTime += delta_t;
			else
				currentTime += maxTimeStep;
			
		} //end simulation loop
		
		
		System.err.println("total time: " + String.valueOf((System.nanoTime() - timeBeforeSim) / 1e9f));
		System.err.println("total step 1 time: " + String.valueOf(step1Time / 1e9f));
		System.err.println("total step 2 time: " + String.valueOf(step2Time / 1e9f));
		System.err.println("total step 3a time: " + String.valueOf(step3aTime / 1e9f));
		System.err.println("total step 3b time: " + String.valueOf(step3bTime / 1e9f));
		System.err.println("total step 4 time: " + String.valueOf(step4Time / 1e9f));
		System.err.println("total step 5 time: " + String.valueOf(step5Time / 1e9f));
		System.err.println("total step 6 time: " + String.valueOf(step6Time / 1e9f));
	}
	
	/**
	 * loads the model and initializes the maps and variables and whatnot
	 * @throws XMLStreamException 
	 * @throws FileNotFoundException 
	 */
	private boolean Initialize(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed) throws FileNotFoundException, XMLStreamException {
		
		randomNumberGenerator = new XORShiftRandom(randomSeed);	
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = reader.readSBML(SBMLFileName);	
		SBMLErrorLog errors = document.getListOfErrors();
		
		//if the sbml document has errors, tell the user and don't simulate
		if (document.getNumErrors() > 0) {
			
			String errorString = "";
			
			for (int i = 0; i < errors.getNumErrors(); i++) {
				errorString += errors.getError(i);
			}
			
			JOptionPane.showMessageDialog(Gui.frame, 
			"The SBML file contains " + document.getNumErrors() + " error(s):\n" + errorString,
			"SBML Error", JOptionPane.ERROR_MESSAGE);
			
			return false;
		}
		
		model = document.getModel();
		long numSpecies = model.getNumSpecies();
		long numParameters = model.getNumParameters();
		long numReactions = model.getNumReactions();
		
		//set initial capacities for collections (1.5 is used to multiply numReactions due to reversible reactions)
		speciesToAffectedReactionSetMap = new HashMap<String, HashSet<String> >((int) numSpecies);
		variableToValueMap = new TObjectDoubleHashMap<String>((int) numSpecies + (int) numParameters);		
		reactionToPropensityMap = new TObjectDoubleHashMap<String>((int) (numReactions * 1.5));		
		reactionToSpeciesAndStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));	
		reactionToReactantStoichiometrySetMap = new HashMap<String, HashSet<StringDoublePair> >((int) (numReactions * 1.5));
		reactionToFormulaMap = new HashMap<String, ASTNode>((int) (numReactions * 1.5));	
		reactionToGroupMap = new TObjectIntHashMap<String>((int) (numReactions * 1.5));
		reactionToSBMLReactionMap = new HashMap<String, Reaction>((int) numReactions);
		
		//add values to hashmap for easy access to species amounts
		for (int i = 0; i < numSpecies; ++i) {
			
			variableToValueMap.put(model.getSpecies(i).getId(), model.getSpecies(i).getInitialAmount());
			speciesToAffectedReactionSetMap.put(model.getSpecies(i).getId(), new HashSet<String>(20));
		}
		
		//add values to hashmap for easy access to global parameter values
		//NOTE: the IDs for the parameters and species must be unique, so putting them in the
		//same hashmap is okay
		for (int i = 0; i < numParameters; ++i) {
			
			variableToValueMap.put(model.getParameter(i).getId(), model.getParameter(i).getValue());
		}
		
		
		//STEP 0A: calculate initial propensities (including the total)		
		CalculateInitialPropensities(numReactions);
		
		
		//STEP OB: create and populate initial groups		
		CreateAndPopulateInitialGroups();
		
		return true;
	}
	
	/**
	 * calculates the initial propensities for each reaction in the model
	 * 
	 * @param numReactions the number of reactions in the model
	 */
	private void CalculateInitialPropensities(long numReactions) {
		
		//loop through all reactions and calculate their propensities
		for (int i = 0; i < numReactions; ++i) {
			
			Reaction reaction = model.getReaction(i);
			String reactionID = reaction.getId();
			KineticLaw reactionKineticLaw = reaction.getKineticLaw();			
			ASTNode reactionFormula = reactionKineticLaw.getMath();
			ListOf<LocalParameter> reactionParameters = reactionKineticLaw.getListOfLocalParameters();
			boolean notEnoughMoleculesFlagFd = false;
			boolean notEnoughMoleculesFlagRv = false;
			boolean notEnoughMoleculesFlag = false;
			
			reactionToSBMLReactionMap.put(reactionID, reaction);
			
			//put the local parameters into a hashmap for easy access
			//NOTE: these may overwrite some global parameters but that's fine,
			//because for each reaction the local parameters are the ones we want
			//and they're always defined
			for (int j = 0; j < reactionParameters.size(); ++j) {
				
				variableToValueMap.put(reactionParameters.get(j).getId(), reactionParameters.get(j).getValue());
			}
						
			//if it's a reversible reaction
			//split into a forward and reverse reaction (based on the minus sign in the middle)
			//and calculate both propensities
			if (reaction.getReversible()) {
				
				//associate the reaction's reactants/products and their stoichiometries with the reaction ID
				//this is a reversible reaction, so the stoichiometries are switched for the reverse reaction
				HashSet<StringDoublePair> speciesAndStoichiometrySetFd = new HashSet<StringDoublePair>();
				HashSet<StringDoublePair> speciesAndStoichiometrySetRv = new HashSet<StringDoublePair>();				
				HashSet<StringDoublePair> reactantStoichiometrySetFd = new HashSet<StringDoublePair>();
				HashSet<StringDoublePair> reactantStoichiometrySetRv = new HashSet<StringDoublePair>();
				
				for (int a = 0; a < reaction.getNumReactants(); ++a) {
					
					SpeciesReference reactant = reaction.getReactant(a);
					String reactantID = reactant.getSpecies();
					double reactantStoichiometry = reactant.getStoichiometry();
					
					speciesAndStoichiometrySetFd.add(new StringDoublePair(reactantID, -reactantStoichiometry));
					speciesAndStoichiometrySetRv.add(new StringDoublePair(reactantID, reactantStoichiometry));
					reactantStoichiometrySetFd.add(new StringDoublePair(reactantID, reactantStoichiometry));
					
					//as a reactant, this species affects the reaction in the forward direction
					speciesToAffectedReactionSetMap.get(reactantID).add(reactionID + "_fd");
					
					//make sure there are enough molecules for this species
					//(in the reverse direction, molecules aren't subtracted, but added)
					if (variableToValueMap.get(reactantID) < reactantStoichiometry)
						notEnoughMoleculesFlagFd = true;
				}
				
				for (int a = 0; a < reaction.getNumProducts(); ++a) {
					
					SpeciesReference product = reaction.getProduct(a);
					String productID = product.getSpecies();
					double productStoichiometry = product.getStoichiometry();
					
					speciesAndStoichiometrySetFd.add(new StringDoublePair(productID, productStoichiometry));
					speciesAndStoichiometrySetRv.add(new StringDoublePair(productID, -productStoichiometry));
					reactantStoichiometrySetRv.add(new StringDoublePair(productID, productStoichiometry));
					
					//as a product, this species affects the reaction in the reverse direction
					speciesToAffectedReactionSetMap.get(productID).add(reactionID + "_rv");
					
					//make sure there are enough molecules for this species
					//(in the forward direction, molecules aren't subtracted, but added)
					if (variableToValueMap.get(productID) < productStoichiometry)
						notEnoughMoleculesFlagRv = true;
				}
				
				for (int a = 0; a < reaction.getNumModifiers(); ++a) {
					
					String modifierID = reaction.getModifier(a).getSpecies();
					
					//as a modifier, this species affects the reaction (in both directions)
					speciesToAffectedReactionSetMap.get(modifierID).add(reactionID + "_fd");
					speciesToAffectedReactionSetMap.get(modifierID).add(reactionID + "_rv");
				}

				reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_fd", speciesAndStoichiometrySetFd);
				reactionToSpeciesAndStoichiometrySetMap.put(reactionID + "_rv", speciesAndStoichiometrySetRv);
				reactionToReactantStoichiometrySetMap.put(reactionID + "_fd", reactantStoichiometrySetFd);
				reactionToReactantStoichiometrySetMap.put(reactionID + "_rv", reactantStoichiometrySetRv);
				
				
				double propensity;
				
				//calculate forward reaction propensity
				if (notEnoughMoleculesFlagFd == true)
					propensity = 0.0;
				else {
					//the left child is what's left of the minus sign
					propensity = CalculatePropensity(reactionFormula.getLeftChild());
					
					if (propensity < minPropensity && propensity > 0) 
						minPropensity = propensity;
					else if (propensity > maxPropensity) 
						maxPropensity = propensity;
					
					totalPropensity += propensity;
				}
				
				reactionToPropensityMap.put(reactionID + "_fd", propensity);
				reactionToFormulaMap.put(reactionID + "_fd", reactionFormula.getLeftChild());
				
				//calculate reverse reaction propensity
				if (notEnoughMoleculesFlagRv == true)
					propensity = 0.0;
				else {
					//the right child is what's right of the minus sign
					propensity = CalculatePropensity(reactionFormula.getRightChild());
					
					if (propensity < minPropensity && propensity > 0) 
						minPropensity = propensity;
					else if (propensity > maxPropensity) 
						maxPropensity = propensity;
					
					totalPropensity += propensity;
				}
				
				reactionToPropensityMap.put(reactionID + "_rv", propensity);
				reactionToFormulaMap.put(reactionID + "_rv", reactionFormula.getRightChild());
			}
			//if it's not a reversible reaction
			else {
				//associate the reaction's reactants/products and their stoichiometries with the reaction ID
				HashSet<StringDoublePair> speciesAndStoichiometrySet = new HashSet<StringDoublePair>();
				HashSet<StringDoublePair> reactantAndModifierStoichiometrySet = new HashSet<StringDoublePair>();
				
				for (int a = 0; a < reaction.getNumReactants(); ++a) {
					
					SpeciesReference reactant = reaction.getReactant(a);
					String reactantID = reactant.getSpecies();
					double reactantStoichiometry = reactant.getStoichiometry();
					
					speciesAndStoichiometrySet.add(new StringDoublePair(reactantID, -reactantStoichiometry));
					reactantAndModifierStoichiometrySet.add(new StringDoublePair(reactantID, reactantStoichiometry));
					
					//as a reactant, this species affects the reaction
					speciesToAffectedReactionSetMap.get(reactantID).add(reactionID);
					
					//make sure there are enough molecules for this species
					if (variableToValueMap.get(reactantID) < reactantStoichiometry)
						notEnoughMoleculesFlag = true;
				}
				
				for (int a = 0; a < reaction.getNumProducts(); ++a) {
					
					SpeciesReference product = reaction.getProduct(a);				
					speciesAndStoichiometrySet.add(new StringDoublePair(product.getSpecies(), product.getStoichiometry()));
					
					//don't need to check if there are enough, because products are added
				}
				
				for (int a = 0; a < reaction.getNumModifiers(); ++a) {
					
					String modifierID = reaction.getModifier(a).getSpecies();
					
					//as a modifier, this species affects the reaction
					speciesToAffectedReactionSetMap.get(modifierID).add(reactionID);
					
					//modifiers don't have stoichiometry, so -1.0 is used
					reactantAndModifierStoichiometrySet.add(new StringDoublePair(modifierID, -1.0));
				}

				reactionToSpeciesAndStoichiometrySetMap.put(reactionID, speciesAndStoichiometrySet);
				reactionToReactantStoichiometrySetMap.put(reactionID, reactantAndModifierStoichiometrySet);
				
				double propensity;
				
				if (notEnoughMoleculesFlag == true)
					propensity = 0.0;
				else {
				
					//calculate propensity
					propensity = CalculatePropensity(reactionFormula);
					
					if (propensity < minPropensity && propensity > 0) minPropensity = propensity;
					if (propensity > maxPropensity) maxPropensity = propensity;
					
					totalPropensity += propensity;
				}
				
				reactionToPropensityMap.put(reactionID, propensity);
				reactionToFormulaMap.put(reactionID, reactionFormula);
			}
		}		
	}
	
	/**
	 * creates the appropriate number of groups and associates reactions with groups
	 */
	private void CreateAndPopulateInitialGroups() {
		
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
		
		groupToPropensityCeilingMap.put(currentGroup, groupPropensityCeiling);
		groupToMaxValueMap.put(currentGroup, 0.0);
		numGroups = currentGroup + 1;
		
		//start at 0 to make a group for zero propensities
		for (int groupNum = 0; groupNum < numGroups; ++groupNum) {

			groupToReactionSetList.add(new HashSet<String>(500));
		}
		
		
		//assign reactions to groups
		for (String reaction : reactionToPropensityMap.keySet()) {
			
			double propensity = reactionToPropensityMap.get(reaction);			
			FRExpResultf frexpResult = FastMath.frexp((float) (propensity / minPropensity));
			int group = frexpResult.exponent;
			
			//System.out.println(reaction + "   " + propensity + "   " + group);
			
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
	 * 
	 * @param reactionFormula
	 * @param reactionParameters
	 * @return
	 */
	private double CalculatePropensity(ASTNode node) {
		
		//these if/else-ifs before the else are leaf conditions
		
		//if it's a mathematical or logical constant
		if (node.isConstant()) {
			
			switch (node.getType()) {
			
			case CONSTANT_E:
				return Math.E;
				
			case CONSTANT_PI:
				return Math.PI;
				
//			case libsbml.AST_CONSTANT_TRUE:
//				return;
//				
//			case libsbml.AST_CONSTANT_FALSE:
//				return;
			}
		}
		//if it's an integer
		else if (node.isInteger())
			return node.getInteger();
		
		//if it's a non-integer
		else if (node.isReal())
			return node.getReal();
		
		//if it's a user-defined variable
		//eg, a species name or global/local parameter
		else if (node.isName())			
			return variableToValueMap.get(node.getName());
		
		//not a leaf node
		else {
			
			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();
			
			switch(node.getType()) {
			
			case PLUS:
				return (CalculatePropensity(leftChild) + CalculatePropensity(rightChild));
				
			case MINUS:
				return (CalculatePropensity(leftChild) - CalculatePropensity(rightChild));
				
			case TIMES:
				return (CalculatePropensity(leftChild) * CalculatePropensity(rightChild));
				
			case DIVIDE:
				return (CalculatePropensity(leftChild) / CalculatePropensity(rightChild));
				
			case FUNCTION_POWER:
				return (Math.pow(CalculatePropensity(leftChild), CalculatePropensity(rightChild)));
				
			} //end switch
			
		}
		
		//System.err.println("returning 0");
		
		return 0.0;
	}
	
	/**
	 * assigns all reactions to (possibly new) groups
	 * this is called when the minPropensity changes, which
	 * changes the groups' floor/ceiling propensity values
	 */
	private void ReassignAllReactionsToGroups() {
		
		int currentGroup = 1;
		double groupPropensityCeiling = 2 * minPropensity;
		
		//re-calulate and store group propensity floors/ceilings
		groupToPropensityCeilingMap.clear();
		groupToPropensityFloorMap.clear();
		groupToPropensityFloorMap.put(1, minPropensity);
		
		while (groupPropensityCeiling < maxPropensity) {
			
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
		for (int groupNum = 1; groupNum < numGroups; ++groupNum) {
			
			groupToReactionSetList.get(groupNum).clear();
			groupToMaxValueMap.put(groupNum, 0.0);
		}
		
		numGroups = newNumGroups;
		
		
		//assign reactions to groups
		for (String reaction : reactionToPropensityMap.keySet()) {
			
			double propensity = reactionToPropensityMap.get(reaction);
			
			//the zero-propensity group doesn't need altering
			if (propensity == 0.0) continue;
			
			FRExpResultf frexpResult = FastMath.frexp((float) (propensity / minPropensity));
			int group = frexpResult.exponent;
			
			groupToReactionSetList.get(group).add(reaction);
			reactionToGroupMap.put(reaction, group);
			
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
	 * class to combine a string and a double
	 */
	private class StringDoublePair {
		
		public String string;
		public double doub;
		
		StringDoublePair(String s, double d) {
			
			string = s;
			doub = d;
		}
	}
}


/*
IMPLEMENTATION NOTES:
	

if the top node of a reversible reaction isn't a minus sign, then give an error
	
modifiers shouldn't determine whether ANY reaction fires
	--it's taken care of in the kinetic law
	
i think you need to check, for a reversible reaction, which side(s) the modifier is in in the kinetic law
	--to determine if the modifer affects the reaction
	
for the groupToReactionSetList, see if you can somehow create a hashset, then create an arraylist and each index is a
Map.Entry (pointer) from the hashset, allowing you to maintain both simultaneously and easily (i think), and also accessing
by index and hashkey in constant time.  to get the map.entry you'll have to use a java hashset, but that may be worth it.

look at the util sbml formula functions to see what happens with strings
	--i'm not sure this is still relevant
	
	
OPTIMIZATION THINGS:
	
look into final and static keywords

get rid of the inner class (ie, make it non-inner)?


*/
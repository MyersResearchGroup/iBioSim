package analysis.dynamicsim.hierarchical.simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;

import analysis.dynamicsim.hierarchical.states.ArraysState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalEventToFire;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public abstract class HierarchicalObjects extends HierarchicalSimulation
{

	private int						numSubmodels;
	private boolean					isGrid;
	private Random					randomNumberGenerator;
	private ModelState				topmodel;
	private ArrayList<String>		filesCreated;
	private HashSet<String>			ibiosimFunctionDefinitions;
	private Map<String, Double>		replacements;
	private Map<String, Model>		models;
	private Map<String, Double>		initReplacementState;
	private Map<String, ModelState>	arrayModels;
	private Map<String, ModelState>	submodels;

	public HierarchicalObjects(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep,
			double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue,
				running, interestingSpecies, quantityType, abstraction);

		SBMLDocument document = getDocument();
		replacements = new HashMap<String, Double>();
		initReplacementState = new HashMap<String, Double>();
		models = new HashMap<String, Model>();
		isGrid = HierarchicalUtilities.checkGrid(document.getModel());
		models.put(document.getModel().getId(), document.getModel().clone());
		filesCreated = new ArrayList<String>();
		arrayModels = new HashMap<String, ModelState>();
		ibiosimFunctionDefinitions = new HashSet<String>(Arrays.asList("uniform", "exponential", "gamma", "chisq", "lognormal", "laplace", "cauchy",
				"poisson", "binomial", "bernoulli", "normal"));
	}

	public Map<String, ModelState> getArrayModels()
	{
		return arrayModels;
	}

	/**
	 * @return the filesCreated
	 */
	public ArrayList<String> getFilesCreated()
	{
		return filesCreated;
	}

	/**
	 * @return the ibiosimFunctionDefinitions
	 */
	public HashSet<String> getIbiosimFunctionDefinitions()
	{
		return ibiosimFunctionDefinitions;
	}

	/**
	 * @return the initReplacementState
	 */
	public Map<String, Double> getInitReplacementState()
	{
		return initReplacementState;
	}

	/**
	 * @return the models
	 */
	public Map<String, Model> getModels()
	{
		return models;
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	public Model getModel(String model)
	{
		return models.get(model);
	}

	/**
	 * @return the numSubmodels
	 */
	public int getNumSubmodels()
	{
		return numSubmodels;
	}

	/**
	 * @return the randomNumberGenerator
	 */
	public Random getRandomNumberGenerator()
	{
		return randomNumberGenerator;
	}

	/**
	 * @return the replacements
	 */
	public Map<String, Double> getReplacements()
	{
		return replacements;
	}

	/**
	 * @return the submodels
	 */
	public Map<String, ModelState> getSubmodels()
	{
		return submodels;
	}

	public void getSubmodelValue(String submodel, String variable)
	{
		if (submodels.containsKey(submodel))
		{

			submodels.get(submodel).getVariableToValue(getReplacements(), variable);
		}

	}

	public double getTopLevelValue(String variable)
	{
		return topmodel.getVariableToValue(getReplacements(), variable);
	}

	/**
	 * @return the topmodel
	 */
	public ModelState getTopmodel()
	{
		return topmodel;
	}

	/**
	 * @return the isGrid
	 */
	public boolean isGrid()
	{
		return isGrid;
	}

	public void setArrayModels(Map<String, ModelState> arrayModels)
	{
		this.arrayModels = arrayModels;
	}

	/**
	 * @param filesCreated
	 *            the filesCreated to set
	 */
	public void setFilesCreated(ArrayList<String> filesCreated)
	{
		this.filesCreated = filesCreated;
	}

	/**
	 * @param isGrid
	 *            the isGrid to set
	 */
	public void setGrid(boolean isGrid)
	{
		this.isGrid = isGrid;
	}

	/**
	 * @param ibiosimFunctionDefinitions
	 *            the ibiosimFunctionDefinitions to set
	 */
	public void setIbiosimFunctionDefinitions(HashSet<String> ibiosimFunctionDefinitions)
	{
		this.ibiosimFunctionDefinitions = ibiosimFunctionDefinitions;
	}

	/**
	 * @param initReplacementState
	 *            the initReplacementState to set
	 */
	public void setInitReplacementState(Map<String, Double> initReplacementState)
	{
		this.initReplacementState = initReplacementState;
	}

	/**
	 * @param models
	 *            the models to set
	 */
	public void setModels(Map<String, Model> models)
	{
		this.models = models;
	}

	/**
	 * @param numSubmodels
	 *            the numSubmodels to set
	 */
	public void setNumSubmodels(int numSubmodels)
	{
		this.numSubmodels = numSubmodels;
	}

	/**
	 * @param randomNumberGenerator
	 *            the randomNumberGenerator to set
	 */
	public void setRandomNumberGenerator(Random randomNumberGenerator)
	{
		this.randomNumberGenerator = randomNumberGenerator;
	}

	/**
	 * @param replacements
	 *            the replacements to set
	 */
	public void setReplacements(Map<String, Double> replacements)
	{
		this.replacements = replacements;
	}

	/**
	 * @param submodels
	 *            the submodels to set
	 */
	public void setSubmodels(Map<String, ModelState> submodels)
	{
		this.submodels = submodels;
	}

	public void setSubmodelValue(String submodel, String variable, double value)
	{
		if (submodels.containsKey(submodel))
		{

			submodels.get(submodel).setVariableToValue(getReplacements(), variable, value);
		}
	}

	public void setTopLevelValue(String variable, double value)
	{
		topmodel.setVariableToValue(getReplacements(), variable, value);
	}

	/**
	 * @param topmodel
	 *            the topmodel to set
	 */
	public void setTopmodel(ModelState topmodel)
	{
		this.topmodel = topmodel;
	}

	public ModelState getModelState(String id)
	{
		if (id.equals("topmodel"))
		{
			return topmodel;
		}
		return submodels.get(id);
	}

	public class ModelState extends ArraysState
	{
		private HierarchicalEventComparator	eventComparator;

		private HashMap<String, Boolean>	initEventToPreviousTriggerValueMap;
		private HashMap<String, ASTNode>	initEventToTriggerMap;
		private HashSet<String>				initUntriggeredEventSet;
		private Map<String, Double>			initVariableState;
		private boolean						isInitSet;

		public ModelState(Map<String, Model> models, String bioModel, String submodelID)
		{
			super(models, bioModel, submodelID);

			if (getNumEvents() > 0)
			{
				setTriggeredEventQueue(new PriorityQueue<HierarchicalEventToFire>((int) getNumEvents(), getEventComparator()));
			}
		}

		public ModelState(ModelState state)
		{
			super(state);
			eventComparator = state.eventComparator;
		}

		/**
		 * @return the eventToPreviousTriggerValueMap
		 */
		public void addEventToPreviousTriggerValueMap(String id, boolean value)
		{
			getEventToPreviousTriggerValueMap().put(id, value);
		}

		public void clear()
		{
			getSpeciesToAffectedReactionSetMap().clear();
			getSpeciesToIsBoundaryConditionMap().clear();
			getSpeciesToHasOnlySubstanceUnitsMap().clear();
			getSpeciesToCompartmentNameMap().clear();
			getSpeciesIDSet().clear();
			getVariableToValueMap().clear();
			getReactionToPropensityMap().clear();
			getReactionToSpeciesAndStoichiometrySetMap().clear();
			getReactionToReactantStoichiometrySetMap().clear();
			getReactionToFormulaMap().clear();
			setNoConstraintsFlag(true);
			setPropensity(0.0);
			setMinPropensity(Double.MAX_VALUE / 10.0);
			setMaxPropensity(Double.MIN_VALUE / 10.0);
		}

		@Override
		public ModelState clone()
		{
			return new ModelState(this);
		}

		/**
		 * @return the eventComparator
		 */
		public HierarchicalEventComparator getEventComparator()
		{
			if (eventComparator == null)
			{
				eventComparator = new HierarchicalEventComparator();
			}
			return eventComparator;
		}

		/**
		 * @return the speciesToReplacement
		 */
		public HashSet<HierarchicalStringPair> getSpeciesToReplacement(String species)
		{
			if (getSpeciesToReplacement().get(species) == null)
			{
				getSpeciesToReplacement().put(species, new HashSet<HierarchicalStringPair>());
			}

			return getSpeciesToReplacement().get(species);
		}

		@Override
		public double getVariableToValue(Map<String, Double> replacements, String variable)
		{
			if (getIsHierarchical().contains(variable))
			{
				String dep = getReplacementDependency().get(variable);
				return replacements.get(dep);
			}
			if (variable.contains("["))
			{
				String arrayedVariable = HierarchicalUtilities.getVariableFromArray(variable);
				int[] indices = HierarchicalUtilities.getIndicesFromVariable(variable);
				int index = HierarchicalUtilities.flattenedIndex(this, arrayedVariable, indices, replacements);
				indices = null;
				return getArrayVariableToValue().get(arrayedVariable)[index];
			}
			return getVariableToValueMap().get(variable);
		}

		public boolean isDeletedByMetaID(String metaid)
		{
			if (getDeletedElementsByMetaId().contains(metaid))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public boolean isDeletedBySID(String sid)
		{
			if (getDeletedElementsById().contains(sid))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public boolean isDeletedByUID(String uid)
		{
			if (getDeletedElementsByUId().contains(uid))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public boolean isInitSet()
		{
			return isInitSet;
		}

		public void restoreInitValues()
		{
			if (isInitSet)
			{

				getVariableToValueMap().clear();
				getVariableToValueMap().putAll(initVariableState);
				if (!isNoEventsFlag())
				{
					getEventToPreviousTriggerValueMap().clear();
					getEventToPreviousTriggerValueMap().putAll(initEventToPreviousTriggerValueMap);

					getEventToTriggerMap().clear();
					getEventToTriggerMap().putAll(initEventToTriggerMap);

					getTriggeredEventQueue().clear();
					getUntriggeredEventSet().clear();
					getUntriggeredEventSet().addAll(initUntriggeredEventSet);
				}
			}
		}

		/**
		 * @param eventComparator
		 *            the eventComparator to set
		 */
		public void setEventComparator(HierarchicalEventComparator eventComparator)
		{
			this.eventComparator = eventComparator;
		}

		public void setInitValues()
		{
			if (!isInitSet)
			{
				isInitSet = true;
				initVariableState = new HashMap<String, Double>(getVariableToValueMap());
				if (!isNoEventsFlag())
				{
					initEventToPreviousTriggerValueMap = new HashMap<String, Boolean>(getEventToPreviousTriggerValueMap());
					initEventToTriggerMap = new HashMap<String, ASTNode>(getEventToTriggerMap());
					initUntriggeredEventSet = new HashSet<String>(getUntriggeredEventSet());
				}
			}
		}

		@Override
		public void setVariableToValue(Map<String, Double> replacements, String variable, double value)
		{
			if (getIsHierarchical().contains(variable))
			{
				String dep = getReplacementDependency().get(variable);
				replacements.put(dep, value);
			}
			if (variable.contains("["))
			{
				String arrayedVariable = HierarchicalUtilities.getVariableFromArray(variable);
				int[] indices = HierarchicalUtilities.getIndicesFromVariable(variable);
				int index = HierarchicalUtilities.flattenedIndex(this, arrayedVariable, indices, replacements);
				getArrayVariableToValue().get(arrayedVariable)[index] = value;
			}
			getVariableToValueMap().put(variable, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "ModelState [ID=" + getID() + "]";
		}

		/**
		 * @return the reactionToPropensityMap
		 */
		public void updateReactionToPropensityMap(String reaction, double value)
		{
			getReactionToPropensityMap().put(reaction, value);
		}
	}

	// EVENT COMPARATOR INNER CLASS
	/**
	 * compares two events to see which one should be before the other in the
	 * priority queue
	 */
	public class HierarchicalEventComparator implements Comparator<HierarchicalEventToFire>
	{
		/**
		 * compares two events based on their fire times and priorities
		 */
		@Override
		public int compare(HierarchicalEventToFire event1, HierarchicalEventToFire event2)
		{

			if (event1.getFireTime() > event2.getFireTime())
			{
				return 1;
			}
			else if (event1.getFireTime() < event2.getFireTime())
			{
				return -1;
			}
			else
			{

				if (event1.getModelID().equals("topmodel"))
				{
				}
				else
				{
					submodels.get(event1.getModelID());
				}

				if (event2.getModelID().equals("topmodel"))
				{
				}
				else
				{
					submodels.get(event2.getModelID());
				}

				if (event1.getPriority() > event2.getPriority())
				{
					return -1;
				}
				else if (event1.getPriority() < event2.getPriority())
				{
					return 1;
				}
				else
				{
					return 0;
				}

			}
		}
	}
}

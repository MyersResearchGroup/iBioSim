package analysis.dynamicsim.hierarchical.simulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.Gui;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;

import analysis.dynamicsim.ParentSimulator;
import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.io.HierarchicalWriter;
import analysis.dynamicsim.hierarchical.util.math.EventNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.ValueNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

/**
 * This class provides the state variables of the simulation.
 * 
 * @author Leandro Watanabe
 * 
 */
public abstract class HierarchicalSimulation implements ParentSimulator
{

	final private int	SBML_LEVEL		= 3;
	final private int	SBML_VERSION	= 1;

	public static enum SimType
	{
		HSSA, HODE, FBA, MIXED, NONE;
	}

	protected final static String		PRODUCT		= "product";
	protected final static String		REACTANT	= "reactant";
	protected final static String		MODIFIER	= "modifier";

	protected final VariableNode		currentTime;
	protected double					printTime;

	private BufferedWriter				bufferedTSDWriter;
	private boolean						cancelFlag;
	private boolean						constraintFailureFlag;
	private boolean						constraintFlag;
	private int							currentRun;
	private String[]					interestingSpecies;
	private double						maxTimeStep;
	private double						minTimeStep;
	private String						outputDirectory;
	private boolean						printConcentrations;
	private HashSet<String>				printConcentrationSpecies;
	private double						printInterval;
	private JProgressBar				progress;
	private JFrame						running;
	private String						SBMLFileName;
	private boolean						sbmlHasErrorsFlag;
	private String						separator;
	private boolean						stoichAmpBoolean;
	private double						stoichAmpGridValue;
	protected double					timeLimit;
	private String						rootDirectory;
	private FileWriter					TSDWriter;
	private SBMLDocument				document;
	private String						abstraction;
	private int							totalRuns;

	protected List<VariableNode>		variableList;
	protected List<VariableNode>		assignmentList;

	protected double[]					initStateCopy;

	protected List<EventNode>			eventList;
	protected PriorityQueue<EventNode>	triggeredEventList;

	protected boolean					isInitialized;
	private int							numSubmodels;
	private boolean						isGrid;
	private Random						randomNumberGenerator;
	private ModelState					topmodel;
	private ArrayList<String>			filesCreated;
	private Map<String, ModelState>		submodels;
	final private SimType				type;
	private List<ModelState>			states;
	private ValueNode					totalPropensity;

	public HierarchicalSimulation(String SBMLFileName, String rootDirectory, String outputDirectory, long randomSeed, int runs, double timeLimit, double maxTimeStep, double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType, String abstraction, SimType type) throws XMLStreamException, IOException
	{
		this.SBMLFileName = SBMLFileName;
		this.timeLimit = timeLimit;
		this.maxTimeStep = maxTimeStep;
		this.minTimeStep = minTimeStep;
		this.progress = progress;
		this.printInterval = printInterval;
		this.printTime = 0;
		this.rootDirectory = rootDirectory;
		this.outputDirectory = outputDirectory;
		this.running = running;
		this.printConcentrationSpecies = new HashSet<String>();
		this.interestingSpecies = interestingSpecies;
		this.document = SBMLReader.read(new File(SBMLFileName));
		this.abstraction = abstraction;
		this.totalRuns = runs;
		this.type = type;
		this.filesCreated = new ArrayList<String>();
		this.topmodel = new ModelState("topmodel");
		this.submodels = new HashMap<String, ModelState>(0);
		this.currentTime = new VariableNode("_time", 0);
		this.currentRun = 1;
		this.randomNumberGenerator = new Random(randomSeed);
		if (abstraction != null)
		{
			if (abstraction.equals("expandReaction"))
			{
				// this.document =
				// HierarchicalUtilities.getFlattenedRegulations(rootDirectory,
				// SBMLFileName);
			}
		}

		if (quantityType != null)
		{
			String[] printConcentration = quantityType.replaceAll(" ", "").split(",");

			for (String s : printConcentration)
			{
				printConcentrationSpecies.add(s);
			}
		}

		if (stoichAmpValue <= 1.0)
		{
			stoichAmpBoolean = false;
		}
		else
		{
			stoichAmpBoolean = true;
			stoichAmpGridValue = stoichAmpValue;
		}

		SBMLErrorLog errors = document.getErrorLog();

		if (document.getErrorCount() > 0)
		{
			String errorString = "";

			for (int i = 0; i < errors.getErrorCount(); i++)
			{
				errorString += errors.getError(i);
			}

			JOptionPane.showMessageDialog(Gui.frame, "The SBML file contains " + document.getErrorCount() + " error(s):\n" + errorString, "SBML Error", JOptionPane.ERROR_MESSAGE);

			sbmlHasErrorsFlag = true;
		}

		separator = Gui.separator;
	}

	public HierarchicalSimulation(HierarchicalSimulation copy)
	{
		this.SBMLFileName = copy.SBMLFileName;
		this.timeLimit = copy.timeLimit;
		this.maxTimeStep = copy.maxTimeStep;
		this.minTimeStep = copy.minTimeStep;
		this.progress = copy.progress;
		this.printInterval = copy.printInterval;
		this.rootDirectory = copy.rootDirectory;
		this.outputDirectory = copy.outputDirectory;
		this.running = copy.running;
		this.printConcentrationSpecies = copy.printConcentrationSpecies;
		this.interestingSpecies = copy.interestingSpecies;
		this.document = copy.document;
		this.abstraction = copy.abstraction;
		this.totalRuns = copy.totalRuns;
		this.type = copy.type;
		this.isGrid = copy.isGrid;
		this.filesCreated = copy.filesCreated;
		this.topmodel = copy.topmodel;
		this.submodels = copy.submodels;
		this.separator = copy.separator;
		this.currentTime = copy.currentTime;
		this.randomNumberGenerator = copy.randomNumberGenerator;
	}

	public void addModelState(ModelState modelstate)
	{

		if (states == null)
		{
			states = new ArrayList<ModelState>();
		}

		states.add(modelstate);
	}

	public List<ModelState> getListOfModelStates()
	{
		return states;
	}

	/**
	 * @return the bufferedTSDWriter
	 */
	public BufferedWriter getBufferedTSDWriter()
	{
		return bufferedTSDWriter;
	}

	/**
	 * @return the cancelFlag
	 */
	public boolean isCancelFlag()
	{
		return cancelFlag;
	}

	/**
	 * @return the constraintFailureFlag
	 */
	public boolean isConstraintFailureFlag()
	{
		return constraintFailureFlag;
	}

	/**
	 * @return the constraintFlag
	 */
	public boolean isConstraintFlag()
	{
		return constraintFlag;
	}

	/**
	 * @return the currentRun
	 */
	public int getCurrentRun()
	{
		return currentRun;
	}

	/**
	 * @return the currentTime
	 */
	public VariableNode getCurrentTime()
	{
		return currentTime;
	}

	/**
	 * @return the interestingSpecies
	 */
	public String[] getInterestingSpecies()
	{
		return interestingSpecies;
	}

	/**
	 * @return the maxTimeStep
	 */
	public double getMaxTimeStep()
	{
		return maxTimeStep;
	}

	/**
	 * @return the minTimeStep
	 */
	public double getMinTimeStep()
	{
		return minTimeStep;
	}

	/**
	 * @return the outputDirectory
	 */
	public String getOutputDirectory()
	{
		return outputDirectory;
	}

	/**
	 * @return the printConcentrations
	 */
	public boolean isPrintConcentrations()
	{
		return printConcentrations;
	}

	/**
	 * @return the printConcentrationSpecies
	 */
	public HashSet<String> getPrintConcentrationSpecies()
	{
		return printConcentrationSpecies;
	}

	/**
	 * @return the printInterval
	 */
	public double getPrintInterval()
	{
		return printInterval;
	}

	/**
	 * @return the progress
	 */
	public JProgressBar getProgress()
	{
		return progress;
	}

	/**
	 * @return the running
	 */
	public JFrame getRunning()
	{
		return running;
	}

	/**
	 * @return the sBML_LEVEL
	 */
	public int getSBML_LEVEL()
	{
		return SBML_LEVEL;
	}

	/**
	 * @return the sBML_VERSION
	 */
	public int getSBML_VERSION()
	{
		return SBML_VERSION;
	}

	/**
	 * @return the sBMLFileName
	 */
	public String getSBMLFileName()
	{
		return SBMLFileName;
	}

	/**
	 * @return the sbmlHasErrorsFlag
	 */
	public boolean isSbmlHasErrorsFlag()
	{
		return sbmlHasErrorsFlag;
	}

	/**
	 * @return the separator
	 */
	public String getSeparator()
	{
		return separator;
	}

	/**
	 * @return the stoichAmpBoolean
	 */
	public boolean isStoichAmpBoolean()
	{
		return stoichAmpBoolean;
	}

	/**
	 * @return the stoichAmpGridValue
	 */
	public double getStoichAmpGridValue()
	{
		return stoichAmpGridValue;
	}

	/**
	 * @return the timeLimit
	 */
	public double getTimeLimit()
	{
		return timeLimit;
	}

	/**
	 * @return the rootDirectory
	 */
	public String getRootDirectory()
	{
		return rootDirectory;
	}

	/**
	 * @return the tSDWriter
	 */
	public FileWriter getTSDWriter()
	{
		return TSDWriter;
	}

	/**
	 * @param bufferedTSDWriter
	 *            the bufferedTSDWriter to set
	 */
	public void setBufferedTSDWriter(BufferedWriter bufferedTSDWriter)
	{
		this.bufferedTSDWriter = bufferedTSDWriter;
	}

	/**
	 * @param cancelFlag
	 *            the cancelFlag to set
	 */
	public void setCancelFlag(boolean cancelFlag)
	{
		this.cancelFlag = cancelFlag;
	}

	/**
	 * @param constraintFailureFlag
	 *            the constraintFailureFlag to set
	 */
	public void setConstraintFailureFlag(boolean constraintFailureFlag)
	{
		this.constraintFailureFlag = constraintFailureFlag;
	}

	/**
	 * @param constraintFlag
	 *            the constraintFlag to set
	 */
	public void setConstraintFlag(boolean constraintFlag)
	{
		this.constraintFlag = constraintFlag;
	}

	/**
	 * @param currentRun
	 *            the currentRun to set
	 */
	public void setCurrentRun(int currentRun)
	{
		this.currentRun = currentRun;
	}

	/**
	 * @param currentTime
	 *            the currentTime to set
	 */
	public void setCurrentTime(double currentTime)
	{
		this.currentTime.setValue(currentTime);
	}

	/**
	 * @param interestingSpecies
	 *            the interestingSpecies to set
	 */
	public void setInterestingSpecies(String[] interestingSpecies)
	{
		this.interestingSpecies = interestingSpecies;
	}

	/**
	 * @param maxTimeStep
	 *            the maxTimeStep to set
	 */
	public void setMaxTimeStep(double maxTimeStep)
	{
		this.maxTimeStep = maxTimeStep;
	}

	/**
	 * @param minTimeStep
	 *            the minTimeStep to set
	 */
	public void setMinTimeStep(double minTimeStep)
	{
		this.minTimeStep = minTimeStep;
	}

	/**
	 * @param outputDirectory
	 *            the outputDirectory to set
	 */
	public void setOutputDirectory(String outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}

	/**
	 * @param printConcentrations
	 *            the printConcentrations to set
	 */
	public void setPrintConcentrations(boolean printConcentrations)
	{
		this.printConcentrations = printConcentrations;
	}

	/**
	 * @param printConcentrationSpecies
	 *            the printConcentrationSpecies to set
	 */
	public void setPrintConcentrationSpecies(HashSet<String> printConcentrationSpecies)
	{
		this.printConcentrationSpecies = printConcentrationSpecies;
	}

	/**
	 * @param printInterval
	 *            the printInterval to set
	 */
	public void setPrintInterval(double printInterval)
	{
		this.printInterval = printInterval;
	}

	/**
	 * @param progress
	 *            the progress to set
	 */
	public void setProgress(JProgressBar progress)
	{
		this.progress = progress;
	}

	/**
	 * @param running
	 *            the running to set
	 */
	public void setRunning(JFrame running)
	{
		this.running = running;
	}

	/**
	 * @param sBMLFileName
	 *            the sBMLFileName to set
	 */
	public void setSBMLFileName(String sBMLFileName)
	{
		SBMLFileName = sBMLFileName;
	}

	/**
	 * @param sbmlHasErrorsFlag
	 *            the sbmlHasErrorsFlag to set
	 */
	public void setSbmlHasErrorsFlag(boolean sbmlHasErrorsFlag)
	{
		this.sbmlHasErrorsFlag = sbmlHasErrorsFlag;
	}

	/**
	 * @param separator
	 *            the separator to set
	 */
	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	/**
	 * @param stoichAmpBoolean
	 *            the stoichAmpBoolean to set
	 */
	public void setStoichAmpBoolean(boolean stoichAmpBoolean)
	{
		this.stoichAmpBoolean = stoichAmpBoolean;
	}

	/**
	 * @param stoichAmpGridValue
	 *            the stoichAmpGridValue to set
	 */
	public void setStoichAmpGridValue(double stoichAmpGridValue)
	{
		this.stoichAmpGridValue = stoichAmpGridValue;
	}

	/**
	 * @param timeLimit
	 *            the timeLimit to set
	 */
	public void setTimeLimit(double timeLimit)
	{
		this.timeLimit = timeLimit;
	}

	/**
	 * @param rootDirectory
	 *            the rootDirectory to set
	 */
	public void setRootDirectory(String rootDirectory)
	{
		this.rootDirectory = rootDirectory;
	}

	/**
	 * @param tSDWriter
	 *            the tSDWriter to set
	 */
	public void setTSDWriter(FileWriter tSDWriter)
	{
		TSDWriter = tSDWriter;
	}

	/**
	 * @return the document
	 */
	public SBMLDocument getDocument()
	{
		return document;
	}

	/**
	 * @param document
	 *            the document to set
	 */
	public void setDocument(SBMLDocument document)
	{
		this.document = document;
	}

	/**
	 * 
	 * @return
	 */
	public String getAbstraction()
	{
		return abstraction;
	}

	/**
	 * 
	 * @param abstraction
	 */
	public void setAbstraction(String abstraction)
	{
		this.abstraction = abstraction;
	}

	/**
	 * 
	 * @return
	 */
	public int getTotalRuns()
	{
		return totalRuns;
	}

	/**
	 * @return the filesCreated
	 */
	public ArrayList<String> getFilesCreated()
	{
		return filesCreated;
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
	 * @return the submodels
	 */
	public Map<String, ModelState> getSubmodels()
	{
		return submodels;
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
	 * @param numSubmodels
	 *            the numSubmodels to set
	 */
	public void setNumSubmodels(int numSubmodels)
	{
		this.numSubmodels = numSubmodels;
	}

	/**
	 * @param submodels
	 *            the submodels to set
	 */
	public void addSubmodel(String id, ModelState modelstate)
	{
		if (submodels == null)
		{
			submodels = new HashMap<String, ModelState>();
		}
		submodels.put(id, modelstate);
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

	public SimType getType()
	{
		return type;
	}

	protected void setupForOutput(int currentRun)
	{
		setCurrentRun(currentRun);
		try
		{
			setTSDWriter(new FileWriter(getOutputDirectory() + "run-" + currentRun + ".tsd"));
			setBufferedTSDWriter(new BufferedWriter(getTSDWriter()));
			getBufferedTSDWriter().write('(');
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected List<ReactionNode> getReactionList()
	{
		if (states == null)
		{
			states = getModelStateList();
		}

		List<ReactionNode> reactions = new ArrayList<ReactionNode>();

		for (ModelState submodel : states)
		{

			if (submodel.getNumOfReactions() > 0)
			{
				reactions.addAll(submodel.getReactions());
			}
		}

		return reactions;

	}

	protected List<VariableNode> getAssignmentRuleList()
	{
		if (variableList == null)
		{
			variableList = getVariableList();
		}

		assignmentList = new ArrayList<VariableNode>();

		for (VariableNode variable : variableList)
		{
			if (variable.hasAssignmentRule())
			{
				assignmentList.add(variable);
			}
		}

		return assignmentList;

	}

	protected List<EventNode> getEventList()
	{
		if (states == null)
		{
			states = getModelStateList();
		}

		List<EventNode> events = new ArrayList<EventNode>();

		for (ModelState modelstate : states)
		{

			if (modelstate.getNumOfEvents() > 0)
			{
				events.addAll(modelstate.getEvents());
			}
		}

		return events;

	}

	protected List<VariableNode> getVariableList()
	{

		if (states == null)
		{
			states = getModelStateList();
		}

		List<VariableNode> variables = new ArrayList<VariableNode>();

		for (ModelState modelstate : states)
		{
			if (modelstate.getNumOfVariables() > 0)
			{
				variables.addAll(modelstate.getVariables());
			}
		}

		return variables;

	}

	protected List<VariableNode> getConstantsList()
	{

		if (states == null)
		{
			states = getModelStateList();
		}

		List<VariableNode> constants = new ArrayList<VariableNode>();

		for (ModelState modelstate : states)
		{
			if (modelstate.getNumOfConstants() > 0)
			{
				constants.addAll(modelstate.getConstants());
			}
		}

		return constants;

	}

	protected List<VariableNode> getInitAssignmentList()
	{

		if (assignmentList == null)
		{
			assignmentList = getAssignmentRuleList();
		}
		List<VariableNode> initAssignmentList = new ArrayList<VariableNode>(assignmentList);
		List<VariableNode> constants = getConstantsList();

		for (VariableNode node : constants)
		{
			if (node.hasInitAssignment())
			{
				initAssignmentList.add(node);
			}
		}

		return initAssignmentList;

	}

	protected double[] getArrayState(List<VariableNode> variables)
	{
		double[] state = new double[variables.size()];
		for (int i = 0; i < state.length; i++)
		{
			state[i] = variables.get(i).getValue();
		}
		return state;

	}

	protected List<ModelState> getModelStateList()
	{

		List<ModelState> listOfStates = new ArrayList<ModelState>();

		ModelState topmodel = getTopmodel();
		listOfStates.add(topmodel);

		for (ModelState submodel : getSubmodels().values())
		{
			listOfStates.add(submodel);
		}

		return listOfStates;

	}

	public void linkPropensities()
	{
		if (states == null)
		{
			states = getModelStateList();
		}
		ValueNode propensity;
		totalPropensity = new ValueNode(0);
		for (ModelState modelstate : states)
		{

			if (modelstate.getNumOfReactions() > 0)
			{
				propensity = modelstate.createPropensity();
				for (ReactionNode node : modelstate.getReactions())
				{
					node.setTotalPropensityRef(totalPropensity);
					node.setModelPropensityRef(propensity);
				}
			}
		}

	}

	/**
	 * Returns the total propensity of all model states.
	 */
	protected double getTotalPropensity()
	{
		return totalPropensity != null ? totalPropensity.getValue() : 0;
	}

	public void setTopLevelValue(String variable, double value)
	{
		if (topmodel != null)
		{
			VariableNode variableNode = topmodel.getNode(variable);
			if (variableNode != null)
			{
				variableNode.setValue(value);
			}
		}
	}

	public double getTopLevelValue(String variable)
	{
		if (topmodel != null)
		{
			VariableNode variableNode = topmodel.getNode(variable);
			if (variableNode != null)
			{
				return variableNode.getValue();
			}
		}

		return Double.NaN;
	}

	protected void printToFile()
	{
		while (currentTime.getValue() >= printTime && printTime <= getTimeLimit())
		{
			try
			{
				HierarchicalWriter.printToTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getInterestingSpecies(), getPrintConcentrationSpecies(), printTime);
				getBufferedTSDWriter().write(",\n");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			printTime = getRoundedDouble(printTime + getPrintInterval());

			if (getRunning() != null)
			{
				getRunning().setTitle("Progress (" + (int) ((getCurrentTime().getValue() / getTimeLimit()) * 100.0) + "%)");
			}
		}
	}

	private double getRoundedDouble(double value)
	{
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(6, BigDecimal.ROUND_HALF_EVEN);
		double newValue = bd.doubleValue();
		bd = null;
		return newValue;
	}

}
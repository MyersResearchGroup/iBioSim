package analysis.dynamicsim.hierarchical.simulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import main.Gui;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;

import analysis.dynamicsim.ParentSimulator;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

/**
 * This class provides the state variables of the simulation.
 * 
 * @author Leandro Watanabe
 * 
 */
public abstract class HierarchicalSimulation implements ParentSimulator
{

	final private int				SBML_LEVEL		= 3;
	final private int				SBML_VERSION	= 1;

	protected final static String	PRODUCT			= "product";
	protected final static String	REACTANT		= "reactant";
	protected final static String	MODIFIER		= "modifier";

	private BufferedWriter			bufferedTSDWriter;
	private boolean					cancelFlag;
	private boolean					constraintFailureFlag;
	private boolean					constraintFlag;
	private int						currentRun;
	private double					currentTime;
	private String[]				interestingSpecies;
	private double					maxTimeStep;
	private double					minTimeStep;
	private String					outputDirectory;
	private boolean					printConcentrations;
	private HashSet<String>			printConcentrationSpecies;
	private double					printInterval;
	private JProgressBar			progress;
	private JFrame					running;
	private String					SBMLFileName;
	private boolean					sbmlHasErrorsFlag;
	private String					separator;
	private boolean					stoichAmpBoolean;
	private double					stoichAmpGridValue;
	private double					timeLimit;
	private String					rootDirectory;
	private FileWriter				TSDWriter;
	private SBMLDocument			document;
	private String					abstraction;
	private int						totalRuns;

	public HierarchicalSimulation(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep,
			double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
			String quantityType, String abstraction) throws IOException, XMLStreamException
	{
		this.SBMLFileName = SBMLFileName;
		this.timeLimit = timeLimit;
		this.maxTimeStep = maxTimeStep;
		this.minTimeStep = minTimeStep;
		this.progress = progress;
		this.printInterval = printInterval;
		this.rootDirectory = rootDirectory;
		this.outputDirectory = outputDirectory;
		this.running = running;
		this.printConcentrationSpecies = new HashSet<String>();
		this.interestingSpecies = interestingSpecies;
		this.document = SBMLReader.read(new File(SBMLFileName));
		this.abstraction = abstraction;
		this.totalRuns = runs;

		if (abstraction != null)
		{
			if (abstraction.equals("expandReaction"))
			{
				this.document = HierarchicalUtilities.getFlattenedRegulations(rootDirectory, SBMLFileName);
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

			JOptionPane.showMessageDialog(Gui.frame, "The SBML file contains " + document.getErrorCount() + " error(s):\n" + errorString,
					"SBML Error", JOptionPane.ERROR_MESSAGE);

			sbmlHasErrorsFlag = true;
		}

		separator = Gui.separator;
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
	public double getCurrentTime()
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
		this.currentTime = currentTime;
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

}
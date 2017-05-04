package edu.utah.ece.async.ibiosim.analysis;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import edu.utah.ece.async.ibiosim.analysis.fba.FluxBalanceAnalysis;
import edu.utah.ece.async.ibiosim.analysis.markov.BuildStateGraphThread;
import edu.utah.ece.async.ibiosim.analysis.markov.PerformSteadyStateMarkovAnalysisThread;
import edu.utah.ece.async.ibiosim.analysis.markov.PerformTransientMarkovAnalysisThread;
import edu.utah.ece.async.ibiosim.analysis.markov.StateGraph;
import edu.utah.ece.async.ibiosim.analysis.markov.StateGraph.Property;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation.SimulationType;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.Simulator;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.MutableString;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.DataParser;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.lema.verification.lpn.Abstraction;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Translator;

public class Run extends Observable
{

	private AnalysisProperties properties;
	private final Message message = new Message();


	private int executeAtacs(Runtime exec, String theFile, String filename, String outDir, String printer_id, boolean refresh, File work) throws IOException, InterruptedException
	{
		int exitValue = 0;
		Process reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=hse2 " + theFile, Executables.envp, work);
		message.setLog("Executing:\natacs -T0.000001 -oqoflhsgllvA " + filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "out.hse");
		this.notifyObservers(message);
		exec.exec("atacs -T0.000001 -oqoflhsgllvA out.hse", null, work);
		if (reb2sac != null)
		{
			exitValue = reb2sac.waitFor();
		}

		return exitValue;
	}

	private int executeFBA(String directory, String theFile, boolean refresh, String filename, String printer_id, String outDir) throws XMLStreamException, IOException
	{
		int exitValue = 255;

		FluxBalanceAnalysis fluxBalanceAnalysis = new FluxBalanceAnalysis(directory + GlobalConstants.separator, theFile, properties.getAbsError());
		exitValue = fluxBalanceAnalysis.PerformFluxBalanceAnalysis();

		if (exitValue == 1)
		{
			message.setErrorDialog("Error", "Flux balance analysis did not converge.");
		}
		else if (exitValue == 2)
		{
			message.setErrorDialog("Error", "Flux balance analysis failed.");
		}
		else if (exitValue == -1)
		{
			message.setErrorDialog("Error", "No flux balance constraints.");
		}
		else if (exitValue == -2)
		{
			message.setErrorDialog("Error", "Initial point must be strictly feasible.");
		}
		else if (exitValue == -3)
		{
			message.setErrorDialog("Error",  "The FBA problem is infeasible.");
		}
		else if (exitValue == -4)
		{
			message.setErrorDialog("Error", "The FBA problem has a singular KKT system.");
		}
		else if (exitValue == -5)
		{
			message.setErrorDialog("Error", "The matrix must have at least one row.");
		}
		else if (exitValue == -6)
		{
			message.setErrorDialog("Error", "The matrix is singular.");
		}
		else if (exitValue == -7)
		{
			message.setErrorDialog("Error", "Equalities matrix A must have full rank.");
		}
		else if (exitValue == -8)
		{
			message.setErrorDialog("Error", "Miscellaneous FBA failure (see console for details).");
		}
		else if (exitValue == -9)
		{
			message.setErrorDialog("Error", "Reaction in flux objective does not have a flux bound.");
		}
		else if (exitValue == -10)
		{
			message.setErrorDialog("Error",  "All reactions must have flux bounds for FBA.");
		}
		else if (exitValue == -11)
		{
			message.setErrorDialog("Error", "No flux objectives.");
		}
		else if (exitValue == -12)
		{
			message.setErrorDialog("Error", "No active flux objective.");
		}
		else if (exitValue == -13)
		{
			message.setErrorDialog("Error", "Unknown flux balance analysis error.");
		}
		if(exitValue != 0)
		{
			this.notifyObservers(message);
		}
		return exitValue;
	}

	private int executeXhtml(String filename, Runtime exec, String directory, String theFile, String out, File work) throws IOException, InterruptedException
	{
		int exitValue = 0;
		Simulator.expandArrays(filename, 1);
		Process reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=xhtml --out=" + out + ".xhtml " + theFile, Executables.envp, work);

		Preferences biosimrc = Preferences.userRoot();
		String xhtmlCmd = biosimrc.get("biosim.general.browser", "");
		message.setLog("Executing:\n" + xhtmlCmd + " " + directory + out + ".xhtml");

		this.notifyObservers(this);
		exec.exec(xhtmlCmd + " " + out + ".xhtml", null, work);

		if (reb2sac != null)
		{
			exitValue = reb2sac.waitFor();
		}

		return exitValue;
	}

}

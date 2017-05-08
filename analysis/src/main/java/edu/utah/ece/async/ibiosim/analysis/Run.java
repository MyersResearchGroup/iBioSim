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

	 private int executeDot(boolean nary, boolean abstraction, Runtime exec, String directory, String root, String theFile, String modelFile, String simName, String filename, String out, File work) throws IOException, InterruptedException
	  {
		 
	    int exitValue= 255;
	    /*
	    if (nary)
	    {
	      LPN lhpnFile = new LPN();
	      lhpnFile.load(directory + GlobalConstants.separator + theFile.replace(".sbml", "").replace(".xml", "") + ".lpn");
	      lhpnFile.printDot(directory + GlobalConstants.separator + theFile.replace(".sbml", "").replace(".xml", "") + ".dot");
	      exitValue = 0;
	    }
	    else if (modelFile.contains(".lpn"))
	    {
	      LPN lhpnFile = new LPN();
	      try {
	        lhpnFile.load(root + GlobalConstants.separator + modelFile);
	      } catch (BioSimException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      }
	      if (abstraction)
	      {
	        Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
	        abst.abstractSTG(false);
	        abst.printDot(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile.replace(".lpn", ".dot"));
	      }
	      else
	      {
	        lhpnFile.printDot(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile.replace(".lpn", ".dot"));
	      }
	      exitValue = 0;
	    }
	    else
	    {
	      writeLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + filename);
	      reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + theFile, Executables.envp, work);
	      exitValue = reb2sac.waitFor();
	    }
	    
	    if (System.getProperty("os.name").contentEquals("Linux"))
	    {
	      writeLog("Executing:\ndotty " + directory + out + ".dot" );
	      exec.exec("dotty " + out + ".dot", null, work);
	    }
	    else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
	    {
	      writeLog("Executing:\nopen " + directory + out + ".dot");
	      exec.exec("open " + out + ".dot", null, work);
	    }
	    else
	    {
	      writeLog("Executing:\ndotty " + directory + out + ".dot");
	      exec.exec("dotty " + out + ".dot", null, work);
	    }
	    

	    */
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

	private int executeMarkov(String modelFile, String theFile, String sim, String root, String directory, String filename, String printer_id, String outDir, String lpnProperty, boolean refresh) throws XMLStreamException, IOException, InterruptedException
	  {
		/*
	    String prop = null;
	    LPN lhpnFile = null;
	    if (modelFile.contains(".lpn"))
	    {
	      lhpnFile = new LPN();
	       lhpnFile.load(root + GlobalConstants.separator + modelFile);
	    }
	    else
	    {
	      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
	      ArrayList<String> specs = new ArrayList<String>();
	      ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
	      String[] intSpecies = properties.getIntSpecies();
	      for (int i = 0; i < intSpecies.length; i++)
	      {
	        if (!intSpecies[i].equals(""))
	        {
	          String[] split = intSpecies[i].split(" ");
	          if (split.length > 1)
	          {
	            String[] levels = split[1].split(",");
	            if (levels.length > 0)
	            {
	              specs.add(split[0]);
	              conLevel.add(levels);
	            }
	          }
	        }
	      }
	      BioModel bioModel = new BioModel(root);
	      bioModel.load(root + GlobalConstants.separator + filename);
	      if (bioModel.flattenModel(true) != null)
	      {
	        if (!lpnProperty.equals(""))
	        {
	          prop = lpnProperty;
	        }
	        ArrayList<String> propList = new ArrayList<String>();
	        if (prop == null)
	        {
	          Model m = bioModel.getSBMLDocument().getModel();
	          for (int num = 0; num < m.getConstraintCount(); num++)
	          {
	            String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	            if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
	            {
	              propList.add(constraint);
	            }
	          }
	        }
	        if (propList.size() > 0)
	        {
//	          String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
//	          if ((s != null) && (s.length() > 0))
//	          {
//	            Model m = bioModel.getSBMLDocument().getModel();
//	            for (int num = 0; num < m.getConstraintCount(); num++)
//	            {
//	              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
//	              if (s.equals(constraint))
//	              {
//	                prop = Translator.convertProperty(m.getConstraint(num).getMath());
//	              }
//	            }
//	          }
	        }
	        MutableString mutProp = new MutableString(prop);
	        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
	        prop = mutProp.getString();
	        if (lhpnFile == null)
	        {
	          new File(directory + GlobalConstants.separator + "running").delete();
	          return 0;
	        }
	        lhpnFile.save(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
	        this.message.setLog("Saving SBML file as LPN:\n" + filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
	      }
	      else
	      {
	        new File(directory + GlobalConstants.separator + "running").delete();
	        return 0;
	      }
	    }
	    StateGraph sg = new StateGraph(lhpnFile);
	    
	    BuildStateGraphThread buildStateGraph = new BuildStateGraphThread(sg, progress);
	    buildStateGraph.start();
	    buildStateGraph.join();
	    message.setLog("Number of states found: " + sg.getNumberOfStates());
	    message.setLog("Number of transitions found: " + sg.getNumberOfTransitions());
	    message.setLog("Memory used during state exploration: " + sg.getMemoryUsed() + "MB");
	    message.setLog("Total memory used: " + sg.getTotalMemoryUsed() + "MB");
	    if (sim.equals("reachability-analysis") && !sg.getStop())
	    {
	      Object[] options = { "Yes", "No" };
	      int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
	        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	      if (value == JOptionPane.YES_OPTION)
	      {
	        String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
	        sg.outputStateGraph(graphFile, true);
	        try
	        {
	          Runtime execGraph = Runtime.getRuntime();
	          if (System.getProperty("os.name").contentEquals("Linux"))
	          {

	        	  message.setLog("Executing:\ndotty " + graphFile);
	            execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	          }
	          else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
	          {
	        	  message.setLog("Executing:\nopen " + graphFile);
	            execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	          }
	          else
	          {
	        	  message.setLog("Executing:\ndotty " + graphFile);
	            execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	          }
	        }
	        catch (Exception e1)
	        {
	          JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
	        }
	      }
	    }
	    else if (sim.equals("steady-state-markov-chain-analysis"))
	    {
	      if (!sg.getStop())
	      {
	    	  message.setLog("Performing steady state Markov chain analysis.");
	        PerformSteadyStateMarkovAnalysisThread performMarkovAnalysis = new PerformSteadyStateMarkovAnalysisThread(sg, progress);
	        if (modelFile.contains(".lpn"))
	        {
	          performMarkovAnalysis.start(absError, null);
	        }
	        else
	        {
	          BioModel gcm = new BioModel(root);
	          gcm.load(root + GlobalConstants.separator + modelEditor.getRefFile());
	          ArrayList<Property> propList = new ArrayList<Property>();
	          if (prop == null)
	          {
	            Model m = gcm.getSBMLDocument().getModel();
	            for (int num = 0; num < m.getConstraintCount(); num++)
	            {
	              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	              if (constraint.startsWith("St("))
	              {
	                propList.add(sg.createProperty(constraint.trim().replace(" ", ""), Translator.convertProperty(m.getConstraint(num).getMath())));
	              }
	            }
	          }
	          // TODO: THIS NEEDS FIXING
	          /*
	           * for (int i = 0; i <
	           * gcmEditor.getGCM().getConditions().size();
	           * i++) { if
	           * (gcmEditor.getGCM().getConditions().
	           * get(i).startsWith("St")) {
	           * conditions.add(Translator
	           * .getProbpropExpression
	           * (gcmEditor.getGCM().getConditions().get(i)));
	           * } }
	           */
	        /*  
			performMarkovAnalysis.start(absError, propList);
	        }
	        performMarkovAnalysis.join();
	        if (!sg.getStop())
	        {
	          String simrep = sg.getMarkovResults();
	          if (simrep != null)
	          {
	            FileOutputStream simrepstream = new FileOutputStream(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
	            simrepstream.write((simrep).getBytes());
	            simrepstream.close();
	          }
	          Object[] options = { "Yes", "No" };
	          int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
	            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	          if (value == JOptionPane.YES_OPTION)
	          {
	            String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
	            sg.outputStateGraph(graphFile, true);
	            try
	            {
	              Runtime execGraph = Runtime.getRuntime();
	              if (System.getProperty("os.name").contentEquals("Linux"))
	              {
	            	  message.setLog("Executing:\ndotty " + graphFile);
	                execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	              }
	              else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
	              {
	            	  message.setLog("Executing:\nopen " + graphFile);
	                execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	              }
	              else
	              {
	            	  message.setLog("Executing:\ndotty " + graphFile);
	                execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	              }
	            }
	            catch (Exception e1)
	            {
	              JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
	            }
	          }
	        }
	      }
	    }
	    else if (sim.equals("transient-markov-chain-analysis"))
	    {
	      if (!sg.getStop())
	      {
	        writeLog("Performing transient Markov chain analysis with uniformization.");
	        PerformTransientMarkovAnalysisThread performMarkovAnalysis = new PerformTransientMarkovAnalysisThread(sg, progress);
	        if (prop != null)
	        {
	          String[] condition;
	          try {
	            condition = Translator.getProbpropParts(Translator.getProbpropExpression(prop));
	            boolean globallyTrue = false;
	            if (prop.contains("PF"))
	            {
	              condition[0] = "true";
	            }
	            else if (prop.contains("PG"))
	            {
	              condition[0] = "true";
	              globallyTrue = true;
	            }
	            performMarkovAnalysis.start(timeLimit, timeStep, printInterval, absError, condition, globallyTrue);
	          } catch (BioSimException e) {
	            e.printStackTrace();
	          }

	        }
	        else
	        {
	          performMarkovAnalysis.start(timeLimit, timeStep, printInterval, absError, null, false);
	        }
	        performMarkovAnalysis.join();
	        if (!sg.getStop())
	        {
	          String simrep = sg.getMarkovResults();
	          if (simrep != null)
	          {
	            FileOutputStream simrepstream = new FileOutputStream(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
	            simrepstream.write((simrep).getBytes());
	            simrepstream.close();
	          }
	          Object[] options = { "Yes", "No" };
	          int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
	            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	          if (value == JOptionPane.YES_OPTION)
	          {
	            String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
	            sg.outputStateGraph(graphFile, true);
	            try
	            {
	              Runtime execGraph = Runtime.getRuntime();
	              if (System.getProperty("os.name").contentEquals("Linux"))
	              {
	                writeLog("Executing:\ndotty " + graphFile);
	                execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	              }
	              else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
	              {
	                writeLog("Executing:\nopen " + graphFile);
	                execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	              }
	              else
	              {
	                writeLog("Executing:\ndotty " + graphFile);
	                execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
	              }
	            }
	            catch (Exception e1)
	            {
	              JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
	            }
	          }
	          if (sg.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd"))
	          {
	            if (refresh)
	            {
	              for (int i = 0; i < simTab.getComponentCount(); i++)
	              {
	                if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
	                {
	                  if (simTab.getComponentAt(i) instanceof Graph)
	                  {
	                    ((Graph) simTab.getComponentAt(i)).refresh();
	                  }
	                }
	              }
	            }
	          }
	        }
	      }
	      if (refresh)
	      {
	        for (int i = 0; i < simTab.getComponentCount(); i++)
	        {
	          if (simTab.getComponentAt(i).getName().equals("Histogram"))
	          {
	            if (simTab.getComponentAt(i) instanceof Graph)
	            {
	              ((Graph) simTab.getComponentAt(i)).refresh();
	            }
	            else
	            {
	              simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
	              simTab.getComponentAt(i).setName("Histogram");
	            }
	          }
	        }
	      }
	    }
	    */
	    return 0;
	  }

	
	 private int executePrism(String modelFile, String root, String directory, String out, String filename, String simName, String lpnProperty, Runtime exec, File work) throws IOException, InterruptedException, XMLStreamException, BioSimException
	  {
	    int exitValue = 255;
	    /*
	    String prop = null;
	    progress.setIndeterminate(true);
	    LPN lhpnFile = null;
	    if (modelFile.contains(".lpn"))
	    {
	      lhpnFile = new LPN();
	       lhpnFile.load(root + GlobalConstants.separator + modelFile);
	    }
	    else
	    {
	      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
	      ArrayList<String> specs = new ArrayList<String>();
	      ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
	      for (int i = 0; i < intSpecies.length; i++)
	      {
	        if (!intSpecies[i].equals(""))
	        {
	          String[] split = intSpecies[i].split(" ");
	          if (split.length > 1)
	          {
	            String[] levels = split[1].split(",");
	            if (levels.length > 0)
	            {
	              specs.add(split[0]);
	              conLevel.add(levels);
	            }
	          }
	        }
	      }
	      BioModel bioModel = new BioModel(root);
	      bioModel.addObserver(this);
	      bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
	      if (bioModel.flattenModel(true) != null)
	      {
	        if (!lpnProperty.equals(""))
	        {
	          prop = lpnProperty;
	        }
	        ArrayList<String> propList = new ArrayList<String>();
	        if (prop == null)
	        {
	          Model m = bioModel.getSBMLDocument().getModel();
	          for (int num = 0; num < m.getConstraintCount(); num++)
	          {
	            String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	            if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
	            {
	              propList.add(constraint);
	            }
	          }
	        }
	        if (propList.size() > 0)
	        {
	          String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
	          if ((s != null) && (s.length() > 0))
	          {
	            Model m = bioModel.getSBMLDocument().getModel();
	            for (int num = 0; num < m.getConstraintCount(); num++)
	            {
	              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	              if (s.equals(constraint))
	              {
	                prop = Translator.convertProperty(m.getConstraint(num).getMath());
	              }
	            }
	          }
	        }
	        MutableString mutProp = new MutableString(prop);
	        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
	        prop = mutProp.getString();
	        if (lhpnFile == null)
	        {
	          new File(directory + GlobalConstants.separator + "running").delete();
	          logFile.close();
	          return 0;
	        }
	        writeLog("Saving SBML file as PRISM file:\n" + filename.replace(".xml", ".prism"));
	        writeLog("Saving PRISM Property file:\n" + filename.replace(".xml", ".pctl"));
	        LPN.convertLPN2PRISM(logFile, lhpnFile, filename.replace(".xml", ".prism"), 
	          bioModel.getSBMLDocument());
	        Preferences biosimrc = Preferences.userRoot();
	        String prismCmd = biosimrc.get("biosim.general.prism", "");
	        writeLog("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl"); 
	        reb2sac = exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
	        String error = "", result = "", fullLog = "";
	        try
	        {
	          InputStream reb = reb2sac.getInputStream();
	          InputStreamReader isr = new InputStreamReader(reb);
	          BufferedReader br = new BufferedReader(isr);
	          String line;
	          while ((line = br.readLine()) != null)
	          {
	            fullLog += line + '\n';
	            if (line.startsWith("Result:"))
	            {
	              result = line + '\n';
	            }
	          }
	          InputStream reb2 = reb2sac.getErrorStream();
	          int read = reb2.read();
	          while (read != -1)
	          {
	            error += (char) read;
	            read = reb2.read();
	          }
	          br.close();
	          isr.close();
	          reb.close();
	          reb2.close();
	        }
	        catch (Exception e)
	        {
	          // e.printStackTrace();
	        }
	        if (reb2sac != null)
	        {
	          exitValue = reb2sac.waitFor();
	        }
	        running.setCursor(null);
	        running.dispose();
	        if (!error.equals(""))
	        {
	          writeLog("Errors:\n" + error + "\n");
	        }
	        else if (!result.equals(""))
	        {
	          writeLog(result);
	        }
	        else
	        {
	          JTextArea messageArea = new JTextArea(fullLog);
	          messageArea.setEditable(false);
	          JScrollPane scroll = new JScrollPane();
	          scroll.setMinimumSize(new Dimension(500, 500));
	          scroll.setPreferredSize(new Dimension(500, 500));
	          scroll.setViewportView(messageArea);
	          JOptionPane.showMessageDialog(Gui.frame, scroll, "Verification Failed", JOptionPane.ERROR_MESSAGE);
	        }

	        exitValue = 0;
	      }
	      else
	      {
	        new File(directory + GlobalConstants.separator + "running").delete();
	        logFile.close();
	        exitValue = 0;
	      }
	    }
	    
	    if (reb2sac != null)
	    {
	      exitValue = reb2sac.waitFor();
	    }
	    
	    Preferences biosimrc = Preferences.userRoot();
	    String prismCmd = biosimrc.get("biosim.general.prism", "");
	    writeLog("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl");
	    exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
	    
	    return exitValue;
	  }
	 
	private int executeNary(String modelFile, String directory, String simName, String root, String lpnProperty, boolean abstraction) throws XMLStreamException, IOException
	  {
	    String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
	    ArrayList<String> specs = new ArrayList<String>();
	    ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
	    String[] intSpecies = properties.getIntSpecies();
	    
	    for (int i = 0; i < intSpecies.length; i++)
	    {
	      if (!intSpecies[i].equals(""))
	      {
	        String[] split = intSpecies[i].split(" ");
	        if (split.length > 1)
	        {
	          String[] levels = split[1].split(",");
	          if (levels.length > 0)
	          {
	            specs.add(split[0]);
	            conLevel.add(levels);
	          }
	        }
	      }
	    }
	    BioModel bioModel = new BioModel(root);
	    //TODO: check
	    bioModel.load(root + GlobalConstants.separator + modelFile);
	    String prop = null;
	    if (!lpnProperty.equals(""))
	    {
	      prop = lpnProperty;
	    }
	    ArrayList<String> propList = new ArrayList<String>();
	    if (prop == null)
	    {
	      Model m = bioModel.getSBMLDocument().getModel();
	      for (int num = 0; num < m.getConstraintCount(); num++)
	      {
	        String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	        if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
	        {
	          propList.add(constraint);
	        }
	      }
	    }
	    if (propList.size() > 0)
	    {
	      //String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
//	      if ((s != null) && (s.length() > 0))
//	      {
//	        Model m = bioModel.getSBMLDocument().getModel();
//	        for (int num = 0; num < m.getConstraintCount(); num++)
//	        {
//	          String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
//	          if (s.equals(constraint))
//	          {
//	            prop = Translator.convertProperty(m.getConstraint(num).getMath());
//	          }
//	        }
//	      }
	    }
	    MutableString mutProp = new MutableString(prop);
	    LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
	    prop = mutProp.getString();
	    if (lpnFile == null)
	    {
	      new File(directory + GlobalConstants.separator + "running").delete();
	      //logFile.close();
	      return 0;
	    }
	    lpnFile.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
	    try
	    {
	      Translator t1 = new Translator();
	      if (abstraction)
	      {
	        LPN lhpnFile = new LPN();
	        lhpnFile.load(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
	        //Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
	        //abst.addObserver(this);
	        //abst.abstractSTG(false);
	        //abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp");
	        t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp", prop);
	      }
	      else
	      {
	        t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName, prop);
	      }
	      t1.setFilename(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName.replace(".lpn", ".xml"));
	      t1.outputSBML();
	    }
	    catch(BioSimException e)
	    {
	      e.printStackTrace();
	    }
	    return 0;
	  }
	
	private int executePrism(String modelFile, String root, String directory, String out, String filename, String simName, String lpnProperty, Component component, ModelEditor modelEditor, Runtime exec, File work) throws IOException, InterruptedException, XMLStreamException, BioSimException
	  {
	    int exitValue = 255;
	    String prop = null;
	    progress.setIndeterminate(true);
	    LPN lhpnFile = null;
	    if (modelFile.contains(".lpn"))
	    {
	      lhpnFile = new LPN();
	       lhpnFile.load(root + GlobalConstants.separator + modelFile);
	    }
	    else
	    {
	      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
	      ArrayList<String> specs = new ArrayList<String>();
	      ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
	      for (int i = 0; i < intSpecies.length; i++)
	      {
	        if (!intSpecies[i].equals(""))
	        {
	          String[] split = intSpecies[i].split(" ");
	          if (split.length > 1)
	          {
	            String[] levels = split[1].split(",");
	            if (levels.length > 0)
	            {
	              specs.add(split[0]);
	              conLevel.add(levels);
	            }
	          }
	        }
	      }
	      BioModel bioModel = new BioModel(root);
	      bioModel.addObserver(this);
	      bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
	      if (bioModel.flattenModel(true) != null)
	      {
	        if (!lpnProperty.equals(""))
	        {
	          prop = lpnProperty;
	        }
	        ArrayList<String> propList = new ArrayList<String>();
	        if (prop == null)
	        {
	          Model m = bioModel.getSBMLDocument().getModel();
	          for (int num = 0; num < m.getConstraintCount(); num++)
	          {
	            String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	            if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
	            {
	              propList.add(constraint);
	            }
	          }
	        }
	        if (propList.size() > 0)
	        {
	          String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
	          if ((s != null) && (s.length() > 0))
	          {
	            Model m = bioModel.getSBMLDocument().getModel();
	            for (int num = 0; num < m.getConstraintCount(); num++)
	            {
	              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	              if (s.equals(constraint))
	              {
	                prop = Translator.convertProperty(m.getConstraint(num).getMath());
	              }
	            }
	          }
	        }
	        MutableString mutProp = new MutableString(prop);
	        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
	        prop = mutProp.getString();
	        if (lhpnFile == null)
	        {
	          new File(directory + GlobalConstants.separator + "running").delete();
	          logFile.close();
	          return 0;
	        }
	        writeLog("Saving SBML file as PRISM file:\n" + filename.replace(".xml", ".prism"));
	        writeLog("Saving PRISM Property file:\n" + filename.replace(".xml", ".pctl"));
	        LPN.convertLPN2PRISM(logFile, lhpnFile, filename.replace(".xml", ".prism"), 
	          bioModel.getSBMLDocument());
	        Preferences biosimrc = Preferences.userRoot();
	        String prismCmd = biosimrc.get("biosim.general.prism", "");
	        writeLog("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl"); 
	        reb2sac = exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
	        String error = "", result = "", fullLog = "";
	        try
	        {
	          InputStream reb = reb2sac.getInputStream();
	          InputStreamReader isr = new InputStreamReader(reb);
	          BufferedReader br = new BufferedReader(isr);
	          String line;
	          while ((line = br.readLine()) != null)
	          {
	            fullLog += line + '\n';
	            if (line.startsWith("Result:"))
	            {
	              result = line + '\n';
	            }
	          }
	          InputStream reb2 = reb2sac.getErrorStream();
	          int read = reb2.read();
	          while (read != -1)
	          {
	            error += (char) read;
	            read = reb2.read();
	          }
	          br.close();
	          isr.close();
	          reb.close();
	          reb2.close();
	        }
	        catch (Exception e)
	        {
	          // e.printStackTrace();
	        }
	        if (reb2sac != null)
	        {
	          exitValue = reb2sac.waitFor();
	        }
	        running.setCursor(null);
	        running.dispose();
	        if (!error.equals(""))
	        {
	          writeLog("Errors:\n" + error + "\n");
	        }
	        else if (!result.equals(""))
	        {
	          writeLog(result);
	        }
	        else
	        {
	          JTextArea messageArea = new JTextArea(fullLog);
	          messageArea.setEditable(false);
	          JScrollPane scroll = new JScrollPane();
	          scroll.setMinimumSize(new Dimension(500, 500));
	          scroll.setPreferredSize(new Dimension(500, 500));
	          scroll.setViewportView(messageArea);
	          JOptionPane.showMessageDialog(Gui.frame, scroll, "Verification Failed", JOptionPane.ERROR_MESSAGE);
	        }

	        exitValue = 0;
	      }
	      else
	      {
	        new File(directory + GlobalConstants.separator + "running").delete();
	        logFile.close();
	        exitValue = 0;
	      }
	    }
	    
	    if (reb2sac != null)
	    {
	      exitValue = reb2sac.waitFor();
	    }
	    
	    Preferences biosimrc = Preferences.userRoot();
	    String prismCmd = biosimrc.get("biosim.general.prism", "");
	    writeLog("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl");
	    exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
	    
	    */
	    return exitValue;
	  }
	
	private int executeSBML(String simName, String root, String directory, String filename, String theFile, String modelFile, boolean abstraction, boolean nary, String lpnProperty, File work, Runtime exec, Component component) throws IOException, HeadlessException, XMLStreamException, InterruptedException
	  {
	    //String sbmlName = JOptionPane.showInputDialog(component, "Enter Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
	    int exitValue = 255;

	    /*
	    if (sbmlName != null && !sbmlName.trim().equals(""))
	    {
	      sbmlName = sbmlName.trim();
	      if (!sbmlName.endsWith(".xml"))
	      {
	        sbmlName += ".xml";
	      }
	      File f = new File(root + GlobalConstants.separator + sbmlName);
	      if (f.exists())
	      {
	        Object[] options = { "Overwrite", "Cancel" };
	        int value = JOptionPane.showOptionDialog(component, "File already exists." + "\nDo you want to overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	        if (value == JOptionPane.YES_OPTION)
	        {
	          File dir = new File(root + GlobalConstants.separator + sbmlName);
	          if (dir.isDirectory())
	          {
	            gui.deleteDir(dir);
	          }
	          else
	          {
	            System.gc();
	            dir.delete();
	          }
	        }
	        else
	        {
	          new File(directory + GlobalConstants.separator + "running").delete();
	          logFile.close();
	          return 0;
	        }
	      }
	      if (modelFile.contains(".lpn"))
	      {
	        progress.setIndeterminate(true);
	        try
	        {
	          Translator t1 = new Translator();
	          if (abstraction)
	          {
	            LPN lhpnFile = new LPN();
	            lhpnFile.load(root + GlobalConstants.separator + modelFile);
	            Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
	            abst.abstractSTG(false);
	            abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile);
	            t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile, lpnProperty);
	          }
	          else
	          {
	            t1.convertLPN2SBML(root + GlobalConstants.separator + modelFile, lpnProperty);
	          }
	          t1.setFilename(root + GlobalConstants.separator + sbmlName);
	          t1.outputSBML();
	          exitValue = 0;
	          logFile.close();
	        }
	        catch(BioSimException e)
	        {
	          e.printStackTrace();
	        }
	        return exitValue;
	      }
	      else if (modelEditor != null && nary)
	      {
	        String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
	        ArrayList<String> specs = new ArrayList<String>();
	        ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
	        for (int i = 0; i < intSpecies.length; i++)
	        {
	          if (!intSpecies[i].equals(""))
	          {
	            String[] split = intSpecies[i].split(" ");
	            if (split.length > 1)
	            {
	              String[] levels = split[1].split(",");
	              if (levels.length > 0)
	              {
	                specs.add(split[0]);
	                conLevel.add(levels);
	              }
	            }
	          }
	        }
	        progress.setIndeterminate(true);
	        BioModel bioModel = new BioModel(root);
	        bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
	        if (bioModel.flattenModel(true) != null)
	        {
	          String prop = null;
	          if (!lpnProperty.equals(""))
	          {
	            prop = lpnProperty;
	          }
	          ArrayList<String> propList = new ArrayList<String>();
	          if (prop == null)
	          {
	            Model m = bioModel.getSBMLDocument().getModel();
	            for (int num = 0; num < m.getConstraintCount(); num++)
	            {
	              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	              if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
	              {
	                propList.add(constraint);
	              }
	            }
	          }
	          if (propList.size() > 0)
	          {
	            String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
	            if ((s != null) && (s.length() > 0))
	            {
	              Model m = bioModel.getSBMLDocument().getModel();
	              for (int num = 0; num < m.getConstraintCount(); num++)
	              {
	                String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
	                if (s.equals(constraint))
	                {
	                  prop = Translator.convertProperty(m.getConstraint(num).getMath());
	                }
	              }
	            }
	          }
	          MutableString mutProp = new MutableString(prop);
	          LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
	          prop = mutProp.getString();
	          if (lpnFile == null)
	          {
	            new File(directory + GlobalConstants.separator + "running").delete();
	            logFile.close();
	            return 0;
	          }
	          lpnFile.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
	          try
	          {
	            Translator t1 = new Translator();
	            if (abstraction)
	            {
	              LPN lhpnFile = new LPN();
	              lhpnFile.load(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
	              Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
	              abst.abstractSTG(false);
	              abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp");
	              t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp", prop);
	            }
	            else
	            {
	              t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName, prop);
	            }
	            t1.setFilename(root + GlobalConstants.separator + sbmlName);
	            t1.outputSBML();
	          }
	          catch(BioSimException e)
	          {
	            e.printStackTrace();
	          }
	        }
	        else
	        {
	          new File(directory + GlobalConstants.separator + "running").delete();
	          logFile.close();
	          return 0;
	        }
	        exitValue = 0;
	      }
	      else
	      {
	        if (analysisView.reb2sacAbstraction() && (abstraction || nary))
	        {
	          writeLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + GlobalConstants.separator + sbmlName + " " + filename);
	          reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + GlobalConstants.separator + sbmlName + " " + theFile, Executables.envp, work);
	        }
	        else
	        {
	          writeLog("Outputting SBML file:\n" + root + GlobalConstants.separator + sbmlName);
	          FileOutputStream fileOutput = new FileOutputStream(new File(root + GlobalConstants.separator + sbmlName));
	          FileInputStream fileInput = new FileInputStream(new File(filename));
	          int read = fileInput.read();
	          while (read != -1)
	          {
	            fileOutput.write(read);
	            read = fileInput.read();
	          }
	          fileInput.close();
	          fileOutput.close();
	          exitValue = 0;
	        }
	      }
	    }

	    if (reb2sac != null)
	    {
	      exitValue = reb2sac.waitFor();
	    }
	    */
	    return exitValue;
	  }
	
	private int executeSimulation(String sim, String direct, String directory, String root, String filename, String outDir, String theFile, boolean abstraction, boolean expandReaction, Runtime exec, Properties properties, File work) throws IOException, InterruptedException
	{
		int exitValue = 0;
		Preferences biosimrc = Preferences.userRoot();
		String reactionAbstraction = abstraction ? "reactionAbstraction" : expandReaction ? "expandReaction" : "None";
		double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
		String SBMLFileName = directory + GlobalConstants.separator + theFile;
		String command = null;
		String[] env = null;
		DynamicSimulation dynSim;

		if (direct != null && !direct.equals("."))
		{
			outDir = outDir + GlobalConstants.separator + direct;
		}
		boolean runJava = true;
		if (sim.equals("SSA-CR (Dynamic)"))
		{
			dynSim = new DynamicSimulation(SimulationType.CR);
		}
		else if (sim.equals("SSA-Direct (Dynamic)"))
		{
			dynSim = new DynamicSimulation(SimulationType.DIRECT);
		}
		else if (sim.equals("SSA-Direct (Flatten)"))
		{
			dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
		}
		else if (sim.equals("SSA-Direct (Hierarchical)"))
		{
			dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
		}
		else if (sim.equals("Mixed-Hierarchical"))
		{
			dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_MIXED);
		}
		else if (sim.equals("Hybrid-Hierarchical"))
		{
			dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_HYBRID);
		}
		else if (sim.equals("Runge-Kutta-Fehlberg (Dynamic)"))
		{
			dynSim = new DynamicSimulation(SimulationType.RK);
		}
		else if (sim.equals("Runge-Kutta-Fehlberg (Hierarchical)"))
		{
			dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
		}
		else if (sim.equals("Runge-Kutta-Fehlberg (Flatten)"))
		{
			dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
		}
		else if (biosimrc.get("biosim.sim.command", "").equals(""))
		{
			command = Executables.reb2sacExecutable + " --target.encoding=" + sim + " " + theFile;
			Simulator.expandArrays(filename, stoichAmpValue);
			env = Executables.envp;
			runJava = false;
		}
		else
		{
			command = biosimrc.get("biosim.sim.command", "");
			String fileStem = theFile.replaceAll(".xml", "");
			fileStem = fileStem.replaceAll(".sbml", "");
			command = command.replaceAll("filename", fileStem);
			command = command.replaceAll("sim", sim);
			runJava = false;
		}

		if(runJava)
		{
			if (direct != null && !direct.equals("."))
			{
				outDir = outDir + GlobalConstants.separator + direct;
			}
			//dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, 0.0, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, absError, printer_track_quantity, genStats, simTab, reactionAbstraction,  initialTime, outputStartTime);

			new File(directory + GlobalConstants.separator + "running").delete();
		}
		else
		{
			Simulator.expandArrays(filename, stoichAmpValue);
			message.setLog("Executing:\n" + command + "\n");
			this.notifyObservers(message);

			Process reb2sac = exec.exec(command, env, work);
			exitValue = reb2sac.waitFor();
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

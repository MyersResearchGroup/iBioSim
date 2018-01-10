/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.ParseException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import org.jdom2.JDOMException;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.XMLException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;

import com.lowagie.text.DocumentException;

import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisPropertiesLoader;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisPropertiesWriter;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.graphData.GraphData;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.BioObserver;

/**
 * Command line method for running the analysis jar file.  
 * <p>
 * Requirements:
 * </p>
 * <ul>
 *  <li>Optional:</li>
 *  <ul>
 *    <li>-d [value]: project directory</li>
 *    <li>-ti [value]: non-negative double initial simulation time</li>
 *    <li>-tl [value]: non-negative double simulation time limit</li>
 *    <li>-ot [value]: non-negative double for output time</li>
 *    <li>-pi [value]: positive double for print interval</li>
 *    <li>-m0 [value]: positive double for minimum step time</li>
 *    <li>-m1 [value]: positive double for maximum step time</li>
 *    <li>-aErr [value]: positive double for absolute error</li>
 *    <li>-sErr [value]: positive double for relative error</li>
 *    <li>-sd [value]: long for random seed</li>
 *    <li>-r [value]: integer for number of runs</li>
 *    <li>-sim [value]: simulation type. Options are: ode, hode, ssa, hssa, dfba, jode, jssa.</li>
 *    <li>-data [value]: graph data type. Options are: csv, tsd.</li>
 *  </ul>
 *  <li>Input file: (Combine archive, SED-ML, or SBML.</li>
 * </ul>
 * @author Leandro Watanabe
 * @author Tramy Nguyen
 * @author Chris Myers
 * 
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Analysis implements BioObserver
{

  private static final String ti = "Initial Time";
  private static final String tl = "Time Limit";
  private static final String ot = "Output Start Time";
  private static final String pi = "Print Interval";
  private static final String m0 = "Minimum Time Step";
  private static final String m1 = "Maximum Time Step";
  private static final String aErr = "Absolute Error";
  private static final String rErr = "Relative Error";
  private static final String sd = "Random Seed";
  private static final String r = "Number of Runs";
  private static final String sim = "Simulation";
  private static final String data = "Graph Data Type";
  
  private String sedML = null;
  private String propertiesFile = null;
  private String omex = null;
  private HashMap<String, String> propertiesMap;
  private final AnalysisProperties properties;

  private static void usage() 
  {
    System.err.println("Description:");
    System.err.println("\t This application is used to run various analysis methods.");
    System.err.println("Usage:");
    System.err.println("\t java -jar [options] input");
    System.err.println("Required:");
    System.err.println("\t input: combine archive, sed-ml, or sbml file.");
    System.err.println("Options:\n");
    System.err.println("\t -d [value]: project directory");
    System.err.println("\t -ti [value]: initial simulation time");
    System.err.println("\t -tl [value]: simulation time limit");
    System.err.println("\t -ot [value]: output time");
    System.err.println("\t -pi [value]: print interval");
    System.err.println("\t -m0 [value]: minimum step time");
    System.err.println("\t -m1 [value]: maximum step time");
    System.err.println("\t -aErr [value]: absolute error");
    System.err.println("\t -sErr [value]: relative error");
    System.err.println("\t -sd [value]: random seed");
    System.err.println("\t -r [value]: number of runs");
    System.err.println("\t -sim [value]: simulation type");
    System.err.println("\t -data [value]: output data type");
    System.exit(1);
  }

  public static void main(String[] args) throws Exception 
  {   
    Executables.checkExecutables();
    Analysis analysis = new Analysis();

    if(args.length < 1)
    {
      usage();
    }

    // Last argument should be a SED-ML file, Combine Archive, or SBML model
    if(args[args.length - 1].endsWith(".xml"))
    {
      analysis.properties.setModelFile(args[args.length - 1]);
      analysis.properties.setId(args[args.length - 1].replace(".xml", ""));
    }
    else if(args[args.length - 1].endsWith(".omex"))
    {
      analysis.properties.setId(args[args.length - 1].replace(".omex", ""));
      analysis.omex = args[args.length - 1];
    }
    else if(args[args.length - 1].endsWith(".sedml"))
    {
      analysis.properties.setId(args[args.length - 1].replace(".sedml", ""));
      analysis.sedML = args[args.length - 1];
    }
    else
    {
      usage();
    }


    // Optional arguments should have a value
    if((args.length - 1) % 2 > 0)
    {
      usage();
    }


    // Retrieve optional arguments
    for(int i = 0; i < args.length - 1; i=i+2)
    {
      String arg = args[i];
      String value = args[i+1];

      switch(arg)
      {
      case "-d":
        analysis.properties.setRoot(value);
        break;
      case "-p":
        if(value.endsWith(".properties"))
        {
          analysis.propertiesFile = value;
        }
        else
        {
          usage();
        }
        break;
      case "-ti":
        analysis.propertiesMap.put(ti, value);
        break;
      case "-tl":
        analysis.propertiesMap.put(tl, value);
        break;
      case "-ot":
        analysis.propertiesMap.put(ot, value);
        break;
      case "-pi":
        analysis.propertiesMap.put(pi, value);
        break;
      case "-m0":
        analysis.propertiesMap.put(m0, value);
        break;
      case "-m1":
        analysis.propertiesMap.put(m1, value);
        break;
      case "-aErr":
        analysis.propertiesMap.put(aErr, value);
        break;
      case "-rErr":
        analysis.propertiesMap.put(rErr, value);
        break;
      case "-sd":
        analysis.propertiesMap.put(sd, value);
        break;
      case "-r":
        analysis.propertiesMap.put(r, value);
        break;
      case "-sim":
        analysis.propertiesMap.put(sim, value);
        break;
      case "-data":
        analysis.propertiesMap.put(data, value);
        break;
      default:
        usage();
      }
    }
    
    try 
    {
      analysis.performAnalysis();
    } 
    catch (XMLException | IOException | XMLStreamException | InterruptedException | BioSimException | DocumentException e) 
    {
      System.out.println(e.getMessage());
    } 
  }

  private Analysis()
  {
    properties = new AnalysisProperties("", "", "", false);
    
    properties.addObserver(this);
    
    propertiesMap = new HashMap<String, String>();
  }

  private void performAnalysis() throws Exception
  {
    Run run = new Run(properties);
    run.addObserver(this);
    if(omex != null)
    {
      List<String> sedMLs = unpackageArchive(omex);
      
      for(String sedML : sedMLs)
      {
        runSEDML(sedML, properties, run, propertiesMap); 
      }
    }
    else if(sedML != null)
    {
      runSEDML(sedML, properties, run, propertiesMap); 
    }
    else
    {
      if(propertiesFile != null)
      {
        AnalysisPropertiesLoader.loadPropertiesFile(properties);
      }
      loadUserValues(properties, propertiesMap);
      run.execute();
    }
  }

  private List<String> unpackageArchive(String omex) throws IOException
  {
    List<String> listOfSedML = new ArrayList<String>();

    new File(properties.getDirectory()).mkdir();
    System.out.println ("--- reading archive. ---");
    File archiveFile = new File (properties.getRoot() + File.separator + omex);

    // read the archive stored in `archiveFile`
    CombineArchive ca;
    try {
      ca = new CombineArchive (archiveFile, true);
    }
    catch (JDOMException | ParseException | CombineArchiveException | IOException e) {

      return listOfSedML;
    }
    
    // read description of the archive itself
    System.out.println ("found " + ca.getDescriptions ().size ()
      + " meta data entries describing the archive.");

    // iterate over all entries in the archive
    for (ArchiveEntry entry : ca.getEntries ())
    {
      if (entry.getFormat().toString().contains("sed-ml")) {
        listOfSedML.add(properties.getDirectory() + File.separator + entry.getFileName());
      }
      System.out.println("Extracting: "+properties.getDirectory() + File.separator + entry.getFileName());
      entry.extractFile (new File(properties.getDirectory() + File.separator + entry.getFileName()));
    }
    properties.setRoot(properties.getDirectory());
    ca.close();
    return listOfSedML;
  }
  
  private SBMLDocument applyChanges(SEDMLDocument sedmlDoc, SBMLDocument sbmlDoc, org.jlibsedml.Model model)
		  throws SBMLException, XPathExpressionException, XMLStreamException, XMLException {
	  SedML sedml = sedmlDoc.getSedMLModel();
	  if (sedml.getModelWithId(model.getSource()) != null) {
		  sbmlDoc = applyChanges(sedmlDoc, sbmlDoc, sedml.getModelWithId(model.getSource()));
	  }
	  SBMLWriter Xwriter = new SBMLWriter();
	  SBMLReader Xreader = new SBMLReader();
	  sbmlDoc = Xreader
			  .readSBMLFromString(sedmlDoc.getChangedModel(model.getId(), Xwriter.writeSBMLToString(sbmlDoc)));
	  return sbmlDoc;
  }

  private void runSEDML(String sedML, AnalysisProperties properties, Run run, HashMap<String, String> userValues) throws Exception
  {
    File sedmlFile = new File(sedML);
    SEDMLDocument sedmlDoc = Libsedml.readDocument(sedmlFile);
    SedML sedml = sedmlDoc.getSedMLModel();
    List<AbstractTask> listOfTasks = sedml.getTasks();
    String root = properties.getRoot();
    for(AbstractTask task : listOfTasks)
    {
      /* Load from SED-ML */
      properties.setId(task.getId());
      
      org.jlibsedml.Model model = sedml.getModelWithId(task.getModelReference());
      String modelSource = sedml.getModelWithId(task.getModelReference()).getSource();
      while (sedml.getModelWithId(modelSource)!=null) {
        modelSource = sedml.getModelWithId(modelSource).getSource();
      }
      if (modelSource.indexOf("/")!=-1) {
    	  modelSource = modelSource.substring(modelSource.lastIndexOf("/")+1);
      }
      SBMLDocument sbmlDoc = SBMLReader.read(new File(root + File.separator + modelSource));
      if (model.getListOfChanges().size() != 0) {
    	  try {
    		  sbmlDoc = applyChanges(sedmlDoc, sbmlDoc, model);
    	  } catch (Exception e) {
    		  // TODO Auto-generated catch block
    		  e.printStackTrace();
    	  }
      } 
      SBMLWriter Xwriter = new SBMLWriter();
      Xwriter.write(sbmlDoc, root + File.separator + modelSource + "_");	
      
      properties.setModelFile(modelSource);
      AnalysisPropertiesLoader.loadSEDML(sedmlDoc, "", properties);
//      File analysisDir = new File(root + File.separator + task.getId());
      /* Replace values with properties given by user */
      loadUserValues(properties, userValues);
      File analysisDir = new File(properties.getDirectory());
      if (!analysisDir.exists()) 
      {
    	  new File(properties.getDirectory()).mkdir();
      }
      /* Flattening happens here */
      BioModel biomodel = new BioModel(root);
      biomodel.addObserver(this);
      biomodel.load(root + File.separator + modelSource + "_");
      SBMLDocument flatten = biomodel.flattenModel(true);
      String newFilename = root + File.separator + task.getId() + File.separator + modelSource;
      SBMLWriter.write(flatten, newFilename, ' ', (short) 2);
      AnalysisPropertiesWriter.createProperties(properties);
      run.execute();
    }
    for (Output output : sedml.getOutputs()) 
    {
      if (output.isPlot2d()) 
      {
//        GraphData.createTSDGraph(sedmlDoc,GraphData.TSD_DATA_TYPE,root,null,output.getId(),
//          root + File.separator + output.getId()+".png",GraphData.PNG_FILE_TYPE,650,400);
        GraphData.createTSDGraph(sedmlDoc,properties.getSimulationProperties().getPrinter_id(),root,null,output.getId(),
          properties.getDirectory() + File.separator + output.getId()+".png",GraphData.PNG_FILE_TYPE,650,400);
      } 
      else if (output.isReport()) 
      {
//        GraphData.createHistogram(sedmlDoc,GraphData.TSD_DATA_TYPE,root,null,output.getId(),
//          root + File.separator + output.getId()+".png",GraphData.PNG_FILE_TYPE,650,400);
        GraphData.createHistogram(sedmlDoc,properties.getSimulationProperties().getPrinter_id(),root,null,output.getId(),
          properties.getDirectory() + File.separator + output.getId()+".png",GraphData.PNG_FILE_TYPE,650,400);
      }
    }
  }



  private static void loadUserValues(AnalysisProperties properties, HashMap<String, String> userValues)
  {
    SimulationProperties simProperties = properties.getSimulationProperties();
    for(String key : userValues.keySet())
    {
      String value = userValues.get(key);

      if(key == ti)
      {
        simProperties.setInitialTime(Double.parseDouble(value));
      }
      else if(key == tl)
      {
        simProperties.setTimeLimit(Double.parseDouble(value));
      }
      else if(key == ot)
      {
        simProperties.setOutputStartTime(Double.parseDouble(value));
      }
      else if(key == pi)
      {
        simProperties.setPrintInterval(Double.parseDouble(value));
      }
      else if(key == m0)
      {
        simProperties.setMinTimeStep(Double.parseDouble(value));
      }
      else if(key == m1)
      {
        simProperties.setMaxTimeStep(Double.parseDouble(value));
      }
      else if(key == aErr)
      {
        simProperties.setAbsError(Double.parseDouble(value));
      }
      else if(key == rErr)
      {
        simProperties.setRelError(Double.parseDouble(value));
      }
      else if(key == sd)
      {
        simProperties.setRndSeed(Long.parseLong(value));
      }
      else if(key == r)
      {
        simProperties.setRun(Integer.parseInt(value));
      }
      else if(key == sim)
      {
        if(value.equals("ode"))
        {
          properties.setSim("rkf45");
        }
        else if(value.equals("hode"))
        {
          properties.setSim("Runge-Kutta-Fehlberg (Hierarchical)");
        }
        else if(value.equals("ssa"))
        {
          properties.setSim("gillespie");
        }
        else if(value.equals("hssa"))
        {
          properties.setSim("SSA-Direct (Hierarchical)");
        }
        else if(value.equals("dfba"))
        {
          properties.setSim("Mixed-Hierarchical");
        }
        else if(value.equals("jode"))
        {
          properties.setSim("Runge-Kutta-Fehlberg (Dynamic)");
        }
        else if(value.equals("jssa"))
        {
          properties.setSim("SSA-Direct (Dynamic)");
        }
        else
        {
          properties.setSim(value);
        }
      }
      else if(key == data)
      {
        if(value.equals("csv"))
        {
          simProperties.setPrinter_id(GraphData.CSV_DATA_TYPE);
        }
        else if(value.equals("tsd"))
        {
          simProperties.setPrinter_id(GraphData.TSD_DATA_TYPE);
        }
      }
    }
  }

  @Override
  public void update(Message message) {
    System.out.println(message.getMessage());

  }
}

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
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jlibsedml.AbstractTask;
import org.jlibsedml.Libsedml;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Task;
import org.jlibsedml.XMLException;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisPropertiesLoader;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * Command line method for running the analysis jar file.  
 * <p>
 * Requirements:
 * </p>
 * <p>
 * inputfile
 * </p>
 *
 *
 * @author Leandro Watanabe
 * @author Tramy Nguyen
 * @author Chris Myers
 * 
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Analysis {

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
  
  private static void usage() 
  {
    System.err.println("Description:");
    System.err.println("\t This application is used to run various analysis methods.");
    System.err.println("Usage:");
    System.err.println("\t java -jar [-d directory] input.xml");
    System.err.println("Required:");
    System.err.println("\t SBML model.");
    System.err.println("Options:\n");
    System.exit(1);
  }

  public static void main(String[] args) 
  {    
    try
    {
      String root = ".";
      String sedML = null;
      String propertiesFile = null;
      HashMap<String, String> propertiesMap = new HashMap<String, String>();
      
      if(args.length < 1)
      {
        usage();
      }

      // Last argument should be a SBML file
      if(!args[args.length - 1].endsWith(".xml"))
      {
        usage();
      }

      // Optional arguments should have a value
      if(args.length % 2  - 1 != 0)
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
          root = value;
          break;
        case "-p":
          if(value.endsWith(".xml") || value.endsWith(".sedml"))
          {
            sedML = value;
          }
          else if(value.endsWith(".properties"))
          {
            propertiesFile = value;
          }
          else
          {
            usage();
          }
          break;
        case "-ti":
          propertiesMap.put(ti, value);
          break;
        case "-tl":
          propertiesMap.put(tl, value);
          break;
        case "-ot":
          propertiesMap.put(ot, value);
          break;
        case "-pi":
          propertiesMap.put(pi, value);
          break;
        case "-m0":
          propertiesMap.put(m0, value);
          break;
        case "-m1":
          propertiesMap.put(m1, value);
          break;
        case "-aErr":
          propertiesMap.put(aErr, value);
          break;
        case "-rErr":
          propertiesMap.put(rErr, value);
          break;
        case "-sd":
          propertiesMap.put(sd, value);
          break;
        case "-r":
          propertiesMap.put(r, value);
          break;
        case "-sim":
          propertiesMap.put(sim, value);
          break;
        default:
          usage();
        }
      }


      final AnalysisProperties properties = new AnalysisProperties("", "", root, false);
      Run run = new Run(properties);

      if(sedML != null)
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
    catch(Exception e)
    {
      usage();
    }
  } 

  private static void runSEDML(String sedML, AnalysisProperties properties, Run run, HashMap<String, String> userValues) throws XMLException, IOException, XMLStreamException, InterruptedException, BioSimException
  {
    File sedmlFile = new File(sedML);
    SEDMLDocument sedmlDoc = Libsedml.readDocument(sedmlFile);
    SedML sedml = sedmlDoc.getSedMLModel();
    List<AbstractTask> listOfTasks = sedml.getTasks();

    for(AbstractTask task : listOfTasks)
    {
      properties.setId(task.getId());
      properties.setModelFile(task.getModelReference());
      AnalysisPropertiesLoader.loadSEDML(sedmlDoc, task.getId(), properties);
      //Replace values with properties given by user
      loadUserValues(properties, userValues);
      run.execute();
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
    }
  }
}

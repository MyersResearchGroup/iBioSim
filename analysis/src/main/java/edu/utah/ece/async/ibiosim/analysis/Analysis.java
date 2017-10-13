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
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * Command line method for running the analysis jar file.  
 * <p>
 * Requirements:
 * <p>
 * inputfile
 * <p>
 * 
 * Options:
 * <p>
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

	private static void usage() {
		System.err.println("Description:\n");
		System.err.println("\t This application is used to run various analysis methods.");
		System.err.println("Usage:\n");
		System.err.println("\t java -jar [-d directory] input.xml");
		System.err.println("Required:\n");
    System.err.println("\t A SED-ML file.");
		System.err.println("Options:\n");
		System.exit(1);
	}

	public static void main(String[] args) {
	
	  String root = ".";
	  
	  if(args.length < 1)
	  {
	    usage();
	  }
	  
	  // Last argument should be a SED-ML file
	  if(!args[args.length - 1].endsWith(".xml") || !args[args.length - 1].endsWith(".sedml"))
	  {
	    usage();
	  }
	  
	  // Optional arguments should have a value
	  if(args.length - 1 % 2 > 0)
	  {
	    usage();
	  }
	  
	  // Retrieve optional arguments
	  for(int i = 0; i < args.length - 1; i=i+1)
	  {
	    String arg = args[i];
	    String value = args[i+1];
	    
	    switch(arg)
	    {
	      case "-d":
	        root = value;
	        break;
	      default:
	        usage();
	    }
	  }
	   

    final AnalysisProperties properties = new AnalysisProperties("", "", root, false);

    Run run = new Run(properties);
	  String sedML = root + File.separator + args[args.length - 1];
    try 
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
        run.execute();
      }
      
    } 
    catch (XMLException e) 
    {
      System.err.println("XMLException");
    } 
    catch (IOException e) 
    {
      System.err.println("IOException.");
    }
    catch (XMLStreamException e) 
    {
      System.err.println("XMLStreamException.");
    } 
    catch (InterruptedException e) 
    {
      System.err.println("InterruptedException.");
    } 
    catch (BioSimException e) 
    {
      System.err.println("BioSimException.");
    }

	  
	} 
}

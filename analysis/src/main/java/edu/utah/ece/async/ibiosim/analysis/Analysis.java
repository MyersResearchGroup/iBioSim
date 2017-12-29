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
import java.text.ParseException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.jdom2.JDOMException;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Task;
import org.jlibsedml.XMLException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;

import com.lowagie.text.DocumentException;

import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisPropertiesLoader;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisPropertiesWriter;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.graphData.GraphData;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
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

	private static void usage() {
		System.err.println("Description:");
		System.err.println("\t This application is used to run various analysis methods.");
		System.err.println("Usage:");
		System.err.println("\t java -jar [-d directory] input.xml");
		System.err.println("Required:");
		System.err.println("\t A SED-ML file.");
		System.err.println("Options:\n");
		System.exit(1);
	}

	public static void main(String[] args) throws DocumentException, IOException {

		String root = ".";

		if(args.length < 1)
		{
			usage();
		}

		// Last argument should be a SED-ML file
		if(!args[args.length - 1].endsWith(".xml") && !args[args.length - 1].endsWith(".sedml")
				 && !args[args.length - 1].endsWith(".omex"))
		{
			usage();
		}

		// Optional arguments should have a value
		if((args.length - 1) % 2 > 0)
		{
			usage();
		}

		// Retrieve optional arguments
		for(int i = 0; i < args.length - 2; i=i+1)
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
		
		String sedML = root + File.separator + args[args.length - 1];
		if (sedML.endsWith(".omex")) {
			new File(root + File.separator + args[args.length - 1].replace(".omex", "")).mkdir();
			root = root + File.separator + args[args.length - 1].replace(".omex", "");
			System.out.println ("--- reading archive. ---");
			File archiveFile = new File (sedML);

			// read the archive stored in `archiveFile`
			CombineArchive ca;
			try {
				ca = new CombineArchive (archiveFile, true);
			}
			catch (JDOMException | ParseException | CombineArchiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			// read description of the archive itself
			System.out.println ("found " + ca.getDescriptions ().size ()
					+ " meta data entries describing the archive.");

			// iterate over all entries in the archive
			for (ArchiveEntry entry : ca.getEntries ())
			{
				if (entry.getFormat().toString().contains("sed-ml")) {
					sedML = root + File.separator + entry.getFileName();
				}
				entry.extractFile (new File(root + File.separator + entry.getFileName()));
			}
			ca.close();
		}

		Executables.checkExecutables();
		final AnalysisProperties properties = new AnalysisProperties("", "", root, false);
		Run run = new Run(properties);

		try 
		{
			File sedmlFile = new File(sedML);
			SEDMLDocument sedmlDoc = Libsedml.readDocument(sedmlFile);
			SedML sedml = sedmlDoc.getSedMLModel();
			List<AbstractTask> listOfTasks = sedml.getTasks();

			for(AbstractTask task : listOfTasks)
			{
				properties.setId(task.getId());
				String modelSource = sedml.getModelWithId(task.getModelReference()).getSource();
				while (sedml.getModelWithId(modelSource)!=null) {
					modelSource = sedml.getModelWithId(modelSource).getSource();
				}
				properties.setModelFile(modelSource);
				AnalysisPropertiesLoader.loadSEDML(sedmlDoc, null/* task.getId()*/, properties);
				File analysisDir = new File(root + File.separator + task.getId());
				if (!analysisDir.exists()) {
					new File(root + File.separator + task.getId()).mkdir();
				}
				BioModel biomodel = new BioModel(root);
				biomodel.load(root + File.separator + modelSource);
				SBMLDocument flatten = biomodel.flattenModel(true);
				String newFilename = root + File.separator + task.getId() + File.separator + modelSource;
				SBMLWriter.write(flatten, newFilename, ' ', (short) 2);
				AnalysisPropertiesWriter.createProperties(properties);
				run.execute();
			}
			for (Output output : sedml.getOutputs()) {
				if (output.isPlot2d()) {
					GraphData.createTSDGraph(sedmlDoc,GraphData.TSD_DATA_TYPE,root,null,output.getId(),
							root + File.separator + output.getId()+".png",GraphData.PNG_FILE_TYPE,650,400);
				} else if (output.isReport()) {
					GraphData.createHistogram(sedmlDoc,GraphData.TSD_DATA_TYPE,root,null,output.getId(),
							root + File.separator + output.getId()+".png",GraphData.PNG_FILE_TYPE,650,400);
				}
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

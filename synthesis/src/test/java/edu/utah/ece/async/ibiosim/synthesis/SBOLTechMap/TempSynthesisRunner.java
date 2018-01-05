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
package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLGraph;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Synthesis;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SynthesisNode;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.WeightedGraph;

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class TempSynthesisRunner
{ 	
	public static void main(String[] args)
	{
		String PATH = "/Users/tramyn/Desktop/sbol_gates/";
		String SPEC_FILE_NAME = "spec.sbol"; 
		String LIB_FILE_NAME = "LIBRARY_ANNOT.sbol";
		String defaultURIPrefix = "http://www.async.ece.utah.edu/";
		//Set up specification graph
		String path1 = PATH + SPEC_FILE_NAME;
		File SPEC_FILE = new File(path1);
		Synthesis syn = new Synthesis();
		syn.createSBOLGraph(path1, false, defaultURIPrefix);
//		syn.getSpecification().createDotFile(PATH + "SPEC2.dot");

		//Set up library graph
		String path2 = PATH + LIB_FILE_NAME;
		File LIBR_FILE = new File(path2);
		syn.createSBOLGraph(path2, true, defaultURIPrefix);

		//Set library gate scores
		List<SBOLGraph> library = syn.getLibrary();
		//TODO: make sure to comment the line of code below out if a different library file is used and write a different form of method to set the score for the graph
		setLibraryGateScores(library);
		Map<SynthesisNode, LinkedList<WeightedGraph>> matches = new HashMap<SynthesisNode, LinkedList<WeightedGraph>>();
		syn.match_topLevel(syn.getSpecification(), matches);
//		printMatches(matches);
		Map<SynthesisNode, SBOLGraph> solution = syn.cover_topLevel(syn.getSpecification(), matches);
//		syn.printCoveredGates(solution);
		try 
		{
			SBOLDocument sbolDoc = syn.getSBOLfromTechMapping(solution, syn.getSpecification(), GlobalConstants.SBOL_AUTHORITY_DEFAULT);
		}
		catch (SBOLValidationException e) 
		{
			e.printStackTrace();
		}
	} 

	public static void printMatches(Map<SynthesisNode, LinkedList<WeightedGraph>> matches)
	{
		for (Map.Entry<SynthesisNode, LinkedList<WeightedGraph>> entry : matches.entrySet())
		{
			for(WeightedGraph g: entry.getValue())
			{
				System.out.println(entry.getKey().toString() + "/" + g.getSBOLGraph().getOutputNode());
			}
		}
	}
	
	public static void setLibraryGateScores(List<SBOLGraph> library)
	{
		for(SBOLGraph g: library)
		{
			if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("INV0_md_S0"))
			{
				g.getOutputNode().setScore(5);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("INV1_md_S1"))
			{
				g.getOutputNode().setScore(15);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("INV2_md_S1"))
			{
				g.getOutputNode().setScore(15);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("INV3_md_S1"))
			{
				g.getOutputNode().setScore(20);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("NOR0_md_S2"))
			{
				g.getOutputNode().setScore(40);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("NOR1_md_S2"))
			{
				g.getOutputNode().setScore(45);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("NOR2_md_S2"))
			{
				g.getOutputNode().setScore(50);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("NOR3_md_S2"))
			{
				g.getOutputNode().setScore(50);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("X1_md_S3"))
			{
				g.getOutputNode().setScore(30);
			}	
		}
	}
}

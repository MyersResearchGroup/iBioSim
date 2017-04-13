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
package edu.utah.ece.async.backend.synthesis.techMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SystemsBiologyOntology;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLGraph
{
	private Map<URI, SynthesisNode> _nodes; 
	private List<SynthesisNode> _root; 
	int uniqueId = 0;
	private List<SynthesisNode> _topologicalSortNodes;

	public SBOLGraph()
	{
		_nodes = new HashMap<URI, SynthesisNode>();
		_root = new ArrayList<SynthesisNode>();
		_topologicalSortNodes = new ArrayList<SynthesisNode>();
	}

	/**
	 * Create an SBOLGraph for each SBOL ModuleDefinition where each FunctionalComponets represents the node 
	 * for the graph.
	 * @param md
	 */
	// TODO: SBO terms seem out-of-date here
	public void createGraph(ModuleDefinition md)
	{

		for(FunctionalComponent f : md.getFunctionalComponents())
		{
			if(!_nodes.containsKey(f.getIdentity()))
			{
				SynthesisNode node = new SynthesisNode(md, f, uniqueId++);
				_nodes.put(f.getIdentity(), node);
			}	
		}
		for(Interaction i : md.getInteractions())
		{
			URI type = i.getTypes().iterator().next();
			if(type.equals(SystemsBiologyOntology.GENETIC_SUPPRESSION))
			{
				List<URI> tf = new ArrayList<URI>();
				URI promoter = null; 
				for(Participation p : i.getParticipations())
				{
					URI role = p.getRoles().iterator().next();
					if(role.equals(SystemsBiologyOntology.PROMOTER))
					{
						promoter = p.getParticipantURI();
					}
					else
					{
						tf.add(p.getParticipantURI());
					}
				}
				for(URI u : tf)
				{
					addChild(u, promoter);
					addParent(u, promoter);
					addRelationship(promoter, u, SystemsBiologyOntology.GENETIC_SUPPRESSION);
				}
			}
			else if(type.equals(SystemsBiologyOntology.GENETIC_ENHANCEMENT))
			{
				List<URI> tf = new ArrayList<URI>();
				URI promoter = null; 
				for(Participation p : i.getParticipations())
				{
					URI role = p.getRoles().iterator().next();
					if(role.equals(SystemsBiologyOntology.PROMOTER))
					{
						promoter = p.getParticipantURI();
					}
					else
					{
						tf.add(p.getParticipantURI());
					}
				}
				for(URI u : tf)
				{
					addChild(u, promoter);
					addParent(u, promoter);
					addRelationship(promoter, u, SystemsBiologyOntology.GENETIC_ENHANCEMENT);
				}
			}
			else //SystemsBiologyOntology.GENETIC_PRODUCTION
			{
				List<URI> tf = new ArrayList<URI>();
				URI promoter = null; 
				for(Participation p : i.getParticipations())
				{
					URI role = p.getRoles().iterator().next();
					if(role.equals(SystemsBiologyOntology.PROMOTER))
					{
						promoter = p.getParticipantURI();
					}
					else
					{
						tf.add(p.getParticipantURI());
					}
				}
				for(URI u : tf)
				{
					addChild(promoter, u);
					addParent(promoter, u);
					addRelationship(promoter, u, SystemsBiologyOntology.GENETIC_PRODUCTION);
				}
			}
		}

		for(SynthesisNode node : _nodes.values())
		{
			if(node.isRoot())
			{
				_root.add(node);
			}
			node.setDegree(getDegree(node, node.getDegree()));
		}

	}
	
	/**
	 * Sort SBOLGraph in toplogical order. All leaves are printed first then it's parent node.
	 */
	public void topologicalSort()
	{
		List<SynthesisNode> sortedElements = new ArrayList<SynthesisNode>();
		Queue<SynthesisNode> unsortedElements = new LinkedList<SynthesisNode>();
		unsortedElements.addAll(getRoots());

		while(!unsortedElements.isEmpty())
		{
			SynthesisNode n = unsortedElements.poll();
			if(sortedElements.contains(n))
				continue;
			sortedElements.add(n);
			for(SynthesisNode m: n.getChildren()) 
			{
				if(m.getParents().size() == 1)
				{
					unsortedElements.add(m.getChildren().get(0));
					break;
				}
				else if(m.getParents().size() == 2)//assume 2 input into promoter
				{
					List<SynthesisNode> parentNodes = m.getParents(); 
					if(parentNodes.contains(n) && parentNodes.contains(unsortedElements.peek()))
					{
						SynthesisNode temp = unsortedElements.poll();
						sortedElements.add(temp);
						unsortedElements.add(m.getChildren().get(0));

					}
					else if(sortedElements.containsAll(parentNodes))
					{
						unsortedElements.add(m.getChildren().get(0));
					}
					else
					{
						unsortedElements.add(n);
					}
				}
			} //end of for loop
		} //end of while loop
		this._topologicalSortNodes = sortedElements;
	}
	
	/**
	 * Retrieves the root/output node of the SBOLGraph
	 * @return
	 */
	public SynthesisNode getOutputNode()
	{
		if(_topologicalSortNodes.size() <= 0)
			return null; 
		return _topologicalSortNodes.get(_topologicalSortNodes.size()-1);
	}

	/**
	 * Retrieve the leaf nodes of the SBOLGraph
	 * @return
	 */
	public List<SynthesisNode> getRoots()
	{
		return this._root;
	}

	private void addChild(URI parent, URI child)
	{
		SynthesisNode parentNode = _nodes.get(parent);
		SynthesisNode childNode = _nodes.get(child);
		parentNode.addChild(childNode);

	}

	private void addParent(URI parent, URI child)
	{
		SynthesisNode parentNode = _nodes.get(parent);
		SynthesisNode childNode = _nodes.get(child);
		childNode.addParent(parentNode);

	}

	private void addRelationship(URI parent, URI child, URI relation)
	{
		SynthesisNode parentNode = _nodes.get(parent);
		SynthesisNode childNode = _nodes.get(child);
		parentNode.addRelationship(childNode, relation);

	}

	/**
	 * Get all nodes within an SBOLGraph
	 * @return
	 */
	public List<SynthesisNode> getAllNodes()
	{
		List<SynthesisNode> nodes = new ArrayList<SynthesisNode>();
		for (Map.Entry<URI,SynthesisNode> entry : this._nodes.entrySet()) 
		{
			nodes.add(entry.getValue());
		}
		return nodes; 
	}

	private int getDegree(SynthesisNode node, int degree)
	{
		if(node.isLeaf())
		{
			return degree;
		}
		int max = 0; int temp;
		for(SynthesisNode child : node.getChildren())
		{
			temp = getDegree(child, degree);
			if(temp > max)
			{
				max = temp;
			}
		}
		return max + 1;
	}
	
	/**
	 * Retrieve a list of nodes sorted from calling toplogical sort on an SBOLGraph
	 * @return - The sorted species nodes of the SBOLGraph after calling topological sort
	 */
	public List<SynthesisNode> getTopologicalSortNodes()
	{
		return _topologicalSortNodes;
	}
	
	/**
	 * Generate a DOT file for the SBOLGraph
	 * @param filename - Name of DOT file to be produced
	 */
	public void createDotFile(String filename)
	{

		BufferedWriter output = null;
		try 
		{
			File file = new File(filename);
			output = new BufferedWriter(new FileWriter(file));
			output.write("digraph G {");

			for(URI uri : _nodes.keySet())
			{
				SynthesisNode node = _nodes.get(uri);
				for(SynthesisNode child : node.getChildren())
				{
					String parentId = node.toString();
					String childId = child.toString();
					output.write(parentId + " -> " + childId + ";");
				}
			}
			output.write("}");
		} 
		catch ( IOException e ) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			if ( output != null ) 
			{
				try
				{
					output.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public String toString()
	{
		return "SBOLGraph [Output node= " + getOutputNode().toString() + "]";
	}
	
	

}

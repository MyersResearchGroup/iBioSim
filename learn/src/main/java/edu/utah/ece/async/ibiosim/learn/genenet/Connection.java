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
package edu.utah.ece.async.ibiosim.learn.genenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A connection object is used to represent a potential influence from a set of influencers
 * to a particular species.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Connection
{

	private double				score;
	private String				child;
	private List<String>		parents;
	private Map<String, Type>	parentToType;

	private enum Type
	{
		ACTIVATOR("activator"), REPRESSOR("repressor"), UNKNOWN("unknown");

		private final String	name;

		Type(String name)
		{
			this.name = name;
		}

	}

	/**
	 * This object class is used to store the connections and their types among different species.
	 * Several connections may exist between two species. A score is used to distinguish the likelihood
	 * of each connection.
	 * 
	 * @param child - the influenced species.
	 * @param score - the likelihood of this connection existence.
	 * @param type - what kind of connection this is.
	 * @param parent - the influencing species.
	 */
	public Connection(String child, double score, String type, String parent)
	{
		this.child = child;
		this.score = score;
		this.parents = new ArrayList<String>();
		this.parents.add(parent);
		this.parentToType = new HashMap<String, Type>();
		this.parentToType.put(parent, getType(type));
	}
	
  /**
   * This object class is used to store the connections and their types among different species.
   * This is used when a child species is influenced by many species.
   * 
   * @param child - the influenced species.
   * @param score - the likelihood of this connection existence.
	 * @param connections - the different connections that influence a certain species.
	 */
	public Connection(String child, double score, List<Connection> connections)
	{
		this.child = child;
		this.score = score;
		this.parents = new ArrayList<String>();
		this.parentToType = new HashMap<String, Type>();

		for (Connection connection : connections)
		{
			this.parents.addAll(connection.getParents());

			for (String parent : connection.getParents())
			{
				this.parentToType.put(parent, getType(connection.getParentType(parent)));
			}
		}

	}

  /**
   * This object class is used to store the connections and their types among different species.
   * This is used when a child species is influenced by many species.
   * 
   * @param child - the influenced species.
   * @param score - the likelihood of this connection existence.
   * @param connections - the different connections that influence a certain species.
   */
	public Connection(String child, double score, Connection... connections)
	{
		this.child = child;
		this.score = score;
		this.parents = new ArrayList<String>();
		this.parentToType = new HashMap<String, Type>();

		for (Connection connection : connections)
		{
			this.parents.addAll(connection.getParents());

			for (String parent : connection.getParents())
			{
				this.parentToType.put(parent, getType(connection.getParentType(parent)));
			}
		}

	}

	
	private Type getType(String type)
	{
		type = type.toLowerCase();

		if (type.equals("repressor"))
		{
			return Type.REPRESSOR;
		}
		else if (type.equals("activator"))
		{
			return Type.ACTIVATOR;
		}
		else
		{
			return Type.UNKNOWN;
		}
	}

	/**
	 * Gets the score of this connection.
	 * 
	 * @return the score value.
	 */
	public double getScore()
	{
		return score;
	}

	/**
	 * Get the parent species (the one that is influencing) of this connection.
	 * 
	 * @return the parent species.
	 */
	public List<String> getParents()
	{
		return parents;
	}

	 /**
   * Get the child species (the one that is influenced by) of this connection.
   * 
   * @return the child species.
   */
	public String getChild()
	{
		return child;
	}

	/**
	 * Get the type of influence by the parent.
	 * 
	 * @param parent - the parent species.
	 * @return the type of influence.
	 */
	public String getParentType(String parent)
	{
		if (parentToType.containsKey(parent))
		{
			return parentToType.get(parent).name();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Check if this connection has all parents from a given list.
	 * 
	 * @param parents - the list of parents that needs to be checked whether they participate in this connection.
	 * @return true if connection has all the parents from given list. False otherwise.
	 */
	public boolean equalParents(List<String> parents)
	{
		for (String parent : parents)
		{
			if (!this.parents.contains(parent))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		return "Connection [score=" + score + ", child=" + child + ", parents=" + getParents() + "]";
	}

	/**
	 * Returns a string corresponding to the parent species of this connection.
	 * 
	 * @return the list of parents as a string.
	 */
	public String getParentString()
	{
		String list = "";

		for (String parent : parents)
		{
			list = list + parent + " ";
		}

		return list;
	}

}

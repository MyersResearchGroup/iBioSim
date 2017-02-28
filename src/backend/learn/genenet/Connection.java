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
package backend.learn.genenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author 
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

	public enum Type
	{
		ACTIVATOR("activator"), REPRESSOR("repressor"), UNKNOWN("unknown");

		private final String	name;

		Type(String name)
		{
			this.name = name;
		}

	}

	public Connection(String child, double score, String type, String parent)
	{
		this.child = child;
		this.score = score;
		this.parents = new ArrayList<String>();
		this.parents.add(parent);
		this.parentToType = new HashMap<String, Type>();
		this.parentToType.put(parent, getType(type));
	}

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

	public Type getType(String type)
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

	public double getScore()
	{
		return score;
	}

	public List<String> getParents()
	{
		return parents;
	}

	public String getChild()
	{
		return child;
	}

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

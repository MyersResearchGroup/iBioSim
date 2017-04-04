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
package edu.utah.ece.async.backend.learn.genenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class NetCon
{

	private Map<String, List<Connection>>	connections;

	public NetCon()
	{
		connections = new HashMap<String, List<Connection>>();
	}

	public boolean containEdge(String s)
	{
		return false;
	}

	public void addConnection(String child, String type, double score, String parent)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(new Connection(child, score, type, parent));
	}

	public void addConnection(String child, Connection connection)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(connection);
	}

	public void addConnection(String child, double score, Connection... collections)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(new Connection(child, score, collections));
	}

	public void removeConnection(String child, Connection connection)
	{
		if (connections.containsKey(child))
		{
			connections.get(child).remove(connection);
		}
	}

	public void removeConnectionByParent(String child, List<String> parents)
	{
		List<Connection> listOfConnections = connections.get(child);

		for (int i = listOfConnections.size() - 1; i >= 0; i--)
		{
			if (listOfConnections.get(i).equalParents(parents))
			{
				listOfConnections.remove(i);
			}
		}

	}

	public void removeConnection(String child, int index)
	{
		if (connections.containsKey(child))
		{
			connections.get(child).remove(index);
		}
	}

	public List<Connection> getListOfConnections(String s)
	{
		return connections.get(s);
	}

	public Map<String, List<Connection>> getConnections()
	{
		return connections;
	}

}

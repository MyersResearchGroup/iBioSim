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
 * A NetCon object holds the connections of the species.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class NetCon
{

	private Map<String, List<Connection>>	connections;

	/**
	 * Creates a NetCon object.
	 */
	public NetCon()
	{
		connections = new HashMap<String, List<Connection>>();
	}

	/**
	 * Checks if a species contains an edge.
	 * 
	 * @param s - Any arbitrary species in the design.
	 * @return true if the given species has an edge. False otherwise.
	 */
	public boolean containEdge(String s)
	{
		return false;
	}

	/**
	 * Adds a connection.
	 * 
	 * @param child - the influenced species.
	 * @param type - the type of influence.
	 * @param score - the score of the connection.
	 * @param parent - the influencing species.
	 */
	public void addConnection(String child, String type, double score, String parent)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(new Connection(child, score, type, parent));
	}

	/**
	 * Add a connection for a given species.
	 * 
	 * @param child - the species that has the connection.
	 * @param connection - the connection object containing information about the connection.
	 */
	public void addConnection(String child, Connection connection)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(connection);
	}

	/**
	 * Adds a collection of connections with a given score to a particular species.
	 * 
	 * @param child - the species that has the connection.
	 * @param score - the score of the connection.
	 * @param collections - a list of connections.
	 */
	public void addConnection(String child, double score, Connection... collections)
	{
		if (!connections.containsKey(child))
		{
			connections.put(child, new ArrayList<Connection>());
		}
		connections.get(child).add(new Connection(child, score, collections));
	}

	/**
	 * Remove a particular connection from a species.
	 * 
	 * @param child - the species that has the connection to be removed.
	 * @param connection - the connection to be removed.
	 */
	public void removeConnection(String child, Connection connection)
	{
		if (connections.containsKey(child))
		{
			connections.get(child).remove(connection);
		}
	}

	/**
	 * Removes connection of a species from a list of influencers.
	 * 
	 * @param child - the species that has the connection to be removed. 
	 * @param parents - the list of species that should have the connections removed from given species.
	 */
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

	/**
	 * Removes connection by index.
	 * 
	 * @param child - the species that has the connection to be removed. 
	 * @param index - the index of connection to be removed.
	 */
	public void removeConnection(String child, int index)
	{
		if (connections.containsKey(child))
		{
			connections.get(child).remove(index);
		}
	}

	/**
	 * Get connections of given species.
	 * 
	 * @param s - a specified species.
	 * @return a list of connections of the given species.
	 */
	public List<Connection> getListOfConnections(String s)
	{
		return connections.get(s);
	}

	/**
	 * Returns a map of all existing connections.
	 * 
	 * @return a map of all existing connections.
	 */
	public Map<String, List<Connection>> getConnections()
	{
		return connections;
	}

}

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
 * A SpeciesCollection object contains the interesting species that participate in the genetic circuit.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SpeciesCollection
{

	private List<String>			interestingSpecies;
	private Map<Integer, String>	columnSpecies;
	private Map<String, Integer>	speciesColumn;

	/**
	 * Creates a species collection object.
	 */
	public SpeciesCollection()
	{
		interestingSpecies = new ArrayList<String>();
		columnSpecies = new HashMap<Integer, String>();
		speciesColumn = new HashMap<String, Integer>();
	}

	/**
	 * Associates species with a particular column in the data table.
	 * 
	 * @param id - id of the species.
	 * @param index - index of the species in the data table.
	 */
	public void addSpecies(String id, int index)
	{
		speciesColumn.put(id, index);
		columnSpecies.put(index, id);
	}

	/**
	 * Adds interesting species to the collection.
	 * 
	 * @param id- id of the species.
	 */
	public void addInterestingSpecies(String id)
	{
		interestingSpecies.add(id);
	}

	/**
	 * Get column index of a given species
	 * 
	 * @param species - id of species.
	 * @return the column index.
	 */
	public int getColumn(String species)
	{
		return speciesColumn.get(species);
	}

	/**
	 * Gets species id from index.
	 * 
	 * @param index - index of species in data table.
	 * @return the id of the species.
	 */
	public String getInterestingSpecies(int index)
	{
		return interestingSpecies.get(index);
	}

	/**
	 * Returns the number of species in the collection.
	 * 
	 * @return the number of species in the collection.
	 */
	public int size()
	{
		return interestingSpecies.size();
	}

	/**
	 * Returns all interesting species.
	 * 
	 * @return list of interesting species.
	 */
	public List<String> getInterestingSpecies()
	{
		return interestingSpecies;
	}

	/**
	 * Checks if species is in the collection already.
	 * 
	 * @param species - id of species.
	 * @return true if species is present. False otherwise.
	 */
	public boolean containsSpeciesData(String species)
	{
	  return speciesColumn.containsKey(species);
	}
}

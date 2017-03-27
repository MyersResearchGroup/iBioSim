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
package edu.utah.ece.async.biomodel.network;

import edu.utah.ece.async.biomodel.visitor.VisitableSpecies;

/**
 * This interface describes a species. Species are nodes in the graph
 * 
 * @author Nam Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public interface SpeciesInterface extends VisitableSpecies {

	/**
	 * Returns the id of the species
	 * 
	 * @return the id of the species
	 */
	public String getId();
	/**
	 * Sets the id of the species
	 * 
	 * @param name
	 *            the name of the species
	 */
	public void setId(String name);

	/**
	 * Returns the name of the species
	 * 
	 * @return the name of the species
	 */
	public String getName();
	/**
	 * Sets the name of the species
	 * 
	 * @param name
	 *            the name of the species
	 */
	public void setName(String name);

	/**
	 * Returns the statename of the species
	 * 
	 * @return the statename of the species
	 */
	public String getStateName();

	/**
	 * Sets the state name of the species
	 * 
	 * @param stateName
	 *            the state name of the species
	 */
	public void setStateName(String stateName);
	
	/**
	 * Sets the properties of the specie
	 * @param properties the property to set
	 */
	//public void setProperties(Properties properties);
	
	/**
	 * Returns the property of the specie
	 * @return the property of the specie
	 */
	//public Properties getProperties();

	/**
	 * Adds a new value to the specie
	 * @param key the key of the value
	 * @param value the value to add
	 */
	//public void addProperty(String key, String value);
	
	/**
	 * Returns the property with the given key
	 * @param key the key of the property
	 * @return
	 */
	//public String getProperty(String key);
	
	public String getType();

	public void setType(String type);

	public void setDiffusible(boolean diffusible);

	public boolean isDiffusible();
	
	public double getInitialAmount();
	
	public void setInitialAmount(double amount);
	
	public double getInitialConcentration();
	
	public void setInitialConcentration(double concentration);
	
	public double getDecay();
	
	public void setDecay(double kd);
	
	public double getKo();
	
	public void setKo(double ko);
	
	public double getnp();
	
	public void setnp(double np);
	
	public double[] getKc();
	
	public void setKc(double kc_f,double kc_r);
	
	public double[] getKmdiff();

	public void setKmdiff(double kmdiff_f,double kmdiff_r);
	
	public double getKecdecay();
	
	public double getKecdiff();
	
	public boolean isActivator();
	
	public void setActivator(boolean set);
	
	public boolean isRepressor();
	
	public void setRepressor(boolean set);
	
	public boolean isAbstractable();
	
	public void setAbstractable(boolean set);
	
	public boolean isSequesterAbstractable();
	
	public void setSequesterAbstractable(boolean set);

	public boolean isSequesterable();
	
	public void setSequesterable(boolean set);
	
	public boolean isConvergent();
	
	public void setConvergent(boolean set);
	
}

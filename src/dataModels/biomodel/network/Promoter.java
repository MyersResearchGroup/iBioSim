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
package dataModels.biomodel.network;


import java.util.Collection;
import java.util.HashMap;

/**
 * This class describes a promoter
 * 
 * @author Nam
 * 
 */
public class Promoter {

	/**
	 * Constructor
	 * 
	 */
	public Promoter() {
		outputs = new HashMap<String, SpeciesInterface>();
		activationMap = new HashMap<String, Influence>();
		repressionMap = new HashMap<String, Influence>();
		activators = new HashMap<String, SpeciesInterface>();
		repressors = new HashMap<String, SpeciesInterface>();
	}
	
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setCompartment(String compartment) {
		this.compartment = compartment;
	}
	
	/**
	 * @return Returns the id.
	 */
	public String getCompartment() {
		return compartment;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	public double getInitialAmount() {
		return amount;
	}
	
	public void setInitialAmount(double amount) {
		this.amount = amount;
	}
	
	/**
	 * Creates a unique name
	 * 
	 */
	public void generatorUID() {
		id = "Promoter_" + uniqueID;
		uniqueID++;
	}
	
	/**
	 * @param specie
	 *            the species to add
	 */
	public void addOutput(String id, SpeciesInterface s) {
		outputs.put(id, s);
	}
	
	/**
	 * @return Returns the outputs.
	 */
	public Collection<SpeciesInterface> getOutputs() {
		return outputs.values();
	}

	/**
	 * @param outputs
	 *            The outputs to set.
	 */
	public void setOutputs(HashMap<String, SpeciesInterface> outputs) {
		this.outputs = outputs;
	}

	/**
	 * Adds reaction to list of reactions
	 * 
	 * @param reaction
	 *            reaction to add
	 */
	public void addToReactionMap(String id, Influence r) {
		if (r.getType().equals("tee")) {
			repressionMap.put(id, r);
		} else if (r.getType().equals("vee")) {
			activationMap.put(id, r);
		} else {
			//throw new IllegalArgumentException(
			//		"Reaction must be activating or repressing");
		}
	}
	
	public void addActivator(String id, SpeciesInterface s) {
		activators.put(id, s);
	}
	
	public void addRepressor(String id, SpeciesInterface s) {
		repressors.put(id, s);
	}

	/**
	 * @return Returns the activators.
	 */
	public Collection<SpeciesInterface> getActivators() {
		return activators.values();
	}
	
	/**
	 * @return Returns the repressors.
	 */
	public Collection<SpeciesInterface> getRepressors() {
		return repressors.values();
	}
	
	/**
	 * @return Returns the reactions.
	 */
	public Collection<Influence> getActivatingInfluences() {
		return activationMap.values();
	}
	
	/**
	 * @return Returns the repressingReactions.
	 */
	public Collection<Influence> getRepressingInfluences() {
		return repressionMap.values();
	}
	
	public HashMap<String, Influence> getActivationMap() {
		return activationMap;
	}
	
	public HashMap<String, Influence> getRepressionMap() {
		return repressionMap;
	}
	
	/*
	public double getPcount() {
		return Double.parseDouble(getProperty(GlobalConstants.PROMOTER_COUNT_STRING));
	}
	*/
	
	/**
	 * Get the activated, open complex (constitutive), and basal production
	 * rate constants
	 */
	public double getKact() {
		return ka;
	}

	public void setKact(double ka) {
		this.ka = ka;
	}

	public double getKbasal() {
		return kb;
	}

	public void setKbasal(double kb) {
		this.kb = kb;
	}
	
	public double getKoc() {
		return ko;
	}
	
	public void setKoc(double ko) {
		this.ko = ko;
	}
		
	/**
	 * Gets the production stoichiometry
	 */
	public double getStoich() {
		return np;
	}

	public void setStoich(double np) {
		this.np = np;
	}
	
	/**
	 * Gets the equilibrium constant for RNAP binding to an open promoter and RNAP binding to a promoter with
	 * an activator TF bound
	 */
	public double[] getKrnap() {
		/*
		if (getProperty(GlobalConstants.RNAP_BINDING_STRING)!=null) {
			String[] props = getProperty(GlobalConstants.RNAP_BINDING_STRING).split("/");
			double[] params = new double[props.length];
			for (int i = 0; i < props.length; i++)
				params[i] = Double.parseDouble(props[i]);
			return params;
		} else {
			double[] params = new double[1];
			params[0] = -1;
			return params;
		}
		*/
		return Ko;
	}
	
	public void setKrnap(double ko_f,double ko_r) {
		Ko = new double[2];
		Ko[0] = ko_f;
		Ko[1] = ko_r;
	}
		
	public double[] getKArnap() {
		/*
		if (getProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING)!=null) {
			String[] props = getProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).split("/");
			double[] params = new double[props.length];
			for (int i = 0; i < props.length; i++)
				params[i] = Double.parseDouble(props[i]);
			return params;
		} else {
			double[] params = new double[1];
			params[0] = -1;
			return params;
		}
		*/
		return Kao;
	}
	
	
	public void setKArnap(double kao_f,double kao_r) {
		Kao = new double[2];
		Kao[0] = kao_f;
		Kao[1] = kao_r;
	}

	
	/**
	 * @param reactions
	 *            The reactions to set.
	 */
	public void setActivatingInfluences(HashMap<String, Influence> activationMap) {
		this.activationMap = activationMap;
	}

	/**
	 * @param repressingReactions
	 *            The repressingReactions to set.
	 */
	public void setRepressingInfluences(HashMap<String, Influence> repressionMap) {
		this.repressionMap = repressionMap;
	}
	
	/*
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public void addProperties(Properties property) {
		if (this.properties == null) {
			this.properties = new Properties();			
		}
		for (Object s : property.keySet()) {
			this.properties.put(s.toString(), property.get(s.toString()));
		}
	}
	
	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new Properties();			
		}
		properties.put(key, value);
	}
	
	public String getProperty(String key) {
		if (properties == null || !properties.containsKey(key)) {
			return null;
		}
		return properties.get(key).toString();
	}
	*/
	
	//protected Properties properties = null;

	// id of promoter
	protected String id = "";
	
	protected String compartment = "";
	
	protected double amount;
	
	protected double ka;
	
	protected double kb;
	
	protected double ko;
	
	protected double np;
	
	protected double[] Ko;
	
	protected double[] Kao;

	// Outputs of promoter
	protected HashMap<String, SpeciesInterface> outputs;

	// List of reactions
	protected HashMap<String, Influence> activationMap;

	protected HashMap<String, Influence> repressionMap;
	
	protected HashMap<String, SpeciesInterface> activators;
	
	protected HashMap<String, SpeciesInterface> repressors;

	protected static int uniqueID = 0;

	
}

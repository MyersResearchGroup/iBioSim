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
package edu.utah.ece.async.ibiosim.synthesis.SBMLTechMap;

import java.net.URI;

/**
 * 
 *
 * @author Nicholas Roehner
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ChEBI {
	
	private ChEBI() {
		
	}
	
	/**
	 * Creates a new URI from the Chemical Entities of Biological Interest (CheBI)
	 * namespace with the given local name. For example, the function call
	 * <value>term("CHEBI_16991")</value> will return the 
	 * URI <value>http://purl.obolibrary.org/obo/CHEBI_16991</value>
	 */
	public static final URI type(String localName) {
		return NAMESPACE.resolve(localName);
	}

	/**
	 * Namespace of ChEBI 
	 * (<a href="http://purl.obolibrary.org/obo/">http://purl.obolibrary.org/obo/</a>).
	 */
	public static final URI NAMESPACE = URI.create("http://purl.obolibrary.org/obo/");

	/**
	 * 	High molecular weight, linear polymers, composed of nucleotides containing 
	 *  deoxyribose and linked by phosphodiester bonds; DNA contain the genetic 
	 *  information of organisms. 
	 * (<a href="http://purl.obolibrary.org/obo/CHEBI_16991">CHEBI_16991</a>).
	 */
	public static final URI DNA = type("CHEBI_16991");
	
	/**
	 * A biological macromolecule minimally consisting of one polypeptide chain 
	 * synthesized at the ribosome. 
	 * (<a href="http://purl.obolibrary.org/obo/CHEBI_36080">CHEBI_36080</a>).
	 */
	public static final URI PROTEIN = type("CHEBI_36080");
	
	/**
	 * Systems consisting of two or more molecular entities held together by 
	 * non-covalent interactions.
	 * (<a href="http://purl.obolibrary.org/obo/CHEBI_50967">CHEBI_50967</a>).
	 */
	public static final URI NON_COVALENTLY_BOUND_MOLECULAR_ENTITY = type("CHEBI_50967");
	
	/**
	 * A small molecule which increases (activator) or decreases (inhibitor) the 
	 * activity of an (allosteric) enzyme by binding to the enzyme at the regulatory 
	 * site (which is different from the substrate-binding catalytic site).
	 * (<a href="http://purl.obolibrary.org/obo/CHEBI_35224">CHEBI_35224</a>).
	 */
	public static final URI EFFECTOR = type("CHEBI_35224");

}

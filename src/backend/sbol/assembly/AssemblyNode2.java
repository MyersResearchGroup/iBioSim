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
package backend.sbol.assembly;

import java.net.URI;
import java.util.List;
import org.sbolstandard.core2.ComponentDefinition;

import dataModels.util.GlobalConstants;

public class AssemblyNode2 {

	private List<URI> sbolURIs;
	private List<ComponentDefinition> dnaComps;
	private String id;
	private String strand;
	
	public AssemblyNode2(List<URI> sbolURIs) {
//		this.id = id;
		this.sbolURIs = sbolURIs;
		this.strand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
	}
	
	public AssemblyNode2(List<URI> sbolURIs, String strand) {
		this.sbolURIs = sbolURIs;
		this.strand = strand;
	}
	
	public String getID() {
		return id;
	}
	
	public void setID(String id) {
		this.id = id;
	}
	
	public List<URI> getURIs() {
		return sbolURIs;
	}

	public List<ComponentDefinition> getDNAComponents() {
		return dnaComps;
	}
	
	public void setDNAComponents(List<ComponentDefinition> dnaComps) {
		this.dnaComps = dnaComps;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	public void setStrand(String strand) {
		this.strand = strand;
	}
	
	public String getStrand() {
		return strand;
	}
	
}

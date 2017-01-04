package sbol.assembly;

import java.net.URI;
import java.util.List;
import org.sbolstandard.core2.ComponentDefinition;
import biomodel.util.GlobalConstants;

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

package sbol;

//import java.net.URI;
import java.net.URI;
import java.util.List;

import org.sbolstandard.core.DnaComponent;

public class SBOLAssemblyNode {

	private List<URI> sbolURIs;
	private List<DnaComponent> dnaComps;
	private String id;
//	private List<SBOLAssemblyNode> nextNodes = new LinkedList<SBOLAssemblyNode>();
	
//	public SBOLAssemblyNode() {
//		
//	}
	
	public SBOLAssemblyNode(List<URI> sbolURIs) {
//		this.id = id;
		this.sbolURIs = sbolURIs;
	}
	
	public String getID() {
		return id;
	}
	
	public void setID(String id) {
		this.id = id;
	}
	
//	public String toString() {
//		return id;
//	}
	
	public List<URI> getURIs() {
		return sbolURIs;
	}
//	
//	public void addURI(URI uri) {
//		sbolURIs.add(uri);
//	}
//	
//	public void setURIs(List<URI> sbolURIs) {
//		this.sbolURIs = sbolURIs;
//	}
//	
	public List<DnaComponent> getDNAComponents() {
		return dnaComps;
	}
	
	public void setDNAComponents(List<DnaComponent> dnaComps) {
		this.dnaComps = dnaComps;
	}
	
//	public void addNextNode(SBOLAssemblyNode nextNode) {
//		nextNodes.add(nextNode);
//	}
//	
//	public void setNextNodes(List<SBOLAssemblyNode> nextNodes) {
//		this.nextNodes = nextNodes;
//	}
//	
//	public List<SBOLAssemblyNode> getNextNodes() {
//		return nextNodes;
//	}
}

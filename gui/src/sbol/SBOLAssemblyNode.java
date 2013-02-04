package sbol;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbolstandard.core.DnaComponent;

public class SBOLAssemblyNode {

	private List<URI> sbolURIs;
	private List<DnaComponent> dnaComps = new LinkedList<DnaComponent>();
	private String id;
	private List<SBOLAssemblyNode> nextNodes = new LinkedList<SBOLAssemblyNode>();
//	private SBOLSynthesizer sbolSynth;
//	private boolean visited;
	
	public SBOLAssemblyNode(String id) {
		this.id = id;
	}
	
	public SBOLAssemblyNode(String id, List<URI> sbolURIs) {
		this.id = id;
		this.sbolURIs = sbolURIs;
//		visited = false;
	}
	
//	public SBOLSynthesisNode(String id, List<URI> sbolURIs, SBOLSynthesizer sbolSynth) {
//		this.id = id;
//		this.sbolURIs = sbolURIs;
//		this.sbolSynth = sbolSynth;
//		visited = false;
//	}
	
	public String getID() {
		return id;
	}
	
	public void setID(String id) {
		this.id = id;
	}
	
	public List<URI> getURIs() {
		return sbolURIs;
	}
	
	public void addURI(URI uri) {
		sbolURIs.add(uri);
	}
	
	public void setURIs(List<URI> sbolURIs) {
		this.sbolURIs = sbolURIs;
	}
	
	public List<DnaComponent> getDNAComponents() {
		return dnaComps;
	}
	
	public void setDNAComponents(List<DnaComponent> dnaComps) {
		this.dnaComps = dnaComps;
	}
	
//	public SBOLSynthesizer getSynthesizer() {
//		return sbolSynth;
//	}
//	
//	public void setSynthesizer(SBOLSynthesizer sbolSynth) {
//		this.sbolSynth = sbolSynth;
//	}
	
	public void addNextNode(SBOLAssemblyNode nextNode) {
		nextNodes.add(nextNode);
	}
	
	public void setNextNodes(List<SBOLAssemblyNode> nextNodes) {
		this.nextNodes = nextNodes;
	}
	
	public List<SBOLAssemblyNode> getNextNodes() {
		return nextNodes;
	}
	
//	public boolean isVisited() {
//		return visited;
//	}
//	
//	public void setVisited(boolean visited) {
//		this.visited = visited;
//	}
}

package biomodel.network;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.sbolstandard.core.DnaComponent;

import sbol.SBOLSynthesizer;

public class SynthesisNode {
	
	private LinkedList<URI> sbolURIs;
	private LinkedList<DnaComponent> dnaComps = new LinkedList<DnaComponent>();
	private String id;
	private Set<SynthesisNode> nextNodes = new HashSet<SynthesisNode>();
	private SBOLSynthesizer sbolSynth;
	private boolean visited;
	
	public SynthesisNode(String id, LinkedList<URI> sbolURIs) {
		this.id = id;
		this.sbolURIs = sbolURIs;
		visited = false;
	}
	
	public SynthesisNode(String id, LinkedList<URI> sbolURIs, SBOLSynthesizer sbolSynth) {
		this.id = id;
		this.sbolURIs = sbolURIs;
		this.sbolSynth = sbolSynth;
		visited = false;
	}
	
	public String getID() {
		return id;
	}
	
	public void setID(String id) {
		this.id = id;
	}
	
	public LinkedList<URI> getSbolURIs() {
		return sbolURIs;
	}
	
	public void addSbolURI(URI uri) {
		sbolURIs.add(uri);
	}
	
	public void setSbolURIs(LinkedList<URI> sbolURIs) {
		this.sbolURIs = sbolURIs;
	}
	
	public LinkedList<DnaComponent> getDNAComponents() {
		return dnaComps;
	}
	
	public void setDNAComponents(LinkedList<DnaComponent> dnaComps) {
		this.dnaComps = dnaComps;
	}
	
	public SBOLSynthesizer getSynthesizer() {
		return sbolSynth;
	}
	
	public void setSynthesizer(SBOLSynthesizer sbolSynth) {
		this.sbolSynth = sbolSynth;
	}
	
	public void addNextNode(SynthesisNode nextNode) {
		nextNodes.add(nextNode);
	}
	
	public Set<SynthesisNode> getNextNodes() {
		return nextNodes;
	}
	
	public boolean isVisited() {
		return visited;
	}
	
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	

}

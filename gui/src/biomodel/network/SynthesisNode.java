package biomodel.network;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import sbol.SBOLSynthesizer;

public class SynthesisNode {
	
	private LinkedList<String> sbolURIs;
	private String id;
	private Set<SynthesisNode> nextNodes = new HashSet<SynthesisNode>();
	private SBOLSynthesizer sbolSynth;
	
	public SynthesisNode(String id, LinkedList<String> sbolURIs) {
		this.id = id;
		this.sbolURIs = sbolURIs;
	}
	
	public SynthesisNode(String id, LinkedList<String> sbolURIs, SBOLSynthesizer sbolSynth) {
		this.id = id;
		this.sbolURIs = sbolURIs;
		this.sbolSynth = sbolSynth;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public LinkedList<String> getSbolURIs() {
		return sbolURIs;
	}
	
	public void addSbolURI(String uri) {
		sbolURIs.add(uri);
	}
	
	public void setSbolURIs(LinkedList<String> sbolURIs) {
		this.sbolURIs = sbolURIs;
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
	

}

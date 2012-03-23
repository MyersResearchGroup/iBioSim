package biomodel.network;

import java.util.HashSet;
import java.util.Set;

public class SynthesisNode {
	
	private String sbolURI;
	private String id;
	private Set<SynthesisNode> nextNodes = new HashSet<SynthesisNode>();
	
	public SynthesisNode(String id) {
		this.id = id;
	}
	
	public SynthesisNode(String id, String sbolURI) {
		this.id = id;
		this.sbolURI = sbolURI;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getSbolURI() {
		return sbolURI;
	}
	
	public void setSbolURI(String sbolURI) {
		this.sbolURI = sbolURI;
	}
	
	public void addNextNode(SynthesisNode nextNode) {
		nextNodes.add(nextNode);
	}
	
	public Set<SynthesisNode> getNextNodes() {
		return nextNodes;
	}
	

}

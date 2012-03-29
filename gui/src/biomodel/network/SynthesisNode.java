package biomodel.network;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class SynthesisNode {
	
	private LinkedList<String> sbolURIs;
	private String id;
	private Set<SynthesisNode> nextNodes = new HashSet<SynthesisNode>();
	
	public SynthesisNode(String id) {
		this.id = id;
		this.sbolURIs = new LinkedList<String>();
	}
	
	public SynthesisNode(String id, LinkedList<String> sbolURIs) {
		this.id = id;
		this.sbolURIs = sbolURIs;
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
	
	public void setSbolURIs(LinkedList<String> sbolURIs) {
		this.sbolURIs = sbolURIs;
	}
	
	public void addNextNode(SynthesisNode nextNode) {
		nextNodes.add(nextNode);
	}
	
	public Set<SynthesisNode> getNextNodes() {
		return nextNodes;
	}
	

}

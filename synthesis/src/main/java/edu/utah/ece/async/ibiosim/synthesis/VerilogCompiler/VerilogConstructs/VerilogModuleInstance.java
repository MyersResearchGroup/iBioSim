package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogModuleInstance implements AbstractVerilogConstruct {

	private String submoduleId, moduleReference;
	private Map<String, String> namedConnections;
	private List<String> orderedConnections;
	
	public VerilogModuleInstance() {
		this.namedConnections = new HashMap<>();
		this.orderedConnections = new ArrayList<>();
	}
	
	public void addNamedConnection(String wire, String portName) {
		this.namedConnections.put(wire, portName);
	}
	
	public void addOrderedConnections(String portName) {
		this.orderedConnections.add(portName);
	}
	
	public void setSubModuleId(String moduleId) {
		this.submoduleId = moduleId;
	}
	
	public void setModuleReference(String moduleReference) {
		this.moduleReference = moduleReference;
	}

	/**
	 * Get instantiated name of the submodule referenced.
	 * @return
	 */
	public String getSubmoduleId() {
		return this.submoduleId;
	}
	
	/**
	 * Get the referenced submodule id.
	 * @return
	 */
	public String getModuleReference() {
		return this.moduleReference;
	}
	
	public Map<String, String> getNamedConnections() {
		return this.namedConnections;
	}
	
	public int getNumNamedConnections() {
		return this.namedConnections.size();
	}
	
	public List<String> getOrderedConnections() {
		return this.orderedConnections;
	}
	
	@Override
	public void addConstruct(AbstractVerilogConstruct construct) { }

}

package VerilogConstructs;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogModule implements AbstractVerilogConstruct {

	private String moduleId;
	private List<VerilogAssignment> variableAssignments;
	private List<String> wirePorts;
	private List<String> inputPorts;
	private List<String> outputPorts;
	private List<String> registers;
	
	private List<VerilogModuleInstance> submodules;
	private List<VerilogInitialBlock> initialBlocks;
	private List<VerilogAlwaysBlock> alwaysBlocks;
	
	private List<String> orderedPorts;
	private List<VerilogAssignment> contAssign;
	
	public VerilogModule() {
		this.wirePorts = new ArrayList<>();
		this.inputPorts = new ArrayList<>();
		this.outputPorts = new ArrayList<>();
		this.registers = new ArrayList<>();
		this.submodules = new ArrayList<>();
		this.initialBlocks = new ArrayList<>();
		this.alwaysBlocks = new ArrayList<>();
		this.orderedPorts = new ArrayList<>();
		this.contAssign = new ArrayList<>();
	}
	
	public void setId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	public String getModuleId() {
		return this.moduleId;
	}
	
	public void addWire(String wireName) {
		this.wirePorts.add(wireName);
	}
	
	public void addInputPort(String input) {
		this.inputPorts.add(input);
		this.orderedPorts.add(input);
	}
	
	public void addOutputPort(String output) {
		this.outputPorts.add(output);
		this.orderedPorts.add(output);
	}
	
	public void addRegister(String register) {
		this.registers.add(register);
	}
	
	public void addContinousAssignment(VerilogAssignment assignment) {
		this.contAssign.add(assignment);
	}
	
	public VerilogInitialBlock createInitialBlock() {
		VerilogInitialBlock initialBlock = new VerilogInitialBlock();
		this.initialBlocks.add(initialBlock);
		return initialBlock;
	}
	
	public VerilogAlwaysBlock createAlwayBlock() {
		VerilogAlwaysBlock alwayBlock = new VerilogAlwaysBlock();
		this.alwaysBlocks.add(alwayBlock);
		return alwayBlock;
	}
	
	public VerilogAssignment createVerilogAssignment() {
		VerilogAssignment assignment = new VerilogAssignment();
		this.variableAssignments.add(assignment);
		return assignment;
	}
	
	public VerilogModuleInstance createVerilogModuleInstance() {
		VerilogModuleInstance instance = new VerilogModuleInstance();
		this.submodules.add(instance);
		return instance;
	}
	
	public List<String> getWirePorts() {
		return this.wirePorts;
	}
	
	public List<String> getInputPorts() {
		return this.inputPorts;
	}
	
	public List<String> getOutputPorts() {
		return this.outputPorts;
	}
	
	public List<String> getRegisters() {
		return this.registers;
	}
	
	public List<VerilogAssignment> getContinousAssignments() {
		return this.contAssign;
	}
	
	public List<VerilogModuleInstance> getSubmodules() {
		return this.submodules;
	}
	
	public List<VerilogInitialBlock> getInitialBlockList() {
		return this.initialBlocks;
	}

	public List<VerilogAlwaysBlock> getAlwaysBlockList() {
		return this.alwaysBlocks;
	}
	
	public VerilogModuleInstance getSubmodule(int index) {
		if(this.submodules != null && this.submodules.size() > 0) {
			return this.submodules.get(index);
		}
		return null;
	}
	
	public VerilogBlock getInitialBlock(int index) {
		if(this.initialBlocks != null && this.initialBlocks.size() > 0) {
			return (VerilogBlock) this.initialBlocks.get(index).getBlock();
		}
		return  null;
	}
	
	public VerilogBlock getAlwaysBlock(int index) {
		if(this.alwaysBlocks != null && this.alwaysBlocks.size() > 0) {
			return (VerilogBlock) this.alwaysBlocks.get(index).getBlock();
		}
		return null;
	}
	
	public VerilogAssignment getContinuousAssignment(int index) {
		if(!this.contAssign.isEmpty()) {
			return this.contAssign.get(index);
		}
		return null;
	}
	
	public int getNumSubmodules() {
		return this.submodules != null ? this.submodules.size() : null;
	}
	
	public int getNumInitialBlock() {
		return this.initialBlocks != null ? this.initialBlocks.size() : null;
	}
	
	public int getNumAlwaysBlock() {
		return this.alwaysBlocks != null ? this.alwaysBlocks.size() : null;
	}
	
	public int getNumWires() {
		return this.wirePorts != null ? this.wirePorts.size() : null;
	}
	
	public int getNumRegisters() {
		return this.registers != null ? this.registers.size() : null;
	}
	
	public int getNumOutputs() {
		return this.outputPorts != null ? this.outputPorts.size() : null;
	}
	
	public int getNumInputs() {
		return this.inputPorts != null ? this.inputPorts.size() : null;
	}
	
	public int getNumContinousAssignments() {
		return this.contAssign != null ? this.contAssign.size() : null;
	}
	
	@Override
	public void addConstruct(AbstractVerilogConstruct construct) { }

}

package backend.verification.platu.platuLpn.io;

import java.util.List;


public class Instance {
	private String name = "";
	private String lpnLabel = null;
	private List<String> variableList = null;
	private List<String> moduleList = null;
	
	public Instance(String lpnLabel, String name, List<String> varList, List<String> modList){
		this.name = name;
		this.lpnLabel = lpnLabel;
		this.variableList = varList;
		this.moduleList = modList;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getLpnLabel(){
		return this.lpnLabel;
	}
	
	public List<String> getVariableList(){
		return this.variableList;
	}
	
	public List<String> getModuleList(){
		return this.moduleList;
	}
}

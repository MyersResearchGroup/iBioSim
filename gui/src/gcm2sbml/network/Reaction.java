package gcm2sbml.network;

import gcm2sbml.util.GlobalConstants;

import java.util.Properties;

/**
 * This class describes a reaction.  A reaction is a binding between
 * a species and a promoter.
 * @author Nam
 *
 */
public class Reaction {	
	
	
	/**
	 * @return Returns the productionConstant.
	 */
	public double getProductionConstant() {
		return productionConstant;
	}
	/**
	 * @param productionConstant The productionConstant to set.
	 */
	public void setProductionConstant(double productionConstant) {
		this.productionConstant = productionConstant;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Returns the inputState.
	 */
	public String getInput() {
		return inputState;
	}
	/**
	 * @param inputState The inputState to set.
	 */
	public void setInput(String inputState) {
		this.inputState = inputState;
	}
	/**
	 * @return Returns the outputState.
	 */
	public String getOutput() {
		return outputState;
	}
	/**
	 * @param outputState The outputState to set.
	 */
	public void setOutput(String outputState) {
		this.outputState = outputState;
	}
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Generates a unique name
	 *
	 */
	public void generateName() {
		this.name = "Reaction_" + uid;
		uid++;
	}

	public boolean isBiochemical() {
		return isBiochemical;
	}
	
	public void setBiochemical(boolean isBiochemical) {
		this.isBiochemical = isBiochemical;
	}
	
	/**
	 * @return Returns the dimer.
	 */
	public double getCoop() {
		return Double.parseDouble(getProperty(GlobalConstants.COOPERATIVITY_STRING));
	}
	
	public double getDimer() {
		return Double.parseDouble(getProperty(GlobalConstants.MAX_DIMER_STRING));
	}
	
	public double getKbio() {
		return Double.parseDouble(getProperty(GlobalConstants.KBIO_STRING));
	}
	
	public double[] getAct() {
		String[] props = getProperty(GlobalConstants.KACT_STRING).split("/");
		double[] params = new double[props.length];
		for (int i = 0; i < props.length; i++)
			params[i] = Double.parseDouble(props[i]);
		return params;
	}
	
	public double[] getRep() {
		String[] props = getProperty(GlobalConstants.KREP_STRING).split("/");
		double[] params = new double[props.length];
		for (int i = 0; i < props.length; i++)
			params[i] = Double.parseDouble(props[i]);
		return params;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new Properties();			
		}
		properties.put(key, value);
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}	
	
	private static int uid = 0;
	
	//The input state
	private String inputState = null;
	//The output state
	private String outputState = null;
	//Activation or repression
	private String type = "";
	//The name of the reaction
	private String name = null;		
	//The production constant
	private double productionConstant = 0.0001;
	//Biochemical reaction
	private boolean isBiochemical = false;
	
	
	//Number property
	private Properties properties = null;

}

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
	 * @return Returns the bindingConstant.
	 */
	public double getBindingConstant() {
		if (getType().equals("vee") && getProperty(GlobalConstants.KACT_STRING) != null) {
			return Double.parseDouble(getProperty(GlobalConstants.KACT_STRING));
		} else if (getType().equals("tee") && getProperty(GlobalConstants.KREP_STRING) != null) {
			return Double.parseDouble(getProperty(GlobalConstants.KREP_STRING));
		}
		return bindingConstant;
	}
	
	/**
	 * @param bindingConstant The bindingConstant to set.
	 */
	public void setBindingConstant(double bindingConstant) {
		this.bindingConstant = bindingConstant;
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
	public String getInputState() {
		return inputState;
	}
	/**
	 * @param inputState The inputState to set.
	 */
	public void setInputState(String inputState) {
		this.inputState = inputState;
	}
	/**
	 * @return Returns the outputState.
	 */
	public String getOutputState() {
		return outputState;
	}
	/**
	 * @param outputState The outputState to set.
	 */
	public void setOutputState(String outputState) {
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
	
	/**
	 * @return Returns the coop.
	 */
	public double getCoop() {
		if (getProperty(GlobalConstants.COOPERATIVITY_STRING) != null) {
			return Double.parseDouble(getProperty(GlobalConstants.COOPERATIVITY_STRING));
		}
		return coop;
	}
	/**
	 * @param coop The coop to set.
	 */
	public void setCoop(double coop) {
		this.coop = coop;
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
	public int getDimer() {
		return dimer;
	}
	/**
	 * @param dimer The dimer to set.
	 */
	public void setDimer(int dimer) {
		this.dimer = dimer;
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
		if (properties == null || !properties.contains(key)) {
			return null;
		}
		return properties.get(key).toString();
	}	
	
	private static int uid = 0;
	
	//The input state
	private String inputState = null;
	//The output state
	private String outputState= null;
	//Binding constant
	private double bindingConstant = -1;
	//Activation or repression
	private String type = "";
	//The name of the reaction
	private String name = null;		
	//The production constant
	private double productionConstant = -1;
	//Cooperativity factor
	private double coop = 1;
	//dimer value 
	private int dimer = 0;	
	//Biochemical reaction
	private boolean isBiochemical = false;
	
	//Number property
	private Properties properties = null;

}

package gcm2sbml.network;

import java.util.Properties;

/**
 * This class describes a reaction.  A reaction is a binding between
 * a species and a promoter.
 * @author Nam
 *
 */
public class Reaction {	
	
	
	
	/**
	 * @return the stateProperties
	 */
	public Properties getStateProperties() {
		return stateProperties;
	}
	/**
	 * @param stateProperties the stateProperties to set
	 */
	public void setStateProperties(Properties stateProperties) {
		this.stateProperties = stateProperties;
	}
	/**
	 * @return the labelProperties
	 */
	public Properties getLabelProperties() {
		return labelProperties;
	}
	/**
	 * @param labelProperties the labelProperties to set
	 */
	public void setLabelProperties(Properties labelProperties) {
		this.labelProperties = labelProperties;
	}
	/**
	 * @return the numberProperties
	 */
	public Properties getNumberProperties() {
		return numberProperties;
	}
	/**
	 * @param numberProperties the numberProperties to set
	 */
	public void setNumberProperties(Properties numberProperties) {
		this.numberProperties = numberProperties;
	}
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
	
//	public static final String ACTIVATION = "vee";
//	public static final String REPRESSION = "tee";
//	public static final String ARROWHEAD = "arrhowhead";
//	public static final String TYPE = "type";
//	public static final String PROMOTER = "promoter";
//	public static final String COOP = "coop";
//	public static final String DIMER = "label";
//	public static final String KBINDING = "binding";
//	public static final String KRNAP = "rnap_binding";
//	public static final String KBIO = "kbio";
//	public static final String OCR = "ocr";
//	public static final String BASAL = "basal";
//	public static final String ACTIVATED = "activated";
	
	//State properties, only have a finite set of values
	private Properties stateProperties = null;
	
	//Arbitrary alphanumeric property
	private Properties labelProperties = null;
	
	//Number property
	private Properties numberProperties = null;

}

package gcm2sbml.parser;

import gcm2sbml.util.GlobalConstants;

import java.util.HashMap;

public class CompatibilityFixer {

	public static String getGuiName(String id) {
		return guiName.get(id);
	}
	
	public static String getSBMLName(String id) {
		return sbmlName.get(id);
	}
	
	public static String convertSBMLName(String id) {
		for (String s : sbmlName.keySet()) {
			if (sbmlName.get(s).equals(id)) {
				return s;
			}
		}
		return null;
	}
	
	public static String getGCMName(String guiid) {
		return gcmName.get(guiid);
	}
	
	private static HashMap<String, String> guiName = null;
	private static HashMap<String, String> sbmlName = null;
	private static HashMap<String, String> gcmName = null;
	
	public final static String GUI_NAME_STRING = "ID";
	public final static String SBML_NAME_STRING = "";
		
	public final static String GUI_INITIAL_STRING = "Initial species count";
	public final static String SBML_INITIAL_STRING = "ns";
	
	public final static String GUI_KBIO_STRING = "Biochemical equilibrium";
	public final static String SBML_KBIO_STRING = "Kb";

	public final static String GUI_MAX_DIMER_STRING = "N-mer as transcription factor";
	public final static String SBML_MAX_DIMER_STRING = "nd";
	
	public final static String GUI_KASSOCIATION_STRING = "Dimerization equilibrium";
	public final static String SBML_KASSOCIATION_STRING = "Kd";

	public final static String GUI_KDECAY_STRING = "Degradation rate";
	public final static String SBML_KDECAY_STRING = "kd";
	
	public final static String GUI_PROMOTER_COUNT_STRING = "Initial promoter count";
	public final static String SBML_PROMOTER_COUNT_STRING = "ng";

	public final static String GUI_RNAP_STRING = "Initial RNAP count";
	public final static String SBML_RNAP_STRING = "nr";

	public final static String GUI_STOICHIOMETRY_STRING = "Stoichiometry of production";
	public final static String SBML_STOICHIOMETRY_STRING = "np";

	public final static String GUI_COOPERATIVITY_STRING = "Degree of cooperativity";
	public final static String SBML_COOPERATIVITY_STRING = "nc";

	public final static String GUI_RNAP_BINDING_STRING = "RNAP binding equilibrium";
	public final static String SBML_RNAP_BINDING_STRING = "Ko";

	public final static String GUI_OCR_STRING = "Open complex production rate";
	public final static String SBML_OCR_STRING = "ko";

	public final static String GUI_KBASAL_STRING = "Basal production rate";
	public final static String SBML_KBASAL_STRING = "kb";

	public final static String GUI_ACTIVED_STRING = "Activated production rate";
	public final static String SBML_ACTIVED_STRING = "ka";

	public final static String GUI_KREP_STRING = "Repression binding equilibrium";
	public final static String SBML_KREP_STRING = "Kr";
	
	public final static String GUI_KACT_STRING = "Activation binding equilibrium";
	public final static String SBML_KACT_STRING = "Ka";
	
	//Static initializer that sets up the hashing 
	static {		
		guiName = new HashMap<String, String>();
		sbmlName = new HashMap<String, String>();
		gcmName = new HashMap<String, String>();
		
		//guiName.put(GlobalConstants.NAME, GUI_NAME_STRING);
		//sbmlName.put(GlobalConstants.NAME, SBML_NAME_STRING);
		
		guiName.put(GlobalConstants.INITIAL_STRING, GUI_INITIAL_STRING);
		sbmlName.put(GlobalConstants.INITIAL_STRING, SBML_INITIAL_STRING);
		
		guiName.put(GlobalConstants.KBIO_STRING, GUI_KBIO_STRING);
		sbmlName.put(GlobalConstants.KBIO_STRING, SBML_KBIO_STRING);

		guiName.put(GlobalConstants.MAX_DIMER_STRING, GUI_MAX_DIMER_STRING);
		sbmlName.put(GlobalConstants.MAX_DIMER_STRING, SBML_MAX_DIMER_STRING);
		
		guiName.put(GlobalConstants.KASSOCIATION_STRING, GUI_KASSOCIATION_STRING);
		sbmlName.put(GlobalConstants.KASSOCIATION_STRING, SBML_KASSOCIATION_STRING);
		
		guiName.put(GlobalConstants.KDECAY_STRING, GUI_KDECAY_STRING);
		sbmlName.put(GlobalConstants.KDECAY_STRING, SBML_KDECAY_STRING);
		
		guiName.put(GlobalConstants.PROMOTER_COUNT_STRING, GUI_PROMOTER_COUNT_STRING);
		sbmlName.put(GlobalConstants.PROMOTER_COUNT_STRING, SBML_PROMOTER_COUNT_STRING);
		
		guiName.put(GlobalConstants.RNAP_STRING, GUI_RNAP_STRING);
		sbmlName.put(GlobalConstants.RNAP_STRING, SBML_RNAP_STRING);

		guiName.put(GlobalConstants.STOICHIOMETRY_STRING, GUI_STOICHIOMETRY_STRING);
		sbmlName.put(GlobalConstants.STOICHIOMETRY_STRING, SBML_STOICHIOMETRY_STRING);

		guiName.put(GlobalConstants.COOPERATIVITY_STRING, GUI_COOPERATIVITY_STRING);
		sbmlName.put(GlobalConstants.COOPERATIVITY_STRING, SBML_COOPERATIVITY_STRING);

		guiName.put(GlobalConstants.RNAP_BINDING_STRING, GUI_RNAP_BINDING_STRING);
		sbmlName.put(GlobalConstants.RNAP_BINDING_STRING, SBML_RNAP_BINDING_STRING);

		guiName.put(GlobalConstants.OCR_STRING, GUI_OCR_STRING);
		sbmlName.put(GlobalConstants.OCR_STRING, SBML_OCR_STRING);

		guiName.put(GlobalConstants.KBASAL_STRING, GUI_KBASAL_STRING);
		sbmlName.put(GlobalConstants.KBASAL_STRING, SBML_KBASAL_STRING);

		guiName.put(GlobalConstants.ACTIVED_STRING, GUI_ACTIVED_STRING);
		sbmlName.put(GlobalConstants.ACTIVED_STRING, SBML_ACTIVED_STRING);

		guiName.put(GlobalConstants.KREP_STRING, GUI_KREP_STRING);
		sbmlName.put(GlobalConstants.KREP_STRING, SBML_KREP_STRING);
		
		guiName.put(GlobalConstants.KACT_STRING, GUI_KACT_STRING);
		sbmlName.put(GlobalConstants.KACT_STRING, SBML_KACT_STRING);
		
		for (String i : guiName.keySet()) {
			gcmName.put(guiName.get(i), i);
		}
	}
}

/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.biomodel.parser;


import java.util.HashMap;

import edu.utah.ece.async.util.GlobalConstants;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class CompatibilityFixer {

	public static String getGuiName(String id) {
		return guiName.get(id);
	}
	
	public static String getOLDName(String id) {
		return oldName.get(id);
	}
	
	//Converts old, ugly name for storing parameter in a gcm and converts it to the corresponding sbml parameter name
	//Returns old name if no corresponding sbml name exists
	public static String convertOLDName(String id) {
		for (String s : oldName.keySet()) {
			if (oldName.get(s).equals(id)) {
				return s;
			}
		}
		return id;
	}
	
	public static String getGCMName(String guiid) {
		return gcmName.get(guiid);
	}
	
	private static HashMap<String, String> guiName = null;
	private static HashMap<String, String> oldName = null;
	private static HashMap<String, String> gcmName = null;
	
//	public final static String GUI_NAME_STRING = "ID";
//	public final static String OLD_NAME_STRING = "ID";

	public final static String GUI_INITIAL_STRING = "Initial amount/concentration";
	public final static String OLD_INITIAL_STRING = "Initial amount";

//	public final static String GUI_KBIO_STRING = "Biochemical equilibrium";
//	public final static String OLD_KBIO_STRING = "Kb";

//	public final static String GUI_MAX_DIMER_STRING = "N-mer as transcription factor";
//	public final static String OLD_MAX_DIMER_STRING = "nd";
//	
//	public final static String GUI_KASSOCIATION_STRING = "Dimerization equilibrium";
//	public final static String OLD_KASSOCIATION_STRING = "Kd";

	public final static String GUI_KDECAY_STRING = "Degradation rate";
	public final static String OLD_KDECAY_STRING = "kdecay";
	
	public final static String GUI_KECDECAY_STRING = "Extracellular degradation rate";
	public final static String OLD_KECDECAY_STRING = "kecdecay";
	
	public final static String GUI_PROMOTER_COUNT_STRING = "Initial promoter count";
	public final static String OLD_PROMOTER_COUNT_STRING = "Promoter count";

	public final static String GUI_RNAP_STRING = "Initial RNAP count";
	public final static String OLD_RNAP_STRING = "RNAP count";

	public final static String GUI_STOICHIOMETRY_STRING = "Stoichiometry of production";
	public final static String OLD_STOICHIOMETRY_STRING = "Stoichiometry of production";

	public final static String GUI_COOPERATIVITY_STRING = "Stoichiometry of binding";
	public final static String OLD_COOPERATIVITY_STRING = "Binding site count for transciption factors";

	public final static String GUI_FORWARD_RNAP_BINDING_STRING = "Forward RNAP binding rate";
	public final static String OLD_FORWARD_RNAP_BINDING_STRING = "KRNAP";

	public final static String GUI_REVERSE_RNAP_BINDING_STRING = "Reverse RNAP binding rate";
	public final static String OLD_REVERSE_RNAP_BINDING_STRING = "KRNAP";

	public final static String GUI_RNAP_BINDING_STRING = "RNAP binding equilibrium";
	public final static String OLD_RNAP_BINDING_STRING = "KRNAP";

	public final static String GUI_FORWARD_ACTIVATED_RNAP_BINDING_STRING = "Forward activated RNAP binding rate";
	public final static String OLD_FORWARD_ACTIVATED_RNAP_BINDING_STRING = "kao_f";

	public final static String GUI_REVERSE_ACTIVATED_RNAP_BINDING_STRING = "Reverse activated RNAP binding rate";
	public final static String OLD_REVERSE_ACTIVATED_RNAP_BINDING_STRING = "kao_r";

	public final static String GUI_ACTIVATED_RNAP_BINDING_STRING = "Activated RNAP binding equilibrium";
	public final static String OLD_ACTIVATED_RNAP_BINDING_STRING = "Kao";

	public final static String GUI_OCR_STRING = "Open complex production rate";
	public final static String OLD_OCR_STRING = "kocr";

	public final static String GUI_KBASAL_STRING = "Basal production rate";
	public final static String OLD_KBASAL_STRING = "kbasal";

	public final static String GUI_ACTIVED_STRING = "Activated production rate";
	public final static String OLD_ACTIVED_STRING = "Activated kocr";

	public final static String GUI_FORWARD_KREP_STRING = "Forward repression binding rate";
	public final static String OLD_FORWARD_KREP_STRING = "krep_f";

	public final static String GUI_REVERSE_KREP_STRING = "Reverse repression binding rate";
	public final static String OLD_REVERSE_KREP_STRING = "krep_r";

	public final static String GUI_KREP_STRING = "Repression binding equilibrium";
	public final static String OLD_KREP_STRING = "Krep";

	public final static String GUI_FORWARD_KACT_STRING = "Forward activation binding rate";
	public final static String OLD_FORWARD_KACT_STRING = "kact_f";

	public final static String GUI_REVERSE_KACT_STRING = "Reverse activation binding rate";
	public final static String OLD_REVERSE_KACT_STRING = "kact_r";

	public final static String GUI_KACT_STRING = "Activation binding equilibrium";
	public final static String OLD_KACT_STRING = "Kact";

	public final static String GUI_FORWARD_KCOMPLEX_STRING = "Forward complex formation rate";
	public final static String OLD_FORWARD_KCOMPLEX_STRING = "kc_f";

	public final static String GUI_REVERSE_KCOMPLEX_STRING = "Reverse complex formation rate";
	public final static String OLD_REVERSE_KCOMPLEX_STRING = "kc_r";

	public final static String GUI_KCOMPLEX_STRING = "Complex formation equilibrium";
	public final static String OLD_KCOMPLEX_STRING = "Kc";
	
	public final static String GUI_MEMDIFF_STRING = "Membrane diffusion rate (fd/rv)";
	public final static String OLD_MEMDIFF_STRING = "kmdiff";
	
	public final static String GUI_FORWARD_MEMDIFF_STRING = "Forward membrane diffusion rate";
	public final static String OLD_FORWARD_MEMDIFF_STRING = "kfmdiff";
	
	public final static String GUI_REVERSE_MEMDIFF_STRING = "Reverse membrane diffusion rate";
	public final static String OLD_REVERSE_MEMDIFF_STRING = "krmdiff";
	
	public final static String GUI_KECDIFF_STRING = "Extracellular diffusion rate";
	public final static String OLD_KECDIFF_STRING = "Kecdiff";

	//Static initializer that sets up the hashing 
	static {		
		guiName = new HashMap<String, String>();
		oldName = new HashMap<String, String>();
		gcmName = new HashMap<String, String>();
		
		//guiName.put(GlobalConstants.NAME, GUI_NAME_STRING);
		//sbmlName.put(GlobalConstants.NAME, SBML_NAME_STRING);
		
		guiName.put(GlobalConstants.INITIAL_STRING, GUI_INITIAL_STRING);
		oldName.put(GlobalConstants.INITIAL_STRING, OLD_INITIAL_STRING);
		
//		guiName.put(GlobalConstants.KBIO_STRING, GUI_KBIO_STRING);
//		oldName.put(GlobalConstants.KBIO_STRING, OLD_KBIO_STRING);
//
//		guiName.put(GlobalConstants.MAX_DIMER_STRING, GUI_MAX_DIMER_STRING);
//		oldName.put(GlobalConstants.MAX_DIMER_STRING, OLD_MAX_DIMER_STRING);
//		
//		guiName.put(GlobalConstants.KASSOCIATION_STRING, GUI_KASSOCIATION_STRING);
//		oldName.put(GlobalConstants.KASSOCIATION_STRING, OLD_KASSOCIATION_STRING);
		
		guiName.put(GlobalConstants.KDECAY_STRING, GUI_KDECAY_STRING);
		oldName.put(GlobalConstants.KDECAY_STRING, OLD_KDECAY_STRING);
		
		guiName.put(GlobalConstants.KECDECAY_STRING, GUI_KECDECAY_STRING);
		oldName.put(GlobalConstants.KECDECAY_STRING, OLD_KECDECAY_STRING);
		
		guiName.put(GlobalConstants.PROMOTER_COUNT_STRING, GUI_PROMOTER_COUNT_STRING);
		oldName.put(GlobalConstants.PROMOTER_COUNT_STRING, OLD_PROMOTER_COUNT_STRING);
		
		guiName.put(GlobalConstants.RNAP_STRING, GUI_RNAP_STRING);
		oldName.put(GlobalConstants.RNAP_STRING, OLD_RNAP_STRING);

		guiName.put(GlobalConstants.STOICHIOMETRY_STRING, GUI_STOICHIOMETRY_STRING);
		oldName.put(GlobalConstants.STOICHIOMETRY_STRING, OLD_STOICHIOMETRY_STRING);

		guiName.put(GlobalConstants.COOPERATIVITY_STRING, GUI_COOPERATIVITY_STRING);
		oldName.put(GlobalConstants.COOPERATIVITY_STRING, OLD_COOPERATIVITY_STRING);

		guiName.put(GlobalConstants.RNAP_BINDING_STRING, GUI_RNAP_BINDING_STRING);
		oldName.put(GlobalConstants.RNAP_BINDING_STRING, OLD_RNAP_BINDING_STRING);

		guiName.put(GlobalConstants.FORWARD_RNAP_BINDING_STRING, GUI_FORWARD_RNAP_BINDING_STRING);
		oldName.put(GlobalConstants.FORWARD_RNAP_BINDING_STRING, OLD_FORWARD_RNAP_BINDING_STRING);

		guiName.put(GlobalConstants.REVERSE_RNAP_BINDING_STRING, GUI_REVERSE_RNAP_BINDING_STRING);
		oldName.put(GlobalConstants.REVERSE_RNAP_BINDING_STRING, OLD_REVERSE_RNAP_BINDING_STRING);
		
		guiName.put(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, GUI_ACTIVATED_RNAP_BINDING_STRING);
		oldName.put(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, OLD_ACTIVATED_RNAP_BINDING_STRING);
		
		guiName.put(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING, GUI_FORWARD_ACTIVATED_RNAP_BINDING_STRING);
		oldName.put(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING, OLD_FORWARD_ACTIVATED_RNAP_BINDING_STRING);
		
		guiName.put(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING, GUI_REVERSE_ACTIVATED_RNAP_BINDING_STRING);
		oldName.put(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING, OLD_REVERSE_ACTIVATED_RNAP_BINDING_STRING);

		guiName.put(GlobalConstants.OCR_STRING, GUI_OCR_STRING);
		oldName.put(GlobalConstants.OCR_STRING, OLD_OCR_STRING);

		guiName.put(GlobalConstants.KBASAL_STRING, GUI_KBASAL_STRING);
		oldName.put(GlobalConstants.KBASAL_STRING, OLD_KBASAL_STRING);

		guiName.put(GlobalConstants.ACTIVATED_STRING, GUI_ACTIVED_STRING);
		oldName.put(GlobalConstants.ACTIVATED_STRING, OLD_ACTIVED_STRING);

		guiName.put(GlobalConstants.KREP_STRING, GUI_KREP_STRING);
		oldName.put(GlobalConstants.KREP_STRING, OLD_KREP_STRING);

		guiName.put(GlobalConstants.FORWARD_KREP_STRING, GUI_FORWARD_KREP_STRING);
		oldName.put(GlobalConstants.FORWARD_KREP_STRING, OLD_FORWARD_KREP_STRING);

		guiName.put(GlobalConstants.REVERSE_KREP_STRING, GUI_REVERSE_KREP_STRING);
		oldName.put(GlobalConstants.REVERSE_KREP_STRING, OLD_REVERSE_KREP_STRING);
		
		guiName.put(GlobalConstants.KACT_STRING, GUI_KACT_STRING);
		oldName.put(GlobalConstants.KACT_STRING, OLD_KACT_STRING);
		
		guiName.put(GlobalConstants.FORWARD_KACT_STRING, GUI_FORWARD_KACT_STRING);
		oldName.put(GlobalConstants.FORWARD_KACT_STRING, OLD_FORWARD_KACT_STRING);
		
		guiName.put(GlobalConstants.REVERSE_KACT_STRING, GUI_REVERSE_KACT_STRING);
		oldName.put(GlobalConstants.REVERSE_KACT_STRING, OLD_REVERSE_KACT_STRING);
		
		guiName.put(GlobalConstants.KCOMPLEX_STRING, GUI_KCOMPLEX_STRING);
		oldName.put(GlobalConstants.KCOMPLEX_STRING, OLD_KCOMPLEX_STRING);
		
		guiName.put(GlobalConstants.FORWARD_KCOMPLEX_STRING, GUI_FORWARD_KCOMPLEX_STRING);
		oldName.put(GlobalConstants.FORWARD_KCOMPLEX_STRING, OLD_FORWARD_KCOMPLEX_STRING);
		
		guiName.put(GlobalConstants.REVERSE_KCOMPLEX_STRING, GUI_REVERSE_KCOMPLEX_STRING);
		oldName.put(GlobalConstants.REVERSE_KCOMPLEX_STRING, OLD_REVERSE_KCOMPLEX_STRING);
		
		guiName.put(GlobalConstants.MEMDIFF_STRING, GUI_MEMDIFF_STRING);
		oldName.put(GlobalConstants.MEMDIFF_STRING, OLD_MEMDIFF_STRING);
		
		guiName.put(GlobalConstants.FORWARD_MEMDIFF_STRING, GUI_FORWARD_MEMDIFF_STRING);
		oldName.put(GlobalConstants.FORWARD_MEMDIFF_STRING, OLD_FORWARD_MEMDIFF_STRING);
		
		guiName.put(GlobalConstants.REVERSE_MEMDIFF_STRING, GUI_REVERSE_MEMDIFF_STRING);
		oldName.put(GlobalConstants.REVERSE_MEMDIFF_STRING, OLD_REVERSE_MEMDIFF_STRING);
		
		guiName.put(GlobalConstants.KECDIFF_STRING, GUI_KECDIFF_STRING);
		oldName.put(GlobalConstants.KECDIFF_STRING, OLD_KECDIFF_STRING);
		
		for (String i : guiName.keySet()) {
			gcmName.put(guiName.get(i), i);
		}
	}
}

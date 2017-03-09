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
package dataModels.util;

import java.io.File;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GlobalConstants {

	public static final String ACTIVATED_RNAP_BINDING_STRING = "Kao";
	public static final String FORWARD_ACTIVATED_RNAP_BINDING_STRING = "kao_f";
	public static final String REVERSE_ACTIVATED_RNAP_BINDING_STRING = "kao_r";
	
	public static final String RNAP_BINDING_STRING = "Ko";
	public static final String FORWARD_RNAP_BINDING_STRING = "ko_f";
	public static final String REVERSE_RNAP_BINDING_STRING = "ko_r";

	public static final String KREP_STRING = "Kr"; 
	public static final String FORWARD_KREP_STRING = "kr_f"; 
	public static final String REVERSE_KREP_STRING = "kr_r"; 
	
	public static final String KACT_STRING = "Ka";
	public static final String FORWARD_KACT_STRING = "ka_f";
	public static final String REVERSE_KACT_STRING = "ka_r";
	
	public static final String KCOMPLEX_STRING = "Kc";
	public static final String FORWARD_KCOMPLEX_STRING = "kc_f";
	public static final String REVERSE_KCOMPLEX_STRING = "kc_r";

	public static final String MEMDIFF_STRING = "kmdiff";
	public static final String FORWARD_MEMDIFF_STRING = "kmdiff_f";
	public static final String REVERSE_MEMDIFF_STRING = "kmdiff_r";
	
//	public static final String KBIO_STRING = "Kbio";

	public static final String OCR_STRING = "ko";
	public static final String KBASAL_STRING = "kb";
	public static final String ACTIVATED_STRING = "ka";
	public static final String KDECAY_STRING = "kd";

	public static final String KECDIFF_STRING = "kecdiff";
	public static final String KECDECAY_STRING = "kecd";

	public static final String PROMOTER_COUNT_STRING = "ng";
	public static final String RNAP_STRING = "nr";
	public static final String STOICHIOMETRY_STRING = "np";
	public static final String COOPERATIVITY_STRING = "nc";
	
	public static final String COMPLEX = "complex";
	public static final String DIFFUSIBLE = "diffusible";
	public static final String KASSOCIATION_STRING = "Kassociation"; // Dimerization value
	public static final String GENE_PRODUCT = "gene product";
	public static final String TRANSCRIPTION_FACTOR = "transcription factor";

	
//	public static final String KREP_VALUE = ".5"; 
//	public static final String KACT_VALUE = ".0033";
//	public static final String KBIO_VALUE = ".05";
//	public static final String PROMOTER_COUNT_VALUE = "2";
//	public static final String KASSOCIATION_VALUE = ".05";
//	public static final String KBASAL_VALUE = ".0001";
//	public static final String OCR_VALUE = ".05";
//	public static final String KDECAY_VALUE = ".0075";
//	public static final String RNAP_VALUE = "30";
//	public static final String RNAP_BINDING_VALUE = ".033";
//	public static final String STOICHIOMETRY_VALUE = "10";
//	public static final String COOPERATIVITY_VALUE = "2";
//	public static final String ACTIVED_VALUE = ".25";
	
	public static final String ID = "ID";
	public static final String COMPONENT = "Component";
	public static final String SPECIES = "Species";
	public static final String INFLUENCE = "Influence";
	public static final String COMPONENT_CONNECTION = "Component Connection";
	public static final String PRODUCTION = "Production";
	public static final String DEGRADATION = "Degradation";
	public static final String COMPLEXATION = "Complex";
	public static final String REACTION = "Reaction";
	public static final String MODIFIER = "Modifier";
	public static final String REACTION_EDGE = "Reaction_Edge";
	public static final String MODIFIER_REACTION_EDGE = "Modifier_Reaction_Edge";
	public static final String RULE_EDGE = "Rule_Edge";
	public static final String CONSTRAINT_EDGE = "Constraint_Edge";
	public static final String EVENT_EDGE = "Event_Edge";
	public static final String PETRI_NET_EDGE = "Petri_Net_Edge";
	public static final String GRID_RECTANGLE = "Grid Rectangle";
	public static final String PORTMAP = "Port Map";
	public static final String SBOTERM = "SBO Term:";
	public static final String PORT = "port";
	public static final String NAME = "Name";
	public static final String CONSTANT = "boundary";
	public static final String SPASTIC = "constitutive";
	public static final String DEGRADES = "degrades";
	public static final String NORMAL = "normal";
	public static final String INPUT = "input";
	public static final String OUTPUT = "output";
	public static final String INTERNAL = "internal";
	public static final String TYPE = "Type";
	public static final String PORTTYPE = "Port Type";
	public static final String MAX_DIMER_STRING = "N-mer as trascription factor";
	public static final String INITIAL_STRING = "ns";	
	public static final String PROMOTER = "Promoter";
	public static final String VARIABLE = "Variable";
	public static final String PLACE = "Place";
	public static final String BOOLEAN = "Boolean";
	public static final String MRNA = "mRNA";
	public static final String EXPLICIT_PROMOTER = "ExplicitPromoter";
	public static final String SBMLFILE = "SBML file";
	public static final String BIOABS = "Biochemical abstraction";
	public static final String DIMABS = "Dimerization abstraction";
	public static final String COMPARTMENT = "compartment";
	public static final String PARAMETER = "parameter";
	public static final String LOCALPARAMETER = "localParameter";
	public static final String SBMLSPECIES = "species";
	public static final String SBMLREACTION = "reaction";
	public static final String EVENT = "event";
	public static final String TRANSITION = "transition";
	public static final String CONSTRAINT = "constraint";
	public static final String FUNCTION = "functionDefinition";
	public static final String UNIT = "unitDefinition";
	public static final String RULE = "rule";
	public static final String ASSIGNMENT_RULE = "assignmentRule";
	public static final String INITIAL_ASSIGNMENT = "initialAssignment";
	public static final String RATE_RULE = "rateRule";
	public static final String ALGEBRAIC_RULE = "algebraicRule";
	public static final String GLYPH = "Glyph";
	public static final String REFERENCE_GLYPH = "ReferenceGlyph";
	public static final String REACTANT_GLYPH = "ReactantGlyph";
	public static final String PRODUCT_GLYPH = "ProductGlyph";
	public static final String MODIFIER_GLYPH = "ModifierGlyph";
	public static final String TEXT_GLYPH = "TextGlyph";
	public static final String ENCLOSING_COMPARTMENT = "enclosingCompartment";
	public static final String DEFAULT_COMPARTMENT = "defaultCompartment";
	public static final String FAIL = "fail";
	public static final String FAIL_TRANSITION = "failTransition";
	public static final String RATE = "rate";
	public static final String TRIGGER = "trigger";
	
	public static final String SBOL_PROMOTER = "sbolPromoter";
	public static final String SBOL_TERMINATOR = "sbolTerminator";
	public static final String SBOL_RBS = "sbolRBS";
	public static final String SBOL_CDS = "sbolCDS";
	public static final String SO_PROMOTER = "SO_0000167";
	public static final String SO_TERMINATOR = "SO_0000141";
	public static final String SO_RBS = "SO_0000139";
	public static final String SO_CDS = "SO_0000316";
	public static final String SO_AUTHORITY = "purl.obolibrary.org";
	public static final String SO_AUTHORITY2 = "identifiers.org";
	public static final String SBOL_COMPONENTDEFINITION = "sbolDnaComponent";
	public static final String GENETIC_CONSTRUCT_REGEX_DEFAULT = 
			SO_PROMOTER + "(" + SO_RBS + "," + SO_CDS + ")+" + SO_TERMINATOR + "+";
	public static final String GENETIC_CONSTRUCT_REGEX_PREFERENCE = "biosim.assembly.regex";
	public static final String SBOL_AUTHORITY_PREFERENCE = "biosim.assembly.authority";
	public static final String SBOL_AUTHORITY_DEFAULT = "http://dummy.org";
	public static final String CONSTRUCT_ASSEMBLY_DEFAULT = "False";
	public static final String CONSTRUCT_ASSEMBLY_PREFERENCE = "biosim.assembly.validation";
	public static final String CONSTRUCT_VALIDATION_DEFAULT = "False";
	public static final String CONSTRUCT_VALIDATION_PREFERENCE = "biosim.assembly.validation";
	public static final String CONSTRUCT_VALIDATION_WARNING_DEFAULT = "False";
	public static final String CONSTRUCT_VALIDATION_WARNING_PREFERENCE = "biosim.assembly.warning";
	public static final String CONSTRUCT_VALIDATION_FAIL_STATE_ID = "Sf";
	public static final String SBOL_ASSEMBLY_PLUS_STRAND = "+";
	public static final String SBOL_ASSEMBLY_MINUS_STRAND = "-";
	public static final int MEAN_CDS_LENGTH = 695;
	public static final int SD_CDS_LENGTH = 268;
	public static final int MEAN_PROMOTER_LENGTH = 62;
	public static final int SD_PROMOTER_LENGTH = 23;
	public static final int RBS_LENGTH = 12;
	public static final int TERMINATOR_LENGTH = 12;
	public static final int MEAN_GENE_LENGTH = 781;
	public static final int SD_GENE_LENGTH = 269;
	public static final String SBOL_FILE_EXTENSION = ".sbol";
	public static final String RDF_FILE_EXTENSION = ".rdf";
	public static final String SBOL_SYNTH_PROPERTIES_EXTENSION = ".sbolsynth.properties";
	public static final String SBOL_SYNTH_SPEC_PROPERTY = "synthesis.spec";
	public static final String SBOL_SYNTH_LIBS_PROPERTY = "synthesis.libraries";
	public static final String SBOL_SYNTH_LIBS_PREFERENCE = "biosim." + SBOL_SYNTH_LIBS_PROPERTY;
	public static final String SBOL_SYNTH_LIBS_DEFAULT = "";
	public static final String SBOL_SYNTH_METHOD_PROPERTY = "synthesis.method";
	public static final String SBOL_SYNTH_METHOD_PREFERENCE = "biosim." + SBOL_SYNTH_METHOD_PROPERTY;
	public static final String SBOL_SYNTH_EXHAUST_BB = "Exact Branch and Bound";
	public static final String SBOL_SYNTH_METHOD_DEFAULT = SBOL_SYNTH_EXHAUST_BB;
	public static final String SBOL_SYNTH_GREEDY_BB = "Greedy Branch and Bound";
	public static final String SBOL_SYNTH_STRUCTURAL_METHODS = SBOL_SYNTH_EXHAUST_BB + "," + SBOL_SYNTH_GREEDY_BB;
	public static final String SBOL_SYNTH_NUM_SOLNS_PROPERTY = "synthesis.numsolutions";
	public static final String SBOL_SYNTH_NUM_SOLNS_PREFERENCE = "biosim." + SBOL_SYNTH_NUM_SOLNS_PROPERTY;
	public static final String SBOL_SYNTH_NUM_SOLNS_DEFAULT = "1";
	public static final String SBOL_SYNTH_COMMAND = "synthesis_project";
	
	public static final String BIO = "biochem";
	public static final String ACTIVATION = "activation";
	public static final String REPRESSION = "repression";
	public static final String REGULATION = "regulation";
	public static final String NOINFLUENCE = "no_influence";
	
	public static final String SBO_FRAMEWORK = "SBO:0000004";
	public static final String SBO_MATERIAL_ENTITY = "SBO:0000240";
	public static final String SBO_INTERACTION = "SBO:0000231";
	public static final String SBO_PARTICIPANT_ROLE = "SBO:0000003";
	public static final String SBO_MATHEMATICAL_EXPRESSION = "SBO:0000064";
	public static final String SBO_PARAMETER = "SBO:0000545";
	
	// Material entities
	public static final String SBO_DNA = "SBO:0000251";
	public static final String SBO_DNA_SEGMENT = "SBO:0000634";
	public static final String SBO_RNA = "SBO:0000250";
	public static final String SBO_RNA_SEGMENT = "SBO:0000635";
	public static final String SBO_PROTEIN = "SBO:0000252";
	public static final String SBO_NONCOVALENT_COMPLEX = "SBO:0000253";
	public static final String SBO_SIMPLE_CHEMICAL = "SBO:0000247";
	
	public static final int SBO_ACTIVATION = 459;
	public static final int SBO_REPRESSION = 20;
	public static final int SBO_DUAL_ACTIVITY = 595;
	public static final int SBO_NEUTRAL = 594;
	public static final int SBO_PROMOTER_MODIFIER = 598;

	public static final int SBO_PROMOTER_BINDING_REGION = 590;
	public static final int SBO_MRNA = 250;

	public static final int SBO_PETRI_NET_PLACE = 593;
	public static final int SBO_LOGICAL = 602;
	public static final int SBO_PETRI_NET_TRANSITION = 591;
	
	public static final int SBO_INPUT_PORT = 600;
	public static final int SBO_OUTPUT_PORT = 601;

	public static final int SBO_DEGRADATION = 179;
	public static final int SBO_DIFFUSION = 185;
	public static final int SBO_GENETIC_PRODUCTION = 589;
	public static final int SBO_ASSOCIATION = 177;
	public static final int SBO_CONSTITUTIVE = 396;

	// obsolete
	public static final int SBO_REGULATION = 19;
	public static final int SBO_OLD_PROMOTER_SPECIES = 369;
	public static final int SBO_PROMOTER = 535;
	public static final int SBO_PROMOTER_SPECIES = 354;
	public static final int SBO_MRNA_OLD = 278;
	public static final int SBO_BOOLEAN = 547;
	public static final int SBO_PLACE = 546;
	public static final int SBO_TRANSITION = 464;
	public static final int SBO_PRODUCTION = 183;
	public static final int SBO_COMPLEX = 526;

	// Model frameworks
	public static final int SBO_NONSPATIAL_CONTINUOUS = 293;
	public static final int SBO_SPATIAL_CONTINUOUS = 292;
	public static final int SBO_NONSPATIAL_DISCRETE = 295;
	public static final int SBO_SPATIAL_DISCRETE = 294;
	public static final int SBO_BOOLEAN_LOGICAL = 547;
	public static final int SBO_FLUX_BALANCE = 624;
	
	public static final String KISAO_GENERIC = "KISAO:0000000";
	public static final String KISAO_MONTE_CARLO = "KISAO:0000319";
	public static final String KISAO_GILLESPIE = "KISAO:0000241";
	public static final String KISAO_GILLESPIE_DIRECT = "KISAO:0000029";
	public static final String KISAO_SSA_CR = "KISAO:0000329";
	public static final String KISAO_EULER = "KISAO:0000030";
	public static final String KISAO_RUNGE_KUTTA_FEHLBERG = "KISAO:0000086";
	public static final String KISAO_RUNGE_KUTTA_PRINCE_DORMAND = "KISAO:0000087";
	public static final String KISAO_FBA = "KISAO:0000437";
	public static final String KISAO_MINIMUM_STEP_SIZE = "KISAO:0000485";
	public static final String KISAO_MAXIMUM_STEP_SIZE = "KISAO:0000467";
	public static final String KISAO_ABSOLUTE_TOLERANCE = "KISAO:0000211";
	public static final String KISAO_RELATIVE_TOLERANCE = "KISAO:0000209";
	public static final String KISAO_SEED = "KISAO:0000488";
	public static final String KISAO_SAMPLES = "KISAO:0000326";
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NONE = "none";
	public static final String OK = "Ok";
	public static final String CANCEL = "Cancel";
	
//	public static final String MAX_DIMER_VALUE = "1";
//	public static final String INITIAL_VALUE = "0";
	
	
//	public static final String DIMER_COUNT_STRING = "label";
//	public static final String TYPE_STRING = "label";

	public static final int DEFAULT_SPECIES_WIDTH = 100;
	public static final int DEFAULT_SPECIES_HEIGHT = 30;
	public static final int DEFAULT_DNA_WIDTH = 100;
	public static final int DEFAULT_DNA_HEIGHT = 30;
	public static final int DEFAULT_RNA_WIDTH = 50;
	public static final int DEFAULT_RNA_HEIGHT = 20;
	public static final int DEFAULT_PROTEIN_WIDTH = 40;
	public static final int DEFAULT_PROTEIN_HEIGHT = 40;
	public static final int DEFAULT_SMALL_MOLECULE_WIDTH = 20;
	public static final int DEFAULT_SMALL_MOLECULE_HEIGHT = 20;
	public static final int DEFAULT_COMPLEX_WIDTH = 100;
	public static final int DEFAULT_COMPLEX_HEIGHT = 30;
	public static final int DEFAULT_REACTION_WIDTH = 20;
	public static final int DEFAULT_REACTION_HEIGHT = 20;
	public static final int DEFAULT_VARIABLE_WIDTH = 30;
	public static final int DEFAULT_VARIABLE_HEIGHT = 30;
	public static final int DEFAULT_RULE_WIDTH = 50;
	public static final int DEFAULT_RULE_HEIGHT = 50;
	public static final int DEFAULT_CONSTRAINT_WIDTH = 50;
	public static final int DEFAULT_CONSTRAINT_HEIGHT = 40;
	public static final int DEFAULT_EVENT_WIDTH = 75;
	public static final int DEFAULT_EVENT_HEIGHT = 25;
	public static final int DEFAULT_TRANSITION_WIDTH = 50;
	public static final int DEFAULT_TRANSITION_HEIGHT = 20;
	public static final int DEFAULT_COMPONENT_WIDTH = 80;
	public static final int DEFAULT_COMPONENT_HEIGHT = 40;
	public static final int DEFAULT_COMPARTMENT_WIDTH = 250;
	public static final int DEFAULT_COMPARTMENT_HEIGHT = 250;
	public static final int DEFAULT_TEXT_WIDTH = 40;
	public static final int DEFAULT_TEXT_HEIGHT = 10;
	public static final String		separator			= (File.separator.equals("\\")) ? "\\\\" : File.separator;
	public static int				SBML_LEVEL			= 3;
	public static int				SBML_VERSION		= 1;
	
	
}

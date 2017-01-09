package backend.sbol.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.impl.DnaComponentImpl;
import org.sbolstandard.core.impl.DnaSequenceImpl;
import org.sbolstandard.core.util.SequenceOntology;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceConstraint;
import org.sbolstandard.core2.SystemsBiologyOntology;

import backend.biomodel.annotation.AnnotationUtility;
import backend.biomodel.annotation.SBOLAnnotation;
import backend.biomodel.parser.BioModel;
import backend.util.GlobalConstants;

public class SBOLTestFactory {

	public SBOLTestFactory() {
		
	}
	
	public static void testWrite() {
//		SBOLDocument sbolDoc = new SBOLDocument();
//		sbolDoc.setDefaultURIprefix(GlobalConstants.SBOL_AUTHORITY_DEFAULT);
//		ModuleDefinition lacIInverter = createInverterDefinition("LacI", "pLac", "cTetR", "TetR", 
//				MyersOntology.TF, sbolDoc);
//		ModuleDefinition tetRInverter = createInverterDefinition("TetR", "pTet", "cLacI", "LacI", 
//				MyersOntology.TF, sbolDoc);
//		createToggleSwitchDefinition("IPTG", "aTc", lacIInverter, tetRInverter, sbolDoc);
////		List<ModuleDefinition> sensorDefs = new LinkedList<ModuleDefinition>();
////		sensorDefs.add(createSensorDefinition("Ara", "Promoter", "araC", "AraC",
////				"pBAD", "ipgC", "IpgC", MyersOntology.CHAPERONE, sbolDoc, true));
////		sensorDefs.add(createSensorDefinition("IPTG", "Promoter", "lacI", "LacI",
////				"pTac", "mxiE", "MxiE", MyersOntology.TF, sbolDoc, false));
////		sensorDefs.add(createSensorDefinition("Three_OC_Six", "Promoter", "luxR", "LuxR",
////				"pLux", "exsC", "ExsC", MyersOntology.CHAPERONE, sbolDoc, true));
////		List<String> geneIDs = new LinkedList<String>();
////		geneIDs.add("exsD");
////		geneIDs.add("exsA");
////		List<String> proteinIDs = new LinkedList<String>();
////		proteinIDs.add("ExsD");
////		proteinIDs.add("ExsA");
////		List<URI> outputRoles = new LinkedList<URI>();
////		outputRoles.add(MyersOntology.CHAPERONE);
////		outputRoles.add(MyersOntology.TF);
////		sensorDefs.add(createSensorDefinition("aTc", "Promoter", "tetR", "TetR",
////				"pTetR", geneIDs, proteinIDs, outputRoles, sbolDoc, false));
////		
////		List<ModuleDefinition> andDefs = new LinkedList<ModuleDefinition>();
////		andDefs.add(createANDDefinition("IpgC", "MxiE", "pipaH", "sicA", "SicA", 
////				MyersOntology.CHAPERONE, sbolDoc));
////		andDefs.add(createANDDefinition("ExsC", "ExsD", "ExsA", "pexsC", "invF", "InvF",
////				MyersOntology.TF, sbolDoc));
////		andDefs.add(createANDDefinition("SicA", "InvF", "psicA", "rfp", "RFP", 
////				MyersOntology.REPORTER, sbolDoc));
////		createANDSensorDefinition(sensorDefs, andDefs, sbolDoc);
////		File test = new File("C:\\Users\\Nic\\Documents\\temp\\andSensor.xml");
////		File test = new File("C:\\Users\\Nic\\Documents\\temp\\testToggle.xml");
////		try {
////			SBOLWriter.write(sbolDoc, test);
////		} catch (FileNotFoundException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		File test = new File("C:\\Users\\Nic\\Documents\\temp\\testToggle.txt");
////		try {
//			try {
//				SBOLWriter.write(sbolDoc,  test, SBOLReader.TURTLE);
//			} catch (Throwable e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
////		} catch (FileNotFoundException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////		Annotation testAnno = new Annotation(null, null);
////		Annotation testAnno = new Annotation(null);
	}
	
	public static Module createToggleSwitchModule(ModuleDefinition toggleDef, ModuleDefinition parentModuleDef) {
		if (toggleDef.containsRole(MyersOntology.TOGGLE)) {
			return createModule(toggleDef.getDisplayId(), toggleDef.getIdentity(), parentModuleDef);
		} else {
			return null;
		} 
		
	}
	
	public static ModuleDefinition createToggleSwitchDefinition(String inducerID1, String inducerID2, 
			ModuleDefinition inverterDef1, ModuleDefinition inverterDef2, SBOLDocument sbolDoc) {
		if (!inverterDef1.containsRole(MyersOntology.INVERTER) 
				|| !inverterDef2.containsRole(MyersOntology.INVERTER)) {
			return null;
		}
		FunctionalComponent remoteTF1 = null;
		FunctionalComponent remoteTF2 = null;
		for (FunctionalComponent remoteComp : inverterDef1.getFunctionalComponents()) {
			if (remoteComp.getDirection().equals(DirectionType.IN)) {
				remoteTF1 = remoteComp;
			} 
		}
		for (FunctionalComponent remoteComp : inverterDef2.getFunctionalComponents()) {
			if (remoteComp.getDirection().equals(DirectionType.IN)) {
				remoteTF2 = remoteComp;
			} 
		}
		for (FunctionalComponent remoteComp : inverterDef1.getFunctionalComponents()) {
			if (remoteComp.getDirection().equals(DirectionType.OUT) 
					&& !remoteComp.getDefinitionURI().equals(remoteTF2.getDefinitionURI())) {
				return null;
			}
		}
		for (FunctionalComponent remoteComp : inverterDef2.getFunctionalComponents()) {
			if (remoteComp.getDirection().equals(DirectionType.OUT) 
					&& !remoteComp.getDefinitionURI().equals(remoteTF1.getDefinitionURI())) {
				return null;
			}
		}
		ModuleDefinition toggleDef = createModuleDefinition(remoteTF1.getDisplayId() + "_" + remoteTF2.getDisplayId() + "_" + "Toggle_Switch", 
				MyersOntology.TOGGLE, sbolDoc);
		FunctionalComponent inducer1 = createInducerComponent(inducerID1, AccessType.PUBLIC, 
				DirectionType.IN, toggleDef, sbolDoc);
		FunctionalComponent inducer2 = createInducerComponent(inducerID2, AccessType.PUBLIC, 
				DirectionType.IN, toggleDef, sbolDoc);
		FunctionalComponent localTF1 = createTFComponent(remoteTF1.getDisplayId(), AccessType.PUBLIC, 
				DirectionType.OUT, toggleDef, sbolDoc);
		FunctionalComponent localTF2 = createTFComponent(remoteTF2.getDisplayId(), AccessType.PUBLIC, 
				DirectionType.OUT, toggleDef, sbolDoc);
		Set<String> ligandIDs1 = new HashSet<String>();
		ligandIDs1.add(inducerID1);
		ligandIDs1.add(remoteTF1.getDisplayId());
		FunctionalComponent complex1 = createComplexComponent(ligandIDs1, AccessType.PRIVATE, 
				DirectionType.NONE, toggleDef, sbolDoc);
		Set<String> ligandIDs2 = new HashSet<String>();
		ligandIDs1.add(inducerID2);
		ligandIDs1.add(remoteTF2.getDisplayId());
		FunctionalComponent complex2 = createComplexComponent(ligandIDs2, AccessType.PRIVATE, 
				DirectionType.NONE, toggleDef, sbolDoc);
		createBindingInteraction(inducer1, localTF1, complex1, toggleDef);
		createBindingInteraction(inducer2, localTF2, complex2, toggleDef);
		Module inverter1 = createInverterModule(inverterDef1, toggleDef, sbolDoc);
		Module inverter2 = createInverterModule(inverterDef2, toggleDef, sbolDoc);
		createIdenticalMappings(inverter1, toggleDef, sbolDoc);
		createIdenticalMappings(inverter2, toggleDef, sbolDoc);
		return toggleDef;
	}
	
	public static Module createInverterModule(ModuleDefinition inverterDef, ModuleDefinition parentModuleDef, 
			SBOLDocument sbolDoc) {
		if (inverterDef.containsRole(MyersOntology.INVERTER)) {
			return createModule(inverterDef.getDisplayId(), inverterDef.getIdentity(), parentModuleDef);
		} else {
			return null; 
		}
	}
	
	public static ModuleDefinition createInverterDefinition(String tfID, String promoterID, 
			String geneID, String proteinID, URI proteinRole, SBOLDocument sbolDoc) {
		ModuleDefinition inverterDef = createModuleDefinition(tfID + "_Inverter", MyersOntology.INVERTER, sbolDoc);
		FunctionalComponent tf = createTFComponent(tfID, AccessType.PUBLIC, DirectionType.IN,
				inverterDef, sbolDoc);
		FunctionalComponent promoter = createPromoterComponent(promoterID, AccessType.PRIVATE, DirectionType.NONE,
				inverterDef, sbolDoc);
		FunctionalComponent gene = createGeneComponent(geneID, AccessType.PRIVATE, DirectionType.NONE,
				inverterDef, sbolDoc);
		FunctionalComponent protein = createProteinComponent(proteinID, proteinRole, 
				AccessType.PUBLIC, DirectionType.OUT, inverterDef, sbolDoc);
		createRepressionInteraction(tf, promoter, inverterDef);
		createProductionInteraction(promoter, gene, protein, inverterDef);
		createDegradationInteraction(protein, inverterDef);
		return inverterDef;
	}
	
	public static Module createANDSensorModule(ModuleDefinition andSensorDef, ModuleDefinition parentModuleDef) {
		if (andSensorDef.containsRole(MyersOntology.SENSOR) && andSensorDef.containsRole(MyersOntology.AND)) {
			return createModule(andSensorDef.getDisplayId(), andSensorDef.getIdentity(), parentModuleDef);
		} else {
			return null;
		}
	}
	
	public static ModuleDefinition createANDSensorDefinition(List<ModuleDefinition> sensorDefs, 
			List<ModuleDefinition> andDefs, SBOLDocument sbolDoc) {
		if (sensorDefs.size() != 4 || !sensorDefs.get(0).containsRole(MyersOntology.SENSOR) 
				|| !sensorDefs.get(1).containsRole(MyersOntology.SENSOR) 
				|| !sensorDefs.get(2).containsRole(MyersOntology.SENSOR) 
				|| !sensorDefs.get(3).containsRole(MyersOntology.SENSOR)) {
			return null;
		} else if (andDefs.size() != 3 || !andDefs.get(0).containsRole(MyersOntology.AND)
				|| !andDefs.get(1).containsRole(MyersOntology.AND)
				|| !andDefs.get(2).containsRole(MyersOntology.AND)) {
			return null;
		}
		List<FunctionalComponent> remoteInputs1 = new LinkedList<FunctionalComponent>();
		List<FunctionalComponent> remoteOutputs1 = new LinkedList<FunctionalComponent>();
		List<FunctionalComponent> remoteInputs2 = new LinkedList<FunctionalComponent>();
		List<FunctionalComponent> remoteOutputs2 = new LinkedList<FunctionalComponent>();
		List<FunctionalComponent> remoteInputs3 = new LinkedList<FunctionalComponent>();
		List<FunctionalComponent> remoteOutputs3 = new LinkedList<FunctionalComponent>();
		for (ModuleDefinition sensorDef : sensorDefs) {
			for (FunctionalComponent remoteComp : sensorDef.getFunctionalComponents()) {
				if (remoteComp.getDirection().equals(DirectionType.IN)) {
					remoteInputs1.add(remoteComp);
				} else if (remoteComp.getDirection().equals(DirectionType.OUT)) {
					remoteOutputs1.add(remoteComp);
				} 
			}
		}
		for (int i = 0; i < andDefs.size() - 1; i++) {
			for (FunctionalComponent remoteComp : andDefs.get(i).getFunctionalComponents()) {
				if (remoteComp.getDirection().equals(DirectionType.IN)) {
					remoteInputs2.add(remoteComp);
				} else if (remoteComp.getDirection().equals(DirectionType.OUT)) {
					remoteOutputs2.add(remoteComp);
				} 
			}
		}
		for (FunctionalComponent remoteComp : andDefs.get(2).getFunctionalComponents()) {
			if (remoteComp.getDirection().equals(DirectionType.IN)) {
				remoteInputs3.add(remoteComp);
			} else if (remoteComp.getDirection().equals(DirectionType.OUT)) {
				remoteOutputs3.add(remoteComp);
			} 
		}
		for (FunctionalComponent remoteOutput1 : remoteOutputs1) {
			int connectCount = 0;
			for (FunctionalComponent remoteInput2 : remoteInputs2) {
				if (remoteOutput1.getDefinitionURI().equals(remoteInput2.getDefinitionURI())) {
					connectCount++;
				}
			}
			if (connectCount != 1) {
				return null;
			}
		}
		for (FunctionalComponent remoteOutput2 : remoteOutputs2) {
			int connectCount = 0;
			for (FunctionalComponent remoteInput3 : remoteInputs3) {
				if (remoteOutput2.getDefinitionURI().equals(remoteInput3.getDefinitionURI())) {
					connectCount++;
				}
			}
			if (connectCount != 1) {
				return null;
			}
		}
		String andSensorID = "_Sensor";
		for (FunctionalComponent remoteInducer : remoteInputs1) {
			andSensorID = "_AND_" + remoteInducer.getDisplayId() + andSensorID;
		}
		andSensorID = andSensorID.substring(5);
		ModuleDefinition andSensorDef = createModuleDefinition(andSensorID, MyersOntology.SENSOR, sbolDoc);
		andSensorDef.addRole(MyersOntology.AND);
		for (FunctionalComponent remoteInducer : remoteInputs1) {
			createInducerComponent(remoteInducer.getDisplayId(), AccessType.PUBLIC, 
					DirectionType.IN, andSensorDef, sbolDoc);
		}
		for (FunctionalComponent remoteOutput : remoteOutputs1) {
			ComponentDefinition remoteOutputDef = sbolDoc.getComponentDefinition(remoteOutput.getDefinitionURI());
			if (remoteOutputDef.containsRole(MyersOntology.CHAPERONE)) {
				FunctionalComponent chap = createChaperoneComponent(remoteOutput.getDisplayId(), 
						AccessType.PRIVATE, DirectionType.NONE, andSensorDef, sbolDoc);
				createDegradationInteraction(chap, andSensorDef);
			} else if (remoteOutputDef.containsRole(MyersOntology.TF)) {
				FunctionalComponent tf = createTFComponent(remoteOutput.getDisplayId(), 
						AccessType.PRIVATE, DirectionType.NONE, andSensorDef, sbolDoc);
				createDegradationInteraction(tf, andSensorDef);
			}
		}
		for (FunctionalComponent remoteOutput : remoteOutputs1) {
			ComponentDefinition remoteOutputDef = sbolDoc.getComponentDefinition(remoteOutput.getDefinitionURI());
			if (remoteOutputDef.containsRole(MyersOntology.CHAPERONE)) {
				FunctionalComponent chap = createChaperoneComponent(remoteOutput.getDisplayId(), 
						AccessType.PRIVATE, DirectionType.NONE, andSensorDef, sbolDoc);
				createDegradationInteraction(chap, andSensorDef);
			} else if (remoteOutputDef.containsRole(MyersOntology.TF)) {
				FunctionalComponent tf = createTFComponent(remoteOutput.getDisplayId(), 
						AccessType.PRIVATE, DirectionType.NONE, andSensorDef, sbolDoc);
				createDegradationInteraction(tf, andSensorDef);
			} else {
				return null;
			}
		}
		for (FunctionalComponent remoteOutput : remoteOutputs2) {
			ComponentDefinition remoteOutputDef = sbolDoc.getComponentDefinition(remoteOutput.getDefinitionURI());
			if (remoteOutputDef.containsRole(MyersOntology.CHAPERONE)) {
				FunctionalComponent chap = createChaperoneComponent(remoteOutput.getDisplayId(), 
						AccessType.PRIVATE, DirectionType.NONE, andSensorDef, sbolDoc);
				createDegradationInteraction(chap, andSensorDef);
			} else if (remoteOutputDef.containsRole(MyersOntology.TF)) {
				FunctionalComponent tf = createTFComponent(remoteOutput.getDisplayId(), 
						AccessType.PRIVATE, DirectionType.NONE, andSensorDef, sbolDoc);
				createDegradationInteraction(tf, andSensorDef);
			} else {
				return null;
			}
		}
		for (FunctionalComponent remoteOutput : remoteOutputs3) {
			ComponentDefinition remoteOutputDef = sbolDoc.getComponentDefinition(remoteOutput.getDefinitionURI());
			if (remoteOutputDef.containsRole(MyersOntology.CHAPERONE)) {
				FunctionalComponent chap = createChaperoneComponent(remoteOutput.getDisplayId(), 
						AccessType.PRIVATE, DirectionType.NONE, andSensorDef, sbolDoc);
				createDegradationInteraction(chap, andSensorDef);
			} else if (remoteOutputDef.containsRole(MyersOntology.TF)) {
				FunctionalComponent tf = createTFComponent(remoteOutput.getDisplayId(), 
						AccessType.PRIVATE, DirectionType.NONE, andSensorDef, sbolDoc);
				createDegradationInteraction(tf, andSensorDef);
			} else if (remoteOutputDef.containsRole(MyersOntology.REPORTER)) {
				FunctionalComponent reporter = createReporterComponent(remoteOutput.getDisplayId(), 
						AccessType.PUBLIC, DirectionType.OUT, andSensorDef, sbolDoc);
				createDegradationInteraction(reporter, andSensorDef);
			}
		}
		for (ModuleDefinition sensorDef : sensorDefs) {
			Module sensor = createSensorModule(sensorDef, andSensorDef);
			createIdenticalMappings(sensor, andSensorDef, sbolDoc);
		}
		for (ModuleDefinition andDef : andDefs) {
			Module andGate = createANDModule(andDef, andSensorDef);
			createIdenticalMappings(andGate, andSensorDef, sbolDoc);
		}
		return andSensorDef;
	}
	
	public static Module createSensorModule(ModuleDefinition sensorDef, ModuleDefinition parentModuleDef) {
		if (sensorDef.containsRole(MyersOntology.SENSOR)) {
			return createModule(sensorDef.getDisplayId(), sensorDef.getIdentity(), parentModuleDef);
		} else {
			return null;
		}
	}
	
	public static ModuleDefinition createSensorDefinition(String inducerID, String promoterID1, 
			String geneID1, String tfID, String promoterID2, String geneID2, String proteinID, 
			URI proteinRole, SBOLDocument sbolDoc, boolean isActivation) {
		List<String> geneIDs2 = new LinkedList<String>();
		List<String> proteinIDs = new LinkedList<String>();
		List<URI> proteinRoles = new LinkedList<URI>();
		geneIDs2.add(geneID2);
		proteinIDs.add(proteinID);
		proteinRoles.add(proteinRole);
		return createSensorDefinition(inducerID, promoterID1, 
			geneID1, tfID, promoterID2, geneIDs2, proteinIDs, 
			proteinRoles, sbolDoc, isActivation);
	}
	
	public static ModuleDefinition createSensorDefinition(String inducerID, String promoterID1, 
			String geneID1, String tfID, String promoterID2, List<String> geneIDs2, List<String> proteinIDs, 
			List<URI> proteinRoles, SBOLDocument sbolDoc, boolean isActivation) {
		ModuleDefinition sensorDef = createModuleDefinition(inducerID + "_Sensor", MyersOntology.SENSOR, 
				sbolDoc);
		FunctionalComponent inducer = createInducerComponent(inducerID, AccessType.PUBLIC, 
				DirectionType.IN, sensorDef, sbolDoc);
		FunctionalComponent promoter1 = createPromoterComponent(promoterID1, AccessType.PRIVATE, 
				DirectionType.NONE, sensorDef, sbolDoc);
		FunctionalComponent gene1 = createGeneComponent(geneID1, AccessType.PRIVATE, DirectionType.NONE,
				sensorDef, sbolDoc);
		FunctionalComponent tf = createTFComponent(tfID, AccessType.PRIVATE, DirectionType.NONE,
				sensorDef, sbolDoc);
		Set<String> ligandIDs = new HashSet<String>();
		ligandIDs.add(inducerID);
		ligandIDs.add(tfID);
		FunctionalComponent complex = createComplexTFComponent(ligandIDs, AccessType.PRIVATE, 
				DirectionType.NONE, sensorDef, sbolDoc);
		FunctionalComponent promoter2 = createPromoterComponent(promoterID2, AccessType.PRIVATE, 
				DirectionType.NONE, sensorDef, sbolDoc);
		List<FunctionalComponent> genes2 = new LinkedList<FunctionalComponent>();
		for (String geneID : geneIDs2) {
			genes2.add(createGeneComponent(geneID, AccessType.PRIVATE, DirectionType.NONE,
				sensorDef, sbolDoc));
		}
		List<FunctionalComponent> proteins = new LinkedList<FunctionalComponent>();
		for (int i = 0; i < proteinIDs.size(); i++) {
			proteins.add(createProteinComponent(proteinIDs.get(i), proteinRoles.get(i), AccessType.PUBLIC,
					DirectionType.OUT, sensorDef, sbolDoc));
		}
		createProductionInteraction(promoter1, gene1, tf, sensorDef);
		createDegradationInteraction(tf, sensorDef);
		createBindingInteraction(tf, inducer, complex, sensorDef);
		createDegradationInteraction(complex, sensorDef);
		if (isActivation) {
			createActivationInteraction(complex, promoter2, sensorDef);
		} else {
			createRepressionInteraction(tf, promoter2, sensorDef);
		}
		for (int i = 0; i < proteins.size(); i++) {
			createProductionInteraction(promoter2, genes2.get(i), proteins.get(i), sensorDef);
			createDegradationInteraction(proteins.get(i), sensorDef);
		}
		return sensorDef;
	}
	
	public static Module createANDModule(ModuleDefinition andDef, ModuleDefinition parentModuleDef) {
		if (andDef.containsRole(MyersOntology.AND)) {
			return createModule(andDef.getDisplayId(), andDef.getIdentity(), parentModuleDef);
		} else {
			return null;
		}
	}
	
	public static ModuleDefinition createANDDefinition(String chapID, String tfID, String promoterID, 
			String geneID, String proteinID, URI proteinRole, SBOLDocument sbolDoc) {
		ModuleDefinition andDef = createModuleDefinition(chapID + "_AND_" + tfID, MyersOntology.AND, 
				sbolDoc);
		FunctionalComponent chaperone = createChaperoneComponent(chapID, AccessType.PUBLIC, 
				DirectionType.IN, andDef, sbolDoc);
		FunctionalComponent tf = createTFComponent(tfID, AccessType.PUBLIC, DirectionType.IN,
				andDef, sbolDoc);
		Set<String> ligandIDs = new HashSet<String>();
		ligandIDs.add(chapID);
		ligandIDs.add(tfID);
		FunctionalComponent complexTF = createComplexTFComponent(ligandIDs, AccessType.PRIVATE, 
				DirectionType.NONE, andDef, sbolDoc);
		FunctionalComponent promoter = createPromoterComponent(promoterID, AccessType.PRIVATE, 
				DirectionType.NONE, andDef, sbolDoc);
		FunctionalComponent gene = createGeneComponent(geneID, AccessType.PRIVATE, DirectionType.NONE,
				andDef, sbolDoc);
		FunctionalComponent protein = createProteinComponent(proteinID, proteinRole, AccessType.PUBLIC,
				DirectionType.OUT, andDef, sbolDoc);
		createBindingInteraction(chaperone, tf, complexTF, andDef);
		createDegradationInteraction(complexTF, andDef);
		createActivationInteraction(complexTF, promoter, andDef);
		createProductionInteraction(promoter, gene, protein, andDef);
		createDegradationInteraction(protein, andDef);
		return andDef;
	}
	
	public static ModuleDefinition createANDDefinition(String chapID1, String chapID2, String tfID, String promoterID, 
			String geneID, String proteinID, URI proteinRole, SBOLDocument sbolDoc) {
		ModuleDefinition andDef = createModuleDefinition(chapID1 + "_AND_" + tfID, MyersOntology.AND, 
				sbolDoc);
		FunctionalComponent chap1 = createChaperoneComponent(chapID1, AccessType.PUBLIC, 
				DirectionType.IN, andDef, sbolDoc);
		FunctionalComponent chap2 = createChaperoneComponent(chapID2, AccessType.PUBLIC, 
				DirectionType.IN, andDef, sbolDoc);
		FunctionalComponent tf = createTFComponent(tfID, AccessType.PUBLIC, DirectionType.IN,
				andDef, sbolDoc);
		Set<String> ligandIDs1 = new HashSet<String>();
		ligandIDs1.add(chapID2);
		ligandIDs1.add(tfID);
		FunctionalComponent complex1 = createComplexComponent(ligandIDs1, AccessType.PRIVATE, 
				DirectionType.NONE, andDef, sbolDoc);
		Set<String> ligandIDs2 = new HashSet<String>();
		ligandIDs2.add(chapID1);
		ligandIDs2.add(chapID2);
		FunctionalComponent complex2 = createComplexComponent(ligandIDs2, AccessType.PRIVATE, 
				DirectionType.NONE, andDef, sbolDoc);
//		Set<String> ligandIDs3 = new HashSet<String>();
//		ligandIDs3.add(chapID1);
//		ligandIDs3.add(chapID2);
//		ligandIDs3.add(tfID);
//		FunctionalComponent complex3 = createComplexComponent(ligandIDs3, SBOLOntology.PRIVATE, 
//				SBOLOntology.NONE, ANDDef, sbolDoc);
		FunctionalComponent promoter = createPromoterComponent(promoterID, AccessType.PRIVATE, 
				DirectionType.NONE, andDef, sbolDoc);
		FunctionalComponent gene = createGeneComponent(geneID, AccessType.PRIVATE, DirectionType.NONE,
				andDef, sbolDoc);
		FunctionalComponent protein = createProteinComponent(proteinID, proteinRole, AccessType.PUBLIC,
				DirectionType.OUT, andDef, sbolDoc);
		createBindingInteraction(chap2, tf, complex1, andDef);
		createBindingInteraction(chap1, chap2, complex2, andDef);
		createDegradationInteraction(complex1, andDef);
		createDegradationInteraction(complex2, andDef);
//		createBindingInteraction(complex1, chap1, complex3, ANDDef);
//		createBindingInteraction(complex2, tf, complex3, ANDDef);
		createActivationInteraction(tf, promoter, andDef);
		createProductionInteraction(promoter, gene, protein, andDef);
		createDegradationInteraction(protein, andDef);
		return andDef;
	}
	
	public static FunctionalComponent createPromoterComponent(String promoterID, AccessType access, DirectionType direction,
			ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		return createDNAComponent(promoterID, SequenceOntology.PROMOTER, access, direction, moduleDef,
				sbolDoc);
	}
	
	public static FunctionalComponent createGeneComponent(String geneID, AccessType access, DirectionType direction,
			ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		FunctionalComponent gene = createDNAComponent(geneID, MyersOntology.GENE, access, direction,
				moduleDef, sbolDoc);
		ComponentDefinition geneDef = sbolDoc.getComponentDefinition(gene.getDefinitionURI());
		Component rbs = createRBSComponent("RBS", AccessType.PRIVATE, geneDef, sbolDoc);
		String cdsID = geneID;
		if (cdsID.equals("rfp")) {
			cdsID = "c" + cdsID.toUpperCase();
		} else {
			cdsID = "c" + cdsID.substring(0, 1).toUpperCase() + geneID.substring(1);
		}
		Component cds = createCDSComponent(cdsID, AccessType.PRIVATE, geneDef, sbolDoc);
		Component terminator = createTerminatorComponent("Terminator", AccessType.PRIVATE, geneDef, sbolDoc);
		int constraintCount = 1;
		createPrecedes(constraintCount, rbs, cds, geneDef);
		constraintCount++;
		createPrecedes(constraintCount, cds, terminator, geneDef);
		return gene;
	}
	
	public static Component createRBSComponent(String rbsID, AccessType access, 
			ComponentDefinition parentCompDef, SBOLDocument sbolDoc) {
		return createDNAComponent(rbsID, SequenceOntology.FIVE_PRIME_UTR, access, parentCompDef,
				sbolDoc);
	}
	
	public static Component createCDSComponent(String cdsID, AccessType access,
			ComponentDefinition parentCompDef, SBOLDocument sbolDoc) {
		return createDNAComponent(cdsID, SequenceOntology.CDS, access, parentCompDef,
				sbolDoc);
	}
	
	public static Component createTerminatorComponent(String terminatorID, AccessType access,
			ComponentDefinition parentCompDef, SBOLDocument sbolDoc) {
		return createDNAComponent(terminatorID, SequenceOntology.TERMINATOR, access, parentCompDef,
				sbolDoc);
	}
	
	public static FunctionalComponent createDNAComponent(String dnaID, URI dnaRole, AccessType access, 
			DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		ComponentDefinition dnaDef = createDNADefinition(dnaID, dnaRole, sbolDoc);
		return createFunctionalComponent(dnaID, dnaDef.getIdentity(), access, direction, moduleDef);
	}
	
	public static Component createDNAComponent(String dnaID, URI dnaRole, AccessType access, 
			ComponentDefinition parentCompDef, SBOLDocument sbolDoc) {
		ComponentDefinition dnaDef = createDNADefinition(dnaID, dnaRole, sbolDoc);
		return createComponent(dnaID, dnaDef.getIdentity(), access, parentCompDef); 
	}
	
	public static ComponentDefinition createDNADefinition(String dnaID, URI dnaRole, SBOLDocument sbolDoc) {
		return createComponentDefinition(dnaID, dnaRole, ChEBI.DNA, sbolDoc);
	}
	
	public static FunctionalComponent createTFComponent(String tfID, AccessType access, DirectionType direction,
			ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		return createProteinComponent(tfID, MyersOntology.TF, access, direction, moduleDef, sbolDoc);
	}
	
	public static FunctionalComponent createChaperoneComponent(String chapID, AccessType access, 
			DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		return createProteinComponent(chapID, MyersOntology.CHAPERONE, access, direction, moduleDef, 
				sbolDoc);
	}
	
	public static FunctionalComponent createReporterComponent(String reporterID, AccessType access, 
			DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		return createProteinComponent(reporterID, MyersOntology.REPORTER, access, direction, moduleDef, 
				sbolDoc);
	}
	
	public static FunctionalComponent createProteinComponent(String id, URI role, AccessType access, 
			DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		ComponentDefinition proteinDef = createProteinDefinition(id, role, sbolDoc);
		return createFunctionalComponent(id, proteinDef.getIdentity(), access, direction, moduleDef);
	} 
	
	public static ComponentDefinition createProteinDefinition(String id, URI role, SBOLDocument sbolDoc) {
		return createComponentDefinition(id, role, ChEBI.PROTEIN, sbolDoc);
	}
	
	public static FunctionalComponent createInducerComponent(String inducerID, AccessType access, 
			DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		return createSmallMoleculeComponent(inducerID, MyersOntology.INDUCER, access, direction, 
				moduleDef, sbolDoc);
	}
	
	public static FunctionalComponent createSmallMoleculeComponent(String smallMoleID, URI smallMoleRole, 
			AccessType access, DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		ComponentDefinition smallMoleDef = createSmallMoleculeDefinition(smallMoleID, smallMoleRole, 
				sbolDoc);
		return createFunctionalComponent(smallMoleID, smallMoleDef.getIdentity(), access, direction, 
				moduleDef);
	}
	
	public static ComponentDefinition createSmallMoleculeDefinition(String smallMoleID, URI smallMoleRole, 
			SBOLDocument sbolDoc) {
		return createComponentDefinition(smallMoleID, smallMoleRole, ChEBI.EFFECTOR, sbolDoc);
	}
	
	public static FunctionalComponent createComplexComponent(Set<String> ligandIDs, 
			AccessType access, DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		return createComplexComponent(ligandIDs, SystemsBiologyOntology.NON_COVALENT_BINDING, access, direction, moduleDef, sbolDoc);
	}
	
	public static FunctionalComponent createComplexTFComponent(Set<String> ligandIDs, 
			AccessType access, DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		return createComplexComponent(ligandIDs, MyersOntology.TF, access, direction, moduleDef, sbolDoc);
	}
	
	public static FunctionalComponent createComplexComponent(Set<String> ligandIDs, URI complexRole, 
			AccessType access, DirectionType direction, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
		ComponentDefinition complexDef = createComplexDefinition(ligandIDs, complexRole, sbolDoc);
		String complexID = "Complex";
		for (String ligandID : ligandIDs) {
			complexID = ligandID + "_" + complexID;
		}
		return createFunctionalComponent(complexID, complexDef.getIdentity(), access, direction, 
				moduleDef); 
	}
	
	public static ComponentDefinition createComplexDefinition(Set<String> ligandIDs, URI complexRole, 
			SBOLDocument sbolDoc) {
		String complexID = "Complex";
		for (String ligandID : ligandIDs) {
			complexID = ligandID + "_" + complexID;
		}
		return createComponentDefinition(complexID, complexRole, 
				ChEBI.NON_COVALENTLY_BOUND_MOLECULAR_ENTITY, sbolDoc);
	}
	
	public static Interaction createBindingInteraction(FunctionalComponent ligand1, FunctionalComponent ligand2, 
			FunctionalComponent complex, ModuleDefinition moduleDef) {
		Set<FunctionalComponent> ligands = new HashSet<FunctionalComponent>();
		ligands.add(ligand1);
		ligands.add(ligand2);
		return createBindingInteraction(ligands, complex, moduleDef);
	}
	
	public static Interaction createBindingInteraction(Set<FunctionalComponent> ligands, FunctionalComponent complex,
			ModuleDefinition moduleDef) {
		String bindingID = "Binding";
		for (FunctionalComponent ligand : ligands) {
			bindingID = ligand.getDisplayId() + "_" + bindingID;
		}
		Interaction binding = createInteraction(bindingID, SystemsBiologyOntology.NON_COVALENT_BINDING, moduleDef);
		int particiCount = 1;
		for (FunctionalComponent ligand : ligands) {
			createParticipation(particiCount, ligand.getDefinitionURI(), SystemsBiologyOntology.REACTANT, binding);
			particiCount++;
		}
		createParticipation(particiCount, complex.getDefinitionURI(), SystemsBiologyOntology.PRODUCT, binding);
		return binding; 
	}
	
	public static Interaction createRepressionInteraction(FunctionalComponent repressor, FunctionalComponent promoter, 
			ModuleDefinition moduleDef) {
		Interaction repression = createInteraction(promoter.getDisplayId() + "_Repression_via_" + repressor.getDisplayId(), 
				SystemsBiologyOntology.GENETIC_SUPPRESSION, moduleDef);
		int particiCount = 1;
		createParticipation(particiCount, repressor.getDefinitionURI(), SystemsBiologyOntology.INHIBITOR, repression);
		particiCount++;
		createParticipation(particiCount, promoter.getDefinitionURI(), SystemsBiologyOntology.PROMOTER, repression);
		return repression;
	}
	
	public static Interaction createActivationInteraction(FunctionalComponent activator, FunctionalComponent promoter, 
			ModuleDefinition moduleDef) {
		Interaction activation = createInteraction(promoter.getDisplayId() + "_Activation_via_" + activator.getDisplayId(), 
				SystemsBiologyOntology.GENETIC_ENHANCEMENT, moduleDef);
		int particiCount = 1;
		createParticipation(particiCount, activator.getDefinitionURI(), SystemsBiologyOntology.STIMULATOR, activation);
		particiCount++;
		createParticipation(particiCount, promoter.getDefinitionURI(), SystemsBiologyOntology.PROMOTER, activation);
		return activation;
	}
	
	public static Interaction createDegradationInteraction(FunctionalComponent degraded, ModuleDefinition moduleDef) {
		Interaction degradation = createInteraction(degraded.getDisplayId() + "_Degradation", 
				SystemsBiologyOntology.DEGRADATION, moduleDef);
		int particiCount = 1;
		createParticipation(particiCount, degraded.getDefinitionURI(), MyersOntology.DEGRADED, degradation);
		return degradation;
	}
	
	public static Interaction createProductionInteraction(FunctionalComponent promoter, FunctionalComponent gene,
			FunctionalComponent protein, ModuleDefinition moduleDef) {
		Interaction production = createInteraction(protein.getDisplayId() + "_Production_via_" + promoter.getDisplayId(), 
				SystemsBiologyOntology.GENETIC_PRODUCTION, moduleDef);
		int particiCount = 1;
		createParticipation(particiCount, promoter.getDefinitionURI(), SystemsBiologyOntology.PROMOTER, production);
		particiCount++;
		createParticipation(particiCount, gene.getDefinitionURI(), SystemsBiologyOntology.PROMOTER, production);
		particiCount++;
		createParticipation(particiCount, protein.getDefinitionURI(), SystemsBiologyOntology.PRODUCT, production);
		return production;
	}
	
	public static Set<MapsTo> createIdenticalMappings(Module module, ModuleDefinition parentModuleDef,
			SBOLDocument sbolDoc) {
		int mappingCount = 1;
		Set<MapsTo> mappings = new HashSet<MapsTo>();
		ModuleDefinition moduleDef = sbolDoc.getModuleDefinition(module.getDefinitionURI());
		for (FunctionalComponent remoteComp : moduleDef.getFunctionalComponents()) { 
			if (remoteComp.getAccess().equals(AccessType.PUBLIC)) {
				for (FunctionalComponent localComp : parentModuleDef.getFunctionalComponents()) {
					if (remoteComp.getDefinitionURI().equals(localComp.getDefinitionURI())) {
						mappings.add(createMapping(mappingCount, localComp.getDefinitionURI(), remoteComp.getDefinitionURI(),
								RefinementType.VERIFYIDENTICAL, module));
						mappingCount++;
					}
				}
			}
		}
		return mappings;
	}
	
	public static SequenceConstraint createPrecedes(int constraintCount,
			Component subjectComp, Component objectComp, ComponentDefinition compDef) {
		return createConstraint(constraintCount, RestrictionType.PRECEDES, subjectComp.getDefinitionURI(),
				objectComp.getDefinitionURI(), compDef);
	}
	
	public static ComponentDefinition createComponentDefinition(String compDefID, URI role, URI type, SBOLDocument sbolDoc) {
		try {
			URI compDefIdentity = new URI(GlobalConstants.SBOL_AUTHORITY_DEFAULT + "/cd/" + compDefID);
			ComponentDefinition compDef = sbolDoc.getComponentDefinition(compDefIdentity);
			if (compDef == null) {
				Set<URI> compDefRoles = new HashSet<URI>();
				compDefRoles.add(role);
				Set<URI> compDefType = new HashSet<URI>();
				compDefType.add(type);
				compDef = sbolDoc.createComponentDefinition(compDefID, "", compDefType);
				compDef.setTypes(compDefType);
				return compDef;
			} else {
				return compDef;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static FunctionalComponent createFunctionalComponent(String compID, URI compDefIdentity, AccessType access, DirectionType direction, 
			ModuleDefinition moduleDef) {
		try {
			URI compIdentity = new URI(moduleDef.getIdentity().toString() + "/" + compID);
			FunctionalComponent comp = moduleDef.getFunctionalComponent(compIdentity);
			if (comp == null) {
				comp = moduleDef.createFunctionalComponent(compID, access, compDefIdentity, direction);
				return comp;
			} else {
				return comp;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Component createComponent(String compID, URI compDefIdentity, AccessType access, 
			ComponentDefinition parentCompDef) {
		try {
			URI compIdentity = new URI(parentCompDef.getIdentity().toString() + "/" + compID);
			Component comp = parentCompDef.getComponent(compIdentity);
			if (comp == null) {
				comp = parentCompDef.createComponent(compID, access, compDefIdentity);
				return comp;
			} else {
				return comp;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static ModuleDefinition createModuleDefinition(String moduleDefID, URI role, SBOLDocument sbolDoc) {
		try {
			URI moduleDefIdentity = new URI(GlobalConstants.SBOL_AUTHORITY_DEFAULT + "/md/" + moduleDefID);
			ModuleDefinition moduleDef = sbolDoc.getModuleDefinition(moduleDefIdentity);
			if (moduleDef == null) {
				Set<URI> moduleDefRoles = new HashSet<URI>();
				moduleDefRoles.add(role);
				moduleDef = sbolDoc.createModuleDefinition(moduleDefID,"");
				moduleDef.setRoles(moduleDefRoles);
				return moduleDef;
			} else {
				return moduleDef;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Module createModule(String moduleID, URI moduleDefIdentity, ModuleDefinition parentModuleDef) {
		try {
			URI moduleIdentity = new URI(parentModuleDef.getIdentity().toString() + "/" + moduleID);
			Module module = parentModuleDef.getModule(moduleIdentity);
			if (module == null) {
				module = parentModuleDef.createModule(moduleID, moduleDefIdentity);
				return module;
			} else {
				return module;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static MapsTo createMapping(int mappingCount, URI localIdentity, URI remoteIdentity, RefinementType type, 
			Module module) {
		try {
			URI mappingIdentity = new URI(module.getIdentity().toString() + "/mapping_" + mappingCount );
			MapsTo mapping = module.getMapsTo(mappingIdentity);
			if (mapping == null) {
				return module.createMapsTo("mapping_"+mappingCount, type, localIdentity, remoteIdentity);
			} else {
				return mapping;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static SequenceConstraint createConstraint(int constraintCount, RestrictionType restriction, 
			URI subject, URI object, ComponentDefinition compDef) {
		try {
			URI constraintIdentity = new URI(compDef.getIdentity().toString() + "/constraint" + constraintCount);
			SequenceConstraint constraint = compDef.getSequenceConstraint(constraintIdentity);
			if (constraint == null) {
				return compDef.createSequenceConstraint("constraint"+constraintCount, restriction, subject, object);
			} else {
				return constraint;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Interaction createInteraction(String interactID, URI type, ModuleDefinition moduleDef) {
		try {
			URI interactIdentity = new URI(moduleDef.getIdentity().toString() + "/" + interactID);
			Interaction interact = moduleDef.getInteraction(interactIdentity);
			if (interact == null) {
				Set<URI> interactType = new HashSet<URI>();
				interactType.add(type);
				interact = moduleDef.createInteraction(interactID, interactType);
				return interact;
			} else {
				return interact;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Participation createParticipation(int particiCount, URI participant, URI role, Interaction interact) {
		try {
			URI particiIdentity = new URI(interact.getIdentity().toString() + "/participation_" + particiCount);
			Participation partici = interact.getParticipation(particiIdentity);
			if (partici == null) {
				Set<URI> particiRoles = new HashSet<URI>();
				particiRoles.add(role);
				partici = interact.createParticipation("participation_"+particiCount, participant,role);
				return partici;
			} else {
				return partici;
			}
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	//Gate proportions: [inverter yes nor or nand and]
	public static List<BioModel> createTestLibrary(int[] gateTotals, String[] gateTypes, String projectDirectory) {
		List<BioModel> gateLibrary = new LinkedList<BioModel>();
		int[] gateCounts = gateTotals.clone();
		int[] gateSums = new int[gateCounts.length];
		gateSums[0] = gateCounts[0];
		for (int i = 1; i < gateSums.length; i++)
			gateSums[i] = gateCounts[i] + gateSums[i - 1];
		Random rGen = new Random();
		while (gateSums[gateSums.length - 1] > 0) {
			int target = rGen.nextInt(gateSums[gateSums.length - 1]);
			int i = 0;
			while (target >= gateSums[i])
				i++;
			String gateID = gateTypes[i] + (gateTotals[i] - gateCounts[i]);
			gateLibrary.add(createTestGate(gateID, projectDirectory));
			gateCounts[i] -= 1;
			gateSums[0] = gateCounts[0];
			for (int j = 1; j < gateSums.length; j++)
				gateSums[j] = gateCounts[j] + gateSums[j - 1];
		} 
//		for (int i = 0; i < gateTypes.length; i++) 
//			for (int j = 0; j < gateCounts.length; j++) 
//				gateLibrary.add(createTestGate(gateTypes[i] + gateCounts[j], projectDirectory));
		return gateLibrary;
	}
	
	public static Set<DnaComponent> annotateTestLibrary(int cdsCopyNum, List<BioModel> gateLibrary) {
		Set<DnaComponent> libraryComps = new HashSet<DnaComponent>();
		DnaComponent previousCDS = null;
		Random rGen = new Random();
		try {
			DnaComponent sbolRBS = createTestDNAComponent(
					new URI(GlobalConstants.SBOL_AUTHORITY_DEFAULT + "/" + "RBS"),
					"RBS", GlobalConstants.SO_RBS, rGen, GlobalConstants.RBS_LENGTH, 0);
			libraryComps.add(sbolRBS);
			DnaComponent sbolTT = createTestDNAComponent(
					new URI(GlobalConstants.SBOL_AUTHORITY_DEFAULT + "/" + "TT"),
					"TT", GlobalConstants.SO_TERMINATOR, rGen, GlobalConstants.TERMINATOR_LENGTH, 0);
			libraryComps.add(sbolTT);
			for (int i = 0; i < gateLibrary.size(); i++) {
				BioModel gateModel = gateLibrary.get(i);
				String gateID = gateModel.getSBMLDocument().getModel().getId();
				for (String promoterID : gateModel.getPromoters()) {
					Reaction sbmlPromoter = gateModel.getProductionReaction(promoterID);
					DnaComponent sbolPromoter = createTestDNAComponent(
							new URI(GlobalConstants.SBOL_AUTHORITY_DEFAULT + "/" + gateID + "_" + promoterID), 
							gateID + "_" + promoterID, GlobalConstants.SO_PROMOTER, rGen, 
							GlobalConstants.MEAN_PROMOTER_LENGTH, GlobalConstants.SD_PROMOTER_LENGTH);
					libraryComps.add(sbolPromoter);
					List<URI> annotationObject = new LinkedList<URI>();
					annotationObject.add(sbolPromoter.getURI());
					AnnotationUtility.setSBOLAnnotation(sbmlPromoter, 
							new SBOLAnnotation(sbmlPromoter.getMetaId(), annotationObject, 
									GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND));
				}
				boolean isFirstCDS = true;
				for (String speciesID : gateModel.getInputSpecies()) {
					Species sbmlSpecies = gateModel.getSBMLDocument().getModel().getSpecies(speciesID);
					DnaComponent sbolCDS;
					if (isFirstCDS) {
						if (i % cdsCopyNum > 0) { 
							sbolCDS = previousCDS;
						} else {
							sbolCDS = createTestDNAComponent(
									new URI(GlobalConstants.SBOL_AUTHORITY_DEFAULT + "/" + gateID + "_" + speciesID),
									gateID + "_" + speciesID, GlobalConstants.SO_CDS, rGen, 
									GlobalConstants.MEAN_CDS_LENGTH, GlobalConstants.SD_CDS_LENGTH);
							libraryComps.add(sbolCDS);
							previousCDS = sbolCDS;
						}
						isFirstCDS = false;
					} else {
						sbolCDS = createTestDNAComponent(
								new URI(GlobalConstants.SBOL_AUTHORITY_DEFAULT + "/" + gateID + "_" + speciesID),
								gateID + "_" + speciesID, GlobalConstants.SO_CDS, rGen, 
								GlobalConstants.MEAN_CDS_LENGTH, GlobalConstants.SD_CDS_LENGTH);
						libraryComps.add(sbolCDS);
					}
					List<URI> annotationObject = new LinkedList<URI>();
					annotationObject.add(sbolRBS.getURI());
					annotationObject.add(sbolCDS.getURI());
					annotationObject.add(sbolTT.getURI());
					AnnotationUtility.setSBOLAnnotation(sbmlSpecies, 
							new SBOLAnnotation(sbmlSpecies.getMetaId(), annotationObject));
				}
					
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return libraryComps;
	}
	
//	public Set<DnaComponent> annotateTestLibrary(int crossGateTotal, int avgCDSCopyNum, int sdCDSCopyNum,
//			List<BioModel> gateLibrary) {
//		Set<DnaComponent> libraryComps = new HashSet<DnaComponent>();
//		List<DnaComponent> cdsComps = new LinkedList<DnaComponent>();
//		Random rGen = new Random();
//		int cdsCopyNum = 1;
//		int cdsCopyCap = (int) Math.round(avgCDSCopyNum + sdCDSCopyNum*rGen.nextGaussian());
//		try {
//			DnaComponent sbolRBS = createTestDNAComponent(
//					new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "/" + "RBS"),
//					"RBS", GlobalConstants.SO_RBS, rGen, 12, 0);
//			libraryComps.add(sbolRBS);
//			DnaComponent sbolTT = createTestDNAComponent(
//					new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "/" + "TT"),
//					"TT", GlobalConstants.SO_TERMINATOR, rGen, 12, 0);
//			libraryComps.add(sbolTT);
//			for (int i = 0; i < gateLibrary.size(); i++) {
//				BioModel gateModel = gateLibrary.get(i);
//				String gateID = gateModel.getSBMLDocument().getModel().getId();
//				for (String promoterID : gateModel.getPromoters()) {
//					Reaction sbmlPromoter = gateModel.getProductionReaction(promoterID);
//					DnaComponent sbolPromoter = createTestDNAComponent(
//							new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "/" + gateID + "_" + promoterID), 
//							gateID + "_" + promoterID, GlobalConstants.SO_PROMOTER, rGen, 62, 23);
//					libraryComps.add(sbolPromoter);
//					List<URI> annotationObject = new LinkedList<URI>();
//					annotationObject.add(sbolPromoter.getURI());
//					AnnotationUtility.setSBOLAnnotation(sbmlPromoter, 
//							new SBOLAnnotation(sbmlPromoter.getMetaId(), annotationObject));
//				}
//				boolean hasCrossTalk = cdsComps.size() > 0 && i < crossGateTotal;
//				for (String speciesID : gateModel.getInputSpecies()) {
//					Species sbmlSpecies = gateModel.getSBMLDocument().getModel().getSpecies(speciesID);
//					DnaComponent sbolCDS;
//					if (hasCrossTalk) { 
//						sbolCDS = cdsComps.get(0);
//						cdsCopyNum++;
//						if (cdsCopyNum >= cdsCopyCap) {
//							cdsComps.remove(0);
//							cdsCopyNum = 1;
//							cdsCopyCap = (int) Math.round(avgCDSCopyNum + sdCDSCopyNum*rGen.nextGaussian());
//						}
//						hasCrossTalk = false;
//					} else {
//						sbolCDS = createTestDNAComponent(
//								new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "/" + gateID + "_" + speciesID),
//								gateID + "_" + speciesID, GlobalConstants.SO_CDS, rGen, 695, 268);
//						libraryComps.add(sbolCDS);
//						cdsComps.add(0, sbolCDS);
//					}
//					List<URI> annotationObject = new LinkedList<URI>();
//					annotationObject.add(sbolRBS.getURI());
//					annotationObject.add(sbolCDS.getURI());
//					annotationObject.add(sbolTT.getURI());
//					AnnotationUtility.setSBOLAnnotation(sbmlSpecies, 
//							new SBOLAnnotation(sbmlSpecies.getMetaId(), annotationObject));
//				}
//					
//			}
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
//		return libraryComps;
//	}
	
	private static BioModel createTestGate(String gateID, String projectDirectory) {
		BioModel gateModel = new BioModel(projectDirectory);
		gateModel.createSBMLDocument(gateID, false, false);
		
		String promoterID;
		if (gateID.startsWith("N"))
			promoterID = "Pr";
		else
			promoterID = "Pa";
		gateModel.createPromoter(promoterID, 20, 20, true);
		
		String inputID = "Si";
		gateModel.createSpecies(inputID, 10, 10);
		gateModel.createDirPort(inputID, GlobalConstants.INPUT);
		
		String outputID = "So";
		gateModel.createSpecies(outputID, 30, 30);
		gateModel.createDirPort(outputID, GlobalConstants.OUTPUT);
		
		if (gateID.startsWith("INV")) 
			createInverter(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("YES")) 
			createYesGate(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("NOR")) 
			createNorGate(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("OR")) 
			createOrGate(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("NAND")) 
			createNandGate(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("AND")) 
			createAndGate(gateModel, promoterID, inputID, outputID);
		return gateModel;
	}
	
	private static DnaComponent createTestDNAComponent(URI uri, String displayID, String soType, Random rGen,
			int avgSize, int sdSize) {
		DnaComponent dnaComp = new DnaComponentImpl();
		dnaComp.setURI(uri);
		dnaComp.setDisplayId(displayID);
		dnaComp.addType(SequenceOntology.type(soType));
		DnaSequence dnaSeq = new DnaSequenceImpl();
		try {
			dnaSeq.setURI(new URI(uri.toString() + "_seq"));
		int size;
		do {
			size = (int) Math.round(avgSize + sdSize*rGen.nextGaussian());
		} while (size <= 0);
		dnaSeq.setNucleotides(createTestNucleotides(size));
		dnaComp.setDnaSequence(dnaSeq);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return dnaComp;
		
	}
	
	private static String createTestNucleotides(int size) {
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0 ; i < size; i++) {
			sb.append('a');
		}
		return sb.toString();
	}
	
	private static void createInverter(BioModel gateModel, String promoterID, String inputID, String outputID) {
		gateModel.addRepressorToProductionReaction(promoterID, inputID, outputID, null, null, null);
	}

	private static void createYesGate(BioModel bioGate, String promoterID, String inputID, String outputID) {
		bioGate.addActivatorToProductionReaction(promoterID, inputID, outputID, null, null, null);
	}

	private static void createNorGate(BioModel gateModel, String promoterID, String inputID, String outputID) {
		String inputID2 = inputID + "2";
		gateModel.createSpecies(inputID2, 20, 10);
		gateModel.createDirPort(inputID2, GlobalConstants.INPUT);
		gateModel.addRepressorToProductionReaction(promoterID, inputID, outputID, null, null, null);
		gateModel.addRepressorToProductionReaction(promoterID, inputID2, "none", null, null, null);
	}

	private static void createOrGate(BioModel gateModel, String promoterID, String inputID, String outputID) {
		String inputID2 = inputID + "2";
		gateModel.createSpecies(inputID2, 20, 10);
		gateModel.createDirPort(inputID2, GlobalConstants.INPUT);
		gateModel.addActivatorToProductionReaction(promoterID, inputID, outputID, null, null, null);
		gateModel.addActivatorToProductionReaction(promoterID, inputID2, "none", null, null, null);
	}

	private static void createNandGate(BioModel gateModel, String promoterID, String inputID, String outputID) {
		String inputID2 = inputID + "2";
		gateModel.createSpecies(inputID2, 20, 10);
		gateModel.createDirPort(inputID2, GlobalConstants.INPUT);
		String complexID = inputID + inputID2;
		gateModel.createSpecies(complexID, 15, 15);
		gateModel.createComplexReaction(complexID, null, false);
		gateModel.addReactantToComplexReaction(inputID, complexID, null, "1");
		gateModel.addReactantToComplexReaction(inputID2, complexID, null, "1");
		gateModel.addRepressorToProductionReaction(promoterID, complexID, outputID, null, null, null);
	}

	private static void createAndGate(BioModel gateModel, String promoterID, String inputID, String outputID) {
		String inputID2 = inputID + "2";
		gateModel.createSpecies(inputID2, 20, 10);
		gateModel.createDirPort(inputID2, GlobalConstants.INPUT);
		String complexID = inputID + inputID2;
		gateModel.createSpecies(complexID, 15, 15);
		gateModel.createComplexReaction(complexID, null, false);
		gateModel.addReactantToComplexReaction(inputID, complexID, null, "1");
		gateModel.addReactantToComplexReaction(inputID2, complexID, null, "1");
		gateModel.addActivatorToProductionReaction(promoterID, complexID, outputID, null, null, null);
	}

}

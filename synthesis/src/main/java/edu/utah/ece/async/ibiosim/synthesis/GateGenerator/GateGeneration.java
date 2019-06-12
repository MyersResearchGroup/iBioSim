package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

import edu.utah.ece.async.ibiosim.conversion.VPRModelGenerator;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GateIdentifier;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate.GateType;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;

/**
 * Class to automate the process of building genetic logic gates from a list of transcriptional units.  
 * @author Tramy Nguyen
 */
public class GateGeneration {
	
	private SBOLUtility sbolUtility;
	private List<GeneticGate> gateList;
	private int mdId;
	
	public GateGeneration() {
		this.sbolUtility = SBOLUtility.getInstance();
		this.gateList = new ArrayList<>();
	}

	public List<SBOLDocument> generateGatesFromTranscriptionalUnits(List<SBOLDocument> transcriptionalUnitList, String SynBioHubRepository) throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException {
		List<SBOLDocument> enrichedTU_list = new ArrayList<>();
		for(SBOLDocument document : transcriptionalUnitList) {
			List<URI> templateURIs = getTemplateFromCombinatorialDerivation(document);
			int counter = 1;
			for(ComponentDefinition rootCD : document.getRootComponentDefinitions()) {
				if(templateURIs.contains(rootCD.getIdentity())) {
					continue;
				}
				SBOLDocument tuDesign = sbolUtility.createSBOLDocument();
				document.createRecursiveCopy(tuDesign, rootCD);
				SBOLDocument enrichedTU = VPRModelGenerator.generateModel(SynBioHubRepository, tuDesign, getEnrichedMdId() + "_" + rootCD.getDisplayId());
				enrichedTU_list.add(enrichedTU);
				System.out.println(counter++ + ": " + rootCD.getDisplayId());
			}
		}
		return enrichedTU_list;
	}
	
	private List<URI> getTemplateFromCombinatorialDerivation(SBOLDocument doc) {
		List<URI> templateURIs = new ArrayList<>();
		for(CombinatorialDerivation combinDeriv : doc.getCombinatorialDerivations()) {
			templateURIs.add(combinDeriv.getTemplateURI());
		}
		return templateURIs;
	}
	
	public void identifyGeneratedGates(List<SBOLDocument> enrichedTU_list) throws GateGenerationExeception, SBOLValidationException {
		for(SBOLDocument enrichedTuDoc : enrichedTU_list) {
			for(ModuleDefinition mdGate : enrichedTuDoc.getRootModuleDefinitions()) {
				GateIdentifier gateUtil = new GateIdentifier(enrichedTuDoc, mdGate);
				GeneticGate gate = gateUtil.getIdentifiedGate();
				gateList.add(gate);
			}
		}
	}
	
	public void generateWiredORGates() throws SBOLValidationException, SBOLException, GateGenerationExeception {
		List<GeneticGate> notGates = getGatesWithType(GateType.NOT);
		WiredGateGenerator generator = new WiredGateGenerator();
		for(GeneticGate gate : notGates) {
			NOTGate not = (NOTGate) gate;
			for(FunctionalComponent input : not.getListOfInputs()) {
				ModuleDefinition wiredNotGate = generator.createWiredOrGate(input.getDefinition(), not.getSBOLDocument());
				GateIdentifier gateUtil = new GateIdentifier(not.getSBOLDocument(), wiredNotGate);
				GeneticGate notGate = gateUtil.getIdentifiedGate();
				gateList.add(notGate);;
			}
			for(FunctionalComponent output : not.getListOfOutputs()) {
				ModuleDefinition wiredNotGate = generator.createWiredOrGate(output.getDefinition(), not.getSBOLDocument());
				GateIdentifier gateUtil = new GateIdentifier(not.getSBOLDocument(), wiredNotGate);
				GeneticGate notGate = gateUtil.getIdentifiedGate();
				gateList.add(notGate);
			}
		}
	}
	
	public List<GeneticGate> getGatesWithType(GateType gateType) {
		List<GeneticGate> gateList = new ArrayList<>();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(gateType)) {
				gateList.add(gate);
			}
		}
		return gateList;
	}
	
	public SBOLDocument getLibraryAsSbol(List<GeneticGate> listOfGates) throws SBOLValidationException {
		SBOLDocument library = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : listOfGates) {
			library.createCopy(gate.getSBOLDocument());
		}
		return library;
	}
	
	public List<GeneticGate> getLibrary(){
		return this.gateList;
	}
	
	public void exportLibrary(List<GeneticGate> listOfGates, String fullPath) throws SBOLValidationException, IOException, SBOLConversionException {
		SBOLDocument libraryDocument = getLibraryAsSbol(listOfGates);
		SBOLWriter.write(libraryDocument, fullPath);
	}
	
	private String getEnrichedMdId() {
		return "TopLevel" + mdId++;
	}
	
	
	
}

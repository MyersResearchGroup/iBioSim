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

	public List<SBOLDocument> enrichedTU(List<SBOLDocument> transcriptionalUnitList, String SynBioHubRepository) throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException {
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
	
	public void sortEnrichedTUList(List<SBOLDocument> enrichedTU_list) throws GateGenerationExeception, SBOLValidationException {
		for(SBOLDocument enrichedTU : enrichedTU_list) {
			for(ModuleDefinition mdGate : enrichedTU.getRootModuleDefinitions()) {
				GateIdentifier gateUtil = new GateIdentifier(enrichedTU, mdGate);
				GeneticGate gate = gateUtil.createGate();
				gateList.add(gate);
			}
		}
	}
	
	public void createWiredNOTGates() throws SBOLValidationException {
		List<GeneticGate> notGates = getGatesWithType(GateType.NOT);
		for(GeneticGate gate : notGates) {
			NOTGate not = (NOTGate) gate;
			List<FunctionalComponent> gateOutputs = not.getListOfOutputs();
			if(gateOutputs.size() == 1) {
				FunctionalComponent signal = gateOutputs.get(0);
				
			}
		}
	}
	
	private List<GeneticGate> getGatesWithType(GateType gateType) {
		List<GeneticGate> gateList = new ArrayList<>();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(gateType)) {
				gateList.add(gate);
			}
		}
		return gateList;
	}
	
	public SBOLDocument getNOTLibrary() throws SBOLValidationException {
		SBOLDocument notLibrary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.NOT)) {
				notLibrary.createCopy(gate.getSBOLDocument());
			}
		}
		return notLibrary;
	}
	
	public SBOLDocument getNORLibrary() throws SBOLValidationException {
		SBOLDocument norLibrary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.NOR)) {
				norLibrary.createCopy(gate.getSBOLDocument());
			}
		}
		return norLibrary;
	}
	
	public SBOLDocument getORLibrary() throws SBOLValidationException {
		SBOLDocument orLibrary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.OR)) {
				orLibrary.createCopy(gate.getSBOLDocument());
			}
		}
		return orLibrary;
	}
	
	public SBOLDocument getNANDLibrary() throws SBOLValidationException {
		SBOLDocument nandLibrary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.NAND)) {
				nandLibrary.createCopy(gate.getSBOLDocument());
			}
		}
		return nandLibrary;
	}
	
	public SBOLDocument getANDLibrary() throws SBOLValidationException {
		SBOLDocument andLibrary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.AND)) {
				andLibrary.createCopy(gate.getSBOLDocument());
			}
		}
		return andLibrary;
	}
	
	public SBOLDocument getNOTSUPPORTEDLibrary() throws SBOLValidationException {
		SBOLDocument notSupportedLibrary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.NOTSUPPORTED)) {
				notSupportedLibrary.createCopy(gate.getSBOLDocument());
			}
		}
		return notSupportedLibrary;
	}
	
	public SBOLDocument getLibrary() throws SBOLValidationException {
		SBOLDocument library = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			library.createCopy(gate.getSBOLDocument());
		}
		return library;
	}
	
	public List<GeneticGate> getGeneticGateList(){
		return this.gateList;
	}
	
	public void exportLibrary(SBOLDocument libraryDocument, String fullPath) throws IOException, SBOLConversionException {
		SBOLWriter.write(libraryDocument, fullPath);
	}
	
	private String getEnrichedMdId() {
		return "TopLevel" + mdId++;
	}
	
	
	
}

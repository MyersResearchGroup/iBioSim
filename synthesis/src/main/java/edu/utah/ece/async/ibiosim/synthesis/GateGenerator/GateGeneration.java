package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sbolstandard.core2.ComponentDefinition;
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

/**
 * Class to automate the process of building genetic logic gates from a list of transcriptional units.  
 * @author Tramy Nguyen
 */
public class GateGeneration {
	
	private SBOLUtility sbolUtility;
	private List<GeneticGate> gateList;
	
	public GateGeneration() {
		this.sbolUtility = SBOLUtility.getInstance();
		this.gateList = new ArrayList<>();
	}

	public List<SBOLDocument> enrichedTU(List<SBOLDocument> transcriptionalUnitList, String SynBioHubRepository) throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException {
		List<SBOLDocument> enrichedTU_list = new ArrayList<>();
		for(SBOLDocument document : transcriptionalUnitList) {
			for(ComponentDefinition rootCD : document.getRootComponentDefinitions()) {
				SBOLDocument tuDesign = sbolUtility.createSBOLDocument();
				document.createRecursiveCopy(tuDesign, rootCD);
				SBOLDocument enrichedTU = VPRModelGenerator.generateModel(SynBioHubRepository, tuDesign, "");
				enrichedTU_list.add(enrichedTU);
			}
		}
		return enrichedTU_list;
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
		SBOLDocument norLibary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.NOR)) {
				norLibary.createCopy(gate.getSBOLDocument());
			}
		}
		return norLibary;
	}
	
	public SBOLDocument getORLibrary() throws SBOLValidationException {
		SBOLDocument orLibary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.OR)) {
				orLibary.createCopy(gate.getSBOLDocument());
			}
		}
		return orLibary;
	}
	
	public SBOLDocument getNANDLibrary() throws SBOLValidationException {
		SBOLDocument nandLibary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.NAND)) {
				nandLibary.createCopy(gate.getSBOLDocument());
			}
		}
		return nandLibary;
	}
	
	public SBOLDocument getANDLibrary() throws SBOLValidationException {
		SBOLDocument andLibary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.AND)) {
				andLibary.createCopy(gate.getSBOLDocument());
			}
		}
		return andLibary;
	}
	
	public SBOLDocument getNOTSUPPORTEDLibrary() throws SBOLValidationException {
		SBOLDocument notSupportedLibary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gateList) {
			if(gate.getType().equals(GateType.NOTSUPPORTED)) {
				notSupportedLibary.createCopy(gate.getSBOLDocument());
			}
		}
		return notSupportedLibary;
	}
	
	public List<GeneticGate> getGeneticGateList(){
		return this.gateList;
	}
	
	public void exportLibrary(SBOLDocument libraryDocument, String fullPath) throws IOException, SBOLConversionException {
		SBOLWriter.write(libraryDocument, fullPath);
	}
	
}

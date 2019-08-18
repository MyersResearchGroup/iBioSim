package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SystemsBiologyOntology;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

import edu.utah.ece.async.ibiosim.conversion.VPRModelGenerator;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GateIdentifier;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NORGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate.GateType;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ORGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.WiredORGate;

/**
 * Class to automate the process of building genetic logic gates from a list of transcriptional units.  
 * @author Tramy Nguyen
 */
public class GateGeneration {
	
	private SBOLUtility sbolUtility;
	private List<GeneticGate> gateList;
	private int mdId;
	
	public GateGeneration() {
		this.sbolUtility = SBOLUtility.getSBOLUtility();
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
				if(gate instanceof NOTGate || gate instanceof NORGate 
					|| gate instanceof ANDGate || gate instanceof NANDGate 
					|| gate instanceof ORGate || gate instanceof WiredORGate) {
					removeInputDegradationProtein(gate);
				}
				gateList.add(gate);
			}
		}
	}
	
	private boolean hasDegradation(ModuleDefinition gateMd) {
		for(Module m : gateMd.getModules()) {
			ModuleDefinition interactionMd = m.getDefinition();
			for(Interaction inter : interactionMd.getInteractions()) {
				if(inter.getTypes().contains(SystemsBiologyOntology.DEGRADATION)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean removeInputDegradationProtein(GeneticGate gate) throws SBOLValidationException {
		for(FunctionalComponent fc : gate.getListOfInputs()) {
			if(isFunctionalComponentProtein(fc) && hasDegradation(gate.getModuleDefinition())) {
				assert(removeDegradation(fc.getIdentity(), gate.getSBOLDocument(), gate.getModuleDefinition()));
			}
		}
		return true;
	}
	
	private boolean isFunctionalComponentProtein(FunctionalComponent fc) {
		ComponentDefinition cd = fc.getDefinition();
		return cd.getTypes().contains(ComponentDefinition.PROTEIN);
	}
	
	private boolean removeDegradation(URI fcUri, SBOLDocument gateDocument, ModuleDefinition gateMD) throws SBOLValidationException {
		Module module = getModuleWithDegradation(fcUri, gateMD.getModules());
		return gateDocument.removeModuleDefinition(module.getDefinition()) && gateMD.removeModule(module);
	}
	
	private Module getModuleWithDegradation(URI fcUri, Set<Module> moduleList) {
		List<Module> degModules = new ArrayList<>();
		for(Module m : moduleList) {
			ModuleDefinition interactionMd = m.getDefinition();
			for(MapsTo mt : m.getMapsTos()) {
				if(mt.getLocalIdentity().equals(fcUri)) {
					for(Interaction inter : interactionMd.getInteractions()) {
						if(inter.getTypes().contains(SystemsBiologyOntology.DEGRADATION)) {
							degModules.add(m);
						}
					}
					
				}
			}
			
		}
		assert(degModules.size() == 1);
		return degModules.get(0);
	}
	
	public List<GeneticGate> generateWiredORGates(List<GeneticGate> notGates) throws SBOLValidationException, SBOLException, GateGenerationExeception {
		WiredGateGenerator generator = new WiredGateGenerator();
		List<GeneticGate> wiredGates = new ArrayList<>();
		List<ComponentDefinition> signals = new ArrayList<>();
		for(GeneticGate gate : notGates) {
			NOTGate not = (NOTGate) gate;
			for(ComponentDefinition cd : not.getListOfOutputsAsComponentDefinition())
			{
				if(!signals.contains(cd))
				{
					ModuleDefinition wiredOrGate = generator.createWiredOrGate(cd, not.getSBOLDocument());
					GateIdentifier gateUtil = new GateIdentifier(not.getSBOLDocument(), wiredOrGate);
					GeneticGate orGate = gateUtil.getIdentifiedGate();
					signals.add(cd);
					wiredGates.add(orGate);
					gateList.add(orGate);
				}
			}
		}
		return wiredGates;
	}
	
	public List<GeneticGate> getGates(GateType gateType) {
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

package sbol.assembly;

import java.net.URI;
import java.util.HashMap;

import org.sbml.jsbml.Species;
import org.sbolstandard.core.component_option1.Component;
import org.sbolstandard.core.component_option1.ComponentInstantiation;
import org.sbolstandard.core.component_option1.sequence.SequenceComponent;
import org.sbolstandard.core.module_option1.Module;
import org.sbolstandard.core.util.SequenceOntology;

import sbol.util.ChEBI;

import biomodel.parser.BioModel;

public class ModelGenerator {
	
	private String separator;
	
	public ModelGenerator(String separator) {
		this.separator = separator;
	}
	
	public BioModel generateModel(Module sourceModule, String generatedFilePath) {
		BioModel generatedModel = new BioModel(generatedFilePath);
		generatedModel.createSBMLDocument(sourceModule.getDisplayId(), false, false);
		generatedModel.setSBMLFile(sourceModule.getDisplayId() + ".xml");
		generateSpecies(sourceModule, generatedModel);
		HashMap<String, Species> idToSpecies = generateSpecies(sourceModule, generatedModel);
		
		generatedModel.save(generatedFilePath + separator + generatedModel.getSBMLFile());
		return generatedModel;
	}
	
	private HashMap<String, Species> generateSpecies(Module sourceModule, BioModel generatedModel) {
		HashMap<String, Species> idToSpecies = new HashMap<String, Species>();
		for (ComponentInstantiation compInstantiation : sourceModule.getComponentInstantiations()) {
			Component comp = compInstantiation.getInstantiatedComponent();
//			if (comp.getType().equals(ChEBI.DNA) && comp instanceof SequenceComponent)
//				for (URI seqType : ((SequenceComponent) comp).getSequenceTypes())
//					if (seqType.equals(SequenceOntology.PROMOTER))
//						;
			String speciesID = "";
			if (comp.getType().equals(ChEBI.NON_COVALENTLY_BOUND_MOLECULAR_ENTITY) || 
					comp.getType().equals(ChEBI.PROTEIN) ||
					comp.getType().equals(ChEBI.EFFECTOR))
				speciesID = generatedModel.createSpecies(comp.getDisplayId(), 30, 30);
			if (speciesID.length() > 0) {
				Species generatedSpecies = generatedModel.getSBMLDocument().getModel().getSpecies(speciesID);
				if (comp.getName() != null)
					generatedSpecies.setName(comp.getName());
				idToSpecies.put(speciesID, generatedSpecies);
			}
		}
		return idToSpecies;
	}
	
	private void generatePromoters(Module sourceModule, BioModel generatedModel, HashMap<String, Species> idToSpecies) {
		for (ComponentInstantiation compInstantiation : sourceModule.getComponentInstantiations()) {
			Component comp = compInstantiation.getInstantiatedComponent();
			if (comp.getType().equals(ChEBI.DNA) && comp instanceof SequenceComponent)
				for (URI seqType : ((SequenceComponent) comp).getSequenceTypes())
					if (seqType.equals(SequenceOntology.PROMOTER)) {
						;
					}
		}
	}

}

package backend.biomodel.visitor;

import java.util.HashMap;
import java.util.Properties;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;

import backend.biomodel.network.BaseSpecies;
import backend.biomodel.network.ComplexSpecies;
import backend.biomodel.network.ConstantSpecies;
import backend.biomodel.network.DiffusibleConstitutiveSpecies;
import backend.biomodel.network.DiffusibleSpecies;
import backend.biomodel.network.SpasticSpecies;
import backend.biomodel.network.SpeciesInterface;
import backend.biomodel.util.SBMLutilities;
import backend.biomodel.util.Utility;
import backend.util.GlobalConstants;
import frontend.main.Gui;

public class PrintSpeciesVisitor extends AbstractPrintVisitor {

	public PrintSpeciesVisitor(SBMLDocument document,
			HashMap<String, SpeciesInterface> species, HashMap<String, Properties> compartments) {
		super(document);
		this.species = species;
		this.compartments = compartments;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface s : species.values()) {
			s.accept(this);
		}
	}
	
	@Override
	public void visitComplex(ComplexSpecies specie) {
		if (!complexAbstraction || (!specie.isAbstractable() && !specie.isSequesterAbstractable())) {
			loadValues(specie);
			String compartment = checkCompartments(specie.getId());
			Species s = Utility.makeSpecies(specie.getId(), compartment, amount, concentration);
//			s.setName(specie.getName()); this causes things to break...specie lacks name for some reason
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
		} else
			 document.getModel().removeSpecies(specie.getId());

	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		Species s = Utility.makeSpecies(specie.getId(), compartment, amount, concentration);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		Species s = Utility.makeSpecies(specie.getId(), compartment, amount, concentration);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		s.setBoundaryCondition(true);
		//s.setConstant(true);
		Utility.addSpecies(document, s);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		Species s = Utility.makeSpecies(specie.getId(), compartment, amount, concentration);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
		
		r = new org.sbml.jsbml.Reaction(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		r.setId("Constitutive_production_" + s.getId());
		r.setCompartment(compartment);
		r.addProduct(Utility.SpeciesReference(s.getId(), specie.getnp()));
		
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addLocalParameter(Utility.Parameter("kp", specie.getKo()));
		kl.setMath(SBMLutilities.myParseFormula("kp"));
		Utility.addReaction(document, r);		
	}
	
	@Override
	public void visitDiffusibleSpecies(DiffusibleSpecies species) {
		
		loadValues(species);
		String compartment = checkCompartments(species.getId());
		Species s = Utility.makeSpecies(species.getId(), compartment, amount, concentration);
		s.setName(species.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
	}
	
	@Override
	public void visitDiffusibleConstitutiveSpecies(DiffusibleConstitutiveSpecies specie) {
		
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		Species s = Utility.makeSpecies(specie.getId(), compartment, amount, concentration);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
		
		r = new org.sbml.jsbml.Reaction(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		r.setId("Constitutive_production_" + s.getId());
		r.setCompartment(compartment);
		r.addProduct(Utility.SpeciesReference(s.getId(), Double.parseDouble(parameters.getParameter(GlobalConstants.STOICHIOMETRY_STRING))));
		
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addLocalParameter(Utility.Parameter("kp", Double.parseDouble(parameters
					.getParameter((GlobalConstants.OCR_STRING)))));	
		kl.setMath(SBMLutilities.myParseFormula("kp"));
		Utility.addReaction(document, r);	
	}
	
	private void loadValues(SpeciesInterface specie) {
		amount = specie.getInitialAmount();
		concentration = specie.getInitialConcentration();
	}
	
	//Checks if species belongs in a compartment other than default
	private String checkCompartments(String species) {
		
		String compartment = document.getModel().getCompartment(0).getId();
		
		if (compartments != null) {
			//String[] splitted = species.split("__");
			String component = species;
			
			while (component.contains("__")) {
				
				component = component.substring(0, component.lastIndexOf("__"));
				
				for (String compartmentName : compartments.keySet()) {
					
					if (compartmentName.equals(component))
						return compartmentName;					
					else if (compartmentName.contains("__") && compartmentName.substring(0, compartmentName.lastIndexOf("__"))
							.equals(component)) {
						return compartmentName;
					}
				}
			}
			/*
			if (compartments.contains(splitted[0]))
				compartment = splitted[0];
				*/
		}
		return compartment;
	}
	
	
	private double amount;
	private double concentration;
	private HashMap<String, Properties> compartments;

}


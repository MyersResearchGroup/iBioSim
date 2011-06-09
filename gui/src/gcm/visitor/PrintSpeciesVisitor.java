package gcm.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import main.Gui;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;

import gcm.network.BaseSpecies;
import gcm.network.ComplexSpecies;
import gcm.network.ConstantSpecies;
import gcm.network.SpasticSpecies;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

public class PrintSpeciesVisitor extends AbstractPrintVisitor {

	public PrintSpeciesVisitor(SBMLDocument document,
			HashMap<String, SpeciesInterface> species, ArrayList<String> compartments) {
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
			Species s = Utility.makeSpecies(specie.getId(), compartment, init);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
		}

	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
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
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
		
		r = new org.sbml.libsbml.Reaction(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		r.setId("Constitutive_production_" + s.getId());
		
		r.addProduct(Utility.SpeciesReference(s.getId(), Double.parseDouble(parameters.getParameter(GlobalConstants.STOICHIOMETRY_STRING))));
		
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kp", Double.parseDouble(parameters
					.getParameter((GlobalConstants.OCR_STRING)))));	
		kl.setFormula("kp");
		Utility.addReaction(document, r);		
	}
	
	private void loadValues(SpeciesInterface specie) {
		init = specie.getInit();
	}
	
	//Checks if species belongs in a compartment other than default
	private String checkCompartments(String species) {
		String compartment = document.getModel().getCompartment(0).getId();
		String[] splitted = species.split("__");
		if (compartments != null) {
			if (compartments.contains(splitted[0]))
				compartment = splitted[0];
		}
		return compartment;
	}
	
	
	private double init;
	private ArrayList<String> compartments;

}


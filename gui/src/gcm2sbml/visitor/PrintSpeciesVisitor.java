package gcm2sbml.visitor;

import java.util.ArrayList;
import java.util.Collection;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import biomodelsim.BioSim;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.ComplexSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

public class PrintSpeciesVisitor extends AbstractPrintVisitor {

	public PrintSpeciesVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species, ArrayList<String> compartments) {
		super(document);
		this.species = species;
		this.compartments = compartments;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {		
		for (SpeciesInterface s : species) {
			s.accept(this);
		}
	}
	
	@Override
	public void visitComplex(ComplexSpecies specie) {
		if (!complexAbstraction) {
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
		
		org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		r.setId("Constitutive_production_" + s.getId());
		
		r.addProduct(Utility.SpeciesReference(s.getId(), Double.parseDouble(parameters.getParameter(GlobalConstants.STOICHIOMETRY_STRING))));
		
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
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
		String compartment = "default";
		String[] splitted = species.split("__");
		if (compartments.contains(splitted[0]))
			compartment = splitted[0];
		return compartment;
	}
	
	private double init;
	private Collection<SpeciesInterface> species;
	private ArrayList<String> compartments;

}


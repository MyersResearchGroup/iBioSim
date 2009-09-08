package gcm2sbml.visitor;

import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

public class PrintSpeciesVisitor extends AbstractPrintVisitor {

	public PrintSpeciesVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species, String compartment, double init) {
		super(document);
		this.defaultinit = init;
		this.species = species;
		this.compartment = compartment;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {		
		for (SpeciesInterface specie : species) {
			specie.accept(this);
		}
	}

	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	public void visitDimer(DimerSpecies specie) {
		loadValues(specie.getProperties());
		if (!dimerizationAbstraction) {
			Species s = Utility.makeSpecies(specie.getId(), compartment, init);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
		}

	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie.getProperties());
		if (!biochemicalAbstraction) {
			Species s = Utility.makeSpecies(specie.getId(), compartment, init);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
		}

	}

	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie.getProperties());
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie.getProperties());
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		s.setBoundaryCondition(true);
		//s.setConstant(true);
		Utility.addSpecies(document, s);
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie.getProperties());
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
		
		org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(2,4);
		r.setId("Spastic_production_" + s.getName());
		
		r.addProduct(Utility.SpeciesReference(s.getName(), Double.parseDouble(parameters.getParameter(GlobalConstants.STOICHIOMETRY_STRING))));
		
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kp", Double.parseDouble(parameters
					.getParameter((GlobalConstants.OCR_STRING)))));	
		kl.setFormula("kp");
		Utility.addReaction(document, r);		
	}
	
	private void loadValues(Properties property) {
		init = getProperty(GlobalConstants.INITIAL_STRING, property, defaultinit);
	}


	private double defaultinit = 0;
	private double init = 0;
	private Collection<SpeciesInterface> species = null;
	private String compartment = null;

}

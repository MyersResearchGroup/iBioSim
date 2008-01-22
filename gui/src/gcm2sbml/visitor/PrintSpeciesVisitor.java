package gcm2sbml.visitor;

import java.util.Collection;

import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.util.Utility;

public class PrintSpeciesVisitor extends AbstractPrintVisitor {

	public PrintSpeciesVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species, String compartment) {
		super(document);
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
		if (!dimerizationAbstraction) {
			Species s = Utility.makeSpecies(specie.getName(), compartment, specie.getInitial());
			s.setHasOnlySubstanceUnits(true);
			document.getModel().addSpecies(s);
		}

	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		if (!biochemicalAbstraction) {
			Species s = Utility.makeSpecies(specie.getName(), compartment, specie.getInitial());
			s.setHasOnlySubstanceUnits(true);
			document.getModel().addSpecies(s);
		}

	}

	public void visitBaseSpecies(BaseSpecies specie) {
		Species s = Utility.makeSpecies(specie.getName(), compartment, specie.getInitial());
		s.setHasOnlySubstanceUnits(true);
		document.getModel().addSpecies(s);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		Species s = Utility.makeSpecies(specie.getName(), compartment, specie.getInitial());
		s.setHasOnlySubstanceUnits(true);
		document.getModel().addSpecies(s);
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		Species s = Utility.makeSpecies(specie.getName(), compartment, specie.getInitial());
		s.setHasOnlySubstanceUnits(true);
		document.getModel().addSpecies(s);
	}

	private Collection<SpeciesInterface> species = null;
	private String compartment = null;

}

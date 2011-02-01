package gcm2sbml.visitor;

import java.util.ArrayList;
import java.util.Collection;

import gcm2sbml.network.ComplexSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.PartSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;

public class PrintComplexVisitor extends AbstractPrintVisitor {
	
	public PrintComplexVisitor(SBMLDocument document, Collection<SpeciesInterface> species,
			ArrayList<String> compartments) {
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
		this.kcomp = specie.getKc();
		String compartment = checkCompartments(specie.getId());
		Reaction r = Utility.Reaction("Complex_formation_" + specie.getId());
		r.setCompartment(compartment);
		String expression = "";
		r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		for (PartSpecies part : specie.getParts()) {
			SpeciesInterface s = part.getSpecies();
			double n = part.getStoich();
			r.addReactant(Utility.SpeciesReference(s.getId(), n));
			expression = expression + "*" + s.getId();
			if (n > 1) {
				expression = expression + '^' + coopString + "_" + s.getId();
				kl.addParameter(Utility.Parameter(coopString + "_" + s.getId(), n, "dimensionless"));
			}
		}
		expression = kcompString + expression;
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (kcomp.length > 1) {
			kl.addParameter(Utility.Parameter("kr", kcomp[1], GeneticNetwork.getMoleTimeParameter(1)));
			kl.addParameter(Utility.Parameter(kcompString, kcomp[0]/kcomp[1],
					GeneticNetwork.getMoleParameter(specie.getSize())));
		} else {
			kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
			kl.addParameter(Utility.Parameter(kcompString, kcomp[0],
					GeneticNetwork.getMoleParameter(specie.getSize())));
		}
		kl.setFormula("kr*" + expression + "-kr*" + specie.getId());
		Utility.addReaction(document, r);
	}
	
	//Checks if species belongs in a compartment other than default
	private String checkCompartments(String species) {
		String compartment = "default";
		String[] splitted = species.split("__");
		if (compartments.contains(splitted[0]))
			compartment = splitted[0];
		return compartment;
	}
	
	private double kcomp[];
	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
	private Collection<SpeciesInterface> species;
	private ArrayList<String> compartments;
}

package gcm.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import gcm.network.ComplexSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.PartSpecies;
import gcm.network.SpeciesInterface;
import gcm.parser.CompatibilityFixer;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;

public class PrintComplexVisitor extends AbstractPrintVisitor {
	
	public PrintComplexVisitor(SBMLDocument document, Collection<SpeciesInterface> species,
			ArrayList<String> compartments, HashMap<String, ArrayList<PartSpecies>> complexMap) {
		super(document);
		this.species = species;
		this.complexMap = complexMap;
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
		String ncSum = "";
		for (PartSpecies part : complexMap.get(specie.getId())) {
			SpeciesInterface s = part.getSpecies();
			double n = part.getStoich();
			r.addReactant(Utility.SpeciesReference(s.getId(), n));
			expression = expression + "*" + s.getId();
			if (n > 1) {
				expression = expression + '^' + coopString + "_" + s.getId();
			}
			kl.addParameter(Utility.Parameter(coopString + "_" + s.getId(), n, "dimensionless"));
			ncSum = ncSum + coopString + "_" + s.getId() + "+";
		}
		expression = kcompString + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-1)" + expression;
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (kcomp.length == 2) {
			kl.addParameter(Utility.Parameter("kr", kcomp[1], GeneticNetwork.getMoleTimeParameter(1)));
			kl.addParameter(Utility.Parameter(kcompString, kcomp[0]/kcomp[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork.getMoleTimeParameter(1)));
			kl.addParameter(Utility.Parameter(kcompString, kcomp[0],
					GeneticNetwork.getMoleParameter(2)));
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
	private HashMap<String, ArrayList<PartSpecies>> complexMap;
}

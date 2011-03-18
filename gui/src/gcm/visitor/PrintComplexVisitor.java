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
	
	public PrintComplexVisitor(SBMLDocument document, HashMap<String, SpeciesInterface> species,
			ArrayList<String> compartments, HashMap<String, ArrayList<PartSpecies>> complexMap, 
			HashMap<String, ArrayList<PartSpecies>> partsMap) {
		super(document);
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.compartments = compartments;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface s : species.values()) {
			if (!s.isAbstractable())
				s.accept(this);
		}
	}

	@Override
	public void visitComplex(ComplexSpecies specie) {
		String compartment = checkCompartments(specie.getId());
		r = Utility.Reaction("Complex_formation_" + specie.getId());
		r.setCompartment(compartment);
		r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		double[] kcomp = specie.getKc();
		double kr = 1;
		if (kcomp.length == 2)
			kr = kcomp[1];
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		String expression = "";
		complexReactants = new HashMap<String, Double>();
		complexModifiers = new ArrayList<String>();
		expression = abstractComplex(specie.getId(), 1);
		for (String reactant : complexReactants.keySet())
			r.addReactant(Utility.SpeciesReference(reactant, complexReactants.get(reactant)));
		for (String modifier : complexModifiers)
			r.addModifier(Utility.ModifierSpeciesReference(modifier));
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
	
	private ArrayList<String> compartments;
	
}

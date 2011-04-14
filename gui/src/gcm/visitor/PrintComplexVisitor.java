package gcm.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import gcm.network.ComplexSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.Influence;
import gcm.network.SpeciesInterface;
import gcm.parser.CompatibilityFixer;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;

public class PrintComplexVisitor extends AbstractPrintVisitor {
	
	public PrintComplexVisitor(SBMLDocument document, HashMap<String, SpeciesInterface> species,
			ArrayList<String> compartments, HashMap<String, ArrayList<Influence>> complexMap, 
			HashMap<String, ArrayList<Influence>> partsMap) {
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
			if (!complexAbstraction || (!s.isAbstractable() && !s.isSequesterAbstractable()))
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
		String complexMolecule = specie.getId();
		if (complexAbstraction) {
			expression = abstractComplex(specie.getId(), 1);
			if (specie.isSequesterable())
				complexMolecule = sequesterSpecies(specie.getId());
		} else {
			String kcompId = kcompString + "__" + specie.getId();
			//Checks if binding parameters are specified as forward and reverse rate constants or 
			//as equilibrium binding constants before adding to kinetic law
			if (kcomp.length == 2) {
				kl.addParameter(Utility.Parameter(kcompId, kcomp[0]/kcomp[1],
						GeneticNetwork.getMoleParameter(2)));
			} else {
				kl.addParameter(Utility.Parameter(kcompId, kcomp[0],
						GeneticNetwork.getMoleParameter(2)));
			}
			String ncSum = "";
			for (Influence infl : complexMap.get(specie.getId())) {
				String partId = infl.getInput();
				double n = infl.getCoop();
				r.addReactant(Utility.SpeciesReference(partId, n));
				String nId = coopString + "__" + partId + "_" + specie.getId();
				kl.addParameter(Utility.Parameter(nId, n, "dimensionless"));
				ncSum = ncSum + nId + "+";
				expression = expression + "*" + "(" + partId + ")^" + nId;
			}
			expression = kcompId + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-1)" + expression;	
		}
		kl.setFormula("kr*" + expression + "-kr*" + complexMolecule);
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
	
	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
	
}

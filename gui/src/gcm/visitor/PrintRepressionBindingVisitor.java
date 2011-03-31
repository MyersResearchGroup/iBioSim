package gcm.visitor;



import java.util.ArrayList;
import java.util.HashMap;



import gcm.network.BaseSpecies;
import gcm.network.ComplexSpecies;
import gcm.network.ConstantSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.PartSpecies;
import gcm.network.Promoter;
import gcm.network.Reaction;
import gcm.network.SpasticSpecies;
import gcm.network.SpeciesInterface;
import gcm.parser.CompatibilityFixer;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.SBMLDocument;

public class PrintRepressionBindingVisitor extends AbstractPrintVisitor {

	public PrintRepressionBindingVisitor(SBMLDocument document, Promoter p, HashMap<String, SpeciesInterface> species, 
			String compartment, 
			HashMap<String, ArrayList<PartSpecies>> complexMap, HashMap<String, ArrayList<PartSpecies>> partsMap) {
		super(document);
		this.promoter = p;
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.compartment = compartment;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : promoter.getRepressors()) {
			String repressor = specie.getId();
			String[] splitted = repressor.split("__");
			if (splitted.length == 2)
				repressor = splitted[1];
			productName = promoter.getId() + "_" + repressor + "_bound";
			reactionName = "R_repression_binding_" + promoter.getId() + "_" + repressor;
			specie.accept(this);
		}
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitComplex(ComplexSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(productName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (krep.length == 2) {
			kl.addParameter(Utility.Parameter(krepString, krep[0]/krep[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(krepString, krep[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String repMolecule = "";
		if (complexAbstraction && specie.isAbstractable()) {
			repMolecule = abstractComplex(specie.getId(), coop);
		} else if (complexAbstraction && specie.isSequesterable()) {
			repMolecule = sequesterSpecies(specie.getId());
			r.addReactant(Utility.SpeciesReference(repMolecule, coop));
		} else {
			repMolecule = specie.getId();
			r.addReactant(Utility.SpeciesReference(repMolecule, coop));
		}
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(generateLaw(productName, repMolecule));
		Utility.addReaction(document, r);
	}
	
	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(productName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (krep.length == 2) {
			kl.addParameter(Utility.Parameter(krepString, krep[0]/krep[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(krepString, krep[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String repMolecule = "";
		//Checks for valid complex sequestering of repressing species if complex abstraction is selected
		if (complexAbstraction && specie.isSequesterable()) {
			repMolecule = repMolecule + sequesterSpecies(specie.getId());
		} else {
			repMolecule = specie.getId();
		}
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.setFormula(generateLaw(productName, repMolecule));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(productName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (krep.length == 2) {
			kl.addParameter(Utility.Parameter(krepString, krep[0]/krep[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(krepString, krep[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String repMolecule = "";
		//Checks for valid complex sequestering of repressing species if complex abstraction is selected
		if (complexAbstraction && specie.isSequesterable()) {
			repMolecule = repMolecule + sequesterSpecies(specie.getId());
		} else {
			repMolecule = specie.getId();
		}
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.setFormula(generateLaw(productName, repMolecule));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(productName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (krep.length == 2) {
			kl.addParameter(Utility.Parameter(krepString, krep[0]/krep[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(krepString, krep[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String repMolecule = "";
		//Checks for valid complex sequestering of repressing species if complex abstraction is selected
		if (complexAbstraction && specie.isSequesterable()) {
			repMolecule = repMolecule + sequesterSpecies(specie.getId());
		} else {
			repMolecule = specie.getId();
		}
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		kl.setFormula(generateLaw(productName, repMolecule));
		Utility.addReaction(document, r);
	}

	/**
	 * Generates a kinetic law
	 * 
	 * @param specieName
	 *            specie name
	 * @param repMolecule
	 *            repressor molecule
	 * @return
	 */
	private String generateLaw(String specieName, String repMolecule) {
		String law = "kr*" + "(" + krepString + "*" + repMolecule + ")" + "^" + coopString + "*"
				+ promoter.getId() + "-kr*" + specieName;
		return law;
	}

	private void loadValues(SpeciesInterface s) {
		Reaction r = promoter.getRepressionMap().get(s.getId());
		coop = r.getCoop();
		krep = r.getRep();
		if (krep.length == 2)
			kr = krep[1];
		else
			kr = 1;
	}
		

	private Promoter promoter;
	
	private double coop;
	private double krep[];
	private double kr;

	private String krepString = GlobalConstants.KREP_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;

	private String productName;
	private String reactionName;
	private String compartment;
}


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

	public PrintRepressionBindingVisitor(SBMLDocument document, Promoter p, String compartment, 
			HashMap<String, ArrayList<PartSpecies>> complexMap) {
		super(document);
		this.promoter = p;
		this.complexMap = complexMap;
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
			speciesName = promoter.getId() + "_" + repressor + "_bound";
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
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
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
		if (complexAbstraction) {
			complexReactants = new HashMap<String, Double>();
			repMolecule = abstractComplex(specie, 1, "");
			for (String reactant : complexReactants.keySet())
				r.addReactant(Utility.SpeciesReference(reactant, complexReactants.get(reactant)));
		} else {
			repMolecule = specie.getId();
			r.addReactant(Utility.SpeciesReference(repMolecule, coop));
		}
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(generateLaw(speciesName, repMolecule));
		Utility.addReaction(document, r);
	}
	
	//Recursively breaks down repressing complex into its constituent species and complex formation equilibria
	private String abstractComplex(SpeciesInterface complex, double multiplier, String ncProduct) {
		String repMolecule = "";
		kcomp = complex.getKc();
		String kcompId = kcompString + "__" + complex.getId();
		if (kcomp.length == 2) {
			kl.addParameter(Utility.Parameter(kcompId, kcomp[0]/kcomp[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kcompId, kcomp[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		String ncSum = "";
		for (PartSpecies part : complexMap.get(complex.getId())) {
			SpeciesInterface s = part.getSpecies();
			double n = part.getStoich();
			String nId = coopString + "__" + s.getId() + "_" + complex.getId();
			kl.addParameter(Utility.Parameter(nId, n, "dimensionless"));
			ncSum = ncSum + nId + "+";
			if (complexMap.containsKey(s.getId())) {
				repMolecule = "*" + abstractComplex(s, multiplier * n, ncProduct + nId + "*") + repMolecule;
			} else {
				if (complexReactants.containsKey(s.getId()))
					complexReactants.put(s.getId(), complexReactants.get(s.getId()) + multiplier * n * coop);
				else 
					complexReactants.put(s.getId(), multiplier * n * coop);
				repMolecule = repMolecule + "*" + s.getId() + '^' + "(" + ncProduct + nId + ")";
			}
		}
		repMolecule = kcompId + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-1)" + repMolecule;	
		return repMolecule;
	}
	
	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
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
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
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
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
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
		kl.setFormula(generateLaw(speciesName, specie.getId()));
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
		kcomp = s.getKc();
	}
		

	private Promoter promoter;
	private HashMap<String, ArrayList<PartSpecies>> complexMap;
	private org.sbml.libsbml.Reaction r;
	private KineticLaw kl;
	private HashMap<String, Double> complexReactants;
	
	private double kcomp[];
	private double coop;
	private double krep[];
	private double kr;

	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
	private String krepString = GlobalConstants.KREP_STRING;

	private String speciesName;
	private String reactionName;
	private String compartment;
}


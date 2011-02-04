package gcm.visitor;



import gcm.network.BaseSpecies;
import gcm.network.ComplexSpecies;
import gcm.network.ConstantSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.NullSpecies;
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

public class PrintActivatedBindingVisitor extends AbstractPrintVisitor {

	public PrintActivatedBindingVisitor(SBMLDocument document, Promoter p, String compartment) {
		super(document);
		this.promoter = p;
		this.compartment = compartment;
		if (compartment.equals("default"))
			rnapName = "RNAP";
		else
			rnapName = compartment + "__RNAP";
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : promoter.getActivators()) {
			String activator = specie.getId();
			String[] splitted = activator.split("__");
			if (splitted.length == 2)
				activator = splitted[1];
			speciesName = promoter.getId() + "_" + activator + "_RNAP";
			reactionName = "R_RNAP_binding_" + promoter.getId() + "_" + activator;
			specie.accept(this);
		}
	}

	@Override
	public void visitNullSpecies(NullSpecies specie) {
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {
	}
	
	@Override
	public void visitComplex(ComplexSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(rnapName, 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (kArnap.length == 2) {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0]/kArnap[1], GeneticNetwork
					.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0], GeneticNetwork
					.getMoleParameter(2)));
		}
		if (kact.length == 2) {
			kl.addParameter(Utility.Parameter(kactString, kact[0]/kact[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kactString, kact[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actMolecule = "";
		if (complexAbstraction) {
			if (kcomp.length == 2) {
				kl.addParameter(Utility.Parameter(kcompString, kcomp[0]/kcomp[1],
						GeneticNetwork.getMoleParameter(2)));
			} else {
				kl.addParameter(Utility.Parameter(kcompString, kcomp[0],
						GeneticNetwork.getMoleParameter(2)));
			}
			String ncSum = "";
			for (PartSpecies part : specie.getParts()) {
				SpeciesInterface s = part.getSpecies();
				double n = part.getStoich();
				r.addReactant(Utility.SpeciesReference(s.getId(), n*coop));
				actMolecule = actMolecule + "*" + s.getId();
				if (n > 1) {
					actMolecule = actMolecule + '^' + coopString + "_" + s.getId();
				}
				kl.addParameter(Utility.Parameter(coopString + "_" + s.getId(), n, "dimensionless"));
				ncSum = ncSum + coopString + "_" + s.getId() + "+";
			}
			actMolecule = kcompString + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-1)" + actMolecule;
		} else {
			actMolecule = specie.getId();
			r.addReactant(Utility.SpeciesReference(actMolecule, coop));
		}
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(generateLaw(speciesName, actMolecule));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(rnapName, 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (kArnap.length == 2) {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0]/kArnap[1], GeneticNetwork
					.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0], GeneticNetwork
					.getMoleParameter(2)));
		}
		if (kact.length == 2) {
			kl.addParameter(Utility.Parameter(kactString, kact[0]/kact[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kactString, kact[0],
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
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(rnapName, 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (kArnap.length == 2) {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0]/kArnap[1], GeneticNetwork
					.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0], GeneticNetwork
					.getMoleParameter(2)));
		}
		if (kact.length == 2) {
			kl.addParameter(Utility.Parameter(kactString, kact[0]/kact[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kactString, kact[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));;
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		org.sbml.libsbml.Reaction r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addReactant(Utility.SpeciesReference(rnapName, 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (kArnap.length == 2) {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0]/kArnap[1], GeneticNetwork
					.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0], GeneticNetwork
					.getMoleParameter(2)));
		}
		if (kact.length == 2) {
			kl.addParameter(Utility.Parameter(kactString, kact[0]/kact[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kactString, kact[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		kl.addParameter(Utility.Parameter("kr", kr, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	private void loadValues(SpeciesInterface s) {
		Reaction r = promoter.getActivationMap().get(s.getId());
		kArnap = promoter.getKArnap();
		if (kArnap.length == 2)
			kr = kArnap[1];
		else
			kr = 1;
		coop = r.getCoop();
		kact = r.getAct();
		kcomp = s.getKc();
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
	private String generateLaw(String specieName, String actMolecule) {
		String law = "kr*" + kArnapString + "*" + "(" + kactString + "*" + actMolecule + ")" + "^"
				+ coopString + "*" + rnapName + "*" + promoter.getId() + "-kr*" + specieName;
		return law;
	}

	private Promoter promoter;

	private double[] kArnap;
	private double[] kcomp;
	private double coop;
	private double[] kact;
	private double kr;
	
	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
	private String kactString = GlobalConstants.KACT_STRING;
	private String kArnapString = GlobalConstants.ACTIVATED_RNAP_BINDING_STRING;

	private String speciesName;
	private String reactionName;
	private String rnapName;
	private String compartment;
}

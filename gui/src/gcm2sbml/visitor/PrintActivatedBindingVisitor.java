package gcm2sbml.visitor;



import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.ComplexSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.NullSpecies;
import gcm2sbml.network.PartSpecies;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.Reaction;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;
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
			if (splitted.length > 1)
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
		double kr = 1;
		if (kArnap.length > 1) {
			kr = kr * kArnap[1];
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0]/kArnap[1], GeneticNetwork
					.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0], GeneticNetwork
					.getMoleParameter(2)));
		}
		if (kact.length > 1) {
			kr = kr * kact[1];
			kl.addParameter(Utility.Parameter(kactString, kact[0]/kact[1],
					GeneticNetwork.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kactString, kact[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		kl.addParameter(Utility.Parameter(coopString, coop, "dimensionless"));
		String actMolecule = "";
		if (complexAbstraction) {
			if (kcomp.length > 1) {
				kr = kr * kcomp[1];
				kl.addParameter(Utility.Parameter(kcompString, kcomp[0]/kcomp[1],
						GeneticNetwork.getMoleParameter(specie.getSize())));
			} else {
				kl.addParameter(Utility.Parameter(kcompString, kcomp[0],
						GeneticNetwork.getMoleParameter(specie.getSize())));
			}
			for (PartSpecies part : specie.getParts()) {
				SpeciesInterface s = part.getSpecies();
				double n = part.getStoich();
				r.addReactant(Utility.SpeciesReference(s.getId(), n*coop));
				actMolecule = actMolecule + "*" + s.getId();
				if (n > 1)
					actMolecule = actMolecule + '^' + n;
			}
			actMolecule = kcompString + actMolecule;
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
		double kr = 1;
		if (kArnap.length > 1) {
			kr = kr * kArnap[1];
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0]/kArnap[1], GeneticNetwork
					.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0], GeneticNetwork
					.getMoleParameter(2)));
		}
		if (kact.length > 1) {
			kr = kr * kact[1];
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
		double kr = 1;
		if (kArnap.length > 1) {
			kr = kr * kArnap[1];
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0]/kArnap[1], GeneticNetwork
					.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0], GeneticNetwork
					.getMoleParameter(2)));
		}
		if (kact.length > 1) {
			kr = kr * kact[1];
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
		double kr = 1;
		if (kArnap.length > 1) {
			kr = kr * kArnap[1];
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0]/kArnap[1], GeneticNetwork
					.getMoleParameter(2)));
		} else {
			kl.addParameter(Utility.Parameter(kArnapString, kArnap[0], GeneticNetwork
					.getMoleParameter(2)));
		}
		if (kact.length > 1) {
			kr = kr * kact[1];
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
	
	private String kcompString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KCOMPLEX_STRING);
	private String coopString = CompatibilityFixer
			.getSBMLName(GlobalConstants.COOPERATIVITY_STRING);
	private String kactString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KACT_STRING);
	private String kArnapString = CompatibilityFixer
	.getSBMLName(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING);

	private String speciesName;
	private String reactionName;
	private String rnapName;
	private String compartment;
}

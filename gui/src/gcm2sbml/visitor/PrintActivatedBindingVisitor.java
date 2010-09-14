package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.NullSpecies;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;

public class PrintActivatedBindingVisitor extends AbstractPrintVisitor {

	public PrintActivatedBindingVisitor(SBMLDocument document, Promoter p,
			double act, double kdimer, double kcoop, double kbio, double dimer) {
		super(document);
		this.promoter = p;
		this.defaultact = act;
		this.defaultkdimer = kdimer;
		this.defaultkcoop = kcoop;
		this.defaultkbio = kbio;
		this.defaultdimer = dimer;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : promoter.getActivators()) {
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
	public void visitDimer(DimerSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(kcoopString, kcoop, "dimensionless"));
		String actName = "";
		if (dimerizationAbstraction) {
			kl.addParameter(Utility.Parameter(kdimerString, kdimer, GeneticNetwork
					.getMoleParameter((int) dimer)));
			kl.addParameter(Utility.Parameter(dimerString, dimer, "dimensionless"));
			actName = kdimerString + "*" + "(" + specie.getMonomer() + ")^"
					+ dimerString;
			r.addReactant(Utility.SpeciesReference(specie.getMonomer().getId(),
					dimer * kcoop));
		} else {
			actName = specie.getId();
			r.addReactant(Utility.SpeciesReference(actName, kcoop));
		}
		kl.addParameter(Utility.Parameter(actString, act,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(kcoopString, kcoop, "dimensionless"));
		String actName = "";
		if (biochemicalAbstraction) {
			String names = "";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(Utility.SpeciesReference(s.getId(), kcoop));
				names = names + "*" + s.getId();
			}

			kl.addParameter(Utility.Parameter(kbioString, kbio, GeneticNetwork
					.getMoleParameter(specie.getInputs().size())));
			actName = kbioString + names;
		} else {
			actName = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), kcoop));
		}
		kl.addParameter(Utility.Parameter(actString, act,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(kcoopString, kcoop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), kcoop));
		kl.addParameter(Utility.Parameter(actString, act,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(kcoopString, kcoop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), kcoop));
		kl.addParameter(Utility.Parameter(actString, act,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_RNAP_binding_" + promoter.getId() + "_"
				+ specie);
		r.addReactant(Utility.SpeciesReference("RNAP", 1));
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter("Krnap", 1, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(Utility.Parameter(kcoopString, kcoop, "dimensionless"));
		String actName = specie.getId();
		r.addReactant(Utility.SpeciesReference(specie.getId(), kcoop));
		kl.addParameter(Utility.Parameter(actString, act,
				GeneticNetwork.getMoleParameter(2)));
		r.addProduct(Utility.SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(specie.getId(), actName));
		Utility.addReaction(document, r);
	}

	private void loadValues(Properties property) {
		kdimer = getProperty(GlobalConstants.KASSOCIATION_STRING, property,
				defaultkdimer);
		kbio = getProperty(GlobalConstants.KBIO_STRING, property, defaultkbio);
		kcoop = getProperty(GlobalConstants.COOPERATIVITY_STRING, property,
				defaultkcoop);
		act = getProperty(GlobalConstants.KACT_STRING, property, defaultact);
		dimer = getProperty(GlobalConstants.MAX_DIMER_STRING, property,
				defaultdimer);
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
		String law = "kr*" + "Krnap*" + "(" + actString + "*" + actMolecule + ")" + "^"
				+ kcoopString + "*RNAP" + "*" + promoter.getId() + "-kr*" + "RNAP_"
				+ promoter.getId() + "_" + specieName;
		return law;
	}

	private String kdimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KASSOCIATION_STRING);
	private String kbioString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KBIO_STRING);
	private String kcoopString = CompatibilityFixer
			.getSBMLName(GlobalConstants.COOPERATIVITY_STRING);
	private String actString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KACT_STRING);
	private String dimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.MAX_DIMER_STRING);

	private Promoter promoter = null;

	private double defaultkdimer = .5;
	private double defaultkbio = .05;
	private double defaultkcoop = 1;
	private double defaultact = .033;
	private double defaultdimer = 1;

	private double dimer = 1;
	private double kdimer = .5;
	private double kbio = .05;
	private double kcoop = 1;
	private double act = .033;
}

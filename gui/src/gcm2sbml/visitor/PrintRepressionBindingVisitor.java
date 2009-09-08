package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SpeciesReference;

public class PrintRepressionBindingVisitor extends AbstractPrintVisitor {

	public PrintRepressionBindingVisitor(SBMLDocument document, Promoter p,
			double rep, double kdimer, double kcoop, double kbio, double dimer) {
		super(document);
		this.defaultrep = rep;
		this.promoter = p;
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
		for (SpeciesInterface specie : promoter.getRepressors()) {
			speciesName = "bound_" + promoter.getId() + "_" + specie.getId();
			reactionName = "R_repression_binding_" + promoter.getId() + "_"
					+ specie.getId();

			specie.accept(this);
		}
	}

	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	public void visitDimer(DimerSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		String repMolecule = "";

		if (dimerizationAbstraction) {
			kl.addParameter(Utility.Parameter(kdimerString, kdimer, GeneticNetwork
					.getMoleParameter((int) dimer)));
			repMolecule = kdimerString + "*" + "(" + specie.getMonomer() + ")^"
					+ reaction.getDimer();
			r.addReactant(Utility.SpeciesReference(specie.getMonomer().getId(),
					dimer * coop));
		} else {
			repMolecule = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.addParameter(Utility.Parameter(repString, Math.pow(rep, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 1)));
		kl.setFormula(generateLaw(speciesName, repMolecule));
		Utility.addReaction(document, r);
	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(repString, Math.pow(rep, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		String repMolecule = "";

		if (biochemicalAbstraction) {
			String names = "";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(Utility.SpeciesReference(s.getId(), coop));
				names = names + "*" + s.getId();
			}

			kl.addParameter(Utility.Parameter(kbioString, kbio, GeneticNetwork
					.getMoleParameter((int) specie.getInputs().size())));
			repMolecule = "(" + kbioString + names + ")^" + coop;
		} else {
			repMolecule = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.addParameter(Utility.Parameter(repString, Math.pow(rep, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 1)));
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(repString, Math.pow(rep, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(repString, Math.pow(rep, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(Utility.Parameter(repString, Math.pow(rep, coop),
				GeneticNetwork.getMoleTimeParameter((int) (coop) + 1)));
		kl.addParameter(Utility.Parameter(kcoopString, coop, "dimensionless"));
		r.addProduct(Utility.SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		kl.setFormula(generateLaw(speciesName, specie.getId()));
		Utility.addReaction(document, r);
	}

	/**
	 * Generates a kenetic law
	 * 
	 * @param specieName
	 *            specie name
	 * @param repMolecule
	 *            repressor molecule
	 * @return
	 */
	private String generateLaw(String specieName, String repMolecule) {
		String law = repString + "*" + repMolecule + "^" + kcoopString + "*"
				+ promoter.getId() + "-kr*" + specieName;
		return law;
	}

	private void loadValues(Properties property) {
		kdimer = getProperty(GlobalConstants.KASSOCIATION_STRING, property,
				defaultkdimer);
		kbio = getProperty(GlobalConstants.KBIO_STRING, property, defaultkbio);
		kcoop = getProperty(GlobalConstants.COOPERATIVITY_STRING, property,
				defaultkcoop);
		rep = getProperty(GlobalConstants.KREP_STRING, property, defaultrep);
		dimer = getProperty(GlobalConstants.MAX_DIMER_STRING, property,
				defaultdimer);
	}

	private Promoter promoter = null;

	private double kdimer = .5;
	private double kbio = .05;
	private double kcoop = 1;
	private double rep = .033;
	private double dimer = 1;

	private double defaultkdimer = .5;
	private double defaultkbio = .05;
	private double defaultkcoop = 1;
	private double defaultrep = .033;
	private double defaultdimer = 1;

	private String kdimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KASSOCIATION_STRING);
	private String kbioString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KBIO_STRING);
	private String kcoopString = CompatibilityFixer
			.getSBMLName(GlobalConstants.COOPERATIVITY_STRING);
	private String repString = CompatibilityFixer
			.getSBMLName(GlobalConstants.KREP_STRING);
	private String dimerString = CompatibilityFixer
			.getSBMLName(GlobalConstants.MAX_DIMER_STRING);

	private String speciesName = "";

	private String reactionName = "";
}

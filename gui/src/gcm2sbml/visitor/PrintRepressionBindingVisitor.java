package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
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
			Collection<SpeciesInterface> species, double rep, double kdimer,
			double kcoop, double kbio, double dimer) {
		super(document);
		this.defaultrep = rep;
		this.promoter = p;
		this.species = species;
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
		for (SpeciesInterface specie : species) {
			speciesName = "bound_" + promoter.getId() + "_"
					+ specie.getId();
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
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		if (dimerizationAbstraction) {
			kl.addParameter(new Parameter("rep", rep, GeneticNetwork
					.getMoleParameter(2)));
			r.addReactant(new SpeciesReference(specie.getMonomer().getId(),
					(int) dimer * coop));
			kl.addParameter(new Parameter("temp", 1, GeneticNetwork
					.getMoleTimeParameter(1)));			
			kl.addParameter(new Parameter("kdimer", kdimer, "dimensionless"));
			kl.setFormula("temp*(kdimer*rep*" + specie.getMonomer().getId() + ")^"
					+ (dimer * coop) + "*" + promoter.getId() + "-kr*"
					+ speciesName);
		} else {
			kl.addParameter(new Parameter("rep", rep, GeneticNetwork
					.getMoleParameter(2)));
			kl.addParameter(new Parameter("temp", 1, GeneticNetwork
					.getMoleTimeParameter(1)));
			r.addReactant(new SpeciesReference(specie.getId(), coop));
			kl.setFormula("temp*(rep*" + specie.getId() + ")^" + coop + "*"
					+ promoter.getId() + "-kr*" + speciesName);
		}
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);

	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getId(), 1));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		if (biochemicalAbstraction) {
			kl.addParameter(new Parameter("rep", rep, GeneticNetwork
					.getMoleParameter((int) coop * specie.getInputs().size()
							+ 1)));
			kl.addParameter(new Parameter("kbio", kbio, "dimensionless"));
			String names = "";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(new SpeciesReference(specie.getId(), coop));
				names = names + specie.getId() + "*";
			}
			names = names.substring(0, names.length() - 1);
			kl.addParameter(new Parameter("temp", 1, GeneticNetwork
					.getMoleTimeParameter(1)));
			kl.setFormula("temp*(kbio*" + names + "rep)^"+coop+"*" + promoter.getId()
					+ "-kr*" + speciesName);
		} else {
			kl.addParameter(new Parameter("rep", rep, GeneticNetwork
					.getMoleParameter(2)));
			kl.addParameter(new Parameter("temp", 1, GeneticNetwork
					.getMoleTimeParameter(1)));
			r.addReactant(new SpeciesReference(specie.getId(), coop));
			kl.setFormula("temp*(rep*" + specie.getId() + ")^" + coop + "*"
					+ promoter.getId() + "-kr*" + speciesName);
		}
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);
	}

	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getId(), 1));
		r.addReactant(new SpeciesReference(specie.getId(), coop));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("rep", rep, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(new Parameter("temp", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula("temp*(rep*" + specie.getId() + ")^" + coop + "*"
				+ promoter.getId() + "-kr*" + speciesName);
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getId(), 1));
		r.addReactant(new SpeciesReference(specie.getId(), coop));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("rep", rep, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(new Parameter("temp", 1, GeneticNetwork
				.getMoleTimeParameter(1)));		
		kl.setFormula("temp*(rep*" + specie.getId() + ")^" + coop + "*"
				+ promoter.getId() + "-kr*" + speciesName);
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getId(), 1));
		r.addReactant(new SpeciesReference(specie.getId(), coop));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("rep", rep, GeneticNetwork
				.getMoleParameter(2)));
		kl.addParameter(new Parameter("temp", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula("temp*(rep*" + specie.getId() + ")^" + coop + "*"
				+ promoter.getId() + "-kr*" + speciesName);
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);
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

	private String speciesName = "";

	private String reactionName = "";

	private Collection<SpeciesInterface> species = null;

}

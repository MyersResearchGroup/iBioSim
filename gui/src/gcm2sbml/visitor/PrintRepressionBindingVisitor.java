package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;

import java.util.Collection;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SpeciesReference;

public class PrintRepressionBindingVisitor extends AbstractPrintVisitor {

	public PrintRepressionBindingVisitor(SBMLDocument document, Promoter p,
			Collection<SpeciesInterface> species, double rep, double kdimer,
			double kcoop, double kbio) {
		super(document);
		this.rep = rep;
		this.promoter = p;
		this.species = species;
		this.kdimer = kdimer;
		this.kcoop = kcoop;
		this.kbio = kbio;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : species) {
			speciesName = "bound_" + promoter.getName() + "_"
					+ specie.getName();
			reactionName = "R_repression_binding_" + promoter.getName() + "_"
					+ specie.getName();
			specie.accept(this);
		}
	}

	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	public void visitDimer(DimerSpecies specie) {
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		if (dimerizationAbstraction) {
			kl.addParameter(new Parameter("rep", rep, GeneticNetwork
					.getMoleTimeParameter(specie.getDimerizationValue()
							* (int) coop + 1)));
			r.addReactant(new SpeciesReference(specie.getMonomer().getName(),
					specie.getDimerizationValue() * coop));
			if (!Double.isNaN(specie.getDimerizationConstant())) {
				kl.addParameter(new Parameter("kdimer", specie
						.getDimerizationConstant(), "dimensionless"));
			} else {
				kl
						.addParameter(new Parameter("kdimer", kdimer,
								"dimensionless"));
			}
			kl.setFormula("kdimer*rep*" + specie.getMonomer().getName() + "^"
					+ (specie.getDimerizationValue() * coop) + "*"
					+ promoter.getName() + "-kr*" + speciesName);
		} else {
			kl.addParameter(new Parameter("rep", rep, GeneticNetwork
					.getMoleTimeParameter((int) coop + 1)));
			r.addReactant(new SpeciesReference(specie.getName(), coop));
			kl.setFormula("rep*" + specie.getName() + "^" + coop + "*"
					+ promoter.getName() + "-kr*" + speciesName);
		}
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);

	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
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
				r.addReactant(new SpeciesReference(specie.getName(), coop));
				names = names + specie.getName() + "^" + coop + "*";
			}
			names = names.substring(0, names.length() - 1);
			kl.setFormula("kbio*rep*" + names + "*" + promoter.getName()
					+ "-kr*" + speciesName);
		} else {
			kl.addParameter(new Parameter("rep", rep, GeneticNetwork
					.getMoleParameter((int) coop + 1)));
			r.addReactant(new SpeciesReference(specie.getName(), coop));
			kl.setFormula("rep*" + specie.getName() + "^" + coop + "*"
					+ promoter.getName() + "-kr*" + speciesName);
		}
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	public void visitBaseSpecies(BaseSpecies specie) {
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		r.addReactant(new SpeciesReference(specie.getName(), coop));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("rep", rep, GeneticNetwork
				.getMoleTimeParameter((int) coop + 1)));
		kl.setFormula("rep*" + specie.getName() + "^" + coop + "*"
				+ promoter.getName() + "-kr*" + speciesName);
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		r.addReactant(new SpeciesReference(specie.getName(), coop));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("rep", rep, GeneticNetwork
				.getMoleTimeParameter((int) coop + 1)));
		kl.setFormula("rep*" + specie.getName() + "^" + coop + "*"
				+ promoter.getName() + "-kr*" + speciesName);
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		Reaction r = new Reaction(reactionName);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		r.addReactant(new SpeciesReference(specie.getName(), coop));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("rep", rep, GeneticNetwork
				.getMoleTimeParameter((int) coop + 1)));
		kl.setFormula("rep*" + specie.getName() + "^" + coop + "*"
				+ promoter.getName() + "-kr*" + speciesName);
		r.addProduct(new SpeciesReference(speciesName, 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	private Promoter promoter = null;

	private double kdimer = .5;

	private double kbio = .05;

	private double kcoop = 1;

	private double rep = .033;

	private String speciesName = "";

	private String reactionName = "";

	private Collection<SpeciesInterface> species = null;

}

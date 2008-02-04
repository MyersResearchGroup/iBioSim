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

public class PrintActivatedBindingVisitor extends AbstractPrintVisitor {

	public PrintActivatedBindingVisitor(SBMLDocument document, Promoter p,
			Collection<SpeciesInterface> species, double act, double kdimer,
			double kcoop, double kbio) {
		super(document);
		this.act = act;
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
			specie.accept(this);
		}
	}

	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	public void visitDimer(DimerSpecies specie) {
		Reaction r = new Reaction("R_RNAP_binding_" + promoter.getName() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference("RNAP", 1));
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		if (dimerizationAbstraction) {
			kl.addParameter(new Parameter("act", act, "dimensionless"));
			r.addReactant(new SpeciesReference(specie.getMonomer().getName(),
					specie.getDimerizationValue() * coop));
			if (!Double.isNaN(specie.getDimerizationConstant())) {
				kl.addParameter(new Parameter("kdimer", specie
						.getDimerizationConstant(),
						GeneticNetwork.getMoleTimeParameter(specie
								.getDimerizationValue() * 2 + 2)));
			} else {
				kl.addParameter(new Parameter("kdimer", kdimer, GeneticNetwork
						.getMoleTimeParameter(1)));
			}

			kl.setFormula("kdimer*act*" + "RNAP*"
					+ specie.getMonomer().getName() + "^"
					+ (specie.getDimerizationValue() * coop) + "*"
					+ promoter.getName() + "-kr*" + "RNAP_"
					+ promoter.getName() + "_" + specie.getName());
		} else {
			kl.addParameter(new Parameter("act", act, GeneticNetwork
					.getMoleTimeParameter(2 + (int) coop)));
			r.addReactant(new SpeciesReference(specie.getName(), coop));
			kl.setFormula("act*RNAP*" + specie.getName() + "^" + coop + "*"
					+ promoter.getName() + "-kr*" + "RNAP_"
					+ promoter.getName() + "_" + specie.getName());
		}
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getName() + "_"
				+ specie.getName(), 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);

	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		Reaction r = new Reaction("R_RNAP_binding_" + promoter.getName() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference("RNAP", 1));
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		if (biochemicalAbstraction) {
			kl.addParameter(new Parameter("act", act, "dimensionless"));
			kl.addParameter(new Parameter("kbio", kbio,
					GeneticNetwork.getMoleParameter((int) (specie.getInputs()
							.size()
							* coop + 2))));
			String names = "";
			for (SpeciesInterface s : specie.getInputs()) {
				r.addReactant(new SpeciesReference(specie.getName(), coop));
				names = names + specie.getName() + "^" + coop + "*";
			}
			names = names.substring(0, names.length() - 1);
			kl.setFormula("kbio*act*" + "RNAP*" + names + "*"
					+ promoter.getName() + "-kr*" + "RNAP_" + promoter.getName()
					+ "_" + specie.getName());
		} else {
			kl.addParameter(new Parameter("act", act, GeneticNetwork
					.getMoleTimeParameter((int) (2 + coop))));
			r.addReactant(new SpeciesReference(specie.getName(), coop));
			kl.setFormula("act*RNAP*" + specie.getName() + "^" + coop + "*"
					+ promoter.getName() + "-kr*" + "RNAP_" + promoter.getName()
					+ "_" + specie.getName());
		}
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getName() + "_"
				+ specie.getName(), 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	public void visitBaseSpecies(BaseSpecies specie) {
		Reaction r = new Reaction("R_RNAP_binding_" + promoter.getName() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference("RNAP", 1));
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		r.addReactant(new SpeciesReference(specie.getName(), coop));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("act", act, GeneticNetwork.getMoleTimeParameter(2+(int)coop)));
		kl.setFormula("act*" + "RNAP*" + specie.getName() + "^" + coop + "*"
				+ promoter.getName() + "-kr*" + "RNAP_" + promoter.getName() + "_"
				+ specie.getName());
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getName() + "_"
				+ specie.getName(), 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		Reaction r = new Reaction("R_RNAP_binding_" + promoter.getName() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();
		r.addReactant(new SpeciesReference(specie.getName(), coop));
		r.addReactant(new SpeciesReference("RNAP", 1));
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("act", act, GeneticNetwork.getMoleParameter(2+(int)coop)));
		kl.setFormula("act*" + "RNAP*" + specie.getName() + "^" + coop + "*"
				+ promoter.getName() + "-kr*" + "RNAP_" + promoter.getName() + "_"
				+ specie.getName());
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getName() + "_"
				+ specie.getName(), 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		Reaction r = new Reaction("R_RNAP_binding_" + promoter.getName() + "_"
				+ specie);
		gcm2sbml.network.Reaction reaction = promoter.getReactionMap().get(
				specie).get(0);
		double coop = reaction.getCoop();

		r.addReactant(new SpeciesReference("RNAP", 1));
		r.addReactant(new SpeciesReference(promoter.getName(), 1));
		r.addReactant(new SpeciesReference(specie.getName(), coop));
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("kr", 1, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.addParameter(new Parameter("act", act, GeneticNetwork.getMoleParameter(2+(int)coop)));
		kl.setFormula("act*" + "RNAP*" + specie.getName() + "^" + coop + "*"
				+ promoter.getName() + "-kr*" + "RNAP_" + promoter.getName() + "_"
				+ specie.getName());
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getName() + "_"
				+ specie.getName(), 1));
		r.setReversible(true);
		r.setFast(false);
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	private Promoter promoter = null;

	private double kdimer = .5;

	private double kbio = .05;

	private double kcoop = 1;

	private double act = .033;

	private Collection<SpeciesInterface> species = null;

}

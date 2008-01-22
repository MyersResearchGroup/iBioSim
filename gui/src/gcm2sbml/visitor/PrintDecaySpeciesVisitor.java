package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;

import java.util.ArrayList;
import java.util.Collection;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SpeciesReference;

public class PrintDecaySpeciesVisitor extends AbstractPrintVisitor {

	public PrintDecaySpeciesVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species, double decay) {
		super(document);
		this.decay = decay;
		this.species = species;
		addDecayUnit();
	}
	
	private void addDecayUnit() {
		decayString = GeneticNetwork.getMoleTimeParameter(1);
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
		//Check if they have decay rates, if not, then don't allow decay
		if (!dimerizationAbstraction && !Double.isNaN(specie.getDecayRate())) {
			Reaction r = new Reaction("Degradation_"+specie.getName());
			r.addReactant(new SpeciesReference(specie.getName(), 1));
			r.setReversible(false);
			r.setFast(false);
			KineticLaw kl = new KineticLaw();
			kl.addParameter(new Parameter("decay", specie.getDecayRate(), decayString));
			kl.setFormula("decay*" + specie.getName());
			r.setKineticLaw(kl);
			document.getModel().addReaction(r);
		}
	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		if (!biochemicalAbstraction && !Double.isNaN(specie.getDecayRate())) {
			Reaction r = new Reaction("Degradation_"+specie.getName());
			r.addReactant(new SpeciesReference(specie.getName(), 1));
			r.setReversible(false);
			r.setFast(false);
			KineticLaw kl = new KineticLaw();
			kl.addParameter(new Parameter("decay", specie.getDecayRate(), decayString));
			kl.setFormula("decay*" + specie.getName());
			r.setKineticLaw(kl);
			document.getModel().addReaction(r);
		}
	}

	public void visitBaseSpecies(BaseSpecies specie) {
		double newDecay = decay;
		if (!Double.isNaN(specie.getDecayRate())) {
			newDecay = specie.getDecayRate();
		}
		Reaction r = new Reaction("Degradation_"+specie.getName());
		r.addReactant(new SpeciesReference(specie.getName(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("decay", newDecay, decayString));
		kl.setFormula("decay*" + specie.getName());
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		//do nothing, constant species can't decay
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		double newDecay = decay;
		if (!Double.isNaN(specie.getDecayRate())) {
			newDecay = specie.getDecayRate();
		}
		Reaction r = new Reaction("Degradation_"+specie.getName());
		r.addReactant(new SpeciesReference(specie.getName(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter("decay", newDecay, decayString));
		kl.setFormula("decay*" + specie.getName());
		r.setKineticLaw(kl);
		document.getModel().addReaction(r);
	}
	
	private double decay = .0075;
	private String decayString = "";
	
	private Collection<SpeciesInterface> species = null;
	

}

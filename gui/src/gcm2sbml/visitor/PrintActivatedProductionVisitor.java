package gcm2sbml.visitor;

import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
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

public class PrintActivatedProductionVisitor extends AbstractPrintVisitor {

	public PrintActivatedProductionVisitor(SBMLDocument document, Promoter p,
			Collection<SpeciesInterface> species, double act, double stoc) {
		super(document);
		this.defaultact = act;
		this.promoter = p;
		this.species = species;
		this.defaultstoc = stoc;
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

	@Override
	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitDimer(DimerSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addModifier(Utility.ModifierSpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId()));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		Utility.addReaction(document, r);

	}

	@Override
	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addModifier(Utility.ModifierSpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId()));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addModifier(Utility.ModifierSpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId()));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addModifier(Utility.ModifierSpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId()));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = Utility.Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addModifier(Utility.ModifierSpeciesReference("RNAP_" + "_" + promoter.getId()
				+ "_" + specie.getId()));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		Utility.addReaction(document, r);
	}

	private void loadValues(Properties property) {
		stoc = getProperty(GlobalConstants.STOICHIOMETRY_STRING, property,
				defaultstoc);
		act = getProperty(GlobalConstants.ACTIVED_STRING, property, defaultact);
	}

	private Promoter promoter = null;

	private double act = .25;
	private double stoc = 1;

	private double defaultact = .25;
	private double defaultstoc = 1;

	private String actString = CompatibilityFixer
			.getSBMLName(GlobalConstants.ACTIVED_STRING);
	private String stocString = CompatibilityFixer
			.getSBMLName(GlobalConstants.STOICHIOMETRY_STRING);

	private Collection<SpeciesInterface> species = null;

}

package gcm2sbml.visitor;


import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.ComplexSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

public class PrintActivatedProductionVisitor extends AbstractPrintVisitor {

	public PrintActivatedProductionVisitor(SBMLDocument document, Promoter p) {
		super(document);
		this.promoter = p;
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
	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}
	
	public void visitComplex(ComplexSpecies specie) {
		loadValues();
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
		loadValues();
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
		loadValues();
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
		loadValues();
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

	private void loadValues() {
		stoc = promoter.getStoich();
		act = promoter.getKact();
	}

	private Promoter promoter;

	private double act;
	private double stoc;

	private String actString = CompatibilityFixer
			.getSBMLName(GlobalConstants.ACTIVED_STRING);
	

	

}


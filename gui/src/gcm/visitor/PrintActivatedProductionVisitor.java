package gcm.visitor;





import org.sbml.libsbml.SBMLDocument;

import gcm.network.BaseSpecies;
import gcm.network.ComplexSpecies;
import gcm.network.ConstantSpecies;
import gcm.network.DiffusibleConstitutiveSpecies;
import gcm.network.DiffusibleSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.Promoter;
import gcm.network.SpasticSpecies;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

public class PrintActivatedProductionVisitor extends AbstractPrintVisitor {

	public PrintActivatedProductionVisitor(SBMLDocument document, Promoter p, String compartment) {
		super(document);
		this.promoter = p;
		this.compartment = compartment;
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
			reactionName = "R_act_production_" + promoter.getId() + "_" + activator;
			specie.accept(this);
		}
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}
	
	public void visitComplex(ComplexSpecies specie) {
		loadValues();
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addModifier(Utility.ModifierSpeciesReference(speciesName));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*" + speciesName);
		Utility.addReaction(document, r);
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues();
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addModifier(Utility.ModifierSpeciesReference(speciesName));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*" + speciesName);
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues();
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addModifier(Utility.ModifierSpeciesReference(speciesName));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*" + speciesName);
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues();
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addModifier(Utility.ModifierSpeciesReference(speciesName));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*" + speciesName);
		Utility.addReaction(document, r);
	}
	
	@Override
	public void visitDiffusibleSpecies(DiffusibleSpecies species) {
		loadValues();
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addModifier(Utility.ModifierSpeciesReference(speciesName));
		for (SpeciesInterface spec : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(spec.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*" + speciesName);
		Utility.addReaction(document, r);
	}

	public void visitDiffusibleConstitutiveSpecies(DiffusibleConstitutiveSpecies specie) {
		
		loadValues();
		r = Utility.Reaction(reactionName);
		r.setCompartment(compartment);
		r.addModifier(Utility.ModifierSpeciesReference(speciesName));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
		}
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*" + speciesName);
		Utility.addReaction(document, r);
	}
	
	private void loadValues() {
		stoc = promoter.getStoich();
		act = promoter.getKact();
	}

	private Promoter promoter;

	private double act;
	private double stoc;

	private String actString = GlobalConstants.ACTIVED_STRING;
	
	private String speciesName;
	private String reactionName;
	private String compartment;

	

}


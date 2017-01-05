package backend.biomodel.visitor;

import org.sbml.jsbml.SBMLDocument;

import backend.biomodel.network.BaseSpecies;
import backend.biomodel.network.ComplexSpecies;
import backend.biomodel.network.ConstantSpecies;
import backend.biomodel.network.DiffusibleConstitutiveSpecies;
import backend.biomodel.network.DiffusibleSpecies;
import backend.biomodel.network.GeneticNetwork;
import backend.biomodel.network.Promoter;
import backend.biomodel.network.SpasticSpecies;
import backend.biomodel.network.SpeciesInterface;
import backend.biomodel.util.SBMLutilities;
import backend.biomodel.util.Utility;
import backend.util.GlobalConstants;

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

	}
	
	@Override
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
		kl.addLocalParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setMath(SBMLutilities.myParseFormula(actString+ "*" + speciesName));
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
		kl.addLocalParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setMath(SBMLutilities.myParseFormula(actString+ "*" + speciesName));
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
		kl.addLocalParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setMath(SBMLutilities.myParseFormula(actString+ "*" + speciesName));
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
		kl.addLocalParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setMath(SBMLutilities.myParseFormula(actString+ "*" + speciesName));
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
		kl.addLocalParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setMath(SBMLutilities.myParseFormula(actString+ "*" + speciesName));
		Utility.addReaction(document, r);
	}

	@Override
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
		kl.addLocalParameter(Utility.Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setMath(SBMLutilities.myParseFormula(actString+ "*" + speciesName));
		Utility.addReaction(document, r);
	}
	
	private void loadValues() {
		stoc = promoter.getStoich();
		act = promoter.getKact();
	}

	private Promoter promoter;

	private double act;
	private double stoc;

	private String actString = GlobalConstants.ACTIVATED_STRING;
	
	private String speciesName;
	private String reactionName;
	private String compartment;

	

}


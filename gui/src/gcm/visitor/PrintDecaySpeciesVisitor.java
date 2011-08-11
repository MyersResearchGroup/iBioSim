package gcm.visitor;

import gcm.network.BaseSpecies;
import gcm.network.ComplexSpecies;
import gcm.network.ConstantSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.Influence;
import gcm.network.SpasticSpecies;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.sbml.libsbml.SBMLDocument;

public class PrintDecaySpeciesVisitor extends AbstractPrintVisitor {

	public PrintDecaySpeciesVisitor(SBMLDocument document,
			HashMap<String, SpeciesInterface> species, HashMap<String, Properties> compartments, 
			HashMap<String, ArrayList<Influence>> complexMap, HashMap<String, ArrayList<Influence>> partsMap) {
		super(document);
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.compartments = compartments;
		addDecayUnit();
	}
	
	private void addDecayUnit() {
		decayUnitString = GeneticNetwork.getMoleTimeParameter(1);
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {		
		for (SpeciesInterface s : species.values()) {
			s.accept(this);
		}
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void visitComplex(ComplexSpecies specie) {
		loadValues(specie);
		if (!complexAbstraction || (!specie.isAbstractable() && !specie.isSequesterAbstractable())) {	
			String compartment = checkCompartments(specie.getId());
			r = Utility.Reaction("Degradation_"+specie.getId());
			r.setCompartment(compartment);
			r.setReversible(false);
			r.setFast(false);
			kl = r.createKineticLaw();
			String decayExpression = "";
			if (complexAbstraction && specie.isSequesterable()) {
				decayExpression = abstractDecay(specie.getId());
				if (decayExpression.length() > 0) {
					kl.setFormula(decayExpression);
					Utility.addReaction(document, r);
				}
			} else if (decay > 0){
				decayExpression = decayString + "*" + specie.getId();
				kl.addParameter(Utility.Parameter(decayString, decay, decayUnitString));
				r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
				kl.setFormula(decayExpression);
				Utility.addReaction(document, r);
			}
		}
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		r = Utility.Reaction("Degradation_"+specie.getId());
		r.setCompartment(compartment);
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		String decayExpression = "";
		if (complexAbstraction && specie.isSequesterable()) {
			decayExpression = abstractDecay(specie.getId());
			if (decayExpression.length() > 0) {
				kl.setFormula(decayExpression);
				Utility.addReaction(document, r);
			}
		} else if (decay > 0){
			decayExpression = decayString + "*" + specie.getId();
			kl.addParameter(Utility.Parameter(decayString, decay, decayUnitString));
			r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
			kl.setFormula(decayExpression);
			Utility.addReaction(document, r);
		}
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		//do nothing, constant species can't decay
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		r = Utility.Reaction("Degradation_"+specie.getId());
		r.setCompartment(compartment);
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		String decayExpression = "";
		if (complexAbstraction && specie.isSequesterable()) {
			decayExpression = abstractDecay(specie.getId());
			if (decayExpression.length() > 0) {
				kl.setFormula(decayExpression);
				Utility.addReaction(document, r);
			}
		} else if (decay > 0){
			decayExpression = decayString + "*" + specie.getId();
			kl.addParameter(Utility.Parameter(decayString, decay, decayUnitString));
			r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
			kl.setFormula(decayExpression);
			Utility.addReaction(document, r);
		}
	}
	
	private void loadValues(SpeciesInterface specie) {
		decay = specie.getDecay();
	}
	
	//Checks if species belongs in a compartment other than default
	private String checkCompartments(String species) {
		String compartment = document.getModel().getCompartment(0).getId();
		//String[] splitted = species.split("__");
		String component = species;
		while (component.contains("__")) {
			component = component.substring(0,component.lastIndexOf("__"));
			for (String compartmentName : compartments.keySet()) {
				if (compartmentName.substring(0,compartmentName.lastIndexOf("__")).equals(component)) {
					return compartmentName;
				}
			}
		}
		/*
		if (compartments.contains(splitted[0]))
			compartment = splitted[0];
			*/
		return compartment;
	}
	
	private double decay;
	private String decayUnitString;
	private String decayString = GlobalConstants.KDECAY_STRING;
	private String kcompString = GlobalConstants.KCOMPLEX_STRING; 
	
	private HashMap<String, Properties> compartments;
}

package gcm.visitor;

import gcm.network.GeneticNetwork;
import gcm.network.PartSpecies;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;

import org.sbml.libsbml.KineticLaw;

public class AbstractionEngine {
	
	public AbstractionEngine(HashMap<String, SpeciesInterface> species, HashMap<String, ArrayList<PartSpecies>> complexMap, 
			HashMap<String, ArrayList<PartSpecies>> partsMap) {
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.sbmlMode = false;
	}
	
	public AbstractionEngine(HashMap<String, SpeciesInterface> species, HashMap<String, ArrayList<PartSpecies>> complexMap, 
			HashMap<String, ArrayList<PartSpecies>> partsMap, KineticLaw kl,  
			HashMap<String, Double> complexReactants, ArrayList<String> complexModifiers) {
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.kl = kl;
		this.complexReactants = complexReactants;
		this.complexModifiers = complexModifiers;
		this.sbmlMode = true;
	}
	
	public String abstractComplex(String complexId, double multiplier, String payNoMind) {
		String repMolecule = "";
		String kcompId = kcompString + "__" + complexId;
		double[] kcomp = species.get(complexId).getKc();
		//Checks if binding parameters are specified as forward and reverse rate constants or 
		//as equilibrium binding constants before adding to kinetic law
		if (sbmlMode && kcomp.length == 2) {
			kl.addParameter(Utility.Parameter(kcompId, kcomp[0]/kcomp[1],
					GeneticNetwork.getMoleParameter(2)));
		} else if (sbmlMode) {
			kl.addParameter(Utility.Parameter(kcompId, kcomp[0],
					GeneticNetwork.getMoleParameter(2)));
		}
		String ncSum = "";
		for (PartSpecies part : complexMap.get(complexId)) {
			String speciesId = part.getSpeciesId();
			double n = part.getStoich();
			String nId = coopString + "__" + speciesId + "_" + complexId;
			if (sbmlMode)
				kl.addParameter(Utility.Parameter(nId, n, "dimensionless"));
			ncSum = ncSum + nId + "+";
			if (!speciesId.equals(payNoMind)) {
				repMolecule = repMolecule + "*" + "(";
				if (species.get(speciesId).isAbstractable()) {
					repMolecule = repMolecule + abstractComplex(speciesId, multiplier * n, "");
				} else {
					repMolecule = repMolecule + speciesId;
					if (payNoMind.equals("")) {
						if (species.get(speciesId).isSequesterable())
							repMolecule = repMolecule + sequesterSpecies(speciesId, complexId);
						if (sbmlMode && complexReactants.containsKey(speciesId))
							complexReactants.put(speciesId, complexReactants.get(speciesId) + multiplier * n);
						else if (sbmlMode) 
							complexReactants.put(speciesId, multiplier * n);
					} else if (sbmlMode) {
						complexModifiers.add(speciesId);
					}
				}
				repMolecule = repMolecule + ")^" + nId;
			}
		}
		repMolecule = kcompId + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-1)" + repMolecule;	
		return repMolecule;
	}
	
	public String sequesterSpecies(String speciesId, String payNoMind) {
		String sequesterFactor = "/(1";
		for (PartSpecies part : partsMap.get(speciesId)) {
			String complexId = part.getComplexId();
			if (!complexId.equals(payNoMind))
				sequesterFactor = sequesterFactor + "+" + abstractComplex(complexId, 1, speciesId);
		}
		sequesterFactor = sequesterFactor + ")";
		return sequesterFactor;
	}
	
	private KineticLaw kl;
	private HashMap<String, SpeciesInterface> species;
	private HashMap<String, Double> complexReactants;
	private ArrayList<String> complexModifiers;
	private HashMap<String, ArrayList<PartSpecies>> complexMap;
	private HashMap<String, ArrayList<PartSpecies>> partsMap;
	private boolean sbmlMode;
	
	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
}

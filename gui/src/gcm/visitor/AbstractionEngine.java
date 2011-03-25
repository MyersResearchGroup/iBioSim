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
			HashMap<String, Double> complexReactantStoich, ArrayList<String> complexModifierStoich) {
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.kl = kl;
		this.complexReactantStoich = complexReactantStoich;
		this.complexModifierStoich = complexModifierStoich;
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
			String partId = part.getPartId();
			double n = part.getStoich();
			String nId = coopString + "__" + partId + "_" + complexId;
			if (sbmlMode)
				kl.addParameter(Utility.Parameter(nId, n, "dimensionless"));
			ncSum = ncSum + nId + "+";
			if (!partId.equals(payNoMind)) {
				repMolecule = repMolecule + "*" + "(";
				if (species.get(partId).isAbstractable()) {
					repMolecule = repMolecule + abstractComplex(partId, multiplier * n, "");
				} else {
					repMolecule = repMolecule + partId;
					if (payNoMind.equals("")) {
						if (species.get(partId).isSequesterable())
							repMolecule = repMolecule + sequesterSpecies(partId, complexId);
						if (sbmlMode && complexReactantStoich.containsKey(partId))
							complexReactantStoich.put(partId, complexReactantStoich.get(partId) + multiplier * n);
						else if (sbmlMode) 
							complexReactantStoich.put(partId, multiplier * n);
					} else if (sbmlMode) {
						complexModifierStoich.add(partId);
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
	private HashMap<String, Double> complexReactantStoich;
	private ArrayList<String> complexModifierStoich;
	private HashMap<String, ArrayList<PartSpecies>> complexMap;
	private HashMap<String, ArrayList<PartSpecies>> partsMap;
	private boolean sbmlMode;
	
	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
}

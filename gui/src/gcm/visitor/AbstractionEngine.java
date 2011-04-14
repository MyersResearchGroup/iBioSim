package gcm.visitor;

import gcm.network.GeneticNetwork;
import gcm.network.Influence;
import gcm.network.Promoter;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;

public class AbstractionEngine {

	public AbstractionEngine(HashMap<String, SpeciesInterface> species, HashMap<String, Promoter> promoters,
			HashMap<String, ArrayList<Influence>> complexMap, HashMap<String, ArrayList<Influence>> partsMap, 
			double RNAP) {
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.promoters = promoters;
		this.RNAP = RNAP;
		this.sbmlMode = false;
	}

	public AbstractionEngine(HashMap<String, SpeciesInterface> species, HashMap<String, Promoter> promoters, 
			HashMap<String, ArrayList<Influence>> complexMap, HashMap<String, ArrayList<Influence>> partsMap, 
			Reaction r, KineticLaw kl) {
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.r = r;
		this.kl = kl;
		this.promoters = promoters;
		this.sbmlMode = true;
	}

	public String abstractComplex(String complexId, double multiplier) {
		complexReactantStoich = new HashMap<String, Double>();
		complexModifiers = new ArrayList<String>();
		String expression = abstractComplexHelper(complexId, multiplier, "");
		for (String reactant : complexReactantStoich.keySet())
			r.addReactant(Utility.SpeciesReference(reactant, complexReactantStoich.get(reactant)));
		for (String modifier : complexModifiers)
			r.addModifier(Utility.ModifierSpeciesReference(modifier));
		return expression;
	}
	
	private String abstractComplexHelper(String complexId, double multiplier, String payNoMind) {
		String repMolecule = "";
		String kcompId = kcompString + "__" + complexId;
		double[] kcomp = species.get(complexId).getKc();
		// Checks if binding parameters are specified as forward and reverse
		// rate constants or
		// as equilibrium binding constants before adding to kinetic law
		if (sbmlMode && kcomp.length == 2) {
			kl.addParameter(Utility.Parameter(kcompId, kcomp[0] / kcomp[1], GeneticNetwork
					.getMoleParameter(2)));
		}
		else if (sbmlMode) {
			kl.addParameter(Utility
					.Parameter(kcompId, kcomp[0], GeneticNetwork.getMoleParameter(2)));
		}
		String ncSum = "";
		for (Influence infl : complexMap.get(complexId)) {
			String partId = infl.getInput();
			double n = infl.getCoop();
			String nId = coopString + "__" + partId + "_" + complexId;
			if (sbmlMode)
				kl.addParameter(Utility.Parameter(nId, n, "dimensionless"));
			ncSum = ncSum + nId + "+";
			if (!partId.equals(payNoMind)) {
				repMolecule = repMolecule + "*" + "(";
				if (species.get(partId).isAbstractable()) {
					repMolecule = repMolecule + abstractComplexHelper(partId, multiplier * n, "");
				}
				else if (payNoMind.equals("")) {
					if (species.get(partId).isSequesterable())
						repMolecule = repMolecule + sequesterSpeciesHelper(partId, complexId);
					else
						repMolecule = repMolecule + partId;
					if (sbmlMode && complexReactantStoich.containsKey(partId))
						complexReactantStoich.put(partId, complexReactantStoich.get(partId)
								+ multiplier * n);
					else if (sbmlMode)
						complexReactantStoich.put(partId, multiplier * n);
				}
				else {
					repMolecule = repMolecule + partId;
					if (sbmlMode)
						complexModifiers.add(partId);
				}
				repMolecule = repMolecule + ")^" + nId;
			}
		}
		repMolecule = kcompId + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-1)"
				+ repMolecule;
		return repMolecule;
	}

	public String sequesterSpecies(String speciesId) {
		complexModifiers = new ArrayList<String>();
		String expression = sequesterSpeciesHelper(speciesId, "");
		for (String modifier : complexModifiers)
			r.addModifier(Utility.ModifierSpeciesReference(modifier));
		return expression;
	}
	
	private String sequesterSpeciesHelper(String speciesId, String payNoMind) {
		String sequesterFactor = speciesId + "/(1";
		for (Influence infl : partsMap.get(speciesId)) {
			String complexId = infl.getOutput();
			if (!complexId.equals(payNoMind) && species.get(complexId).isSequesterAbstractable())
				sequesterFactor = sequesterFactor + "+" + abstractComplexHelper(complexId, 1, speciesId);
		}
		sequesterFactor = sequesterFactor + ")";
		return sequesterFactor;
	}

	public String abstractOperatorSite(String promoterId) {
		String promRate = "";
		if (promoters.containsKey(promoterId)) {
			if (promoters.get(promoterId).getActivators().size() != 0) {
				double np = promoters.get(promoterId).getStoich();
				double ng = promoters.get(promoterId).getPcount();
				double kb = promoters.get(promoterId).getKbasal();
				double[] KoArray = promoters.get(promoterId).getKrnap();
				double Ko = KoArray[0] / KoArray[1];
				if (sbmlMode) {
					promRate += "(np__" + promoterId + "*ng__" + promoterId + ")*((kb__"
							+ promoterId + "*Ko__" + promoterId + "*RNAP)";
					kl.addParameter(Utility.Parameter("np__" + promoterId, np, GeneticNetwork
							.getMoleParameter(1)));
					kl.addParameter(Utility.Parameter("ng__" + promoterId, ng, GeneticNetwork
							.getMoleParameter(1)));
					kl.addParameter(Utility.Parameter("kb__" + promoterId, kb, GeneticNetwork
							.getMoleTimeParameter(1)));
					kl.addParameter(Utility.Parameter("Ko__" + promoterId, Ko, GeneticNetwork
							.getMoleParameter(2)));
				}
				else {
					promRate += "(" + np + "*" + ng + ")*((" + kb + "*" + Ko + "*" + RNAP + ")";
				}
				String actBottom = "";
				for (SpeciesInterface act : promoters.get(promoterId).getActivators()) {
					String activator = act.getId();
					String expression = activator;
					if (species.get(activator).isSequesterable()) {
						expression = sequesterSpecies(activator);
					}
					else if (complexMap.containsKey(activator)) {
						expression = abstractComplex(activator, 0);
					}
					for (Influence influ : promoters.get(promoterId).getActivatingInfluences()) {
						if (influ.getInput().equals(activator)) {
							double nc = influ.getCoop();
							double[] KaArray = influ.getAct();
							double Ka = KaArray[0] / KaArray[1];
							double ka = promoters.get(promoterId).getKact();
							if (sbmlMode) {
								promRate += "+(ka__" + activator + "_" + promoterId + "*((Ka__"
										+ activator + "_" + promoterId + "*RNAP*" + expression
										+ ")^nc__" + activator + "_" + promoterId + "))";
								actBottom += "+((Ka__" + activator + "_" + promoterId + "*RNAP*"
										+ expression + ")^nc__" + activator + "_" + promoterId
										+ ")";
								kl.addParameter(Utility.Parameter("nc__" + activator + "_"
										+ promoterId, nc, "dimensionless"));
								kl.addParameter(Utility.Parameter("Ka__" + activator + "_"
										+ promoterId, Ka, GeneticNetwork.getMoleParameter(2)));
								kl.addParameter(Utility.Parameter("ka__" + activator + "_"
										+ promoterId, ka, GeneticNetwork.getMoleTimeParameter(1)));
							}
							else {
								promRate += "+(" + ka + "*((" + Ka + "*" + RNAP + "*" + expression
										+ ")^" + nc + "))";
								actBottom += "+((" + Ka + "*" + RNAP + "*" + expression + ")^" + nc
										+ ")";
							}
						}
					}
					if (sbmlMode) {
						promRate += ")/((1+(Ko__" + promoterId + "*RNAP))" + actBottom;
					}
					else {
						promRate += ")/((1+(" + Ko + "*" + RNAP + "))" + actBottom;
					}
					if (promoters.get(promoterId).getRepressors().size() != 0) {
						for (SpeciesInterface rep : promoters.get(promoterId).getRepressors()) {
							String repressor = rep.getId();
							String expression2 = repressor;
							if (species.get(repressor).isSequesterable()) {
								expression2 = sequesterSpecies(repressor);
							}
							else if (complexMap.containsKey(repressor)) {
								expression2 = abstractComplex(repressor, 0);
							}
							for (Influence influ : promoters.get(promoterId)
									.getRepressingInfluences()) {
								if (influ.getInput().equals(repressor)) {
									double nc = influ.getCoop();
									double[] KrArray = influ.getRep();
									double Kr = KrArray[0] / KrArray[1];
									if (sbmlMode) {
										promRate += "+((Kr__" + repressor + "_" + promoterId + "*"
												+ expression2 + ")^nc__" + repressor + "_"
												+ promoterId + ")";
										kl.addParameter(Utility.Parameter("nc__" + repressor + "_"
												+ promoterId, nc, "dimensionless"));
										kl.addParameter(Utility.Parameter("Kr__" + repressor + "_"
												+ promoterId, Kr, GeneticNetwork
												.getMoleParameter(2)));
									}
									else {
										promRate += "+((" + Kr + "*" + expression2 + ")^" + nc
												+ ")";
									}
								}
							}
						}
					}
					promRate += ")";
				}
			}
			else {
				if (promoters.get(promoterId).getRepressors().size() != 0) {
					double np = promoters.get(promoterId).getStoich();
					double ng = promoters.get(promoterId).getPcount();
					double ko = promoters.get(promoterId).getKoc();
					double[] KoArray = promoters.get(promoterId).getKrnap();
					double Ko = KoArray[0] / KoArray[1];
					if (sbmlMode) {
						promRate += "(np__" + promoterId + "*ko__" + promoterId + "*ng__"
								+ promoterId + ")*((Ko__" + promoterId + "*RNAP))/((1+(Ko__"
								+ promoterId + "*RNAP))";
						kl.addParameter(Utility.Parameter("np__" + promoterId, np, GeneticNetwork
								.getMoleParameter(1)));
						kl.addParameter(Utility.Parameter("ng__" + promoterId, ng, GeneticNetwork
								.getMoleParameter(1)));
						kl.addParameter(Utility.Parameter("Ko__" + promoterId, Ko, GeneticNetwork
								.getMoleParameter(2)));
						kl.addParameter(Utility.Parameter("ko__" + promoterId, ko, GeneticNetwork
								.getMoleTimeParameter(1)));
					}
					else {
						promRate += "(" + np + "*" + ko + "*" + ng + ")*((" + Ko + "*" + RNAP
								+ "))/((1+(" + Ko + "*" + RNAP + "))";
					}
					for (SpeciesInterface rep : promoters.get(promoterId).getRepressors()) {
						String repressor = rep.getId();
						String expression = repressor;
						if (species.get(repressor).isSequesterable()) {
							expression = sequesterSpecies(repressor);
						}
						else if (complexMap.containsKey(repressor)) {
							expression = abstractComplex(repressor, 0);
						}
						for (Influence influ : promoters.get(promoterId).getRepressingInfluences()) {
							if (influ.getInput().equals(repressor)) {
								double nc = influ.getCoop();
								double[] KrArray = influ.getRep();
								double Kr = KrArray[0] / KrArray[1];
								if (sbmlMode) {
									promRate += "+((Kr__" + repressor + "_" + promoterId + "*"
											+ expression + ")^nc__" + repressor + "_" + promoterId
											+ ")";
									kl.addParameter(Utility.Parameter("nc__" + repressor + "_"
											+ promoterId, nc, "dimensionless"));
									kl.addParameter(Utility.Parameter("Kr__" + repressor + "_"
											+ promoterId, Kr, GeneticNetwork.getMoleParameter(2)));
								}
								else {
									promRate += "+((" + Kr + "*" + expression + ")^" + nc + ")";
								}
							}
						}
					}
					promRate += ")";
				}
			}
		}
		return promRate;
	}

	private Reaction r;
	private KineticLaw kl;
	private HashMap<String, SpeciesInterface> species;
	private HashMap<String, Double> complexReactantStoich;
	private ArrayList<String> complexModifiers;
	private HashMap<String, ArrayList<Influence>> complexMap;
	private HashMap<String, ArrayList<Influence>> partsMap;
	private HashMap<String, Promoter> promoters;
	private double RNAP;
	private boolean sbmlMode;

	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
}

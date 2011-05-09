package gcm.network;

import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;

public class AbstractionEngine {

	public AbstractionEngine(HashMap<String, SpeciesInterface> species,
			HashMap<String, ArrayList<Influence>> complexMap,
			HashMap<String, ArrayList<Influence>> partsMap, double RNAP) {
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.RNAP = RNAP;
		this.sbmlMode = false;
	}

	public AbstractionEngine(HashMap<String, SpeciesInterface> species,
			HashMap<String, ArrayList<Influence>> complexMap,
			HashMap<String, ArrayList<Influence>> partsMap, double RNAP, Reaction r, KineticLaw kl) {
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.RNAP = RNAP;
		this.r = r;
		this.kl = kl;
		this.sbmlMode = true;
	}

	public String abstractComplex(String complexId, double multiplier, boolean operatorAbstraction) {
		if (sbmlMode) {
			complexReactantStoich = new HashMap<String, Double>();
			complexModifiers = new ArrayList<String>();
		}
		String expression = abstractComplexHelper(complexId, multiplier, "", operatorAbstraction);
		if (sbmlMode) {
			for (String reactant : complexReactantStoich.keySet())
				if (operatorAbstraction)
					r.addModifier(Utility.ModifierSpeciesReference(reactant));
				else
					r.addReactant(Utility.SpeciesReference(reactant, complexReactantStoich.get(reactant)));
			for (String modifier : complexModifiers)
				r.addModifier(Utility.ModifierSpeciesReference(modifier));
		}
		return expression;
	}
	
	private String abstractComplexHelper(String complexId, double multiplier, String payNoMind, boolean operatorAbstraction) {
		String compExpression = "";
		if (sbmlMode) {
			String kcompId = kcompString + "__" + complexId;
			double[] kcomp = species.get(complexId).getKc();
			// Checks if binding parameters are specified as forward and reverse
			// rate constants or
			// as equilibrium binding constants before adding to kinetic law
			if (kcomp.length == 2) {
				kl.addParameter(Utility.Parameter(kcompId, kcomp[0] / kcomp[1], GeneticNetwork
						.getMoleParameter(2)));
			} else {
				kl.addParameter(Utility
						.Parameter(kcompId, kcomp[0], GeneticNetwork.getMoleParameter(2)));
			}
			String ncSum = "";
			for (Influence infl : complexMap.get(complexId)) {
				String partId = infl.getInput();
				double n = infl.getCoop();
				String nId = coopString + "__" + partId + "_" + complexId;
				kl.addParameter(Utility.Parameter(nId, n, "dimensionless"));
				ncSum = ncSum + nId + "+";
				if (!partId.equals(payNoMind)) {
					compExpression = compExpression + "*" + "(";
					if (species.get(partId).isAbstractable()) {
						compExpression = compExpression + abstractComplexHelper(partId, multiplier * n, "", operatorAbstraction);
					} else if (payNoMind.equals("")) {
						if (species.get(partId).isSequesterable())
							compExpression = compExpression + sequesterSpeciesHelper(partId, complexId, operatorAbstraction);
						else
							compExpression = compExpression + partId;
						if (complexReactantStoich.containsKey(partId))
							complexReactantStoich.put(partId, complexReactantStoich.get(partId)
									+ multiplier * n);
						else
							complexReactantStoich.put(partId, multiplier * n);
					} else {
						compExpression = compExpression + partId;
						complexModifiers.add(partId);
					}
					compExpression = compExpression + ")^" + nId;
				}
			}
			compExpression = kcompId + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-1)"
			+ compExpression;
		} else {
			double ncSum = 0;
			for (Influence infl : complexMap.get(complexId)) {
				String partId = infl.getInput();
				double n = infl.getCoop();
				ncSum = ncSum + n;
				if (!partId.equals(payNoMind)) {
					compExpression = compExpression + "*" + "(";
					if (species.get(partId).isAbstractable()) {
						compExpression = compExpression + abstractComplexHelper(partId, multiplier * n, "", operatorAbstraction);
					} else if (payNoMind.equals("")) {
						if (species.get(partId).isSequesterable())
							compExpression = compExpression + sequesterSpeciesHelper(partId, complexId, operatorAbstraction);
						else
							compExpression = compExpression + partId;
					} else {
						compExpression = compExpression + partId;
					}
					compExpression = compExpression + ")^" + n;
				}
			}
			double[] kcomp = species.get(complexId).getKc();
			if (kcomp.length == 2)
				compExpression = kcomp[0]/kcomp[1] + "^" + (ncSum - 1) + compExpression;
			else 
				compExpression = kcomp[0] + "^" + (ncSum - 1) + compExpression;
		}
		return compExpression;
	}

	public String sequesterSpecies(String speciesId, double n, boolean operatorAbstraction) {
		if (sbmlMode)
			complexModifiers = new ArrayList<String>();
		String expression = sequesterSpeciesHelper(speciesId, "", operatorAbstraction);
		if (sbmlMode) {
			if (operatorAbstraction)
				r.addModifier(Utility.ModifierSpeciesReference(speciesId));
			else if (n > 0)  // necessary to ignore the case of a reverse formation reaction for a complex that's being sequestered
				r.addReactant(Utility.SpeciesReference(speciesId, n));
			for (String modifier : complexModifiers)
				r.addModifier(Utility.ModifierSpeciesReference(modifier));
		}
		return expression;
	}
	
	private String sequesterSpeciesHelper(String speciesId, String payNoMind, boolean operatorAbstraction) {
		String sequesterFactor = speciesId + "/(1";
		for (Influence infl : partsMap.get(speciesId)) {
			String complexId = infl.getOutput();
			if (!complexId.equals(payNoMind) && species.get(complexId).isSequesterAbstractable())
				sequesterFactor = sequesterFactor + "+" + abstractComplexHelper(complexId, 1, speciesId, operatorAbstraction);
		}
		sequesterFactor = sequesterFactor + ")";
		return sequesterFactor;
	}

	public String abstractOperatorSite(Promoter promoter) {
		if (sbmlMode)
			kl.addParameter(Utility.Parameter("RNAP", RNAP, GeneticNetwork.getMoleParameter(2)));
		String promRate = "";
		if (promoter.getActivators().size() != 0) {
			double np = promoter.getStoich();
			double ng = promoter.getPcount();
			double kb = promoter.getKbasal();
			double[] KoArray = promoter.getKrnap();
			double Ko;
			if (KoArray.length == 2) {
				Ko = KoArray[0] / KoArray[1];
			}
			else {
				Ko = KoArray[0];
			}
			if (sbmlMode) {
				promRate += "(ng__" + promoter.getId() + ")*((kb__" + promoter.getId() + "*Ko__"
						+ promoter.getId() + "*RNAP)";
				// kl.addParameter(Utility.Parameter("np__" + promoter.getId(),
				// np, GeneticNetwork
				// .getMoleParameter(1)));
				kl.addParameter(Utility.Parameter("ng__" + promoter.getId(), ng, GeneticNetwork
						.getMoleParameter(1)));
				kl.addParameter(Utility.Parameter("kb__" + promoter.getId(), kb, GeneticNetwork
						.getMoleTimeParameter(1)));
				kl.addParameter(Utility.Parameter("Ko__" + promoter.getId(), Ko, GeneticNetwork
						.getMoleParameter(2)));
			}
			else {
				promRate += "(" + np + "*" + ng + ")*((" + kb + "*" + Ko + "*" + RNAP + ")";
			}
			String actBottom = "";
			for (SpeciesInterface act : promoter.getActivators()) {
				String activator = act.getId();
				for (Influence influ : promoter.getActivatingInfluences()) {
					if (influ.getInput().equals(activator)) {
						double nc = influ.getCoop();
						double[] KaArray = influ.getAct();
						double Ka;
						if (KaArray.length == 2) {
							Ka = KaArray[0] / KaArray[1];
						}
						else {
							Ka = KaArray[0];
						}
						double ka = promoter.getKact();
						String expression = activator;
						if (species.get(activator).isSequesterable()) {
							expression = sequesterSpecies(activator, nc, true);
						}
						else if (species.get(activator).isAbstractable()) {
							expression = abstractComplex(activator, nc, true);
						} 
						else if (sbmlMode)
							r.addModifier(Utility.ModifierSpeciesReference(activator));
						if (sbmlMode) {
							promRate += "+(ka__" + activator + "_" + promoter.getId() + "*((Ka__"
									+ activator + "_" + promoter.getId() + "*RNAP*" + expression
									+ ")^nc__" + activator + "_" + promoter.getId() + "))";
							actBottom += "+((Ka__" + activator + "_" + promoter.getId() + "*RNAP*"
									+ expression + ")^nc__" + activator + "_" + promoter.getId()
									+ ")";
							kl.addParameter(Utility.Parameter("nc__" + activator + "_"
									+ promoter.getId(), nc, "dimensionless"));
							kl.addParameter(Utility.Parameter("Ka__" + activator + "_"
									+ promoter.getId(), Ka, GeneticNetwork.getMoleParameter(2)));
							kl
									.addParameter(Utility.Parameter("ka__" + activator + "_"
											+ promoter.getId(), ka, GeneticNetwork
											.getMoleTimeParameter(1)));
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
					promRate += ")/((1+(Ko__" + promoter.getId() + "*RNAP))" + actBottom;
				}
				else {
					promRate += ")/((1+(" + Ko + "*" + RNAP + "))" + actBottom;
				}
				if (promoter.getRepressors().size() != 0) {
					for (SpeciesInterface rep : promoter.getRepressors()) {
						String repressor = rep.getId();
						for (Influence influ : promoter.getRepressingInfluences()) {
							if (influ.getInput().equals(repressor)) {
								double nc = influ.getCoop();
								double[] KrArray = influ.getRep();
								double Kr;
								if (KrArray.length == 2) {
									Kr = KrArray[0] / KrArray[1];
								}
								else {
									Kr = KrArray[0];
								}
								String expression = repressor;
								if (species.get(repressor).isSequesterable()) {
									expression = sequesterSpecies(repressor, nc, true);
								}
								else if (species.get(repressor).isAbstractable()) {
									expression = abstractComplex(repressor, nc, true);
								} 
								else if (sbmlMode)
									r.addModifier(Utility.ModifierSpeciesReference(repressor));
								if (sbmlMode) {
									promRate += "+((Kr__" + repressor + "_" + promoter.getId()
											+ "*" + expression + ")^nc__" + repressor + "_"
											+ promoter.getId() + ")";
									kl.addParameter(Utility.Parameter("nc__" + repressor + "_"
											+ promoter.getId(), nc, "dimensionless"));
									kl.addParameter(Utility.Parameter("Kr__" + repressor + "_"
											+ promoter.getId(), Kr, GeneticNetwork
											.getMoleParameter(2)));
								}
								else {
									promRate += "+((" + Kr + "*" + expression + ")^" + nc + ")";
								}
							}
						}
					}
				}
				promRate += ")";
			}
		}
		else {
			if (promoter.getRepressors().size() != 0) {
				double np = promoter.getStoich();
				double ng = promoter.getPcount();
				double ko = promoter.getKoc();
				double[] KoArray = promoter.getKrnap();
				double Ko;
				if (KoArray.length == 2) {
					Ko = KoArray[0] / KoArray[1];
				}
				else {
					Ko = KoArray[0];
				}
				if (sbmlMode) {
					promRate += "(ko__" + promoter.getId() + "*ng__" + promoter.getId()
							+ ")*((Ko__" + promoter.getId() + "*RNAP))/((1+(Ko__"
							+ promoter.getId() + "*RNAP))";
					// kl.addParameter(Utility.Parameter("np__" +
					// promoter.getId(), np, GeneticNetwork
					// .getMoleParameter(1)));
					kl.addParameter(Utility.Parameter("ng__" + promoter.getId(), ng, GeneticNetwork
							.getMoleParameter(1)));
					kl.addParameter(Utility.Parameter("Ko__" + promoter.getId(), Ko, GeneticNetwork
							.getMoleParameter(2)));
					kl.addParameter(Utility.Parameter("ko__" + promoter.getId(), ko, GeneticNetwork
							.getMoleTimeParameter(1)));
				}
				else {
					promRate += "(" + np + "*" + ko + "*" + ng + ")*((" + Ko + "*" + RNAP
							+ "))/((1+(" + Ko + "*" + RNAP + "))";
				}
				for (SpeciesInterface rep : promoter.getRepressors()) {
					String repressor = rep.getId();
					for (Influence influ : promoter.getRepressingInfluences()) {
						if (influ.getInput().equals(repressor)) {
							double nc = influ.getCoop();
							double[] KrArray = influ.getRep();
							double Kr;
							if (KrArray.length == 2) {
								Kr = KrArray[0] / KrArray[1];
							}
							else {
								Kr = KrArray[0];
							}
							String expression = repressor;
							if (species.get(repressor).isSequesterable()) {
								expression = sequesterSpecies(repressor, nc, true);
							}
							else if (species.get(repressor).isAbstractable()) {
								expression = abstractComplex(repressor, nc, true);
							}
							else if (sbmlMode)
								r.addModifier(Utility.ModifierSpeciesReference(repressor));
							if (sbmlMode) {
								promRate += "+((Kr__" + repressor + "_" + promoter.getId() + "*"
										+ expression + ")^nc__" + repressor + "_"
										+ promoter.getId() + ")";
								kl.addParameter(Utility.Parameter("nc__" + repressor + "_"
										+ promoter.getId(), nc, "dimensionless"));
								kl
										.addParameter(Utility.Parameter("Kr__" + repressor + "_"
												+ promoter.getId(), Kr, GeneticNetwork
												.getMoleParameter(2)));
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
		return promRate;
	}

	private Reaction r;
	private KineticLaw kl;
	private HashMap<String, SpeciesInterface> species;
	private HashMap<String, Double> complexReactantStoich;
	private ArrayList<String> complexModifiers;
	private HashMap<String, ArrayList<Influence>> complexMap;
	private HashMap<String, ArrayList<Influence>> partsMap;
	private double RNAP;
	private boolean sbmlMode;

	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
}

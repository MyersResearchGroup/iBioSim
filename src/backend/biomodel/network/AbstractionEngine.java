package backend.biomodel.network;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;

import backend.biomodel.util.SBMLutilities;
import backend.biomodel.util.Utility;
import backend.util.GlobalConstants;

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

	// Creates abstract expression for formation of given complex and adds associated SBML constructs if in sbmlMode
	public String abstractComplex(String complexId, double multiplier, boolean operatorAbstraction) {
		if (sbmlMode) {
//			reactantStoich = new HashMap<String, Double>();
			modifiers = new HashSet<String>();
		}
		String expression = abstractComplexHelper(complexId, multiplier, "", operatorAbstraction);
		if (sbmlMode) {
//			for (String reactant : reactantStoich.keySet()) {
//				if (operatorAbstraction) {
//					r.removeModifier(reactant);
//					r.addModifier(Utility.ModifierSpeciesReference(reactant));
//				} else
//					r.addReactant(Utility.SpeciesReference(reactant, reactantStoich.get(reactant)));
//			}
			for (String modifier : modifiers) {
				SBMLutilities.removeModifier(r, modifier);
				r.addModifier(Utility.ModifierSpeciesReference(modifier));
			}
		}
		return expression;
	}
	
	// Recursively walks complexMap to build up abstract expression for complex formation
	private String abstractComplexHelper(String complexId, double multiplier, String sequesterRoot, boolean operatorAbstraction) {
		String compExpression = "";
		if (sbmlMode) {
			String kcompId = kcompString + "__" + complexId;
			String kcompIdf = "kf_c__" + complexId;
			String kcompIdr = "kr_c__" + complexId;
			double[] kcomp = species.get(complexId).getKc();
			String kdecayId = decayString + "__" + complexId;
			double kdecay = species.get(complexId).getDecay();
			if (kdecay > 0) {
				if (kl.getLocalParameter(kcompIdf)==null) {
					kl.addLocalParameter(Utility.Parameter(kcompIdf, kcomp[0], GeneticNetwork.getMoleTimeParameter(2)));
				}
				if (kl.getLocalParameter(kdecayId)==null) {
					kl.addLocalParameter(Utility.Parameter(kdecayId, kdecay, GeneticNetwork.getMoleTimeParameter(1)));
				}
			}
			// Checks if binding parameters are specified as forward and reverse rate constants
			//  or as equilibrium binding constants before adding to kinetic law
			if (kcomp.length == 2) {
				if (kdecay > 0) {
					if (kl.getLocalParameter(kcompIdr)==null) {
						kl.addLocalParameter(Utility.Parameter(kcompIdr, kcomp[1], GeneticNetwork.getMoleTimeParameter(1)));
					}
				} else {
					if (kl.getLocalParameter(kcompId)==null) {
						kl.addLocalParameter(Utility.Parameter(kcompId, kcomp[0] / kcomp[1], GeneticNetwork.getMoleParameter(2)));
					}
				}
			} else {
				if (kdecay > 0) {
					if (kl.getLocalParameter(kcompIdr)==null) {
						kl.addLocalParameter(Utility.Parameter(kcompIdr, 1, GeneticNetwork.getMoleTimeParameter(1)));
					}
				} else {
					if (kl.getLocalParameter(kcompId)==null) {
						kl.addLocalParameter(Utility.Parameter(kcompId, kcomp[0], GeneticNetwork.getMoleParameter(2)));
					}
				}
			}
			String ncSum = "";
			for (Influence infl : complexMap.get(complexId)) {
				String partId = infl.getInput();
				double n = infl.getCoop();
				String nId = coopString + "__" + partId + "_" + complexId;
				if (kl.getLocalParameter(nId)==null) {	
					kl.addLocalParameter(Utility.Parameter(nId, n, "dimensionless"));
				}
				ncSum = ncSum + nId + "+";
				if (!partId.equals(sequesterRoot)) {
					compExpression = compExpression + "*" + "(";
					if (species.get(partId).isAbstractable()) {
						compExpression = compExpression + abstractComplexHelper(partId, multiplier * n, sequesterRoot, operatorAbstraction);
					} else if (sequesterRoot.equals("") && species.get(partId).isSequesterable()) {
//						if (species.get(partId).isSequesterable())
						compExpression = compExpression + sequesterSpeciesHelper(partId, sequesterRoot, operatorAbstraction);
//						else
//							compExpression = compExpression + partId;
//						if (reactantStoich.containsKey(partId))
//							reactantStoich.put(partId, reactantStoich.get(partId)
//									+ multiplier * n);
//						else
//							reactantStoich.put(partId, multiplier * n);
						modifiers.add(partId);
					} else {
						compExpression = compExpression + partId;
						modifiers.add(partId);
					}
					compExpression = compExpression + ")^" + nId;
				}
			}
			compExpression = "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-1)" + compExpression;
			if (kdecay > 0)
				compExpression = "(" + kcompIdf + "/" + "(" + kcompIdr + "+" + kdecayId + "))" + compExpression;
			else
				compExpression = kcompId + compExpression;
		} else if (!sbmlMode) {
			double ncSum = 0;
			for (Influence infl : complexMap.get(complexId)) {
				String partId = infl.getInput();
				double n = infl.getCoop();
				ncSum = ncSum + n;
				if (!partId.equals(sequesterRoot)) {
					compExpression = compExpression + "*" + "(";
					if (species.get(partId).isAbstractable()) {
						compExpression = compExpression + abstractComplexHelper(partId, multiplier * n, sequesterRoot, operatorAbstraction);
					} else if (sequesterRoot.equals("")) {
						if (species.get(partId).isSequesterable())
							compExpression = compExpression + sequesterSpeciesHelper(partId, sequesterRoot, operatorAbstraction);
						else
							compExpression = compExpression + partId;
					} else {
						compExpression = compExpression + partId;
					}
					compExpression = compExpression + ")^" + n;
				}
			}
			double[] kcomp = species.get(complexId).getKc();
			double kdecay = species.get(complexId).getDecay();
			if (kcomp.length == 2) {
				compExpression = kcomp[0]/(kcomp[1] + kdecay) + "^" + (ncSum - 1) + compExpression;
			} else {
				compExpression = kcomp[0]/(1 + kdecay) + "^" + (ncSum - 1) + compExpression;
			}
		}
		return compExpression;
	}

	public String sequesterSpecies(String speciesId, double n, boolean operatorAbstraction) {
		if (sbmlMode)
			modifiers = new HashSet<String>();
		String expression = sequesterSpeciesHelper(speciesId, "", operatorAbstraction);
		if (sbmlMode) {
//			if (operatorAbstraction) {
			if (n > 0) {
				SBMLutilities.removeModifier(r, speciesId);
				r.addModifier(Utility.ModifierSpeciesReference(speciesId));
			} 
//			} else if (n > 0)  // n = 0 in the case of a complex formation reaction for a sequestered complex (complex added as reaction product and not reactant)
//				r.addReactant(Utility.SpeciesReference(speciesId, n));
			for (String modifier : modifiers) {
				SBMLutilities.removeModifier(r, modifier);
				r.addModifier(Utility.ModifierSpeciesReference(modifier));
			}
		}
		return expression;
	}
	
	private String sequesterSpeciesHelper(String speciesId, String sequesterRoot, boolean operatorAbstraction) {
		String sequesterFactor = "";
		if (sequesterRoot.equals(""))
			sequesterFactor = speciesId + "/(1";
		for (Influence infl : partsMap.get(speciesId)) {
			String complexId = infl.getOutput();
			if (species.get(complexId).isSequesterAbstractable() && sequesterRoot.equals("")) {
				sequesterFactor = sequesterFactor + "+" + abstractComplexHelper(complexId, 1, speciesId, operatorAbstraction);
				if (partsMap.containsKey(complexId))
					sequesterFactor = sequesterFactor + sequesterSpeciesHelper(complexId, speciesId, operatorAbstraction);
			} else if (species.get(complexId).isSequesterAbstractable()) {
				sequesterFactor = sequesterFactor + "+" + abstractComplexHelper(complexId, 1, sequesterRoot, operatorAbstraction);
				if (partsMap.containsKey(complexId))
					sequesterFactor = sequesterFactor + sequesterSpeciesHelper(complexId, sequesterRoot, operatorAbstraction);
			}
		}
		if (sequesterRoot.equals(""))
			sequesterFactor = sequesterFactor + ")";
		return sequesterFactor;
	}

	public String abstractOperatorSite(Promoter promoter) {
		if (sbmlMode)
			kl.addLocalParameter(Utility.Parameter("RNAP", RNAP, GeneticNetwork.getMoleParameter(2)));
		String promRate = "";
		if (promoter.getActivators().size() != 0) {
			double np = promoter.getStoich();
			double ng = promoter.getInitialAmount();
			double kb = promoter.getKbasal();
			double[] KoArray = promoter.getKrnap();
			double Ko;
			if (KoArray.length == 2) {
				Ko = KoArray[0] / KoArray[1];
			}
			else {
				Ko = KoArray[0];
			}
			double[] KaoArray = promoter.getKArnap();
			double Kao;
			if (KaoArray.length == 2) {
				Kao = KaoArray[0] / KaoArray[1];
			}
			else {
				Kao = KaoArray[0];
			}
			if (sbmlMode) {
				promRate += "(ng__" + promoter.getId() + ")*((kb__" + promoter.getId() + "*Ko__"
						+ promoter.getId() + "*RNAP)";
				kl.addLocalParameter(Utility.Parameter("ng__" + promoter.getId(), ng, GeneticNetwork
						.getMoleParameter(2)));
				kl.addLocalParameter(Utility.Parameter("kb__" + promoter.getId(), kb, GeneticNetwork
						.getMoleTimeParameter(1)));
				kl.addLocalParameter(Utility.Parameter("Ko__" + promoter.getId(), Ko, GeneticNetwork
						.getMoleParameter(2)));
				kl.addLocalParameter(Utility.Parameter("Kao__" + promoter.getId(), Kao, GeneticNetwork
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
						else if (sbmlMode) {
							SBMLutilities.removeModifier(r, activator);
							r.addModifier(Utility.ModifierSpeciesReference(activator));
						}
						if (sbmlMode) {
							promRate += "+(ka__" + activator + "_" + promoter.getId() + "*RNAP*((Ka__"
									+ activator + "_" + promoter.getId() + "*" + expression
									+ ")^nc__" + activator + "_" + promoter.getId() + "))";
							actBottom += "+(Kao__" + promoter.getId() + "*RNAP*(Ka__" + activator + "_" + promoter.getId() + "*"
									+ expression + ")^nc__" + activator + "_" + promoter.getId()
									+ ")";
							kl.addLocalParameter(Utility.Parameter("nc__" + activator + "_"
									+ promoter.getId(), nc, "dimensionless"));
							kl.addLocalParameter(Utility.Parameter("Ka__" + activator + "_"
									+ promoter.getId(), Ka, GeneticNetwork.getMoleParameter(2)));
							kl.addLocalParameter(Utility.Parameter("ka__" + activator + "_"
											+ promoter.getId(), ka, GeneticNetwork
											.getMoleTimeParameter(1)));
						}
						else {
							promRate += "+(" + ka + "*" + Kao + "*" + RNAP + "*((" + Ka + "*" + expression
									+ ")^" + nc + "))";
							actBottom += "+(" + Kao + "*" + RNAP + "*" + "(" + Ka + "*" + expression + ")^" + nc
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
								else if (sbmlMode) {
									SBMLutilities.removeModifier(r, repressor);
									r.addModifier(Utility.ModifierSpeciesReference(repressor));
								}
								if (sbmlMode) {
									promRate += "+((Kr__" + repressor + "_" + promoter.getId()
											+ "*" + expression + ")^nc__" + repressor + "_"
											+ promoter.getId() + ")";
									kl.addLocalParameter(Utility.Parameter("nc__" + repressor + "_"
											+ promoter.getId(), nc, "dimensionless"));
									kl.addLocalParameter(Utility.Parameter("Kr__" + repressor + "_"
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
			double np = promoter.getStoich();
			double ng = promoter.getInitialAmount();
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
				kl.addLocalParameter(Utility.Parameter("ng__" + promoter.getId(), ng, GeneticNetwork
						.getMoleParameter(2)));
				kl.addLocalParameter(Utility.Parameter("Ko__" + promoter.getId(), Ko, GeneticNetwork
						.getMoleParameter(2)));
				kl.addLocalParameter(Utility.Parameter("ko__" + promoter.getId(), ko, GeneticNetwork
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
						else if (sbmlMode) {
							SBMLutilities.removeModifier(r, repressor);
							r.addModifier(Utility.ModifierSpeciesReference(repressor));
						}
						if (sbmlMode) {
							promRate += "+((Kr__" + repressor + "_" + promoter.getId() + "*"
							+ expression + ")^nc__" + repressor + "_"
							+ promoter.getId() + ")";
							kl.addLocalParameter(Utility.Parameter("nc__" + repressor + "_"
									+ promoter.getId(), nc, "dimensionless"));
							kl.addLocalParameter(Utility.Parameter("Kr__" + repressor + "_"
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
		return promRate;
	}

	public String abstractDecay(String speciesId) {
		String decayExpression = "";
		double kd = species.get(speciesId).getDecay();
		if (kdSequesterEquivalence(speciesId, kd)) {
			if (sbmlMode) {
				kl.addLocalParameter(Utility.Parameter("kd", kd, GeneticNetwork.getMoleTimeParameter(1)));
				r.addReactant(Utility.SpeciesReference(speciesId, 1));
				decayExpression = "kd*" + speciesId;
			} else
				decayExpression = kd + "*" + speciesId;
		} else if (sbmlMode) {
			modifiers = new HashSet<String>();
			String kdId;
			for (Influence infl : partsMap.get(speciesId)) {
				String complexId = infl.getOutput();
				if (species.get(complexId).isSequesterAbstractable() && species.get(complexId).getDecay() > 0) {
					kdId = decayString + "__" + complexId;
					kd = species.get(complexId).getDecay();
					kl.addLocalParameter(Utility.Parameter(kdId, kd, GeneticNetwork.getMoleTimeParameter(1)));
					decayExpression = decayExpression + "+" + kdId + "*" + abstractComplexHelper(complexId, 1, speciesId, false);
				}
			}
			kd = species.get(speciesId).getDecay();
			if (kd > 0) {
				kdId = decayString + "__" + speciesId;
				decayExpression = "(" + kdId + decayExpression + ")*" + sequesterSpeciesHelper(speciesId, "", false);
				kl.addLocalParameter(Utility.Parameter(kdId, kd, GeneticNetwork.getMoleTimeParameter(1)));
			} else if (decayExpression.length() > 0) {
				decayExpression = "(" + decayExpression.substring(1, decayExpression.length()) + ")*" 
				+ sequesterSpeciesHelper(speciesId, "", false);
			}
			r.addReactant(Utility.SpeciesReference(speciesId, 1));
			for (String modifier : modifiers) {
				SBMLutilities.removeModifier(r, modifier);
				r.addModifier(Utility.ModifierSpeciesReference(modifier));
			}
		} else if (!sbmlMode) {
			for (Influence infl : partsMap.get(speciesId)) {
				String complexId = infl.getOutput();
				if (species.get(complexId).isSequesterAbstractable() && species.get(complexId).getDecay() > 0) {
					kd = species.get(complexId).getDecay();
					decayExpression = decayExpression + "+" + kd + "*" + abstractComplexHelper(complexId, 1, speciesId, false);
				}
			}
			kd = species.get(speciesId).getDecay();
			if (kd > 0) {
				decayExpression = "(" + kd + decayExpression + ")*" + sequesterSpeciesHelper(speciesId, "", false);
			} else if (decayExpression.length() > 0) {
				decayExpression = "(" + decayExpression.substring(1, decayExpression.length()) + ")*" 
				+ sequesterSpeciesHelper(speciesId, "", false);
			}
		}
		return decayExpression;
	}
	
	private boolean kdSequesterEquivalence(String speciesId, double kd) {
		if (kd == 0)
			return false;
		for (Influence infl : partsMap.get(speciesId)) {
			String complexId = infl.getOutput();
			if (species.get(complexId).isSequesterAbstractable() && species.get(complexId).getDecay() != kd) {
				return false;
			}
		}
		return true;
	}
	
	private Reaction r;
	private KineticLaw kl;
	private HashMap<String, SpeciesInterface> species;
//	private HashMap<String, Double> reactantStoich;
	private HashSet<String> modifiers;
	private HashMap<String, ArrayList<Influence>> complexMap;
	private HashMap<String, ArrayList<Influence>> partsMap;
	private double RNAP;
	private boolean sbmlMode;

	private String decayString = GlobalConstants.KDECAY_STRING;
	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
}

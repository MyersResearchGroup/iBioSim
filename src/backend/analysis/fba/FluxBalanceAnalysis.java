package backend.analysis.fba;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FBCReactionPlugin;
import org.sbml.jsbml.ext.fbc.Objective.Type;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import com.joptimizer.optimizers.*;

import dataModels.biomodel.util.SBMLutilities;

public class FluxBalanceAnalysis {
	
	private String root;
	
	private Model model;
	
	private FBCModelPlugin fbc;
	
	private double absError;
	
	private HashMap<String,Double> fluxes;
	
	public FluxBalanceAnalysis(String root,String sbmlFileName,double absError) {
		this.root = root;
		this.absError = absError;
		fluxes = new HashMap<String,Double>();
		SBMLDocument sbml = SBMLutilities.readSBML(root + sbmlFileName);
		model = sbml.getModel();
		fbc = SBMLutilities.getFBCModelPlugin(sbml.getModel(),true);
	}
	
	public FluxBalanceAnalysis(Model model,double absError) {
		this.root = null;
		this.absError = absError;
		fluxes = new HashMap<String,Double>();
		this.model = model;
		fbc = SBMLutilities.getFBCModelPlugin(model,true);
	}
	
	public static String vectorToString(double[] objective, HashMap<String,Integer> reactionIndex) {
		String result = "";
		for (String reaction : reactionIndex.keySet()) {
			double value = objective[reactionIndex.get(reaction)];
			if (value == 1) {
				if (!result.equals("")) result += " + ";
				result += reaction;
			} else if (value == -1) {
				if (!result.equals("")) result += " + ";
				result += "-" + reaction;
			} else if (value != 0) {
				if (!result.equals("")) result += " + ";
				result += value + "*" + reaction;
			}
		}
		return result;
	}
	
	public int PerformFluxBalanceAnalysis(){
		if (fbc == null) return -1;
		if (fbc.getNumObjective()==0) return -11;
		if (fbc.getActiveObjective()==null || fbc.getActiveObjective().equals("")) return -12; //TODO: what if missing active?

		HashMap<String, Integer> reactionIndex = new HashMap<String, Integer>();
		int kp = 0;
//		for(int l = 0;l<fbc.getListOfFluxBounds().size();l++){
//			if(!reactionIndex.containsKey(fbc.getFluxBound(l).getReaction())){
//				reactionIndex.put(fbc.getFluxBound(l).getReaction(), kp);
//				kp++;
//			}
//		}
		// Support for FBC Version 2
		for (int l = 0; l < model.getReactionCount(); l++) {
			Reaction r = model.getReaction(l);
			FBCReactionPlugin rBounds = (FBCReactionPlugin)r.getExtension(FBCConstants.namespaceURI);
			if (rBounds!=null) {
				if (rBounds.isSetLowerFluxBound()||rBounds.isSetUpperFluxBound()) {
					reactionIndex.put(r.getId(), kp);
					kp++;
				}
			}
		}
		for (int i = 0; i < fbc.getListOfObjectives().size(); i++) {
			if (!fbc.getActiveObjective().equals(fbc.getObjective(i).getId())) continue;
			double [] objective = new double[model.getReactionCount()];				
			for (int j = 0; j < fbc.getObjective(i).getListOfFluxObjectives().size(); j++) {
				if (reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())==null) {
					// no flux bound on objective
					return -9;
				}
				if (fbc.getObjective(i).getType().equals(Type.MINIMIZE)) {
					objective [reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())] = fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient();
				} else {
					objective [reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())] = (-1)*fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient();
				}
			}
			//System.out.println("Minimize: " + vectorToString(objective,reactionIndex));
			//System.out.println("Subject to:");

			double [] lowerBounds = new double[model.getReactionCount()];
			double [] upperBounds = new double[model.getReactionCount()];
			double minLb = LPPrimalDualMethod.DEFAULT_MIN_LOWER_BOUND;
			double maxUb = LPPrimalDualMethod.DEFAULT_MAX_UPPER_BOUND;
			int m = 0;
//			for (int j = 0; j < fbc.getListOfFluxBounds().size(); j++) {
//				FluxBound bound = fbc.getFluxBound(j);
//				//double R [] = new double [reactionIndex.size()];
//				double boundVal = bound.getValue();
//				if (Double.isInfinite(boundVal) && boundVal > 0) boundVal = 10;
//				if(bound.getOperation().equals(FluxBound.Operation.GREATER_EQUAL)){
//					if (Double.isInfinite(boundVal)) boundVal = minLb;
//					lowerBounds[reactionIndex.get(bound.getReaction())] = boundVal;
//					//R[reactionIndex.get(bound.getReaction())]=1;
//					//System.out.println("  " + vectorToString(R,reactionIndex) + " >= " + boundVal);
//				}
//				else if(bound.getOperation().equals(FluxBound.Operation.LESS_EQUAL)){
//					if (Double.isInfinite(boundVal)) boundVal = maxUb;
//					upperBounds[reactionIndex.get(bound.getReaction())] = boundVal;
//					//R[reactionIndex.get(bound.getReaction())]=1;
//					//System.out.println("  " + vectorToString(R,reactionIndex) + " <= " + boundVal);
//				} 
//				else if(bound.getOperation().equals(FluxBound.Operation.EQUAL)){
//					lowerBounds[reactionIndex.get(bound.getReaction())] = boundVal; 
//					upperBounds[reactionIndex.get(bound.getReaction())] = boundVal; 
//					//R[reactionIndex.get(bound.getReaction())]=1;
//					//System.out.println("  " + vectorToString(R,reactionIndex) + " == " + boundVal);
//				}
//			}
			// Support for FBC Version 2
			for (int l = 0; l < model.getReactionCount(); l++) {
				Reaction r = model.getReaction(l);
				FBCReactionPlugin rBounds = (FBCReactionPlugin)r.getExtension(FBCConstants.namespaceURI);
				if (rBounds!=null) {
					if (rBounds.isSetLowerFluxBound()) {
						lowerBounds[reactionIndex.get(r.getId())] = rBounds.getLowerFluxBoundInstance().getValue();
						if (Double.isInfinite(lowerBounds[reactionIndex.get(r.getId())])) 
							lowerBounds[reactionIndex.get(r.getId())] = minLb;
					}
					if (rBounds.isSetUpperFluxBound()) {
						upperBounds[reactionIndex.get(r.getId())] = rBounds.getUpperFluxBoundInstance().getValue();
						if (Double.isInfinite(upperBounds[reactionIndex.get(r.getId())])) 
							upperBounds[reactionIndex.get(r.getId())] = maxUb;
					}
				}
			}

			m = 0;
			int nonBoundarySpeciesCount = 0;
			for (int j = 0; j < model.getSpeciesCount(); j++) {
				if (!model.getSpecies(j).getBoundaryCondition()) nonBoundarySpeciesCount++;
			}
			double[][] stoch = new double [nonBoundarySpeciesCount][(model.getReactionCount())];
			double[] zero = new double [nonBoundarySpeciesCount];
			for (int j = 0; j < model.getSpeciesCount(); j++) {
				Species species = model.getSpecies(j);
				if (species.getBoundaryCondition()) continue;
				zero[m] = 0;
				for (int k = 0; k < model.getReactionCount(); k++) {
					Reaction r = model.getReaction(k);
					if (reactionIndex.get(r.getId())==null) {
						// reaction missing flux bound
						return -10;
					}
					for (int l = 0; l < r.getReactantCount(); l++) {
						SpeciesReference sr = r.getReactant(l);
						if (sr.getSpecies().equals(species.getId())) {
							stoch[m][(reactionIndex.get(r.getId()))]=(-1)*sr.getStoichiometry();
						}
					}
					for (int l = 0; l < r.getProductCount(); l++) {
						SpeciesReference sr = r.getProduct(l);
						if (sr.getSpecies().equals(species.getId())) {
							stoch[m][(reactionIndex.get(r.getId()))]=sr.getStoichiometry();
						}
					}
				}
				m++;
			}

			//optimization problem
			LPOptimizationRequest or = new LPOptimizationRequest();
			//or.setDumpProblem(true);
			or.setC(objective);
			or.setA(stoch);
			or.setB(zero);
			or.setLb(lowerBounds);
			or.setUb(upperBounds);
			or.setTolerance(absError);
			or.setToleranceFeas(absError);

			//optimization
			LPPrimalDualMethod opt = new LPPrimalDualMethod();
			opt.setLPOptimizationRequest(or);
			try {
				int error = opt.optimize();
				double [] sol = opt.getLPOptimizationResponse().getSolution();
				if (root!=null) {
					File f = new File(root + "sim-rep.txt");
					FileWriter fw = new FileWriter(f);
					BufferedWriter bw = new BufferedWriter(fw);
					double objkVal = 0;
					double objkCo = 0;
					for (int j = 0; j < fbc.getObjective(i).getListOfFluxObjectives().size(); j++) { 
						objkCo = fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient();
						double scale = Math.round(1/absError);
						objkVal += Math.round(objkCo*sol[reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())] * scale) / scale;
					}
					String firstLine = ("#total Objective");
					String secondLine = ("100 " + objkVal);
					for (String reaction : reactionIndex.keySet()) {
						double value = sol[reactionIndex.get(reaction)];
						double scale = Math.round(1/absError);
						value = Math.round(value * scale) / scale;  
						firstLine += (" " + reaction);
						secondLine += (" "+ value);
					}
					bw.write(firstLine);
					bw.write("\n");
					bw.write(secondLine);
					bw.write("\n");
					bw.close();
				} else {
					for (String reaction : reactionIndex.keySet()) {
						double value = sol[reactionIndex.get(reaction)];
						double scale = Math.round(1/absError);
						value = Math.round(value * scale) / scale;  
						fluxes.put(reaction, value);
					}
				}
				return error;
			} catch (Exception e) {
				File f = new File(root + "sim-rep.txt");
				if (f.exists()) {
					f.delete();
				}
				if (e.getMessage().equals("initial point must be strictly feasible")) return -2;
				else if (e.getMessage().equals("infeasible problem")) return -3;
				else if (e.getMessage().equals("singular KKT system")) return -4;
				else if (e.getMessage().equals("matrix must have at least one row")) return -5;
				else if (e.getMessage().equals("matrix is singular")) return -6;
				else if (e.getMessage().equals("Equalities matrix A must have full rank")) return -7;
				else {
					System.out.println(e.getMessage());
					return -8;
				}
			}
		}
		return -13;
	}

	public HashMap<String, Double> getFluxes() {
		return fluxes;
	}
	
	public void setBoundParameters(HashMap<String,Double> bounds) {
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter p = model.getParameter(i);
			if (bounds.containsKey(p.getId())) {
				p.setValue(bounds.get(p.getId()));
			}
		}
	}
}
package analysis;

import java.text.NumberFormat;
import java.util.HashMap;

import main.Gui;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.sbml.libsbml.FbcModelPlugin;
import org.sbml.libsbml.FluxBound;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;

import com.joptimizer.solvers.*;
import com.joptimizer.optimizers.*;
import com.joptimizer.functions.*;
import com.joptimizer.util.*;

public class FluxBalanceAnalysis {
	
	private String root;
	
	private String sbmlFileName;
	
	private SBMLDocument sbml;
	
	private FbcModelPlugin fbc;
	
	private NumberFormat nf;
	
	private double absError;
	
	
	public FluxBalanceAnalysis(String root,String sbmlFileName,double absError) {
		// Load the SBML file
		this.root = root;
		this.sbmlFileName = sbmlFileName;
		this.absError = absError;
		sbml = Gui.readSBML(root + this.sbmlFileName);
		fbc = (FbcModelPlugin)sbml.getModel().getPlugin("fbc");
		
		//Set number of digits after decimal point
		nf = NumberFormat.getNumberInstance();		
		nf.setMaximumFractionDigits(4);
		nf.setGroupingUsed(false);
	}
	
	public String vectorToString(double[] objective, HashMap<String,Integer> reactionIndex) {
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
	
	// TODO: Scott - remove all print to console
	public int PerformFluxBalanceAnalysis(){
		if (fbc != null) {
			HashMap<String, Integer> reactionIndex = new HashMap<String, Integer>();
			int kp = 0;
			for(int l =0;l<fbc.getNumFluxBounds();l++){
				if(!reactionIndex.containsKey(fbc.getFluxBound(l).getReaction())){
					reactionIndex.put(fbc.getFluxBound(l).getReaction(), kp);
					kp++;
				}
			}
			for (long i = 0; i < fbc.getNumObjectives(); i++) {
				double [] objective = new double[(int) sbml.getModel().getNumReactions()];				
				for (int j = 0; j < fbc.getObjective(i).getNumFluxObjectives(); j++) {
					if (fbc.getObjective(i).getType().equals("minimize")) {
						objective [(int) reactionIndex.get(fbc.getObjective(i).getFluxObjective(j).getReaction())] = fbc.getObjective(i).getFluxObjective(j).getCoefficient();
					} else {
						objective [(int) reactionIndex.get(fbc.getObjective(i).getFluxObjective(j).getReaction())] = (-1)*fbc.getObjective(i).getFluxObjective(j).getCoefficient();
					}
				}
				LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(objective, 0);
				System.out.println("Minimize: " + vectorToString(objective,reactionIndex));
				System.out.println("Subject to:");

				int numEquals = 0;
				for (long j = 0; j < fbc.getNumFluxBounds(); j++) {
					if (fbc.getFluxBound(j).getOperation().equals("equal")) {
						numEquals++;
					}
				}
				
				ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[(int)(fbc.getNumFluxBounds())-numEquals];
				int m = 0;
				for (long j = 0; j < fbc.getNumFluxBounds(); j++) {
					FluxBound bound = fbc.getFluxBound(j);
					double R [] = new double [reactionIndex.size()];
					double boundVal = bound.getValue();
					if(bound.getOperation().equals("greaterEqual")){
						R[reactionIndex.get(bound.getReaction())]=-1;
						inequalities[m] = new LinearMultivariateRealFunction(R, boundVal);
						m++;
						if (boundVal!=0) boundVal=(-1)*boundVal;
						System.out.println("  " + vectorToString(R,reactionIndex) + " <= " + boundVal);
					}
					else if(bound.getOperation().equals("lessEqual")){
						R[reactionIndex.get(bound.getReaction())]=1;
						System.out.println("  " + vectorToString(R,reactionIndex) + " <= " + boundVal);
						if (boundVal!=0) boundVal=(-1)*boundVal;
						inequalities[m] = new LinearMultivariateRealFunction(R, boundVal);
						m++;
					} 
				}

				m = 0;
				int nonBoundarySpeciesCount = 0;
				for (int j = 0; j < sbml.getModel().getNumSpecies(); j++) {
					if (!sbml.getModel().getSpecies(j).getBoundaryCondition()) nonBoundarySpeciesCount++;
				}
				double[][] stoch = new double [nonBoundarySpeciesCount+numEquals][(int) (sbml.getModel().getNumReactions())];
				double[] zero = new double [nonBoundarySpeciesCount+numEquals];
				for (long j = 0; j < sbml.getModel().getNumSpecies(); j++) {
					Species species = sbml.getModel().getSpecies(j);
					if (species.getBoundaryCondition()) continue;
					zero[m] = 0;
					for (long k = 0; k < sbml.getModel().getNumReactions(); k++) {
						Reaction r = sbml.getModel().getReaction(k);
						for (long l = 0; l < r.getNumReactants(); l++) {
							SpeciesReference sr = r.getReactant(l);
							if (sr.getSpecies().equals(species.getId())) {
								stoch[m][(int) (reactionIndex.get(r.getId()))]=(-1)*sr.getStoichiometry();
							}
						}
						for (long l = 0; l < r.getNumProducts(); l++) {
							SpeciesReference sr = r.getProduct(l);
							if (sr.getSpecies().equals(species.getId())) {
								stoch[m][(int) (reactionIndex.get(r.getId()))]=sr.getStoichiometry();
							}
						}
					}
					//inequalities[(int) (m + fbc.getNumFluxBounds())] = new LinearMultivariateRealFunction(stoch, 0);
					System.out.println("  " + vectorToString(stoch[m],reactionIndex) + " = 0" + " (" + species.getId() + ")");
					m++;
				}
				for (long j = 0; j < fbc.getNumFluxBounds(); j++) {
					FluxBound bound = fbc.getFluxBound(j);
					if (bound.getOperation().equals("equal")) {
						stoch[m][(int)(reactionIndex.get(bound.getReaction()))] = 1.0;
						zero[m] = bound.getValue();
						System.out.println("  " + vectorToString(stoch[m],reactionIndex) + " = " + zero[m]);
						m++;
					}
				}

				//optimization problem
				OptimizationRequest or = new OptimizationRequest();
				or.setF0(objectiveFunction);
				or.setA(stoch);
				or.setB(zero);
				or.setFi(inequalities);
				or.setTolerance(absError);
				or.setToleranceFeas(absError);
				//double[] ip = new double[reactionIndex.size()];
				//for (int j = 0; j < reactionIndex.size(); j++) {
				//	ip[j] = 0;
				//}
				//or.setInitialPoint(ip);//initial feasible point, not mandatory
				//or.setNotFeasibleInitialPoint(ip);

				//optimization
				JOptimizer opt = new JOptimizer();
				opt.setOptimizationRequest(or);
				try {
					opt.optimize();
					// TODO: SCOTT - create (root + "sim-rep.txt") file with the results in it.
					double [] sol = opt.getOptimizationResponse().getSolution();
					for (String reaction : reactionIndex.keySet()) {
						double value = sol[reactionIndex.get(reaction)];
						double scale = Math.round(1/absError);
						value = Math.round(value * scale) / scale;  
						if (value != 0) {
							System.out.println(reaction + " = " + value);
						}
					}
				} catch (Exception e) {
					// TODO: SCOTT - return exit code that problem is infeasible
					e.printStackTrace();
				}
			}
			return 0;
		} else {
			//System.out.println("No flux balance constraints");
			// TODO: SCOTT - change to code indicating no flux balance constraints
			return 0;
		}
	}
}
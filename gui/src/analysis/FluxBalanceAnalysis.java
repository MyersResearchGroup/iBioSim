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
	
	private JOptimizer jop;
	
	private String sbmlFileName;
	
	private SBMLDocument sbml;
	
	private FbcModelPlugin fbc;
	
	private NumberFormat nf;
	
	
	public FluxBalanceAnalysis(String sbmlFileName) {
		// Load the SBML file
		this.sbmlFileName = sbmlFileName;
		sbml = Gui.readSBML(this.sbmlFileName);
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

				ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[(int)(fbc.getNumFluxBounds())];

				for (long j = 0; j < fbc.getNumFluxBounds(); j++) {
					FluxBound bound = fbc.getFluxBound(j);
					double R [] = new double [reactionIndex.size()];
					if(reactionIndex.containsKey(bound.getReaction())){
						if(bound.getOperation().equals("greaterEqual")) {
							R[reactionIndex.get(bound.getReaction())]=-1;
						} else {
							R[reactionIndex.get(bound.getReaction())]=1;
						}
					}

					double boundVal = bound.getValue();
					if(bound.getOperation().equals("greaterEqual")){
						inequalities[(int) j] = new LinearMultivariateRealFunction(R, boundVal);
						if (boundVal!=0) boundVal=(-1)*boundVal;
						System.out.println("  " + vectorToString(R,reactionIndex) + " <= " + boundVal);
					}
					else if(bound.getOperation().equals("lessEqual")){
						System.out.println("  " + vectorToString(R,reactionIndex) + " <= " + boundVal);
						if (boundVal!=0) boundVal=(-1)*boundVal;
						inequalities[(int) j] = new LinearMultivariateRealFunction(R, boundVal);
					}
				}

				int m = 0;
				int nonBoundarySpeciesCount = 0;
				for (int j = 0; j < sbml.getModel().getNumSpecies(); j++) {
					if (!sbml.getModel().getSpecies(j).getBoundaryCondition()) nonBoundarySpeciesCount++;
				}
				double[][] stoch = new double [nonBoundarySpeciesCount][(int) (sbml.getModel().getNumReactions())];
				double[] zero = new double [nonBoundarySpeciesCount];
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
				//optimization problem
				OptimizationRequest or = new OptimizationRequest();
				or.setF0(objectiveFunction);
				or.setFi(inequalities);
				or.setA(stoch);
				or.setB(zero);
				double[] ip = new double[reactionIndex.size()];
				for (int j = 0; j < reactionIndex.size(); j++) {
					ip[j] = 0;
				}
				or.setNotFeasibleInitialPoint(ip);
				//or.setInitialPoint(zero);//initial feasible point, not mandatory
				or.setToleranceFeas(1.E-9);
				or.setTolerance(1.E-9);

				//optimization
				jop = new JOptimizer();
				jop.setOptimizationRequest(or);
				try {
					jop.optimize();
					double [] sol = jop.getOptimizationResponse().getSolution();
					for (int j = 0; j < sol.length; j++) {
						System.out.print(sol[j]+" ");
					}
					System.out.println();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return 0;
		} else {
			System.out.println("No flux balance constraints");
			return 0;
		}
	}
		
	/**
	 * @return
	 */
	public int PerformFluxBalanceAnalysis2(){
		// Fill the objective array
		if (fbc != null) {
			for (long i = 0; i < fbc.getNumObjectives(); i++) {
				HashMap<String, Integer> reactionIndex = new HashMap<String, Integer>();
				int kp = 0;
				for(int l =0;l<fbc.getNumFluxBounds();l++){
					if(!reactionIndex.containsKey(fbc.getFluxBound(l).getReaction())){
						reactionIndex.put(fbc.getFluxBound(l).getReaction(), kp);
						kp++;
					}
				}
				System.out.println(reactionIndex);
				
				//Set the array to be the max it will ever be, Min & max for each + possible reversible reactions
				double [] objective = new double[(int) sbml.getModel().getNumReactions()];				
				for (int j = 0; j < fbc.getObjective(i).getNumFluxObjectives(); j++) {
					objective [(int) reactionIndex.get(fbc.getObjective(i).getFluxObjective(j).getReaction())] = fbc.getObjective(i).getFluxObjective(j).getCoefficient();
					System.out.println(fbc.getObjective(i).getFluxObjective(j).getReaction() + " " + fbc.getObjective(i).getFluxObjective(j).getColumn());
					// Insert the coefficient in the array in the appropriate location for the reaction in this flux objective
				}
				String printTest = "";
				for (int j = 0; j < objective.length; j++) {
					printTest += (objective[j] + " ");
				}				
				System.out.println(printTest);
				// [ 0 0 ... 1.0 ]
				//LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(objective, 0);
				
				//TODO: Positive = minimum, negative = maximum???
				LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(new double[] {-1.,-1.}, 0);
				//Note: Doesn't like multiple objectives... two objectives needs 4 equalities?
				
				// Construct set of inequalities which is number of flux bounds (+ locations for equal bounds) + 2*number of species
				//inequalities (polyhedral feasible set G.X<H )
				//ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[(int) (2*(sbml.getModel().getNumSpecies()) + fbc.getNumFluxBounds())];
				
				ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[4];
				//TODO: If G is negative it sets a minimum, positive = maximum..... F always negative? Elaboration: no, F must be positive to set a min with a positive number
				//Does the position in G correspond with objctivefunction?
				//F/G=limit, Ex. G=-0.5 F=-100 minimum value >= -200 if there is only one.
				//Ex. for positive: G = 1 , F = -10 Max <= 10
				double[][] G = new double[][] {{1,1}, {3,1}, {2, 1.}, {0.5, 1.}};
				double[] F = new double[] {2., 1./2., 2., 1./2.};
				//Set Maximum
				inequalities[0] = new LinearMultivariateRealFunction(G[0], -15);
				//Set Minimum
				inequalities[1] = new LinearMultivariateRealFunction(G[1], -90);
				inequalities[2] = new LinearMultivariateRealFunction(G[2], -30);
				inequalities[3] = new LinearMultivariateRealFunction(G[3], -5);
				
				System.out.println(fbc.getNumFluxBounds());
				for (long j = 0; j < fbc.getNumFluxBounds(); j++) {
					FluxBound bound = fbc.getFluxBound(j);
					double R [] = new double [reactionIndex.size()];
					if(reactionIndex.containsKey(bound.getReaction())){
						if(bound.getOperation().equals("greaterEqual")) {
							R[reactionIndex.get(bound.getReaction())]=-1;
						} else {
							R[reactionIndex.get(bound.getReaction())]=1;
						}
					}
					

					
					if(bound.getOperation().equals("greaterEqual")){
						//inequalities[(int) j] = new LinearMultivariateRealFunction(R, bound.getValue());
					}
					else if(bound.getOperation().equals("lessEqual")){
						//inequalities[(int) j] = new LinearMultivariateRealFunction(R, bound.getValue());
					}
					String secondTest = "";
					for (int k = 0; k < R.length; k++) {
						secondTest += (R[k]+" ");
					}				
					System.out.println(secondTest);
					System.out.println(bound.getReaction() + " " + bound.getOperation() + " " + bound.getValue());
					// Create a vector R size number of reactions where the entry is 1.0 for the reaction for this bound and 0 otherwise
					// rSign/hSign is determined by the operation, <= +/-, >= -/+
					// inequalities[j] = new LinearMultivariateRealFunction(rSign * R, hSign * value);
				}
				
				for (long j = 0; j < sbml.getModel().getNumSpecies(); j++) {
					double[] stoch = new double [(int) (sbml.getModel().getNumReactions())];
					Species species = sbml.getModel().getSpecies(j);
					System.out.println(species.getId());
					// Construct a stoch vector size of number of reactions, init with 0's
					for (long k = 0; k < sbml.getModel().getNumReactions(); k++) {
						Reaction r = sbml.getModel().getReaction(k);
						for (long l = 0; l < r.getNumReactants(); l++) {
							SpeciesReference sr = r.getReactant(l);
							if (sr.getSpecies().equals(species.getId())) {
								System.out.println(r.getId() + ":" + (-1)*sr.getStoichiometry());
								stoch[(int) (reactionIndex.get(r.getId()))]=(-1)*sr.getStoichiometry();
							}
						}
						for (long l = 0; l < r.getNumProducts(); l++) {
							SpeciesReference sr = r.getProduct(l);
							if (sr.getSpecies().equals(species.getId())) {
								System.out.println(r.getId() + ":" + sr.getStoichiometry());
								stoch[(int) (reactionIndex.get(r.getId()))]=sr.getStoichiometry();
							}
						}
						// Do same for products
						// Insert (product stoch - reactant stoch) into appropriate location in the vector 
					}
					//inequalities[(int) (2*j + fbc.getNumFluxBounds())] = new LinearMultivariateRealFunction(stoch, 0);
					for(int k = 0; k<stoch.length;k++){
						stoch[k] = -1*stoch[k];
					}
					//inequalities[(int) (2*j + 1 + fbc.getNumFluxBounds())] = new LinearMultivariateRealFunction(stoch, 0);
				}
				//optimization problem
				OptimizationRequest or = new OptimizationRequest();
				or.setF0(objectiveFunction);
				or.setFi(inequalities);
				//or.setInitialPoint(new double[] {0.0, 0.0});//initial feasible point, not mandatory
				or.setToleranceFeas(1.E-9);
				or.setTolerance(1.E-9);
				
				//optimization
				jop = new JOptimizer();
				jop.setOptimizationRequest(or);
				try {
					jop.optimize();
					//TODO: Need to fill inequalities?
					
					double [] sol = jop.getOptimizationResponse().getSolution();
					
					for (long j = 0; j < fbc.getNumObjectives(); j++) {
						//TODO: Add for loop per each FluxObjective
						System.out.println(fbc.getObjective(j).getFluxObjective(0).getReaction() + " = " + sol[(int) j]);
						System.out.println(fbc.getObjective(j).getFluxObjective(0).getReaction() + " = " + sol[(int) 1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return 0;
		} else {
			System.out.println("No flux balance constraints");
			return 0;
		}
		
//		// Objective function (plane)
//		LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(new double[] { -1., -1. }, 4);
//
//		//inequalities (polyhedral feasible set G.X<H )
//		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[4];
//		double[][] G = new double[][] {{4./3., -1}, {-1./2., 1.}, {-2., -1.}, {1./3., 1.}};
//		double[] H = new double[] {2., 1./2., 2., 1./2.};
//		inequalities[0] = new LinearMultivariateRealFunction(G[0], -H[0]);
//		inequalities[1] = new LinearMultivariateRealFunction(G[1], -H[1]);
//		inequalities[2] = new LinearMultivariateRealFunction(G[2], -H[2]);
//		inequalities[3] = new LinearMultivariateRealFunction(G[3], -H[3]);
//		
//		//optimization problem
//		OptimizationRequest or = new OptimizationRequest();
//		or.setF0(objectiveFunction);
//		or.setFi(inequalities);
//		//or.setInitialPoint(new double[] {0.0, 0.0});//initial feasible point, not mandatory
//		or.setToleranceFeas(1.E-9);
//		or.setTolerance(1.E-9);
//		
//		//optimization
//		jop = new JOptimizer();
//		jop.setOptimizationRequest(or);
//		try {
//			jop.optimize();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		double [] sol = jop.getOptimizationResponse().getSolution();
//		System.out.println(nf.format(sol[0]));
//		System.out.println(nf.format(sol[1]));
//		return (int) sol[0];
	}

}
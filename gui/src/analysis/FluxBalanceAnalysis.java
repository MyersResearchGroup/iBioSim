package analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.HashMap;

import main.Gui;

import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FluxBound;
import org.sbml.jsbml.ext.fbc.Objective.Type;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import biomodel.gui.textualeditor.SBMLutilities;

import com.joptimizer.optimizers.*;
import com.joptimizer.functions.*;

public class FluxBalanceAnalysis {
	
	private String root;
	
	private String sbmlFileName;
	
	private SBMLDocument sbml;
	
	private FBCModelPlugin fbc;
	
	private NumberFormat nf;
	
	private double absError;
	
	
	public FluxBalanceAnalysis(String root,String sbmlFileName,double absError) {
		// Load the SBML file
		this.root = root;
		this.sbmlFileName = sbmlFileName;
		this.absError = absError;
		sbml = Gui.readSBML(root + this.sbmlFileName);
		fbc = (FBCModelPlugin)SBMLutilities.getPlugin(FBCConstants.namespaceURI, sbml.getModel(), true);
		
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
			for(int l =0;l<fbc.getListOfFluxBounds().size();l++){
				if(!reactionIndex.containsKey(fbc.getFluxBound(l).getReaction())){
					reactionIndex.put(fbc.getFluxBound(l).getReaction(), kp);
					kp++;
				}
			}
			for (int i = 0; i < fbc.getListOfObjectives().size(); i++) {
				double [] objective = new double[(int) sbml.getModel().getReactionCount()];				
				for (int j = 0; j < fbc.getObjective(i).getListOfFluxObjectives().size(); j++) {
					if (fbc.getObjective(i).getType().equals(Type.MINIMIZE)) {
						objective [(int) reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())] = fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient();
					} else {
						objective [(int) reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())] = (-1)*fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient();
					}
				}
				LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(objective, 0);
//				System.out.println("Minimize: " + vectorToString(objective,reactionIndex));
//				System.out.println("Subject to:");

				int numEquals = 0;
				for (int j = 0; j < fbc.getListOfFluxBounds().size(); j++) {
					if (fbc.getFluxBound(j).getOperation().equals(FluxBound.Operation.EQUAL)) {
						numEquals++;
					}
				}
				
				ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[(int)(fbc.getListOfFluxBounds().size())-numEquals];
				int m = 0;
				for (int j = 0; j < fbc.getListOfFluxBounds().size(); j++) {
					FluxBound bound = fbc.getFluxBound(j);
					double R [] = new double [reactionIndex.size()];
					double boundVal = bound.getValue();
					if(bound.getOperation().equals(FluxBound.Operation.GREATER_EQUAL)){
						R[reactionIndex.get(bound.getReaction())]=-1;
						inequalities[m] = new LinearMultivariateRealFunction(R, boundVal);
						m++;
						if (boundVal!=0) boundVal=(-1)*boundVal;
//						System.out.println("  " + vectorToString(R,reactionIndex) + " <= " + boundVal);
					}
					else if(bound.getOperation().equals(FluxBound.Operation.LESS_EQUAL)){
						R[reactionIndex.get(bound.getReaction())]=1;
//						System.out.println("  " + vectorToString(R,reactionIndex) + " <= " + boundVal);
						if (boundVal!=0) boundVal=(-1)*boundVal;
						inequalities[m] = new LinearMultivariateRealFunction(R, boundVal);
						m++;
					} 
				}

				m = 0;
				int nonBoundarySpeciesCount = 0;
				for (int j = 0; j < sbml.getModel().getSpeciesCount(); j++) {
					if (!sbml.getModel().getSpecies(j).getBoundaryCondition()) nonBoundarySpeciesCount++;
				}
				double[][] stoch = new double [nonBoundarySpeciesCount+numEquals][(int) (sbml.getModel().getReactionCount())];
				double[] zero = new double [nonBoundarySpeciesCount+numEquals];
				for (int j = 0; j < sbml.getModel().getSpeciesCount(); j++) {
					Species species = sbml.getModel().getSpecies(j);
					if (species.getBoundaryCondition()) continue;
					zero[m] = 0;
					for (int k = 0; k < sbml.getModel().getReactionCount(); k++) {
						Reaction r = sbml.getModel().getReaction(k);
						for (int l = 0; l < r.getReactantCount(); l++) {
							SpeciesReference sr = r.getReactant(l);
							if (sr.getSpecies().equals(species.getId())) {
								stoch[m][(int) (reactionIndex.get(r.getId()))]=(-1)*sr.getStoichiometry();
							}
						}
						for (int l = 0; l < r.getProductCount(); l++) {
							SpeciesReference sr = r.getProduct(l);
							if (sr.getSpecies().equals(species.getId())) {
								stoch[m][(int) (reactionIndex.get(r.getId()))]=sr.getStoichiometry();
							}
						}
					}
					m++;
				}
				for (int j = 0; j < fbc.getListOfFluxBounds().size(); j++) {
					FluxBound bound = fbc.getFluxBound(j);
					if (bound.getOperation().equals(FluxBound.Operation.EQUAL)) {
						stoch[m][(int)(reactionIndex.get(bound.getReaction()))] = 1.0;
						zero[m] = bound.getValue();
//						System.out.println("  " + vectorToString(stoch[m],reactionIndex) + " = " + zero[m]);
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
					File f = new File(root + "sim-rep.txt");
					FileWriter fw = new FileWriter(f);
					BufferedWriter bw = new BufferedWriter(fw);
					double [] sol = opt.getOptimizationResponse().getSolution();
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
//						System.out.println(reaction + " = " + value);
						firstLine += (" " + reaction);
						secondLine += (" "+ value);
					}
					bw.write(firstLine);
					bw.write("\n");
					bw.write(secondLine);
					bw.write("\n");
					bw.close();
				} catch (Exception e) {
					// DELETE sim-rep.txt here if it exists
					// TODO: SCOTT - return different code based on the message
					System.out.println(e.getMessage());
					return 1;
				}
			}
			return 0;
		} else {
			//System.out.println("No flux balance constraints");
			// TODO: SCOTT - change to code indicating no flux balance constraints
			return 2;
		}
	}
}
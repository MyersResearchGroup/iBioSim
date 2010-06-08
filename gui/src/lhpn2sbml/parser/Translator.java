package lhpn2sbml.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.batik.svggen.font.table.Program;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Delay;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.RateRule;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Trigger;
import org.sbml.libsbml.libsbml;

import sbmleditor.SBML_Editor;
import lhpn2sbml.parser.ExprTree;
import biomodelsim.BioSim;

/**
 * This class converts a lph file to a sbml file
 * 
 * @author Zhen Zhang
 * 
 */
public class Translator {
	private String filename;
	private SBMLDocument document;
	
	
	public void BuildTemplate(String lhpnFilename) {
		this.filename = lhpnFilename.replace(".lpn", ".xml");
		// load lhpn file
		LhpnFile lhpn = new LhpnFile();
		lhpn.load(lhpnFilename);
		
		// create sbml file
		document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		Model m = document.createModel();
		Compartment c = m.createCompartment();
		m.setId(filename.replace(".xml", ""));
		c.setId("default");
		c.setSize(1.0);
		c.setConstant(true);
		c.setSpatialDimensions(3);
		
		// Create bitwise operators for sbml
		createFunction(m, "BITAND", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITOR", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITNOT", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITXOR", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "mod", "Modular", "lambda(a,b,a-floor(a/b)*b)");
		createFunction(m, "and", "Logical AND", "lambda(a,b,a*b)");
		createFunction(m, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
		createFunction(m, "normal", "Normal distribution", "lambda(m,s,m)");
		createFunction(m, "exponential", "Exponential distribution", "lambda(mu,mu)");
		createFunction(m, "gamma", "Gamma distribution", "lambda(a,b,a*b)");
		createFunction(m, "lognormal", "Lognormal distribution", "lambda(z,s,exp(z+s^2/2))");
		createFunction(m, "chisq", "Chi-squared distribution", "lambda(nu,nu)");
		createFunction(m, "laplace", "Laplace distribution", "lambda(a,a)");
		createFunction(m, "cauchy", "Cauchy distribution", "lambda(a,a)");
		createFunction(m, "rayleigh", "Rayleigh distribution", "lambda(s,s*sqrt(pi/2))");
		createFunction(m, "poisson", "Poisson distribution", "lambda(mu,mu)");
		createFunction(m, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
		createFunction(m, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
		
		// translate from lhpn to sbml
		// ----variables -> parameters-----	
		for (String v: lhpn.getVariables()){
//			System.out.println("Vars from lhpn.getVariables() " + v);
			if (v != null){
				String initVal = lhpn.getInitialVal(v);
//				System.out.println("Begin:" + v + "= " + initVal);			
				if (lhpn.isContinuous(v) || lhpn.isInteger(v)){
					Parameter p = m.createParameter(); 
					p.setConstant(false);
					p.setId(v);
					
					// For each continuous variable v, create rate rule dv/dt and set its initial value to lhpn.getInitialRate(v). 
					if (lhpn.isContinuous(v)){
						Parameter p_dot = m.createParameter();
						p_dot.setConstant(false);
						p_dot.setId(v + "_dot");
//						System.out.println("v_dot = " + v + "_dot");
						RateRule rateRule = m.createRateRule();
						rateRule.setVariable(v);
						rateRule.setMath(SBML_Editor.myParseFormula(v + "_dot"));
						String initValDot= lhpn.getInitialRate(v);
						double initValDot_dbl = Double.parseDouble(initValDot);
						p_dot.setValue(initValDot_dbl);
					}
				
				
					// Assign initial values to continuous, discrete and boolean variables
//					Short term fix: Extract the lower and upper bounds and set the initial value to the mean. 
//									Anything that involves infinity, take either the lower or upper bound which is not infinity.  
//									If both are infinity, set to 0.	
					String initValue = lhpn.getInitialVal(v);
					String tmp_initValue = initValue;
					String[] subString = initValue.split(",");
					String lowerBound = null;
					String upperBound = null;
					
					// initial value is a range
					if (tmp_initValue.contains(",")){
						// If the initial value is a range, check the range only contains one ","
						tmp_initValue = tmp_initValue.replaceFirst(",", "");
//						// Test if tmp_initValue contains any more ",": if not, continue to extract upper and lower bounds 
//						if (tmp_initValue.contains(",")){
//							System.out.println("The inital range of variable " + v + " is incorrect.");
//							System.exit(0);
//						}
						// Extract the lower and upper bound of the initValue
						int i;
						for (i = 0; i<subString.length; i ++)
						{
//							System.out.println("splitted initValue range " + subString[i].toString());
							if (subString[i].contains("[")){
								lowerBound = subString[i].replace("[", "");	
//								System.out.println("remove [ " + subString[i].replace("[", "").toString());
							}
							if (subString[i].contains("uniform(")){
								lowerBound = subString[i].replace("uniform(", "");	
//								System.out.println("remove uniform( " + subString[i].replace("[", "").toString());
							}
							else if(subString[i].contains("]")){
								upperBound = subString[i].replace("]", "");
//								System.out.println("remove ] " + subString[i].replace("]", "").toString());
							}
							else if(subString[i].contains(")")){
								upperBound = subString[i].replace(")", "");
//								System.out.println("remove ) " + subString[i].replace("]", "").toString());
							}
						}
						
						// initial value involves infinity
						if (lowerBound.contains("inf") || upperBound.contains("inf")){
							if (lowerBound.contains("-inf") && upperBound.contains("inf")){
								initValue = "0" ; // if [-inf,inf], initValue = 0
							}
							else if (lowerBound.contains("-inf") && !upperBound.contains("inf")){
								initValue = upperBound; // if [-inf,a], initValue = a
							}
							else if (!lowerBound.contains("-inf") && upperBound.contains("inf")){
								initValue = lowerBound; // if [a,inf], initValue = a
							}
							double initVal_dbl = Double.parseDouble(initValue);
							p.setValue(initVal_dbl);
						}
						// initial value is a range, not involving infinity	
					    else {
					    	double lowerBound_dbl = Double.parseDouble(lowerBound);
					    	double upperBound_dbl = Double.parseDouble(upperBound);
					    	double initVal_dbl = (lowerBound_dbl + upperBound_dbl)/2;
					    	initVal_dbl = initVal_dbl;
					    	p.setValue(initVal_dbl);
						}	
					} 
					
					// initial value is a single number
					else {
							double initVal_dbl = Double.parseDouble(initValue);
							p.setValue(initVal_dbl);
					}
				}
				else  // boolean variable 
				{
					Parameter p = m.createParameter(); 
					p.setConstant(false);
					p.setId(v);
					String initValue = lhpn.getInitialVal(v);
//					System.out.println(v + "=" + initValue);
					// check initValue type; if boolean, set parameter value as 0 or 1.
					if (initValue.equals("true")){
						p.setValue(1);
					}
					else if (initValue.equals("false")){
						p.setValue(0);
					}
					else if (initValue.equals("unknown")){
						p.setValue(0);
					}
					else {
//							double initVal_dbl = Double.parseDouble(initValue);
//							p.setValue(initVal_dbl);
							System.out.println("It should be a boolean variable.");
							System.exit(0);
					}
				}
				

			}
		}
				
		// ----places -> species-----	
		for (String p: lhpn.getPlaceList()){
			
			Boolean initMarking = lhpn.getPlace(p).isMarked();
//			System.out.println(p + "=" + initMarking);
			Species sp = m.createSpecies();
			sp.setId(p);
			sp.setCompartment("default");
			sp.setBoundaryCondition(false);
			sp.setConstant(false);
			sp.setHasOnlySubstanceUnits(false);
			if (initMarking){
				sp.setInitialAmount(1);
			}
			else {
				sp.setInitialAmount(0);
			}
			sp.setUnits("");	
		}
		
		// ----convert transitions -----
		// if transition rate is not null, use reaction and event;
		// else use event only
			int counter = lhpn.getTransitionList().length - 1;
			for (String t : lhpn.getTransitionList()) {
				if(lhpn.getTransition(t).getTransitionRate()!=null){
					Species spT = m.createSpecies();
					spT.setId(t);
					spT.setCompartment("default");
					spT.setBoundaryCondition(false);
					spT.setConstant(false);
					spT.setHasOnlySubstanceUnits(false);
					spT.setInitialAmount(0);
					spT.setUnits("");
					
					Reaction r = m.createReaction();
					r.setReversible(false);
					r.setId("r" + counter);
					SpeciesReference reactant = r.createReactant();
					// get preset(s) of a transition and set each as a reactant
					for (String x : lhpn.getPreset(t)){
						reactant.setId(x);
						reactant.setSpecies(x);
						//System.out.println("transition:" + t + "preset:" + x);
					}
					
					SpeciesReference product  = r.createProduct();
					product.setSpecies(t);
						
					KineticLaw rateReaction = r.createKineticLaw(); // rate of reaction
					//Parameter p_local = rateReaction.createParameter();
					//p_local.setConstant(false);
					//p_local.setId("rate" + counter);
					// get the transition rate from LHPN
					//System.out.println("transition rate = " + lhpn.getTransitionRate(t));
					//double tRate = Double.parseDouble(lhpn.getTransitionRate(t));	
					//p_local.setValue(tRate);
					//lhpn.getTransitionRateTree(t)
					
					// create exp for KineticLaw
					// expTestNull is used to test if the enabling condition exists for a transistion t
					String expTestNull = lhpn.getTransition(t).getEnabling();
					String exp;
					if (expTestNull == null){
						exp = "1";
					}
					else {
						String tmp_exp = lhpn.getEnablingTree(t).getElement("SBML");
//						System.out.println("tmp_exp = "+ tmp_exp);
						exp = "piecewise(1," + tmp_exp + ",0)";

					}

					rateReaction.setFormula("(" + lhpn.getTransitionRateTree(t).getElement("SBML") + ")" + "*" + reactant.getSpecies() + "*" + exp); 
					//System.out.println("trans " + t + " enableCond " + lhpn.getEnabling(t));
					
					Event e = m.createEvent();
					e.setId("event" + counter);			
					Trigger trigger = e.createTrigger();
					trigger.setMath(SBML_Editor.myParseFormula("eq(" + product.getSpecies() + ",1)"));
					
					// t_postSet = 1
					for (String x : lhpn.getPostset(t)){
						EventAssignment assign0 = e.createEventAssignment();
						assign0.setVariable(x);
						assign0.setMath(SBML_Editor.myParseFormula("1"));
		//				System.out.println("transition: " + t + " postset: " + x);
					}
					
					// t = 0
					EventAssignment assign1 = e.createEventAssignment();
					assign1.setVariable(product.getSpecies());
					assign1.setMath(SBML_Editor.myParseFormula("0"));
					
					// assignment <A>
					// continuous assignment
					if (lhpn.getContVars(t) != null){
						for (String var : lhpn.getContVars(t)){
							if (lhpn.getContAssign(t, var) != null) {
								ExprTree assignContTree = lhpn.getContAssignTree(t, var);	
								String assignCont = assignContTree.toString("SBML");
//								System.out.println("continuous assign: "+ assignCont);
								EventAssignment assign2 = e.createEventAssignment();
								assign2.setVariable(var);
								assign2.setMath(SBML_Editor.myParseFormula(assignCont));
							}
						}
					}
					
					// integer assignment
					if (lhpn.getIntVars()!= null){
						for (String var : lhpn.getIntVars()){
							if (lhpn.getIntAssign(t, var) != null) {
								ExprTree assignIntTree = lhpn.getIntAssignTree(t, var);
								String assignInt = assignIntTree.toString("SBML");
//								System.out.println("integer assignment from LHPN: " + var + " := " + assignInt);
								EventAssignment assign3 = e.createEventAssignment();
								assign3.setVariable(var);
								assign3.setMath(SBML_Editor.myParseFormula(assignInt));
							}
						}
					}
					
					// boolean assignment
					if (lhpn.getBooleanVars(t)!= null){
						for (String var :lhpn.getBooleanVars(t)){
							if (lhpn.getBoolAssign(t, var) != null) {
								ExprTree assignBoolTree = lhpn.getBoolAssignTree(t, var);
								String assignBool_tmp = assignBoolTree.toString("SBML");
								String assignBool = "piecewise(1," + assignBool_tmp + ",0)";
//								System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
								EventAssignment assign4 = e.createEventAssignment();
								assign4.setVariable(var);
								assign4.setMath(SBML_Editor.myParseFormula(assignBool));
							}
						}
					}
					
					// rate assignment
					if (lhpn.getRateVars(t)!= null){
						for (String var : lhpn.getRateVars(t)){
//							 System.out.println("rate var: "+ var);
							if (lhpn.getRateAssign(t, var) != null) {
								ExprTree assignRateTree = lhpn.getRateAssignTree(t, var);
								String assignRate = assignRateTree.toString("SBML");
		//						System.out.println("rate assign: "+ assignRate);
								
								EventAssignment assign5 = e.createEventAssignment();
								assign5.setVariable(var + "_dot");
								assign5.setMath(SBML_Editor.myParseFormula(assignRate));
							}
						}
					}
				}
				
				
				 //Only use event
				else {			//lhpn.getContAssign(t, var) == null
//					System.out.println("Event Only");
					Event e = m.createEvent();
					e.setId("event" + counter);	
					Trigger trigger = e.createTrigger();
					
					//trigger = CheckPreset(t) && En(t);
					//test En(t)
					String EnablingTestNull = lhpn.getTransition(t).getEnabling();
					String Enabling;
					if (EnablingTestNull == null){
						Enabling = "eq(1,1)"; // Enabling is true (Boolean)
					}
					else {
						Enabling = lhpn.getEnablingTree(t).getElement("SBML");
					}
//					System.out.println("Enabling = " + Enabling);
					
					//test Preset(t)
					String CheckPreset = null;
					int indexPreset = 0;
					
					// Check if all the presets of transition t are marked
					for (String x:lhpn.getPreset(t)){
						if (indexPreset == 0){
							CheckPreset = "eq(" + x + ",1)";
						}
						else {
							CheckPreset = "and(" + CheckPreset + "," + "eq(" + x + ",1)" + ")";
						}	
						indexPreset ++;
					}
					
					trigger.setMath(SBML_Editor.myParseFormula("and(gt(t,0)," + CheckPreset + "," + Enabling + ")"));
										
					// triggerCanBeDisabled := true
					trigger.setAnnotation("<TriggerCanBeDisabled/>");
					
					// use values at trigger time = false
					e.setUseValuesFromTriggerTime(false);
				    
					// Delay D(t)
					if (lhpn.getTransition(t).getDelay()!=null) {
						Delay delay = e.createDelay();
						delay.setMath(SBML_Editor.myParseFormula(lhpn.getTransition(t).getDelay()));
					}
					
					// Check if there is any self-loop. If the intersection between lhpn.getPreset(t) and lhpn.getPostset(t)
					// is not empty, self-loop exists. 
					List<String> t_preset = Arrays.asList(lhpn.getPreset(t));
					List<String> t_postset = Arrays.asList(lhpn.getPostset(t));
					List<String> t_intersect = new ArrayList<String>(); // intersection of t_preset and t_postset
					List<String> t_NoIntersect = new ArrayList<String>(); // t_NoIntersect = t_postset - t_preset
					Boolean selfLoopFlag = false;
					
					// Check if there is intersection between the preset and postset. 
					for (String x : lhpn.getPreset(t)){
						if (t_postset.contains(x)){
							selfLoopFlag = true;
							t_intersect.add(x);
						}
					}
					
					if (selfLoopFlag) {
						t_NoIntersect.removeAll(t_intersect);
						// t_preset = 0
						for (String x : lhpn.getPreset(t)){
							EventAssignment assign0 = e.createEventAssignment();
							assign0.setVariable(x);
							assign0.setMath(SBML_Editor.myParseFormula("0"));
			//				System.out.println("transition: " + t + " preset: " + x);
						}
								
						// t_NoIntersect  = 1
						for (String x : t_NoIntersect){
							EventAssignment assign1 = e.createEventAssignment();
							assign1.setVariable(x);
							assign1.setMath(SBML_Editor.myParseFormula("1"));
			//				System.out.println("transition: " + t + " postset: " + x);
						}
						
					}
					else {			// no self-loop 
						// t_preSet = 0
						for (String x : lhpn.getPreset(t)){
							EventAssignment assign0 = e.createEventAssignment();
							assign0.setVariable(x);
							assign0.setMath(SBML_Editor.myParseFormula("0"));
			//				System.out.println("transition: " + t + " preset: " + x);
						}
								
						// t_postSet = 1
						for (String x : lhpn.getPostset(t)){
							EventAssignment assign1 = e.createEventAssignment();
							assign1.setVariable(x);
							assign1.setMath(SBML_Editor.myParseFormula("1"));
			//				System.out.println("transition: " + t + " postset: " + x);
						}
					}

					// assignment <A>
					// continuous assignment
					if (lhpn.getContVars(t) != null){
						for (String var : lhpn.getContVars(t)){
							if (lhpn.getContAssign(t, var) != null) {
								ExprTree assignContTree = lhpn.getContAssignTree(t, var);	
								String assignCont = assignContTree.toString("SBML");
//								System.out.println("continuous assign: "+ assignCont);
								EventAssignment assign2 = e.createEventAssignment();
								assign2.setVariable(var);
								assign2.setMath(SBML_Editor.myParseFormula(assignCont));
							}
						}
					}
			
					// integer assignment
					if (lhpn.getIntVars()!= null){
						for (String var : lhpn.getIntVars()){
							if (lhpn.getIntAssign(t, var) != null) {
								ExprTree assignIntTree = lhpn.getIntAssignTree(t, var);
								String assignInt = assignIntTree.toString("SBML");
							  //System.out.println("integer assignment from LHPN: " + var + " := " + assignInt);
								EventAssignment assign3 = e.createEventAssignment();
								assign3.setVariable(var);
								assign3.setMath(SBML_Editor.myParseFormula(assignInt));
							}
						}
					}
					
					// boolean assignment
					if (selfLoopFlag) {
						// if self-loop exists, create a new variable, extraVar, and a new event
						String extraVar = t.concat("_extraVar");
						Parameter p = m.createParameter(); 
						p.setConstant(false);
						p.setId(extraVar);
						
						EventAssignment assign4ex = e.createEventAssignment();
						assign4ex.setVariable(extraVar);
						assign4ex.setMath(SBML_Editor.myParseFormula("1"));
						
						// Create other boolean assignments
						if (lhpn.getBooleanVars(t)!= null){
							for (String var :lhpn.getBooleanVars(t)){
								if (lhpn.getBoolAssign(t, var) != null) {
									ExprTree assignBoolTree = lhpn.getBoolAssignTree(t, var);
									String assignBool_tmp = assignBoolTree.toString("SBML");
									String assignBool = "piecewise(1," + assignBool_tmp + ",0)";
									//System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
									EventAssignment assign4 = e.createEventAssignment();
									assign4.setVariable(var);
									assign4.setMath(SBML_Editor.myParseFormula(assignBool));
								}
							}
						}
						
					   // create a new event 
						Event extraEvent = m.createEvent();
						extraEvent.setId("extraEvent" + counter);	
						Trigger triggerExtra = extraEvent.createTrigger();
						triggerExtra.setMath(SBML_Editor.myParseFormula("and(gt(t,0),eq(" + extraVar + ",1))"));
						extraEvent.setUseValuesFromTriggerTime(false);
						// assignments
						EventAssignment assign5ex1 = extraEvent.createEventAssignment();
						EventAssignment assign5ex2 = extraEvent.createEventAssignment();
						for (String var : t_intersect){
							assign5ex1.setVariable(var);
							assign5ex1.setMath(SBML_Editor.myParseFormula("1"));
						}
						assign5ex2.setVariable(extraVar);
						assign5ex2.setMath(SBML_Editor.myParseFormula("0"));
					}
					else {
						if (lhpn.getBooleanVars(t)!= null){
							for (String var :lhpn.getBooleanVars(t)){
								if (lhpn.getBoolAssign(t, var) != null) {
									ExprTree assignBoolTree = lhpn.getBoolAssignTree(t, var);
									String assignBool_tmp = assignBoolTree.toString("SBML");
									String assignBool = "piecewise(1," + assignBool_tmp + ",0)";
									//System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
									EventAssignment assign4 = e.createEventAssignment();
									assign4.setVariable(var);
									assign4.setMath(SBML_Editor.myParseFormula(assignBool));
								}
							}
						}
					}

					// rate assignment
					if (lhpn.getRateVars(t)!= null){
						for (String var : lhpn.getRateVars(t)){
//							 System.out.println("rate var: "+ var);
							if (lhpn.getRateAssign(t, var) != null) {
								ExprTree assignRateTree = lhpn.getRateAssignTree(t, var);
								String assignRate = assignRateTree.toString("SBML");
		//						System.out.println("rate assign: "+ assignRate);
								
								EventAssignment assign5 = e.createEventAssignment();
								assign5.setVariable(var + "_dot");
								assign5.setMath(SBML_Editor.myParseFormula(assignRate));
							}
						}
					}
					
				}
			counter --;
		}
		
		//filename.replace(".lpn", ".xml");
		//SBMLWriter writer = new SBMLWriter();
		//writer.writeSBML(document, filename);
	}
	
	private void createFunction(Model model, String id, String name, String formula) {
	if (document.getModel().getFunctionDefinition(id) == null) {
		FunctionDefinition f = model.createFunctionDefinition();
		f.setId(id);
		f.setName(name);
		f.setMath(libsbml.parseFormula(formula));
	}
}

	public void outputSBML() {
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBML(document, filename);
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}
}

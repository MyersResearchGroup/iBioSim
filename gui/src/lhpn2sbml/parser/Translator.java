package lhpn2sbml.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

//import org.apache.batik.svggen.font.table.Program;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Constraint;
//import org.sbml.libsbml.Delay;
import org.sbml.libsbml.AssignmentRule;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
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
	
	public void BuildTemplate(String lhpnFilename, String property) {
		this.filename = lhpnFilename.replace(".lpn", ".xml");
		// load lhpn file
		LhpnFile lhpn = new LhpnFile();
		lhpn.load(lhpnFilename);
		
		// create sbml file
		//document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		document = new SBMLDocument(3,1);
		Model m = document.createModel();
		Compartment c = m.createCompartment();
		m.setId(filename.replace(".xml", ""));
		c.setId("default");
		c.setSize(1.0);
		c.setConstant(true);
		c.setSpatialDimensions(3);
		
		// Create bitwise operators for sbml
		createFunction(m, "rate", "Rate", "lambda(a,a)");
		createFunction(m, "BITAND", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITOR", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITNOT", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITXOR", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "mod", "Modular", "lambda(a,b,a-floor(a/b)*b)");
		createFunction(m, "and", "Logical AND", "lambda(a,b,a*b)");
		createFunction(m, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
		createFunction(m, "normal", "Normal distribution", "lambda(m,s,m)");
		createFunction(m, "exponential", "Exponential distribution", "lambda(l,1/l)");
		createFunction(m, "gamma", "Gamma distribution", "lambda(a,b,a*b)");
		createFunction(m, "lognormal", "Lognormal distribution", "lambda(z,s,exp(z+s^2/2))");
		createFunction(m, "chisq", "Chi-squared distribution", "lambda(nu,nu)");
		createFunction(m, "laplace", "Laplace distribution", "lambda(a,0)");
		createFunction(m, "cauchy", "Cauchy distribution", "lambda(a,a)");
		createFunction(m, "rayleigh", "Rayleigh distribution", "lambda(s,s*sqrt(pi/2))");
		createFunction(m, "poisson", "Poisson distribution", "lambda(mu,mu)");
		createFunction(m, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
		createFunction(m, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
		//createFunction(m, "priority", "Priority expressions", "lambda(d,p,d)");
		
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
					
					//test En(t)
					String EnablingTestNull = lhpn.getTransition(t).getEnabling();
					String Enabling;
					String EnablingBool = null;
					if (EnablingTestNull == null){
						EnablingBool = "true";
						Enabling = "1"; // Enabling is true
					}
					else {
						EnablingBool = lhpn.getEnablingTree(t).getElement("SBML");
						Enabling = "piecewise(1, " + EnablingBool + ", 0)";
					}
					
					//test Preset(t)
					String CheckPreset = null;
					int indexPreset = 0;
					
					// Check if all the presets of transition t are marked
					// Transition t can fire only when all its preset are marked.
					for (String x:lhpn.getPreset(t)){
						if (indexPreset == 0){
							CheckPreset = "eq(" + x + ",1)";
						}
						else {
							CheckPreset = "and(" + CheckPreset + "," + "eq(" + x + ",1)" + ")";
						}	
						indexPreset ++;
					}
				
					String modifierStr = "";
					String reactantStr = "";
					for (String x : lhpn.getPreset(t)){
						// Is transition persistent?
						if (lhpn.getTransition(t).isPersistent()){
							// transition is persistent
							// Create a rule for the persistent transition t. 
							AssignmentRule rulePersis = m.createAssignmentRule();
							String rulePersisSpeciesStr = "rPersis_" + t + x;
							// Create a parameter (id = rulePersisTriggName). 
							Species rulePersisSpecies = m.createSpecies();
							rulePersisSpecies.setId(rulePersisSpeciesStr);
							rulePersisSpecies.setCompartment("default");
							rulePersisSpecies.setBoundaryCondition(false);
							rulePersisSpecies.setConstant(false);
							rulePersisSpecies.setHasOnlySubstanceUnits(false);
							rulePersisSpecies.setUnits("");
							
							String ruleExpBool = "or(and(" + CheckPreset + "," + EnablingBool + "), and(" + CheckPreset + "," + "eq(" + rulePersisSpeciesStr + ", 1)" +"))";
							String ruleExpReal = "piecewise(1, " + ruleExpBool + ", 0)";
							rulePersis.setVariable(rulePersisSpeciesStr);
							double ruleVal = rulePersis.setMath(SBML_Editor.myParseFormula(ruleExpReal));
							rulePersisSpecies.setInitialAmount(0);
							ModifierSpeciesReference modifier = r.createModifier();
							modifier.setSpecies(rulePersisSpeciesStr);
							// create the part of Kinetic law expression involving modifiers 
							modifierStr = modifierStr + modifier.getSpecies().toString() + "*";
						}
						else {
							// get preset(s) of a transition and set each as a reactant
							SpeciesReference reactant = r.createReactant();
							reactant.setSpecies(x);
							reactant.setStoichiometry(1.0);
							reactantStr =  reactantStr + reactant.getSpecies().toString() + "*";
						}
					}

					
					SpeciesReference product  = r.createProduct();
					product.setSpecies(t);
					product.setStoichiometry(1.0);
						
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
					if (lhpn.getTransition(t).isPersistent())
						rateReaction.setFormula("(" + modifierStr + Enabling + "*" + lhpn.getTransitionRateTree(t).getElement("SBML") + ")"); 
					else
						rateReaction.setFormula("(" + reactantStr + Enabling + "*" + lhpn.getTransitionRateTree(t).getElement("SBML") + ")"); 
					
					Event e = m.createEvent();
					e.setId("event" + counter);		
					Trigger trigger = e.createTrigger();
					trigger.setMath(SBML_Editor.myParseFormula("eq(" + product.getSpecies() + ",1)"));
					// For persistent transition, it does not matter whether the trigger is persistent or not, because the delay is set to 0. 
					trigger.setPersistent(false);
					e.setUseValuesFromTriggerTime(false);
					trigger.setInitialValue(false);
				
					// t_postSet = 1
					for (String x : lhpn.getPostset(t)){
						EventAssignment assign0 = e.createEventAssignment();
						assign0.setVariable(x);
						assign0.setMath(SBML_Editor.myParseFormula("1"));
		//				System.out.println("transition: " + t + " postset: " + x);
					}
					
					// product = 0
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
				
				else {			// Transition rate = null. Only use event. Transitions only have ranges
//					System.out.println("Event Only");
					Event e = m.createEvent();
					e.setId("event" + counter);	
					Trigger trigger = e.createTrigger();

					//trigger = CheckPreset(t) && En(t);
					//test En(t)
					String EnablingTestNull = lhpn.getTransition(t).getEnabling();
					String Enabling;
					if (EnablingTestNull == null){
						Enabling = "true"; // Enabling is true (Boolean)
					}
					else {
						Enabling = lhpn.getEnablingTree(t).getElement("SBML");
					}
//					System.out.println("Enabling = " + Enabling);
					
					//test Preset(t)
					String CheckPreset = null;
					int indexPreset = 0;
					
					// Check if all the presets of transition t are marked
					// Transition t can fire only when all its preset are marked.
					for (String x:lhpn.getPreset(t)){
						if (indexPreset == 0){
							CheckPreset = "eq(" + x + ",1)";
						}
						else {
							CheckPreset = "and(" + CheckPreset + "," + "eq(" + x + ",1)" + ")";
						}	
						indexPreset ++;
					}
					
					// Is transition persistent?
					if (!lhpn.getTransition(t).isPersistent() || (lhpn.getTransition(t).isPersistent() && !lhpn.getTransition(t).hasConflictSet())){
						if (!lhpn.getTransition(t).isPersistent()) {
							trigger.setPersistent(false);
						}
						else {
							trigger.setPersistent(true);
						}
						
						trigger.setMath(SBML_Editor.myParseFormula("and(" + CheckPreset + "," + Enabling + ")"));
					}
					else { // transition is persistent
						// Create a rule for the persistent transition t. 
						AssignmentRule rulePersisTrigg = m.createAssignmentRule();
						String rulePersisTriggName = "trigg_" + t;
						// Create a parameter (id = rulePersisTriggName). 
						Parameter rulePersisParam = m.createParameter();
						rulePersisParam.setId(rulePersisTriggName);
						rulePersisParam.setValue(0);
						rulePersisParam.setConstant(false);
						rulePersisParam.setUnits("");
						String ruleExpBool = "or(and(" + CheckPreset + "," + Enabling + "), and(" + CheckPreset + "," + "eq(" + rulePersisTriggName + ", 1)" +"))";
						String ruleExpReal = "piecewise(1, " + ruleExpBool + ", 0)";
						rulePersisTrigg.setVariable(rulePersisTriggName);
						rulePersisTrigg.setMath(SBML_Editor.myParseFormula(ruleExpReal));
						trigger.setPersistent(false);
						trigger.setMath(SBML_Editor.myParseFormula("eq(" + rulePersisTriggName + ", 1)"));
					}
										
					// TriggerInitiallyFalse
//					trigger.setAnnotation("<TriggerInitiallyFalse/>");
					trigger.setInitialValue(false);
					
					// use values at trigger time = false
					e.setUseValuesFromTriggerTime(false);
				    					
					// Priority and delay
					if (lhpn.getTransition(t).getDelay()!=null) {
						e.createDelay();
						e.getDelay().setMath(SBML_Editor.myParseFormula(lhpn.getTransition(t).getDelay()));
					}
					if (lhpn.getTransition(t).getPriority()!=null) {
						e.createPriority();
						e.getPriority().setMath(SBML_Editor.myParseFormula(lhpn.getTransition(t).getPriority()));
					}
					/*
					if (lhpn.getTransition(t).getPriority()==null) {
						if (lhpn.getTransition(t).getDelay()!=null) {
							e.createDelay();
							e.getDelay().setMath(SBML_Editor.myParseFormula(lhpn.getTransition(t).getDelay()));
						}
					}
					else {
						if (lhpn.getTransition(t).getDelay()!=null) {
							e.createDelay();
							e.getDelay().setMath(SBML_Editor.myParseFormula("priority(" + lhpn.getTransition(t).getDelay() + "," + lhpn.getTransition(t).getPriority() + ")"));
						} 
						else {
						e.createDelay();
						e.getDelay().setMath(SBML_Editor.myParseFormula("priority(0," + lhpn.getTransition(t).getPriority() + ")"));
						}
					}
					*/
					
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
//							    System.out.println("integer assignment from LHPN: " + var + " := " + assignInt);
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
						p.setValue(0);
						
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
						//triggerExtra.setMath(SBML_Editor.myParseFormula("and(gt(t,0),eq(" + extraVar + ",1))"));
						triggerExtra.setMath(SBML_Editor.myParseFormula("eq(" + extraVar + ",1)"));
						//triggerExtra.setAnnotation("<TriggerInitiallyFalse/>");
						triggerExtra.setPersistent(true);
						triggerExtra.setInitialValue(false);
						extraEvent.setUseValuesFromTriggerTime(false);
						// assignments
						EventAssignment assign5ex2 = extraEvent.createEventAssignment();
						for (String var : t_intersect){
							EventAssignment assign5ex1 = extraEvent.createEventAssignment();
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

		// Property parsing is dealt with in PropertyPanel.java
		// translate the LPN property to SBML constraints
		String probprop = "";
		String[] probpropParts = new String[4];
		if(!(property == null) && !property.equals("")){
			probprop=getProbpropExpression(property);
			probpropParts=getProbpropParts(probprop);
			//System.out.println("probprop=" + probprop);
			//System.out.println("probpropParts[0]=" + probpropParts[0]);
			//System.out.println("probpropParts[1]=" + probpropParts[1]);
			//System.out.println("probpropParts[2]=" + probpropParts[2]);
			//System.out.println("probpropParts[3]=" + probpropParts[3]);
			
			
			// Convert extrated property parts into SBML constraints
			// probpropParts=[probpropLeft, probpropRight, lowerBound, upperBound]
			Constraint constraintFail = m.createConstraint();	
			Constraint constraintSucc = m.createConstraint();
			ExprTree probpropLeftTree = String2ExprTree(lhpn, probpropParts[0]);
			String probpropLeftSBML = probpropLeftTree.toString("SBML");
				
			ExprTree probpropRightTree = String2ExprTree(lhpn, probpropParts[1]);
			String probpropRightSBML = probpropRightTree.toString("SBML");
				
			ExprTree lowerBoundTree = String2ExprTree(lhpn, probpropParts[2]);
			String lowerBoundSBML = lowerBoundTree.toString("SBML");
			String lowerConstraint = "leq(t," + lowerBoundSBML + ")";
				
			ExprTree upperBoundTree = String2ExprTree(lhpn, probpropParts[3]);
			String upperBoundSBML = upperBoundTree.toString("SBML");
			String upperConstraint = "leq(t," + upperBoundSBML + ")";
			    
			if (property.contains("PU")){   
				// construct the SBML constraints
				constraintFail.setMetaId("Fail");
				constraintFail.setMath(SBML_Editor.myParseFormula("and(" + probpropLeftSBML + "," + upperConstraint + ")"));
				constraintSucc.setMetaId("Success");
				constraintSucc.setMath(SBML_Editor.myParseFormula("not(" + probpropRightSBML + ")"));		    
			}
			if (property.contains("PF")){
				constraintFail.setMetaId("Fail");
				constraintFail.setMath(SBML_Editor.myParseFormula(upperConstraint));
				constraintSucc.setMetaId("Success");
				constraintSucc.setMath(SBML_Editor.myParseFormula("not(" + probpropRightSBML + ")"));
			}
			if (property.contains("PG")){
				constraintFail.setMetaId("Fail");
				constraintFail.setMath(SBML_Editor.myParseFormula(probpropRightSBML));
				constraintSucc.setMetaId("Success");
				constraintSucc.setMath(SBML_Editor.myParseFormula(upperConstraint));
			}
		}

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
	
	// getProbpropExpression strips off the "Pr"("St"), relop and REAL parts of a property
	public static String getProbpropExpression(String property){
		System.out.println("property (getProbpropExpression)= " + property);
		String probprop="";
		// probproperty 
		if (property.startsWith("Pr") | property.startsWith("St")){
			// remove Pr/St from the property spec
			property=property.substring(2);
			boolean relopFlag = property.startsWith(">")
								| property.startsWith(">=")
								| property.startsWith("<")
								| property.startsWith("<=")
								| (property.startsWith("=") && !property.contains("?"));
			if (relopFlag){
				if(property.startsWith(">=") | property.startsWith("<=")){
					property=property.substring(2);
				}
				else{
					property=property.substring(1);
				}
				// check the probability value after relop
				String probabilityValue = property.substring(0,property.indexOf("{"));
				Pattern ProbabilityValuePattern = Pattern.compile(probabilityValue);
				Matcher ProbabilityValueMatcher = ProbabilityValuePattern.matcher(probabilityValue);
				boolean correctProbabilityValue = ProbabilityValueMatcher.matches();
				if(correctProbabilityValue) {
					property=property.replaceFirst(probabilityValue, "");
					property=property.replace("{", "");
					property=property.replace("}", "");
					probprop=property;
				}
				else{
					JOptionPane.showMessageDialog(BioSim.frame, "Invalid probability value",
							"Error in Property", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if(property.startsWith("=") && property.contains("?")){
				property=property.substring(3);
				property=property.replace("}", "");
				probprop=property;
			}
		}
		else { // hsf
			return "";
			
		}		
		return probprop;
	}
	
	// getProbpropParts extracts the expressions before and after the PU (after PG and PF)
	// and time bound from the probprop
	// Currently, we assume no nested until property
	public static String[] getProbpropParts(String probprop){
		String symbol = "@";
		String[] probpropParts;
		probpropParts = new String[4];
		boolean PUFlag = probprop.contains("PU");
		boolean PFFlag = probprop.contains("PF");
		boolean PGFlag = probprop.contains("PG");
		String probpropRight="";
		String probpropLeft="";
		String timeBound="";
		String upperBound="";
		String lowerBound="";
		if (!probprop.equals("")){
			// property should be in this format at this stage: probprop
			// obtain the hsf AFTER bound
			probpropRight= probprop.substring(probprop.indexOf("]")+1, probprop.length());			
			// obtain the time bound
			timeBound= probprop.substring(probprop.indexOf("["), probprop.indexOf("]")+1);							 
			// bound: [<= upper]
			if(timeBound.contains("<=")){
				// upper bound
				upperBound = timeBound.substring(timeBound.indexOf("<")+2, timeBound.indexOf("]"));			    
				if(PUFlag){
					probprop = probprop.replace("PU",symbol);
					// obtain the logic BEFORE the temporal operator
					probpropLeft= probprop.substring(0, probprop.indexOf(symbol));
					// if probpropLeft has a pair of outermost parentheses, remove them
					if (probpropLeft.startsWith("(") & probpropLeft.endsWith(")")){
						probpropLeft=probprop.substring(1,probpropLeft.length()-1);
					}
				}
				if(PFFlag){
					probprop = probprop.replace("PF",symbol);
					// Remove the outermost parentheses
					if (probprop.startsWith("(") & probpropLeft.endsWith(")")){
						probprop=probprop.substring(1,probpropLeft.length()-1);
					}	
				}
				if(PGFlag){
					probprop = probprop.replace("PGs",symbol);
					// Remove the outermost parentheses
					if (probprop.startsWith("(") & probpropLeft.endsWith(")")){
						probprop=probprop.substring(1,probpropLeft.length()-1);
					}
				}			
			}
			// bound: [lower, upper]
			else if (timeBound.contains(",")){ 
				// lower bound
				lowerBound = timeBound.substring(timeBound.indexOf("[")+1, timeBound.indexOf(","));
				// upper bound
				upperBound = timeBound.substring(timeBound.indexOf(",")+1, timeBound.indexOf("]"));		 						
				if(PUFlag){
						probprop = probprop.replace("PU",symbol);
						// obtain the logic BEFORE the temporal operator
						probpropLeft= probprop.substring(0, probprop.indexOf(symbol));
						// if probpropLeft has a pair of outermost parentheses, remove them
						if (probpropLeft.startsWith("(") & probpropLeft.endsWith(")")){
							probpropLeft=probprop.substring(1,probpropLeft.length()-1);
						}
				}
			} 
		}
		else { // hsfFlag = true	
			JOptionPane.showMessageDialog(BioSim.frame, "Property does not contain the until operator",
					"Warning in Property", JOptionPane.WARNING_MESSAGE);
		}
		probpropParts[0]=probpropLeft;
		probpropParts[1]=probpropRight;
		probpropParts[2]=lowerBound;
		probpropParts[3]=upperBound;
		return probpropParts;
	}
	
	public ExprTree String2ExprTree(LhpnFile lhpn, String str) {
		boolean retVal;
		ExprTree result = new ExprTree(lhpn);
		ExprTree expr = new ExprTree(lhpn);
		expr.token = expr.intexpr_gettok(str);
		retVal = expr.intexpr_L(str);
		if (retVal) {
			result = expr;
		}
		return result;
	}
	
	private static final String probabilityValue = "(0\\.[0-9]+)";
}
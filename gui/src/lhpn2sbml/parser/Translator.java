package lhpn2sbml.parser;

import gcm2sbml.network.Promoter;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.Utility;
import gcm2sbml.visitor.AbstractPrintVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import junit.framework.TestCase;

import org.omg.CORBA.PRIVATE_MEMBER;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOfParameters;
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
import sun.font.CreatedFontTracker;
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
		LHPNFile lhpn = new LHPNFile();
		lhpn.load(lhpnFilename);
		
		// create sbml file
		document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		Model m = document.createModel();
		Compartment c = m.createCompartment();
		c.setId("default");
		c.setSize(1.0);
		c.setConstant(true);
		c.setSpatialDimensions(3);
	//	lhpn.getEnablingTree(var)
		
		// Create bitwise operators for sbml
		createFunction(m, "BITAND", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITOR", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITNOT", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "BITXOR", "Bitwise AND", "lambda(a,b,a*b)");
		createFunction(m, "MOD", "Bitwise AND", "lambda(a,b,a-floor(a/b)*b)");
		
		// translate from lhpn to sbml
		// ----variables -> parameters-----
		for (String v: lhpn.getAllVariables()){
			if (v != null){
				String initVal = lhpn.getInitialVal(v);
				//System.out.println("Begin:" + v + "= " + initVal);
				if (lhpn.isContinuous(v)){
					Parameter p = m.createParameter(); 
					p.setConstant(false);
					p.setId(v);
					
					Parameter p_dot = m.createParameter();
					p_dot.setConstant(false);
					p_dot.setId(v + "_dot");
					System.out.println("v_dot = " + v + "_dot");
					RateRule rateRule = m.createRateRule();
					rateRule.setVariable(v);
					rateRule.setMath(SBML_Editor.myParseFormula(v + "_dot"));
//					boolean error = checkRateRuleUnits(rateRule);
				
					
					String initValue = lhpn.getInitialVal(v);
					String initValDot= lhpn.getInitialRate(v);
					//System.out.println("initValue = " + initValue);
					//System.out.println("initValDot = " + initValDot);
					//System.out.println(v + "=" + initValue);
					double initVal_dbl = Double.parseDouble(initValue);
					p.setValue(initVal_dbl);
					double initValDot_dbl = Double.parseDouble(initValDot);
					p_dot.setValue(initValDot_dbl);
					}
				else
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
					else
					{	
					
						// TODO variable to param mapping for int and continous vars
						double initVal_dbl = Double.parseDouble(initValue);
						p.setValue(initVal_dbl);
//						System.out.println(Double.parseDouble("3"));
					}
				}
			}
		}
				
		// ----places -> species-----	
		for (String p: lhpn.getPlaceList()){
			
			Boolean initMarking = lhpn.getPlaceInitial(p);
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
		// if transition rate is null, use reaction and event;
		// else use event only
		if (lhpn.getTransitionRates() != null){
			int counter = lhpn.getTransitionList().length - 1;
			for (String t : lhpn.getTransitionList()) {
				//System.out.println(s);
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
				System.out.println("transition rate = " + lhpn.getTransitionRate(t));
				//double tRate = Double.parseDouble(lhpn.getTransitionRate(t));	// need to
				
				//lhpn.getTransitionRateTree(t)
				
				//p_local.setValue(tRate);
				// create exp for KineticLaw
				String exp = lhpn.getEnabling(t);
				if (exp == null) {
					exp = "1";
				} else if (exp.startsWith("~")){
					exp = "(1 - " + exp.substring(1) + ")";
				}
				rateReaction.setFormula("(" + lhpn.getTransitionRate(t) + ")" + "*" + reactant.getSpecies() + "*" + exp); 
				//System.out.println("trans " + t + " enableCond " + lhpn.getEnabling(t));
				
				Event e = m.createEvent();
				e.setId("event" + counter);			
				Trigger trigger = e.createTrigger();
				trigger.setMath(SBML_Editor.myParseFormula("eq(" + product.getSpecies() + ",1)"));
				// t_postSet = 1
				EventAssignment assign0 = e.createEventAssignment();
				for (String x : lhpn.getPostset(t)){
					assign0.setVariable(x);
					assign0.setMath(SBML_Editor.myParseFormula("1"));
	//				System.out.println("transition: " + t + " postset: " + x);
				}
				// t = 0
				EventAssignment assign1 = e.createEventAssignment();
				assign1.setVariable(product.getSpecies());
				assign1.setMath(SBML_Editor.myParseFormula("0"));
				
				// assignment <A>
				// TODO need to test the continuous assignment
				if (lhpn.getContAssignVars(t) != null){
					for (String var : lhpn.getContAssignVars(t)){
						if (lhpn.getContAssign(t, var) != null) {
							String assignCont = lhpn.getContAssign(t, var);
							System.out.println("continuous assign: "+ assignCont);
							EventAssignment assign2 = e.createEventAssignment();
							assign2.setVariable(var);
							assign2.setMath(SBML_Editor.myParseFormula(assignCont));
						}
					}
				}
				
				// TODO need to test the integer assignment
				if (lhpn.getIntVars()!= null){
					for (String var : lhpn.getIntVars()){
						if (lhpn.getIntAssign(t, var) != null) {
							ExprTree[] assignIntTree = lhpn.getIntAssignTree(t, var);
							String assignInt = assignIntTree[0].toString("SBML");
							System.out.println("integer assignment from LHPN: " + var + " := " + assignInt);
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
							ExprTree[] assignBoolTree = lhpn.getBoolAssignTree(t, var);
							String assignBool = assignBoolTree[0].toString("SBML");
							System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
							EventAssignment assign4 = e.createEventAssignment();
							assign4.setVariable(var);
							if (assignBool.equals("true")){
								assign4.setMath(SBML_Editor.myParseFormula("1"));
							}
							if(assignBool.equals("false")){
								assign4.setMath(SBML_Editor.myParseFormula("0"));
							}
						}
					}
				}
				
				// rate assignment
				if (lhpn.getRateVars(t)!= null){
					for (String var : lhpn.getRateVars(t)){
						 System.out.println("rate var: "+ var);
						if (lhpn.getRateAssign(t, var) != null) {
							String assignRate = lhpn.getRateAssign(t, var);
	//						System.out.println("rate assign: "+ assignRate);
							EventAssignment assign5 = e.createEventAssignment();
							assign5.setVariable(var + "_dot");
							assign5.setMath(SBML_Editor.myParseFormula(assignRate));
						}
					}
				}
				// The formula in function does not represent the actual function. 
				counter --;
			}
		}
		 //Only use event
		else {
				int counter = lhpn.getTransitionList().length - 1;
				for (String t : lhpn.getTransitionList()) {
					Event e = m.createEvent();
					e.setId("event" + counter);			
					Trigger trigger = e.createTrigger();
					// TODO write correct expression for trigger
//					trigger = lhpn.getPreset(t) && En(t);
//					trigger.setMath(SBML_Editor.myParseFormula("eq(" + product.getSpecies() + ",1)"));
					
					// triggerCanBeDisabled := true
					
					// Delay D(t)
					
					// t_postSet = 1
					EventAssignment assign0 = e.createEventAssignment();
					for (String x : lhpn.getPostset(t)){
						assign0.setVariable(x);
						assign0.setMath(SBML_Editor.myParseFormula("1"));
		//				System.out.println("transition: " + t + " postset: " + x);
					}
					// t = 0
					EventAssignment assign1 = e.createEventAssignment();
					//assign1.setVariable(product.getSpecies());
					assign1.setMath(SBML_Editor.myParseFormula("0"));
					
					// assignment <A>
					// TODO need to test the continuous assignment
					if (lhpn.getContAssignVars(t) != null){
						for (String var : lhpn.getContAssignVars(t)){
							if (lhpn.getContAssign(t, var) != null) {
								String assignCont = lhpn.getContAssign(t, var);
								System.out.println("continuous assign: "+ assignCont);
								EventAssignment assign2 = e.createEventAssignment();
								assign2.setVariable(var);
								assign2.setMath(SBML_Editor.myParseFormula(assignCont));
							}
						}
					}
					
					// TODO need to test the integer assignment
					if (lhpn.getIntVars()!= null){
						for (String var : lhpn.getIntVars()){
							if (lhpn.getIntAssign(t, var) != null) {
								String assignInt = lhpn.getIntAssign(t, var);
								System.out.println("integer assignment from LHPN: " + var + " := " + assignInt);
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
								String assignBool = lhpn.getBoolAssign(t, var);
								System.out.println("boolean assignment from LHPN: " + var + " := " + assignBool);
								EventAssignment assign4 = e.createEventAssignment();
								assign4.setVariable(var);
								if (assignBool.equals("true")){
									assign4.setMath(SBML_Editor.myParseFormula("1"));
								}
								if(assignBool.equals("false")){
									assign4.setMath(SBML_Editor.myParseFormula("0"));
								}
							}
						}
					}
					
					// rate assignment
					if (lhpn.getRateVars(t)!= null){
						for (String var : lhpn.getRateVars(t)){
							 System.out.println("rate var: "+ var);
							if (lhpn.getRateAssign(t, var) != null) {
								String assignRate = lhpn.getRateAssign(t, var);
		//						System.out.println("rate assign: "+ assignRate);
								EventAssignment assign5 = e.createEventAssignment();
								assign5.setVariable(var + "_dot");
								assign5.setMath(SBML_Editor.myParseFormula(assignRate));
							}
						}
					}
					// The formula in function does not represent the actual function. 
					counter --;
				}
			}

		
		//filename.replace(".lpn", ".xml");
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBML(document, filename);
	}
	
	private void createFunction(Model model, String id, String name, String formula) {
	if (document.getModel().getFunctionDefinition(id) == null) {
		FunctionDefinition f = model.createFunctionDefinition();
		f.setId(id);
		f.setName(name);
		f.setMath(libsbml.parseFormula(formula));
	}
}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}
	
//	public String toString() {
//		String result = "";
//		//result = getElement("LHPN");
//		result = ExprTree.getElement("SBML");
//		return result;
//	}
}


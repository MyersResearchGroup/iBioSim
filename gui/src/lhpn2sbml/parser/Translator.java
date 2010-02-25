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
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Trigger;
import org.sbml.libsbml.libsbml;

import sbmleditor.SBML_Editor;

import biomodelsim.BioSim;

/**
 * This class converts a lph file to a sbml file
 * 
 * @author Zhen
 * 
 */
public class Translator {
	private String filename;
	
	
	public Translator() {
		setFilename("req_ack.lpn");
	}
	
	public void BuildTemplate(String lhpnFilename) {
		this.filename = lhpnFilename.replace(".lpn", ".xml");
	
		// load lhpn file
		LHPNFile lhpn = new LHPNFile();
		lhpn.load(lhpnFilename);
		
		// create sbml file
		SBMLDocument document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		Model m = document.createModel();
		Compartment c = m.createCompartment();
		c.setId("default");
		c.setSize(1.0);
		c.setConstant(true);
		c.setSpatialDimensions(3);
		
		
		// translate from lhpn to sbml
		// ----variables -> parameters-----
		for (String v: lhpn.getAllVariables()){
			if (v != null){
				//System.out.println(s);
				Parameter p = m.createParameter(); 
				p.setConstant(false);
				p.setId(v);
				String initValue = lhpn.getInitialVal(v);
				//System.out.println(v + "=" + initValue);
				
				// check initValue type; if boolean, set parameter value as 0 or 1.
				if (initValue.equals("true")){
					p.setValue(1);
				}
				else if (initValue.equals("false")){
					p.setValue(0);
				}
				else
				{
					// TODO need to create other variable types to test this case
					double initVal_dbl = Double.parseDouble(initValue);
					p.setValue(initVal_dbl);
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
			Parameter p_local = rateReaction.createParameter();
			p_local.setConstant(false);
			p_local.setId("rate" + counter);
			p_local.setValue(0.7);
			
			// TODO add req/ack to the Kinetic law formula
			rateReaction.setFormula(p_local.getId() + "*" + reactant.getSpecies() + "*" + lhpn.getEnabling(t));
			//System.out.println("trans " + t + " enableCond " + lhpn.getEnabling(t));
	

			Event e = m.createEvent();
			e.setId("event" + counter);			
			Trigger trigger = e.createTrigger();
			trigger.setMath(SBML_Editor.myParseFormula("eq(" + product.getSpecies() + ",1)"));
			EventAssignment assign = e.createEventAssignment();
			assign.setVariable(product.getSpecies());
			assign.setMath(SBML_Editor.myParseFormula("0"));
			
			
			
	
			
			
//			org.sbml.libsbml.Event e = m.createEvent();
//			e.createTrigger();
			counter --;
		}
		//filename.replace(".lpn", ".xml");
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBML(document, filename);
		


	}
	
	
	public void loadLhpn(String filename) {
		LHPNFile lhpn = new LHPNFile();
		LHPNFile foo = new LHPNFile();
		lhpn = foo;
		lhpn.load(filename);
		String[] transitionList = lhpn.getTransitionList();
		String[] places = lhpn.getPlaceList();
		String[] continuous = lhpn.getContVars();
		String[] integers = lhpn.getIntVars();
		String[] booleans = lhpn.getBooleanVars();
		for (String p : places) {
			Boolean initMarking = lhpn.getPlaceInitial(p);
		}
		for (String v : continuous) {
			String initValue = lhpn.getInitialVal(v);
			String initRate = lhpn.getInitialRate(v);
		}
		for (String v : integers) {
			String initValue = lhpn.getInitialVal(v);
		}
		for (String v : booleans) {
			String initValue = lhpn.getInitialVal(v);
		}
		for (String t : transitionList) {
			for (String v : continuous) {
				if (lhpn.getContAssign(t, v) != null) {
					String value = lhpn.getContAssign(t, v);
				}
			}
			for (String v : integers) {
				if (lhpn.getIntAssign(t, v) != null) {
					String value = lhpn.getIntAssign(t, v);
				}
			}

			for (String v : booleans) {
				if (lhpn.getBoolAssign(t, v) != null) {
					String value = lhpn.getBoolAssign(t, v);
				}
			}
		}
		for (String t : transitionList) {
			String enabling = lhpn.getEnabling(t);
			String delay = lhpn.getDelay(t);
		}
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}
	
	
	
}


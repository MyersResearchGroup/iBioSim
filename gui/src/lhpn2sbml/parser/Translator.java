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
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;

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
			System.out.println(p + "=" + initMarking);
			
		}
		
//		for (String s : lhpn.getTransitionList()) {
//			System.out.println(s);
//		}
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


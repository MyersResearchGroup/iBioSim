package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.Port;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;

/**
 * Test SBML properties with evenzeroes example to ensure flattening method produces valid results.
 * 
 * @author Tramy Nguyen 
 */
public class FlatteningSBML_Test {
	
	private static Model imp_model; 
	private static Model tb_model; 
	private static Model flat_model;
	
	@BeforeClass
	public static void setupTest() throws BioSimException {
		try {
			String implement = TestingFiles.outputDir + File.separator + "evenzeroes_imp.xml";
			String testbench = TestingFiles.outputDir + File.separator + "evenzeroes_testbench.xml";
			SBMLDocument imp_doc = SBMLReader.read(new File(implement));
			SBMLDocument tb_doc = SBMLReader.read(new File(testbench));
			BioModel sbmlDoc = new BioModel(TestingFiles.outputDir); 
			
			//Loading the testbench file will also load the implementation file as well since externalModelDefinition is used.
			boolean isDocumentLoaded = sbmlDoc.load(testbench);
			if(!isDocumentLoaded) {
				throw new BioSimException("Unable to perform flattening for the following SBML file " + testbench, "Error Flattening SBML Files");
			}
			SBMLDocument flat_doc = sbmlDoc.flattenModel(true);
			
			imp_model = imp_doc.getModel();
			tb_model = tb_doc.getModel();
			flat_model = flat_doc.getModel();
		
		} 
		catch (XMLStreamException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	@Test
	public void Test_impEventSize() {
		Assert.assertEquals(29, imp_model.getNumEvents());
	}
	
	@Test
	public void Test_tbEventSize() {
		Assert.assertEquals(23, tb_model.getNumEvents());
	}
	
	@Test
	public void Test_flatEventSize() {
		Assert.assertEquals(52, flat_model.getNumEvents());
	}

	@Test
	public void Test_impDelaySize() {
		int actual_delaySize = 0;
		for(Event event : imp_model.getListOfEvents()) {
			if(event.isSetDelay()) {
				actual_delaySize++;
			}
		}
		Assert.assertEquals(7, actual_delaySize);
	}
	
	@Test
	public void Test_tbDelaySize() {
		int actual_delaySize = 0;
		for(Event event : tb_model.getListOfEvents()) {
			if(event.isSetDelay()) {
				actual_delaySize++;
			}
		}
		Assert.assertEquals(7, actual_delaySize);
	}

	@Test
	public void Test_flatDelaySize() {
		int actual_delaySize = 0;
		for(Event event : flat_model.getListOfEvents()) {
			if(event.isSetDelay()) {
				actual_delaySize++;
			}
		}
		Assert.assertEquals(14, actual_delaySize);
	}
		
	@Test
	public void Test_impDelayType() {
		for(Event e : imp_model.getListOfEvents()){
			if(e.isSetDelay()) {
				Delay actual_delay = e.getDelay();
				Assert.assertEquals(ASTNode.Type.REAL, actual_delay.getMath().getType());
			}
		}
	}

	@Test
	public void Test_tbDelayType() {
		for(Event e : tb_model.getListOfEvents()){
			if(e.isSetDelay()) {
				Delay actual_delay = e.getDelay();
				Assert.assertEquals(ASTNode.Type.REAL, actual_delay.getMath().getType());
			}
		}
	}

	@Test
	public void Test_flatDelayType() {
		for(Event flat_event : flat_model.getListOfEvents()) {
			if(flat_event.isSetDelay()) {
				String flat_id = flat_event.getId();
				Type flat_delayType = flat_event.getDelay().getMath().getType();
				if(flat_id.startsWith("ez_instance__")) {
					Assert.assertEquals(ASTNode.Type.INTEGER, flat_delayType);
					Event imp_event = imp_model.getEvent(flat_id.substring(13));
					Assert.assertNotNull(imp_event);
					Assert.assertTrue(imp_event.isSetDelay());
					Assert.assertEquals(imp_event.getDelay().getMath().getType(), ASTNode.Type.REAL);
				}
				else {
					Assert.assertEquals(ASTNode.Type.REAL, flat_delayType);
					Event tb_event = tb_model.getEvent(flat_id);
					Assert.assertNotNull(tb_event);
					Assert.assertTrue(tb_event.isSetDelay());
					Assert.assertEquals(tb_event.getDelay().getMath().getType(), flat_delayType);
				}
			}
		}
	}

	@Test
	public void Test_impDelayValue() {
		for(Event e : imp_model.getListOfEvents()) {
			if(e.isSetDelay()) {
				Delay actual_delay = e.getDelay();
				Assert.assertTrue(actual_delay.getMath().isReal());
				Assert.assertTrue(5.0 ==  actual_delay.getMath().getReal());
			}
		}
	}

	@Test
	public void Test_tbDelayValue() {
		for(Event e : tb_model.getListOfEvents()) {
			if(e.isSetDelay()) {
				Delay actual_delay = e.getDelay();
				Assert.assertTrue(actual_delay.getMath().isReal());
				Assert.assertTrue(5.0 ==  actual_delay.getMath().getReal());
			}
		}
	}

	@Test
	public void Test_impInitAssignSize() {
		Assert.assertEquals(3, imp_model.getNumInitialAssignments());
	}

	@Test
	public void Test_tbInitAssignSize() {
		Assert.assertEquals(3, tb_model.getNumInitialAssignments());
	}

	@Test
	public void Test_flatInitAssignSize() {
		Assert.assertEquals(6, flat_model.getNumInitialAssignments());
	}

	@Test
	public void Test_impInitAssign() {
		String[] expected_symbols = {"parity0", "parity1", "state"};
		for(int index = 0; index < expected_symbols.length; index++) {
			InitialAssignment actual_initAssign = imp_model.getInitialAssignmentBySymbol(expected_symbols[index]);
			Assert.assertNotNull(actual_initAssign);
			Assert.assertEquals(ASTNode.Type.INTEGER, actual_initAssign.getMath().getType());
			Assert.assertEquals(0, actual_initAssign.getMath().getInteger());
		}
	}

	@Test
	public void Test_tbInitAssign() {
		String[] expected_symbols = {"bit0", "bit1", "next"};
		for(int index = 0; index < expected_symbols.length; index++) {
			InitialAssignment actual_initAssign = tb_model.getInitialAssignmentBySymbol(expected_symbols[index]);
			Assert.assertNotNull(actual_initAssign);
			Assert.assertEquals(ASTNode.Type.INTEGER, actual_initAssign.getMath().getType());
			Assert.assertEquals(0, actual_initAssign.getMath().getInteger());
		}
	}

	@Test
	public void Test_flatInitAssign() {
		String[] expected_symbols = {"parity0", "parity1", "ez_instance__state", "bit0", "bit1", "next"};
		for(int index = 0; index < expected_symbols.length; index++) {
			InitialAssignment actual_initAssign = flat_model.getInitialAssignmentBySymbol(expected_symbols[index]);
			Assert.assertNotNull(actual_initAssign);
			Assert.assertEquals(ASTNode.Type.INTEGER, actual_initAssign.getMath().getType());
			Assert.assertEquals(0, actual_initAssign.getMath().getInteger());
		}
	}
	
	@Test
	public void Test_impPortSize() {
		CompModelPlugin compPlugin = (CompModelPlugin) imp_model.getPlugin("comp");
		Assert.assertEquals(4, compPlugin.getNumPorts());
	}

	@Test
	public void Test_tbPortSize() {
		CompModelPlugin compPlugin = (CompModelPlugin) tb_model.getPlugin("comp");
		Assert.assertEquals(0, compPlugin.getNumPorts());
	}

	@Test
	public void Test_flatPortSize() {
		CompModelPlugin compPlugin = (CompModelPlugin) flat_model.getPlugin("comp");
		Assert.assertEquals(0, compPlugin.getNumPorts());
	}

	@Test
	public void Test_impPort() {
		HashMap<String, String> expected_inPorts = new HashMap<String, String>();
		expected_inPorts.put("evenzeroes_imp__bit0", "bit0");
		expected_inPorts.put("evenzeroes_imp__bit1", "bit1");

		HashMap<String, String> expected_outPorts = new HashMap<String, String>();
		expected_outPorts.put("evenzeroes_imp__parity0", "parity0");
		expected_outPorts.put("evenzeroes_imp__parity1", "parity1");
		
		CompModelPlugin compPlugin = (CompModelPlugin) imp_model.getPlugin("comp");
		
		for(Port actual_port : compPlugin.getListOfPorts()){
			if(actual_port.getSBOTerm() == 600) {
				String expected_val = expected_inPorts.get(actual_port.getId());
				Assert.assertNotNull(expected_val);
				Assert.assertEquals(expected_val, actual_port.getIdRef());
			
			}
			else if(actual_port.getSBOTerm() == 601) {
				String expected_val = expected_outPorts.get(actual_port.getId());
				Assert.assertNotNull(expected_val);
				Assert.assertEquals(expected_val, actual_port.getIdRef());
			}
		}
	}


}
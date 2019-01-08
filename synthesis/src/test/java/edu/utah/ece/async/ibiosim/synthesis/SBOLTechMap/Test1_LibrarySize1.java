package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SystemsBiologyOntology;


/**
 * Perform tech. map with a NOT gate.
 *  
 * @author Tramy Nguyen
 */
public class Test1_LibrarySize1 {
	
	private static SBOLDocument sbolDoc = null;
	
	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-sf", SBOLTechMapTestSuite.NOT_Spec, "-lf", SBOLTechMapTestSuite.NOT2_LibSize1, "-sbol"};
		sbolDoc = SBOLTechMapTestSuite.testEnv.runTechMap(cmd);
	}
	
	
	@Test
	public void Test_cdSize(){
		Assert.assertEquals(3, sbolDoc.getComponentDefinitions().size());
	}
	
	@Test
	public void Test_fcTopLevelSize() {
		assertEquals(2, sbolDoc.getModuleDefinition("TechMapSolution_Circuit", "1.0").getFunctionalComponents().size());
	}
	
	@Test
	public void Test_fcGateSize() {
		assertEquals(3, sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0").getFunctionalComponents().size());
	}
	
	@Test
	public void Test_gateInteractionSize() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		assertEquals(2, notGate.getInteractions().size());
	}

	@Test
	public void Test_gateInputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		Interaction inhibition = notGate.getInteraction("I0_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());
		
		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals("FC1_inputProtein", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals("FC0_tu", p.getParticipant().getDisplayId());
			}
		}
	}
	
	@Test
	public void Test_gateOutputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		Interaction production = notGate.getInteraction("I1_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());
		
		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.PROMOTER)) {
				Assert.assertEquals("FC0_tu", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals("FC2_outputProtein", p.getParticipant().getDisplayId());
			}
		}
	}
	
	@Test
	public void Test_mapsTo() {
		ModuleDefinition fullCircuit = sbolDoc.getModuleDefinition("TechMapSolution_Circuit", "1.0");
		ModuleDefinition gate = sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		
		Module circuit_instance = fullCircuit.getModule("M0_gate");
		for(MapsTo mp : circuit_instance.getMapsTos()) {
			String localId = mp.getLocal().getDisplayId();
			
			if(localId.equals("FC0_FC2_outputProtein")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC2_outputProtein"));
			}
			else if(localId.equals("FC1_FC1_inputProtein")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC1_inputProtein"));
			}
		}
	}

}

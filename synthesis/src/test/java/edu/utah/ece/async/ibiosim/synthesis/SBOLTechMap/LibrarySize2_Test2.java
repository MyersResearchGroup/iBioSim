package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

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
 * Perform tech. map on NOT gate connected in series to NOR gate
 * @author Tramy Nguyen
 */
public class LibrarySize2_Test2 {

	private static SBOLDocument sbolDoc = null;
	
	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-sf", SBOLTechMapTestSuite.NOTNOR_Spec, "-lf", SBOLTechMapTestSuite.NOTNOR_LibSize2, "-sbol"};
		sbolDoc = SBOLTechMapTestSuite.testEnv.runTechMap(cmd);
	}
	
	
	@Test
	public void Test_cdSize(){
		Assert.assertEquals(16, sbolDoc.getComponentDefinitions().size());
	}
	
	@Test
	public void Test_cd(){
		List<String> expectedCD = Arrays.asList("CD0_promoter", "CD1_tu", "CD2_ribosome", "CD3_cds", "CD4_terminator", "CD5_inputProtein", "CD6_outputProtein", 
				"CD7_promoter", "CD8_promoter", "CD9_tu", "CD10_ribosome", "CD11_cds", "CD12_terminator",
				"CD13_inputProtein1", "CD14_inputProtein2", "CD15_outputProtein");	
		for(String expectedId : expectedCD) {
			assertNotNull(sbolDoc.getComponentDefinition(expectedId, "1.0"));
		}
	}
	
	@Test
	public void Test_mdSize() {
		assertEquals(3, sbolDoc.getModuleDefinitions().size());
	}
	
	@Test
	public void Test_md() {
		assertNotNull(sbolDoc.getModuleDefinition("circuit_notNorDesign_solution", "1.0"));
		assertNotNull(sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0"));
		assertNotNull(sbolDoc.getModuleDefinition("MD1_TandemPromoterNORGate", "1.0"));
	}
	
	@Test
	public void Test_fcTopLevelSize() {
		assertEquals(4, sbolDoc.getModuleDefinition("circuit_notNorDesign_solution", "1.0").getFunctionalComponents().size());
	}
	
	@Test
	public void Test_fcGateSize() {
		assertEquals(4, sbolDoc.getModuleDefinition("MD1_TandemPromoterNORGate", "1.0").getFunctionalComponents().size());
	}	
	
	@Test
	public void Test_NOTInputInteraction() {
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
	public void Test_NOTOutputInteraction() {
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
	public void Test_NORInputInteractions() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD1_TandemPromoterNORGate", "1.0");
		Interaction inhibition = notGate.getInteraction("I2_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());
		
		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals("FC4_inputProtein1", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals("FC3_tu", p.getParticipant().getDisplayId());
			}
		}
		
		inhibition = notGate.getInteraction("I3_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());
		
		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals("FC5_inputProtein2", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals("FC3_tu", p.getParticipant().getDisplayId());
			}
		}
	}
	
	@Test
	public void Test_NOROutputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD1_TandemPromoterNORGate", "1.0");
		Interaction production = notGate.getInteraction("I4_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());
		
		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.PROMOTER)) {
				Assert.assertEquals("FC3_tu", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals("FC6_outputProtein", p.getParticipant().getDisplayId());
			}
		}
	}
	
	@Test
	public void Test_NOTMapsTo1() {
		ModuleDefinition fullCircuit = sbolDoc.getModuleDefinition("circuit_notNorDesign_solution", "1.0");
		ModuleDefinition gate = sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		
		Module circuit_instance = fullCircuit.getModule("M0_MD0_NOTGate");
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
	
	@Test
	public void Test_NOTMapsTo2() {
		ModuleDefinition fullCircuit = sbolDoc.getModuleDefinition("circuit_notNorDesign_solution", "1.0");
		ModuleDefinition gate = sbolDoc.getModuleDefinition("MD1_TandemPromoterNORGate", "1.0");
		
		Module circuit_instance = fullCircuit.getModule("M1_MD1_TandemPromoterNORGate");
		for(MapsTo mp : circuit_instance.getMapsTos()) {
			String localId = mp.getLocal().getDisplayId();
			
			if(localId.equals("FC4_inputProtein1")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC2_FC4_inputProtein1"));
			}
			else if(localId.equals("FC5_inputProtein2")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC3_FC5_inputProtein2"));
			}
			else if(localId.equals("FC6_outputProtein")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC4_FC6_outputProtein"));
			}
		}
	}
	
}

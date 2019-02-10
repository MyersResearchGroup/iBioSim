package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SystemsBiologyOntology;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapRunner;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Synthesis;

/**
 * Test SBOL tech. map on spec designed y = a & b. This will map one NOR gate and 2 NOT gates.   
 * @author Tramy Nguyen
 *
 */
public class LibrarySize3_Test1 {
	
	private static SBOLDocument sbolDoc = null;

	@BeforeClass
	public static void setupTest() {
		try {
			SBOLTechMapOptions techMapOptions = new SBOLTechMapOptions();
			techMapOptions.setSpecificationFile(TestingFiles.NORNOT_Spec);
			techMapOptions.setLibraryFile(TestingFiles.NORNOT_LibSize3);

			Synthesis syn = SBOLTechMapRunner.run(techMapOptions.getSpeficationFile(), techMapOptions.getLibraryFile());
			sbolDoc = syn.getSBOLfromTechMapping();
		} 
		catch (SBOLException | SBOLValidationException | IOException | SBOLConversionException | SBOLTechMapException e) {
			e.printStackTrace();
		}
	}


	@Test
	public void Test_cdSize(){
		Assert.assertEquals(23, sbolDoc.getComponentDefinitions().size());
	}
	
	@Test
	public void Test_cd(){
		List<String> expectedCD = Arrays.asList(
				"CD0_promoter", "CD1_tu", "CD2_ribosome", "CD3_cds", "CD4_terminator", "CD5_inputProtein", "CD6_outputProtein", 
				"CD7_promoter", "CD8_tu", "CD9_ribosome", "CD10_cds", "CD11_terminator", "CD12_inputProtein", "CD13_outputProtein", 
				"CD14_promoter", "CD15_promoter", "CD16_tu", "CD17_ribosome", "CD18_cds", "CD19_terminator", "CD20_inputProtein1", "CD21_inputProtein2", "CD22_outputProtein");
		for(String expectedId : expectedCD) {
			assertNotNull(sbolDoc.getComponentDefinition(expectedId, "1.0"));
		}
	
	}
	
	@Test
	public void Test_mdSize() {
		assertEquals(4, sbolDoc.getModuleDefinitions().size());
	}
	
	@Test
	public void Test_md() {
		assertNotNull(sbolDoc.getModuleDefinition("circuit_norNotDesign_solution", "1.0"));
		assertNotNull(sbolDoc.getModuleDefinition("MD2_TandemPromoterNORGate", "1.0"));
		assertNotNull(sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0"));
		assertNotNull(sbolDoc.getModuleDefinition("MD1_NOTGate", "1.0"));
	}
	
	@Test
	public void Test_fcTopLevelSize() {
		assertEquals(5, sbolDoc.getModuleDefinition("circuit_norNotDesign_solution", "1.0").getFunctionalComponents().size());
	}
	
	@Test
	public void Test_fcNORGateSize() {
		assertEquals(4, sbolDoc.getModuleDefinition("MD2_TandemPromoterNORGate", "1.0").getFunctionalComponents().size());
	}

	@Test
	public void Test_fcNOT1GateSize() {
		assertEquals(3, sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0").getFunctionalComponents().size());
	}
	
	@Test
	public void Test_fcNOT2GateSize() {
		assertEquals(3, sbolDoc.getModuleDefinition("MD1_NOTGate", "1.0").getFunctionalComponents().size());
	}

	@Test
	public void Test_fcTopLevel() {
		ModuleDefinition md = sbolDoc.getModuleDefinition("circuit_norNotDesign_solution", "1.0");
		List<String> expectedFCList = Arrays.asList("FC0_FC9_outputProtein", "FC1_FC8_inputProtein2", "FC2_FC1_inputProtein", "FC3_FC7_inputProtein1", "FC4_FC4_inputProtein");
		for(String expectedFc : expectedFCList) {
			FunctionalComponent fc = md.getFunctionalComponent(expectedFc);
			assertNotNull(fc);
			assertEquals(DirectionType.INOUT, fc.getDirection());
		}
	}
	
	@Test
	public void Test_NORInputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD2_TandemPromoterNORGate", "1.0");
		Interaction inhibition = notGate.getInteraction("I4_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());
		
		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals("FC7_inputProtein1", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals("FC6_tu", p.getParticipant().getDisplayId());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
		
		inhibition = notGate.getInteraction("I5_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());
		
		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals("FC8_inputProtein2", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals("FC6_tu", p.getParticipant().getDisplayId());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_NOROutputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD2_TandemPromoterNORGate", "1.0");
		Interaction production = notGate.getInteraction("I6_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());
		
		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals("FC6_tu", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals("FC9_outputProtein", p.getParticipant().getDisplayId());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_NOT1InputInteraction() {
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
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_NOT1OutputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		Interaction production = notGate.getInteraction("I1_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());
		
		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals("FC0_tu", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals("FC2_outputProtein", p.getParticipant().getDisplayId());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_NOT2InputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD1_NOTGate", "1.0");
		Interaction inhibition = notGate.getInteraction("I2_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());
		
		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals("FC4_inputProtein", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals("FC3_tu", p.getParticipant().getDisplayId());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_NOT2OutputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD1_NOTGate", "1.0");
		Interaction production = notGate.getInteraction("I3_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());
		
		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals("FC3_tu", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals("FC5_outputProtein", p.getParticipant().getDisplayId());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_NORMapsTo1() {
		ModuleDefinition fullCircuit = sbolDoc.getModuleDefinition("circuit_norNotDesign_solution", "1.0");
		ModuleDefinition gate = sbolDoc.getModuleDefinition("MD2_TandemPromoterNORGate", "1.0");
		
		Module circuit_instance = fullCircuit.getModule("M0_MD2_TandemPromoterNORGate");
		for(MapsTo mp : circuit_instance.getMapsTos()) {
			String localId = mp.getLocal().getDisplayId();
			
			if(localId.equals("FC0_FC9_outputProtein")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC9_outputProtein"));
			}
			else if(localId.equals("FC1_FC8_inputProtein2")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC8_inputProtein2"));
			}
			else if(localId.equals("FC3_FC7_inputProtein1")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC7_inputProtein1"));
			}
			else {
				Assert.fail("This MapsTo object has an unexpected Local ID: " + localId);
			}
		}
	}
	
	@Test
	public void Test_NOT1MapsTo2() {
		ModuleDefinition fullCircuit = sbolDoc.getModuleDefinition("circuit_norNotDesign_solution", "1.0");
		ModuleDefinition gate = sbolDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		
		Module circuit_instance = fullCircuit.getModule("M1_MD0_NOTGate");
		for(MapsTo mp : circuit_instance.getMapsTos()) {
			String localId = mp.getLocal().getDisplayId();
			
			if(localId.equals("FC1_FC8_inputProtein2")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC2_outputProtein"));
			}
			else if(localId.equals("FC2_FC1_inputProtein")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC1_inputProtein"));
			}
			else {
				Assert.fail("This MapsTo object has an unexpected Local ID: " + localId);
			}
		}
	}
	
	@Test
	public void Test_NOT2MapsTo3() {
		ModuleDefinition fullCircuit = sbolDoc.getModuleDefinition("circuit_norNotDesign_solution", "1.0");
		ModuleDefinition gate = sbolDoc.getModuleDefinition("MD1_NOTGate", "1.0");
		
		Module circuit_instance = fullCircuit.getModule("M2_MD1_NOTGate");
		for(MapsTo mp : circuit_instance.getMapsTos()) {
			String localId = mp.getLocal().getDisplayId();
			
			if(localId.equals("FC3_FC7_inputProtein1")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC5_outputProtein"));
			}
			else if(localId.equals("FC4_FC4_inputProtein")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC4_inputProtein"));
			}
			else {
				Assert.fail("This MapsTo object has an unexpected Local ID: " + localId);
			}
		}
	}
}

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
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SystemsBiologyOntology;

/**
 * Perform tech. map with a NOR gate using tandem promoters. 
 * @author Tramy Nguyen
 */
public class LibrarySize1_Test2 {
	
	private static SBOLDocument sbolDoc = null;

	@BeforeClass
	public static void setupTest() {

		String[] cmd = {"-sf", SBOLTechMapTestSuite.NOR1_LibSize1, "-lf", SBOLTechMapTestSuite.NOR1_LibSize1, "-sbol"};
		sbolDoc = SBOLTechMapTestSuite.testEnv.runTechMap(cmd);
	}


	@Test
	public void Test_cdSize(){
		Assert.assertEquals(9, sbolDoc.getComponentDefinitions().size());
	}

	@Test
	public void Test_cd(){
		List<String> expectedCD = Arrays.asList("CD0_promoter", "CD1_promoter", "CD2_tu", "CD3_ribosome", "CD4_cds", "CD5_terminator",
				"CD6_inputProtein1", "CD7_inputProtein2", "CD8_outputProtein");
		for(String expectedId : expectedCD) {
			assertNotNull(sbolDoc.getComponentDefinition(expectedId, "1.0"));
		}
	}
	
	@Test
	public void Test_mdSize() {
		assertEquals(2, sbolDoc.getModuleDefinitions().size());
	}
	
	@Test
	public void Test_md() {
		assertNotNull(sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate_solution", "1.0"));
		assertNotNull(sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate", "1.0"));
	}
	
	@Test
	public void Test_fcTopLevelSize() {
		assertEquals(3, sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate_solution", "1.0").getFunctionalComponents().size());
	}
	
	@Test
	public void Test_fcGateSize() {
		assertEquals(4, sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate", "1.0").getFunctionalComponents().size());
	}
	
	@Test
	public void Test_gateInteractionSize() {
		ModuleDefinition norGate = sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate", "1.0");
		assertEquals(3, norGate.getInteractions().size());
	}
	
	@Test
	public void Test_gateInputInteractions() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate", "1.0");
		Interaction inhibition = notGate.getInteraction("I0_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());
		
		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals("FC1_inputProtein1", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals("FC0_tu", p.getParticipant().getDisplayId());
			}
		}
		
		inhibition = notGate.getInteraction("I1_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());
		
		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals("FC2_inputProtein2", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals("FC0_tu", p.getParticipant().getDisplayId());
			}
		}
	}
	
	@Test
	public void Test_gateOutputInteraction() {
		ModuleDefinition notGate = sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate", "1.0");
		Interaction production = notGate.getInteraction("I2_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());
		
		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.PROMOTER)) {
				Assert.assertEquals("FC0_tu", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals("FC3_outputProtein", p.getParticipant().getDisplayId());
			}
		}
	}
}

package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLGraph;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMap;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Synthesis;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SynthesisNode;

/**
 * Test SBOL tech. map on spec designed for a NOR gate. This will map a NOR gate using tandem promoters. 
 * @author Tramy Nguyen
 */
public class LibrarySize1_Test2 {
	
	private static SBOLDocument sbolDoc = null;

	@BeforeClass
	public static void setupTest() {
		try {
			SBOLTechMapOptions techMapOptions = new SBOLTechMapOptions();
			techMapOptions.setSpecificationFile(SBOLTechMapTestSuite.NOR1_LibSize1);
			techMapOptions.setLibraryFile(SBOLTechMapTestSuite.NOR1_LibSize1);
			
			Synthesis syn = SBOLTechMap.runSBOLTechMap(techMapOptions.getSpeficationFile(), techMapOptions.getLibraryFile());
			Map<SynthesisNode, SBOLGraph> solution = syn.getBestSolution();
			sbolDoc = syn.getSBOLfromTechMapping(solution, syn.getSpecification());
		} 
		catch (SBOLException | SBOLValidationException | IOException | SBOLConversionException | SBOLTechMapException e) {
			e.printStackTrace();
		}
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
	public void Test_fcTopLevel() {
		ModuleDefinition md = sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate_solution", "1.0");
		List<String> expectedFCList = Arrays.asList("FC0_FC3_outputProtein", "FC1_FC1_inputProtein1", "FC2_FC2_inputProtein2");
		for(String expectedFC : expectedFCList) {
			FunctionalComponent fc = md.getFunctionalComponent(expectedFC);
			assertNotNull(fc);
			assertEquals(DirectionType.INOUT, fc.getDirection());
		}
	}
	
	@Test
	public void Test_fcNORGate() {
		ModuleDefinition md = sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate", "1.0");
		List<String> expectedFCList = Arrays.asList("FC3_outputProtein", "FC1_inputProtein1", "FC2_inputProtein2");
		for(String expectedFC : expectedFCList) {
			assertNotNull(md.getFunctionalComponent(expectedFC));
		}
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
			else {
				Assert.fail("Unexpected role found: " + role);
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
			else {
				Assert.fail("Unexpected role found: " + role);
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
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals("FC0_tu", p.getParticipant().getDisplayId());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals("FC3_outputProtein", p.getParticipant().getDisplayId());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_mapsTo() {
		ModuleDefinition fullCircuit = sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate_solution", "1.0");
		ModuleDefinition gate = sbolDoc.getModuleDefinition("MD0_TandemPromoterNORGate", "1.0");
		Module circuit_instance = fullCircuit.getModule("M0_MD0_TandemPromoterNORGate");
		for(MapsTo mp : circuit_instance.getMapsTos()) {
			String localId = mp.getLocal().getDisplayId();
			
			if(localId.equals("FC0_FC3_outputProtein")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC3_outputProtein"));
			}
			else if(localId.equals("FC1_FC1_inputProtein1")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC1_inputProtein1"));
			}
			else if(localId.equals("FC2_FC2_inputProtein2")) {
				assertEquals(mp.getLocal(), fullCircuit.getFunctionalComponent(localId));
				assertEquals(mp.getRemote(), gate.getFunctionalComponent("FC2_inputProtein2"));
			}
			else {
				Assert.fail("This MapsTo object has an unexpected Local ID: " + localId);
			}
		}
	}
}

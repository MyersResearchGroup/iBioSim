package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;

/**
 * Test libSBOLj createCopy methods
 * @author Tramy Nguyen
 */
public class Test_SBOLCreateCopy {
	
	private static SBOLDocument inputDoc; 
	
	@BeforeClass
	public static void setupTest() {
		
		SBOLReader.setURIPrefix(SBOLUtility.getInstance().getURIPrefix());
		try {
			inputDoc = SBOLReader.read(new File(SBOLTechMapTestSuite.NOT1_LibSize1));
		} catch (SBOLValidationException | IOException | SBOLConversionException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void TestComponentDefinition_createCopy() throws SBOLValidationException { 
		SBOLDocument resultDoc = SBOLUtility.getInstance().createSBOLDocument();
		for(ComponentDefinition c : inputDoc.getComponentDefinitions()) {
			resultDoc.createCopy(c);
		}
		
		assertEquals(7, resultDoc.getComponentDefinitions().size());
		
		for(ComponentDefinition actualCD : resultDoc.getComponentDefinitions()) {
			ComponentDefinition expectedCD = inputDoc.getComponentDefinition(actualCD.getIdentity());
			assertEquals(expectedCD, actualCD);
			assertTrue(actualCD.equals(expectedCD));
		}
		
		List<String> expectedCDList = Arrays.asList("CD0_promoter", "CD1_tu", "CD2_ribosome", "CD3_cds", "CD4_terminator",
				"CD5_inputProtein", "CD6_outputProtein");
		for(String expectedCD : expectedCDList) {
			assertNotNull(resultDoc.getComponentDefinition(expectedCD, "1.0"));
		}
		
	}
	
	@Test
	public void TestComponentDefinition_createRecursiveCopy() throws SBOLValidationException {
		SBOLDocument resultDoc = SBOLUtility.getInstance().createSBOLDocument();
		for(ComponentDefinition c : inputDoc.getComponentDefinitions()) {
			inputDoc.createRecursiveCopy(resultDoc, c);
		}
		assertEquals(7, resultDoc.getComponentDefinitions().size());
		
		for(ComponentDefinition actualCD : resultDoc.getComponentDefinitions()) {
			ComponentDefinition expectedCD = inputDoc.getComponentDefinition(actualCD.getIdentity());
			assertEquals(expectedCD, actualCD);
			assertTrue(actualCD.equals(expectedCD));
		}
		
		List<String> expectedCDList = Arrays.asList("CD0_promoter", "CD1_tu", "CD2_ribosome", "CD3_cds", "CD4_terminator",
				"CD5_inputProtein", "CD6_outputProtein");
		for(String expectedCDId : expectedCDList) {
			assertNotNull(resultDoc.getComponentDefinition(expectedCDId, "1.0"));
		}
		
	}

	@Test
	public void TestModuleDefinition_createCopy() throws SBOLValidationException {
		SBOLDocument resultDoc = SBOLUtility.getInstance().createSBOLDocument();
		for(ModuleDefinition md : inputDoc.getModuleDefinitions()) {
			resultDoc.createCopy(md);
		}
		assertEquals(1, resultDoc.getModuleDefinitions().size());

		ModuleDefinition actualMD = resultDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		ModuleDefinition expectedMD = inputDoc.getModuleDefinition(actualMD.getIdentity());
		assertEquals(expectedMD, actualMD);
		assertTrue(actualMD.equals(expectedMD));
		
		assertEquals(3, actualMD.getFunctionalComponents().size());
		for(FunctionalComponent actualFC : actualMD.getFunctionalComponents()) {
			FunctionalComponent expectedFC = expectedMD.getFunctionalComponent(actualFC.getIdentity());
			assertEquals(expectedFC, actualFC);
			assertTrue(actualFC.equals(expectedFC));
		}
		List<String> expectedFCList = Arrays.asList("FC0_tu", "FC1_inputProtein", "FC2_outputProtein");
		for(String expectedFCId : expectedFCList) {
			assertNotNull(actualMD.getFunctionalComponent(expectedFCId));
		}
		
		assertEquals(2, actualMD.getInteractions().size());
		for(Interaction actualInter : actualMD.getInteractions()) {
			Interaction expectedInter = expectedMD.getInteraction(actualInter.getIdentity());
			assertEquals(expectedInter, actualInter);
			assertTrue(actualInter.equals(expectedInter));
		}
		List<String> expectedInterList = Arrays.asList("I0_Inhib", "I1_Prod"); 
		for(String expecterInterId : expectedInterList) {
			assertNotNull(actualMD.getInteraction(expecterInterId));
		}
	}
	
	@Test
	public void TestModuleDefinition_createRecursiveCopy() throws SBOLValidationException {
		SBOLDocument resultDoc = SBOLUtility.getInstance().createSBOLDocument();
		for(ModuleDefinition md : inputDoc.getModuleDefinitions()) {
			inputDoc.createRecursiveCopy(resultDoc, md);
		}
		assertEquals(1, resultDoc.getModuleDefinitions().size());

		ModuleDefinition actualMD = resultDoc.getModuleDefinition("MD0_NOTGate", "1.0");
		ModuleDefinition expectedMD = inputDoc.getModuleDefinition(actualMD.getIdentity());
		assertEquals(expectedMD, actualMD);
		assertTrue(actualMD.equals(expectedMD));
		
		assertEquals(3, actualMD.getFunctionalComponents().size());
		for(FunctionalComponent actualFC : actualMD.getFunctionalComponents()) {
			FunctionalComponent expectedFC = expectedMD.getFunctionalComponent(actualFC.getIdentity());
			assertEquals(expectedFC, actualFC);
			assertTrue(actualFC.equals(expectedFC));
		}
		List<String> expectedFCList = Arrays.asList("FC0_tu", "FC1_inputProtein", "FC2_outputProtein");
		for(String expectedFCId : expectedFCList) {
			assertNotNull(actualMD.getFunctionalComponent(expectedFCId));
		}
		
		assertEquals(2, actualMD.getInteractions().size());
		for(Interaction actualInter : actualMD.getInteractions()) {
			Interaction expectedInter = expectedMD.getInteraction(actualInter.getIdentity());
			assertEquals(expectedInter, actualInter);
			assertTrue(actualInter.equals(expectedInter));
		}
		List<String> expectedInterList = Arrays.asList("I0_Inhib", "I1_Prod"); 
		for(String expecterInterId : expectedInterList) {
			assertNotNull(actualMD.getInteraction(expecterInterId));
		}	
	}
	
	@Test
	public void Test_fcDefinition() {
		Set<ComponentDefinition> cdList = inputDoc.getComponentDefinitions();
		for(ModuleDefinition md : inputDoc.getModuleDefinitions()) {
			for(FunctionalComponent fc : md.getFunctionalComponents()) {
				URI definitionId = fc.getDefinition().getIdentity();
				Assert.assertTrue(cdList.contains(fc.getDefinition()));
				Assert.assertEquals(definitionId, inputDoc.getComponentDefinition(definitionId).getIdentity());
			}
		}
	}
	
}

package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GateIdentifier;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NORGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ORGate;

/**
 * Test gate types that are supported in GateIdentifier class
 * @author Tramy Nguyen
 *
 */
public class GateIdentifier_Test {
	
	@Test
	public void Test_NOT() {
		try {
			SBOLDocument inFile = SBOLReader.read(new File(TestingFiles.NOT_LibSize1));
			Assert.assertEquals(1, inFile.getRootModuleDefinitions().size());
			ModuleDefinition md = inFile.getRootModuleDefinitions().iterator().next();
			GateIdentifier sortInstance = new GateIdentifier(inFile, md);
			GeneticGate gate = sortInstance.getIdentifiedGate();
			Assert.assertTrue(gate instanceof NOTGate);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void Test_OR() {
		try {
			SBOLDocument orGate = SyntheticGateExamples.createORGate();
			Assert.assertEquals(1, orGate.getRootModuleDefinitions().size());
			ModuleDefinition md = orGate.getRootModuleDefinitions().iterator().next();

			GateIdentifier sortInstance = new GateIdentifier(orGate, md);
			GeneticGate gate = sortInstance.getIdentifiedGate();
			Assert.assertTrue(gate instanceof ORGate);
			Assert.assertEquals(2, gate.getListOfInputs().size());
			for(FunctionalComponent fc : gate.getListOfInputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC1_x0_protein") || fc.getDisplayId().equals("FC2_x1_protein"));
			}

			Assert.assertEquals(1, gate.getListOfOutputs().size());
			for(FunctionalComponent fc : gate.getListOfOutputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC0_Y_protein"));
			}

		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		} 
		catch (GateGenerationExeception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void Test_NOR1(){
		try {
			SBOLDocument norGate = SyntheticGateExamples.createNORGate1();
			ModuleDefinition md = norGate.getRootModuleDefinitions().iterator().next();
			GateIdentifier sortInstance = new GateIdentifier(norGate, md);
			GeneticGate gate = sortInstance.getIdentifiedGate();
			Assert.assertTrue(gate instanceof NORGate);
			Assert.assertEquals(2, gate.getListOfInputs().size());
			for(FunctionalComponent fc : gate.getListOfInputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC1_x0_protein") || fc.getDisplayId().equals("FC2_x1_protein"));
			}

			Assert.assertEquals(1, gate.getListOfOutputs().size());
			for(FunctionalComponent fc : gate.getListOfOutputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC0_Y_protein"));
			}

		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void Test_NOR2() {
		try {
			SBOLDocument norGate = SyntheticGateExamples.createNORGate2();
			ModuleDefinition md = norGate.getRootModuleDefinitions().iterator().next();
			GateIdentifier sortInstance = new GateIdentifier(norGate, md);
			GeneticGate gate = sortInstance.getIdentifiedGate();
			Assert.assertTrue(gate instanceof NORGate);
			Assert.assertEquals(2, gate.getListOfInputs().size());
			for(FunctionalComponent fc : gate.getListOfInputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC1_x0_protein") || fc.getDisplayId().equals("FC2_x1_protein"));
			}

			Assert.assertEquals(1, gate.getListOfOutputs().size());
			for(FunctionalComponent fc : gate.getListOfOutputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC0_Y_protein"));
			}

		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void Test_NOR3() {
		try {
			SBOLDocument norGate = SyntheticGateExamples.createNORGate3();
			ModuleDefinition md = norGate.getRootModuleDefinitions().iterator().next();
			GateIdentifier sortInstance = new GateIdentifier(norGate, md);
			GeneticGate gate = sortInstance.getIdentifiedGate();
			Assert.assertTrue(gate instanceof NORGate);
			Assert.assertEquals(2, gate.getListOfInputs().size());
			for(FunctionalComponent fc : gate.getListOfInputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC1_x0_protein") || fc.getDisplayId().equals("FC2_x1_smallMolecule"));
			}

			Assert.assertEquals(1, gate.getListOfOutputs().size());
			for(FunctionalComponent fc : gate.getListOfOutputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC0_Y_protein"));
			}

		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void Test_NAND() {
		try {
			SBOLDocument nandGate = SyntheticGateExamples.createNANDGate();
			ModuleDefinition md = nandGate.getRootModuleDefinitions().iterator().next();
			GateIdentifier sortInstance = new GateIdentifier(nandGate, md);
			GeneticGate gate = sortInstance.getIdentifiedGate();
			Assert.assertTrue(gate instanceof NANDGate);
			Assert.assertEquals(2, gate.getListOfInputs().size());
			for(FunctionalComponent fc : gate.getListOfInputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC1_x0_protein") || fc.getDisplayId().equals("FC2_x1_protein"));
			}

			Assert.assertEquals(1, gate.getListOfOutputs().size());
			for(FunctionalComponent fc : gate.getListOfOutputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC0_Y_protein"));
			}

		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void Test_AND() {
		try {
			SBOLDocument nandGate = SyntheticGateExamples.createANDGate();
			ModuleDefinition md = nandGate.getRootModuleDefinitions().iterator().next();
			GateIdentifier sortInstance = new GateIdentifier(nandGate, md);
			GeneticGate gate = sortInstance.getIdentifiedGate();
			Assert.assertTrue(gate instanceof ANDGate);
			Assert.assertEquals(2, gate.getListOfInputs().size());
			for(FunctionalComponent fc : gate.getListOfInputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC1_x0_protein") || fc.getDisplayId().equals("FC2_x1_protein"));
			}

			Assert.assertEquals(1, gate.getListOfOutputs().size());
			for(FunctionalComponent fc : gate.getListOfOutputs()) {
				Assert.assertTrue(fc.getDisplayId().equals("FC0_Y_protein"));
			}	
		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		}
	}
}

package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
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
public class TestGateIdentifier {
	
	@Test
	public void Test_NOT() throws SBOLValidationException {
		try {
			SBOLDocument inFile = SBOLReader.read(new File(TestingFiles.NOT1_LibSize1));
			for(ModuleDefinition md : inFile.getRootModuleDefinitions()) {
				GateIdentifier sortInstance = new GateIdentifier(inFile, md);
				
				GeneticGate gate = sortInstance.createGate();
				Assert.assertTrue(gate instanceof NOTGate);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void Test_OR() {
		try {
			SBOLDocument orGate = SyntheticGateExamples.createORGate();
			for(ModuleDefinition md : orGate.getRootModuleDefinitions()) {
				GateIdentifier sortInstance = new GateIdentifier(orGate, md);
				GeneticGate gate = sortInstance.createGate();
				Assert.assertTrue(gate instanceof ORGate);
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
	public void Test_NOR1() throws SBOLValidationException {
		try {
			SBOLDocument norGate = SyntheticGateExamples.createNORGate1();
			for(ModuleDefinition md : norGate.getRootModuleDefinitions()) {
				GateIdentifier sortInstance = new GateIdentifier(norGate, md);
				GeneticGate gate = sortInstance.createGate();
				Assert.assertTrue(gate instanceof NORGate);
			}
		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void Test_NOR2() throws SBOLValidationException {
		try {
			SBOLDocument norGate = SyntheticGateExamples.createNORGate2();
			for(ModuleDefinition md : norGate.getRootModuleDefinitions()) {
				GateIdentifier sortInstance = new GateIdentifier(norGate, md);
				GeneticGate gate = sortInstance.createGate();
				Assert.assertTrue(gate instanceof NORGate);
			}
		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void Test_NOR3() throws SBOLValidationException {
		try {
			SBOLDocument norGate = SyntheticGateExamples.createNORGate3();
			for(ModuleDefinition md : norGate.getRootModuleDefinitions()) {
				GateIdentifier sortInstance = new GateIdentifier(norGate, md);
				GeneticGate gate = sortInstance.createGate();
				Assert.assertTrue(gate instanceof NORGate);
			}
		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void Test_NAND() throws SBOLValidationException {
		try {
			SBOLDocument nandGate = SyntheticGateExamples.createNANDGate();
			for(ModuleDefinition md : nandGate.getRootModuleDefinitions()) {
				GateIdentifier sortInstance = new GateIdentifier(nandGate, md);
				GeneticGate gate = sortInstance.createGate();
				Assert.assertTrue(gate instanceof NANDGate);
			}
		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void Test_AND() throws SBOLValidationException {
		try {
			SBOLDocument nandGate = SyntheticGateExamples.createANDGate();
			for(ModuleDefinition md : nandGate.getRootModuleDefinitions()) {
				GateIdentifier sortInstance = new GateIdentifier(nandGate, md);
				GeneticGate gate = sortInstance.createGate();
				Assert.assertTrue(gate instanceof ANDGate);
			}
		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (GateGenerationExeception e) {
			e.printStackTrace();
		}
	}
}

package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class TestSort {
	
	@Test
	public void Test_NOT() throws SBOLValidationException {
		try {
			SBOLDocument inFile = SBOLReader.read(new File(TestingFiles.NOT1_LibSize1));
			for(ModuleDefinition md : inFile.getRootModuleDefinitions()) {
				GateIdentifier sortInstance = new GateIdentifier(inFile, md);
				String gatePattern = sortInstance.createGate();
				Assert.assertTrue("prominhibproteincdsprodprotein".equals(gatePattern));
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
				String gatePattern = sortInstance.createGate();
				System.out.println("hello: " + gatePattern);
				//Assert.assertTrue("promstimproteinstimproteincdsprodprotein".equals(gatePattern));
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
	public void Test_tu() throws SBOLValidationException {
		SBOLDocument tu = SyntheticGateExamples.testComponent();
		for(ComponentDefinition cd : tu.getComponentDefinitions()) {
			for(Component c : cd.getSortedComponents()) {
				ComponentDefinition real = c.getDefinition();
				System.out.println(real.getDisplayId());
				System.out.println(c.getDisplayId());
			}
		}
	}
	
}

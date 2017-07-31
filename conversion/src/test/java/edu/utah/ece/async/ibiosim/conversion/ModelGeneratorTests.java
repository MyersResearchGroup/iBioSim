package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import junit.framework.Assert;
import uk.ac.ncl.ico2s.VPRException;
import uk.ac.ncl.ico2s.VPRTripleStoreException;

// TODO: temporarily removed until VPR library sorted
//import uk.ac.ncl.ico2s.VPRException;
//import uk.ac.ncl.ico2s.VPRTripleStoreException;

public class ModelGeneratorTests extends ConversionAbstractTests{

	/*
	 * r1.xml - module_BO_10845_represses_BO_27632; module_BO_28528_encodes_BO_26934
	 * r2.xml - module_BO_10858_represses_BO_27720; module_BO_28529_encodes_BO_26892
	 * r3.xml - module_BO_11205_activates_BO_27654; module_BO_28532_encodes_BO_26966
	 * r4.xml - Pro: BO_2685 RBS: BO_27789 CDS: BO_28531 T: BO_4261
	 */
	
	private SBOLDocument readSBOLFile(String fullInputFileName)
	{
		File file = new File(fullInputFileName);
		SBOLDocument inSBOLDoc = null;
		try {
			inSBOLDoc = SBOLReader.read(file);
		} catch (SBOLValidationException e) {
			System.err.println("ERROR: Invalid SBOL file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ERROR: Unable to read file.");
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			System.err.println("ERROR: Unable to perform internal SBOL conversion from libSBOLj library.");
			e.printStackTrace();
		}
		
		return inSBOLDoc;
	}
	
	@Test
	public void test_cmd()
	{
		/* Test if VPR model generation can be run from command line */
		String fileName = "inverterExample"; 
		String inputfile = sbolDir + fileName + ".xml";
		String selectedRepo = "https://synbiohub.org";
		String uriPrefix = "http://www.async.ece.utah.edu/";
		
		String[] cmdArgs = {"-u", uriPrefix, "-r", selectedRepo, inputfile, "-o", sbolDir+fileName+"_ibiosim_output"};
		edu.utah.ece.async.ibiosim.conversion.VPRModelGenerator.main(cmdArgs);
	}
	
	@Test
	public void test_port()
	{
		String fileName = "inverterExample"; 
		String selectedRepo = "https://synbiohub.org";
		
		SBOLDocument inSBOL = readSBOLFile(sbolDir + fileName + ".xml");
		SBOLDocument goldenSBOL = readSBOLFile(sbolDir + fileName + "_output.xml");
		if(inSBOL == null)
		{
			System.err.println("ERROR: Invalid SBOL file.");
			return;
		}
		try 
		{
			SBOLDocument generatedSBOL = edu.utah.ece.async.ibiosim.conversion.VPRModelGenerator.generateModel(selectedRepo, inSBOL);
			Assert.assertTrue(goldenSBOL.equals(generatedSBOL));
			ModuleDefinition design1 = generatedSBOL.getModuleDefinition("design1_module", "1");
			Assert.assertTrue(design1.getFunctionalComponent("BO_10845").getDirection().equals(DirectionType.INOUT));
			
			ModuleDefinition design2 = generatedSBOL.getModuleDefinition("design2_module", "1");
			Assert.assertTrue(design2.getFunctionalComponent("BO_11410").getDirection().equals(DirectionType.INOUT));
			
			ModuleDefinition topModel = generatedSBOL.getModuleDefinition(URI.create("https://synbiohub.org/public/bsu/design1_design2_module"));
			Assert.assertTrue(topModel.getFunctionalComponent("BO_10845").getDirection().equals(DirectionType.INOUT));
			Assert.assertTrue(topModel.getFunctionalComponent("BO_11410").getDirection().equals(DirectionType.INOUT));
		} 
		catch (SBOLValidationException e) 
		{
			System.err.println("ERROR: Invalid SBOL file.");
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			System.err.println("ERROR: Unable to read file.");
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) 
		{
			System.err.println("ERROR: Unable to perform internal SBOL conversion from libSBOLj library.");
			e.printStackTrace();
		} 
		catch (VPRException e) 
		{
			System.err.println("ERROR: Unable to Perform VPR Model Generation");
			e.printStackTrace();
		} 
		catch (VPRTripleStoreException e) 
		{
			System.err.println("ERROR: This SBOL file has contents that can't perform VPR model generation.");
			e.printStackTrace();
		}
	}
	
}

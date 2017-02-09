package test.conversion;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import conversion.SBML2SBOL;
import conversion.SBOL2SBML;


/**
 * Class to test roundtripping of SBML and SBOL files in SBML2SBOL and SBOL2SBML conversion.
 *  
 * @author Tramy Nguyen
 */
public class RoundtripTest extends ConversionAbstractTests{
	
	String workingDirectory = System.getProperty("user.dir");
	String resourceDir = workingDirectory + File.separator + "src" + File.separator + "test" + File.separator + "conversion" + File.separator + "resources" + File.separator ;
	String sbml2sbol_outputDir = resourceDir + "SBML2SBOL_Output" + File.separator;
	String sbol2sbml_outputDir = resourceDir + "SBOL2SBML_Output" + File.separator;
	String sbmlDir = resourceDir + "SBML" + File.separator;
	String sbolDir = resourceDir + "SBOL" + File.separator;
	
	
	@Override
	public void roundtripSBOLFile(final String file){
		
	}
	
	@Override
	public void roundtripSBMLFile(final String fileName){
		String inputFile = sbmlDir + fileName + ".xml";
		String outputFile = sbml2sbol_outputDir + fileName + ".rdf";
		
		//inputFile -o outputFileName
		String[] sbml2sbol_cmdArgs = {inputFile,"-o", outputFile};
		SBML2SBOL.main(sbml2sbol_cmdArgs);
		
		inputFile = outputFile;
		outputFile = sbol2sbml_outputDir + fileName + ".xml";
		String[] sbol2sbml_cmdArgs = {inputFile,"-o", outputFile};
		SBOL2SBML.main(sbol2sbml_cmdArgs);
		
	}
	
	public void convertSBML(String fileName){
		String inputFile = sbmlDir + fileName + ".xml";
		String outputFile = sbml2sbol_outputDir + fileName + ".rdf";
		
		//inputFile -o outputFileName
		String[] sbml2sbol_cmdArgs = {inputFile,"-o", outputFile};
		SBML2SBOL.main(sbml2sbol_cmdArgs);
	}
	
	public void convertSBOL(String fileName){
		String inputFile = sbml2sbol_outputDir + fileName + ".rdf";
		String outputFile = sbol2sbml_outputDir + fileName + ".xml";
		String[] sbol2sbml_cmdArgs = {inputFile,"-o", outputFile};
		SBOL2SBML.main(sbol2sbml_cmdArgs);
	}
	
	
	@Test
	public void run_repressibleTU_Connected(){
		
		String fileName = "repressibleTU_Connected";
		roundtripSBMLFile(fileName);
	}
}

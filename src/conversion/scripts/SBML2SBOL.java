package conversion.scripts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import conversion.SBMLtoSBOL;
import dataModels.biomodel.parser.BioModel;

public class SBML2SBOL {
	
	
	public static void main(String[] args) {
		//GOAL: inputFile -I SBML_ExternalPath -o outputFileName
		if(args.length < 0){
			//TODO: Display help message to run command line
			return;
		}
		
		
		if(args[0].equals("-h")){
			
		}
		else{
			Arguments argument = new Arguments();
			argument.parseArguments(args);
			try {
				SBMLtoSBOL sbml2Sbol = new SBMLtoSBOL(argument.getSbolInputFiles(), argument.getIncludePath(), argument.createBioModel());
				sbml2Sbol.export(argument.getOutputName()+".rdf", "SBOL");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SBOLValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SBOLConversionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void helpOptions(){
		
	}
	

}

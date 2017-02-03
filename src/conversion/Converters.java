package conversion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import dataModels.biomodel.parser.BioModel;

public class Converters {
	
	private static class Arguments{
		
		//Required Information
		private HashSet<String> sbolInputFiles;
		private String includePath;
		private String inputName;
		
		//Optional Information
		private String outputName;
		private String uriPrefix;
		
		
		public Arguments()
		{
			sbolInputFiles = new HashSet<String>();
		}
	
		public boolean parseArguments(String[] args){
			
			for(int i = 1; i< args.length-1; i=i+2){
				String flag = args[i];
				String value = args[i+1];
				
				switch(flag)
				{
				case "-I":
					includePath = value;
					break;
				case "-i":
					inputName = value;
					break;
				case "-o":
					outputName = value;
					break;
					
				default:
					return false;
				}
				
			}
			return true;
		}
		
		public BioModel createBioModel()
		{
			if(includePath != null && inputName != null)
			{
				BioModel bioModel = new BioModel(includePath);
				bioModel.load(inputName);
				return bioModel;
			}
			
			return null;
		}
		
		public HashSet<String> getSbolInputFiles() {
			return sbolInputFiles;
		}

		public String getIncludePath() {
			return includePath;
		}

		public String getInputName() {
			return inputName;
		}

		public String getOutputName() {
			return outputName;
		}

		public String getUriPrefix() {
			return uriPrefix;
		}
		
	}
	
	
	public static void main(String[] args) {
		if(args.length < 0){
			//TODO: Display help message to run command line
			return;
		}
		
		Arguments argument = new Arguments();
		argument.parseArguments(args);
		
		if(args[0].equals("SBML2SBOL")){
			
			try {
				SBMLtoSBOL sbml2Sbol = new SBMLtoSBOL(argument.getSbolInputFiles(), argument.getIncludePath(), argument.createBioModel());
				sbml2Sbol.export(argument.getOutputName(), "SBOL");
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
		else if(args[0].equals("SBOL2SBML")){
			
		}
		else if(args[0].equals("-h")){
			
		}
	}
	
	private void helpOptions(){
		
	}
	

}

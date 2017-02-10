package conversion.scripts;

import java.util.HashSet;

import dataModels.biomodel.parser.BioModel;

public class Arguments{
		
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
			inputName = args[0];
			for(int i = 1; i< args.length-1; i=i+2){
				String flag = args[i];
				String value = args[i+1];
				
				switch(flag)
				{
				case "-I":
					includePath = value;
					break;
				case "-o":
					outputName = value;
					break;
				case "-u":
					uriPrefix = value;
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
		
		public boolean isSetURIPrefix(){
			
			return uriPrefix != null;
		}
		
	}
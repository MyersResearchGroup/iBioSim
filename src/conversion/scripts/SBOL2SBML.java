package conversion.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import conversion.ModelGenerator;
import dataModels.biomodel.parser.BioModel;;

public class SBOL2SBML {

	public static void main(String[] args) {
		
		//GOAL: inputFile -o outputLocation -u optionalURI
		
		if(args.length < 0){
			//TODO: Display help message to run command line
			return;
		}


		if(args[0].equals("-h")){

		}
		else{
			Arguments argument = new Arguments();
			argument.parseArguments(args);

			SBOLDocument sbolDoc;
			try {
				sbolDoc = SBOLReader.read(new FileInputStream(argument.getInputName()));
				String projectDirectory = argument.getOutputName();
				
				if(argument.isSetURIPrefix()){
					ModuleDefinition topModuleDef= sbolDoc.getModuleDefinition(URI.create(argument.getUriPrefix()));
					System.out.println(topModuleDef.getIdentity());
					List<BioModel> models = ModelGenerator.generateModel(projectDirectory, topModuleDef, sbolDoc);
					for (BioModel model : models)
					{
						model.save(projectDirectory + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
					}
				}
				else{
					//No ModuleDefinition URI provided so loop over all rootModuleDefinition
					for (ModuleDefinition moduleDef : sbolDoc.getRootModuleDefinitions())
					{
						List<BioModel> models = ModelGenerator.generateModel(projectDirectory, moduleDef, sbolDoc);
						for (BioModel model : models)
						{
							model.save(projectDirectory + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
						}
					}
				}
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

}

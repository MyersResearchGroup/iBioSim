/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.EDAMOntology;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;
import org.sbolstandard.core2.TopLevel;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;

/**
 * Perform conversion from SBML to PRISM. 
 *
 * @author Lukas Buecherl
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBML2PRISM {
	
	
	public static void GoodJob() //throws XMLStreamException, IOException, BioSimException
	{
		System.err.println("Good Job!");
	}
	
	
	public static void convertSBML2PRISM(SBMLDocument sbmlDoc, String filename) throws IOException 
	{
		Model model = sbmlDoc.getModel();
		File file = new File(filename);
		
			System.err.println("Species: ");
			for (int i = 0; i < model.getSpeciesCount(); i++) 
			{
				Species species = model.getSpecies(i);
				System.err.println(species);
			}

			System.err.println("Reactions: ");
			for (int i = 0; i < model.getReactionCount(); i++) 
			{
				Reaction reaction = model.getReaction(i);
				System.err.println(reaction);

			}
		
			for (int i = 0; i < model.getConstraintCount(); i++) 
			{
				System.err.println(SBMLutilities.convertMath2PrismProperty(model.getConstraint(i).getMath()));
			}
		
			
		
			FileWriter out = new FileWriter(file);
			out.write("ctmc\n");
			out.close();
			
			/*if (s.isConstant()) {
				// write const 
			} else {
				// write modules
			}
			*/
	}
		
		
		
		
		
		
/*		
		
		
		
		private int executePrism(String filename) throws IOException, InterruptedException, XMLStreamException, BioSimException {
		    int exitValue = 255;
		    String prop = null;
		    String directory = properties.getDirectory() + File.separator;
		    String out = properties.getModelFile().replace(".xml", "").replaceAll(".lpn", "").replaceAll(".sbml", "");
		    LPN lhpnFile = null;
		    // String root = properties.getRoot();


		      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
		      ArrayList<String> specs = new ArrayList<>();
		      ArrayList<Object[]> conLevel = new ArrayList<>();
		      retrieveSpeciesAndConLevels(specs, conLevel);
		      BioModel bioModel = BioModel.createBioModel(properties.getDirectory(), this);
		      bioModel.load(filename);
		      if (bioModel.flattenModel(true) != null) {
		        if (properties.getVerificationProperties().getLpnProperty() != null && !properties.getVerificationProperties().getLpnProperty().equals("")) {
		          prop = properties.getVerificationProperties().getLpnProperty();
		        } else {
		          prop = properties.getVerificationProperties().getConstraintProperty();
		        }
		        MutableString mutProp = new MutableString(prop);
		        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
		        prop = mutProp.getString();
		        if (lhpnFile == null) {
		          new File(properties.getDirectory() + File.separator + "running").delete();
		          return 0;
		        }
		        message.setLog("Saving SBML file as PRISM file:\n" + filename.replace(".xml", ".prism"));
		        this.notifyObservers(message);
		        message.setLog("Saving PRISM Property file:\n" + filename.replace(".xml", ".pctl"));
		        this.notifyObservers(message);
		    	// TODO: LUKAS
		    	// bioModel.convertSBML2PRISM(logFile, filename);
		        LPN.convertLPN2PRISM(logFile, lhpnFile, filename.replace(".xml", ".prism"), bioModel.getSBMLDocument());
		        Preferences biosimrc = Preferences.userRoot();
		        String prismCmd = biosimrc.get("biosim.general.prism", "");
		        if (prismCmd.contains("$prism")) {
		          prismCmd = prismCmd.replace("$prism", directory + out + ".prism");
		        } else {
		          prismCmd = prismCmd + " " + directory + out + ".prism";
		        }
		        if (prismCmd.contains("$pctl")) {
		          prismCmd = prismCmd.replace("$pctl", directory + out + ".pctl");
		        } else {
		          prismCmd = prismCmd + " " + directory + out + ".pctl";
		        }
		        message.setLog("Executing:\n" + prismCmd);
		        this.notifyObservers(message);
		        reb2sac = exec.exec(prismCmd, null, work);
		        String error = "", result = "";
		        try {
		          InputStream reb = reb2sac.getInputStream();
		          InputStreamReader isr = new InputStreamReader(reb);
		          BufferedReader br = new BufferedReader(isr);
		          String line;
		          while ((line = br.readLine()) != null) {
		            if (line.startsWith("Result")) {
		              result = line + '\n';
		            }
		          }
		          InputStream reb2 = reb2sac.getErrorStream();
		          int read = reb2.read();
		          while (read != -1) {
		            error += (char) read;
		            read = reb2.read();
		          }
		          br.close();
		          isr.close();
		          reb.close();
		          reb2.close();
		        }
		        catch (Exception e) {
		          // e.printStackTrace();
		        }
		        if (reb2sac != null) {
		          exitValue = reb2sac.waitFor();
		        }
		        if (!error.equals("")) {
		          message.setLog("Errors:\n" + error + "\n");

		          this.notifyObservers(message);
		        } else if (!result.equals("")) {
		          message.setLog(result);
		          this.notifyObservers(message);
		        } else {
		          throw new BioSimException("Verification Failed!", "Verification could not be executed. Something went wrong.");
		        }

		        exitValue = 0;
		      } else {
		        new File(directory + File.separator + "running").delete();
		        logFile.close();
		        exitValue = 0;
		      }
		    }

		    if (reb2sac != null) {
		      exitValue = reb2sac.waitFor();
		    }

		    return exitValue;
		  }
	
		
		*?

		
		
		
		
		
		// write pctl file using the SBML constraints
/*
		File file = new File(filename);
		try {
			FileWriter out = new FileWriter(file);
			out.write("ctmc\n");
			for (String var : LPN.getVariables()) {
				int i=0;
				Place place;
				String lastValue="";
				while ((place = LPN.getPlace(var+i))!=null) {
					Transition inTrans = place.getPreset()[0];
					ExprTree assign = inTrans.getAssignTree(var);
					lastValue = assign.toString();
					i++;
				}
				Variable variable = LPN.getVariable(var);
				String initValue = variable.getInitValue();
				initValue = Long.toString((Math.round(Double.valueOf(initValue))));
				if (lastValue.equals("")) {
					out.write("const int "+var+"="+initValue+";\n");
				} else {
					out.write("module "+var+"_def\n");
					out.write("  "+var+" : "+"[0.."+lastValue+"] init "+initValue+";\n");
					i=0;
					while ((place = LPN.getPlace(var+i))!=null) {
						Transition inTrans = place.getPreset()[0];
						ExprTree assign = inTrans.getAssignTree(var);
						out.write("  [] "+var+"="+assign.toString()+" -> ");
						boolean first = true;
						for (Transition outTrans : place.getPostset()) {
							assign = outTrans.getAssignTree(var);
							ExprTree delay = outTrans.getDelayTree();
							String rate = delay.toString("prism");
							rate = rate.replace("exponential", "");
							if (!first) out.write(" + ");
							out.write(rate+":("+var+"'="+assign.toString()+")");
							first = false;
						}
						out.write(";\n");
						i++;
					}
					out.write("endmodule\n");
				}
			}
			out.close();
			for (int i = 0; i < sbml.getModel().getConstraintCount(); i++) {
				file = new File(filename.replace(".prism", ".pctl"));
				out = new FileWriter(file);
				out.write(SBMLutilities.convertMath2PrismProperty(sbml.getModel().getConstraint(i).getMath()));
				out.close();
			}
		}
		catch (IOException e) {
			//TODO: Leandro fix Me
			//message.setErrorDialog("Error Writing File", "I/O error when writing PRISM file");
		}
		*/
	
}



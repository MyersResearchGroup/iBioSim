package edu.utah.ece.async.ibiosim.learn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.learn.genenet.Experiments;
import edu.utah.ece.async.ibiosim.learn.genenet.Run;
import edu.utah.ece.async.ibiosim.learn.genenet.SpeciesCollection;
import edu.utah.ece.async.ibiosim.learn.parameterestimator.ParameterEstimator;

/**
 * Command line method for running the learn jar file.  
 * <p>
 * Requirements:
 * <p>
 * inputfile: full path to the input SBML file.
 * <p>
 * directory: directory where the experimental data is located.
 * <p>
 * Options:
 * <p>
 * -e: when specified, the program will run parameter estimation.
 * -l: when specified, parameter estimation will use the estimate the value of the paramaters in the list.
 * 
 * @author Leandro Watanabe
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Learn {

  private static void usage() {
    System.err.println("Description:");
    System.err.println("\tExecutes bayesian methods for structural learning using GeneNet or parameter estimation using SRES.");
    System.err.println("Usage:"); 
    System.err.println("\tjava -jar iBioSim-learn-3.0.0-SNAPSHOT-jar-with-dependencies.jar [options] <Input File> <Project Directory>");
    System.err.println("Required:");
    System.err.println("\t<Input File> the input SBML file.");
    System.err.println("\t<Project Directory> the directory where the experimental data is located.");
    System.err.println("Options:");
    System.err.println("\t-e to execute parameter estimation.");
    System.err.println("\t-l to specify the list of parameters to estimate. If not specified, all parameters are estimated.");
    System.err.println("\t   to use it, specify the parameters separated by commas (e.g. p1,p2,p3).");
    
    System.exit(1);
  }

  public static void main(String[] args) {

    if(args.length  < 2)
    {
      usage();
    }

    boolean runParameterEstimation = false;
    List<String> listOfParameters = null;
    String filename = args[args.length-2];
    String directory = args[args.length-1];
    
    for(int i = 0; i < args.length-2; i++)
    {
      if(args[i].startsWith("-"))
      {
        if(args[i].length() == 2)
        {
          if(args[i].charAt(1) == 'e')
          {
            runParameterEstimation = true;
          }
          else if(args[i].charAt(1) == 'l')
          {
            if(i+1 < args.length-2)
            {
              listOfParameters = new ArrayList<String>();
              String unparsedList = args[i+1];
              String[] parsedList = unparsedList.split(",");
              for(String param : parsedList)
              {
                listOfParameters.add(param);
              }
            }
            else
            {
              usage();
            }
          }
        }
        else
        {
          usage();
        }
      }
    }
    
    if(runParameterEstimation)
    { 
      try {
        if(listOfParameters == null)
        {
          listOfParameters = new ArrayList<String>();
          SBMLDocument doc = SBMLReader.read(new File(filename));
          Model model = doc.getModel();
          for(Parameter param : model.getListOfParameters())
          {
            listOfParameters.add(param.getId());
          }
        }
        
        SpeciesCollection S = new SpeciesCollection();
        Experiments E = new Experiments();
        Run.init(filename, S);
        Run.loadExperiments(directory, S, E);
        SBMLDocument newDocument = ParameterEstimator.estimate(filename, directory, listOfParameters, E, S);
        if (newDocument != null)
        {
          Model model = newDocument.getModel();
          for (String parameterId : listOfParameters)
          {
            Parameter parameter = model.getParameter(parameterId);
            if(parameter != null)
            {
              System.out.println(parameterId + " = " + parameter.getValue());
            }
            else
            {
              System.out.println(parameterId + " = NA");
            }
            
          }
        }
      } catch (IOException e) {
        System.err.println("The program encoutered IO problems.");
      } catch (XMLStreamException e) {
        System.err.println("The program could not parse the input SBML file.");
      } catch (BioSimException e) {
        System.err.println("Error: " + e.getMessage());
      }

    }
    else
    {
      try {
        Run.run(filename, directory);
      } catch (BioSimException e) {
        System.err.println("Error: " + e.getMessage());
      }
    }
  } 
}

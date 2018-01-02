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
 * <ul>
 * <li>-e: when specified, the program will run parameter estimation.</li>
 * <li>-l: when specified, parameter estimation will use the estimate the value of the parameters in the list.</li>
 * <li>-ta [num]: Sets the activation threshold.  Default 1.15</li>
 * <li>-tr [num]: Sets the repression threshold.  Default 0.75</li>
 * <li>-ti [num]: Sets how high a score must be to be considered a parent.  Default 0.5</li>
 * <li>-tm [num]: Sets how close IVs must be in score to be considered for combination.  Default 0.01</li>
 * <li>-tn [num]: Sets minimum number of parents to allow through in SelectInitialParents. Default 2</li>
 * <li>-tj [num]: Sets the max parents of merged influence vectors, Default 2</li>
 * <li>-tt [num]: Sets how fast the bound is relaxed for ta and tr, Default 0.025</li>
 * <li>-d [num]:  Sets the debug or output level.  Default 0</li>
 * <li>-wr [num]: Sets how much larger a number must be to be considered as a rise.  Default 1</li>
 * <li>-ws [num]: Sets how far the TSD points are when compared.  Default 1</li>
 * <li>-nb [num]: Sets how many bins are used in the evaluation.  Default 4</li>
 * <li>--lvl:     Writes out the suggested levels for every species.</li>
 * <li>--readLevels: Reads the levels from level.lvl file for every species.</li>
 * <li>--cpp: runs the C++ GeneNet. Default is the Java version. </li>
 * <li>--cpp_harshenBoundsOnTie:  Determines if harsher bounds are used when parents tie in CPP.</li>
 * <li>--cpp_cmp_output_donotInvertSortOrder: Sets the inverted sort order in the 3 places back to normal</li>
 * <li>--cpp_seedParents  Determines if parents should be ranked by score, not tsd order in CPP.</li>
 * <li>--cmp_score_mustNotWinMajority:  Determines if score should be used when following conditions are not met a &gt; r+n || r &gt; a + n</li>
 * <li>--score_donotTossSingleRatioParents:   Determines if single ratio parents should be kept</li>
 * <li>--output_donotTossChangedInfluenceSingleParents: Determines if parents that change influence should not be tossed</li>
 * <li>-binNumbers: Equal spacing per bin</li>
 * <li>-noSUCC: to not use successors in calculating probabilities</li>
 * <li>-PRED: use preicessors in calculating probabilities</li>
 * <li>-basicFBP: to use the basic FindBaseProb function</li>
 * </ul>
 * 
 * @author Leandro Watanabe
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Learn {


  private double ta, tr, ti, tm, tn, tj, tt;
  private int d, wr, ws, nb;
  private boolean runParameterEstimation, lvl, readLevels, cpp, cpp_harshenBoundsOnTie, cpp_cmp_output_donotInvertSortOrder,
  cpp_seedParents, cmp_score_mustNotWinMajority, score_donotTossSingleRatioParents, output_donotTossChangedInfluenceSingleParents, binNumbers,
  noSUCC, PRED, basicFBP;
  private String directory;
  private String filename;
  private List<String> listOfParameters;
  
  private Learn()
  {
    ta = 1.15;
    tr = 0.75;
    ti = 0.5;
    tm = 0.01;
    tn = 2;
    tj = 2;
    tt = 0.025;
    d = 0;
    wr = 1;
    ws = 1;
    nb = 4;
  }
  
  private static void usage() {
    System.err.println("Description:");
    System.err.println("\tExecutes bayesian methods for structural learning of regulatory networks using GeneNet or parameter estimation using SRES.");
    System.err.println("Usage:"); 
    System.err.println("\tjava -jar iBioSim-learn-3.0.0-SNAPSHOT-jar-with-dependencies.jar [options] <Input SBML File> <Project Directory>");
    System.err.println("Required:");
    System.err.println("\t<Input File> the input SBML file.");
    System.err.println("\t<Project Directory> the directory where the experimental data is located.");
    System.err.println("Options:");
    System.err.println("\t-e to execute parameter estimation.");
    System.err.println("\t-l to specify the list of parameters to estimate. If not specified, all parameters are estimated. To use it, specify the parameters separated by commas (e.g. p1,p2,p3).");

    System.exit(1);
  }

  public static void main(String[] args) 
  {

    if(args.length  < 2)
    {
      usage();
    }

    Learn learn = new Learn();

    learn.filename = args[args.length-2];
    learn.directory = args[args.length-1];

    int end = args.length-2;
    for(int i = 0; i < end; i++)
    {
      String flag = args[i];
      switch(flag)
      {
      case "-e":
        learn.runParameterEstimation = true;
        break;
      case "-l":
        learn.listOfParameters = new ArrayList<String>();
        if(i+1 < end)
        {
          String[] parsedList = args[i+1].split(",");
          for(String parameter : parsedList)
          {
            learn.listOfParameters.add(parameter);
          }
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-ta":
        if(i+1 < end)
        {
          learn.ta = Double.parseDouble(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-tr":
        if(i+1 < end)
        {
          learn.tr = Double.parseDouble(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-ti":
        if(i+1 < end)
        {
          learn.ti = Double.parseDouble(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-tm":
        if(i+1 < end)
        {
          learn.tm = Double.parseDouble(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-tn":
        if(i+1 < end)
        {
          learn.tn = Double.parseDouble(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-tj":
        if(i+1 < end)
        {
          learn.tj = Double.parseDouble(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-tt":
        if(i+1 < end)
        {
          learn.tt = Double.parseDouble(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-d":
        if(i+1 < end)
        {
          learn.d = Integer.parseInt(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-wr":
        if(i+1 < end)
        {
          learn.wr = Integer.parseInt(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-ws":
        if(i+1 < end)
        {
          learn.ws = Integer.parseInt(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-nb":
        if(i+1 < end)
        {
          learn.nb = Integer.parseInt(args[i+1]);
          i=i+1;
        }
        else
        {
          usage();
        }
        break;
      case "-lvl":
        learn.lvl = true;
        break;
      case "--readLevels":
        learn.readLevels = true;
        break;
      case "--cpp":
        learn.cpp = true;
        break;
      case "--cpp_harshenBoundsOnTie":
        learn.cpp_harshenBoundsOnTie = true;
        break;
      case "--cpp_cmp_output_donotInvertSortOrder":
        learn.cpp_cmp_output_donotInvertSortOrder = true;
        break;
      case "--cpp_seedParents":
        learn.cpp_seedParents = true;
        break;
      case "--cmp_score_mustNotWinMajority":
        learn.cmp_score_mustNotWinMajority = true;
        break;
      case "--score_donotTossSingleRatioParents":
        learn.score_donotTossSingleRatioParents = true;
        break;
      case "--output_donotTossChangedInfluenceSingleParents":
        learn.output_donotTossChangedInfluenceSingleParents = true;
        break;
      case "-binNumbers":
        learn.binNumbers = true;
        break;
      case "--noSUCC":
        learn.noSUCC = true;
        break;
      case "--PRED":
        learn.PRED = true;
        break;
      case "--basicFBP":
        learn.basicFBP = true;
        break;
      }
    }

    try
    {
      if(learn.runParameterEstimation)
      { 
        learn.runParameterEstimation();
      }
      else
      {
        learn.runGeneNet();
      }
    }
    catch(Exception e)
    {
      System.err.println(e.getMessage());
    }
  } 

  private void runParameterEstimation() throws XMLStreamException, IOException, BioSimException
  {
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
  }

  private void runGeneNet() throws BioSimException
  {
    if(cpp)
    {
      
    }
    else
    {
      Run.run(filename, directory);
    }
  }
}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.BioObserver;
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
 * <li>--cpp: runs the C++ GeneNet. Default is the Java version. </li>
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
 * <li>--cpp_harshenBoundsOnTie:  Determines if harsher bounds are used when parents tie in CPP.</li>
 * <li>--cpp_cmp_output_donotInvertSortOrder: Sets the inverted sort order in the 3 places back to normal</li>
 * <li>--cpp_seedParents  Determines if parents should be ranked by score, not tsd order in CPP.</li>
 * <li>--cmp_score_mustNotWinMajority:  Determines if score should be used when following conditions are not met a &gt; r+n || r &gt; a + n</li>
 * <li>--score_donotTossSingleRatioParents:   Determines if single ratio parents should be kept</li>
 * <li>--output_donotTossChangedInfluenceSingleParents: Determines if parents that change influence should not be tossed</li>
 * <li>-binNumbers: Equal spacing per bin</li>
 * <li>-noSUCC: to not use successors in calculating probabilities</li>
 * <li>-PRED: use predecessors in calculating probabilities</li>
 * <li>-basicFBP: to use the basic FindBaseProb function</li>
 * </ul>
 * 
 * @author Leandro Watanabe
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Learn implements BioObserver
{
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
    System.err.println("\t--cpp: runs the C++ GeneNet. Default is the Java version.");
    System.err.println("\t-l to specify the list of parameters to estimate. If not specified, all parameters are estimated. To use it, specify the parameters separated by commas (e.g. p1,p2,p3).");
    System.err.println("\t-ta [num]: Sets the activation threshold.  Default 1.15");
    System.err.println("\t-tr [num]: Sets the repression threshold.  Default 0.75");
    System.err.println("\t-ti [num]: Sets how high a score must be to be considered a parent.  Default 0.5");
    System.err.println("\t-tm [num]: Sets how close IVs must be in score to be considered for combination.  Default 0.01");
    System.err.println("\t-tn [num]: Sets minimum number of parents to allow through in SelectInitialParents. Default 2");
    System.err.println("\t-tj [num]: Sets the max parents of merged influence vectors, Default 2");
    System.err.println("\t-tt [num]: Sets how fast the bound is relaxed for ta and tr, Default 0.025");
    System.err.println("\t-d [num]:  Sets the debug or output level.  Default 0");
    System.err.println("\t-wr [num]: Sets how much larger a number must be to be considered as a rise.  Default 1");
    System.err.println("\t-ws [num]: Sets how far the TSD points are when compared.  Default 1");
    System.err.println("\t-nb [num]: Sets how many bins are used in the evaluation.  Default 4");
    System.err.println("\t--lvl:     Writes out the suggested levels for every species.");
    System.err.println("\t--readLevels: Reads the levels from level.lvl file for every species.");
    System.err.println("\t--cpp_harshenBoundsOnTie:  Determines if harsher bounds are used when parents tie in CPP.");
    System.err.println("\t--cpp_cmp_output_donotInvertSortOrder: Sets the inverted sort order in the 3 places back to normal");
    System.err.println("\t--cpp_seedParents  Determines if parents should be ranked by score, not tsd order in CPP.");
    System.err.println("\t--cmp_score_mustNotWinMajority:  Determines if score should be used when following conditions are not met a &gt; r+n || r &gt; a + n");
    System.err.println("\t--score_donotTossSingleRatioParents:   Determines if single ratio parents should be kept");
    System.err.println("\t--output_donotTossChangedInfluenceSingleParents: Determines if parents that change influence should not be tossed");
    System.err.println("\t-binNumbers: Equal spacing per bin");
    System.err.println("\t-noSUCC: to not use successors in calculating probabilities");
    System.err.println("\t-PRED: use preicessors in calculating probabilities");
    System.err.println("\t-basicFBP: to use the basic FindBaseProb function");
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

  private void runGeneNet() throws BioSimException, IOException, XMLStreamException, InterruptedException
  {
    if(cpp)
    {
      System.out.println("Running GeneNet (C++)");
      File work = new File(directory);
      Runtime exec = Runtime.getRuntime();
      Learn.writeLearnFile(this);
      List<String> speciesList = Learn.writeBackgroundFile(filename, directory);
      Learn.writeLevelsFile(directory, speciesList, nb);
      String[] command = getProcessArguments();

      System.out.println("Executing:");
      System.out.println("\t" + SBMLutilities.commandString(command));
      Process process = exec.exec(command, null, work);
      process.waitFor();
    }
    else
    {
      System.out.println("Running GeneNet (Java)");
      Run.run(filename, directory);
    }
  }

  private String[] getProcessArguments()
  {
    ArrayList<String> args = new ArrayList<String>();
    args.add("GeneNet");
    args.add("-ta");
    args.add(String.valueOf(ta));
    args.add("-tr");
    args.add(String.valueOf(tr));
    args.add("-ti");
    args.add(String.valueOf(ti));
    args.add("-tm");
    args.add(String.valueOf(tm));
    args.add("-tn");
    args.add(String.valueOf(tn));
    args.add("-tj");
    args.add(String.valueOf(tj));
    args.add("-tt");
    args.add(String.valueOf(tt));
    args.add("-d");
    args.add(String.valueOf(d));
    args.add("-wr");
    args.add(String.valueOf(wr));
    args.add("-ws");
    args.add(String.valueOf(ws));
    args.add("-nb");
    args.add(String.valueOf(nb));

    if(lvl) args.add("--lvl");
    if(readLevels) args.add("--readLevels");
    if(cpp_harshenBoundsOnTie) args.add("--cpp_harshenBoundsOnTie");
    if(cpp_cmp_output_donotInvertSortOrder) args.add("--cpp_cmp_output_donotInvertSortOrder");
    if(cpp_seedParents) args.add("--cpp_seedParents");
    if(cmp_score_mustNotWinMajority) args.add("--cmp_score_mustNotWinMajority");
    if(score_donotTossSingleRatioParents) args.add("--score_donotTossSingleRatioParents");
    if(output_donotTossChangedInfluenceSingleParents) args.add("--output_donotTossChangedInfluenceSingleParents");
    if(binNumbers) args.add("-binNumbers");
    if(noSUCC) args.add("-noSUCC");
    if(PRED) args.add("-PRED");
    if(basicFBP) args.add("-basicFBP");
    args.add(directory);
    return args.toArray(new String[args.size()]);
  }

  /**
   * Writes the learn properties file associated that can be used in iBioSim. 
   * 
   * @param learn - the object that contains the parameters for the learn procedure.
   * @throws IOException - if a problem occurs with the reading/write of learn files.
   */
  public static void writeLearnFile(Learn learn) throws IOException
  {
    if(learn == null)
    {
      return;
    }
    
    String filename = learn.directory + File.separator + "learn.lrn";
    File[] list = new File(learn.directory).listFiles();
    
    Properties prop = new Properties();
    File learnFile = new File(filename);
    
    if(learnFile.exists())
    {
      FileInputStream in = new FileInputStream(learnFile);
      prop.load(in);
      in.close();
    }
    
    prop.setProperty("genenet.file", filename);
    prop.setProperty("genenet.Tn", String.valueOf(learn.tn));
    prop.setProperty("genenet.Tj", String.valueOf(learn.tj));
    prop.setProperty("genenet.Ti", String.valueOf(learn.ti));
    prop.setProperty("genenet.Ta", String.valueOf(learn.ta));
    prop.setProperty("genenet.Tr", String.valueOf(learn.tr));
    prop.setProperty("genenet.Tm", String.valueOf(learn.tm));
    prop.setProperty("genenet.Tt", String.valueOf(learn.tt));
    prop.setProperty("genenet.bins", String.valueOf(learn.binNumbers));
    prop.setProperty("genenet.debug", String.valueOf(learn.binNumbers));
    if (learn.binNumbers)
    {
      prop.setProperty("genenet.equal", "spacing");
    }
    else
    {
      prop.setProperty("genenet.equal", "data");
    }
    if (learn.lvl)
    {
      prop.setProperty("genenet.use", "user");
    }
    else
    {
      prop.setProperty("genenet.use", "auto");
    }
    if (learn.noSUCC)
    {
      prop.setProperty("genenet.data.type", "succ");
    }
    else if (learn.PRED)
    {
      prop.setProperty("genenet.data.type", "pred");
    }
    else
    {
      prop.setProperty("genenet.data.type", "both");
    }
    if (learn.basicFBP)
    {
      prop.setProperty("genenet.find.base.prob", "true");
    }
    else
    {
      prop.setProperty("genenet.find.base.prob", "false");
    }

    for(File file : list)
    {
      String fullPath = file.getPath();
      String name = file.getName();
      if(name.endsWith(".tsd"))
      {
        prop.setProperty(fullPath, name);
      }
    }
    
    FileOutputStream out = new FileOutputStream(new File(filename));
    prop.store(out, filename);
    out.close();
  }


  /**
   * Writes a default levels file.
   * 
   * @param directory - the directory of the project
   * @param species - the interesting species of the regulatory network
   * @param bins - number of bins
   * @throws IOException - if something wrong happens when writing the levels file.
   */
  public static void writeLevelsFile(String directory, List<String> species, int bins) throws IOException
  {
    FileWriter write = new FileWriter(new File(directory + File.separator + "levels.lvl"));
    final String time = "time, 0\n";
    write.write(time);
    for (int i = 0; i < species.size(); i++)
    {
      write.write(species.get(0));

      write.write(", " + bins);

      for(int j = 1; j < bins; j++)
      {
        write.write(", " + -1);
      }
      write.write("\n");
    }
    write.close();
  }

  /**
   * Write background file that informs GeneNet of known prior information about
   * the regulatory networks.
   * 
   * @param learnFile - the sbml file that the background file is generated from.
   * @param directory - the directory the background file is saved to
   * @return a list of interesting species.
   * 
   * @throws XMLStreamException - when reading bad formatted sbml file
   * @throws IOException - when something wrong happens when saving the background file.
   */
  public static List<String> writeBackgroundFile(String learnFile, String directory) throws XMLStreamException, IOException
  {
    ArrayList<String> speciesList = new ArrayList<String>();
    if ((learnFile.contains(".sbml")) || (learnFile.contains(".xml")))
    {
      SBMLDocument document = SBMLutilities.readSBML(learnFile);
      Model model = document.getModel();
      FileWriter write = new FileWriter(new File(directory + File.separator + "background.gcm"));
      write.write("digraph G {\n");
      for (int i = 0; i < model.getSpeciesCount(); i++)
      {
        Species species = model.getSpecies(i);
        if (BioModel.isPromoterSpecies(species))
        {
          continue;
        }
        speciesList.add(species.getId());
        write.write(species.getId() + " [shape=ellipse,color=black,label=\"" + species.getId() + "\"" + "];\n");
      }
      for (int i = 0; i < model.getReactionCount(); i++)
      {
        Reaction r = model.getReaction(i);
        if (BioModel.isProductionReaction(r))
        {
          for (int j = 0; j < r.getModifierCount(); j++)
          {
            ModifierSpeciesReference modifier = r.getModifier(j);
            if (BioModel.isNeutral(modifier))
            {
              for (int k = 0; k < r.getProductCount(); k++)
              {
                SpeciesReference product = r.getProduct(k);
                write.write(modifier.getSpecies() + " -> " + product.getSpecies() + " [arrowhead=diamond];\n");
              }
            }
            if (BioModel.isActivator(modifier))
            {
              for (int k = 0; k < r.getProductCount(); k++)
              {
                SpeciesReference product = r.getProduct(k);
                write.write(modifier.getSpecies() + " -> " + product.getSpecies() + " [arrowhead=vee];\n");
              }
            }
            if (BioModel.isRepressor(modifier))
            {
              for (int k = 0; k < r.getProductCount(); k++)
              {
                SpeciesReference product = r.getProduct(k);
                write.write(modifier.getSpecies() + " -> " + product.getSpecies() + " [arrowhead=tee];\n");
              }
            }
            if (BioModel.isRegulator(modifier))
            {
              for (int k = 0; k < r.getProductCount(); k++)
              {
                SpeciesReference product = r.getProduct(k);
                write.write(modifier.getSpecies() + " -> " + product.getSpecies() + " [arrowhead=tee];\n");
                write.write(modifier.getSpecies() + " -> " + product.getSpecies() + " [arrowhead=vee];\n");
              }
            }
          }
        }
      }
      write.write("}\n");
      write.close();
    } 
    else
    {
      BioModel bioModel = new BioModel(directory);
      bioModel.load(learnFile);
      speciesList = bioModel.getSpecies();
      FileWriter write = new FileWriter(new File(directory + File.separator + "background.gcm"));
      BufferedReader input = new BufferedReader(new FileReader(new File(learnFile)));
      String line = null;
      while ((line = input.readLine()) != null)
      {
        write.write(line + "\n");
      }
      write.close();
      input.close();

    }

    return speciesList;
  }
  
  @Override
  public void update(Message message) 
  {
    System.out.println(message);
  }


 
}


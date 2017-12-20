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
package edu.utah.ece.async.ibiosim.analysis.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.jlibsedml.AbstractTask;
import org.jlibsedml.OneStep;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Simulation;
import org.jlibsedml.UniformTimeCourse;

import edu.utah.ece.async.ibiosim.analysis.util.SEDMLutilities;
import edu.utah.ece.async.lema.verification.lpn.properties.AbstractionProperty;

/**
 * 
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public class AnalysisPropertiesLoader {

  /**
   * Load a given SED-ML task to an {@link AnalysisProperties}.
   * 
   * @param sedmlDoc - a SED-ML object
   * @param subTask - the task id being loaded
   * @param properties - the properties object that the SED-ML is being loaded to.
   */
  public static void loadSEDML(SEDMLDocument sedmlDoc, String subTask, AnalysisProperties properties)
  {
    SedML sedml = sedmlDoc.getSedMLModel();
    String simName = properties.getId();

    String taskId = simName;
    if (subTask!=null && !subTask.equals("")) 
    {
      taskId = taskId + "__" + subTask;
    }
    
    for (AbstractTask task : sedml.getTasks())
    {
      String prefix = simName+"__";
      if (task.getId().startsWith(prefix)) 
      {
       properties.addTask(task.getId().substring(prefix.length()));
      }
    }
    
    AbstractTask task = sedml.getTaskWithId(taskId);
    if (task != null) 
    {
      Simulation simulation = sedml.getSimulation(task.getSimulationReference());
      PropertiesUtil.setAlgorithm(simulation.getAlgorithm(), properties);
      SimulationProperties simProperties =  properties.getSimulationProperties();
      if (simulation instanceof UniformTimeCourse) 
      {
        UniformTimeCourse utcSimulation = (UniformTimeCourse) simulation;
        String printInterval = SEDMLutilities.getSEDBaseAnnotation(simulation, "printInterval", "Print_Interval", null);
        if (printInterval != null)
        {
          simProperties.setPrintInterval(PropertiesUtil.parseDouble(printInterval));
        } 
        else 
        {
          printInterval = SEDMLutilities.getSEDBaseAnnotation(simulation, "printInterval", "Minimum_Print_Interval", null);
          if (printInterval != null) 
          {
            simProperties.setMinTimeStep(PropertiesUtil.parseDouble(printInterval));
          } else 
          {
            simProperties.setNumSteps(utcSimulation.getNumberOfPoints());
          }
        }
        simProperties.setInitialTime(utcSimulation.getInitialTime());
        simProperties.setOutputStartTime(utcSimulation.getOutputStartTime());
        simProperties.setTimeLimit(utcSimulation.getOutputEndTime());
      } else if (simulation instanceof OneStep) {
        OneStep osSimulation = (OneStep) simulation;
        simProperties.setInitialTime(0);
        simProperties.setOutputStartTime(0);
        simProperties.setTimeLimit(osSimulation.getStep());
        simProperties.setPrintInterval(1.);
      }

    }
  }

  /**
   * Loads a properties files to an {@link AnalysisProperties} object.
   * 
   * @throws IOException - if there is a problem when reading in the given property file.
   */
  public static void loadPropertiesFile(AnalysisProperties properties) throws IOException
  {
    String root = properties.getRoot();
    String simName = properties.getId();
    String openFile = root + File.separator + simName + File.separator + simName + ".properties";
    String modelName = properties.getModelFile();

    VerificationProperties verifProperties = properties.getVerificationProperties();
    SimulationProperties simProperties = properties.getSimulationProperties();
    AdvancedProperties advProperties = properties.getAdvancedProperties();
    
    if (!(new File(openFile)).exists())
    {
      openFile = root + File.separator + simName + File.separator + modelName + ".properties";
      if (!(new File(openFile)).exists())
      {
        return;
      }
    }
    Properties load = new Properties();
    if (!openFile.equals(""))
    {
      FileInputStream in = new FileInputStream(new File(openFile));
      load.load(in);
      in.close();
      ArrayList<String> loadProperties = new ArrayList<String>();
      for (Object key : load.keySet())
      {
        String type = key.toString().substring(0, key.toString().indexOf('.'));
        if (type.equals("gcm"))
        {
          loadProperties.add(key.toString() + "=" + load.getProperty(key.toString()));
        }
        else if (key.equals("reb2sac.abstraction.method.0.1"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.0.1").equals("enzyme-kinetic-qssa-1"))
          {
            loadProperties.add("reb2sac.abstraction.method.0.1=" + load.getProperty("reb2sac.abstraction.method.0.1"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.0.2"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.0.2").equals("reversible-to-irreversible-transformer"))
          {
            loadProperties.add("reb2sac.abstraction.method.0.2=" + load.getProperty("reb2sac.abstraction.method.0.2"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.0.3"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.0.3").equals("multiple-products-reaction-eliminator"))
          {
            loadProperties.add("reb2sac.abstraction.method.0.3=" + load.getProperty("reb2sac.abstraction.method.0.3"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.0.4"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.0.4").equals("multiple-reactants-reaction-eliminator"))
          {
            loadProperties.add("reb2sac.abstraction.method.0.4=" + load.getProperty("reb2sac.abstraction.method.0.4"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.0.5"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.0.5").equals("single-reactant-product-reaction-eliminator"))
          {
            loadProperties.add("reb2sac.abstraction.method.0.5=" + load.getProperty("reb2sac.abstraction.method.0.5"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.0.6"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.0.6").equals("dimer-to-monomer-substitutor"))
          {
            loadProperties.add("reb2sac.abstraction.method.0.6=" + load.getProperty("reb2sac.abstraction.method.0.6"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.0.7"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.0.7").equals("inducer-structure-transformer"))
          {
            loadProperties.add("reb2sac.abstraction.method.0.7=" + load.getProperty("reb2sac.abstraction.method.0.7"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.1.1"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.1.1").equals("modifier-structure-transformer"))
          {
            loadProperties.add("reb2sac.abstraction.method.1.1=" + load.getProperty("reb2sac.abstraction.method.1.1"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.1.2"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.1.2").equals("modifier-constant-propagation"))
          {
            loadProperties.add("reb2sac.abstraction.method.1.2=" + load.getProperty("reb2sac.abstraction.method.1.2"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.2.1"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.2.1").equals("operator-site-forward-binding-remover"))
          {
            loadProperties.add("reb2sac.abstraction.method.2.1=" + load.getProperty("reb2sac.abstraction.method.2.1"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.2.3"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.2.3").equals("enzyme-kinetic-rapid-equilibrium-1"))
          {
            loadProperties.add("reb2sac.abstraction.method.2.3=" + load.getProperty("reb2sac.abstraction.method.2.3"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.2.4"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.2.4").equals("irrelevant-species-remover"))
          {
            loadProperties.add("reb2sac.abstraction.method.2.4=" + load.getProperty("reb2sac.abstraction.method.2.4"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.2.5"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.2.5").equals("inducer-structure-transformer"))
          {
            loadProperties.add("reb2sac.abstraction.method.2.5=" + load.getProperty("reb2sac.abstraction.method.2.5"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.2.6"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.2.6").equals("modifier-constant-propagation"))
          {
            loadProperties.add("reb2sac.abstraction.method.2.6=" + load.getProperty("reb2sac.abstraction.method.2.6"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.2.7"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.2.7").equals("similar-reaction-combiner"))
          {
            loadProperties.add("reb2sac.abstraction.method.2.7=" + load.getProperty("reb2sac.abstraction.method.2.7"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.2.8"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.2.8").equals("modifier-constant-propagation"))
          {
            loadProperties.add("reb2sac.abstraction.method.2.8=" + load.getProperty("reb2sac.abstraction.method.2.8"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.2.2"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.2.2").equals("dimerization-reduction") && !load.getProperty("reb2sac.abstraction.method.2.2").equals("dimerization-reduction-level-assignment"))
          {
            loadProperties.add("reb2sac.abstraction.method.2.2=" + load.getProperty("reb2sac.abstraction.method.2.2"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.3.1"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.3.1").equals("kinetic-law-constants-simplifier") && !load.getProperty("reb2sac.abstraction.method.3.1").equals("reversible-to-irreversible-transformer")
              && !load.getProperty("reb2sac.abstraction.method.3.1").equals("nary-order-unary-transformer"))
          {
            loadProperties.add("reb2sac.abstraction.method.3.1=" + load.getProperty("reb2sac.abstraction.method.3.1"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.3.2"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.3.2").equals("kinetic-law-constants-simplifier") && !load.getProperty("reb2sac.abstraction.method.3.2").equals("modifier-constant-propagation"))
          {
            loadProperties.add("reb2sac.abstraction.method.3.2=" + load.getProperty("reb2sac.abstraction.method.3.2"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.3.3"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.3.3").equals("absolute-inhibition-generator"))
          {
            loadProperties.add("reb2sac.abstraction.method.3.3=" + load.getProperty("reb2sac.abstraction.method.3.3"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.3.4"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.3.4").equals("final-state-generator"))
          {
            loadProperties.add("reb2sac.abstraction.method.3.4=" + load.getProperty("reb2sac.abstraction.method.3.4"));
          }
        }
        else if (key.equals("reb2sac.abstraction.method.3.5"))
        {
          if (!load.getProperty("reb2sac.abstraction.method.3.5").equals("stop-flag-generator"))
          {
            loadProperties.add("reb2sac.abstraction.method.3.5=" + load.getProperty("reb2sac.abstraction.method.3.5"));
          }
        }
        else if (key.equals("reb2sac.nary.order.decider"))
        {
          if (!load.getProperty("reb2sac.nary.order.decider").equals("distinct"))
          {
            loadProperties.add("reb2sac.nary.order.decider=" + load.getProperty("reb2sac.nary.order.decider"));
          }
        }
        else if (key.equals("simulation.printer"))
        {
          if (!load.getProperty("simulation.printer").equals("tsd.printer"))
          {
            loadProperties.add("simulation.printer=" + load.getProperty("simulation.printer"));
          }
        }
        else if (key.equals("simulation.printer.tracking.quantity"))
        {
          if (!load.getProperty("simulation.printer.tracking.quantity").equals("amount"))
          {
            loadProperties.add("simulation.printer.tracking.quantity=" + load.getProperty("simulation.printer.tracking.quantity"));
          }
        }
        else if (key.equals("selected.simulator"))
        {
        }
        else if (key.equals("file.stem"))
        {
        }
        else if (((String) key).length() > 36 && ((String) key).substring(0, 37).equals("simulation.run.termination.condition."))
        {
        }
        else if (((String) key).length() > 37 && ((String) key).substring(0, 38).equals("reb2sac.absolute.inhibition.threshold."))
        {
        }
        else if (((String) key).length() > 27 && ((String) key).substring(0, 28).equals("reb2sac.concentration.level."))
        {
        }
        else if (((String) key).length() > 19 && ((String) key).substring(0, 20).equals("reb2sac.final.state."))
        {
        }
        else if (key.equals("reb2sac.analysis.stop.enabled"))
        {
        }
        else if (key.equals("reb2sac.analysis.stop.rate"))
        {
        }
        else if (key.equals("monte.carlo.simulation.start.index"))
        {
        }
        else if (key.equals("abstraction.interesting"))
        {
          String intVars = load.getProperty("abstraction.interesting");
          String[] array = intVars.split(" ");
          for (String s : array)
          {
            if (!s.equals(""))
            {
              verifProperties.addAbstractInteresting(s);
            }
          }
        }
        else if (key.equals("abstraction.factor"))
        {
          verifProperties.setFactorField(load.getProperty("abstraction.factor"));
        }
        else if (key.equals("abstraction.iterations"))
        {
          verifProperties.setIterField(load.getProperty("abstraction.iterations"));
        }
        else if (key.toString().startsWith("abstraction.transform"))
        {
          continue;
        }
        else
        {
          loadProperties.add(key + "=" + load.getProperty((String) key));
        }
      }
      HashMap<Integer, String> preOrder = new HashMap<Integer, String>();
      HashMap<Integer, String> loopOrder = new HashMap<Integer, String>();
      HashMap<Integer, String> postOrder = new HashMap<Integer, String>();
      HashMap<String, Boolean> containsXform = new HashMap<String, Boolean>();
      boolean containsAbstractions = false;
      if (properties.getVerificationProperties().getAbsProperty() != null)
      {
        AbstractionProperty abs = verifProperties.getAbsProperty();
        for (String s : abs.transforms)
        {
          if (load.containsKey("abstraction.transform." + s))
          {
            if (load.getProperty("abstraction.transform." + s).contains("preloop"))
            {
              Pattern prePattern = Pattern.compile("preloop(\\d+)");
              Matcher intMatch = prePattern.matcher(load.getProperty("abstraction.transform." + s));
              if (intMatch.find())
              {
                Integer index = Integer.parseInt(intMatch.group(1));
                preOrder.put(index, s);
              }
              else
              {
                if (!abs.preAbsModel.contains(s)) {
                  abs.preAbsModel.addElement(s);
                }
              }
            }
            else
            {
              abs.preAbsModel.removeElement(s);
            }
            if (load.getProperty("abstraction.transform." + s).contains("mainloop"))
            {
              Pattern loopPattern = Pattern.compile("mainloop(\\d+)");
              Matcher intMatch = loopPattern.matcher(load.getProperty("abstraction.transform." + s));
              if (intMatch.find())
              {
                Integer index = Integer.parseInt(intMatch.group(1));
                loopOrder.put(index, s);
              }
              else
              {
                verifProperties.getAbsProperty().loopAbsModel.addElement(s);
              }
            }
            else
            {
              abs.loopAbsModel.removeElement(s);
            }
            if (load.getProperty("abstraction.transform." + s).contains("postloop"))
            {
              Pattern postPattern = Pattern.compile("postloop(\\d+)");
              Matcher intMatch = postPattern.matcher(load.getProperty("abstraction.transform." + s));
              if (intMatch.find())
              {
                Integer index = Integer.parseInt(intMatch.group(1));
                postOrder.put(index, s);
              }
              else
              {
                if (!abs.postAbsModel.contains(s)) {
                  abs.postAbsModel.addElement(s);
                }
              }
            }
            else
            {
              abs.postAbsModel.removeElement(s);
            }
          }
          else if (containsAbstractions && !containsXform.get(s))
          {
            abs.preAbsModel.removeElement(s);
            abs.loopAbsModel.removeElement(s);
            abs.postAbsModel.removeElement(s);
          }
        }
        if (preOrder.size() > 0)
        {
          abs.preAbsModel.removeAllElements();
        }
        for (Integer j = 0; j < preOrder.size(); j++)
        {
          abs.preAbsModel.addElement(preOrder.get(j));
        }
        if (loopOrder.size() > 0)
        {
          abs.loopAbsModel.removeAllElements();
        }
        for (Integer j = 0; j < loopOrder.size(); j++)
        {
          abs.loopAbsModel.addElement(loopOrder.get(j));
        }
        if (postOrder.size() > 0)
        {
          abs.postAbsModel.removeAllElements();
        }
        for (Integer j = 0; j < postOrder.size(); j++)
        {
          abs.postAbsModel.addElement(postOrder.get(j));
        }
      }
      if (load.getProperty("reb2sac.abstraction.method").equals("none"))
      {
        properties.setNone();
      }
      else if (load.getProperty("reb2sac.abstraction.method").equals("expand"))
      {
        properties.setExpand();
      }
      else if (load.getProperty("reb2sac.abstraction.method").equals("abs"))
      {
        properties.setAbs();
      }
      else
      {
        properties.setNary();
      }

      if (load.containsKey("file.stem"))
      {
      }
      if (load.containsKey("ode.simulation.absolute.error"))
      {
        simProperties.setAbsError(PropertiesUtil.parseDouble(load.getProperty("ode.simulation.absolute.error")));
      }
      else
      {
        simProperties.setAbsError(1.0E-9);
      }
      if (load.containsKey("ode.simulation.relative.error"))
      {
        simProperties.setRelError(PropertiesUtil.parseDouble(load.getProperty("ode.simulation.relative.error")));
      }
      else
      {
        simProperties.setRelError(0.);
      }
      if (load.containsKey("monte.carlo.simulation.time.step"))
      {
        simProperties.setMaxTimeStep(PropertiesUtil.parseDouble(load.getProperty("monte.carlo.simulation.time.step")));
      }
      else
      {
        simProperties.setMaxTimeStep(Double.POSITIVE_INFINITY);
      }
      if (load.containsKey("monte.carlo.simulation.min.time.step"))
      {
        simProperties.setMinTimeStep(PropertiesUtil.parseDouble(load.getProperty("monte.carlo.simulation.min.time.step")));
      }
      else
      {
        simProperties.setMinTimeStep(0.);
      }
      if (load.containsKey("monte.carlo.simulation.time.limit"))
      {
        simProperties.setTimeLimit(PropertiesUtil.parseDouble(load.getProperty("monte.carlo.simulation.time.limit")));
      }
      else
      {
        simProperties.setTimeLimit(100.);
      }
      if (load.containsKey("simulation.initial.time"))
      {
        simProperties.setInitialTime(PropertiesUtil.parseDouble(load.getProperty("simulation.initial.time")));
      }
      else
      {
        simProperties.setInitialTime(0.);
      }
      if (load.containsKey("simulation.output.start.time"))
      {
        simProperties.setOutputStartTime(PropertiesUtil.parseDouble(load.getProperty("simulation.output.start.time")));
      }
      else
      {
        simProperties.setOutputStartTime(0.);
      }
      if (load.containsKey("monte.carlo.simulation.print.interval"))
      {
        simProperties.setPrintInterval(PropertiesUtil.parseDouble(load.getProperty("monte.carlo.simulation.print.interval")));

      }
      else if (load.containsKey("monte.carlo.simulation.minimum.print.interval"))
      {
        simProperties.setMinTimeStep(PropertiesUtil.parseDouble(load.getProperty("monte.carlo.simulation.minimum.print.interval")));

      }
      else if (load.containsKey("monte.carlo.simulation.number.steps"))
      {
        simProperties.setNumSteps(Integer.parseInt(load.getProperty("monte.carlo.simulation.number.steps")));
      }
      else
      {
        simProperties.setPrintInterval(1.);
      }
      if (load.containsKey("monte.carlo.simulation.random.seed"))
      {
        simProperties.setRndSeed(Long.parseLong(load.getProperty("monte.carlo.simulation.random.seed")));
      }
      if (load.containsKey("monte.carlo.simulation.runs"))
      {
        simProperties.setRun(Integer.parseInt(load.getProperty("monte.carlo.simulation.runs")));
      }
      if (load.containsKey("simulation.time.series.species.level.file"))
      {
        // usingSSA.doClick();
      }
      if (load.containsKey("simulation.printer.tracking.quantity"))
      {
        simProperties.setPrinter_track_quantity(load.getProperty("simulation.printer.tracking.quantity"));
      }
      if (load.containsKey("simulation.printer"))
      {
        simProperties.setPrinter_id(load.getProperty("simulation.printer"));
      }
      if (load.containsKey("reb2sac.simulation.method"))
      {
        if (load.getProperty("reb2sac.simulation.method").equals("ODE"))
        {
          properties.setOde();
        }
        else if (load.getProperty("reb2sac.simulation.method").equals("monteCarlo"))
        {
          properties.setSsa();
          String simId = properties.getSim();
          if(simId == null)
          {
            
          }
          else if (simId.equals("mpde"))
          {
            properties.getIncrementalProperties().setMpde(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(1);
          }
          else if (simId.equals("mean_path"))
          {
            properties.getIncrementalProperties().setMeanPath(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(1);
          }
          else if (simId.equals("median_path"))
          {
            properties.getIncrementalProperties().setMedianPath(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(1);
          }
          else if (simId.equals("mean_path-bifurcation"))
          {

            properties.getIncrementalProperties().setMeanPath(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(2);
          }
          else if (simId.equals("median_path-bifurcation"))
          {
            properties.getIncrementalProperties().setMedianPath(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(2);
          }
          else if (simId.equals("mean_path-adaptive"))
          {
            properties.getIncrementalProperties().setMeanPath(true);
            properties.getIncrementalProperties().setAdaptive(true);
            properties.getIncrementalProperties().setNumPaths(1);
          }
          else if (simId.equals("median_path-adaptive"))
          {

            properties.getIncrementalProperties().setMedianPath(true);
            properties.getIncrementalProperties().setAdaptive(true);
            properties.getIncrementalProperties().setNumPaths(1);
          }
          else if (simId.equals("mean_path-adaptive-bifurcation"))
          {
            properties.getIncrementalProperties().setMeanPath(true);
            properties.getIncrementalProperties().setAdaptive(true);
            properties.getIncrementalProperties().setNumPaths(2);
          }
          else if (simId.equals("median_path-adaptive-bifurcation"))
          {
            properties.getIncrementalProperties().setMedianPath(true);
            properties.getIncrementalProperties().setAdaptive(true);
            properties.getIncrementalProperties().setNumPaths(2);
          }
          else if (simId.equals("mean_path-event"))
          {
            properties.getIncrementalProperties().setMeanPath(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(1);
          }
          else if (simId.equals("median_path-event"))
          {

            properties.getIncrementalProperties().setMedianPath(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(1);
          }
          else if (simId.equals("mean_path-event-bifurcation"))
          {

            properties.getIncrementalProperties().setMeanPath(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(2);
          }
          else if (simId.equals("median_path-event-bifurcation"))
          {
            properties.getIncrementalProperties().setMedianPath(true);
            properties.getIncrementalProperties().setAdaptive(false);
            properties.getIncrementalProperties().setNumPaths(2);
          }

        }
        else if (load.getProperty("reb2sac.simulation.method").equals("markov"))
        {
          properties.setMarkov();
        }
        else if (load.getProperty("reb2sac.simulation.method").equals("FBA"))
        {
          properties.setFba();
          // absErr.setEnabled(false);
        }
        else if (load.getProperty("reb2sac.simulation.method").equals("SBML"))
        {
          properties.setSbml();
        }
        else if (load.getProperty("reb2sac.simulation.method").equals("Network"))
        {
          properties.setDot();
        }
        else if (load.getProperty("reb2sac.simulation.method").equals("Browser"))
        {
          properties.setXhtml();
        }
        else if (load.getProperty("reb2sac.simulation.method").equals("LPN"))
        {
          properties.setLhpn();
        }
      }
      if (load.containsKey("reb2sac.abstraction.method"))
      {
        if (load.getProperty("reb2sac.abstraction.method").equals("none"))
        {
          properties.setNone();
        }
        else if (load.getProperty("reb2sac.abstraction.method").equals("expand"))
        {
          properties.setExpand();
        }
        else if (load.getProperty("reb2sac.abstraction.method").equals("abs"))
        {
          properties.setAbs();
        }
        else if (load.getProperty("reb2sac.abstraction.method").equals("nary"))
        {
          properties.setNary();
        }
      }
      if (load.containsKey("selected.property"))
      {
        //          if (transientProperties != null)
        //          {
        //            transientProperties.setSelectedItem(load.getProperty("selected.property"));
        //          }
      }
      ArrayList<String> getLists = new ArrayList<String>();
      int i = 1;
      while (load.containsKey("simulation.run.termination.condition." + i))
      {
        getLists.add(load.getProperty("simulation.run.termination.condition." + i));
        i++;
      }
      getLists = new ArrayList<String>();
      i = 1;
      while (load.containsKey("reb2sac.interesting.species." + i))
      {
        String species = load.getProperty("reb2sac.interesting.species." + i);
        int j = 2;
        String interesting = " ";
        if (load.containsKey("reb2sac.concentration.level." + species + ".1"))
        {
          interesting += load.getProperty("reb2sac.concentration.level." + species + ".1");
        }
        while (load.containsKey("reb2sac.concentration.level." + species + "." + j))
        {
          interesting += "," + load.getProperty("reb2sac.concentration.level." + species + "." + j);
          j++;
        }
        if (!interesting.equals(" "))
        {
          species += interesting;
        }
        getLists.add(species);
        i++;
      }
      for (String s : getLists)
      {
        String[] split1 = s.split(" ");

        // load the species and its thresholds into the list of
        // interesting species
        String speciesAndThresholds = split1[0];

        if (split1.length > 1)
        {
          speciesAndThresholds += " " + split1[1];
        }

        simProperties.addIntSpecies(speciesAndThresholds);
      }

      getLists = new ArrayList<String>();
      i = 1;
      while (load.containsKey("gcm.abstraction.method." + i))
      {
        getLists.add(load.getProperty("gcm.abstraction.method." + i));
        i++;
      }
      i = 1;
      while (load.containsKey("reb2sac.abstraction.method.1." + i))
      {
        getLists.add(load.getProperty("reb2sac.abstraction.method.1." + i));
        i++;
      }
      properties.getAdvancedProperties().setPreAbs(getLists);
      getLists = new ArrayList<String>();
      i = 1;
      while (load.containsKey("reb2sac.abstraction.method.2." + i))
      {
        getLists.add(load.getProperty("reb2sac.abstraction.method.2." + i));
        i++;
      }
      properties.getAdvancedProperties().setLoopAbs(getLists);

      getLists = new ArrayList<String>();
      i = 1;
      while (load.containsKey("reb2sac.abstraction.method.3." + i))
      {
        getLists.add(load.getProperty("reb2sac.abstraction.method.3." + i));
        i++;
      }
      
      properties.getAdvancedProperties().setPostAbs(getLists);

      if (load.containsKey("reb2sac.rapid.equilibrium.condition.1"))
      {
        advProperties.setRap1(PropertiesUtil.parseDouble(load.getProperty("reb2sac.rapid.equilibrium.condition.1")));
      }
      if (load.containsKey("reb2sac.rapid.equilibrium.condition.2"))
      {
        advProperties.setRap2(PropertiesUtil.parseDouble(load.getProperty("reb2sac.rapid.equilibrium.condition.2")));
      }
      if (load.containsKey("reb2sac.qssa.condition.1"))
      {
        advProperties.setQss(PropertiesUtil.parseDouble(load.getProperty("reb2sac.qssa.condition.1")));
      }
      if (load.containsKey("reb2sac.operator.max.concentration.threshold"))
      {
        advProperties.setCon(Integer.parseInt(load.getProperty("reb2sac.operator.max.concentration.threshold")));
      }
      if (load.containsKey("reb2sac.diffusion.stoichiometry.amplification.value"))
      {
        advProperties.setStoichAmp(PropertiesUtil.parseDouble(load.getProperty("reb2sac.diffusion.stoichiometry.amplification.value")));
      }
      if (load.containsKey("reb2sac.iSSA.number.paths"))
      {
        properties.getIncrementalProperties().setNumPaths(Integer.parseInt(load.getProperty("reb2sac.iSSA.number.paths")));
      }
      if (load.containsKey("reb2sac.iSSA.type"))
      {
        String type = load.getProperty("reb2sac.iSSA.type");
        if (type.equals("mpde"))
        {
          properties.getIncrementalProperties().setMpde(true);
        }
        else if (type.equals("medianPath"))
        {
          properties.getIncrementalProperties().setMedianPath(true);
        }
        else
        {
          properties.getIncrementalProperties().setMeanPath(true);
        }
      }
      if (load.containsKey("reb2sac.iSSA.adaptive"))
      {
        String type = load.getProperty("reb2sac.iSSA.adaptive");
        if (type.equals("true"))
        {
          properties.getIncrementalProperties().setAdaptive(true);
        }
        else
        {
          properties.getIncrementalProperties().setAdaptive(false);
        }
      }
    }
    else
    {
      if (load.containsKey("selected.simulator"))
      {
        properties.setSim(load.getProperty("selected.simulator"));
      }
      if (load.containsKey("file.stem"))
      {
        properties.setFileStem(load.getProperty("file.stem"));
      }
      if (load.containsKey("simulation.printer.tracking.quantity"))
      {
       simProperties.setPrinter_track_quantity(load.getProperty("simulation.printer.tracking.quantity")); 
      }
      if (load.containsKey("simulation.printer"))
      {
        simProperties.setGenStats(load.getProperty("simulation.printer")); 
      }
    }

  }

  


}

package edu.utah.ece.async.ibiosim.analysis.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.Algorithm;
import org.jlibsedml.Annotation;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.Model;
import org.jlibsedml.OneStep;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Simulation;
import org.jlibsedml.SteadyState;
import org.jlibsedml.Task;
import org.jlibsedml.UniformTimeCourse;
import org.jlibsedml.Variable;
import org.jlibsedml.VariableSymbol;

import edu.utah.ece.async.ibiosim.analysis.util.SEDMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.lema.verification.lpn.properties.AbstractionProperty;
import static edu.utah.ece.async.ibiosim.analysis.properties.PropertiesConstants.*;
public class AnalysisPropertiesWriter {

  /**
   * 
   * @param analysisProperties
   * @throws IOException
   */
  public static void createProperties(AnalysisProperties analysisProperties) throws IOException
  {
    Properties properties = new Properties();
    if (analysisProperties.isAbs() || analysisProperties.isNary())
    {
      int gcmIndex = 1;
      if(analysisProperties.getAdvancedProperties().getPreAbs() != null)
      {
        for (int i = 0; i < analysisProperties.getAdvancedProperties().getPreAbs().size(); i++)
        {
          String abstractionOption = analysisProperties.getAdvancedProperties().getPreAbs().get(i);
          if (abstractionOption.equals(complex_formation_abstraction) || abstractionOption.equals(operator_site_abstraction))
          {
            properties.setProperty(gcm_Abstraction + gcmIndex, abstractionOption);
            gcmIndex++;
          }
          else
          {
            properties.setProperty(reb2sac_abstraction_1 + (i + 1), abstractionOption);
          }
        }
      }
      if(analysisProperties.getAdvancedProperties().getLoopAbs() != null)
      {
        for (int i = 0; i < analysisProperties.getAdvancedProperties().getLoopAbs().size(); i++)
        {
          properties.setProperty(reb2sac_abstraction_2 + (i + 1), analysisProperties.getAdvancedProperties().getLoopAbs().get(i));
        }
      }

    }
    if(analysisProperties.getAdvancedProperties().getPostAbs() != null)
    {
      for (int i = 0; i < analysisProperties.getAdvancedProperties().getPostAbs().size(); i++)
      {
        properties.setProperty(reb2sac_abstraction_3 + (i + 1), analysisProperties.getAdvancedProperties().getPostAbs().get(i));
      }
    }

    if(analysisProperties.getSimulationProperties().getPrinter_id() != null)
    {
      properties.setProperty(sim_printer, analysisProperties.getSimulationProperties().getPrinter_id());
    }
    if(analysisProperties.getSimulationProperties() != null)
    {
      properties.setProperty(sim_tracking_quantity, analysisProperties.getSimulationProperties().getPrinter_track_quantity());
    }
    if(analysisProperties.getSimulationProperties().getIntSpecies() != null)
    {
      for (int i = 0; i < analysisProperties.getSimulationProperties().getIntSpecies().size(); i++)
      {
        String species = analysisProperties.getSimulationProperties().getIntSpecies().get(i);
        if (!species.equals(""))
        {
          String[] split = species.split(" ");
          properties.setProperty(reb2sac_interesting_species + (i + 1), split[0]);
          if (split.length > 1)
          {
            String[] levels = split[1].split(",");
            for (int j = 0; j < levels.length; j++)
            {
              properties.setProperty(reb2sac_concentration_level + split[0] + "." + (j + 1), levels[j]);
            }
          }
        }
      }
    }

    properties.setProperty(reb2sac_rapid_equil_1, String.valueOf(analysisProperties.getAdvancedProperties().getRap1()));
    properties.setProperty(reb2sac_rapid_equil_2, String.valueOf(analysisProperties.getAdvancedProperties().getRap2()));
    properties.setProperty(reb2sac_qssa_1, String.valueOf(analysisProperties.getAdvancedProperties().getQss()));
    properties.setProperty(reb2sac_max_operator_threshold, String.valueOf(analysisProperties.getAdvancedProperties().getCon()));
    properties.setProperty(reb2sac_diff_stoich_ampl, String.valueOf(analysisProperties.getAdvancedProperties().getStoichAmp()));
    if(analysisProperties.getSimulationProperties().getGenStats() != null)
    {
      properties.setProperty(reb2sac_stats, analysisProperties.getSimulationProperties().getGenStats());
    }
    if (analysisProperties.isNone())
    {
      properties.setProperty(reb2sac_abstraction, "none");
    }
    if (analysisProperties.isExpand())
    {
      properties.setProperty(reb2sac_abstraction, "expand");
    }
    if (analysisProperties.isAbs())
    {
      properties.setProperty(reb2sac_abstraction, "abs");
    }
    else if (analysisProperties.isNary())
    {
      properties.setProperty(reb2sac_abstraction, "nary");
    }
    if (analysisProperties.getVerificationProperties().getAbsProperty() != null)
    {
      AbstractionProperty absProperty =  analysisProperties.getVerificationProperties().getAbsProperty();

      for (Integer i = 0; i < absProperty.preAbsModel.size(); i++)
      {
        properties.setProperty(reb2sac_transform + absProperty.preAbsModel.getElementAt(i).toString(), "preloop" + i.toString());
      }
      for (Integer i = 0; i < absProperty.loopAbsModel.size(); i++)
      {
        if (absProperty.preAbsModel.contains(absProperty.loopAbsModel.getElementAt(i)))
        {
          String value = properties.getProperty(reb2sac_transform + absProperty.loopAbsModel.getElementAt(i).toString());
          value = value + "mainloop" + i.toString();
          properties.setProperty(reb2sac_transform + absProperty.loopAbsModel.getElementAt(i).toString(), value);
        }
        else
        {
          properties.setProperty(reb2sac_transform + absProperty.loopAbsModel.getElementAt(i).toString(), "mainloop" + i.toString());
        }
      }
      for (Integer i = 0; i < absProperty.postAbsModel.size(); i++)
      {
        if (absProperty.preAbsModel.contains(absProperty.postAbsModel.getElementAt(i)) || absProperty.preAbsModel.contains(absProperty.postAbsModel.get(i)))
        {
          String value = properties.getProperty(reb2sac_transform + absProperty.postAbsModel.getElementAt(i).toString());
          value = value + "postloop" + i.toString();
          properties.setProperty(reb2sac_transform + absProperty.postAbsModel.getElementAt(i).toString(), value);
        }
        else
        {
          properties.setProperty(reb2sac_transform + absProperty.postAbsModel.getElementAt(i).toString(), "postloop" + i.toString());
        }
      }
      for (String s : absProperty.transforms)
      {
        if (!absProperty.preAbsModel.contains(s) && !absProperty.loopAbsModel.contains(s) && !absProperty.postAbsModel.contains(s))
        {
          properties.remove(s);
        }
      }
    }
    if (analysisProperties.isOde())
    {
      properties.setProperty(reb2sac_simulation, "ODE");
    }
    else if (analysisProperties.isSsa())
    {
      properties.setProperty(reb2sac_simulation, "monteCarlo");
      properties.setProperty(reb2sac_issa_paths, String.valueOf(analysisProperties.getIncrementalProperties().getNumPaths()));
      if (analysisProperties.getIncrementalProperties().isMpde())
      {
        properties.setProperty(reb2sac_issa_type, "mpde");
      }
      else if (analysisProperties.getIncrementalProperties().isMeanPath())
      {
        properties.setProperty(reb2sac_issa_type, "meanPath");
      }
      else
      {
        properties.setProperty(reb2sac_issa_type, "medianPath");
      }
      if (analysisProperties.getIncrementalProperties().isAdaptive())
      {
        properties.setProperty(reb2sac_issa_adaptive, "true");
      }
      else
      {
        properties.setProperty(reb2sac_issa_adaptive, "false");
      }
    }
    else if (analysisProperties.isMarkov())
    {
      properties.setProperty(reb2sac_simulation, "markov");
    }
    else if (analysisProperties.isFba())
    {
      properties.setProperty(reb2sac_simulation, "FBA");
    }
    else if (analysisProperties.isSbml())
    {
      properties.setProperty(reb2sac_simulation, "SBML");
    }
    else if (analysisProperties.isDot())
    {
      properties.setProperty(reb2sac_simulation, "Network");
    }
    else if (analysisProperties.isXhtml())
    {
      properties.setProperty(reb2sac_simulation, "Browser");
    }
    else if (analysisProperties.isLhpn())
    {
      properties.setProperty(reb2sac_simulation, "LPN");
    }
    if (!analysisProperties.isSsa())
    {
      properties.setProperty(sim_init_time, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getInitialTime()));
      properties.setProperty(sim_out_time,  PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getOutputStartTime()));
      properties.setProperty(ode_time_limit, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getTimeLimit()));
      if (analysisProperties.isPrintInterval())
      {
        properties.setProperty(ode_print_interval , PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getPrintInterval()));
      }
      else if (analysisProperties.isMinPrintInterval())
      {
        properties.setProperty(ode_min_print_interval, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getPrintInterval()));
      }
      else
      {
        properties.setProperty(ode_number_steps, String.valueOf(((int) analysisProperties.getSimulationProperties().getPrintInterval())));
      }
      if (analysisProperties.getSimulationProperties().getTimeStep() == Double.MAX_VALUE)
      {
        properties.setProperty(ode_time_step, "inf");
      }
      else
      {
        properties.setProperty(ode_time_step, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getTimeStep()));
      }
      properties.setProperty(ode_min_time_step, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getMinTimeStep()));
      properties.setProperty(ode_abs_error, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getAbsError()));
      properties.setProperty(ode_rel_error, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getRelError()));
      properties.setProperty(ode_out_dir, analysisProperties.getOutDir());
      properties.setProperty(mc_seed, String.valueOf(analysisProperties.getSimulationProperties().getRndSeed()));
      properties.setProperty(mc_runs, String.valueOf(analysisProperties.getSimulationProperties().getRun()));
    }
    if (!analysisProperties.isOde())
    {
      properties.setProperty(sim_init_time, PropertiesUtil.parseDouble( analysisProperties.getSimulationProperties().getInitialTime()));
      properties.setProperty(sim_out_time, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getOutputStartTime()));
      properties.setProperty(mc_time_limit, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getTimeLimit()));
      if (analysisProperties.isPrintInterval())
      {
        properties.setProperty(mc_print_interval , PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getPrintInterval()));
      }
      else if (analysisProperties.isMinPrintInterval())
      {
        properties.setProperty(mc_min_print_interval, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getPrintInterval()));
      }
      else
      {
        properties.setProperty(mc_number_steps, "" + ((int) analysisProperties.getSimulationProperties().getPrintInterval()));
      }
      if (analysisProperties.getSimulationProperties().getTimeStep() == Double.MAX_VALUE)
      {
        properties.setProperty(mc_time_step, "inf");
      }
      else
      {
        properties.setProperty(mc_time_step, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getTimeStep()));
      }
      properties.setProperty(mc_min_time_step, PropertiesUtil.parseDouble(analysisProperties.getSimulationProperties().getMinTimeStep()));
      properties.setProperty(mc_seed, String.valueOf(analysisProperties.getSimulationProperties().getRndSeed()));
      properties.setProperty(mc_runs, String.valueOf(analysisProperties.getSimulationProperties().getRun()));
      properties.setProperty(ode_out_dir, analysisProperties.getOutDir());
    }
    properties.setProperty(sim_run_term , "constraint");


    FileOutputStream store = new FileOutputStream(new File(analysisProperties.getPropertiesName()));
    properties.store(store, analysisProperties.getId() + " Properties");
    store.close();
  }

  public static void saveSEDML(SEDMLDocument sedmlDoc, AnalysisProperties properties) {
    double initialTime = properties.getSimulationProperties().getInitialTime();
    double outputStartTime = properties.getSimulationProperties().getOutputStartTime();
    double timeLimit = properties.getSimulationProperties().getTimeLimit();
    double printInterval = properties.getSimulationProperties().getPrintInterval();
    int numberOfSteps;
    String fileStem = properties.getFileStem();
    String simName = properties.getSim();
    if (properties.isNumSteps())
    {
      numberOfSteps = properties.getSimulationProperties().getNumSteps();
    }
    else
    {
      numberOfSteps = (int) Math.floor(timeLimit / printInterval) + 1;
    }
    SedML sedml = sedmlDoc.getSedMLModel();
    String taskId = properties.getId();
    if (!fileStem.trim().equals("")) {
      taskId = properties.getId() + "__" + fileStem.trim();
    }
    AbstractTask task = sedml.getTaskWithId(taskId);
    Simulation simulation = sedml.getSimulation(taskId+"_sim");
    if (simulation != null) {
      sedml.removeSimulation(simulation);
    }
    Model model = sedml.getModelWithId(simName+"_model");
    if (model!=null) {
      sedml.removeModel(model);
    }
    if (task != null) {
      sedml.removeTask(task);
    }
    Algorithm algo = PropertiesUtil.getAlgorithm(properties);
    if (algo.getKisaoID().equals(GlobalConstants.KISAO_FBA)) {
      simulation = new SteadyState(taskId+"_sim", "", algo);
    } else if (algo.getKisaoID().equals(GlobalConstants.KISAO_GENERIC)) {
      simulation = new SteadyState(taskId+"_sim", "", algo);
      // TODO: need to deal with transient Markov chain method which is a type of UniformTimeCourse
    } else {
      simulation = new UniformTimeCourse(taskId+"_sim", "", initialTime, outputStartTime, timeLimit, numberOfSteps, algo);
      if (properties.isPrintInterval()) {
        Element para = new Element("printInterval");
        para.setNamespace(Namespace.getNamespace("http://www.async.ece.utah.edu/iBioSim"));
        para.setAttribute("Print_Interval", String.valueOf(properties.getSimulationProperties().getPrintInterval()));
        Annotation ann = new Annotation(para);
        simulation.addAnnotation(ann);
      } else if (properties.isMinPrintInterval()) {
        Element para = new Element("printInterval");
        para.setNamespace(Namespace.getNamespace("http://www.async.ece.utah.edu/iBioSim"));
        para.setAttribute("Minimum_Print_Interval", String.valueOf(properties.getSimulationProperties().getPrintInterval()));
        Annotation ann = new Annotation(para);
        simulation.addAnnotation(ann);
      }
    }
    sedml.addSimulation(simulation);
    task = new Task(taskId, "", taskId+"_model", simulation.getId());
    sedml.addTask(task);
    DataGenerator dataGen = sedml.getDataGeneratorWithId("time_"+taskId+"_dg");
    if (dataGen != null) {
      sedml.removeDataGenerator(dataGen);
    }
    Variable variable = new Variable("time_"+taskId,"",taskId,VariableSymbol.TIME);
    org.jmathml.ASTNode math = org.jlibsedml.Libsedml.parseFormulaString("time_"+taskId);
    dataGen = new DataGenerator("time_"+taskId+"_dg","time",math);
    dataGen.addVariable(variable);
    sedml.addDataGenerator(dataGen);

    //TODO: write this
    //	    for (int i = 0; i < simTab.getComponentCount(); i++)
    //	    {
    //	      if (simTab.getComponentAt(i) instanceof Graph)
    //	      {
    //	        ((Graph) simTab.getComponentAt(i)).saveSEDML(sedmlDoc,simName,null);
    //	      }
    //	    }
    //	    gui.writeSEDMLDocument();
  }


}

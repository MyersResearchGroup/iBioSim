package edu.utah.ece.async.ibiosim.analysis.properties;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.jlibsedml.AbstractTask;
import org.jlibsedml.OneStep;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Simulation;
import org.jlibsedml.UniformTimeCourse;

import edu.utah.ece.async.ibiosim.analysis.util.SEDMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;

public class AnalysisPropertiesLoader {

  private void loadSEDML(SEDMLDocument sedmlDoc, String subTask, AnalysisProperties properties)
  {
    /*
    SedML sedml = sedmlDoc.getSedMLModel();
    String simName = properties.getId();
    
    subTaskList.removeAllItems();
    subTaskList.addItem("(none)");
    for (AbstractTask task : sedml.getTasks())
    {
      if (task.getId().startsWith(simName+"__")) {
        subTaskList.addItem(task.getId().replace(simName+"__",""));
      }
    }
    String taskId = simName;
    if (subTask!=null && !subTask.equals("")) {
      taskId = taskId + "__" + subTask;
      subTaskList.setSelectedItem(subTask);
    }
    AbstractTask task = sedml.getTaskWithId(taskId);
    if (task != null) {
      Simulation simulation = sedml.getSimulation(task.getSimulationReference());
      setAlgorithm(simulation.getAlgorithm());
      fileStem.setText(subTask);
      if (ODE.isSelected()||monteCarlo.isSelected())
      {
        // TODO: what java ODE simulator fails on 987
        if (simulation instanceof UniformTimeCourse) {
          UniformTimeCourse utcSimulation = (UniformTimeCourse) simulation;
          String printInterval = SEDMLutilities.getSEDBaseAnnotation(simulation, "printInterval", "Print_Interval", null);
          if (printInterval != null)
          {
            intervalLabel.setSelectedItem("Print Interval");
            interval.setText(printInterval);
          } else {
            printInterval = SEDMLutilities.getSEDBaseAnnotation(simulation, "printInterval", "Minimum_Print_Interval", null);
            if (printInterval != null) {
              intervalLabel.setSelectedItem("Minimum Print Interval");
              interval.setText(printInterval);
            } else {
              intervalLabel.setSelectedItem("Number Of Steps");
              interval.setText("" + utcSimulation.getNumberOfPoints());
            }
          }
          initialTimeField.setText("" + utcSimulation.getInitialTime());
          outputStartTimeField.setText("" + utcSimulation.getOutputStartTime());
          limit.setText("" + utcSimulation.getOutputEndTime());
        } else if (simulation instanceof OneStep) {
          OneStep osSimulation = (OneStep) simulation;
          intervalLabel.setSelectedItem("Number Of Steps");
          interval.setText("" + 1);
          initialTimeField.setText("" + 0);
          outputStartTimeField.setText("" + 0);
          limit.setText("" + osSimulation.getStep());
        }
      }
      else if (fba.isSelected())
      {
        absErr.setText("1e-4");
      }
    }
    */
  }
  
  /**
   * Loads the simulate options.
   */
  public void loadPropertiesFile(AnalysisProperties properties)
  {
    /*
    String root = properties.getRoot();
    String simName = properties.getId();
    String openFile = root + GlobalConstants.separator + simName + GlobalConstants.separator + simName + ".properties";
    if (!(new File(openFile)).exists())
    {
      openFile = root + GlobalConstants.separator + simName + GlobalConstants.separator + modelName + ".properties";
      if (!(new File(openFile)).exists())
      {
        return;
      }
    }
    Properties load = new Properties();
    try
    {
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
          else if (((String) key).length() > 27 && ((String) key).substring(0, 28).equals("reb2sac.interesting.species."))
          {
          }
          else if (key.equals("reb2sac.rapid.equilibrium.condition.1"))
          {
          }
          else if (key.equals("reb2sac.rapid.equilibrium.condition.2"))
          {
          }
          else if (key.equals("reb2sac.qssa.condition.1"))
          {
          }
          else if (key.equals("reb2sac.operator.max.concentration.threshold"))
          {
          }
          else if (key.equals("reb2sac.diffusion.stoichiometry.amplification.value"))
          {
          }
          else if (key.equals("reb2sac.iSSA.number.paths"))
          {
          }
          else if (key.equals("reb2sac.iSSA.type"))
          {
          }
          else if (key.equals("reb2sac.iSSA.adaptive"))
          {
          }
          else if (key.equals("ode.simulation.time.limit"))
          {
          }
          else if (key.equals("simulation.initial.time"))
          {
          }
          else if (key.equals("simulation.output.start.time"))
          {
          }
          else if (key.equals("ode.simulation.print.interval"))
          {
          }
          else if (key.equals("ode.simulation.number.steps"))
          {
          }
          else if (key.equals("ode.simulation.min.time.step"))
          {
          }
          else if (key.equals("ode.simulation.time.step"))
          {
          }
          else if (key.equals("ode.simulation.absolute.error"))
          {
          }
          else if (key.equals("ode.simulation.out.dir"))
          {
          }
          else if (key.equals("monte.carlo.simulation.time.limit"))
          {
          }
          else if (key.equals("monte.carlo.simulation.print.interval"))
          {
          }
          else if (key.equals("monte.carlo.simulation.number.steps"))
          {
          }
          else if (key.equals("monte.carlo.simulation.min.time.step"))
          {
          }
          else if (key.equals("monte.carlo.simulation.time.step"))
          {
          }
          else if (key.equals("monte.carlo.simulation.random.seed"))
          {
          }
          else if (key.equals("monte.carlo.simulation.runs"))
          {
          }
          else if (key.equals("monte.carlo.simulation.out.dir"))
          {
          }
          else if (key.equals("simulation.run.termination.decider"))
          {
          }
          else if (key.equals("computation.analysis.sad.path"))
          {
          }
          else if (key.equals("simulation.time.series.species.level.file"))
          {
          }
          else if (key.equals("reb2sac.simulation.method"))
          {
          }
          else if (key.equals("reb2sac.abstraction.method"))
          {
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
          else if (key.equals("abstraction.interesting") && lpnAbstraction != null)
          {
            String intVars = load.getProperty("abstraction.interesting");
            String[] array = intVars.split(" ");
            for (String s : array)
            {
              if (!s.equals(""))
              {
                lpnAbstraction.addIntVar(s);
              }
            }
          }
          else if (key.equals("abstraction.factor") && lpnAbstraction != null)
          {
            lpnAbstraction.factorField.setText(load.getProperty("abstraction.factor"));
          }
          else if (key.equals("abstraction.iterations") && lpnAbstraction != null)
          {
            lpnAbstraction.iterField.setText(load.getProperty("abstraction.iterations"));
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
        if (lpnAbstraction != null)
        {
          for (String s : lpnAbstraction.getAbstractionProperty().transforms)
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
                  lpnAbstraction.addPreXform(s);
                }
              }
              else
              {
                lpnAbstraction.getAbstractionProperty().preAbsModel.removeElement(s);
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
                  lpnAbstraction.addLoopXform(s);
                }
              }
              else
              {
                lpnAbstraction.getAbstractionProperty().loopAbsModel.removeElement(s);
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
                  lpnAbstraction.addPostXform(s);
                }
              }
              else
              {
                lpnAbstraction.getAbstractionProperty().postAbsModel.removeElement(s);
              }
            }
            else if (containsAbstractions && !containsXform.get(s))
            {
              lpnAbstraction.getAbstractionProperty().preAbsModel.removeElement(s);
              lpnAbstraction.getAbstractionProperty().loopAbsModel.removeElement(s);
              lpnAbstraction.getAbstractionProperty().postAbsModel.removeElement(s);
            }
          }
          if (preOrder.size() > 0)
          {
            lpnAbstraction.getAbstractionProperty().preAbsModel.removeAllElements();
          }
          for (Integer j = 0; j < preOrder.size(); j++)
          {
            lpnAbstraction.getAbstractionProperty().preAbsModel.addElement(preOrder.get(j));
          }
          if (loopOrder.size() > 0)
          {
            lpnAbstraction.getAbstractionProperty().loopAbsModel.removeAllElements();
          }
          for (Integer j = 0; j < loopOrder.size(); j++)
          {
            lpnAbstraction.getAbstractionProperty().loopAbsModel.addElement(loopOrder.get(j));
          }
          if (postOrder.size() > 0)
          {
            lpnAbstraction.getAbstractionProperty().postAbsModel.removeAllElements();
          }
          for (Integer j = 0; j < postOrder.size(); j++)
          {
            lpnAbstraction.getAbstractionProperty().postAbsModel.addElement(postOrder.get(j));
          }
          lpnAbstraction.preAbs.setListData(lpnAbstraction.getAbstractionProperty().preAbsModel.toArray());
          lpnAbstraction.loopAbs.setListData(lpnAbstraction.getAbstractionProperty().loopAbsModel.toArray());
          lpnAbstraction.postAbs.setListData(lpnAbstraction.getAbstractionProperty().postAbsModel.toArray());
        }
        if (load.getProperty("reb2sac.abstraction.method").equals("none"))
        {
          noAbstraction.setSelected(true);
          enableNoAbstraction();
        }
        else if (load.getProperty("reb2sac.abstraction.method").equals("expand"))
        {
          expandReactions.setSelected(true);
          enableNoAbstraction();
        }
        else if (load.getProperty("reb2sac.abstraction.method").equals("abs"))
        {
          reactionAbstraction.setSelected(true);
          enableReactionAbstraction();
        }
        else
        {
          stateAbstraction.setSelected(true);
          enableStateAbstraction();
        }
        if (load.containsKey("ode.simulation.absolute.error"))
        {
          absErr.setText(load.getProperty("ode.simulation.absolute.error"));
        }
        else
        {
          absErr.setText("1.0E-9");
        }
        if (load.containsKey("ode.simulation.relative.error"))
        {
          relErr.setText(load.getProperty("ode.simulation.relative.error"));
        }
        else
        {
          relErr.setText("0.0");
        }
        if (load.containsKey("monte.carlo.simulation.time.step"))
        {
          step.setText(load.getProperty("monte.carlo.simulation.time.step"));
        }
        else
        {
          step.setText("inf");
        }
        if (load.containsKey("monte.carlo.simulation.min.time.step"))
        {
          minStep.setText(load.getProperty("monte.carlo.simulation.min.time.step"));
        }
        else
        {
          minStep.setText("0");
        }
        if (load.containsKey("monte.carlo.simulation.time.limit"))
        {
          limit.setText(load.getProperty("monte.carlo.simulation.time.limit"));
        }
        else
        {
          limit.setText("100.0");
        }
        if (load.containsKey("simulation.initial.time"))
        {
          initialTimeField.setText(load.getProperty("simulation.initial.time"));
        }
        else
        {
          initialTimeField.setText("0.0");
        }
        if (load.containsKey("simulation.output.start.time"))
        {
          outputStartTimeField.setText(load.getProperty("simulation.output.start.time"));
        }
        else
        {
          outputStartTimeField.setText("0.0");
        }
        if (load.containsKey("monte.carlo.simulation.print.interval"))
        {
          intervalLabel.setSelectedItem("Print Interval");
          interval.setText(load.getProperty("monte.carlo.simulation.print.interval"));
        }
        else if (load.containsKey("monte.carlo.simulation.minimum.print.interval"))
        {
          intervalLabel.setSelectedItem("Minimum Print Interval");
          interval.setText(load.getProperty("monte.carlo.simulation.minimum.print.interval"));
        }
        else if (load.containsKey("monte.carlo.simulation.number.steps"))
        {
          intervalLabel.setSelectedItem("Number Of Steps");
          interval.setText(load.getProperty("monte.carlo.simulation.number.steps"));
        }
        else
        {
          interval.setText("1.0");
        }
        if (load.containsKey("monte.carlo.simulation.random.seed"))
        {
          seed.setText(load.getProperty("monte.carlo.simulation.random.seed"));
        }
        if (load.containsKey("monte.carlo.simulation.runs"))
        {
          runs.setText(load.getProperty("monte.carlo.simulation.runs"));
        }
        if (load.containsKey("simulation.time.series.species.level.file"))
        {
          // usingSSA.doClick();
        }
        else
        {
          description.setEnabled(true);
          explanation.setEnabled(true);
          simulators.setEnabled(true);
          simulatorsLabel.setEnabled(true);
          if (!stateAbstraction.isSelected())
          {
            ODE.setEnabled(true);
          }
          else
          {
            markov.setEnabled(true);
          }
        }
        if (load.containsKey("simulation.printer.tracking.quantity"))
        {
          if (load.getProperty("simulation.printer.tracking.quantity").equals("concentration"))
          {
            concentrations.doClick();
          }
        }
        if (load.containsKey("simulation.printer"))
        {
          if (load.getProperty("simulation.printer").equals("null.printer"))
          {
            genRuns.doClick();
          }
        }
        if (load.containsKey("reb2sac.simulation.method"))
        {
          if (load.getProperty("reb2sac.simulation.method").equals("ODE"))
          {
            ODE.setSelected(true);
            if (load.containsKey("simulation.initial.time"))
            {
              initialTimeField.setText(load.getProperty("simulation.initial.time"));
            }
            else
            {
              initialTimeField.setText("0.0");
            }
            if (load.containsKey("simulation.output.start.time"))
            {
              outputStartTimeField.setText(load.getProperty("simulation.output.start.time"));
            }
            else
            {
              outputStartTimeField.setText("0.0");
            }
            if (load.containsKey("ode.simulation.time.limit"))
            {
              limit.setText(load.getProperty("ode.simulation.time.limit"));
            }
            if (load.containsKey("ode.simulation.print.interval"))
            {
              intervalLabel.setSelectedItem("Print Interval");
              interval.setText(load.getProperty("ode.simulation.print.interval"));
            }
            if (load.containsKey("ode.simulation.minimum.print.interval"))
            {
              intervalLabel.setSelectedItem("Minimum Print Interval");
              interval.setText(load.getProperty("ode.simulation.minimum.print.interval"));
            }
            else if (load.containsKey("ode.simulation.number.steps"))
            {
              intervalLabel.setSelectedItem("Number Of Steps");
              interval.setText(load.getProperty("ode.simulation.number.steps"));
            }
            if (load.containsKey("ode.simulation.time.step"))
            {
              step.setText(load.getProperty("ode.simulation.time.step"));
            }
            if (load.containsKey("ode.simulation.min.time.step"))
            {
              minStep.setText(load.getProperty("ode.simulation.min.time.step"));
            }
            enableODE();
            if (load.containsKey("selected.simulator"))
            {
              simulators.setSelectedItem(load.getProperty("selected.simulator"));
            }
            if (load.containsKey("file.stem"))
            {
              fileStem.setText(load.getProperty("file.stem"));
            }
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("monteCarlo"))
          {
            monteCarlo.setSelected(true);
            append.setEnabled(true);
            enableMonteCarlo();
            if (load.containsKey("selected.simulator"))
            {
              String simId = load.getProperty("selected.simulator");
              if (simId.equals("mpde"))
              {
                simulators.setSelectedItem("iSSA");
                mpde.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("mean_path"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("median_path"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("mean_path-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("median_path-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("mean_path-adaptive"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                adaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("median_path-adaptive"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                adaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("mean_path-adaptive-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                adaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("median_path-adaptive-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                adaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("mean_path-event"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("median_path-event"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("mean_path-event-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("median_path-event-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else
              {
                simulators.setSelectedItem(simId);
              }
            }
            if (load.containsKey("file.stem"))
            {
              fileStem.setText(load.getProperty("file.stem"));
            }
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("markov"))
          {
            markov.setSelected(true);
            enableMarkov();
            if (load.containsKey("selected.simulator"))
            {
              selectedMarkovSim = load.getProperty("selected.simulator");
              simulators.setSelectedItem(selectedMarkovSim);
            }
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("FBA"))
          {
            fba.doClick();
            enableFBA();
            // absErr.setEnabled(false);
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("SBML"))
          {
            sbml.setSelected(true);
            enableSbmlDotAndXhtml();
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("Network"))
          {
            dot.setSelected(true);
            enableSbmlDotAndXhtml();
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("Browser"))
          {
            xhtml.setSelected(true);
            enableSbmlDotAndXhtml();
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("LPN"))
          {
            enableSbmlDotAndXhtml();
          }
        }
        if (load.containsKey("reb2sac.abstraction.method"))
        {
          if (load.getProperty("reb2sac.abstraction.method").equals("none"))
          {
            noAbstraction.setSelected(true);
          }
          else if (load.getProperty("reb2sac.abstraction.method").equals("expand"))
          {
            expandReactions.setSelected(true);
          }
          else if (load.getProperty("reb2sac.abstraction.method").equals("abs"))
          {
            reactionAbstraction.setSelected(true);
          }
          else if (load.getProperty("reb2sac.abstraction.method").equals("nary"))
          {
            stateAbstraction.setSelected(true);
          }
        }
        if (load.containsKey("selected.property"))
        {
          if (transientProperties != null)
          {
            transientProperties.setSelectedItem(load.getProperty("selected.property"));
          }
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

          interestingSpecies.add(speciesAndThresholds);
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
        preAbstractions = getLists.toArray();
        preAbs.setListData(preAbstractions);

        getLists = new ArrayList<String>();
        i = 1;
        while (load.containsKey("reb2sac.abstraction.method.2." + i))
        {
          getLists.add(load.getProperty("reb2sac.abstraction.method.2." + i));
          i++;
        }
        loopAbstractions = getLists.toArray();
        loopAbs.setListData(loopAbstractions);

        getLists = new ArrayList<String>();
        i = 1;
        while (load.containsKey("reb2sac.abstraction.method.3." + i))
        {
          getLists.add(load.getProperty("reb2sac.abstraction.method.3." + i));
          i++;
        }
        postAbstractions = getLists.toArray();
        postAbs.setListData(postAbstractions);

        if (load.containsKey("reb2sac.rapid.equilibrium.condition.1"))
        {
          rapid1.setText(load.getProperty("reb2sac.rapid.equilibrium.condition.1"));
        }
        if (load.containsKey("reb2sac.rapid.equilibrium.condition.2"))
        {
          rapid2.setText(load.getProperty("reb2sac.rapid.equilibrium.condition.2"));
        }
        if (load.containsKey("reb2sac.qssa.condition.1"))
        {
          qssa.setText(load.getProperty("reb2sac.qssa.condition.1"));
        }
        if (load.containsKey("reb2sac.operator.max.concentration.threshold"))
        {
          maxCon.setText(load.getProperty("reb2sac.operator.max.concentration.threshold"));
        }
        if (load.containsKey("reb2sac.diffusion.stoichiometry.amplification.value"))
        {
          diffStoichAmp.setText(load.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
        }
        if (load.containsKey("reb2sac.iSSA.number.paths"))
        {
          bifurcation.setSelectedItem(load.getProperty("reb2sac.iSSA.number.paths"));
        }
        if (load.containsKey("reb2sac.iSSA.type"))
        {
          String type = load.getProperty("reb2sac.iSSA.type");
          if (type.equals("mpde"))
          {
            mpde.doClick();
          }
          else if (type.equals("medianPath"))
          {
            medianPath.doClick();
          }
          else
          {
            meanPath.doClick();
          }
        }
        if (load.containsKey("reb2sac.iSSA.adaptive"))
        {
          String type = load.getProperty("reb2sac.iSSA.adaptive");
          if (type.equals("true"))
          {
            adaptive.doClick();
          }
          else
          {
            nonAdaptive.doClick();
          }
        }
      }
      else
      {
        if (load.containsKey("selected.simulator"))
        {
          simulators.setSelectedItem(load.getProperty("selected.simulator"));
        }
        if (load.containsKey("file.stem"))
        {
          fileStem.setText(load.getProperty("file.stem"));
        }
        if (load.containsKey("simulation.printer.tracking.quantity"))
        {
          if (load.getProperty("simulation.printer.tracking.quantity").equals("concentration"))
          {
            concentrations.doClick();
          }
        }
        if (load.containsKey("simulation.printer"))
        {
          if (load.getProperty("simulation.printer").equals("null.printer"))
          {
            genRuns.doClick();
          }
        }
      }
      change = false;
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
      JOptionPane.showMessageDialog(Gui.frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
    }
    */
  }
  
}

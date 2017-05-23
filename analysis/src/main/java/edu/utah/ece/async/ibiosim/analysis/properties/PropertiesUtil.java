package edu.utah.ece.async.ibiosim.analysis.properties;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jlibsedml.Algorithm;
import org.jlibsedml.AlgorithmParameter;
import org.jlibsedml.Annotation;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;

public class PropertiesUtil {
  
  static Algorithm getAlgorithm()
  {
    Algorithm algorithm = null;
//    Element para = new Element("analysis");
//    para.setNamespace(Namespace.getNamespace("http://www.async.ece.utah.edu/iBioSim"));
//    if (ODE.isSelected())
//    {
//      if (((String) simulators.getSelectedItem()).contains("euler"))
//      {
//        algorithm = new Algorithm(GlobalConstants.KISAO_EULER);
//      }
//      else if (((String) simulators.getSelectedItem()).contains("rk8pd"))
//      {
//        algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_PRINCE_DORMAND);
//      }
//      else if (((String) simulators.getSelectedItem()).contains("rkf45") || ((String) simulators.getSelectedItem()).contains("Runge-Kutta-Fehlberg"))
//      {
//        algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_FEHLBERG);
//        para.setAttribute("method", ((String) simulators.getSelectedItem()));
//      } 
//      else {
//        algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_FEHLBERG);
//        para.setAttribute("method", ((String) simulators.getSelectedItem()));
//      }
//    }
//    else if (monteCarlo.isSelected())
//    {
//      if (((String) simulators.getSelectedItem()).equals("gillespie"))
//      {
//        algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
//      }
//      if (((String) simulators.getSelectedItem()).contains("Hierarchical"))
//      {
//        algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
//      }
//      else if (((String) simulators.getSelectedItem()).contains("SSA-CR"))
//      {
//        algorithm = new Algorithm(GlobalConstants.KISAO_SSA_CR);
//      }
//      else {
//        algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
//        para.setAttribute("method", ((String) simulators.getSelectedItem()));
//      }
//    } 
//    else if (fba.isSelected()) 
//    {
//      algorithm = new Algorithm(GlobalConstants.KISAO_FBA);
//    }
//    else
//    {
//      algorithm = new Algorithm(GlobalConstants.KISAO_GENERIC);
//      if (sbml.isSelected()) {
//        para.setAttribute("method", "Model");
//      } else if (dot.isSelected()) {
//        para.setAttribute("method", "Network");
//      } else if (xhtml.isSelected()) {
//        para.setAttribute("method", "Browser");
//      } else {
//        para.setAttribute("method", ((String) simulators.getSelectedItem()));
//      }
//    }
//    if (expandReactions.isSelected()) {
//      para.setAttribute("abstraction", "Expand Reactions");
//    } else if (reactionAbstraction.isSelected()) {
//      para.setAttribute("abstraction", "Reaction-based");
//    } else if (stateAbstraction.isSelected()) {
//      para.setAttribute("abstraction", "State-based");
//    }
//    Annotation ann = new Annotation(para);
//    algorithm.addAnnotation(ann);
//    AlgorithmParameter ap = new AlgorithmParameter(GlobalConstants.KISAO_MINIMUM_STEP_SIZE,minStep.getText());
//    algorithm.addAlgorithmParameter(ap);
//    ap = new AlgorithmParameter(GlobalConstants.KISAO_MAXIMUM_STEP_SIZE, step.getText());
//    algorithm.addAlgorithmParameter(ap);
//    ap = new AlgorithmParameter(GlobalConstants.KISAO_ABSOLUTE_TOLERANCE, absErr.getText());
//    algorithm.addAlgorithmParameter(ap);
//    ap = new AlgorithmParameter(GlobalConstants.KISAO_RELATIVE_TOLERANCE,relErr.getText());
//    algorithm.addAlgorithmParameter(ap);
//    ap = new AlgorithmParameter(GlobalConstants.KISAO_SEED,seed.getText());
//    algorithm.addAlgorithmParameter(ap);
//    ap = new AlgorithmParameter(GlobalConstants.KISAO_SAMPLES,runs.getText());
//    algorithm.addAlgorithmParameter(ap);
    return algorithm;
  }

}

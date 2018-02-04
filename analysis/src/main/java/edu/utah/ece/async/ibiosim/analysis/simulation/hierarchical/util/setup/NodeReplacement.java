package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;

public class NodeReplacement 
{
  HierarchicalModel replacingModel;
  HierarchicalModel replacedModel;
  String replacingVariable;
  String replacedVariable;
  
  NodeReplacement(HierarchicalModel top, HierarchicalModel sub, String topVariable, String subVariable)
  {
    this.replacingModel = top;
    this.replacedModel = sub;
    this.replacingVariable = topVariable;
    this.replacedVariable = subVariable;
  }
}

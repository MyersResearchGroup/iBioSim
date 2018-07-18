package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;

/**
 * Pairs a replacing element to the element being replaced, as well as their corresponding models.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
class NodeReplacement {
  HierarchicalModel replacingModel;
  HierarchicalModel replacedModel;
  String replacingVariable;
  String replacedVariable;
  boolean isTopMetaId;
  boolean isSubMetaId;

  NodeReplacement(HierarchicalModel top, HierarchicalModel sub, String topVariable, String subVariable, boolean isTopMetaId, boolean isSubMetaId) {
    this.replacingModel = top;
    this.replacedModel = sub;
    this.replacingVariable = topVariable;
    this.replacedVariable = subVariable;
    this.isTopMetaId = isTopMetaId;
    this.isSubMetaId = isSubMetaId;
  }
}

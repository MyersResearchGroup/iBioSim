package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;

/**
 * Pairs a deleted element with its corresponding model.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class NodeDeletion {

  HierarchicalModel model;
  String deletedId;
  String deletedMetaId;

  NodeDeletion(HierarchicalModel model, String id, String metaId) {
    this.model = model;
    this.deletedId = id;
    this.deletedMetaId = metaId;
  }

}

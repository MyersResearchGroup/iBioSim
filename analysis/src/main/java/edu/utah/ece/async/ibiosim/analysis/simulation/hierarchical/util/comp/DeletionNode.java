package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp;

import java.util.List;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;

/**
 * Pairs a deleted element with its corresponding model.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DeletionNode {
  private final List<String> submodels;

  protected String subId;
  protected String subMetaId;

  public DeletionNode(List<String> submodels) {
    this.submodels = submodels;
  }

  public void setSubIdRef(String id) {
    this.subId = id;
  }

  public void setSubMetaIdRef(String metaid) {
    this.subMetaId = metaid;
  }

  public String getSubIdRef() {
    return this.subId;
  }

  public String getSubMetaIdRef() {
    return this.subMetaId;
  }

  /**
   *
   * @param topModel
   * @return
   */
  public HierarchicalModel getSubmodel(HierarchicalModel topModel) {
    HierarchicalModel submodel = topModel;
    for (String submodelId : submodels) {
      submodel = submodel.getSubmodel(submodelId);
    }
    return submodel;
  }

}

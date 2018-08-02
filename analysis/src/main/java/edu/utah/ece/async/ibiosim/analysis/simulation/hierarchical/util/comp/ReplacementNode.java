package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp;

import java.util.List;

/**
 * Pairs a replacing element to the element being replaced, as well as their corresponding models.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ReplacementNode extends DeletionNode {
  private String topId;
  private String topMetaId;
  private boolean isReplacedBy;
  private boolean justDelete;

  public ReplacementNode(List<String> submodels) {
    super(submodels);
    justDelete = false;
  }

  public void setTopIdRef(String idRef) {
    this.topId = idRef;
  }

  public void setTopMetaIdRef(String metaIdRef) {
    this.topMetaId = metaIdRef;
  }

  public void setReplacedBy(boolean isReplacedBy) {
    this.isReplacedBy = isReplacedBy;
  }

  public String getTopIdRef() {
    return topId;
  }

  public String getTopMetaIdRef() {
    return topMetaId;
  }

  public boolean isReplacedBy() {
    return isReplacedBy;
  }

  public void setJustDelete(boolean justDelete) {
    this.justDelete = justDelete;
  }

  public boolean isJustDelete() {
    return justDelete;
  }
}

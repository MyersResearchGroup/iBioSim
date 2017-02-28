package dataModels.biomodel.parser;


public class GridTable {

  private int numRows, numCols;
  
  public GridTable()
  {
    numRows = 0;
    numCols = 0;
  }
  
  /**
   * @return the numRows
   */
  public int getNumRows() {
    return numRows;
  }

  /**
   * @return the numCols
   */
  public int getNumCols() {
    return numCols;
  }
  
  /**
   * @return the numRows
   */
  public void setNumRows(int numRows) {
    this.numRows = numRows;
  }

  /**
   * @return the numCols
   */
  public void setNumCols(int numCols) {
    this.numCols = numCols;
  }
}

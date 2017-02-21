package dataModels.util.exceptions;

public class BioSimException extends Exception{

  /**
   * 
   */
  private static final long serialVersionUID = 889807138206436229L;
  private String title;
  
  public BioSimException(String message, String messageTitle){
    super(message);
    this.title = messageTitle;
  }
  
  public String getTitle(){
    return this.title;
  }
}

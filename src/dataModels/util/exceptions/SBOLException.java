package dataModels.util.exceptions;

public class SBOLException extends BioSimException{
	
	private String title;
	
	public SBOLException(String message, String messageTitle){
		super(message);
		this.title = messageTitle;
	}
	
	public String getTitle(){
		return this.title;
	}

}

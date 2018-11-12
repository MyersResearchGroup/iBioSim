package VerilogConstructs;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogWait implements AbstractVerilogConstruct{
	
	private String waitExpression;
	private AbstractVerilogConstruct delayConstruct;
	
	public void setWaitExpression(String waitExpression) {
		this.waitExpression = waitExpression;
	}
	
	public String getWaitExpression() {
		return this.waitExpression;
	}
	
	public AbstractVerilogConstruct getDelayConstruct() {
		return this.delayConstruct;
	}
	
	@Override
	public void addConstruct(AbstractVerilogConstruct construct) { 
		this.delayConstruct = construct;
	}
}
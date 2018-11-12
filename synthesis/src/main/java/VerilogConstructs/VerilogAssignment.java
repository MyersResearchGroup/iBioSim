package VerilogConstructs;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogAssignment implements AbstractVerilogConstruct{

	private String variable;
	private String expression;
	private AbstractVerilogConstruct delayConstruct;
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public void setVariable(String variable) {
		this.variable = variable;
	}
	
	public boolean isExpressionSet() {
		return this.expression != null;
	}
	
	public boolean isVariableSet() {
		return this.variable != null;
	}
	
	public String getVariable() {
		return this.variable;
	}
	
	public String getExpression() {
		return this.expression;
	}
	
	public AbstractVerilogConstruct getDelayConstruct() {
		return this.delayConstruct;
	}
	
	@Override
	public void addConstruct(AbstractVerilogConstruct construct) { 
		this.delayConstruct = construct;
	}

}
package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogConditional implements AbstractVerilogConstruct {

	private String ifCondition;
	private AbstractVerilogConstruct ifBlock;
	private AbstractVerilogConstruct elseBlock;

	public void setIfCondition(String condition) {
		this.ifCondition = condition;
	}
	
	public void setElseConditional(VerilogConditional elseStatement) {
		this.elseBlock = elseStatement;
	}
	
	public String getIfCondition() {
		return this.ifCondition;
	}
	
	public AbstractVerilogConstruct getIfBlock() {
		return this.ifBlock;
	}

	public AbstractVerilogConstruct getElseBlock() {
		return this.elseBlock;
	}
	
	@Override
	public void addConstruct(AbstractVerilogConstruct construct) {
		if(ifBlock == null) {
			this.ifBlock = construct;
		}
		else {
			this.elseBlock = construct;
		}
		
	}

}
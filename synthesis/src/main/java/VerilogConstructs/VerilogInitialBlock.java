package VerilogConstructs;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogInitialBlock implements AbstractVerilogConstruct{
	private AbstractVerilogConstruct initialBlock; 
	
	public AbstractVerilogConstruct getBlock() {
		return this.initialBlock;
	}
	
	@Override
	public void addConstruct(AbstractVerilogConstruct construct) {
		this.initialBlock = construct;
	}
	
}

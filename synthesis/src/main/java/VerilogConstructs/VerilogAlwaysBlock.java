package VerilogConstructs;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogAlwaysBlock implements AbstractVerilogConstruct{
	private AbstractVerilogConstruct alwaysBlock;

	public AbstractVerilogConstruct getBlock() {
		return this.alwaysBlock;
	}

	@Override
	public void addConstruct(AbstractVerilogConstruct construct) {
		this.alwaysBlock = construct;
		
	}
	
}
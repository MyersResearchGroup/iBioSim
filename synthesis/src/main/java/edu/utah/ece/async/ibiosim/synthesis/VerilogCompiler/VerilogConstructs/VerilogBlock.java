package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs;
import java.util.ArrayList;
import java.util.List;

/**
 * Any verilog block that has a begin and end. 
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogBlock implements AbstractVerilogConstruct{

	protected List<AbstractVerilogConstruct> constructList; 
	
	public VerilogBlock() {
		this.constructList = new ArrayList<>();
	}
	
	/**
	 * A construct added to this VerilogBlock could be conditional statement, assignments, and wait statements.
	 * @param construct
	 */
	public void addConstruct(AbstractVerilogConstruct construct) {
		this.constructList.add(construct);
	}
	
	public List<AbstractVerilogConstruct> getBlockConstructs() {
		return this.constructList;
	}
	
	public int getNumConstructSize() {
		return this.constructList != null ? this.constructList.size() : null;
	}

}
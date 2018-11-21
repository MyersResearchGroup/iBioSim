package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.List;

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAlwaysBlock;
import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogConditional;
import VerilogConstructs.VerilogDelay;
import VerilogConstructs.VerilogWait;

/**
 * A utility class used to easily access Verilog constructs when testing the structured Verilog classes. 
 * @author Tramy Nguyen
 *
 */
public class VerilogTestUtility {
	
	public static VerilogDelay getDelayConstruct(AbstractVerilogConstruct construct) {
		VerilogDelay delay = null;
		if(construct instanceof VerilogWait) {
			VerilogWait waitStmt = (VerilogWait) construct;
			delay = (VerilogDelay) waitStmt.getDelayConstruct();
		}
		else {
			delay = (VerilogDelay) construct;
		}
		return delay;
	}
	
	public static AbstractVerilogConstruct getBlockConstruct(VerilogBlock block, int index) {	
		assert(block.getBlockConstructs() != null);
		
		int size = block.getNumConstructSize();
		if(size > 0 && index < size) {
			return block.getBlockConstructs().get(index);
		}
		return null;
	}
	
	public static VerilogWait getWaitConstruct(AbstractVerilogConstruct construct) {
		assert(construct instanceof VerilogWait);
		return (VerilogWait) construct;
	}
	
	public static VerilogConditional getConditionalConstruct(AbstractVerilogConstruct construct) {
		assert(construct instanceof VerilogConditional);
		return (VerilogConditional) construct;
	}
	
	public static VerilogBlock getVerilogBlock(AbstractVerilogConstruct construct) {
		VerilogBlock block = null;
		if(construct instanceof VerilogConditional) {
			VerilogConditional cond = (VerilogConditional) construct;
			block =  (VerilogBlock) cond.getIfBlock();
		}
		else if(construct instanceof VerilogAlwaysBlock) {
			VerilogAlwaysBlock alwy = (VerilogAlwaysBlock) construct; 
			block = (VerilogBlock) alwy.getBlock();
		}
		else {
			block = (VerilogBlock) construct;
		}
		return block;
	}
	
	public static VerilogAssignment getVerilogAssignment(AbstractVerilogConstruct construct){
		assert(construct instanceof VerilogAssignment);
		return (VerilogAssignment) construct;
	}
}
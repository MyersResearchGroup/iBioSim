package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogModule;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class EvenZeroes_Synth extends AbstractVerilogParserTest{

	private static VerilogModule verilogModule;
	private static ModuleDefinition sbolDesign;

	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-v", reader.getFile("evenzeroes_sequential_synth.v"), "-sbol", "-od", CompilerTestSuite.outputDirectory};
		
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		
		String vName = "bit1_bit0_parity1_parity0_ez_instance__state_net";
		verilogModule = moduleList.get(vName);
		Assert.assertNotNull(verilogModule);
		
		WrappedSBOL sbolWrapper = compiledVerilog.getSBOLWrapper(vName);
		Assert.assertNotNull(sbolWrapper);
		SBOLDocument sbolDoc = sbolWrapper.getSBOLDocument();
		Assert.assertEquals(1, sbolDoc.getModuleDefinitions().size());
		sbolDesign = sbolDoc.getModuleDefinition(vName, "1.0");
	}
	
	@Test
	public void Test_imp_inputSize() {
		Assert.assertEquals(2, verilogModule.getNumInputs());
	}
	
	@Test
	public void Test_imp_inputs() {
		List<String> actual_inputPorts = verilogModule.getInputPorts();
		List<String> expected_inputs = Arrays.asList("bit1", "bit0");
		Assert.assertEquals(expected_inputs, actual_inputPorts);
	}
	
	@Test
	public void Test_imp_outputSize() {
		Assert.assertEquals(3, verilogModule.getNumOutputs());
	}
	
	@Test
	public void Test_imp_outputs() {
		List<String> actual_outputPorts = verilogModule.getOutputPorts();
		List<String> expected_outputs = Arrays.asList("parity1", "parity0", "ez_instance__state");
		Assert.assertEquals(expected_outputs, actual_outputPorts);
	}
	
	@Test
	public void Test_imp_regSize() {
		Assert.assertEquals(0, verilogModule.getNumRegisters());
	}
	
	@Test 
	public void Test_imp_wireSize() {
		Assert.assertEquals(0, verilogModule.getNumWires());
	}
	
	@Test
	public void Test_imp_submoduleSize() {
		Assert.assertEquals(0, verilogModule.getNumSubmodules());
	}
	
	@Test
	public void Test_imp_alwaysblkSize() {
		Assert.assertEquals(0, verilogModule.getNumAlwaysBlock());
	}
	
	@Test
	public void Test_continAssignSize() {
		Assert.assertEquals(3, verilogModule.getNumContinousAssignments());
	}
	
	@Test
	public void Test_continAssign1() {
		VerilogAssignment actual_assign = verilogModule.getContinuousAssignment(0);
		Assert.assertEquals("parity1", actual_assign.getVariable());
		//Combinational expr: or(and(bit1,not(ez_instance__state)),or(and(bit0,and(not(parity0),ez_instance__state)),and(bit0,parity1)))
		Assert.assertEquals("or(and(bit1,not(ez_instance__state)),or(and(bit0,and(not(parity0),ez_instance__state)),and(or(bit1,bit0),parity1)))", actual_assign.getExpression());
	}
	
	@Test
	public void Test_continAssign2() {
		VerilogAssignment actual_assign = verilogModule.getContinuousAssignment(1);
		Assert.assertEquals("parity0", actual_assign.getVariable());
		//Combinational expr: or(and(bit1,ez_instance__state),or(and(bit0,parity0),and(bit0,and(not(parity1),not(ez_instance__state)))))
		Assert.assertEquals("or(and(bit1,ez_instance__state),or(and(bit0,and(not(parity1),not(ez_instance__state))),and(or(bit1,bit0),parity0)))", actual_assign.getExpression());
	}
	
	@Test
	public void Test_continAssign3() {
		VerilogAssignment actual_assign = verilogModule.getContinuousAssignment(2);
		Assert.assertEquals("ez_instance__state", actual_assign.getVariable());
		//Combinational expr: or(parity0,and(not(parity1),ez_instance__state))
		Assert.assertEquals("or(parity0,and(not(parity1),ez_instance__state))", actual_assign.getExpression());
	}
	
	@Test
	public void Test_SBOL() {
		Assert.assertEquals(5, sbolDesign.getFunctionalComponents().size());
		Assert.assertNotNull(sbolDesign.getFunctionalComponent("FC0_bit1"));
		
	}

	
}
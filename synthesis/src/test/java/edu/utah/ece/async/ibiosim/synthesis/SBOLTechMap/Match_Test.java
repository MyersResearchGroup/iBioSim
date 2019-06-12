package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ORGate;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.DirectMatch;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Match;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapUtility;

public class Match_Test {

	
	@Test
	public void Test_MatchAndSpecification() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException {
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.AND_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getOutputNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof ANDGate);
	}
	
	@Test
	public void Test_MatchNandSpecification1() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.NAND_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getOutputNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof NANDGate);	
	}
	
	@Test
	public void Test_MatchNandSpecification2() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.NAND_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getOutputNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(2, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof NANDGate);	
	}
	
	@Test
	public void Test_MatchNandSpecification3() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.NAND_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getOutputNode());
		
		matchedSolution = m.getGateList(m.getSpecification().getNode(URI.create("http://dummy.org/circuit_Nand/FC7_wiredProtein/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNode(URI.create("http://dummy.org/circuit_Nand/FC6_wiredProtein/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNode(URI.create("http://dummy.org/circuit_Nand/FC4_wiredProtein/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNode(URI.create("http://dummy.org/circuit_Nand/FC2_y/1.0")));
		Assert.assertEquals(3, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchNotSpecification1() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.NOT_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getOutputNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof NOTGate);	
	}
	
	@Test
	public void Test_MatchNotSpecification2() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.NOT_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getOutputNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(2, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof NOTGate);	
	}
	
	@Test
	public void Test_MatchOrSpecification() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.OR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.OR_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getOutputNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof ORGate);	
	}
	
	@Test
	public void Test_MatchSRLatchSpecification() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.SRLatch_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getOutputNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNode(URI.create("http://dummy.org/circuit_r_s_q_sl_instance__state_net/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNode(URI.create("http://dummy.org/circuit_r_s_q_sl_instance__state_net/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNode(URI.create("http://dummy.org/circuit_r_s_q_sl_instance__state_net/FC2_q/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
	}
}

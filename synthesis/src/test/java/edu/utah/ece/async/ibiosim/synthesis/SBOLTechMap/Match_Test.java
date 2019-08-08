package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ORGate;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Match;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.PreSelectedMatch;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapUtility;

/**
 * Test matching procedure 
 * @author Tramy Nguyen
 *
 */
public class Match_Test {

	
	@Test
	public void Test_MatchAndSpecification1() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException {
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof ANDGate);
	}
	
	@Test
	public void Test_MatchAndSpecification2() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException {
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertNull(matchedSolution);
	}
	
	@Test
	public void Test_MatchAndSpecification3() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException {
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/contAssign2/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/contAssign2/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());

		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/contAssign2/FC2_y/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchAndSpecification4() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		
		DecomposedGraphNode output = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/contAssign2/FC2_y/1.0"));
		output.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/IPTG/1"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertNull(matchedSolution);
		
	}
	
	@Test
	public void Test_MatchNandSpecification1() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof NANDGate);	
	}
	
	@Test
	public void Test_MatchNandSpecification2() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
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
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/circuit_Nand/FC7_wiredProtein/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/circuit_Nand/FC6_wiredProtein/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/circuit_Nand/FC4_wiredProtein/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/circuit_Nand/FC2_y/1.0")));
		Assert.assertEquals(3, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchNandSpecification4() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertNull(matchedSolution);
	}
	
	@Test
	public void Test_MatchNandSpecification5() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		
		DecomposedGraphNode output = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/circuit_Nand/FC0_a/1.0"));
		output.setPreselectedComponentDefinition(URI.create("https://testrepo"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertNull(matchedSolution);
		
	}
	
	@Test
	public void Test_MatchNotSpecification1() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof NOTGate);	
	}
	
	@Test
	public void Test_MatchNotSpecification2() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(2, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof NOTGate);	
	}
	
	@Test
	public void Test_MatchNotSpecification3() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertNull(matchedSolution);	
	}
	
	@Test
	public void Test_MatchNotSpecification4() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		
		DecomposedGraphNode output = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/contAssign/FC1_y/1.0"));
		output.setPreselectedComponentDefinition(URI.create("https://testrepo"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertNull(matchedSolution);
		
	}
	
	@Test
	public void Test_MatchOrSpecification1() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.OR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompOR_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		Assert.assertTrue(matchedSolution.get(0) instanceof ORGate);	
	}
	
	@Test
	public void Test_MatchOrSpecification2() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompOR_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertNull(matchedSolution);
	}
	
	@Test
	public void Test_MatchOrSpecification3() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompOR_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
	
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/contAssign4/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/contAssign4/FC2_y/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchOrSpecification4() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.OR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompOR_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		
		DecomposedGraphNode output = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/contAssign4/FC1_b/1.0"));
		output.setPreselectedComponentDefinition(URI.create("https://testrepo"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertNull(matchedSolution);
		
	}
	
	@Test
	public void Test_MatchSRLatchSpecification() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchPreSelected_SRLatchSpecification() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		
		DecomposedGraphNode s_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC1_s/1.0"));
		DecomposedGraphNode r_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC0_r/1.0"));
		
		s_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/IPTG/1"));
		r_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/aTc/1"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchPreSelected_SRLatchSpecification2() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		
		DecomposedGraphNode s_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC1_s/1.0"));
		DecomposedGraphNode r_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC0_r/1.0"));
		
		s_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/IPTG/1"));
		r_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/aTc/1"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchPreSelected_SRLatchSpecification3() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		
		DecomposedGraphNode s_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC1_s/1.0"));
		DecomposedGraphNode r_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC0_r/1.0"));
		DecomposedGraphNode q_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0"));
		
		s_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/IPTG/1"));
		r_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/aTc/1"));
		q_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/LacI_protein/1"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchPreSelected_SRLatchSpecification4() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		
		DecomposedGraphNode s_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC1_s/1.0"));
		DecomposedGraphNode r_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC0_r/1.0"));
		DecomposedGraphNode q_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0"));
		DecomposedGraphNode q_output = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0"));
		
		s_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/IPTG/1"));
		r_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/aTc/1"));
		q_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/LacI_protein/1"));
		q_output.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/YFP_protein/1"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchPreSelected_SRLatchSpecification5() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		DecomposedGraphNode s_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC1_s/1.0"));
		DecomposedGraphNode r_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC0_r/1.0"));
		DecomposedGraphNode q_output = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0"));
		
		s_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/IPTG/1"));
		r_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/aTc/1"));
		q_output.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/YFP_protein/1"));

		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchPreSelected_SRLatchSpecification6() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0")));
		Assert.assertEquals(2, matchedSolution.size());
	}
	
	@Test
	public void Test_MatchPreSelected_SRLatchSpecification7() throws GeneticGatesException, SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		List<GeneticGate> matchedSolution = m.getGateList(m.getSpecification().getRootNode());
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
		
		matchedSolution = m.getGateList(m.getSpecification().getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0")));
		Assert.assertEquals(1, matchedSolution.size());
	}
}

package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Cover;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Match;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.PreSelectedMatch;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapSolution;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapUtility;

/**
 * Test case to test Exhaustive covering algorithm
 * @author Tramy Nguyen
 */
public class Exhaustive_Test {

	@Test
	public void Test1_Not() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(1, coverSols.size());
		TechMapSolution techSol = coverSols.stream().findFirst().get();
		Assert.assertTrue(techSol.getGateMapping().values().iterator().next() instanceof NOTGate);
	}
	
	@Test
	public void Test2_Not() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(0, coverSols.size());
	}
	
	@Test
	public void Test1_Srlatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(1, coverSols.size());
		TechMapSolution[] actualSols = coverSols.stream().toArray(TechMapSolution[]::new);
		Assert.assertEquals(actualSols[0].getScore(), libGraph.stream().mapToDouble(x->x.getDecomposedGraph().getRootNode().getScore()).sum(), 0.001);
	}
	
	@Test
	public void Test2_Srlatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1); 
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1); 
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(2, coverSols.size());
		TechMapSolution[] actualSols = coverSols.stream().toArray(TechMapSolution[]::new);
		
		DecomposedGraphNode q = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0"));
		DecomposedGraphNode qnot = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC4__1_/1.0"));
		DecomposedGraphNode out = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC2_q/1.0"));
		Assert.assertEquals(actualSols[0].getGateFromNode(q), actualSols[1].getGateFromNode(qnot));
		Assert.assertEquals(actualSols[0].getGateFromNode(q), actualSols[1].getGateFromNode(qnot));
		Assert.assertNotEquals(actualSols[0].getGateFromNode(out), actualSols[1].getGateFromNode(out));
		Assert.assertNotEquals(actualSols[0].getScore(), actualSols[1].getScore());
	}
	
	@Test
	public void Test3_Srlatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1); 
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1); 
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(1, coverSols.size());
		TechMapSolution[] actualSols = coverSols.stream().toArray(TechMapSolution[]::new);
		Assert.assertTrue(actualSols[0].getScore() == 6112.0);
		
	}
	
	@Test
	public void Test4_Srlatch_signalMatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1); 
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(0, coverSols.size());
		
	}
	
	@Test
	public void Test5_Srlatch_crosstalk() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(0, coverSols.size());
		
	}
	
	@Test
	public void Test6_Srlatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.OR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(0, coverSols.size());
		
	}
	
	@Test
	public void Test7_Srlatch_preselect() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1); 
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1); ;
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		DecomposedGraphNode q_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0"));
		q_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/LacI_protein/1"));
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		Set<TechMapSolution> coverSols = c.exhaustiveCover();	
		Assert.assertEquals(1, coverSols.size());
	}
	

}

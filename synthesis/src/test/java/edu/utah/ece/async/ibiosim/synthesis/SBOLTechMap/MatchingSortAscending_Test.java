package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Match;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.PreSelectedMatch;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapUtility;

/**
 * Test sort in ascending order for solutions produced from Matching step. 
 * @author tramyn
 *
 */
public class MatchingSortAscending_Test {

	@Test
	public void Test_LibSize1_small() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertTrue(libGraph.size() == 1);
		libGraph.get(0).getDecomposedGraph().getRootNode().setScore(5);
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		m.sortAscendingOrder(libGraph);
		Assert.assertTrue(libGraph.get(0).getDecomposedGraph().getRootNode().getScore() == 5.0);
		
	}
	
	@Test
	public void Test_LibSize2_small() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertTrue(libGraph.size() == 2);
		libGraph.get(0).getDecomposedGraph().getRootNode().setScore(15);
		libGraph.get(1).getDecomposedGraph().getRootNode().setScore(5);
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		m.sortAscendingOrder(libGraph);
		Assert.assertTrue(libGraph.get(0).getDecomposedGraph().getRootNode().getScore() == 5.0);
		Assert.assertTrue(libGraph.get(1).getDecomposedGraph().getRootNode().getScore() == 15.0);
	}
	
	@Test
	public void Test1_LibSize3_duplicates() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertTrue(libGraph.size() == 3);
		libGraph.get(0).getDecomposedGraph().getRootNode().setScore(5);
		libGraph.get(1).getDecomposedGraph().getRootNode().setScore(15);
		libGraph.get(2).getDecomposedGraph().getRootNode().setScore(5);
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		m.sortAscendingOrder(libGraph);
		Assert.assertTrue(libGraph.get(0).getDecomposedGraph().getRootNode().getScore() == 5.0);
		Assert.assertTrue(libGraph.get(1).getDecomposedGraph().getRootNode().getScore() == 5.0);
		Assert.assertTrue(libGraph.get(2).getDecomposedGraph().getRootNode().getScore() == 15.0);
	}
	
	@Test
	public void Test2_LibSize3() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertTrue(libGraph.size() == 3);
		libGraph.get(0).getDecomposedGraph().getRootNode().setScore(1);
		libGraph.get(1).getDecomposedGraph().getRootNode().setScore(2);
		libGraph.get(2).getDecomposedGraph().getRootNode().setScore(3);
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		m.sortAscendingOrder(libGraph);
		Assert.assertTrue(libGraph.get(0).getDecomposedGraph().getRootNode().getScore() == 1.0);
		Assert.assertTrue(libGraph.get(1).getDecomposedGraph().getRootNode().getScore() == 2.0);
		Assert.assertTrue(libGraph.get(2).getDecomposedGraph().getRootNode().getScore() == 3.0);
	}
	
	@Test
	public void Test3_LibSize3() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertTrue(libGraph.size() == 3);
		libGraph.get(0).getDecomposedGraph().getRootNode().setScore(3);
		libGraph.get(1).getDecomposedGraph().getRootNode().setScore(2);
		libGraph.get(2).getDecomposedGraph().getRootNode().setScore(1);
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		m.sortAscendingOrder(libGraph);
		Assert.assertTrue(libGraph.get(0).getDecomposedGraph().getRootNode().getScore() == 1.0);
		Assert.assertTrue(libGraph.get(1).getDecomposedGraph().getRootNode().getScore() == 2.0);
		Assert.assertTrue(libGraph.get(2).getDecomposedGraph().getRootNode().getScore() == 3.0);
	}
	
	@Test
	public void Test4_LibSize3() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertTrue(libGraph.size() == 3);
		libGraph.get(0).getDecomposedGraph().getRootNode().setScore(0);
		libGraph.get(1).getDecomposedGraph().getRootNode().setScore(1);
		libGraph.get(2).getDecomposedGraph().getRootNode().setScore(-1);
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		m.sortAscendingOrder(libGraph);
		Assert.assertTrue(libGraph.get(0).getDecomposedGraph().getRootNode().getScore() == -1.0);
		Assert.assertTrue(libGraph.get(1).getDecomposedGraph().getRootNode().getScore() == 0.0);
		Assert.assertTrue(libGraph.get(2).getDecomposedGraph().getRootNode().getScore() == 1.0);
	}
	
	@Test
	public void Test_LibSize10() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize2);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertTrue(libGraph.size() == 10);
		libGraph.get(0).getDecomposedGraph().getRootNode().setScore(100309);
		libGraph.get(1).getDecomposedGraph().getRootNode().setScore(150993);
		libGraph.get(2).getDecomposedGraph().getRootNode().setScore(38);
		libGraph.get(3).getDecomposedGraph().getRootNode().setScore(13209);
		libGraph.get(4).getDecomposedGraph().getRootNode().setScore(2.0);
		libGraph.get(5).getDecomposedGraph().getRootNode().setScore(88.923789234978);
		libGraph.get(6).getDecomposedGraph().getRootNode().setScore(8743895);
		libGraph.get(7).getDecomposedGraph().getRootNode().setScore(32532403);
		libGraph.get(8).getDecomposedGraph().getRootNode().setScore(101.3283932);
		libGraph.get(9).getDecomposedGraph().getRootNode().setScore(9609839329809808989.0);
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		m.sortAscendingOrder(libGraph);
		Assert.assertTrue(libGraph.get(0).getDecomposedGraph().getRootNode().getScore() == 2.0);
		Assert.assertTrue(libGraph.get(1).getDecomposedGraph().getRootNode().getScore() == 38.0);
		Assert.assertTrue(libGraph.get(2).getDecomposedGraph().getRootNode().getScore() == 88.923789234978);
		Assert.assertTrue(libGraph.get(3).getDecomposedGraph().getRootNode().getScore() == 101.3283932);
		Assert.assertTrue(libGraph.get(4).getDecomposedGraph().getRootNode().getScore() == 13209);
		Assert.assertTrue(libGraph.get(5).getDecomposedGraph().getRootNode().getScore() == 100309);
		Assert.assertTrue(libGraph.get(6).getDecomposedGraph().getRootNode().getScore() == 150993);
		Assert.assertTrue(libGraph.get(7).getDecomposedGraph().getRootNode().getScore() == 8743895);
		Assert.assertTrue(libGraph.get(8).getDecomposedGraph().getRootNode().getScore() == 32532403);
		Assert.assertTrue(libGraph.get(9).getDecomposedGraph().getRootNode().getScore() == 9609839329809808989.0);
	}
}

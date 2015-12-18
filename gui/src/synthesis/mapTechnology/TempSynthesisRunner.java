package synthesis.mapTechnology;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TempSynthesisRunner
{ 	
	public static void main(String[] args)
	{
		String PATH = "/Users/tramynguyen/Desktop/SBOL/";

		//Set up specification graph
		String path1 = PATH + "spec.sbol";
		File SPEC_FILE = new File(path1);
		Synthesis syn = new Synthesis();
		syn.createSBOLGraph(SPEC_FILE, false);
		syn.getSpecification().createDotFile(PATH + "SPEC2.dot");

		//Set up library graph
		String path2 = PATH + "LIBRARY_ANNOT.sbol";
		File LIBR_FILE = new File(path2);
		syn.createSBOLGraph(LIBR_FILE, true);

		//Set library gate scores
		List<SBOLGraph> library = syn.getLibrary();
		setLibraryGateScores(library);
		Map<SynthesisNode, LinkedList<WeightedGraph>> matches = new HashMap<SynthesisNode, LinkedList<WeightedGraph>>();
		Map<SynthesisNode, SBOLGraph> coveredGates = new HashMap<SynthesisNode, SBOLGraph>();
		syn.match_topLevel(syn.getSpecification(), matches);
//		printMatches(matches);
		
		syn.cover_topLevel(syn.getSpecification().getOutputNode(), matches);
//		coveredGates = syn.cover(syn.getSpecification().getOutputNode(), matches, coveredGates, Double.POSITIVE_INFINITY);
//		printCoveredGates(coveredGates);
	} 

	public static void printMatches(Map<SynthesisNode, LinkedList<WeightedGraph>> matches)
	{
		for (Map.Entry<SynthesisNode, LinkedList<WeightedGraph>> entry : matches.entrySet())
		{
			for(WeightedGraph g: entry.getValue())
			{
				System.out.println(entry.getKey().toString() + "/" + g.getSBOLGraph().getOutputNode());
			}
		}
	}
	
	public static void printCoveredGates(Map<SynthesisNode, SBOLGraph> coveredGates)
	{
		for(SBOLGraph g : coveredGates.values())
		{
			System.out.println(g.getOutputNode().toString());
		}
	}

	public static void setLibraryGateScores(List<SBOLGraph> library)
	{
		for(SBOLGraph g: library)
		{
			if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("INV0"))
			{
				g.getOutputNode().setScore(5);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("INV1"))
			{
				g.getOutputNode().setScore(15);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("INV2"))
			{
				g.getOutputNode().setScore(15);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("INV3"))
			{
				g.getOutputNode().setScore(20);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("NOR0"))
			{
				g.getOutputNode().setScore(40);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("NOR1"))
			{
				g.getOutputNode().setScore(45);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("NOR2"))
			{
				g.getOutputNode().setScore(50);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("NOR3"))
			{
				g.getOutputNode().setScore(50);
			}
			else if(g.getOutputNode().getModuleDefinition().getDisplayId().equals("X1"))
			{
				g.getOutputNode().setScore(30);
			}	
		}
	}
}

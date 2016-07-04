package analysis.dynamicsim.hierarchical.tests.unit;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import analysis.dynamicsim.hierarchical.methods.HierarchicalODERKSimulator;
import analysis.dynamicsim.hierarchical.methods.HierarchicalSSADirectSimulator;
import analysis.dynamicsim.hierarchical.states.ModelState;

public class SetupTest
{

	/**
	 * Test inital assignments
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	@Test
	public void test00() throws IOException, XMLStreamException
	{
		String SBMLFile = "./gui/src/analysis/dynamicsim/hierarchical/tests/resources/test00.xml";
		HierarchicalODERKSimulator odeSim = new HierarchicalODERKSimulator(SBMLFile, "", 10);
		ModelState topModel = odeSim.getTopmodel();
		assert (topModel.getNode("A").getValue() == 3);
		assert (topModel.getNode("B").getValue() == 2);
		assert (topModel.getNode("C").getValue() == 1);

	}

	/**
	 * Test inital assignments
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	@Test
	public void test03() throws IOException, XMLStreamException
	{
		String SBMLFile = "./gui/src/analysis/dynamicsim/hierarchical/tests/resources/test03.xml";
		new HierarchicalODERKSimulator(SBMLFile, "", 10);

	}

	/**
	 * 
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	@Test
	public void test04() throws IOException, XMLStreamException
	{
		String SBMLFile = "./gui/src/analysis/dynamicsim/hierarchical/tests/resources/test04.xml";
		HierarchicalODERKSimulator odeSim = new HierarchicalODERKSimulator(SBMLFile, "", 10);
		odeSim.simulate();

	}

	/**
	 * 
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	@Test
	public void test05() throws IOException, XMLStreamException
	{
		String SBMLFile = "./gui/src/analysis/dynamicsim/hierarchical/tests/resources/test05.xml";
		HierarchicalSSADirectSimulator ssaSim = new HierarchicalSSADirectSimulator(SBMLFile, "", "", 1, 10, 0, 1, false);
		ssaSim.simulate();

	}

	/**
	 * 
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	@Test
	public void test06() throws IOException, XMLStreamException
	{
		String SBMLFile = "./gui/src/analysis/dynamicsim/hierarchical/tests/resources/test06.xml";
		HierarchicalSSADirectSimulator ssaSim = new HierarchicalSSADirectSimulator(SBMLFile, "", "", 1, 10, 0, 1, false);
		ssaSim.simulate();

	}

	/**
	 * 
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	@Test
	public void test07() throws IOException, XMLStreamException
	{
		String SBMLFile = "./gui/src/analysis/dynamicsim/hierarchical/tests/resources/test07.xml";
		HierarchicalSSADirectSimulator ssaSim = new HierarchicalSSADirectSimulator(SBMLFile, "", "", 1, 10, 0, 1, false);
		ssaSim.simulate();

	}

	/**
	 * 
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	@Test
	public void test11() throws IOException, XMLStreamException
	{
		String SBMLFile = "./gui/src/analysis/dynamicsim/hierarchical/tests/resources/test11.xml";
		HierarchicalSSADirectSimulator ssaSim = new HierarchicalSSADirectSimulator(SBMLFile, "", "", 1, 10, 0, 1, false);
		ssaSim.initialize(0, 1);
	}

}

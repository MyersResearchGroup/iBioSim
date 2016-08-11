package analysis.dynamicsim.hierarchical.tests.unit;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

import analysis.dynamicsim.hierarchical.methods.HierarchicalODERKSimulator;
import analysis.dynamicsim.hierarchical.methods.HierarchicalSSADirectSimulator;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;

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
		odeSim.initialize(0, 1);
		HierarchicalModel topModel = odeSim.getTopmodel();
		Assert.assertTrue(topModel.getNode("A").getValue() == 2);
		Assert.assertTrue(topModel.getNode("B").getValue() == 1);
		Assert.assertTrue(topModel.getNode("C").getValue() == 3);

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
		HierarchicalSSADirectSimulator ssaSim = new HierarchicalSSADirectSimulator(SBMLFile, "", "", 1, 10, 0, 1, 0, 0, false);
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
		HierarchicalSSADirectSimulator ssaSim = new HierarchicalSSADirectSimulator(SBMLFile, "", "", 1, 10, 0, 1, 0, 0, false);
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
		HierarchicalSSADirectSimulator ssaSim = new HierarchicalSSADirectSimulator(SBMLFile, "", "", 1, 10, 0, 1, 0, 0, false);
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
		HierarchicalSSADirectSimulator ssaSim = new HierarchicalSSADirectSimulator(SBMLFile, "", "", 1, 10, 0, 1, 0, 0, false);
		ssaSim.initialize(0, 1);
	}

}

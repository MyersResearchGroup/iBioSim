package analysis.dynamicsim.hierarchical.tests.unit;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class UtilitiesTest
{

	HierarchicalModel	modelstate;

	@Before
	public void setUp() throws Exception
	{
		modelstate = new HierarchicalModel("top");
		modelstate.addVariable(new VariableNode("a", 0));
		modelstate.addConstant(new VariableNode("b", 2));
		modelstate.getNode("a").setAssignmentRule(modelstate.getNode("b"));
	}

	@Test
	public void test()
	{
		HierarchicalModel clone = modelstate.clone();
		Assert.assertTrue(clone.getNode("a") != null);
	}

}

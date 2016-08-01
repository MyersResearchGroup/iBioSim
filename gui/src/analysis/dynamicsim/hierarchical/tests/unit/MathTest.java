package analysis.dynamicsim.hierarchical.tests.unit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;

import analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;
import analysis.dynamicsim.hierarchical.util.interpreter.RateSplitterInterpreter;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class MathTest
{

	@Test
	public void test01() throws ParseException
	{
		ASTNode node = ASTNode.parseFormula("k * (a - b)");
		ASTNode split0 = ASTNode.parseFormula("k * a");
		ASTNode split1 = ASTNode.parseFormula("k * (-b) ");
		List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
		assert (listOfNodes.size() == 2);
		assert (listOfNodes.get(0).equals(split0));
		assert (listOfNodes.get(1).equals(split1));
	}

	@Test
	public void test02() throws ParseException
	{
		ASTNode node = ASTNode.parseFormula("k * (a * (b - c) )");
		ASTNode split0 = ASTNode.parseFormula("k * a * b");
		ASTNode split1 = ASTNode.parseFormula("- k * b * c ");
		List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
		assert (listOfNodes.size() == 2);
		assert (listOfNodes.get(0).equals(split0));
		assert (listOfNodes.get(1).equals(split1));
	}

	@Test
	public void test03() throws ParseException
	{
		ASTNode node = ASTNode.parseFormula("(a + b) * (c - d)");
		List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
		assert (listOfNodes.size() == 4);
	}

	@Test
	public void test04() throws ParseException
	{
		ASTNode node = ASTNode.parseFormula("(a + b) * (c + d)");
		List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
		assert (listOfNodes.size() == 4);
	}

	@Test
	public void test05() throws ParseException
	{
		ASTNode node = ASTNode.parseFormula("1/(a - b)");
		List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
		assert (listOfNodes.size() == 1);
	}

	@Test
	public void test06() throws ParseException
	{
		ASTNode node = ASTNode.parseFormula("(a + b * (c - d)) * (e + f)");
		List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
		assert (listOfNodes.size() == 1);
	}

	@Test
	public void test07() throws ParseException
	{
		Map<String, VariableNode> variableToNodes = new HashMap<String, VariableNode>();
		variableToNodes.put("a", new VariableNode("a", 0));
		variableToNodes.put("b", new VariableNode("b", 0));
		ASTNode node = ASTNode.parseFormula("( a + 1) - b");
		HierarchicalNode newNode = MathInterpreter.parseASTNode(node, variableToNodes);
		HierarchicalNode copyNode = new HierarchicalNode(newNode);
		assert (newNode.toString().equals(copyNode.toString()));
	}

	@Test
	public void test08() throws ParseException
	{
		Map<String, VariableNode> variableToNodes = new HashMap<String, VariableNode>();
		variableToNodes.put("a", new VariableNode("a", 0));
		ASTNode node = ASTNode.parseFormula("a");
		HierarchicalNode newNode = MathInterpreter.parseASTNode(node, variableToNodes);
		HierarchicalNode copyNode = newNode.clone();
		assert (newNode.toString().equals(copyNode.toString()));

		assert (newNode.getChild(0) != copyNode.getChild(0));

		((VariableNode) copyNode).setValue(1);

		assert (((VariableNode) newNode).getValue() == 0);
		assert (((VariableNode) copyNode).getValue() == 0);
	}

	@Test
	public void test09() throws ParseException
	{
		Map<String, VariableNode> variableToNodes = new HashMap<String, VariableNode>();
		variableToNodes.put("a", new VariableNode("a", 2));
		variableToNodes.put("b", new VariableNode("b", 2));
		ASTNode node = ASTNode.parseFormula("a+b");
		HierarchicalNode newNode = MathInterpreter.parseASTNode(node, variableToNodes);
		MathInterpreter.replaceNameNodes(newNode, variableToNodes);

		assert (newNode.toString() == "2 + 2");
	}
}

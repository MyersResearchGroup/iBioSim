package analysis.dynamicsim.hierarchical.tests.unit;

import java.util.List;

import org.junit.Test;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;

import analysis.dynamicsim.hierarchical.util.interpreter.RateSplitterInterpreter;

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
}

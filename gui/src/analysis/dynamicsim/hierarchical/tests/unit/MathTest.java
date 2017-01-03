package analysis.dynamicsim.hierarchical.tests.unit;


public class MathTest
{

	// @Test
	// public void test01() throws ParseException
	// {
	// ASTNode node = ASTNode.parseFormula("k * (a - b)");
	// ASTNode split0 = ASTNode.parseFormula("k*a");
	// ASTNode split1 = ASTNode.parseFormula("-(k*b)");
	// List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
	// Assert.assertTrue(listOfNodes.size() == 2);
	// Assert.assertTrue(listOfNodes.get(0).equals(split0));
	// Assert.assertTrue(listOfNodes.get(1).equals(split1));
	// }
	//
	// @Test
	// public void test02() throws ParseException
	// {
	// ASTNode node = ASTNode.parseFormula("k * (a * (b - c) )");
	// ASTNode split0 = ASTNode.parseFormula("k*(a*b)");
	// ASTNode split1 = ASTNode.parseFormula("-(k*(a*c))");
	// List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
	// Assert.assertTrue(listOfNodes.size() == 2);
	// Assert.assertTrue(listOfNodes.get(0).equals(split0));
	// Assert.assertTrue(listOfNodes.get(1).equals(split1));
	// }
	//
	// @Test
	// public void test03() throws ParseException
	// {
	// ASTNode node = ASTNode.parseFormula("(a + b) * (c - d)");
	// List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
	// Assert.assertTrue(listOfNodes.size() == 4);
	// }
	//
	// @Test
	// public void test04() throws ParseException
	// {
	// ASTNode node = ASTNode.parseFormula("(a + b) * (c + d)");
	// List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
	// Assert.assertTrue(listOfNodes.size() == 4);
	// }
	//
	// @Test
	// public void test05() throws ParseException
	// {
	// ASTNode node = ASTNode.parseFormula("1/(a - b)");
	// List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
	// Assert.assertTrue(listOfNodes.size() == 1);
	// }
	//
	// @Test
	// public void test06() throws ParseException
	// {
	// ASTNode node = ASTNode.parseFormula("(a + b * (c - d)) * (e + f)");
	// List<ASTNode> listOfNodes = RateSplitterInterpreter.parseASTNode(node);
	// Assert.assertTrue(listOfNodes.size() == 6);
	// }
	//
	// @Test
	// public void test07() throws ParseException
	// {
	// Map<String, VariableNode> variableToNodes = new HashMap<String,
	// VariableNode>();
	// VariableNode a = new VariableNode("a", 0);
	// variableToNodes.put("a", a);
	// VariableNode b = new VariableNode("b", 0);
	// variableToNodes.put("b", b);
	// ASTNode node = ASTNode.parseFormula("( a + 1) - b");
	// HierarchicalNode newNode = MathInterpreter.parseASTNode(node,
	// variableToNodes);
	// HierarchicalNode copyNode = newNode.clone();
	// Assert.assertTrue(newNode.toString().equals(copyNode.toString()));
	// }
	//
	// @Test
	// public void test08() throws ParseException
	// {
	// Map<String, VariableNode> variableToNodes = new HashMap<String,
	// VariableNode>();
	// VariableNode a = new VariableNode("a", 0);
	// variableToNodes.put("a", a);
	// ASTNode node = ASTNode.parseFormula("a");
	// HierarchicalNode newNode = MathInterpreter.parseASTNode(node,
	// variableToNodes);
	// HierarchicalNode copyNode = newNode.clone();
	// Assert.assertTrue(newNode.toString().equals(copyNode.toString()));
	//
	// Assert.assertTrue(newNode != copyNode);
	//
	// ((VariableNode) copyNode).setValue(1);
	//
	// Assert.assertTrue(((VariableNode) newNode).getValue() == 0);
	// Assert.assertTrue(((VariableNode) copyNode).getValue() == 1);
	// }
	//
	// @Test
	// public void test09() throws ParseException
	// {
	//
	// Map<String, VariableNode> variableToNodes = new HashMap<String,
	// VariableNode>();
	// Map<String, VariableNode> dimensionMap = new HashMap<String,
	// VariableNode>();
	// VariableNode a = new VariableNode("a", 2);
	// VariableNode b = new VariableNode("b", 2);
	// VariableNode c = new VariableNode("c", 2);
	// dimensionMap.put("a", a);
	// dimensionMap.put("b", b);
	// variableToNodes.put("c", c);
	// ASTNode node = ASTNode.parseFormula("a+b");
	// HierarchicalNode newNode = MathInterpreter.parseASTNode(node,
	// dimensionMap);
	// newNode = MathInterpreter.copyMath(newNode, dimensionMap, true);
	//
	// Assert.assertTrue(newNode.toString().equals("(PLUS 2.0 2.0)"));
	//
	// node = ASTNode.parseFormula("a");
	// newNode = MathInterpreter.parseASTNode(node, dimensionMap);
	// newNode = MathInterpreter.copyMath(newNode, dimensionMap, true);
	//
	// Assert.assertTrue(newNode.toString().equals("2.0"));
	//
	// node = ASTNode.parseFormula("c");
	// newNode = MathInterpreter.parseASTNode(node, variableToNodes);
	// newNode = MathInterpreter.copyMath(newNode, dimensionMap, true);
	//
	// Assert.assertTrue(!newNode.toString().equals("c"));
	// }

}

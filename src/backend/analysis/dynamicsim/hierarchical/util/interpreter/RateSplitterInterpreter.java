package backend.analysis.dynamicsim.hierarchical.util.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.sbml.jsbml.ASTNode;

public final class RateSplitterInterpreter
{

	public static List<ASTNode> parseASTNode(ASTNode math)
	{
		List<ASTNode> listOfNodes = new ArrayList<ASTNode>();
		List<ASTNode> tmp1, tmp2;
		switch (math.getType())
		{
		case PRODUCT:
		case TIMES:
			if (needsRefiniment(math))
			{
				tmp1 = parseASTNode(math.getChild(0));
				for (int i = 1; i < math.getChildCount(); i++)
				{
					tmp2 = parseASTNode(math.getChild(i));
					tmp1 = getCombinations(tmp1, tmp2);
				}

				listOfNodes = tmp1;
			}
			else
			{
				listOfNodes.add(math);
			}
			break;
		case SUM:
		case PLUS:
			for (int i = 0; i < math.getChildCount(); i++)
			{
				listOfNodes.addAll(parseASTNode(math.getChild(i)));
			}
			break;
		case MINUS:
			if (math.getNumChildren() == 1)
			{
				tmp1 = parseASTNode(math.getChild(0));
				for (ASTNode node : tmp1)
				{
					listOfNodes.add(ASTNode.uMinus(node));
				}
			}
			else
			{
				listOfNodes = parseASTNode(math.getChild(0));
				tmp2 = new ArrayList<ASTNode>();
				for (int i = 1; i < math.getChildCount(); i++)
				{
					tmp2.addAll(parseASTNode(math.getChild(i)));
				}
				for (ASTNode node : tmp2)
				{
					listOfNodes.add(ASTNode.uMinus(node));
				}

			}
			break;
		default:
			listOfNodes.add(math);
			break;
		}
		return listOfNodes;
	}

	private static boolean needsRefiniment(ASTNode node)
	{

		for (ASTNode child : node.getChildren())
		{
			if (!child.isLeaf())
			{
				return true;
			}
		}
		return false;
	}

	private static List<ASTNode> getCombinations(List<ASTNode> A, List<ASTNode> B)
	{
		List<ASTNode> listOfNodes = new ArrayList<ASTNode>();

		for (ASTNode a : A)
		{
			for (ASTNode b : B)
			{
				if (a.isUMinus() && b.isUMinus())
				{
					ASTNode product = new ASTNode(ASTNode.Type.TIMES);
					product.addChild(a);
					product.addChild(b);
					listOfNodes.add(product);
				}
				else if (a.isUMinus())
				{
					ASTNode product = new ASTNode(ASTNode.Type.TIMES);
					product.addChild(a.getChild(0));
					product.addChild(b);
					listOfNodes.add(ASTNode.uMinus(product));
				}
				else if (b.isUMinus())
				{
					ASTNode product = new ASTNode(ASTNode.Type.TIMES);
					product.addChild(a);
					product.addChild(b.getChild(0));
					listOfNodes.add(ASTNode.uMinus(product));
				}
				else
				{
					ASTNode product = new ASTNode(ASTNode.Type.TIMES);
					product.addChild(a);
					product.addChild(b);
					listOfNodes.add(product);
				}
			}
		}

		return listOfNodes;
	}
}

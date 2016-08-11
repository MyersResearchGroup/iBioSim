package analysis.dynamicsim.hierarchical.util.math;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReactionNode extends VariableNode
{

	private List<SpeciesReferenceNode>	reactants;
	private List<SpeciesReferenceNode>	products;
	private HierarchicalNode			forwardRate;
	private HierarchicalNode			reverseRate;
	private boolean						hasEnoughMoleculesFd;
	private boolean						hasEnoughMoleculesRv;
	private double						forwardRateValue;
	private double						reverseRateValue;
	private ValueNode					totalPropensityRef;
	private ValueNode					modelPropensityRef;
	private double						initPropensity;
	private double						initForwardPropensity;

	public ReactionNode(String name)
	{
		super(name, 0);
		isReaction = true;
	}

	public ReactionNode(ReactionNode copy)
	{
		super(copy);
		isReaction = true;
	}

	public List<SpeciesReferenceNode> getListOfReactants()
	{
		return reactants;
	}

	public void addReactant(SpeciesReferenceNode speciesRef)
	{
		if (reactants == null)
		{
			reactants = new ArrayList<SpeciesReferenceNode>();
		}
		speciesRef.getSpecies().addReactionDependency(this);
		reactants.add(speciesRef);
	}

	public void addProduct(SpeciesReferenceNode speciesRef)
	{
		if (products == null)
		{
			products = new ArrayList<SpeciesReferenceNode>();
		}

		speciesRef.getSpecies().addReactionDependency(this);
		products.add(speciesRef);
	}

	public void setForwardRate(HierarchicalNode kineticLaw)
	{
		this.forwardRate = kineticLaw;
	}

	public HierarchicalNode getForwardRate()
	{
		return forwardRate;
	}

	public HierarchicalNode getReverseRate()
	{
		return forwardRate;
	}

	public boolean hasEnoughMolecules()
	{
		return hasEnoughMoleculesFd;
	}

	public boolean computePropensity()
	{
		double oldValue = value;
		if (forwardRate != null)
		{
			forwardRateValue = Evaluator.evaluateExpressionRecursive(forwardRate);
			value = forwardRateValue;
		}
		if (reverseRate != null)
		{
			reverseRateValue = Evaluator.evaluateExpressionRecursive(reverseRate);
			value = forwardRateValue + reverseRateValue;
		}
		if (totalPropensityRef != null)
		{
			totalPropensityRef.setValue(totalPropensityRef.value + (value - oldValue));
		}
		if (modelPropensityRef != null)
		{
			modelPropensityRef.setValue(modelPropensityRef.value + (value - oldValue));
		}
		return oldValue != value;
	}

	private void fireReaction(boolean hasEnoughMolecules, List<SpeciesReferenceNode> reactants, List<SpeciesReferenceNode> products)
	{
		if (hasEnoughMolecules)
		{
			Set<ReactionNode> dependentReactions = new HashSet<ReactionNode>();

			if (reactants != null)
			{
				for (SpeciesReferenceNode specRef : reactants)
				{
					double stoichiometry = specRef.getStoichiometry();
					SpeciesNode speciesNode = specRef.getSpecies();
					speciesNode.setValue(speciesNode.getValue() - stoichiometry);
					dependentReactions.addAll(speciesNode.getReactionDependents());
				}
			}

			if (products != null)
			{
				for (SpeciesReferenceNode specRef : products)
				{
					double stoichiometry = specRef.getStoichiometry();
					SpeciesNode speciesNode = specRef.getSpecies();
					speciesNode.setValue(speciesNode.getValue() + stoichiometry);
					dependentReactions.addAll(speciesNode.getReactionDependents());
				}
			}
			updateDependentReactions(dependentReactions);
		}
	}

	public void fireReaction(double threshold)
	{
		computeNotEnoughEnoughMolecules();
		if (forwardRateValue >= threshold)
		{
			fireReaction(hasEnoughMoleculesFd, reactants, products);
		}
		else
		{
			fireReaction(hasEnoughMoleculesRv, products, reactants);
		}
	}

	private void updateDependentReactions(Set<ReactionNode> dependentReactions)
	{
		for (ReactionNode reaction : dependentReactions)
		{
			reaction.computePropensity();
		}
	}

	public void computeNotEnoughEnoughMolecules()
	{

		hasEnoughMoleculesFd = true;
		if (reactants != null)
		{
			for (SpeciesReferenceNode specRef : reactants)
			{
				if (specRef.getSpecies().getValue() < specRef.getValue())
				{
					hasEnoughMoleculesFd = false;
					break;
				}
			}
		}
		if (reverseRate != null)
		{
			hasEnoughMoleculesRv = true;
			if (products != null)
			{
				for (SpeciesReferenceNode specRef : products)
				{
					if (specRef.getSpecies().getValue() < specRef.getValue())
					{
						hasEnoughMoleculesRv = false;
						return;
					}
				}
			}
		}
	}

	public void setModelPropensityRef(ValueNode ref)
	{
		this.modelPropensityRef = ref;
	}

	public void setTotalPropensityRef(ValueNode ref)
	{
		this.totalPropensityRef = ref;
	}

	public void setReverseRate(HierarchicalNode rate)
	{
		this.reverseRate = rate;
	}

	public void setInitPropensity()
	{
		this.initPropensity = value;
		this.initForwardPropensity = forwardRateValue;
	}

	public void restoreInitPropensity()
	{
		this.value = initPropensity;
		this.forwardRateValue = initForwardPropensity;
		this.reverseRateValue = value - forwardRateValue;
	}

	@Override
	public ReactionNode clone()
	{
		return new ReactionNode(this);
	}
}

package analysis.dynamicsim.hierarchical.util.math;

public class SpeciesNode extends VariableNode
{

	private VariableNode		compartment;
	private boolean				isBoundary;
	private boolean				hasOnlySubstance;
	private HierarchicalNode	speciesRateEquation;
	private HierarchicalNode	odeRate;

	public SpeciesNode(String name, double value)
	{
		super(name, value);
		this.isSpecies = true;
	}

	public SpeciesNode(SpeciesNode copy)
	{
		this(copy.name, copy.value);
		this.isBoundary = copy.isBoundary;
		this.hasOnlySubstance = copy.hasOnlySubstance;
		this.compartment = copy.compartment;
	}

	public void setCompartment(VariableNode compartment)
	{
		this.compartment = compartment;
	}

	public VariableNode getCompartment()
	{
		return compartment;
	}

	public double getConcentration()
	{
		return this.getValue() / compartment.getValue();
	}

	public boolean isBoundaryCondition()
	{
		return isBoundary;
	}

	public void setBoundaryCondition(boolean isBoundary)
	{
		this.isBoundary = isBoundary;
	}

	public boolean hasOnlySubstance()
	{
		return hasOnlySubstance;
	}

	public void setHasOnlySubstance(boolean substance)
	{
		this.hasOnlySubstance = substance;
	}

	public HierarchicalNode getSpeciesRateEquation()
	{
		return speciesRateEquation;
	}

	public HierarchicalNode getODERate()
	{
		return odeRate;
	}

	public void addODERate(ReactionNode reactionNode, SpeciesReferenceNode specRefNode)
	{
		if (odeRate == null)
		{
			odeRate = new HierarchicalNode(Type.PLUS);
		}

		HierarchicalNode reactionRate = new HierarchicalNode(Type.TIMES);
		reactionRate.addChild(reactionNode);
		reactionRate.addChild(specRefNode);
		odeRate.addChild(reactionRate);
	}

	public void subtractODERate(ReactionNode reactionNode, SpeciesReferenceNode specRefNode)
	{
		if (odeRate == null)
		{
			odeRate = new HierarchicalNode(Type.PLUS);
		}

		HierarchicalNode sub = new HierarchicalNode(Type.MINUS);

		HierarchicalNode reactionRate = new HierarchicalNode(Type.TIMES);

		reactionRate.addChild(reactionNode);

		reactionRate.addChild(specRefNode);

		sub.addChild(reactionRate);

		odeRate.addChild(sub);
	}

	@Override
	public boolean computeInitialValue()
	{
		if (initAssign == null)
		{
			return false;
		}

		double oldValue = value;
		double newValue = Evaluator.evaluateExpressionRecursive(initAssign, false);
		this.value = newValue;
		boolean isNaN = Double.isNaN(oldValue) && Double.isNaN(newValue);
		return !isNaN && oldValue != newValue;
	}

	@Override
	public boolean computeAssignmentValue()
	{
		if (assignRule == null)
		{
			return false;
		}
		double oldValue = value;
		double newValue = Evaluator.evaluateExpressionRecursive(assignRule, false);

		this.value = newValue;
		boolean isNaN = Double.isNaN(oldValue) && Double.isNaN(newValue);
		return !isNaN && oldValue != newValue;
	}

	@Override
	public double computeRateOfChange(double time)
	{
		double rate = 0;
		if (rateRule != null)
		{
			rate = Evaluator.evaluateExpressionRecursive(rateRule);

			if (!hasOnlySubstance)
			{
				double compartmentChange = compartment.computeRateOfChange(time);
				if (compartmentChange != 0)
				{
					rate = rate * compartment.getValue();
					rate = rate + value / compartment.getValue() * compartmentChange;
				}
				else
				{
					rate = rate * compartment.getValue();
				}
			}
		}
		else if (odeRate != null && !isBoundary)
		{
			rate = Evaluator.evaluateExpressionRecursive(odeRate);
		}
		return rate;
	}

	@Override
	public SpeciesNode clone()
	{
		return new SpeciesNode(this);
	}

	public void setODERate(HierarchicalNode odeRate)
	{
		this.odeRate = odeRate;
	}
}

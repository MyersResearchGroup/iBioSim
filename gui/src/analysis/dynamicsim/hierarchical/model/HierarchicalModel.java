package analysis.dynamicsim.hierarchical.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.util.math.ConstraintNode;
import analysis.dynamicsim.hierarchical.util.math.EventNode;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;
import biomodel.util.GlobalConstants;

public final class HierarchicalModel extends Container
{

	private boolean						isInitSet;
	private ModelType					type;

	private Set<String>					deletedBySId;
	private Set<String>					deletedByMetaId;

	private List<EventNode>				events;
	private List<VariableNode>			variables;
	private List<VariableNode>			constants;
	private List<ReactionNode>			reactions;
	private List<VariableNode>			arrays;
	private List<ConstraintNode>		constraints;

	private Map<String, VariableNode>	idToNode;

	// TODO: keep track of child model states because needed when cloning

	public HierarchicalModel(String submodelID)
	{
		super(submodelID);
	}

	public HierarchicalModel(HierarchicalModel state)
	{
		super(state);
		this.type = state.type;
	}

	public void clear()
	{

	}

	public void copyConstraints(HierarchicalModel state)
	{
		// TODO:
	}

	public void copyEvents(HierarchicalModel state)
	{
		// TODO:
	}

	public void copyReactions(HierarchicalModel state)
	{
		// TODO:
	}

	@Override
	public HierarchicalModel clone()
	{
		return new HierarchicalModel(this);
	}

	public boolean isInitSet()
	{
		return isInitSet;
	}

	public Map<String, VariableNode> createVariableToNodeMap()
	{
		Map<String, VariableNode> variableToNodes = new HashMap<String, VariableNode>();
		this.idToNode = variableToNodes;
		return variableToNodes;
	}

	public Map<String, VariableNode> getVariableToNodeMap()
	{
		return idToNode;
	}

	public ReactionNode addReaction(String variable)
	{
		ReactionNode node = new ReactionNode(variable);
		addReaction(node);
		return node;
	}

	public ReactionNode addReaction(ReactionNode node)
	{
		if (reactions == null)
		{
			reactions = new ArrayList<ReactionNode>();
		}
		if (idToNode == null)
		{
			idToNode = new HashMap<String, VariableNode>();
		}
		reactions.add(node);
		idToNode.put(node.getName(), node);
		return node;
	}

	public VariableNode addConstant(String variable, double value)
	{

		VariableNode node = new VariableNode(variable, value);

		return addConstant(node);
	}

	public VariableNode addConstant(VariableNode node)
	{

		node.setIsVariableConstant(true);
		if (constants == null)
		{
			constants = new ArrayList<VariableNode>();
		}
		if (idToNode == null)
		{
			idToNode = new HashMap<String, VariableNode>();
		}

		constants.add(node);
		idToNode.put(node.getName(), node);
		return node;
	}

	public EventNode addEvent(HierarchicalNode triggerNode)
	{

		EventNode node = new EventNode(triggerNode);
		return addEvent(node);
	}

	public EventNode addEvent(EventNode node)
	{

		if (events == null)
		{
			events = new LinkedList<EventNode>();
		}

		events.add(node);
		return node;
	}

	public VariableNode addArray(VariableNode node)
	{

		if (arrays == null)
		{
			arrays = new ArrayList<VariableNode>();
		}

		if (idToNode == null)
		{
			idToNode = new HashMap<String, VariableNode>();
		}
		arrays.add(node);
		idToNode.put(node.getName(), node);
		return node;
	}

	public VariableNode addVariable(String variable, double value)
	{
		VariableNode node = new VariableNode(variable, value);
		return addVariable(node);
	}

	public VariableNode addVariable(VariableNode node)
	{

		if (variables == null)
		{
			variables = new ArrayList<VariableNode>();
		}
		if (idToNode == null)
		{
			idToNode = new HashMap<String, VariableNode>();
		}

		variables.add(node);
		idToNode.put(node.getName(), node);
		return node;
	}

	public ConstraintNode addConstraint(String variable, HierarchicalNode node)
	{

		if (constraints == null)
		{
			constraints = new ArrayList<ConstraintNode>();
		}

		ConstraintNode constraintNode = new ConstraintNode(variable, node);

		constraints.add(constraintNode);

		return constraintNode;
	}

	public List<ConstraintNode> getListOfConstraints()
	{
		return constraints;
	}

	public void addMappingNode(String variable, VariableNode node)
	{
		idToNode.put(variable, node);
	}

	public VariableNode getNode(String variable)
	{
		return idToNode.get(variable);
	}

	public List<VariableNode> getVariables()
	{
		return variables;
	}

	public List<VariableNode> getConstants()
	{
		return constants;
	}

	public List<ConstraintNode> getConstraints()
	{
		return constraints;
	}

	public void addDeletedBySid(String id)
	{

		if (deletedBySId == null)
		{
			deletedBySId = new HashSet<String>();
		}

		deletedBySId.add(id);
	}

	public void addDeletedByMetaId(String metaid)
	{

		if (deletedByMetaId == null)
		{
			deletedByMetaId = new HashSet<String>();
		}

		deletedByMetaId.add(metaid);
	}

	public boolean isDeletedBySId(String sid)
	{

		if (deletedBySId == null)
		{
			return false;
		}

		return deletedBySId.contains(sid);
	}

	public boolean isDeletedByMetaId(String metaid)
	{

		if (deletedByMetaId == null)
		{
			return false;
		}

		return deletedByMetaId.contains(metaid);
	}

	public int getNumOfVariables()
	{
		return variables == null ? 0 : variables.size();
	}

	public int getNumOfReactions()
	{
		return reactions == null ? 0 : reactions.size();
	}

	public int getNumOfEvents()
	{
		return events == null ? 0 : events.size();
	}

	public int getNumOfConstants()
	{
		return constants == null ? 0 : constants.size();
	}

	public int getNumOfConstraints()
	{
		return constraints == null ? 0 : constraints.size();
	}

	public int getNumOfArrays()
	{
		return arrays == null ? 0 : arrays.size();
	}

	public List<VariableNode> getArrays()
	{
		return arrays;
	}

	public List<ReactionNode> getReactions()
	{
		return reactions;
	}

	public List<EventNode> getEvents()
	{
		return events;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ModelState [ID=" + getID() + "]";
	}

	public ModelType getModelType()
	{
		return type;
	}

	public void setModelType(ModelType type)
	{
		this.type = type;
	}

	public void setModelType(Model model)
	{
		if (model == null)
		{
			type = ModelType.NONE;
		}
		else if (model.getSBOTerm() == GlobalConstants.SBO_FLUX_BALANCE)
		{
			type = ModelType.HFBA;
		}
		else if (model.getSBOTerm() == GlobalConstants.SBO_NONSPATIAL_CONTINUOUS)
		{
			type = ModelType.HODE;
		}
		else
		{
			type = ModelType.HSSA;
		}
	}

}
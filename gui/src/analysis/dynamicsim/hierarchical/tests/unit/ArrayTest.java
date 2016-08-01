package analysis.dynamicsim.hierarchical.tests.unit;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.math.ArrayNode.ArraysType;
import analysis.dynamicsim.hierarchical.util.math.EventAssignmentNode;
import analysis.dynamicsim.hierarchical.util.math.EventNode;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesReferenceNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;
import analysis.dynamicsim.hierarchical.util.setup.ArraysSetup;

public class ArrayTest
{
	Species					species;
	Compartment				compartment;
	Parameter				parameter;
	AssignmentRule			rule;
	Reaction				reaction;
	SpeciesReference		speciesReference;
	Event					event;
	EventAssignment			eventAssignment;

	SpeciesNode				node;
	VariableNode			compNode;
	VariableNode			varNode;
	ReactionNode			reactionNode;
	SpeciesReferenceNode	speciesRefNode;
	EventNode				eventNode;
	EventAssignmentNode		eventAssignNode;

	@Before
	public void setUp() throws Exception
	{
		species = new Species(3, 1);
		compartment = new Compartment(3, 1);
		parameter = new Parameter(3, 1);
		rule = new AssignmentRule(3, 1);
		event = new Event(3, 1);
		eventAssignment = new EventAssignment(3, 1);

		compartment.setId("y");
		compartment.setValue(1);
		species.setHasOnlySubstanceUnits(false);
		species.setConstant(false);
		species.setBoundaryCondition(false);
		species.setValue(0.0);
		species.setCompartment("y");
		species.setId("x");
		parameter.setConstant(false);
		parameter.setValue(0);
		parameter.setId("z");

		rule.setVariable(parameter.getId());
		rule.setMath(new ASTNode("d0"));

		ArraysSBasePlugin arraysSBasePlugin = new ArraysSBasePlugin(compartment);
		compartment.addExtension(ArraysConstants.shortLabel, arraysSBasePlugin);
		Dimension d0 = arraysSBasePlugin.createDimension("d0");
		d0.setArrayDimension(0);
		d0.setSize("n");

		Index ind0 = arraysSBasePlugin.createIndex();
		ind0.setReferencedAttribute("compartment");
		ind0.setArrayDimension(0);
		ind0.setMath(new ASTNode("d0"));

		arraysSBasePlugin = new ArraysSBasePlugin(species);
		species.addExtension(ArraysConstants.shortLabel, arraysSBasePlugin);
		d0 = arraysSBasePlugin.createDimension("d0");
		d0.setArrayDimension(0);
		d0.setSize("n");

		arraysSBasePlugin = new ArraysSBasePlugin(parameter);
		parameter.addExtension(ArraysConstants.shortLabel, arraysSBasePlugin);
		d0 = arraysSBasePlugin.createDimension("d0");
		d0.setArrayDimension(0);
		d0.setSize("n");

		arraysSBasePlugin = new ArraysSBasePlugin(rule);
		rule.addExtension(ArraysConstants.shortLabel, arraysSBasePlugin);
		d0 = arraysSBasePlugin.createDimension("d0");
		d0.setArrayDimension(0);
		d0.setSize("n");

		ind0 = arraysSBasePlugin.createIndex();
		ind0.setReferencedAttribute("variable");
		ind0.setArrayDimension(0);
		ind0.setMath(new ASTNode("d0"));

		reaction = new Reaction(3, 1);
		reaction.setId("r");
		speciesReference = new SpeciesReference(3, 1);
		speciesReference.setSpecies(species);
		speciesReference.setId("sr");
		speciesReference.setStoichiometry(1);

		arraysSBasePlugin = new ArraysSBasePlugin(speciesReference);
		speciesReference.addExtension(ArraysConstants.shortLabel, arraysSBasePlugin);
		d0 = arraysSBasePlugin.createDimension("rd0");
		d0.setArrayDimension(0);
		d0.setSize("n");

		ind0 = arraysSBasePlugin.createIndex();
		ind0.setReferencedAttribute("species");
		ind0.setArrayDimension(0);
		ind0.setMath(new ASTNode("rd0"));

		arraysSBasePlugin = new ArraysSBasePlugin(reaction);
		reaction.addExtension(ArraysConstants.shortLabel, arraysSBasePlugin);
		d0 = arraysSBasePlugin.createDimension("d0");
		d0.setArrayDimension(0);
		d0.setSize("n");

		reaction.addReactant(speciesReference);
		KineticLaw kinetic = reaction.createKineticLaw();
		kinetic.setMath(ASTNode.parseFormula("x[d0]"));

		Trigger trigger = new Trigger();
		trigger.setMath(new ASTNode("d0"));
		event.setTrigger(trigger);
		arraysSBasePlugin = new ArraysSBasePlugin(event);
		event.addExtension(ArraysConstants.shortLabel, arraysSBasePlugin);
		d0 = arraysSBasePlugin.createDimension("d0");
		d0.setArrayDimension(0);
		d0.setSize("n");

		eventAssignment.setMath(ASTNode.parseFormula("ed0"));
		eventAssignment.setVariable(parameter.getId());
		arraysSBasePlugin = new ArraysSBasePlugin(eventAssignment);
		eventAssignment.addExtension(ArraysConstants.shortLabel, arraysSBasePlugin);
		d0 = arraysSBasePlugin.createDimension("ed0");
		d0.setArrayDimension(0);
		d0.setSize("n");
		ind0 = arraysSBasePlugin.createIndex();
		ind0.setReferencedAttribute("variable");
		ind0.setArrayDimension(0);
		ind0.setMath(new ASTNode("ed0"));

		node = new SpeciesNode(species.getId(), species.getValue());
		compNode = new VariableNode(compartment.getId(), compartment.getValue());
		varNode = new VariableNode(parameter.getId(), parameter.getValue());
		reactionNode = new ReactionNode(reaction.getId());
		speciesRefNode = new SpeciesReferenceNode(speciesReference.getId(), 3);
		speciesRefNode.setSpecies(node);
	}

	@Test
	public void test01() throws IOException, XMLStreamException
	{
		ModelState modelstate = new ModelState("toplevel");
		modelstate.addVariable(new VariableNode("n", 1));
		ArraysSetup.setupDimensions(modelstate, compartment, compNode, ArraysType.COMPARTMENT);
		ArraysSetup.linkDimensionSize(modelstate);
		ArraysSetup.expandArray(modelstate, compNode);

		ArraysSetup.setupDimensions(modelstate, species, node, ArraysType.SPECIES);
		ArraysSetup.setupIndices(modelstate, species, node.getArrayNode(), ArraysType.SPECIES);
		ArraysSetup.expandArray(modelstate, node);

		assert (node.getArrayNode().getChild(0) instanceof VariableNode);
	}

	@Test
	public void test02() throws IOException, XMLStreamException
	{
		ModelState modelstate = new ModelState("toplevel");
		modelstate.addVariable(new VariableNode("n", 1));
		ArraysSetup.setupDimensions(modelstate, parameter, varNode, ArraysType.PARAMETER);
		ArraysSetup.linkDimensionSize(modelstate);
		ArraysSetup.expandArray(modelstate, varNode);
		HierarchicalNode ruleNode = ArraysSetup.setupDimensions(modelstate, rule, rule.getMath(), ArraysType.ASSIGNRULE);
		ArraysSetup.setupIndices(modelstate, rule, ruleNode.getArrayNode(), ArraysType.ASSIGNRULE);
		ArraysSetup.expandArray(modelstate, ruleNode);

	}

	@Test
	public void test03() throws IOException, XMLStreamException
	{
		ModelState modelstate = new ModelState("toplevel");
		modelstate.addVariable(new VariableNode("n", 2));
		ArraysSetup.setupDimensions(modelstate, species, node, ArraysType.SPECIES);

		ArraysSetup.setupDimensions(modelstate, reaction, reactionNode, ArraysType.REACTION);

		ArraysSetup.setupDimensions(modelstate, speciesReference, speciesRefNode, reactionNode, ArraysType.REACTANT);
		ArraysSetup.setupIndices(modelstate, speciesReference, speciesRefNode.getArrayNode(), ArraysType.REACTANT);

		ArraysSetup.expandArray(modelstate, node);
		ArraysSetup.expandArray(modelstate, reactionNode);
		ArraysSetup.expandArray(modelstate, speciesRefNode, reactionNode);
	}

	@Test
	public void test04() throws IOException, XMLStreamException
	{
		ModelState modelstate = new ModelState("toplevel");
		modelstate.addVariable(new VariableNode("n", 2));
		ArraysSetup.setupDimensions(modelstate, parameter, varNode, ArraysType.PARAMETER);
		ArraysSetup.linkDimensionSize(modelstate);
		eventNode = (EventNode) ArraysSetup.setupDimensions(modelstate, event, event.getTrigger().getMath(), ArraysType.EVENT);
		eventAssignNode = (EventAssignmentNode) ArraysSetup.setupDimensions(modelstate, eventAssignment, eventAssignment.getMath(), eventNode, ArraysType.EVENTASSIGNMENT);
		eventAssignNode.setVariable(modelstate.getNode(parameter.getId()));
		ArraysSetup.setupIndices(modelstate, eventAssignment, eventAssignNode.getArrayNode(), ArraysType.EVENTASSIGNMENT);
		ArraysSetup.expandArray(modelstate, varNode);
		ArraysSetup.expandArray(modelstate, eventNode);
		ArraysSetup.expandArray(modelstate, eventAssignNode, eventNode);

	}
}

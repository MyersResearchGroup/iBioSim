package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.Trigger;

/**
 * A Utility class that is used to encode the Verilog syntax using the SBML data model.
 * 
 * @author Tramy Nguyen
 *
 */
public class WrappedSBML {
	private SBMLDocument sbmlDocument;
	private Model model;
	private CompModelPlugin compPlugin;
	private CompSBMLDocumentPlugin docPlugin;
	private int placeCounter, transitionCounter, waitCounter, delayCounter;
	private int condCounter, assignCounter;
	private List<String> inputs;
	private List<String> outputs;
	private Map<String, String> submodulRef;
	
	// Constants
	private final ASTNode node_true = new ASTNode(Type.CONSTANT_TRUE);
	private final ASTNode node_zero = new ASTNode(0);
	private final ASTNode node_one = new ASTNode(1);
	private final String sbmlCompPack = "comp";
	
	public WrappedSBML() {
		this.sbmlDocument = new SBMLDocument(3,1);
		this.model = sbmlDocument.createModel(); 
		this.compPlugin = (CompModelPlugin) model.createPlugin(sbmlCompPack);
		this.docPlugin = (CompSBMLDocumentPlugin) sbmlDocument.createPlugin(sbmlCompPack);
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.submodulRef = new HashMap<String, String>();

		initializeRandomFunctions();
	}

	public void addAssignmentToTransition(Event event, String variable, String expression) {
		try {
			ASTNode expressionMath = ASTNode.parseFormula(expression);
			event.createEventAssignment(variable, expressionMath);
		} catch (ParseException e) {
			e.printStackTrace(); 
		}
	}
	/**
	 * 
	 * @param booleanId
	 */
	public void addBoolean(String booleanId) {
		Parameter bool = model.createParameter(booleanId);
		bool.setValue(0);
		bool.setConstant(false);
		bool.setSBOTerm(602);
	}

	/**
	 * 
	 * @param booleanId
	 * @param assignmentMath
	 */
	public void addInitialAssignment(String booleanId, ASTNode assignmentMath) {
		InitialAssignment initialAssignment = model.createInitialAssignment();
		initialAssignment.setVariable(booleanId);
		initialAssignment.setMath(assignmentMath);
	}
	
	public void addAssignmentRule(String booleanId, ASTNode assignmentMath) {
		AssignmentRule assignRule = model.createAssignmentRule();
		assignRule.setVariable(booleanId);
		assignRule.setMath(assignmentMath);
	}

	/**
	 * Add the Verilog input to the SBML model.
	 * @param input: Name of the Verilog input
	 */
	public void addInput(String input) {
		this.inputs.add(input);
		addBoolean(input);
		String portId = model.getId() + "__" + input;
		Port port = compPlugin.createPort(portId);
		port.setIdRef(input);
		port.setSBOTerm(600); 
	}

	public ExternalModelDefinition addExternalModuleDefinition(String moduleInstance) {
		ExternalModelDefinition modelDef = this.docPlugin.createExternalModelDefinition(moduleInstance);
		modelDef.setSource(moduleInstance + ".xml");
		return modelDef;
	}
	
	public Model addModuleDefinition(String moduleInstance) {
		ModelDefinition modelDef = (ModelDefinition) this.docPlugin.createModelDefinition(moduleInstance);
		return modelDef;
	}
	
	/**
	 * 
	 * @param moduleInstance: Name of the module that is being referenced 
	 * @param moduleInstanceName: The instantiated module name
	 */
	public Submodel addSubmodel(String moduleInstance, String moduleInstanceName) {
		Submodel submodel = this.compPlugin.createSubmodel(moduleInstanceName);
		submodel.setModelRef(moduleInstance); 
		this.submodulRef.put(moduleInstanceName, moduleInstance);
		return submodel;
	}
	
	private CompSBasePlugin getCompPlugin(String elementId) {
		SBase element = model.getElementBySId(elementId);
		if(element.isSetPlugin(sbmlCompPack)) {
			return (CompSBasePlugin) element.getPlugin(sbmlCompPack);
		}
		CompSBasePlugin newPlugin = (CompSBasePlugin) element.createPlugin(sbmlCompPack);
		return newPlugin;
	}
	
	private ReplacedElement createReplacement(String modelRef, String submodelId, String replacedElement) {
		ReplacedElement replacement = new ReplacedElement();
		replacement.setSubmodelRef(submodelId);
		String portName = modelRef + "__" + replacedElement;
		replacement.setPortRef(portName);
		return replacement;
	}
	
	public void addReplacement(String topElementId, String modelRef, String submodelId, String replacedElement) {
		CompSBasePlugin plugin = getCompPlugin(topElementId);
		ReplacedElement replacement = createReplacement(modelRef, submodelId, replacedElement);
		plugin.addReplacedElement(replacement);
	}

	public void addReplacedBy(String topElementId, String modelRef, String submodelId, String replacedElement) {
		CompSBasePlugin plugin = getCompPlugin(topElementId);
		ReplacedBy replacedBy = plugin.createReplacedBy();
		replacedBy.setSubmodelRef(submodelId);
		String portName = modelRef + "__" + replacedElement;
		replacedBy.setPortRef(portName);
	}
 
	/**
	 * Add the Verilog output to the SBML model.
	 * @param outputPort: Name of the Verilog output
	 */
	public void addOutput(String output) {
		this.outputs.add(output);
		addBoolean(output);

		String portId = model.getId() + "__" + output;
		Port port = compPlugin.createPort(portId);
		port.setIdRef(output);
		port.setSBOTerm(601); 
	}

	public Event closeNet() {
		String transitionName = getNextTransitionId();
		Event transition = createTransition(transitionName);
		return transition;
	}

	public Event createAssignmentTransition() {
		String assignId = getNextAssignId();
		Event assignTransition = createTransition(assignId);
		return assignTransition;
		
	}

	public Event createConditionalTransition() {
		String ifId = getNextConditionalId();
		Event ifTransition = createTransition(ifId);
		return ifTransition;
	}

	
	public Event createWaitTransition() {
		String waitId = getNextWaitId();
		Event waitTransition = createTransition(waitId);
		return waitTransition;
	}

	/**
	 * WrappedSBML creates an SBML model for each verilog module that was converted. 
	 * @return The SBML model
	 */
	public Model getModel() {
		return model;
	}
	
	public SBMLDocument getSBMLDocument() {
		return this.sbmlDocument;
	}

	/**
	 * Print the current SBMLDocument to the command line.
	 */
	public void printSBMLDocument() {
		try {
			SBMLWriter.write(this.sbmlDocument, System.out, ' ', (short) 4);
		} catch (SBMLException e) {
			System.err.println("ERROR: Invalid SBML.");
			e.printStackTrace();
		} catch (XMLStreamException e) {
			System.err.println("ERROR: Invalid XML.");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param moduleId
	 */
	public void setModuleID(String moduleId) {
		this.model.setId(moduleId);
	}

	/**
	 * Add movement from transition to postset place.
	 * 
	 * @param transitionName
	 */
	public void addMovementToPlace(Event transitionEvent, String placeName) {
		transitionEvent.createEventAssignment(placeName, node_one);
	}

	/**
	 * 
	 * @param presetPlaces
	 * @param transitionEvent
	 * @param condition
	 */
	public void addMovementToTransition(HashSet<String> presetPlaces, Event transitionEvent, ASTNode condition) {
		ASTNode triggerMath = createTransitionTrigger(condition, presetPlaces);
		Trigger trigger = transitionEvent.createTrigger();
		trigger.setMath(triggerMath);
		trigger.setInitialValue(false);
		trigger.setPersistent(false);
		for(String preset : presetPlaces) {
			transitionEvent.createEventAssignment(preset, node_zero);
		}
	}


	public String addPlace(boolean initialValue) {
		String placeName = getNextPlaceId();
		Parameter bool = model.createParameter(placeName);
		if(initialValue) {
			bool.setValue(1);
		} else {
			bool.setValue(0);
		}
		bool.setConstant(false);
		bool.setSBOTerm(593);
		return placeName;
	}
	
	public Event createDelay(String delayValue) {
		String delayName = getNextDelayId();
		Event delayEvent = createTransition(delayName);
		delayEvent.createDelay(createASTNode(delayValue));
		return delayEvent;
	}
	
	private ASTNode createASTNode(String math) {
		try {
			double value = Double.parseDouble(math);
			return new ASTNode(value); 
		}
		catch(NumberFormatException e) {
			//TODO:	log this exception 
		}
		return new ASTNode(math);
	}

	private Event createTransition(String transitionName) {
		Event transition = model.createEvent(transitionName);
		transition.setUseValuesFromTriggerTime(false);
		transition.setSBOTerm(591);
		return transition;
	}

	private ASTNode createTransitionTrigger(ASTNode triggerCondition, HashSet<String> presets) {
		ASTNode triggerMath = new ASTNode(Type.LOGICAL_AND);
		triggerMath.addChild(triggerCondition);
		for(String placeId : presets) {
			triggerMath.addChild(variableEquality(placeId));
		} 
		return triggerMath;
	}


	/**
	 * 
	 * @return
	 */
	public String getNextAssignId() {
		return "assign_" + assignCounter++;
	}

	/**
	 * 
	 * @return
	 */
	public String getNextConditionalId() {
		return "if_" + condCounter++;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getNextPlaceId() {
		return "P" + placeCounter++;
	}

	/**
	 * 
	 * @return
	 */
	public String getNextTransitionId() {
		return "T" + transitionCounter++;
	}

	/**
	 * 
	 * @return
	 */
	public String getNextWaitId() {
		return "wait_" + waitCounter++;
	}
	
	public String getNextDelayId() {
		return "delay_" + delayCounter++;
	}

	public ASTNode getTrueNode() {
		return node_true;
	}
	
	private ASTNode variableEquality(String variable) {
		ASTNode equalityMath = new ASTNode(Type.RELATIONAL_EQ);
		equalityMath.addChild(new ASTNode(variable));
		equalityMath.addChild(new ASTNode(1));
		return equalityMath;
	}
	
	public List<String> getInputList() {
		return this.inputs;
	}
	
	public List<String> getOutputList() {
		return this.outputs;
	}
	
	public Map<String, String> getSubmoduleReferences() {
		return this.submodulRef;
	}
	
	private void initializeRandomFunctions() {
		try {
			FunctionDefinition unif = this.model.createFunctionDefinition("uniform");
			ASTNode math = ASTNode.parseFormula("lambda(a,b,(a+b)/2)");
			unif.setMath(math);
		} 
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
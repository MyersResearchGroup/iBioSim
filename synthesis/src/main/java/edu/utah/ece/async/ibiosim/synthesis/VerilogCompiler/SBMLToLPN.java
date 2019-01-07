package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedElement;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.lema.verification.lpn.LPN;

/**
 * Class to perform conversion from event-based SBML data model to an LPN model.
 * @author Tramy Nguyen
 */
public class SBMLToLPN {

	private WrappedSBML impWrapper, tbWrapper;
	private SBMLDocument sbmlDocument;
	private LPN lpn;
	private HashMap<String, Integer> parameterConstants;
	private Set<String> ignoreTransitions;
	private Set<String> ignoreVariables;
	private Set<String> places;
	private ArrayList<String> eventAssignmentBooleans;
	private ASTNode ignoreNode = new ASTNode(Type.CONSTANT_TRUE);
	
	/**
	 * The SBML to LPN constructor needed to setup the necessary variables before running conversion.
	 * @param tbWrapper: The SBML testbench of the modeling circuit. Set this parameter to null if the SBML document has no external ports to convert.
	 * @param impWrapper: The SBML circuit. Set this parameter to null if the SBML document has no external ports to convert.
	 * @param sbmlDocument: The event-based SBML document to perform conversion
	 */
	public SBMLToLPN(WrappedSBML tbWrapper, WrappedSBML impWrapper, SBMLDocument sbmlDocument) {
		this.sbmlDocument = sbmlDocument;
		this.impWrapper = impWrapper;
		this.tbWrapper = tbWrapper;
		this.parameterConstants = new HashMap<>();

		this.places = new HashSet<>();
		this.ignoreTransitions = new HashSet<>();
		this.ignoreVariables = new HashSet<>();
		this.eventAssignmentBooleans = new ArrayList<>();
	}
	
	
	/**
	 * Convert SBML to LPN.
	 * @return The LPN model
	 */
	public LPN convert() {
		lpn = new LPN();
		
		convertSBMLParameters();
		addTransitions();
		convertPorts();
		convertSBMLEvents();

		return lpn;
	}
	
	/**
	 * Convert the given SBML document to an LPN model.
	 * @param tbWrapper
	 * @param impWrapper
	 * @param sbmlDocument: The SBML document to convert into LPN. 
	 * @return
	 */
	public static LPN convertSBMLtoLPN(WrappedSBML tbWrapper, WrappedSBML impWrapper, SBMLDocument sbmlDocument) { 
		SBMLToLPN converter = new SBMLToLPN(tbWrapper, impWrapper, sbmlDocument);
		return converter.convert();
	}
	

	private void convertPorts() {
		Model model = sbmlDocument.getModel();
		Model tbModel = tbWrapper.getModel();
		CompModelPlugin modelPlugin = (CompModelPlugin) impWrapper.getModel().getPlugin("comp");
		
		boolean isOutput = false;
		boolean isInput = false;
		for(Parameter parameter : model.getListOfParameters()) {
			isOutput = false;
			isInput = false;
			//Go over all parameters of flat model that are marked as booleans
			if(parameter.getSBOTerm() == 602) {
				String id = parameter.getId();
				if(!ignoreVariables.contains(id)) {
					Parameter tbParameter = tbModel.getParameter(id);
					if(tbParameter != null) {
						CompSBasePlugin plugin = (CompSBasePlugin) tbParameter.getPlugin("comp");
						if(plugin != null) {
							for (ReplacedElement replacement : plugin.getListOfReplacedElements()) {
								String submoduleRefName = tbWrapper.getSubmoduleReferences().get(replacement.getSubmodelRef());
								if(submoduleRefName != null && impWrapper.getModel().getId().equals(submoduleRefName)) {
									String portRef = replacement.getPortRef();
									Port port = modelPlugin.getPort(portRef);
									if(port.getSBOTerm() == 601) {
										isOutput = true;
										break;
									}
									else if(port.getSBOTerm() == 600) {
										isInput = true;
										break;
									}
								}
							}
						}
					}
					if(isOutput) {
						lpn.addOutput(id, "false");
					} 
					else if(isInput){
						lpn.addInput(id, "false");
					}
					else {
						lpn.addBoolean(id, "false");
					}
				}
			}
		}
	}

	private void convertSBMLParameters() {
		List<Parameter> parameters = sbmlDocument.getModel().getListOfParameters();
		for(Parameter param : parameters) {
			boolean ic = false;
			if(param.getValue() == 1) {
				ic = true;
			}

			//A place that is set to true indicates the starting position of an LPN
			if(param.getSBOTerm() == 593) {
				lpn.addPlace(param.getId(), ic);
				places.add(param.getId());
			}
			else if(param.getSBOTerm() == 602) {
				eventAssignmentBooleans.add(param.getId());
			}

			if(param.isConstant()) {
				parameterConstants.put(param.getId(), (int) param.getValue());
			}
		}
	}

	private void convertSBMLEvents() {
		List<Event> events = sbmlDocument.getModel().getListOfEvents();

		for(Event event : events) {
			convertSBMLEventAssignments(event);
			convertSBMLTriggers(event);
			convertSBMLDelay(event);
		}
	}

	private void addTransitions() {

		List<Event> events = sbmlDocument.getModel().getListOfEvents();

		for(Event event : events) {
			for(EventAssignment eventAssign : event.getListOfEventAssignments()) {
				String variable = eventAssign.getVariable();
				String assignment = eventAssign.getMath().toString();

				//Look for SBML eventAssignments that should be ignored when converting to lpn. 
				//Specifically, look for when random value between 0 and 1 are generated in the SBML model.
				if(assignment.equals("piecewise(1, uniform(0, 1) < 0.5, 0)")) {
					ignoreVariables.add(variable);
					ignoreTransitions.add(event.getId());
					break;
				}
			}
			lpn.addTransition(event.getId());
			
		}
	}

	private void convertSBMLDelay(Event event){
		if(event.isSetDelay()) {
			Delay delay = event.getDelay();
			String delayValue = convertSBMLASTNode(delay.getMath()); 
			lpn.changeDelay(event.getId(), delayValue);
		}

	}

	private void convertSBMLEventAssignments(Event event) {
		for(EventAssignment eventAssign : event.getListOfEventAssignments()) {
			String variable = eventAssign.getVariable();
			ASTNode assignment = eventAssign.getMath();
			if(places.contains(variable)) {
				if(assignment.getInteger() == 0) {
					lpn.addMovement(variable, event.getId());
				} 
				else{
					lpn.addMovement(event.getId(), variable);
				}
			} 
			else {
				if(!ignoreTransitions.contains(event.getId())) {
					lpn.addBoolAssign(event.getId(), variable, convertSBMLASTNode(assignment));
				}
			}
			
		}
	}

	private void convertSBMLTriggers(Event event) {
		if(event.isSetTrigger()) {
			Trigger trigger = event.getTrigger();
			ASTNode math = removePresetFromASTNode(trigger.getMath());			
			String enabling = convertSBMLASTNode(math);
			lpn.addEnabling(event.getId(), enabling);
		}

	}
	
	private ASTNode removePresetFromASTNode(ASTNode math) {
		ASTNode clone = math.clone();
		Queue<ASTNode> queue = new LinkedList<>();
		queue.add(clone);
		
		while(!queue.isEmpty()) {
			ASTNode node = queue.poll();
			
			if(node.getType() == ASTNode.Type.RELATIONAL_EQ) {
				if(node.getLeftChild().isName()) {
					if(places.contains(node.getLeftChild().getName())) {
						node.removeFromParent();
						continue;
					}
				}
			} else if(node.getType() == ASTNode.Type.NAME) {
				if(ignoreVariables.contains(node.getName())) {
					return ignoreNode;
				}
			}
			
			for(ASTNode child : node.getListOfNodes()) {
				queue.add(child);
			}
		}
		if (clone.getNumChildren() == 1) {
			return clone.getChild(0);
		}
		return clone;
		
		
	}

	private String convertSBMLASTNode(ASTNode mathNode) {
		return SBMLutilities.SBMLMathToLPNString(mathNode, parameterConstants, eventAssignmentBooleans);
	}

}

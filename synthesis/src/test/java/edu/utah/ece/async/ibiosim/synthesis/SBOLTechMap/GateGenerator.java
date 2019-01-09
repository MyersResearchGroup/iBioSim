package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogCompilerException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.WrappedSBOL;

/**
 * Build synthetic logic gates using the SBOL data format.
 * 
 * @author Tramy Nguyen
 */
public class GateGenerator {
	
	
	private WrappedSBOL sbolWrapper;
	private int mdCounter;
	
	public GateGenerator() {
		sbolWrapper = new WrappedSBOL();
	}

	public ModuleDefinition createNOTGate() throws SBOLValidationException, SBOLException, VerilogCompilerException {
		ModuleDefinition gate = sbolWrapper.createCircuit(getMDId() + "_NOTGate"); 
		FunctionalComponent tu = sbolWrapper.createTranscriptionalUnit(gate, "tu", sbolWrapper.generatePromoters(1));
		
		FunctionalComponent inputProtein = sbolWrapper.addProtein(gate, "inputProtein", DirectionType.IN);
		sbolWrapper.createInhibitionInteraction(gate, inputProtein, tu);
		
		FunctionalComponent outputProtein = sbolWrapper.addProtein(gate, "outputProtein", DirectionType.OUT);
		sbolWrapper.createProductionInteraction(gate, tu, outputProtein);
		
		return gate;
	}
	
	public ModuleDefinition createTandemPromoterGate() throws SBOLValidationException, SBOLException {
		ModuleDefinition gate = sbolWrapper.createCircuit(getMDId() + "_TandemPromoterNORGate");
		FunctionalComponent tu = sbolWrapper.createTranscriptionalUnit(gate, "tu", sbolWrapper.generatePromoters(2));
	
		FunctionalComponent inputProtein1 = sbolWrapper.addProtein(gate, "inputProtein1", DirectionType.IN);
		sbolWrapper.createInhibitionInteraction(gate, inputProtein1, tu);
		
		FunctionalComponent inputProtein2 = sbolWrapper.addProtein(gate, "inputProtein2", DirectionType.IN);
		sbolWrapper.createInhibitionInteraction(gate, inputProtein2, tu);
		
		FunctionalComponent outputProtein = sbolWrapper.addProtein(gate, "outputProtein", DirectionType.OUT);
		sbolWrapper.createProductionInteraction(gate, tu, outputProtein);
		return gate;
	}

	public SBOLDocument getGateSBOLDocument() {
		return sbolWrapper.getSBOLDocument();
	}

	private String getMDId() {
		return "MD" + this.mdCounter++;
	}
	
}

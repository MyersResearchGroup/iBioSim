package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;


import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.WrappedSBOL;

public class SyntheticGateExamples {

	public static SBOLDocument createORGate() throws SBOLValidationException, SBOLException {
		WrappedSBOL wrapper = new WrappedSBOL();
		ModuleDefinition ORGate = wrapper.createCircuit("OR_gate");
		ComponentDefinition exp_pro = wrapper.createPromoter();
		ComponentDefinition exp_cds = wrapper.createCDS();
		FunctionalComponent exp_pro_fc = ORGate.createFunctionalComponent("expected_Pro", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_cds_fc = ORGate.createFunctionalComponent("expected_CDS", AccessType.PUBLIC, exp_cds.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_y_protein = wrapper.addProtein(ORGate, "Y_protein", DirectionType.INOUT);
		FunctionalComponent exp_x0_protein = wrapper.addProtein(ORGate, "x0_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1_protein = wrapper.addProtein(ORGate, "x1_protein", DirectionType.INOUT);
		
		ComponentDefinition tu = wrapper.createEngineeredRegion();
		ComponentDefinition promoter = wrapper.createPromoter();
		ComponentDefinition rbs = wrapper.createRibosome();
		ComponentDefinition cds = wrapper.createCDS();
		ComponentDefinition terminator = wrapper.createTerminator();
		Component gen_pro  = tu.createComponent("Pro", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_rbs  = tu.createComponent("rbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("cds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("Ter", AccessType.PUBLIC, terminator.getIdentity());
		FunctionalComponent tu_fc = ORGate.createFunctionalComponent("orTU_fc", AccessType.PUBLIC, tu.getIdentity(), DirectionType.INOUT);		
		tu_fc.createMapsTo("expectedPro_mapsTo", RefinementType.USELOCAL, exp_pro_fc.getIdentity(), gen_pro.getIdentity());
		tu_fc.createMapsTo("expectedCDS_mapsTo", RefinementType.USELOCAL, exp_cds_fc.getIdentity(), gen_cds.getIdentity());
		
		
		ModuleDefinition y_protein_prod = wrapper.createCircuit("Y_protein_production");
		FunctionalComponent gen_y_protein = wrapper.addProtein(y_protein_prod, "Y_protein", DirectionType.INOUT);
		FunctionalComponent gen_cds_fc = y_protein_prod.createFunctionalComponent("gen_cds_fc", AccessType.PUBLIC, cds.getIdentity(), DirectionType.INOUT);		
		Interaction y_protein_inter = wrapper.createProductionInteraction(y_protein_prod, gen_cds_fc, gen_y_protein);
		Module y_prod_instance = ORGate.createModule("orGate_y_protein_production", y_protein_prod.getIdentity());
		y_prod_instance.createMapsTo("expected_y_protein_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y_protein.getIdentity());
		y_prod_instance.createMapsTo("expected_cds_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_cds_fc.getIdentity(), gen_cds_fc.getIdentity());
		
		ModuleDefinition x0_protein_act = wrapper.createCircuit("X0_protein_activation");
		FunctionalComponent gen_x0_protein = wrapper.addProtein(x0_protein_act, "X0_protein", DirectionType.INOUT);
		FunctionalComponent gen_pro_fc = x0_protein_act.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		Interaction x0_protein_inter = wrapper.createActivationInteraction(x0_protein_act, gen_x0_protein, gen_pro_fc);
		Module x0_act_instance = ORGate.createModule("orGate_X0_protein_activation", x0_protein_act.getIdentity());
		x0_act_instance.createMapsTo("expected_X0_protein_stim_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x0_protein.getIdentity(), gen_x0_protein.getIdentity());
		x0_act_instance.createMapsTo("expected_pro_stim1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_pro_fc.getIdentity());
		
		
		ModuleDefinition x1_protein_act = wrapper.createCircuit("X1_protein_activation");
		FunctionalComponent gen_x1_protein = wrapper.addProtein(x1_protein_act, "X1_protein", DirectionType.INOUT);
		FunctionalComponent gen_x1pro_fc = x1_protein_act.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		Interaction x1_protein_inter = wrapper.createActivationInteraction(x1_protein_act, gen_x1_protein, gen_x1pro_fc);
		Module x1_act_instance = ORGate.createModule("orGate_X1_protein_activation", x1_protein_act.getIdentity());
		x1_act_instance.createMapsTo("expected_X1_protein_stim_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1_protein.getIdentity(), gen_x1_protein.getIdentity());
		x1_act_instance.createMapsTo("expected_pro_stim2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_x1pro_fc.getIdentity());
		return wrapper.getSBOLDocument();
	}
	
	public static SBOLDocument testComponent() throws SBOLValidationException {
		SBOLUtility util = SBOLUtility.getInstance();
		SBOLDocument doc = util.createSBOLDocument();
		ComponentDefinition promoter = doc.createComponentDefinition("pro", "1.0", SequenceOntology.PROMOTER);
		ComponentDefinition rbs = doc.createComponentDefinition("rbs", "1.0", SequenceOntology.RIBOSOME_ENTRY_SITE);
		ComponentDefinition cds = doc.createComponentDefinition("cds", "1.0", SequenceOntology.CDS);
		ComponentDefinition terminator = doc.createComponentDefinition("ter", "1.0", SequenceOntology.TERMINATOR);
		
		ComponentDefinition tu = doc.createComponentDefinition("gen", "1.0", ComponentDefinition.DNA_REGION);
		tu.addRole(SequenceOntology.ENGINEERED_REGION);
		
		
		Component gen_pro  = tu.createComponent("pro", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_rbs  = tu.createComponent("rbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("cds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("ter", AccessType.PUBLIC, terminator.getIdentity());
		return doc;
	}
	
	
}

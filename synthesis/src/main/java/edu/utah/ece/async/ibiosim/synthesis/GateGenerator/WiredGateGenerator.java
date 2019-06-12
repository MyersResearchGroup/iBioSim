package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;


import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.WrappedSBOL;

public class WiredGateGenerator {
	
	private WrappedSBOL wrapper;

	
	WiredGateGenerator(){
		wrapper = new WrappedSBOL();
	}
	
	public ModuleDefinition createWiredOrGate(ComponentDefinition cd, SBOLDocument gateDoc) throws SBOLValidationException, SBOLException {
		wrapper.setSBOLDocument(gateDoc);
		ModuleDefinition gateMd = wrapper.createModuleDefinition(cd.getDisplayId() + "WiredOrGate");
		ComponentDefinition exp_pro = wrapper.createPromoter();
		ComponentDefinition exp_cds = wrapper.createCDS();
		FunctionalComponent exp_pro_fc = gateMd.createFunctionalComponent("expected_Pro", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_cds_fc = gateMd.createFunctionalComponent("expected_CDS", AccessType.PUBLIC, exp_cds.getIdentity(), DirectionType.INOUT);
		
		FunctionalComponent exp_y_protein = gateMd.createFunctionalComponent(cd.getDisplayId() + "Output", AccessType.PUBLIC, cd.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_x0_protein = gateMd.createFunctionalComponent(cd.getDisplayId() + "Input1", AccessType.PUBLIC, cd.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_x1_protein = gateMd.createFunctionalComponent(cd.getDisplayId() + "Input2", AccessType.PUBLIC, cd.getIdentity(), DirectionType.INOUT);
		
		ComponentDefinition tu = wrapper.createEngineeredRegion();
		ComponentDefinition promoter = wrapper.createPromoter();
		ComponentDefinition rbs = wrapper.createRibosome();
		ComponentDefinition cds = wrapper.createCDS();
		ComponentDefinition terminator = wrapper.createTerminator();
		Component gen_pro  = tu.createComponent("GenPro", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_rbs  = tu.createComponent("GenRbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("GenCds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("GenTer", AccessType.PUBLIC, terminator.getIdentity());
		
		tu.createSequenceConstraint("SequenceConstraint1", RestrictionType.PRECEDES, gen_pro.getIdentity(), gen_rbs.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint2", RestrictionType.PRECEDES, gen_rbs.getIdentity(), gen_cds.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint3", RestrictionType.PRECEDES, gen_cds.getIdentity(), gen_ter.getIdentity());
		
		FunctionalComponent tu_fc = gateMd.createFunctionalComponent("orTU_fc", AccessType.PUBLIC, tu.getIdentity(), DirectionType.INOUT);		
		tu_fc.createMapsTo("expectedPro_mapsTo", RefinementType.USELOCAL, exp_pro_fc.getIdentity(), gen_pro.getIdentity());
		tu_fc.createMapsTo("expectedCDS_mapsTo", RefinementType.USELOCAL, exp_cds_fc.getIdentity(), gen_cds.getIdentity());
			
		ModuleDefinition y_protein_prod = wrapper.createModuleDefinition(cd.getDisplayId() + "_production");
		FunctionalComponent gen_y_protein = y_protein_prod.createFunctionalComponent("Gen" + cd.getDisplayId(), AccessType.PUBLIC, cd.getIdentity(), DirectionType.INOUT);	
		FunctionalComponent gen_cds_fc = y_protein_prod.createFunctionalComponent("gen_cds_fc", AccessType.PUBLIC, cds.getIdentity(), DirectionType.INOUT);		
		wrapper.createProductionInteraction(y_protein_prod, gen_cds_fc, gen_y_protein);
		Module y_prod_instance = gateMd.createModule(cd.getDisplayId() + "_production_module", y_protein_prod.getIdentity());
		y_prod_instance.createMapsTo(cd.getDisplayId() + "_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y_protein.getIdentity());
		y_prod_instance.createMapsTo("cds_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_cds_fc.getIdentity(), gen_cds_fc.getIdentity());
		
		ModuleDefinition x0_protein_act = wrapper.createModuleDefinition(cd.getDisplayId() + "_activation");
		FunctionalComponent gen_x0_protein = x0_protein_act.createFunctionalComponent("Gen" + cd.getDisplayId(), AccessType.PUBLIC, cd.getIdentity(), DirectionType.INOUT);	
		FunctionalComponent gen_pro_fc = x0_protein_act.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createActivationInteraction(x0_protein_act, gen_x0_protein, gen_pro_fc);
		Module x0_act_instance = gateMd.createModule(cd.getDisplayId() + "_activation_module", x0_protein_act.getIdentity());
		x0_act_instance.createMapsTo(cd.getDisplayId() + "_stim_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x0_protein.getIdentity(), gen_x0_protein.getIdentity());
		x0_act_instance.createMapsTo("pro_stim1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_pro_fc.getIdentity());
		
		
		ModuleDefinition x1_protein_act = wrapper.createModuleDefinition(cd.getDisplayId() + "_activation");
		FunctionalComponent gen_x1_protein = x1_protein_act.createFunctionalComponent("Gen" + cd.getDisplayId(), AccessType.PUBLIC, cd.getIdentity(), DirectionType.INOUT);	
		FunctionalComponent gen_x1pro_fc = x1_protein_act.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createActivationInteraction(x1_protein_act, gen_x1_protein, gen_x1pro_fc);
		Module x1_act_instance = gateMd.createModule(cd.getDisplayId() + "_activation_module", x1_protein_act.getIdentity());
		x1_act_instance.createMapsTo(cd.getDisplayId() + "_stim_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1_protein.getIdentity(), gen_x1_protein.getIdentity());
		x1_act_instance.createMapsTo("pro_stim2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_x1pro_fc.getIdentity());
		
		return gateMd;
	}
	
	
}

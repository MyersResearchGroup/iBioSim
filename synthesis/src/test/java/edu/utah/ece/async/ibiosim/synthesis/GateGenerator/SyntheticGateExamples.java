package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;


import java.io.File;
import java.io.IOException;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.WrappedSBOL;

/**
 * Genetic gates created that resemble VPR after it has been enriched. 
 *  
 * @author Tramy Nguyen
 */
public class SyntheticGateExamples {

	public static SBOLDocument createORGate() throws SBOLValidationException, SBOLException {
		WrappedSBOL wrapper = new WrappedSBOL();
		ModuleDefinition ORGate = wrapper.createModuleDefinition("OR_gate");
		ComponentDefinition exp_pro = wrapper.createPromoter();
		ComponentDefinition exp_cds = wrapper.createCDS();
		FunctionalComponent exp_pro_fc = ORGate.createFunctionalComponent("expected_Pro", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_cds_fc = ORGate.createFunctionalComponent("expected_CDS", AccessType.PUBLIC, exp_cds.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_y_protein = wrapper.createProtein(ORGate, "Y_protein", DirectionType.INOUT);
		FunctionalComponent exp_x0_protein = wrapper.createProtein(ORGate, "x0_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1_protein = wrapper.createProtein(ORGate, "x1_protein", DirectionType.INOUT);
		
		ComponentDefinition tu = wrapper.createEngineeredRegion();
		ComponentDefinition promoter = wrapper.createPromoter();
		ComponentDefinition rbs = wrapper.createRibosome();
		ComponentDefinition cds = wrapper.createCDS();
		ComponentDefinition terminator = wrapper.createTerminator();
		Component gen_pro  = tu.createComponent("Pro", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_rbs  = tu.createComponent("rbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("cds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("Ter", AccessType.PUBLIC, terminator.getIdentity());
		
		tu.createSequenceConstraint("SequenceConstraint1", RestrictionType.PRECEDES, gen_pro.getIdentity(), gen_rbs.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint2", RestrictionType.PRECEDES, gen_rbs.getIdentity(), gen_cds.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint3", RestrictionType.PRECEDES, gen_cds.getIdentity(), gen_ter.getIdentity());
		
		FunctionalComponent tu_fc = ORGate.createFunctionalComponent("orTU_fc", AccessType.PUBLIC, tu.getIdentity(), DirectionType.INOUT);		
		tu_fc.createMapsTo("expectedPro_mapsTo", RefinementType.USELOCAL, exp_pro_fc.getIdentity(), gen_pro.getIdentity());
		tu_fc.createMapsTo("expectedCDS_mapsTo", RefinementType.USELOCAL, exp_cds_fc.getIdentity(), gen_cds.getIdentity());
		
		
		ModuleDefinition y_protein_prod = wrapper.createModuleDefinition("Y_protein_production");
		FunctionalComponent gen_y_protein = wrapper.createProtein(y_protein_prod, "Y_protein", DirectionType.INOUT);
		FunctionalComponent gen_cds_fc = y_protein_prod.createFunctionalComponent("gen_cds_fc", AccessType.PUBLIC, cds.getIdentity(), DirectionType.INOUT);		
		wrapper.createProductionInteraction(y_protein_prod, gen_cds_fc, gen_y_protein);
		Module y_prod_instance = ORGate.createModule("orGate_y_protein_production", y_protein_prod.getIdentity());
		y_prod_instance.createMapsTo("expected_y_protein_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y_protein.getIdentity());
		y_prod_instance.createMapsTo("expected_cds_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_cds_fc.getIdentity(), gen_cds_fc.getIdentity());
		
		ModuleDefinition x0_protein_act = wrapper.createModuleDefinition("X0_protein_activation");
		FunctionalComponent gen_x0_protein = wrapper.createProtein(x0_protein_act, "X0_protein", DirectionType.INOUT);
		FunctionalComponent gen_pro_fc = x0_protein_act.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createActivationInteraction(x0_protein_act, gen_x0_protein, gen_pro_fc);
		Module x0_act_instance = ORGate.createModule("orGate_X0_protein_activation", x0_protein_act.getIdentity());
		x0_act_instance.createMapsTo("expected_X0_protein_stim_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x0_protein.getIdentity(), gen_x0_protein.getIdentity());
		x0_act_instance.createMapsTo("expected_pro_stim1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_pro_fc.getIdentity());
		
		
		ModuleDefinition x1_protein_act = wrapper.createModuleDefinition("X1_protein_activation");
		FunctionalComponent gen_x1_protein = wrapper.createProtein(x1_protein_act, "X1_protein", DirectionType.INOUT);
		FunctionalComponent gen_x1pro_fc = x1_protein_act.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createActivationInteraction(x1_protein_act, gen_x1_protein, gen_x1pro_fc);
		Module x1_act_instance = ORGate.createModule("orGate_X1_protein_activation", x1_protein_act.getIdentity());
		x1_act_instance.createMapsTo("expected_X1_protein_stim_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1_protein.getIdentity(), gen_x1_protein.getIdentity());
		x1_act_instance.createMapsTo("expected_pro_stim2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_x1pro_fc.getIdentity());
		
		return wrapper.getSBOLDocument();
	}
	
	
	public static SBOLDocument createNORGate1() throws SBOLValidationException, SBOLException {
		WrappedSBOL wrapper = new WrappedSBOL();
		ModuleDefinition NORGate = wrapper.createModuleDefinition("NOR_gate");
		ComponentDefinition exp_pro = wrapper.createPromoter();
		ComponentDefinition exp_cds = wrapper.createCDS();
		FunctionalComponent exp_pro_fc = NORGate.createFunctionalComponent("expected_Pro", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_cds_fc = NORGate.createFunctionalComponent("expected_CDS", AccessType.PUBLIC, exp_cds.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_y_protein = wrapper.createProtein(NORGate, "Y_protein", DirectionType.INOUT);
		FunctionalComponent exp_x0_protein = wrapper.createProtein(NORGate, "x0_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1_protein = wrapper.createProtein(NORGate, "x1_protein", DirectionType.INOUT);
		
		ComponentDefinition tu = wrapper.createEngineeredRegion();
		ComponentDefinition promoter = wrapper.createPromoter();
		ComponentDefinition rbs = wrapper.createRibosome();
		ComponentDefinition cds = wrapper.createCDS();
		ComponentDefinition terminator = wrapper.createTerminator();
		Component gen_pro  = tu.createComponent("Pro", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_rbs  = tu.createComponent("rbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("cds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("Ter", AccessType.PUBLIC, terminator.getIdentity());
		
		tu.createSequenceConstraint("SequenceConstraint1", RestrictionType.PRECEDES, gen_pro.getIdentity(), gen_rbs.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint2", RestrictionType.PRECEDES, gen_rbs.getIdentity(), gen_cds.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint3", RestrictionType.PRECEDES, gen_cds.getIdentity(), gen_ter.getIdentity());
		
		FunctionalComponent tu_fc = NORGate.createFunctionalComponent("norTU_fc", AccessType.PUBLIC, tu.getIdentity(), DirectionType.INOUT);		
		tu_fc.createMapsTo("expectedPro_mapsTo", RefinementType.USELOCAL, exp_pro_fc.getIdentity(), gen_pro.getIdentity());
		tu_fc.createMapsTo("expectedCDS_mapsTo", RefinementType.USELOCAL, exp_cds_fc.getIdentity(), gen_cds.getIdentity());
		
		
		ModuleDefinition y_protein_prod = wrapper.createModuleDefinition("Y_protein_production");
		FunctionalComponent gen_y_protein = wrapper.createProtein(y_protein_prod, "Y_protein", DirectionType.INOUT);
		FunctionalComponent gen_cds_fc = y_protein_prod.createFunctionalComponent("gen_cds_fc", AccessType.PUBLIC, cds.getIdentity(), DirectionType.INOUT);		
		wrapper.createProductionInteraction(y_protein_prod, gen_cds_fc, gen_y_protein);
		Module y_prod_instance = NORGate.createModule("norGate_y_protein_production", y_protein_prod.getIdentity());
		y_prod_instance.createMapsTo("expected_y_protein_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y_protein.getIdentity());
		y_prod_instance.createMapsTo("expected_cds_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_cds_fc.getIdentity(), gen_cds_fc.getIdentity());
		
		ModuleDefinition x0_protein_inhib = wrapper.createModuleDefinition("X0_protein_inhibition");
		FunctionalComponent gen_x0_protein = wrapper.createProtein(x0_protein_inhib, "X0_protein", DirectionType.INOUT);
		FunctionalComponent gen_pro_fc = x0_protein_inhib.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createInhibitionInteraction(x0_protein_inhib, gen_x0_protein, gen_pro_fc);
		Module x0_act_instance = NORGate.createModule("norGate_X0_protein_inhibition", x0_protein_inhib.getIdentity());
		x0_act_instance.createMapsTo("expected_X0_protein_inhib1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x0_protein.getIdentity(), gen_x0_protein.getIdentity());
		x0_act_instance.createMapsTo("expected_pro_inhib2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_pro_fc.getIdentity());
		
		
		ModuleDefinition x1_protein_inhib = wrapper.createModuleDefinition("X1_protein_inhibition");
		FunctionalComponent gen_x1_protein = wrapper.createProtein(x1_protein_inhib, "X1_protein", DirectionType.INOUT);
		FunctionalComponent gen_x1pro_fc = x1_protein_inhib.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createInhibitionInteraction(x1_protein_inhib, gen_x1_protein, gen_x1pro_fc);
		Module x1_act_instance = NORGate.createModule("norGate_X1_protein_inhibition", x1_protein_inhib.getIdentity());
		x1_act_instance.createMapsTo("expected_X1_protein_inhib1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1_protein.getIdentity(), gen_x1_protein.getIdentity());
		x1_act_instance.createMapsTo("expected_pro_inhib2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_x1pro_fc.getIdentity());
		return wrapper.getSBOLDocument();
	}
	
	public static SBOLDocument createNORGate2() throws SBOLValidationException, SBOLException {
		WrappedSBOL wrapper = new WrappedSBOL();
		ModuleDefinition NORGate = wrapper.createModuleDefinition("NOR_gate");
		ComponentDefinition exp_pro = wrapper.createPromoter();
		ComponentDefinition exp_cds = wrapper.createCDS();
		FunctionalComponent exp_pro_fc = NORGate.createFunctionalComponent("expected_Pro", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_cds_fc = NORGate.createFunctionalComponent("expected_CDS", AccessType.PUBLIC, exp_cds.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_y_protein = wrapper.createProtein(NORGate, "Y_protein", DirectionType.INOUT);
		FunctionalComponent exp_x0_protein = wrapper.createProtein(NORGate, "x0_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1_protein = wrapper.createProtein(NORGate, "x1_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1y_complex = wrapper.createMolecule(NORGate, "x1y_complex", ComponentDefinition.COMPLEX, DirectionType.INOUT);
		
		ComponentDefinition tu = wrapper.createEngineeredRegion();
		ComponentDefinition promoter = wrapper.createPromoter();
		ComponentDefinition rbs = wrapper.createRibosome();
		ComponentDefinition cds = wrapper.createCDS();
		ComponentDefinition terminator = wrapper.createTerminator();
		Component gen_pro  = tu.createComponent("Pro", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_rbs  = tu.createComponent("rbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("cds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("Ter", AccessType.PUBLIC, terminator.getIdentity());
		
		tu.createSequenceConstraint("SequenceConstraint1", RestrictionType.PRECEDES, gen_pro.getIdentity(), gen_rbs.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint2", RestrictionType.PRECEDES, gen_rbs.getIdentity(), gen_cds.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint3", RestrictionType.PRECEDES, gen_cds.getIdentity(), gen_ter.getIdentity());
		
		
		FunctionalComponent tu_fc = NORGate.createFunctionalComponent("norTU_fc", AccessType.PUBLIC, tu.getIdentity(), DirectionType.INOUT);		
		tu_fc.createMapsTo("expectedPro_mapsTo", RefinementType.USELOCAL, exp_pro_fc.getIdentity(), gen_pro.getIdentity());
		tu_fc.createMapsTo("expectedCDS_mapsTo", RefinementType.USELOCAL, exp_cds_fc.getIdentity(), gen_cds.getIdentity());
		
		
		ModuleDefinition y_protein_prod = wrapper.createModuleDefinition("Y_protein_production");
		FunctionalComponent gen_y_protein = wrapper.createProtein(y_protein_prod, "Y_protein", DirectionType.INOUT);
		FunctionalComponent gen_cds_fc = y_protein_prod.createFunctionalComponent("gen_cds_fc", AccessType.PUBLIC, cds.getIdentity(), DirectionType.INOUT);		
		wrapper.createProductionInteraction(y_protein_prod, gen_cds_fc, gen_y_protein);
		Module y_prod_instance = NORGate.createModule("norGate_y_protein_production", y_protein_prod.getIdentity());
		y_prod_instance.createMapsTo("expected_y_protein_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y_protein.getIdentity());
		y_prod_instance.createMapsTo("expected_cds_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_cds_fc.getIdentity(), gen_cds_fc.getIdentity());
		
		ModuleDefinition x0_protein_inhib = wrapper.createModuleDefinition("X0_protein_inhibition");
		FunctionalComponent gen_x0_protein = wrapper.createProtein(x0_protein_inhib, "X0_protein", DirectionType.INOUT);
		FunctionalComponent gen_pro_fc = x0_protein_inhib.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createInhibitionInteraction(x0_protein_inhib, gen_x0_protein, gen_pro_fc);
		Module x0_act_instance = NORGate.createModule("norGate_X0_protein_inhibition", x0_protein_inhib.getIdentity());
		x0_act_instance.createMapsTo("expected_X0_protein_inhib1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x0_protein.getIdentity(), gen_x0_protein.getIdentity());
		x0_act_instance.createMapsTo("expected_pro_inhib2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_pro_fc.getIdentity());
		
		
		ModuleDefinition x1_protein_stim = wrapper.createModuleDefinition("X1_protein_complex");
		FunctionalComponent gen_x1_protein = wrapper.createProtein(x1_protein_stim, "X1_protein", DirectionType.INOUT);
		FunctionalComponent gen_y2_protein = wrapper.addFunctionalComponent(x1_protein_stim, "Y_protein", AccessType.PUBLIC, gen_y_protein.getDefinition().getIdentity(), DirectionType.INOUT);
		FunctionalComponent gen_x1complex_fc = wrapper.createMolecule(x1_protein_stim, "gen_comp_fc", ComponentDefinition.COMPLEX, DirectionType.INOUT);
		wrapper.createComplexFormationInteraction(x1_protein_stim, gen_x1_protein, gen_y2_protein, gen_x1complex_fc);
		Module x1_act_instance = NORGate.createModule("norGate_X1_protein_complex1", x1_protein_stim.getIdentity());
		x1_act_instance.createMapsTo("expected_X1_protein_comp_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1_protein.getIdentity(), gen_x1_protein.getIdentity());
		x1_act_instance.createMapsTo("expected_y_protein_comp_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y2_protein.getIdentity());
		x1_act_instance.createMapsTo("expected_comp_x1y_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1y_complex.getIdentity(), gen_x1complex_fc.getIdentity());
		
		return wrapper.getSBOLDocument();
	}
	
	public static SBOLDocument createNORGate3() throws SBOLValidationException, SBOLException {
		WrappedSBOL wrapper = new WrappedSBOL();
		ModuleDefinition NORGate = wrapper.createModuleDefinition("NOR_gate");
		ComponentDefinition exp_pro = wrapper.createPromoter();
		ComponentDefinition exp_cds = wrapper.createCDS();
		FunctionalComponent exp_pro_fc = NORGate.createFunctionalComponent("expected_Pro", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_cds_fc = NORGate.createFunctionalComponent("expected_CDS", AccessType.PUBLIC, exp_cds.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_y_protein = wrapper.createProtein(NORGate, "Y_protein", DirectionType.INOUT);
		FunctionalComponent exp_x0_protein = wrapper.createProtein(NORGate, "x0_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1_protein = wrapper.createMolecule(NORGate, "x1_smallMolecule", ComponentDefinition.SMALL_MOLECULE, DirectionType.INOUT);
		FunctionalComponent exp_x1y_complex = wrapper.createMolecule(NORGate, "x1y_complex", ComponentDefinition.COMPLEX, DirectionType.INOUT);
		
		ComponentDefinition tu = wrapper.createEngineeredRegion();
		ComponentDefinition promoter = wrapper.createPromoter();
		ComponentDefinition rbs = wrapper.createRibosome();
		ComponentDefinition cds = wrapper.createCDS();
		ComponentDefinition terminator = wrapper.createTerminator();
		Component gen_pro  = tu.createComponent("Pro", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_rbs  = tu.createComponent("rbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("cds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("Ter", AccessType.PUBLIC, terminator.getIdentity());
		
		tu.createSequenceConstraint("SequenceConstraint1", RestrictionType.PRECEDES, gen_pro.getIdentity(), gen_rbs.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint2", RestrictionType.PRECEDES, gen_rbs.getIdentity(), gen_cds.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint3", RestrictionType.PRECEDES, gen_cds.getIdentity(), gen_ter.getIdentity());
		
		
		FunctionalComponent tu_fc = NORGate.createFunctionalComponent("norTU_fc", AccessType.PUBLIC, tu.getIdentity(), DirectionType.INOUT);		
		tu_fc.createMapsTo("expectedPro_mapsTo", RefinementType.USELOCAL, exp_pro_fc.getIdentity(), gen_pro.getIdentity());
		tu_fc.createMapsTo("expectedCDS_mapsTo", RefinementType.USELOCAL, exp_cds_fc.getIdentity(), gen_cds.getIdentity());
		
		
		ModuleDefinition y_protein_prod = wrapper.createModuleDefinition("Y_protein_production");
		FunctionalComponent gen_y_protein = wrapper.createProtein(y_protein_prod, "Y_protein", DirectionType.INOUT);
		FunctionalComponent gen_cds_fc = y_protein_prod.createFunctionalComponent("gen_cds_fc", AccessType.PUBLIC, cds.getIdentity(), DirectionType.INOUT);		
		wrapper.createProductionInteraction(y_protein_prod, gen_cds_fc, gen_y_protein);
		Module y_prod_instance = NORGate.createModule("norGate_y_protein_production", y_protein_prod.getIdentity());
		y_prod_instance.createMapsTo("expected_y_protein_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y_protein.getIdentity());
		y_prod_instance.createMapsTo("expected_cds_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_cds_fc.getIdentity(), gen_cds_fc.getIdentity());
		
		ModuleDefinition x0_protein_inhib = wrapper.createModuleDefinition("X0_protein_inhibition");
		FunctionalComponent gen_x0_protein = wrapper.createProtein(x0_protein_inhib, "X0_protein", DirectionType.INOUT);
		FunctionalComponent gen_pro_fc = x0_protein_inhib.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createInhibitionInteraction(x0_protein_inhib, gen_x0_protein, gen_pro_fc);
		Module x0_act_instance = NORGate.createModule("norGate_X0_protein_inhibition", x0_protein_inhib.getIdentity());
		x0_act_instance.createMapsTo("expected_X0_protein_inhib1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x0_protein.getIdentity(), gen_x0_protein.getIdentity());
		x0_act_instance.createMapsTo("expected_pro_inhib2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_pro_fc.getIdentity());
		
		
		ModuleDefinition x1_protein_stim = wrapper.createModuleDefinition("X1_protein_complex");
		FunctionalComponent gen_x1_protein = wrapper.createProtein(x1_protein_stim, "X1_protein", DirectionType.INOUT);
		FunctionalComponent gen_y2_protein = wrapper.addFunctionalComponent(x1_protein_stim, "Y_protein", AccessType.PUBLIC, gen_y_protein.getDefinition().getIdentity(), DirectionType.INOUT);
		FunctionalComponent gen_x1complex_fc = wrapper.createMolecule(x1_protein_stim, "gen_comp_fc", ComponentDefinition.COMPLEX, DirectionType.INOUT);
		wrapper.createComplexFormationInteraction(x1_protein_stim, gen_x1_protein, gen_y2_protein, gen_x1complex_fc);
		Module x1_act_instance = NORGate.createModule("norGate_X1_protein_complex1", x1_protein_stim.getIdentity());
		x1_act_instance.createMapsTo("expected_X1_protein_comp_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1_protein.getIdentity(), gen_x1_protein.getIdentity());
		x1_act_instance.createMapsTo("expected_y_protein_comp_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y2_protein.getIdentity());
		x1_act_instance.createMapsTo("expected_comp_x1y_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1y_complex.getIdentity(), gen_x1complex_fc.getIdentity());
		
		return wrapper.getSBOLDocument();
	}
	
	public static SBOLDocument createANDGate() throws SBOLValidationException, SBOLException {
		WrappedSBOL wrapper = new WrappedSBOL();
		ModuleDefinition ANDGate = wrapper.createModuleDefinition("AND_gate");
		ComponentDefinition exp_pro = wrapper.createPromoter();
		ComponentDefinition exp_cds = wrapper.createCDS();
		FunctionalComponent exp_pro_fc = ANDGate.createFunctionalComponent("expected_Pro", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_cds_fc = ANDGate.createFunctionalComponent("expected_CDS", AccessType.PUBLIC, exp_cds.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_y_protein = wrapper.createProtein(ANDGate, "Y_protein", DirectionType.INOUT);
		FunctionalComponent exp_x0_protein = wrapper.createProtein(ANDGate, "x0_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1_protein = wrapper.createProtein(ANDGate, "x1_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1x0_complex = wrapper.createMolecule(ANDGate, "x1x0_complex", ComponentDefinition.COMPLEX, DirectionType.INOUT);
		
		ComponentDefinition tu = wrapper.createEngineeredRegion();
		ComponentDefinition promoter = wrapper.createPromoter();
		ComponentDefinition rbs = wrapper.createRibosome();
		ComponentDefinition cds = wrapper.createCDS();
		ComponentDefinition terminator = wrapper.createTerminator();
		Component gen_pro  = tu.createComponent("Pro", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_rbs  = tu.createComponent("rbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("cds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("Ter", AccessType.PUBLIC, terminator.getIdentity());
		
		tu.createSequenceConstraint("SequenceConstraint1", RestrictionType.PRECEDES, gen_pro.getIdentity(), gen_rbs.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint2", RestrictionType.PRECEDES, gen_rbs.getIdentity(), gen_cds.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint3", RestrictionType.PRECEDES, gen_cds.getIdentity(), gen_ter.getIdentity());
		
		
		FunctionalComponent tu_fc = ANDGate.createFunctionalComponent("andTU_fc", AccessType.PUBLIC, tu.getIdentity(), DirectionType.INOUT);		
		tu_fc.createMapsTo("expectedPro_mapsTo", RefinementType.USELOCAL, exp_pro_fc.getIdentity(), gen_pro.getIdentity());
		tu_fc.createMapsTo("expectedCDS_mapsTo", RefinementType.USELOCAL, exp_cds_fc.getIdentity(), gen_cds.getIdentity());
		
		
		ModuleDefinition y_protein_prod = wrapper.createModuleDefinition("Y_protein_production");
		FunctionalComponent gen_y_protein = wrapper.createProtein(y_protein_prod, "Y_protein", DirectionType.INOUT);
		FunctionalComponent gen_cds_fc = y_protein_prod.createFunctionalComponent("gen_cds_fc", AccessType.PUBLIC, cds.getIdentity(), DirectionType.INOUT);		
		wrapper.createProductionInteraction(y_protein_prod, gen_cds_fc, gen_y_protein);
		Module y_prod_instance = ANDGate.createModule("andGate_y_protein_production", y_protein_prod.getIdentity());
		y_prod_instance.createMapsTo("expected_y_protein_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y_protein.getIdentity());
		y_prod_instance.createMapsTo("expected_cds_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_cds_fc.getIdentity(), gen_cds_fc.getIdentity());
		
		ModuleDefinition x0x1_stimInterac = wrapper.createModuleDefinition("complex_stim");
		FunctionalComponent gen_x1x0complex_fc = wrapper.createMolecule(x0x1_stimInterac, "gen_comp_fc", ComponentDefinition.COMPLEX, DirectionType.INOUT);
		FunctionalComponent gen_pro_fc = x0x1_stimInterac.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createActivationInteraction(x0x1_stimInterac, gen_x1x0complex_fc, gen_pro_fc);
		Module x0_act_instance = ANDGate.createModule("andGate_complex_stimulation", x0x1_stimInterac.getIdentity());
		x0_act_instance.createMapsTo("expected_complex_stim_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1x0_complex.getIdentity(), gen_x1x0complex_fc.getIdentity());
		x0_act_instance.createMapsTo("expected_pro_stim_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_pro_fc.getIdentity());
		
		
		ModuleDefinition complexInteraction = wrapper.createModuleDefinition("X1X0_protein_complex");
		FunctionalComponent gen_x1_protein = wrapper.createProtein(complexInteraction, "X1_protein", DirectionType.INOUT);
		FunctionalComponent gen_x0_protein = wrapper.createProtein(complexInteraction, "X0_protein", DirectionType.INOUT);
		FunctionalComponent gen_comp2 = complexInteraction.createFunctionalComponent("gen_comp2_fc", AccessType.PUBLIC, gen_x1x0complex_fc.getIdentity(), DirectionType.INOUT);		
		wrapper.createComplexFormationInteraction(complexInteraction, gen_x1_protein, gen_x0_protein, gen_comp2);
		Module x1x0_comp_instance = ANDGate.createModule("andGate_complex", complexInteraction.getIdentity());
		x1x0_comp_instance.createMapsTo("expected_X1_protein_comp_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1_protein.getIdentity(), gen_x1_protein.getIdentity());
		x1x0_comp_instance.createMapsTo("expected_x0_protein_comp_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x0_protein.getIdentity(), gen_x0_protein.getIdentity());
		x1x0_comp_instance.createMapsTo("expected_comp_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1x0_complex.getIdentity(), gen_comp2.getIdentity());
		
		return wrapper.getSBOLDocument();
	}
	
	public static SBOLDocument createNANDGate() throws SBOLValidationException, SBOLException {
		WrappedSBOL wrapper = new WrappedSBOL();
		ModuleDefinition NANDGate = wrapper.createModuleDefinition("NAND_gate");
		ComponentDefinition exp_pro = wrapper.createPromoter();
		ComponentDefinition exp_cds = wrapper.createCDS();
		FunctionalComponent exp_pro_fc = NANDGate.createFunctionalComponent("expected_Pro", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_pro2_fc = NANDGate.createFunctionalComponent("expected_Pro2", AccessType.PUBLIC, exp_pro.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_cds_fc = NANDGate.createFunctionalComponent("expected_CDS", AccessType.PUBLIC, exp_cds.getIdentity(), DirectionType.INOUT);
		FunctionalComponent exp_y_protein = wrapper.createProtein(NANDGate, "Y_protein", DirectionType.INOUT);
		FunctionalComponent exp_x0_protein = wrapper.createProtein(NANDGate, "x0_protein", DirectionType.INOUT);
		FunctionalComponent exp_x1_protein = wrapper.createProtein(NANDGate, "x1_protein", DirectionType.INOUT);
		
		ComponentDefinition tu = wrapper.createEngineeredRegion();
		ComponentDefinition promoter = wrapper.createPromoter();
		ComponentDefinition promoter2 = wrapper.createPromoter();
		ComponentDefinition rbs = wrapper.createRibosome();
		ComponentDefinition cds = wrapper.createCDS();
		ComponentDefinition terminator = wrapper.createTerminator();
		Component gen_pro  = tu.createComponent("Pro1", AccessType.PUBLIC, promoter.getIdentity());
		Component gen_pro2  = tu.createComponent("Pro2", AccessType.PUBLIC, promoter2.getIdentity());
		Component gen_rbs  = tu.createComponent("rbs", AccessType.PUBLIC, rbs.getIdentity());
		Component gen_cds  = tu.createComponent("cds", AccessType.PUBLIC, cds.getIdentity());
		Component gen_ter  = tu.createComponent("Ter", AccessType.PUBLIC, terminator.getIdentity());
		
		tu.createSequenceConstraint("SequenceConstraint1", RestrictionType.PRECEDES, gen_pro.getIdentity(), gen_pro2.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint2", RestrictionType.PRECEDES, gen_pro2.getIdentity(), gen_rbs.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint3", RestrictionType.PRECEDES, gen_rbs.getIdentity(), gen_cds.getIdentity());
		tu.createSequenceConstraint("SequenceConstraint4", RestrictionType.PRECEDES, gen_cds.getIdentity(), gen_ter.getIdentity());
		
		FunctionalComponent tu_fc = NANDGate.createFunctionalComponent("nandTU_fc", AccessType.PUBLIC, tu.getIdentity(), DirectionType.INOUT);		
		tu_fc.createMapsTo("expectedPro_mapsTo", RefinementType.USELOCAL, exp_pro_fc.getIdentity(), gen_pro.getIdentity());
		tu_fc.createMapsTo("expectedPro2_mapsTo", RefinementType.USELOCAL, exp_pro2_fc.getIdentity(), gen_pro2.getIdentity());
		tu_fc.createMapsTo("expectedCDS_mapsTo", RefinementType.USELOCAL, exp_cds_fc.getIdentity(), gen_cds.getIdentity());
		
		
		ModuleDefinition y_protein_prod = wrapper.createModuleDefinition("Y_protein_production");
		FunctionalComponent gen_y_protein = wrapper.createProtein(y_protein_prod, "Y_protein", DirectionType.INOUT);
		FunctionalComponent gen_cds_fc = y_protein_prod.createFunctionalComponent("gen_cds_fc", AccessType.PUBLIC, cds.getIdentity(), DirectionType.INOUT);		
		wrapper.createProductionInteraction(y_protein_prod, gen_cds_fc, gen_y_protein);
		Module y_prod_instance = NANDGate.createModule("nandGate_y_protein_production", y_protein_prod.getIdentity());
		y_prod_instance.createMapsTo("expected_y_protein_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_y_protein.getIdentity(), gen_y_protein.getIdentity());
		y_prod_instance.createMapsTo("expected_cds_prod_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_cds_fc.getIdentity(), gen_cds_fc.getIdentity());
		
		ModuleDefinition x0_protein_inhib = wrapper.createModuleDefinition("X0_protein_inhibition");
		FunctionalComponent gen_x0_protein = wrapper.createProtein(x0_protein_inhib, "X0_protein", DirectionType.INOUT);
		FunctionalComponent gen_pro_fc = x0_protein_inhib.createFunctionalComponent("gen_pro_fc", AccessType.PUBLIC, promoter.getIdentity(), DirectionType.INOUT);		
		wrapper.createInhibitionInteraction(x0_protein_inhib, gen_x0_protein, gen_pro_fc);
		Module x0_act_instance = NANDGate.createModule("nandGate_X0_protein_inhibition", x0_protein_inhib.getIdentity());
		x0_act_instance.createMapsTo("expected_X0_protein_inhib1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x0_protein.getIdentity(), gen_x0_protein.getIdentity());
		x0_act_instance.createMapsTo("expected_pro_inhib2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro_fc.getIdentity(), gen_pro_fc.getIdentity());
		
		
		ModuleDefinition x1_protein_inhib = wrapper.createModuleDefinition("X1_protein_inhibition");
		FunctionalComponent gen_x1_protein = wrapper.createProtein(x1_protein_inhib, "X1_protein", DirectionType.INOUT);
		FunctionalComponent gen_x1pro_fc = x1_protein_inhib.createFunctionalComponent("gen_pro2_fc", AccessType.PUBLIC, promoter2.getIdentity(), DirectionType.INOUT);		
		wrapper.createInhibitionInteraction(x1_protein_inhib, gen_x1_protein, gen_x1pro_fc);
		Module x1_act_instance = NANDGate.createModule("nandGate_X1_protein_inhibition", x1_protein_inhib.getIdentity());
		x1_act_instance.createMapsTo("expected_X1_protein_inhib1_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_x1_protein.getIdentity(), gen_x1_protein.getIdentity());
		x1_act_instance.createMapsTo("expected_pro_inhib2_interaction_mapsTo", RefinementType.VERIFYIDENTICAL, exp_pro2_fc.getIdentity(), gen_x1pro_fc.getIdentity());
		return wrapper.getSBOLDocument();
	}
	
	
}

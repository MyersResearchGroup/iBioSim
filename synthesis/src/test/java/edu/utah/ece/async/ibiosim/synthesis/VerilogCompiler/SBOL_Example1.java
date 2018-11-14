package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;


/**
 * Test NOT gate design that was compiled from a verilog expression to an SBOL data model
 * @author Tramy Nguyen
 *
 */
public class SBOL_Example1 extends AbstractVerilogParserTest{

	private static SBOLDocument sbolDoc;
	private static ModuleDefinition sbolDesign;

	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-v", reader.getFile("contAssign3.v"), "-sbol"};
		
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		String vName = "contAssign";
		WrappedSBOL sbolWrapper = compiledVerilog.getSBOLWrapper(vName);
		Assert.assertNotNull(sbolWrapper);
		sbolDoc = sbolWrapper.getSBOLDocument();
		Assert.assertEquals(1, sbolDoc.getModuleDefinitions().size());
		sbolDesign = sbolDoc.getModuleDefinition(vName, "1.0");
	}
	
	@Test
	public void Test_cdSize() {
		Assert.assertEquals(3, sbolDoc.getComponentDefinitions().size());
	}
	
	@Test
	public void Test_fcSize() {
		Assert.assertEquals(3, sbolDesign.getFunctionalComponents().size());
	}
	
	@Test
	public void Test_CD1() {
		ComponentDefinition cd = sbolDoc.getComponentDefinition("CD0_a", "1.0");
		Assert.assertNotNull(cd);
		Assert.assertEquals(1, cd.getTypes().size());
		Assert.assertEquals(ComponentDefinition.PROTEIN, cd.getTypes().iterator().next());
	}

	@Test
	public void Test_CD2() {
		ComponentDefinition cd = sbolDoc.getComponentDefinition("CD1_y", "1.0");
		Assert.assertNotNull(cd);
		Assert.assertEquals(1, cd.getTypes().size());
		Assert.assertEquals(ComponentDefinition.PROTEIN, cd.getTypes().iterator().next());
	}
	
	@Test
	public void Test_CD3() {
		ComponentDefinition cd = sbolDoc.getComponentDefinition("CD2_notGate", "1.0");
		Assert.assertNotNull(cd);
		Assert.assertEquals(1, cd.getTypes().size());
		Assert.assertEquals(ComponentDefinition.DNA, cd.getTypes().iterator().next());
		Assert.assertEquals(1, cd.getRoles().size());
		Assert.assertEquals(SequenceOntology.ENGINEERED_REGION, cd.getRoles().iterator().next());
	}
	
	@Test
	public void Test_FC1() {
		FunctionalComponent fc = sbolDesign.getFunctionalComponent("FC0_a");
		Assert.assertNotNull(fc);
		Assert.assertEquals(AccessType.PUBLIC, fc.getAccess());
		Assert.assertEquals(DirectionType.IN, fc.getDirection());
	}
	
	@Test
	public void Test_FC2() {
		FunctionalComponent fc = sbolDesign.getFunctionalComponent("FC1_y");
		Assert.assertNotNull(fc);
		Assert.assertEquals(AccessType.PUBLIC, fc.getAccess());
		Assert.assertEquals(DirectionType.OUT, fc.getDirection());
	}
	
	@Test
	public void Test_FC3() {
		FunctionalComponent fc = sbolDesign.getFunctionalComponent("FC2_notGate");
		Assert.assertNotNull(fc);
		Assert.assertEquals(AccessType.PUBLIC, fc.getAccess());
		Assert.assertEquals(DirectionType.NONE, fc.getDirection());
	}
	
	@Test
	public void Test_InteractionSize() {
		Assert.assertEquals(2, sbolDesign.getInteractions().size());
	}
	
	@Test
	public void Test_Interaction1() {
		Interaction interaction = sbolDesign.getInteraction("I0");
		Assert.assertNotNull(interaction);
		Assert.assertEquals(1, interaction.getTypes().size());
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, interaction.getTypes().iterator().next());
		
		Participation p1 = interaction.getParticipation("P0");
		Assert.assertNotNull(p1);
		Assert.assertEquals(sbolDesign.getFunctionalComponent("FC0_a"), p1.getParticipant());
		Assert.assertEquals(1, p1.getRoles().size());
		Assert.assertEquals(SystemsBiologyOntology.INHIBITOR, p1.getRoles().iterator().next());
		
		Participation p2 = interaction.getParticipation("P1");
		Assert.assertNotNull(p2);
		Assert.assertEquals(sbolDesign.getFunctionalComponent("FC2_notGate"), p2.getParticipant());
		Assert.assertEquals(1, p2.getRoles().size());
		Assert.assertEquals(SystemsBiologyOntology.INHIBITED, p2.getRoles().iterator().next());
	}
	
	@Test
	public void Test_Interaction2() {
		Interaction interaction = sbolDesign.getInteraction("I1");
		Assert.assertNotNull(interaction);
		Assert.assertEquals(1, interaction.getTypes().size());
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, interaction.getTypes().iterator().next());
		
		Participation p1 = interaction.getParticipation("P2");
		Assert.assertNotNull(p1);
		Assert.assertEquals(sbolDesign.getFunctionalComponent("FC2_notGate"), p1.getParticipant());
		Assert.assertEquals(1, p1.getRoles().size());
		Assert.assertEquals(SystemsBiologyOntology.PROMOTER, p1.getRoles().iterator().next());
		
		Participation p2 = interaction.getParticipation("P3");
		Assert.assertNotNull(p2);
		Assert.assertEquals(sbolDesign.getFunctionalComponent("FC1_y"), p2.getParticipant());
		Assert.assertEquals(1, p2.getRoles().size());
		Assert.assertEquals(SystemsBiologyOntology.PRODUCT, p2.getRoles().iterator().next());
	}
}

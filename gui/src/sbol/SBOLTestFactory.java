package sbol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;


import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.impl.DnaComponentImpl;
import org.sbolstandard.core.impl.DnaSequenceImpl;
import org.sbolstandard.core.util.SequenceOntology;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;

public class SBOLTestFactory {

	public SBOLTestFactory() {
		
	}
	
	//Gate proportions: [inverter yes nor or nand and]
	public List<BioModel> createTestLibrary(int[] gateTotals, String[] gateTypes, String projectDirectory) {
		List<BioModel> gateLibrary = new LinkedList<BioModel>();
		int[] gateCounts = gateTotals.clone();
		int[] gateSums = new int[gateCounts.length];
		gateSums[0] = gateCounts[0];
		for (int i = 1; i < gateSums.length; i++)
			gateSums[i] = gateCounts[i] + gateSums[i - 1];
		Random rGen = new Random();
		while (gateSums[gateSums.length - 1] > 0) {
			int target = rGen.nextInt(gateSums[gateSums.length - 1]);
			int i = 0;
			while (target >= gateSums[i])
				i++;
			String gateID = gateTypes[i] + (gateTotals[i] - gateCounts[i]);
			gateLibrary.add(createTestGate(gateID, projectDirectory));
			gateCounts[i] -= 1;
			gateSums[0] = gateCounts[0];
			for (int j = 1; j < gateSums.length; j++)
				gateSums[j] = gateCounts[j] + gateSums[j - 1];
		} 
//		for (int i = 0; i < gateTypes.length; i++) 
//			for (int j = 0; j < gateCounts.length; j++) 
//				gateLibrary.add(createTestGate(gateTypes[i] + gateCounts[j], projectDirectory));
		return gateLibrary;
	}
	
	public Set<DnaComponent> annotateTestLibrary(int cdsCopyNum, List<BioModel> gateLibrary) {
		Set<DnaComponent> libraryComps = new HashSet<DnaComponent>();
		DnaComponent previousCDS = null;
		Random rGen = new Random();
		try {
			DnaComponent sbolRBS = createTestDNAComponent(
					new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + "RBS"),
					"RBS", GlobalConstants.SO_RBS, rGen, GlobalConstants.RBS_LENGTH, 0);
			libraryComps.add(sbolRBS);
			DnaComponent sbolTT = createTestDNAComponent(
					new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + "TT"),
					"TT", GlobalConstants.SO_TERMINATOR, rGen, GlobalConstants.TERMINATOR_LENGTH, 0);
			libraryComps.add(sbolTT);
			for (int i = 0; i < gateLibrary.size(); i++) {
				BioModel gateModel = gateLibrary.get(i);
				String gateID = gateModel.getSBMLDocument().getModel().getId();
				for (String promoterID : gateModel.getPromoters()) {
					Reaction sbmlPromoter = gateModel.getProductionReaction(promoterID);
					DnaComponent sbolPromoter = createTestDNAComponent(
							new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + gateID + "_" + promoterID), 
							gateID + "_" + promoterID, GlobalConstants.SO_PROMOTER, rGen, 
							GlobalConstants.MEAN_PROMOTER_LENGTH, GlobalConstants.SD_PROMOTER_LENGTH);
					libraryComps.add(sbolPromoter);
					List<URI> annotationObject = new LinkedList<URI>();
					annotationObject.add(sbolPromoter.getURI());
					AnnotationUtility.setSBOLAnnotation(sbmlPromoter, 
							new SBOLAnnotation(sbmlPromoter.getMetaId(), annotationObject));
				}
				boolean isFirstCDS = true;
				for (String speciesID : gateModel.getInputSpecies()) {
					Species sbmlSpecies = gateModel.getSBMLDocument().getModel().getSpecies(speciesID);
					DnaComponent sbolCDS;
					if (isFirstCDS) {
						if (i % cdsCopyNum > 0) { 
							sbolCDS = previousCDS;
						} else {
							sbolCDS = createTestDNAComponent(
									new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + gateID + "_" + speciesID),
									gateID + "_" + speciesID, GlobalConstants.SO_CDS, rGen, 
									GlobalConstants.MEAN_CDS_LENGTH, GlobalConstants.SD_CDS_LENGTH);
							libraryComps.add(sbolCDS);
							previousCDS = sbolCDS;
						}
						isFirstCDS = false;
					} else {
						sbolCDS = createTestDNAComponent(
								new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + gateID + "_" + speciesID),
								gateID + "_" + speciesID, GlobalConstants.SO_CDS, rGen, 
								GlobalConstants.MEAN_CDS_LENGTH, GlobalConstants.SD_CDS_LENGTH);
						libraryComps.add(sbolCDS);
					}
					List<URI> annotationObject = new LinkedList<URI>();
					annotationObject.add(sbolRBS.getURI());
					annotationObject.add(sbolCDS.getURI());
					annotationObject.add(sbolTT.getURI());
					AnnotationUtility.setSBOLAnnotation(sbmlSpecies, 
							new SBOLAnnotation(sbmlSpecies.getMetaId(), annotationObject));
				}
					
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return libraryComps;
	}
	
//	public Set<DnaComponent> annotateTestLibrary(int crossGateTotal, int avgCDSCopyNum, int sdCDSCopyNum,
//			List<BioModel> gateLibrary) {
//		Set<DnaComponent> libraryComps = new HashSet<DnaComponent>();
//		List<DnaComponent> cdsComps = new LinkedList<DnaComponent>();
//		Random rGen = new Random();
//		int cdsCopyNum = 1;
//		int cdsCopyCap = (int) Math.round(avgCDSCopyNum + sdCDSCopyNum*rGen.nextGaussian());
//		try {
//			DnaComponent sbolRBS = createTestDNAComponent(
//					new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + "RBS"),
//					"RBS", GlobalConstants.SO_RBS, rGen, 12, 0);
//			libraryComps.add(sbolRBS);
//			DnaComponent sbolTT = createTestDNAComponent(
//					new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + "TT"),
//					"TT", GlobalConstants.SO_TERMINATOR, rGen, 12, 0);
//			libraryComps.add(sbolTT);
//			for (int i = 0; i < gateLibrary.size(); i++) {
//				BioModel gateModel = gateLibrary.get(i);
//				String gateID = gateModel.getSBMLDocument().getModel().getId();
//				for (String promoterID : gateModel.getPromoters()) {
//					Reaction sbmlPromoter = gateModel.getProductionReaction(promoterID);
//					DnaComponent sbolPromoter = createTestDNAComponent(
//							new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + gateID + "_" + promoterID), 
//							gateID + "_" + promoterID, GlobalConstants.SO_PROMOTER, rGen, 62, 23);
//					libraryComps.add(sbolPromoter);
//					List<URI> annotationObject = new LinkedList<URI>();
//					annotationObject.add(sbolPromoter.getURI());
//					AnnotationUtility.setSBOLAnnotation(sbmlPromoter, 
//							new SBOLAnnotation(sbmlPromoter.getMetaId(), annotationObject));
//				}
//				boolean hasCrossTalk = cdsComps.size() > 0 && i < crossGateTotal;
//				for (String speciesID : gateModel.getInputSpecies()) {
//					Species sbmlSpecies = gateModel.getSBMLDocument().getModel().getSpecies(speciesID);
//					DnaComponent sbolCDS;
//					if (hasCrossTalk) { 
//						sbolCDS = cdsComps.get(0);
//						cdsCopyNum++;
//						if (cdsCopyNum >= cdsCopyCap) {
//							cdsComps.remove(0);
//							cdsCopyNum = 1;
//							cdsCopyCap = (int) Math.round(avgCDSCopyNum + sdCDSCopyNum*rGen.nextGaussian());
//						}
//						hasCrossTalk = false;
//					} else {
//						sbolCDS = createTestDNAComponent(
//								new URI(GlobalConstants.MYERS_LAB_AUTHORITY + "#" + gateID + "_" + speciesID),
//								gateID + "_" + speciesID, GlobalConstants.SO_CDS, rGen, 695, 268);
//						libraryComps.add(sbolCDS);
//						cdsComps.add(0, sbolCDS);
//					}
//					List<URI> annotationObject = new LinkedList<URI>();
//					annotationObject.add(sbolRBS.getURI());
//					annotationObject.add(sbolCDS.getURI());
//					annotationObject.add(sbolTT.getURI());
//					AnnotationUtility.setSBOLAnnotation(sbmlSpecies, 
//							new SBOLAnnotation(sbmlSpecies.getMetaId(), annotationObject));
//				}
//					
//			}
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return libraryComps;
//	}
	
	private BioModel createTestGate(String gateID, String projectDirectory) {
		BioModel gateModel = new BioModel(projectDirectory);
		gateModel.createSBMLDocument(gateID, false, false);
		
		String promoterID;
		if (gateID.startsWith("N"))
			promoterID = "Pr";
		else
			promoterID = "Pa";
		gateModel.createPromoter(promoterID, 20, 20, true);
		
		String inputID = "Si";
		gateModel.createSpecies(inputID, 10, 10);
		gateModel.createDirPort(inputID, GlobalConstants.INPUT);
		
		String outputID = "So";
		gateModel.createSpecies(outputID, 30, 30);
		gateModel.createDirPort(outputID, GlobalConstants.OUTPUT);
		
		if (gateID.startsWith("INV")) 
			createInverter(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("YES")) 
			createYesGate(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("NOR")) 
			createNorGate(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("OR")) 
			createOrGate(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("NAND")) 
			createNandGate(gateModel, promoterID, inputID, outputID);
		else if (gateID.startsWith("AND")) 
			createAndGate(gateModel, promoterID, inputID, outputID);
		return gateModel;
	}
	
	private DnaComponent createTestDNAComponent(URI uri, String displayID, String soType, Random rGen,
			int avgSize, int sdSize) {
		DnaComponent dnaComp = new DnaComponentImpl();
		dnaComp.setURI(uri);
		dnaComp.setDisplayId(displayID);
		dnaComp.addType(SequenceOntology.type(soType));
		DnaSequence dnaSeq = new DnaSequenceImpl();
		try {
			dnaSeq.setURI(new URI(uri.toString() + "_seq"));
		int size;
		do {
			size = (int) Math.round(avgSize + sdSize*rGen.nextGaussian());
		} while (size <= 0);
		dnaSeq.setNucleotides(createTestNucleotides(size));
		dnaComp.setDnaSequence(dnaSeq);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dnaComp;
		
	}
	
	private String createTestNucleotides(int size) {
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0 ; i < size; i++) {
			sb.append('a');
		}
		return sb.toString();
	}
	
	private void createInverter(BioModel gateModel, String promoterID, String inputID, String outputID) {
		gateModel.addRepressorToProductionReaction(promoterID, inputID, outputID, null, null, null);
	}

	private void createYesGate(BioModel bioGate, String promoterID, String inputID, String outputID) {
		bioGate.addActivatorToProductionReaction(promoterID, inputID, outputID, null, null, null);
	}

	private void createNorGate(BioModel gateModel, String promoterID, String inputID, String outputID) {
		String inputID2 = inputID + "2";
		gateModel.createSpecies(inputID2, 20, 10);
		gateModel.createDirPort(inputID2, GlobalConstants.INPUT);
		gateModel.addRepressorToProductionReaction(promoterID, inputID, outputID, null, null, null);
		gateModel.addRepressorToProductionReaction(promoterID, inputID2, "none", null, null, null);
	}

	private void createOrGate(BioModel gateModel, String promoterID, String inputID, String outputID) {
		String inputID2 = inputID + "2";
		gateModel.createSpecies(inputID2, 20, 10);
		gateModel.createDirPort(inputID2, GlobalConstants.INPUT);
		gateModel.addActivatorToProductionReaction(promoterID, inputID, outputID, null, null, null);
		gateModel.addActivatorToProductionReaction(promoterID, inputID2, "none", null, null, null);
	}

	private void createNandGate(BioModel gateModel, String promoterID, String inputID, String outputID) {
		String inputID2 = inputID + "2";
		gateModel.createSpecies(inputID2, 20, 10);
		gateModel.createDirPort(inputID2, GlobalConstants.INPUT);
		String complexID = inputID + inputID2;
		gateModel.createSpecies(complexID, 15, 15);
		gateModel.createComplexReaction(complexID, null, false);
		gateModel.addReactantToComplexReaction(inputID, complexID, null, "1");
		gateModel.addReactantToComplexReaction(inputID2, complexID, null, "1");
		gateModel.addRepressorToProductionReaction(promoterID, complexID, outputID, null, null, null);
	}

	private void createAndGate(BioModel gateModel, String promoterID, String inputID, String outputID) {
		String inputID2 = inputID + "2";
		gateModel.createSpecies(inputID2, 20, 10);
		gateModel.createDirPort(inputID2, GlobalConstants.INPUT);
		String complexID = inputID + inputID2;
		gateModel.createSpecies(complexID, 15, 15);
		gateModel.createComplexReaction(complexID, null, false);
		gateModel.addReactantToComplexReaction(inputID, complexID, null, "1");
		gateModel.addReactantToComplexReaction(inputID2, complexID, null, "1");
		gateModel.addActivatorToProductionReaction(promoterID, complexID, outputID, null, null, null);
	}

}

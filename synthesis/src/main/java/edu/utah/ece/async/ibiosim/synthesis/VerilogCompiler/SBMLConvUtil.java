package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

public class SBMLConvUtil {
	public static void main(String[] args) {
		BioModel biomodel = new BioModel(args[0]);
		try {
			biomodel.load(args[1]);
			for(String species : biomodel.getSpecies()){
				biomodel.createDegradationReaction(species, 0.75, null, false, null);
			}
			biomodel.save(args[2]);
		} catch (XMLStreamException | IOException | BioSimException e) {
			e.printStackTrace();
		}
		
	}
}

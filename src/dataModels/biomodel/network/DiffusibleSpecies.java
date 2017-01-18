package dataModels.biomodel.network;

import dataModels.biomodel.visitor.SpeciesVisitor;

public class DiffusibleSpecies extends AbstractSpecies{
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitDiffusibleSpecies(this);
		
	}
}

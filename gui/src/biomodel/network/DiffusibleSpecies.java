package biomodel.network;

import biomodel.visitor.SpeciesVisitor;

public class DiffusibleSpecies extends AbstractSpecies{
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitDiffusibleSpecies(this);
		
	}
}

package biomodel.visitor;
/**
 * Describes a species that can be visited
 * @author Nam 
 *
 */
public interface VisitableSpecies {
	/**
	 * Accepts a visitor
	 * @param visitor the visitor to accept
	 */
	public void accept(SpeciesVisitor visitor);
}

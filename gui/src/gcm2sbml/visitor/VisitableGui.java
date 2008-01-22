package gcm2sbml.visitor;
/**
 * Describes a gui that can be visited
 * @author Nam 
 *
 */
public interface VisitableGui {
	/**
	 * Accepts a visitor
	 * @param visitor the visitor to accept
	 */
	public void accept(GuiVisitor visitor);
}

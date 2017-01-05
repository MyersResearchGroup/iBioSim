package backend.verification.platu.platuLpn;

import java.util.HashMap;

import backend.verification.platu.expression.Expression;
import backend.verification.platu.expression.VarNode;

/**
 * Assignment data structure.
 */
public class VarExpr {
    private VarNode var;
    private Expression expr;

    public VarExpr(VarNode var, Expression expr){
    	this.var = var;
    	this.expr = expr;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VarExpr) {
            VarExpr other = (VarExpr) obj;
            return getVar() == other.getVar() && expr.toString().compareTo(other.expr.toString()) == 0;
        }
        
        return false;
    }

    public VarExpr copy(HashMap<String, VarNode> variables){
    	return new VarExpr((VarNode) this.var.copy(variables), this.expr.copy(variables));
    }

    /**
     * @return The variable assigned.
     */
    public VarNode getVar() {
        return var;
    }

    /**
     * @return The assignment expression.
     */
    public Expression getExpr() {
        return expr;
    }

    /**
     * @param expr The assignment expression.
     */
    public void setExpr(Expression expr) {
        this.expr = expr;
    }
    
    /**
     * @param var The assigned variable.
     */
    public void setVar(VarNode var) {
        this.var = var;
    }

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}

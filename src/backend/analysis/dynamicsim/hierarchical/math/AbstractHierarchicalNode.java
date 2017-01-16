package backend.analysis.dynamicsim.hierarchical.math;

public abstract class AbstractHierarchicalNode
{
    public static enum Type
    {
      CONSTANT_E, CONSTANT_FALSE, CONSTANT_PI, CONSTRUCTOR_PIECE, CONSTRUCTOR_OTHERWISE, CONSTANT_TRUE, DIVIDE, FUNCTION, FUNCTION_ABS, FUNCTION_ARCCOS, FUNCTION_ARCCOSH, FUNCTION_ARCCOT, FUNCTION_ARCCOTH, FUNCTION_ARCCSC, FUNCTION_ARCCSCH, FUNCTION_ARCSEC, FUNCTION_ARCSECH, FUNCTION_ARCSIN, FUNCTION_ARCSINH, FUNCTION_ARCTAN, FUNCTION_ARCTANH, FUNCTION_CEILING, FUNCTION_COS, FUNCTION_COSH, FUNCTION_COT, FUNCTION_COTH, FUNCTION_CSC, FUNCTION_CSCH, FUNCTION_DELAY, FUNCTION_EXP, FUNCTION_FACTORIAL, FUNCTION_FLOOR, FUNCTION_LN, FUNCTION_LOG, FUNCTION_PIECEWISE, FUNCTION_POWER, FUNCTION_ROOT, FUNCTION_SEC, FUNCTION_SECH, FUNCTION_SELECTOR, FUNCTION_SIN, FUNCTION_SINH, FUNCTION_TAN, FUNCTION_TANH, LAMBDA, LOGICAL_AND, LOGICAL_NOT, LOGICAL_OR, LOGICAL_XOR, MINUS, NAME, NAME_AVOGADRO, NAME_TIME, NUMBER, PLUS, POWER, PRODUCT, QUALIFIER_BVAR, QUALIFIER_DEGREE, QUALIFIER_LOGBASE, RATIONAL, REAL_E, RELATIONAL_EQ, RELATIONAL_GEQ, RELATIONAL_GT, RELATIONAL_LEQ, RELATIONAL_LT, RELATIONAL_NEQ, SUM, TIMES, UNKNOWN, VECTOR;
    };

    private Type          type;

    private String metaId;
    protected boolean       isSpecies;
    protected boolean       isReaction;
    
    
    public AbstractHierarchicalNode(Type type)
    {
      this.type = type;
    }

    public AbstractHierarchicalNode(AbstractHierarchicalNode copy)
    {
      this.type = copy.type;
      this.isSpecies = copy.isSpecies;
      this.isReaction = copy.isReaction;
    }

    /**
     * This method is used to report information about the node when printing
     * something onto a file.
     * 
     * @return
     */
    public abstract String report();

    public Type getType()
    {
      return type;
    }

    public boolean isBoolean()
    {
      return type == Type.CONSTANT_FALSE || type == Type.CONSTANT_TRUE || isLogical() || isRelational();
    }

    public boolean isLogical()
    {
      return type.toString().startsWith("LOGICAL_");
    }

    public boolean isName()
    {
      return (type == Type.NAME) || (type == Type.NAME_TIME) || (type == Type.NAME_AVOGADRO);
    }

    public boolean isConstant()
    {
      return type.toString().startsWith("CONSTANT") || type == Type.NAME_AVOGADRO;
    }

    public boolean isRelational()
    {
      return type == Type.RELATIONAL_EQ || type == Type.RELATIONAL_GEQ || type == Type.RELATIONAL_GT || type == Type.RELATIONAL_LEQ || type == Type.RELATIONAL_LT || type == Type.RELATIONAL_NEQ;
    }

    public boolean isNumber()
    {
      return type == Type.NUMBER;
    }

    public boolean isOperator()
    {
      return type == Type.PLUS || type == Type.MINUS || type == Type.TIMES || type == Type.DIVIDE || type == Type.POWER;
    }

    public boolean isTime()
    {
      return type == Type.NAME_TIME;
    }

    public boolean isSpecies()
    {
      return isSpecies;
    }

    public boolean isReaction()
    {
      return isReaction;
    }
    
    public void setMetaId(String metaId)
    {
      this.metaId = metaId;
    }

    public String getMetaId()
    {
      return metaId;
    }

}

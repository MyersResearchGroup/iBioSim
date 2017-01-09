package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;

import analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.math.ReactionNode;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;

public class ReactionSetup
{

  public static void setupReactions(HierarchicalModel modelstate, Model model)
  {
    Reaction reaction;
    for (int i = 0; i < model.getNumReactions(); i++)
    {
      reaction = model.getReaction(i);
      ParameterSetup.setupLocalParameters(modelstate, reaction.getKineticLaw(), reaction);
      if (modelstate.isDeletedBySId(reaction.getId()))
      {
        continue;
      }
      else if (ArraysSetup.checkArray(reaction))
      {
        continue;
      }
      
      setupSingleReaction(modelstate, reaction, false, model);
    }

  }

  /**
   * calculates the initial propensity of a single reaction also does some
   * initialization stuff
   * 
   * @param reactionID
   * @param reactionFormula
   * @param reversible
   * @param reactantsList
   * @param productsList
   * @param modifiersList
   */
  private static void setupSingleReaction(HierarchicalModel modelstate, Reaction reaction, boolean split, Model model)
  {

    ReactionNode reactionNode = modelstate.addReaction(reaction.getId());
    reactionNode.createState(StateType.SPARSE);
    reactionNode.setValue(modelstate.getIndex(), 0);
    
    for (SpeciesReference reactant : reaction.getListOfReactants())
    {
      SpeciesReferenceSetup.setupSingleReactant(modelstate, reactionNode, reactant.getSpecies(), reactant);
    }
    for (SpeciesReference product : reaction.getListOfProducts())
    {
      SpeciesReferenceSetup.setupSingleProduct(modelstate, reactionNode, product.getSpecies(), product);
    }
    KineticLaw kineticLaw = reaction.getKineticLaw();
    if (kineticLaw != null && kineticLaw.isSetMath())
    {
      ASTNode reactionFormula = kineticLaw.getMath();
      reactionFormula = HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, model);

      if (reaction.isReversible() && split)
      {
        setupSingleRevReaction(modelstate, reactionNode, reactionFormula);
      }
      else
      {
        setupSingleNonRevReaction(modelstate, reactionNode, reactionFormula, model);
      }
    }

  }

  private static void setupSingleNonRevReaction(HierarchicalModel modelstate, ReactionNode reactionNode, ASTNode reactionFormula, Model model)
  {
    HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, modelstate.getVariableToNodeMap(), reactionNode);
    reactionNode.setForwardRate(math);
    reactionNode.computeNotEnoughEnoughMolecules(modelstate.getIndex());
  }

  private static void setupSingleRevReaction(HierarchicalModel modelstate, ReactionNode reactionNode, ASTNode reactionFormula)
  {
    ASTNode[] splitMath = HierarchicalUtilities.splitMath(reactionFormula);
    if (splitMath == null)
    {
      HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, modelstate.getVariableToNodeMap(), reactionNode);
      reactionNode.setForwardRate(math);
      reactionNode.computeNotEnoughEnoughMolecules(modelstate.getIndex());
    }
    else
    {
      HierarchicalNode forwardRate = MathInterpreter.parseASTNode(splitMath[0], modelstate.getVariableToNodeMap(), reactionNode);
      HierarchicalNode reverseRate = MathInterpreter.parseASTNode(splitMath[1], modelstate.getVariableToNodeMap(), reactionNode);
      reactionNode.setForwardRate(forwardRate);
      reactionNode.setReverseRate(reverseRate);
      reactionNode.computeNotEnoughEnoughMolecules(modelstate.getIndex());
    }
  }

}

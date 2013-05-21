package verification.platu.markovianAnalysis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import verification.platu.project.PrjState;
import verification.platu.project.ProbabilisticPrjState;
import verification.platu.stategraph.State;
import verification.timed_state_exploration.zoneProject.StateSet;

public class ProbabilisticStateSet extends StateSet {
	
	
	private boolean stop;
	
	public ProbabilisticStateSet() {
		super();
	}

//	public boolean performSteadyStateMarkovianAnalysis(double tolerance, ArrayList<Property> conditions, ProbabilisticPrjState givenInitial) {
//		// TODO: Deal with threading (the "stop" variable).
//		if (!canPerformMarkovianAnalysis()) {
//			stop = true;
//			return false;
//		}
//		else {
//			if (givenInitial == null && !stop) {
//				givenInitial = (ProbabilisticPrjState) get_initState();
//				for (PrjState m : this) {
//					((ProbabilisticPrjState) m).setCurrentProb(0.0);
//				}
//				givenInitial.setCurrentProb(1.0);
//			}
//			resetColorsForMarkovianAnalysis();
//			int period = findPeriod(givenInitial);
//			if (period == 0) {
//				period = 1;
//			}
//			int step = 0;
//			boolean done = false;
//			if (!stop) {
//				do {
//					step++;
//					step = step % period;
//					for (PrjState m : this) {
//						// for (String state : stateGraph.keySet()) {
//						// for (State m : stateGraph.get(state)) {
//						if (((ProbabilisticPrjState) m).getColor() != -1 && ((ProbabilisticPrjState) m).getColor() % period == step) {
//							double nextProb = ((ProbabilisticPrjState) m).getCurrentProb();
//
//							
//							
//							for (StateTransitionPair next : m.getNextStatesWithTrans()) {
//								if (next.isEnabled() && next.transition > 0) {
//									nextProb = 0.0;
//								}
//							}
//							for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
//								if (prev.isEnabled()) {
//									double transProb = 0.0;
//									transProb += prev.getTransition();
//									// if
//									// (lhpn.getTransitionRateTree(prev.getTransition())
//									// !=
//									// null) {
//									// if
//									// (lhpn.getTransitionRateTree(prev.getTransition())
//									// !=
//									// null) {
//									// if
//									// (!lhpn.isExpTransitionRateTree(prev
//									// .getTransition())
//									// &&
//									// lhpn.getDelayTree(prev.getTransition())
//									// .evaluateExpr(null) == 0) {
//									// transProb = 1.0;
//									// }
//									// else {
//									// transProb =
//									// lhpn.getTransitionRateTree(prev.getTransition()).evaluateExpr(
//									// prev.getState().getVariables());
//									// }
//									// }
//									// }
//									// else {
//									// transProb = 1.0;
//									// }
//									double transitionSum = prev.getState().getTransitionSum(1.0, m);
//									if (transitionSum != 0) {
//										transProb = (transProb / transitionSum);
//									}
//									else {
//										transProb = 0.0;
//									}
//									nextProb += (prev.getState().getCurrentProb() * transProb);
//									if (stop) {
//										return false;
//									}
//								}
//							}
//							m.setNextProb(nextProb);
//						}
//						if (stop) {
//							return false;
//						}
//					}
//					if (stop) {
//						return false;
//					}
//					// }
//					boolean change = false;
//					for (PrjState m : this) {
//						// for (String state : stateGraph.keySet()) {
//						// for (State m : stateGraph.get(state)) {
//						if (((ProbabilisticPrjState) m).getColor() != -1 && ((ProbabilisticPrjState) m).getColor() % period == step) {
//							if ((((ProbabilisticPrjState) m).getCurrentProb() != 0)
//									&& (Math.abs(((((ProbabilisticPrjState) m).getCurrentProb() - ((ProbabilisticPrjState) m).getNextProb())) / ((ProbabilisticPrjState) m).getCurrentProb()) > tolerance)) {
//								change = true;
//							}
//							else if (((ProbabilisticPrjState) m).getCurrentProb() == 0 && ((ProbabilisticPrjState) m).getNextProb() > tolerance) {
//								change = true;
//							}
//							((ProbabilisticPrjState) m).setCurrentProbToNext();
//						}
//						if (stop) {
//							return false;
//						}
//					}
//					if (stop) {
//						return false;
//					}
//					// }
//					if (!change) {
//						done = true;
//					}
//				}
//				while (!done && !stop);
//			}
//			if (!stop) {
//				double totalProb = 0.0;
//				for (PrjState m : this) {
//					// for (String state : stateGraph.keySet()) {
//					// for (State m : stateGraph.get(state)) {
//					double transitionSum = m.getTransitionSum(1.0, null);
//					if (transitionSum != 0.0) {
//						m.setCurrentProb((m.getCurrentProb() / period) / transitionSum);
//					}
//					totalProb += m.getCurrentProb();
//					if (stop) {
//						return false;
//					}
//				}
//				if (stop) {
//					return false;
//				}
//				// }
//				for (State m : stateGraph) {
//					// for (String state : stateGraph.keySet()) {
//					// for (State m : stateGraph.get(state)) {
//					if (totalProb != 0.0) {
//						m.setCurrentProb(m.getCurrentProb() / totalProb);
//					}
//					if (stop) {
//						return false;
//					}
//				}
//				if (stop) {
//					return false;
//				}
//				// }
//				resetColors();
//				HashMap<String, Double> output = new HashMap<String, Double>();
//				if (conditions != null && !stop) {
//					for (Property cond : conditions) {
//						double prob = 0;
//						// for (String ss : s.split("&&")) {
//						// if (ss.split("->").length == 2) {
//						// String[] states = ss.split("->");
//						// for (String state : stateGraph.keySet()) {
//						// for (State m : stateGraph.get(state)) {
//						// ExprTree expr = new ExprTree(lhpn);
//						// expr.token = expr.intexpr_gettok(states[0]);
//						// expr.intexpr_L(states[0]);
//						// if (expr.evaluateExpr(m.getVariables()) == 1.0) {
//						// for (StateTransitionPair nextState : m
//						// .getNextStatesWithTrans()) {
//						// ExprTree nextExpr = new ExprTree(lhpn);
//						// nextExpr.token = nextExpr
//						// .intexpr_gettok(states[1]);
//						// nextExpr.intexpr_L(states[1]);
//						// if (nextExpr.evaluateExpr(nextState.getState()
//						// .getVariables()) == 1.0) {
//						// prob += (m.getCurrentProb() * (lhpn
//						// .getTransitionRateTree(
//						// nextState.getTransition())
//						// .evaluateExpr(m.getVariables()) / m
//						// .getTransitionSum(1.0, null)));
//						// }
//						// }
//						// if (stop) {
//						// return false;
//						// }
//						// }
//						// if (stop) {
//						// return false;
//						// }
//						// }
//						// if (stop) {
//						// return false;
//						// }
//						// }
//						// }
//						// else {
//						for (State m : stateGraph) {
//							// for (String state : stateGraph.keySet()) {
//							// for (State m : stateGraph.get(state)) {
//							ExprTree expr = new ExprTree(lhpn);
//							expr.token = expr.intexpr_gettok(cond.getProperty());
//							expr.intexpr_L(cond.getProperty());
//							if (expr.evaluateExpr(m.getVariables()) == 1.0) {
//								prob += m.getCurrentProb();
//							}
//							// }
//						}
//						output.put(cond.getLabel().trim(), prob);
//					}
//					String result1 = "#total";
//					String result2 = "1.0";
//					for (String s : output.keySet()) {
//						result1 += " " + s;
//						result2 += " " + output.get(s);
//					}
//					markovResults = result1 + "\n" + result2 + "\n";
//				}
//			}
//			return true;
//		}
//	}
//
//	public boolean canPerformMarkovianAnalysis() {
//		for (String trans : lhpn.getTransitionList()) {
//			if (!lhpn.isExpTransitionRateTree(trans)) {
//				JOptionPane.showMessageDialog(Gui.frame, "LPN has transitions without exponential delay.",
//						"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
//				return false;
//			}
//			for (String var : lhpn.getVariables()) {
//				if (lhpn.isRandomBoolAssignTree(trans, var)) {
//					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
//							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				if (lhpn.isRandomContAssignTree(trans, var)) {
//					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
//							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				if (lhpn.isRandomIntAssignTree(trans, var)) {
//					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
//							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//			}
//		}
//		if (lhpn.getContVars().length > 0) {
//			JOptionPane.showMessageDialog(Gui.frame, "LPN contains continuous variables.",
//					"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
//			return false;
//		}
//		return true;		
//	}
//	
//	private void resetColorsForMarkovianAnalysis() {
//		for (PrjState m : this) {
//			((ProbabilisticPrjState) m).setColor(-1);
//			if (stop) {
//				return;
//			}
//		}
//	}
//	
//	private int findPeriod(ProbabilisticPrjState state) {
//		if (stop) {
//			return 0;
//		}
//		int period = 0;
//		int color = 0;
//		state.setColor(color);
//		color = state.getColor() + 1;
//		Queue<PrjState> unVisitedStates = new LinkedList<PrjState>();
//		for (ProbabilisticPrjState s : state.getNextStates()) {
//			if (((ProbabilisticPrjState) s).getColor() == -1) {
//				((ProbabilisticPrjState) s).setColor(color);
//				unVisitedStates.add(s);
//			}
//			else {
//				if (period == 0) {
//					period = (state.getColor() - s.getColor() + 1);
//				}
//				else {
//					period = gcd(state.getColor() - s.getColor() + 1, period);
//				}
//			}
//			if (stop) {
//				return 0;
//			}
//		}
//		while (!unVisitedStates.isEmpty() && !stop) {
//			state = (ProbabilisticPrjState) unVisitedStates.poll();
//			color = state.getColor() + 1;
//			for (ProbabilisticPrjState s : state.getNextStates()) {
//				if (s.getColor() == -1) {
//					s.setColor(color);
//					unVisitedStates.add(s);
//				}
//				else {
//					if (period == 0) {
//						period = (state.getColor() - s.getColor() + 1);
//					}
//					else {
//						period = gcd(state.getColor() - s.getColor() + 1, period);
//					}
//				}
//				if (stop) {
//					return 0;
//				}
//			}
//		}
//		return period;
//	}
//
//	private int gcd(int a, int b) {
//		if (b == 0)
//			return a;
//		return gcd(b, a % b);
//	}

}

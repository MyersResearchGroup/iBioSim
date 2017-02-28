/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package backend.verification.platu.por1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import backend.verification.platu.platuLpn.LPNTranRelation;
import backend.verification.platu.platuLpn.LpnTranList;
import backend.verification.platu.stategraph.StateGraph;
import dataModels.lpn.parser.Transition;

public class AmpleSet {
	
	private HashSet<Transition> allInterleavingTrans = new HashSet<Transition>();
	private HashMap<Transition, HashSet<Transition>> interleavingSet = new HashMap<Transition, HashSet<Transition>>();
	private HashMap<Transition, HashSet<Transition>> indepTranSet = new HashMap<Transition, HashSet<Transition>>();
	
	public HashMap<Transition,HashSet<Transition>> getIndepTranSet_FromState(StateGraph[] lpnList,
			                                                           LPNTranRelation lpnTranRelation)
   {
		SearchDepFromState sg = new SearchDepFromState(lpnList,lpnTranRelation);
		indepTranSet = sg.getIndepTranSet();
		allInterleavingTrans = sg.getInterleavingTrans();
		interleavingSet = sg.getInterleavingSet();
		return indepTranSet;
   }
	
	public HashMap<Transition, HashSet<Transition>> getIndepTranSet_FromLPN(StateGraph[] lpnList)
    {
		SearchDepFromLPN sg = new SearchDepFromLPN();
        sg.setIndep(lpnList);
        indepTranSet = sg.getIndepSet();
        allInterleavingTrans = sg.getAllInterleavingTrans();
        interleavingSet = sg.getInterleavingSet();
		return indepTranSet;
    }
	
	public LpnTranList generateAmpleTranSet(LinkedList<Transition>[] enabledArray,
            HashMap<Transition,HashSet<Transition>> allIndepSet)
	{
		LpnTranList ampleSet = new LpnTranList();
		LpnTranList enableTranSet = AmpleSet.getEnableTranSet(enabledArray);
			if(enableTranSet.size()==0)
			{
				System.out.println("there is no enable transition need to be fired!");
				ampleSet = enableTranSet;
			}
			else
			{
				//if there exist independent transition
				if(allIndepSet == null || allIndepSet.size() == 0)
					ampleSet = enableTranSet;
				else
				{
					LpnTranList interleavingEnabledSet = AmpleSet.getInterleavingEnabledSet(enableTranSet, allInterleavingTrans);

					if(interleavingEnabledSet.size() == 0)   //only consider set1 and set2
						ampleSet = AmpleSet.getSubset_noInterleaving(enableTranSet, allIndepSet);
					else //partition enable transition to three part:(1)interleaving_pair(2)interleaving(3)set1 and set2
						ampleSet = AmpleSet.getSubset_withInterleaving1(interleavingEnabledSet, enableTranSet, allIndepSet,this.interleavingSet);

				}
			}
			return ampleSet;
	}
	
	public LpnTranList generateAmpleTranSet(LpnTranList enableTranSet,HashMap<Transition,HashSet<Transition>> allIndepSet)
	{
		LpnTranList ampleSet = new LpnTranList();
		if(enableTranSet.size()==0)
		{
			System.out.println("there is no enable transition need to be fired!");
			ampleSet = enableTranSet;
		}
		else
		{
			//if there exist independent transition
			if(allIndepSet == null || allIndepSet.size() == 0)
				ampleSet = enableTranSet;
			else
			{
				LpnTranList interleavingEnabledSet = AmpleSet.getInterleavingEnabledSet(enableTranSet, allInterleavingTrans);

				if(interleavingEnabledSet.size() == 0)   //only consider set1 and set2
					ampleSet = AmpleSet.getSubset_noInterleaving(enableTranSet, allIndepSet);
				else //partition enable transition to three part:(1)interleaving_pair(2)interleaving(3)set1 and set2
					ampleSet = AmpleSet.getSubset_withInterleaving1(interleavingEnabledSet, enableTranSet, allIndepSet,this.interleavingSet);

			}
		}
		return ampleSet;
	}
	/**
	 * if the enabledSet has no interleaving transition, 
	 * we partition this enableSet to two independent subset and choose the small one.
	 * @param enabledList
	 * @param indepTranSet
	 * @return
	 */
	private static LpnTranList getSubset_noInterleaving(LpnTranList enableTranSet,
			                                    HashMap<Transition,HashSet<Transition>> indepTranSet)
	{
		LpnTranList set1 = new LpnTranList();
		LpnTranList set2 = new LpnTranList();

		//partition set1 and set2
		for(Transition tran : enableTranSet)
		{
			if(set1.isEmpty())
				set1.add(tran);
			else
			{
				HashSet<Transition> indep_tran = indepTranSet.get(tran);
				if(indep_tran == null)
					set1.add(tran);
				else
				{
					boolean dep_flag = false;
					for(Transition t : set1)
					{
						if(!indep_tran.contains(t))
						{
							dep_flag = true;
							break;
						}
					}
					if(dep_flag)
						set1.add(tran);
					else
						set2.add(tran);
				}
			}
		}

		//set subset
		if(set1.size() > set2.size() && set2.size() != 0)
			return set2;
		return set1;
	}
	
	
	/**
	 * partition to 3 part: 
	 * (1)interleaving transitions, do not consider dependent transitions
	 * (2) set1 and set2
	 * @param interleavingEnabledSet
	 * @param remainEnabledList
	 * @param indepTranSet
	 * @return
	 */
		private static LpnTranList getSubset_withInterleaving1(LpnTranList interleavingEnabledSet,
													   LpnTranList enableTranSet,
				                                       HashMap<Transition,HashSet<Transition>> indepTranSet,
				                                       HashMap<Transition, HashSet<Transition>> interleavingSet)
		{
			LpnTranList subset = new LpnTranList();
			LpnTranList set1 = new LpnTranList();
			LpnTranList set2 = new LpnTranList();

			//partition to 3 part      
			for(Transition tran : enableTranSet)
			{
				if(interleavingEnabledSet.contains(tran))
					continue;
				
				if(set1.isEmpty())
					set1.add(tran);
				else
				{
					HashSet<Transition> indep_tran = indepTranSet.get(tran);
					if(indep_tran == null)
						set1.add(tran);
					else
					{
						boolean dep_flag = false;
						for(Transition t : set1)
						{
							if(!indep_tran.contains(t))
							{
								dep_flag = true;
								break;
							}
						}
						if(dep_flag)
							set1.add(tran);
						else
							set2.add(tran);
					}
				}
			}

			//choose independent subset
			if(!set1.isEmpty() || !set2.isEmpty())
			{
				//set subset
				if(set1.size() > set2.size() && set2.size() != 0)
					subset = set2;
				else
					subset = set1;
			}
			else//indep is empty
			{
				//System.out.println("there is no independent transition");
//				for(Transition tran : interleavingEnabledSet)
//					set1.add(tran);
//				return set1;
				subset = AmpleSet.subset_indepIsEmpty(interleavingEnabledSet, interleavingSet, indepTranSet);
			}
			return subset;
		}
/**
 * all transitions are interleaving transition
 * @param interleavingEnabledSet
 * @param interleavingSet
 * @param allIndepSet
 * @return
 */
	private static LpnTranList subset_indepIsEmpty(LpnTranList interleavingEnabledSet,
									       HashMap<Transition, HashSet<Transition>> interleavingSet,
										   HashMap<Transition,HashSet<Transition>> allIndepSet)
	{
		
		LpnTranList ready_interleaving = new LpnTranList();

		// if there exist interleaving pair
		for(Transition tran1 : interleavingEnabledSet)
		{
			HashSet<Transition> inter_setTran1 = interleavingSet.get(tran1);
			boolean noExist = false;
			//System.out.println("inter_setTran1.size():"+inter_setTran1.size());
			for(Transition inter : inter_setTran1)
			{
				if(!interleavingEnabledSet.contains(inter))
				{
					noExist = true;break;
				}
			}
			//interleaving pair exist, can be fire first
			if(!noExist)
			{
				ready_interleaving.add(tran1);
				for(Transition inter : inter_setTran1)
					ready_interleaving.add(inter);
				break;
			}
		}
		/*
		System.out.print("ready_interleaving:");
		for(Transition trans : ready_interleaving)
			System.out.print(trans.getFullLabel()+",");
		System.out.println();
		*/
		if(!ready_interleaving.isEmpty())
		{
			//System.out.println("there exists interleaving pairs ready to fire!");
			return ready_interleaving;
		}
		//System.out.println("choose all of the interleaving transitions which are independent with others");
		return interleavingEnabledSet;
		//return this.getSubsetOfInterleavingTrans(interleavingEnabledSet);

	}
	
	
	/**
	 * if there does not exist ready_interleaving, 
	 * get subset of the interleaving
	 * @param interleavingEnabledSet
	 * @return
	 */
	@SuppressWarnings("unused")
	private LpnTranList getSubsetOfInterleavingTrans(LpnTranList interleavingEnabledSet)
	{
		LpnTranList set1 = new LpnTranList();
		LpnTranList set2 = new LpnTranList();
		for(Transition tran : interleavingEnabledSet)
		{
			if(set1.isEmpty())
				set1.add(tran);
			else
			{
				boolean interleaving = false;
				for(Transition setTran : set1)
				{
					HashSet<Transition> inter_setTran = interleavingSet.get(setTran);
					if(inter_setTran.contains(tran))
					{
						interleaving = true;
						break;
					}
				}
				if(interleaving == true)
					set1.add(tran);
				else
					set2.add(tran);	
			}
		}
		
		if(set1.size() > set2.size() && set2.size() != 0)
			return set2;
		return set1;
	}
	
	/**
	 * get the interleaving transitions from the enable set
	 * @param enabledList
	 * @param interleavingSet
	 * @return
	 */
	private static LpnTranList getInterleavingEnabledSet(LpnTranList enableTranSet, HashSet<Transition> interleavingSet)
	{
		LpnTranList interleavingEnabledSet = new LpnTranList();
		//System.out.println("interleavingSet.size():"+interleavingSet.size());
		if(interleavingSet != null)
		{
			for(Transition tran : enableTranSet)
			{
				if(interleavingSet.contains(tran))
					interleavingEnabledSet.add(tran);
			}
		}
		return interleavingEnabledSet;
	}
	
	private static LpnTranList getEnableTranSet(LinkedList<Transition>[] enabledList)
	{
		LpnTranList enableTranSet = new LpnTranList();
		for (int index = 0; index < enabledList.length; index++) 
    	{
    		//System.out.println("index:"+index);
			if(enabledList[index].size()!=0)
			{
				for(Transition t: enabledList[index])
					enableTranSet.add(t);
			}
    	}
		//System.out.println("enableTranSet.size():"+enableTranSet.size());
		return enableTranSet;
	}
	

}

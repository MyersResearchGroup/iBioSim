package platu.por1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.lpn.LPNTranRelation;
import platu.lpn.LpnTranList;

public class AmpleSubset {
	
	private HashSet<LPNTran> allInterleavingTrans = new HashSet<LPNTran>();
	private HashMap<LPNTran, HashSet<LPNTran>> interleavingSet = new HashMap<LPNTran, HashSet<LPNTran>>();
	
	public HashMap<LPNTran,HashSet<LPNTran>> getIndepTranSet_FromState(LPN[] lpnList,
			                                                           LPNTranRelation lpnTranRelation)
   {
		HashMap<LPNTran,HashSet<LPNTran>> indepTranSet = new HashMap<LPNTran,HashSet<LPNTran>>();
		SearchDepFromState sg = new SearchDepFromState(lpnList,lpnTranRelation);
		indepTranSet = sg.getIndepTranSet();
		allInterleavingTrans = sg.getInterleavingTrans();
		interleavingSet = sg.getInterleavingSet();
		return indepTranSet;
   }
	
	public HashMap<LPNTran,HashSet<LPNTran>> getIndepTranSet_FromLPN(LPN[] lpnList)
    {
		HashMap<LPNTran,HashSet<LPNTran>> indepTranSet = new HashMap<LPNTran,HashSet<LPNTran>>();
		SearchDepFromLPN sg = new SearchDepFromLPN();
        sg.setIndep(lpnList);
        indepTranSet = sg.getIndepSet();
        allInterleavingTrans = sg.getAllInterleavingTrans();
        interleavingSet = sg.getInterleavingSet();
		return indepTranSet;
    }
	
   
	
	/**(POR)
	 * reduce enableList to ampleList
	 * @param lpnList
	 * @param stateVisited
	 * @param enabledArray
	 * @param allIndepSet
	 * @return
	 */
	public LpnTranList generateAmpleList(boolean stateVisited,
										String approach,
										LinkedList<LPNTran>[] enabledArray,
										HashMap<LPNTran,HashSet<LPNTran>> allIndepSet)
	{
		LpnTranList ampleList = new LpnTranList();
		LpnTranList enableTranSet = this.getEnableTranSet(enabledArray);
				
		if(enableTranSet.size()==0)
		{
			System.out.println("no enable transition,deadlock!");
			ampleList = enableTranSet;
		}
		else if(enableTranSet.size()==1)
		{
			ampleList = enableTranSet;
		}
		else
		{
			//cycle
	    	if(stateVisited == true)
	    	{
	    		ampleList = enableTranSet;
	    		return ampleList;
	    	}
	    	//exist independent transition
	    	if(allIndepSet == null || allIndepSet.size() == 0)
	    		ampleList = enableTranSet;
	    	else
	    	{
	    		LpnTranList interleavingEnabledSet = this.getInterleavingEnabledSet(enableTranSet, allInterleavingTrans);
	    		//System.out.println("interleavingEnabledSet.size():"+interleavingEnabledSet.size());
	    		
	    		if(interleavingEnabledSet.size() == 0)   //only consider set1 and set2
	    		{
	    			ampleList = this.getSubset_noInterleaving(enabledArray, allIndepSet);
	    			//System.out.println("ampleList.size():"+ampleList.size());
	    			return ampleList;
	    		}
	    		else //partition enable transition to four part:(1)interleaving(2)dep_interleaving(3)set1 and set2
	    		{
	    			/*
	    			System.out.print("interleaving trans:");
	    			for(LPNTran tran : interleavingEnabledSet)
	    			{
	    				System.out.print(tran.getFullLabel()+",");
	    			}
	    			System.out.println();
	    			*/
	    			ampleList = this.getSubset_withInterleaving(interleavingEnabledSet, enabledArray, allIndepSet,this.interleavingSet);
	    			return ampleList;
	    		}
	    	}
		}

		return ampleList;
	}
	
	/**
	 * get the interleaving transitions from the enable set
	 * @param enabledList
	 * @param interleavingSet
	 * @return
	 */
	private LpnTranList getInterleavingEnabledSet(LpnTranList enableTranSet, HashSet<LPNTran> interleavingSet)
	{
		LpnTranList interleavingEnabledSet = new LpnTranList();
		//System.out.println("interleavingSet.size():"+interleavingSet.size());
		if(interleavingSet != null)
		{
			for(LPNTran tran : enableTranSet)
			{
				if(interleavingSet.contains(tran))
					interleavingEnabledSet.add(tran);
			}
		}
		return interleavingEnabledSet;
	}
/*
	private LPNTranSet getInterleavingEnabledSet(LinkedList<LPNTran>[] remainEnabledList,
												 HashSet<LPNTran> interleavingSet)
	{
		LPNTranSet interleavingEnabledSet = null;
		for (int index = 0; index < remainEnabledList.length; index++) 
    	{
			if(!remainEnabledList[index].isEmpty())
			{
				LinkedList<LPNTran> enableSet = remainEnabledList[index];
				
				for(LPNTran t: enableSet)
				{
					if(interleavingSet.contains(t))
					{
						if(interleavingEnabledSet==null)
							interleavingEnabledSet = new LPNTranSet();
						interleavingEnabledSet.add(t);
						remainEnabledList[index].remove(t);
					}
				}
			}
    	}
		return interleavingEnabledSet;
	}
*/
	/**
	 * if the enabledSet has no interleaving transition, 
	 * we partition this enableSet to two independent subset and choose the small one.
	 * @param enabledList
	 * @param indepTranSet
	 * @return
	 */
	private LpnTranList getSubset_noInterleaving(LinkedList<LPNTran>[] enabledList,
			                                    HashMap<LPNTran,HashSet<LPNTran>> indepTranSet)
	{
		LpnTranList set1 = new LpnTranList();
		LpnTranList set2 = new LpnTranList();
		//order
		if(enabledList.length>1)
			this.order(enabledList);
		//partition set1 and set2
		for(int i=0;i<=enabledList.length-1;i++)
		{
			for(LPNTran tran : enabledList[i])
			{
				if(set1.isEmpty())
					set1.add(tran);
				else
				{
					HashSet<LPNTran> indep_tran = indepTranSet.get(tran);
					if(indep_tran == null)
						set1.add(tran);
					else
					{
						boolean dep_flag = false;
						for(LPNTran t : set1)
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
		}
		/*
		System.out.print("Set1:  ");
		for(LPNTran tran: set1)
		{
			System.out.print(tran.getFullLabel()+",");
		}
		System.out.println();
		System.out.print("Set2:  ");
		for(LPNTran tran: set2)
		{
			System.out.print(tran.getFullLabel()+",");
		}
		System.out.println();
		*/
		//set subset
		if(set1.size() > set2.size() && set2.size() != 0)
			return set2;
		else
			return set1;
	}
/**
 * partition to 3 part: 
 * (1)transition t1 dependent with interleaving and transition t2 dependent with t1
 * (2) set1 and set2
 * @param interleavingEnabledSet
 * @param remainEnabledList
 * @param indepTranSet
 * @return
 */
	private LpnTranList getSubset_withInterleaving(LpnTranList interleavingEnabledSet,
			                                      LinkedList<LPNTran>[] enabledList,
			                                      HashMap<LPNTran,HashSet<LPNTran>> indepTranSet,
			                                      HashMap<LPNTran, HashSet<LPNTran>> interleavingSet)
	{
		LpnTranList subset = new LpnTranList();
		LpnTranList dep_interleaving = new LpnTranList();
		LpnTranList set1 = new LpnTranList();
		LpnTranList set2 = new LpnTranList();
		//order
		if(enabledList.length>1)
			this.order(enabledList);
		//partition to 3 part               
		for(int i=0;i<=enabledList.length-1;i++)
		{
			for(LPNTran tran : enabledList[i])
			{
				if(interleavingEnabledSet.contains(tran))
					continue;
				
				HashSet<LPNTran> indep_tran = indepTranSet.get(tran);
				if(indep_tran == null)
					dep_interleaving.add(tran);
				else
				{
					boolean dep_interleavingTR = false;
					for(LPNTran interleaveTran : interleavingEnabledSet)
					{
						if(!indep_tran.contains(interleaveTran))
						{
							dep_interleavingTR = true;
							dep_interleaving.add(tran);
							break;
						}
					}
					//indep with interleaving. (1)consider indep with depset(2)consider set1 and set2
					if(dep_interleavingTR == false)
					{
						boolean dep_dep = false;
						if(!dep_interleaving.isEmpty()) 
						{
							for(LPNTran depTran : dep_interleaving)
							{
								if(!indep_tran.contains(depTran))
								{
									dep_dep = true;
									dep_interleaving.add(depTran);
									break;
								}
							}
						}
						//indep with dep_transition set	,consider set1 and set2
						if(dep_dep == false)
						{
							if(set1.isEmpty())
								set1.add(tran);
							else
							{
								boolean dep_flag = false;
								for(LPNTran t : set1)
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
			
			for(LPNTran tran : interleavingEnabledSet)
				set1.add(tran);
			for(LPNTran tran : dep_interleaving)
				set1.add(tran);
			return set1;
			
			//return this.subset_indepIsEmpty(interleavingEnabledSet, interleavingSet, dep_interleaving,indepTranSet);
		}
		return subset;
	}
	
	private LpnTranList subset_indepIsEmpty(LpnTranList interleavingEnabledSet,
									       HashMap<LPNTran, HashSet<LPNTran>> interleavingSet,
									       LpnTranList dep_interleaving,
										   HashMap<LPNTran,HashSet<LPNTran>> allIndepSet)
	{
		LpnTranList ready_interleaving = new LpnTranList();

		// if there exist interleaving pair
		for(LPNTran tran1 : interleavingEnabledSet)
		{
			HashSet<LPNTran> inter_setTran1 = interleavingSet.get(tran1);
			boolean existNotContain = false;
			for(LPNTran inter : inter_setTran1)
			{
				if(!interleavingEnabledSet.contains(inter))
				{
					existNotContain = true;break;
				}
			}
			//interleaving pair exist, can be fire first
			if(!existNotContain)
			{
				ready_interleaving.add(tran1);
				for(LPNTran inter : inter_setTran1)
					ready_interleaving.add(inter);
			}
		}
		
		if(!ready_interleaving.isEmpty())
		{
			if(!dep_interleaving.isEmpty())
			{
				LpnTranList dep = this.getDepOfInterleavingTrans(ready_interleaving, dep_interleaving, allIndepSet);
				for(LPNTran tran : dep)
					ready_interleaving.add(tran);
			}
			return ready_interleaving;
		}
		else
		{
			LpnTranList subset = this.getSubsetOfInterleavingTrans(interleavingEnabledSet);
			if(!dep_interleaving.isEmpty())
			{
				LpnTranList dep = this.getDepOfInterleavingTrans(subset, dep_interleaving, allIndepSet);
				for(LPNTran tran : dep)
					subset.add(tran);
			}
			return subset;
		}
	}
	/**
	 * if there does not exist ready_interleaving, 
	 * get subset of the interleaving
	 * @param interleavingEnabledSet
	 * @return
	 */
	private LpnTranList getSubsetOfInterleavingTrans(LpnTranList interleavingEnabledSet)
	{
		LpnTranList set1 = new LpnTranList();
		LpnTranList set2 = new LpnTranList();
		for(LPNTran tran : interleavingEnabledSet)
		{
			if(set1.isEmpty())
				set1.add(tran);
			else
			{
				boolean contain = false;
				for(LPNTran setTran : set1)
				{
					HashSet<LPNTran> inter_setTran = interleavingSet.get(setTran);
					if(inter_setTran.contains(tran))
					{
						contain = true;
						break;
					}
				}
				if(contain == true)
					set1.add(tran);
				else
					set2.add(tran);	
			}
		}
		
		if(set1.size() > set2.size() && set2.size() != 0)
			return set2;
		else
			return set1;
	}
	
	/**
	 * get the dependent transitions of the enableInterleaving transitions
	 * @param subInterleavingSet
	 * @param dep_interleaving
	 * @param allIndepSet
	 * @return
	 */
	private LpnTranList getDepOfInterleavingTrans(LpnTranList subInterleavingSet, LpnTranList dep_interleaving,
			                                     HashMap<LPNTran,HashSet<LPNTran>> allIndepSet)
	{
		//dependent on interleaving
		LpnTranList dep_sub = new LpnTranList();
		for(LPNTran tran : dep_interleaving)
		{
			HashSet<LPNTran> indep_tran = allIndepSet.get(tran);
			for(LPNTran tran_inter : subInterleavingSet)
			{
				if(!indep_tran.contains(tran_inter))
				{
					dep_sub.add(tran);break;
				}
			}
		}
		//dependent on dep_interleaving
		LpnTranList dep_dep = new LpnTranList();
		for(LPNTran tran : dep_interleaving)
		{
			if(!dep_sub.contains(tran))
			{
				HashSet<LPNTran> indep_tran = allIndepSet.get(tran);
				for(LPNTran tran_dep : dep_sub)
				{
					if(!indep_tran.contains(tran_dep))
					{
						dep_dep.add(tran);break;
					}
				}
			}
		}
		
		for(LPNTran dep : dep_dep)
		{
			dep_sub.add(dep);
		}
		return dep_sub;
	}
	
	/**
	 * order the enabledList: the smallest the first
	 * @param enabledList
	 */
	private void order(LinkedList<LPNTran>[] enabledList)
	{
		LinkedList<LPNTran> temp = new LinkedList<LPNTran>();
		for(int i=0;i<enabledList.length-1;i++)
		{
			for(int j=i+1;j<enabledList.length;j++)
			{
				if(enabledList[i].size()>enabledList[j].size())
				{
					temp = enabledList[j];
					enabledList[j] = enabledList[i];
					enabledList[i] = temp;
				}
			}
		}
	}
	
	private LpnTranList getEnableTranSet(LinkedList<LPNTran>[] enabledList)
	{
		LpnTranList enableTranSet = new LpnTranList();
		for (int index = 0; index < enabledList.length; index++) 
    	{
    		//System.out.println("index:"+index);
			if(enabledList[index].size()!=0)
			{
				for(LPNTran t: enabledList[index])
					enableTranSet.add(t);
			}
    	}
		//System.out.println("enableTranSet.size():"+enableTranSet.size());
		return enableTranSet;
	}
	
	public void printMap(HashMap<LPNTran, HashSet<LPNTran>> tranSet)
	{
		long number = 0;
		Iterator it = tranSet.keySet().iterator();
		if(!it.hasNext())
		{
			System.out.println("this transition type is null");
		}
		while(it.hasNext())
		{
			LPNTran key = (LPNTran)it.next();
			HashSet<LPNTran> value = tranSet.get(key);
			System.out.print(key.getFullLabel()+"   " );
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				number++;
				System.out.print(((LPNTran)bb.next()).getFullLabel()+",");
			}
			System.out.println();
		}
		System.out.println("number"+number);
	}
	
}

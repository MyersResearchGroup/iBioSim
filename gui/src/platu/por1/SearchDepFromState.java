package platu.por1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.lpn.LPNTranRelation;
import platu.lpn.LpnTranList;
import platu.stategraph.StateGraph;

/**
 * in this approach, we do not consider transition interleaving
 * @author Administrator
 *
 */

public class SearchDepFromState {
	
	private Set<Entry<LPNTran, Set<LPNTran>>> initialDepTrans;
	private Set<Entry<LPNTran, Set<LPNTran>>> initialInterleavingTrans;
	
	private HashSet<LPNTran> interleavingTrans = new HashSet<LPNTran>();
	private HashMap<LPNTran, HashSet<LPNTran>> indepTranSet = new HashMap<LPNTran, HashSet<LPNTran>>();
	private HashMap<LPNTran, HashSet<LPNTran>> interleavingSet = new HashMap<LPNTran, HashSet<LPNTran>>();
	
	
	public SearchDepFromState(StateGraph[] lpnList,LPNTranRelation lpnTranRelation)
	{
		this.getInitialDepTran(lpnTranRelation);      //get dependent and interleaving transitions
		this.setInterleavingTrans(initialInterleavingTrans);
		
		if(initialDepTrans.size() !=0  || initialInterleavingTrans.size() !=0)
		{
			this.setInitIndepTrans(lpnList);              //set independent number: n*(n-1)
			this.reduceIndepTrans(initialDepTrans);
			this.reduceIndepTrans(initialInterleavingTrans);
//			System.out.println("final indep:");
//			this.printMap(indepTranSet);
		}
		else
		{
			System.out.println("do not call lpnTranRelation.getDep seccessful!");
			indepTranSet = null;
		}
		/*
		System.out.println("print original depTranSet");
		this.printSet(initialDepTrans);
		System.out.println("print original interleavingSet");
		this.printSet(initialInterleavingTrans);
		//this.printMap(interleavingSet);
		*/
		
	
	}
	
	public HashMap<LPNTran, HashSet<LPNTran>> getIndepTranSet()
	{
		return this.indepTranSet;
	}
	
	public HashSet<LPNTran> getInterleavingTrans()
	{
		return this.interleavingTrans;
	}
	
	public HashMap<LPNTran, HashSet<LPNTran>> getInterleavingSet()
	{
		return this.interleavingSet;
	}
	
	/**
	 * get individual dependent transition
	 * get initial interleaving transition
	 * @param lpnTranRelation
	 */
	private void getInitialDepTran(LPNTranRelation lpnTranRelation)
	{
		lpnTranRelation.findCompositionalDependencies();
		//get individual dependent
		initialDepTrans = lpnTranRelation.getDependentTrans();
		//get interleaving
		initialInterleavingTrans = lpnTranRelation.getInterleavingTrans();
		this.setInterleavingSet(initialInterleavingTrans);
		
//		System.out.println("print original depTranSet");
//		this.printSet(initialDepTrans);
//		System.out.println("print original interleavingSet");
//		this.printSet(initialInterleavingTrans);
		
	}
	
	private void setInterleavingTrans(Set<Entry<LPNTran, Set<LPNTran>>> initialInterleavingTrans)
	{
		Iterator in = initialInterleavingTrans.iterator();  
		while(in.hasNext())
		{
			Map.Entry me = (Map.Entry)in.next();
			LPNTran key = (LPNTran)me.getKey();
			Set<LPNTran> value = (Set<LPNTran>)me.getValue();
			
			interleavingTrans.add(key);
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				interleavingTrans.add((LPNTran)bb.next());
			}	
		}
	}
	private void setInterleavingSet(Set<Entry<LPNTran, Set<LPNTran>>> initialInterleavingTrans)
	{
		Iterator in = initialInterleavingTrans.iterator();  
		while(in.hasNext())
		{
			Map.Entry me = (Map.Entry)in.next();
			LPNTran key = (LPNTran)me.getKey();
			Set<LPNTran> value = (Set<LPNTran>)me.getValue();
			
			HashSet<LPNTran> key_inter = this.interleavingSet.get(key);
			if(key_inter == null)
			{
				key_inter = new HashSet<LPNTran>();
				this.interleavingSet.put(key, key_inter);
			}
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				key_inter.add((LPNTran)bb.next());
			}
		}
	}
	/**
	 * get all transition, save to indepTranSet(Initial),independent number: n*(n-1)
	 * @param lpnList
	 * @return
	 */
	private void setInitIndepTrans(StateGraph[] lpnList)
	{
		//get all transitions
		LpnTranList allTran = new LpnTranList();
		for(StateGraph sg : lpnList)
		{
			// TODO: need to user our transitions.
			/*
			LpnTranList trans = sg.getLpn().getTransitions();
			for(LPNTran tran: trans)
			{
				allTran.add(tran);
			}
			*/
		}
		//save to indepTranSet
		for(LPNTran key: allTran)
		{
			HashSet<LPNTran> value = indepTranSet.get(key);
			if(value == null)
			{
				value = new HashSet<LPNTran>();
				indepTranSet.put(key, value);
			}
			for(LPNTran t: allTran)
			{
				if(t!=key)
					value.add(t);
			}
		}
	}
	
	private void reduceIndepTrans(Set<Entry<LPNTran, Set<LPNTran>>> initialDepTrans)
	{
		Iterator it = initialDepTrans.iterator();
		while(it.hasNext())
		{
			Map.Entry me = (Map.Entry)it.next();
			LPNTran key = (LPNTran)me.getKey();
			Set<LPNTran> value = (Set<LPNTran>)me.getValue();
			Iterator itor = value.iterator();
			while(itor.hasNext())
			{
				LPNTran tran = (LPNTran)itor.next();
				//reduce indep
				this.reduceIndepSet_pair(key, tran);
				this.reduceIndepSet_pair(tran, key);
			}
		}
	}

	/**
	 * refine independent set{minus (key,tran) and (tran, key)}
	 * @param key
	 * @param tran
	 */
	private void reduceIndepSet_pair(LPNTran key, LPNTran tran)
	{
		HashSet<LPNTran> value = indepTranSet.get(key);
		if(value != null)
		{
			Iterator it = value.iterator();
			while(it.hasNext())
			{
				LPNTran object = (LPNTran)it.next();
				if(object == tran)
				{
					value.remove(object);
					break;
				}
			}
		}
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
	
	public void printSet(Set<Entry<LPNTran, Set<LPNTran>>> tranSet)
	{
		Iterator in = tranSet.iterator();  
		if(!in.hasNext())
		{
			System.out.println("this transition type is null");
		}
		while(in.hasNext())
		{
			Map.Entry me = (Map.Entry)in.next();
			LPNTran key = (LPNTran)me.getKey();
			Set<LPNTran> value = (Set<LPNTran>)me.getValue();
			System.out.print(key.getFullLabel()+"   " );
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				System.out.print(((LPNTran)bb.next()).getFullLabel()+",");
			}
			System.out.println();
		}
	}

}

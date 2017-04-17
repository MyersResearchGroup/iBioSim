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
package edu.utah.ece.async.dataModels.verification.platu.por1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.utah.ece.async.dataModels.lpn.Transition;
import edu.utah.ece.async.dataModels.verification.platu.platuLpn.LPNTranRelation;
import edu.utah.ece.async.dataModels.verification.platu.platuLpn.LpnTranList;
import edu.utah.ece.async.dataModels.verification.platu.stategraph.StateGraph;

/**
 * in this approach, we do not consider transition interleaving
 * @author Administrator
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SearchDepFromState {
	
	private Set<Entry<Transition, Set<Transition>>> initialDepTrans;
	private Set<Entry<Transition, Set<Transition>>> initialInterleavingTrans;
	
	private HashSet<Transition> interleavingTrans = new HashSet<Transition>();
	private HashMap<Transition, HashSet<Transition>> indepTranSet = new HashMap<Transition, HashSet<Transition>>();
	private HashMap<Transition, HashSet<Transition>> interleavingSet = new HashMap<Transition, HashSet<Transition>>();
	
	
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
	
	public HashMap<Transition, HashSet<Transition>> getIndepTranSet()
	{
		return this.indepTranSet;
	}
	
	public HashSet<Transition> getInterleavingTrans()
	{
		return this.interleavingTrans;
	}
	
	public HashMap<Transition, HashSet<Transition>> getInterleavingSet()
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
		lpnTranRelation.printCompositionalDependencies();
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
	
	private void setInterleavingTrans(Set<Entry<Transition, Set<Transition>>> initialInterleavingTrans)
	{
		Iterator in = initialInterleavingTrans.iterator();  
		while(in.hasNext())
		{
			Map.Entry me = (Map.Entry)in.next();
			Transition key = (Transition)me.getKey();
			Set<Transition> value = (Set<Transition>)me.getValue();
			
			interleavingTrans.add(key);
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				interleavingTrans.add((Transition)bb.next());
			}	
		}
	}
	private void setInterleavingSet(Set<Entry<Transition, Set<Transition>>> initialInterleavingTrans)
	{
		Iterator in = initialInterleavingTrans.iterator();  
		while(in.hasNext())
		{
			Map.Entry me = (Map.Entry)in.next();
			Transition key = (Transition)me.getKey();
			Set<Transition> value = (Set<Transition>)me.getValue();
			
			HashSet<Transition> key_inter = this.interleavingSet.get(key);
			if(key_inter == null)
			{
				key_inter = new HashSet<Transition>();
				this.interleavingSet.put(key, key_inter);
			}
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				key_inter.add((Transition)bb.next());
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
		//for(StateGraph sg : lpnList)
		//{
			// TODO: (future) need to user our transitions.
			/*
			LpnTranList trans = sg.getLpn().getTransitions();
			for(LPNTran tran: trans)
			{
				allTran.add(tran);
			}
			*/
		//}
		//save to indepTranSet
		for(Transition key: allTran)
		{
			HashSet<Transition> value = indepTranSet.get(key);
			if(value == null)
			{
				value = new HashSet<Transition>();
				indepTranSet.put(key, value);
			}
			for(Transition t: allTran)
			{
				if(t!=key)
					value.add(t);
			}
		}
	}
	
	private void reduceIndepTrans(Set<Entry<Transition, Set<Transition>>> initialDepTrans)
	{
		Iterator it = initialDepTrans.iterator();
		while(it.hasNext())
		{
			Map.Entry me = (Map.Entry)it.next();
			Transition key = (Transition)me.getKey();
			Set<Transition> value = (Set<Transition>)me.getValue();
			Iterator itor = value.iterator();
			while(itor.hasNext())
			{
				Transition tran = (Transition)itor.next();
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
	private void reduceIndepSet_pair(Transition key, Transition tran)
	{
		HashSet<Transition> value = indepTranSet.get(key);
		if(value != null)
		{
			Iterator it = value.iterator();
			while(it.hasNext())
			{
				Transition object = (Transition)it.next();
				if(object == tran)
				{
					value.remove(object);
					break;
				}
			}
		}
	}
	
	public static void printMap(HashMap<Transition, HashSet<Transition>> tranSet)
	{
		long number = 0;
		Iterator it = tranSet.keySet().iterator();
		if(!it.hasNext())
		{
			System.out.println("this transition type is null");
		}
		while(it.hasNext())
		{
			Transition key = (Transition)it.next();
			HashSet<Transition> value = tranSet.get(key);
			System.out.print(key.getFullLabel()+"   " );
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				number++;
				System.out.print(((Transition)bb.next()).getFullLabel()+",");
			}
			System.out.println();
		}
		System.out.println("number"+number);
	}
	
	public static void printSet(Set<Entry<Transition, Set<Transition>>> tranSet)
	{
		Iterator in = tranSet.iterator();  
		if(!in.hasNext())
		{
			System.out.println("this transition type is null");
		}
		while(in.hasNext())
		{
			Map.Entry me = (Map.Entry)in.next();
			Transition key = (Transition)me.getKey();
			Set<Transition> value = (Set<Transition>)me.getValue();
			System.out.print(key.getFullLabel()+"   " );
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				System.out.print(((Transition)bb.next()).getFullLabel()+",");
			}
			System.out.println();
		}
	}

}

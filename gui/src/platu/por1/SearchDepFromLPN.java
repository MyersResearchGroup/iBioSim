package platu.por1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import platu.expression.VarNode;
import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.lpn.LpnTranList;

/**
 * in this approach, we consider 7 conditions for dependent transition
 * Seperate the interleaving transition and dependent transition.
 * @author Administrator
 *
 */

public class SearchDepFromLPN {
	
	private HashMap<LPNTran, HashSet<LPNTran>> indepTranSet = new HashMap<LPNTran, HashSet<LPNTran>>();
	private HashMap<LPNTran, HashSet<LPNTran>> interleavingSet = new HashMap<LPNTran, HashSet<LPNTran>>();
	private HashSet<LPNTran> interleavingTrans = new HashSet<LPNTran>();
	
    public HashMap<LPNTran, HashSet<LPNTran>> getIndepSet()
    {
    	return this.indepTranSet;
    }
    public HashSet<LPNTran> getAllInterleavingTrans()
    {
    	return this.interleavingTrans;
    }
    public HashMap<LPNTran, HashSet<LPNTran>> getInterleavingSet()
    {
    	return this.interleavingSet;
    }
    
    public void setIndep(LPN[] lpnList)
    {
    	int number = 0;
    	for(LPN lpn : lpnList)
    		number = number + lpn.getTransitions().size();
    	LPNTran[] wholeSet = new LPNTran[number];
    	
    	int i=0;
    	for(LPN lpn : lpnList)
    	{
    		for(LPNTran tran : lpn.getTransitions())
    		{
    			wholeSet[i] = tran;
    			i++;
    		}
    	}
    	//System.out.println("wholeSet.size():"+wholeSet.length + " i : "+i);
    	
    	for(int t1=0;t1<wholeSet.length-1;t1++)
    	{
    		for(int t2=t1+1;t2<wholeSet.length;t2++)
    		{
    			//System.out.println(wholeSet[0].getFullLabel()+","+wholeSet[1]).getFullLabel());
    			int flag = this.checkDep(wholeSet[t1], wholeSet[t2], wholeSet);
    			//independent
    			if(flag == 0)
    				this.addTran(wholeSet[t1], wholeSet[t2], indepTranSet);
    			else if(flag == 2)
    				this.addTran(wholeSet[t1], wholeSet[t2], interleavingSet);
    		}
    	}
    	/*
    	System.out.println("indepTranSet");
    	this.printMap(indepTranSet);
    	System.out.println("interleaving");
    	this.printMap(interleavingSet);
    	*/
    	this.setInterleavingTrans(interleavingSet);
    }
	/**
	 * add transition pair(t1, t2)(t2, t1) to transition relation set.
	 * @param t1
	 * @param t2
	 * @param LPNTransSet
	 */
    private void addTran(LPNTran t1,LPNTran t2,HashMap<LPNTran, HashSet<LPNTran>> LPNTransSet)
    {
    	HashSet<LPNTran> t1_dep = LPNTransSet.get(t1);
    	if(t1_dep == null)
    	{
    		t1_dep = new HashSet<LPNTran>();
    		LPNTransSet.put(t1, t1_dep);
    	}
    	t1_dep.add(t2);
    	
    	HashSet<LPNTran> t2_dep = LPNTransSet.get(t2);
    	if(t2_dep == null)
    	{
    		t2_dep = new HashSet<LPNTran>();
    		LPNTransSet.put(t2, t2_dep);
    	}
    	t2_dep.add(t1);
    }
	/**
	 * flag 0 : represent independent
	 * flag 1:  represent calsality
	 * flag 2: represent interleaving 
	 * @param t1
	 * @param t2
	 * @param wholeSet
	 * @return
	 */
	private int checkDep(LPNTran t1, LPNTran t2, LPNTran[] wholeSet)
	{
		int flag = 0;	
		//in same LPN
		if(t1.getLpn().getLabel().equals(t2.getLpn().getLabel()) )
		{
			if(this.ConditionCommonPreset(t1, t2))
			{
				flag = 2;
				//System.out.println("condition 1:"+ t1.getFullLabel()+ " same preset, interleaving "+t2.getFullLabel());
				return flag;
			}
			else if(this.ConditionPrePost(t1, t2) || this.ConditionPrePost(t2, t1))
			{
				flag = 1;
				//System.out.println("condition 2,3:"+ t1.getFullLabel()+ " pre-post,calsality "+t2.getFullLabel());
				return flag;
			}
			
		}
		//change
		if(this.ConditionSameAssignVar(t1, t2))
		{
			flag = 2;
			//System.out.println("condition 4:"+t1.getFullLabel()+ " same assign, interleaving "+t2.getFullLabel());
		}
		//change
		else if(this.ConditionSuppAssign(t1, t2) || this.ConditionSuppAssign(t2, t1))
		{
			flag = 1;
			//System.out.println("condition 5,6:"+t1.getFullLabel()+ " supp assign calsality "+t2.getFullLabel());//????????
		}
		//change
		else if(this.ConditionChangeThirdTrans(t1, t2, wholeSet))
		{
			flag = 2;
			//System.out.println("condition 7:"+t1.getFullLabel()+ " third transition interleaving "+t2.getFullLabel());
		}
		/*
		else
		{
			System.out.println(t1.getFullLabel()+ " independent "+t2.getFullLabel());
		}
		*/
		return flag;
	}
	
	/**Condition 1
	 * check if these two transition has common preset
	 * @param t1
	 * @param t2
	 * @return
	 */
	private boolean ConditionCommonPreset(LPNTran t1, LPNTran t2)
	{
		boolean flag = false;
		for(int i : t1.getPreSet())
		{
			for(int j: t2.getPreSet())
			{
				if(i==j)
				{
					flag = true;
					break;
				}
			}
			if(flag==true)
				break;
		}	
		return flag;
	}
	
	/**Condition 2 and 3
	 * t1 -> t2
	 * @param t1
	 * @param t2
	 * @return
	 */
	private boolean ConditionPrePost(LPNTran t1, LPNTran t2)
	{
		boolean flag = false;
		for(int i : t2.getPreSet())
		{
			if(t1.getPostSet().contains(i))
			{
				flag = true;
				break;
			}
		}		
		return flag;
	}
	/**Condition 4
	 * check if two transition change the same variable
	 * @param t1
	 * @param t2
	 * @return
	 */
	private boolean ConditionSameAssignVar(LPNTran t1, LPNTran t2)
	{
		boolean flag = false;	
		for(VarNode var1 : t1.getAssignedVar())
		{
			for(VarNode var2 : t2.getAssignedVar())
			{
				if(var2.getName() == var1.getName())
				{
					flag = true;
					break;
				}
			}
			if(flag==true)
				break;
		}
		return flag;
	}
	/**Condition 5 and 6
	 * t1 effect t2
	 * @param t1
	 * @param t2
	 * @return
	 */
	private boolean ConditionSuppAssign(LPNTran t1, LPNTran t2)
	{
		boolean flag = false;
		for(VarNode var1 : t1.getAssignedVar())
		{
			if(t2.getSupportVar().contains(var1.getName()))
			{
				flag = true;break;
			}
		}
		return flag;
	}
	/**Condition 7
	 * if two transition all change third transition's support variable, then 
	 * these two transition need to be consider as interleaving.
	 * @param t1
	 * @param t2
	 * @param set
	 * @return
	 */
	private boolean ConditionChangeThirdTrans(LPNTran t1, LPNTran t2, LPNTran[] wholeSet)
	{
		boolean flag = false;
		for(LPNTran t3 : wholeSet)
		{
			if(t3 != t1 && t3 != t2)
			{
				boolean flag_1 = false;
				boolean flag_2 = false;
				for(VarNode var : t1.getAssignedVar())
				{
					if(t3.getSupportVar().contains(var.getName()))
					{
						flag_1 = true;break;
					}
				}
				for(VarNode var : t2.getAssignedVar())
				{
					if(t3.getSupportVar().contains(var.getName()))
					{
						flag_2 = true;break;
					}
				}
				if(flag_1 == true && flag_2 == true)
				{
					flag = true;
					//System.out.println("third : "+ t3.getFullLabel());
					break;
				}
			}
		}
		return flag;
	}
	
	private void setInterleavingTrans(HashMap<LPNTran, HashSet<LPNTran>> interleavingSet)
	{
		Iterator in = interleavingSet.entrySet().iterator();  
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
	
	public void printMap(HashMap<LPNTran, HashSet<LPNTran>> tranSet)
	{
		//long number = 0;
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
				//number++;
				System.out.print(((LPNTran)bb.next()).getFullLabel()+",");
			}
			System.out.println();
		}
		//System.out.println("number"+number);
	}
	

}

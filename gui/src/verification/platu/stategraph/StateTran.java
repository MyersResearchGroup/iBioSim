/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package verification.platu.stategraph;

import verification.platu.lpn.LPNTran;

/**
 *
 * @author ldtwo
 */
public class StateTran {

    public State head;
    public State tail;
    public LPNTran lpnTran;
    int hashVal = 0;

    public StateTran(State tail, LPNTran lpnTran, State head) {
        this.head = head;
        this.tail = tail;
        this.lpnTran = lpnTran;
    }

    @Override
	public int hashCode() {
    	if(hashVal == 0){
			final int prime = 31;
			hashVal = 17;
			hashVal = prime * hashVal + head.hashCode();
			hashVal = prime * hashVal + lpnTran.hashCode();
			hashVal = prime * hashVal + tail.hashCode();
    	}
    	
		return hashVal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		StateTran other = (StateTran) obj;
		if (head == null) {
			if (other.head != null)
				return false;
		} 
		else if (!head.equals(other.head)){
			return false;
		}
		
		if (lpnTran == null) {
			if (other.lpnTran != null)
				return false;
		} 
		else if (!lpnTran.equals(other.lpnTran)){
			return false;
		}
		
		if (tail == null) {
			if (other.tail != null)
				return false;
		} 
		else if (!tail.equals(other.tail)){
			return false;
		}
		
		return true;
	}
}

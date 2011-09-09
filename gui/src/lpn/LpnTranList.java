package platu.lpn;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import platu.expression.VarNode;

final public class LpnTranList extends LinkedList<LPNTran> {

    private LPN lpn;

    public LpnTranList() {
        super();
    }

    public LpnTranList(Collection<? extends LPNTran> lpnt) {
        super(lpnt);
    }

    public LpnTranList(final int i) {
    }

    public LPNTran get(LPNTran lpnt) {
        for (LPNTran t : this) {
            if (lpnt.getLabel() == t.getLabel()) {
                return t;
            }
        }
        return null;
    }

    /**
     * @return the lpn
     */
    public LPN getLpn() {
        return lpn;
    }

    /**
     * @param lpn the lpn to set
     */
    public void setLPN(LPN lpn) {
        this.lpn = lpn;
        for (LPNTran t : this) {
            t.setLpn(lpn);
        }
    }

    @Override
    public String toString() {
        String ret = "";
        Iterator<LPNTran> it = this.iterator();
        while (it.hasNext()) {
            ret += it.next().getLabel();
            if (it.hasNext()) {
                ret += ", ";
            }
        }
        return "[" + ret + "]";
    }

    @Override
    public LpnTranList clone() {
        return new LpnTranList(this);
    }
    
    public LpnTranList copy(HashMap<String, VarNode> variables){
    	LpnTranList copy = new LpnTranList();
    	for(LPNTran lpnTran : this){
    		copy.add(lpnTran.copy(variables));
    	}
    	
    	return copy;
    }
}

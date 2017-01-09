package backend.verification.platu.platuLpn;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import backend.lpn.parser.LPN;
import backend.lpn.parser.Transition;

final public class LpnTranList extends LinkedList<Transition> {

	private static final long serialVersionUID = 1L;
	private LPN lpn;

    public LpnTranList() {
        super();
    }

    public LpnTranList(Collection<? extends Transition> lpnt) {
        super(lpnt);
    }

	public LpnTranList(final int i) {
    }

    public Transition get(Transition lpnt) {
    	for (Transition t: this) {
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
     * @param lpn2 the lpn to set
     */
    public void setLPN(LPN lpn2) {
        this.lpn = lpn2;
        for (Transition t : this) {
            t.setLpn(lpn2);
        }
    }

    @Override
    public String toString() {
        String ret = "";
        Iterator<Transition> it = this.iterator();
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
    
    // TODO: (check) copy has been rewritten.
    /*
    public LpnTranList copy(HashMap<String, VarNode> variables){
    	LpnTranList copy = new LpnTranList();
    	for(LPNTran lpnTran : this){
    		copy.add(lpnTran.copy(variables));
    	}
    	
    	return copy;
    }
    */
    public LpnTranList copy() {
    	LpnTranList copy = new LpnTranList();
    	for (Transition lpnTran: this) {
    		copy.add(lpnTran);
    	}
    	return copy;
    }

	public void setLPN(PlatuLPN lpn2) {
		// Hack here. This is used to get rid of errors in PlatuGrammearParser.
		
	}

	public void add(LPNTran transition4) {
		// Hack here. This is used to get rid of errors in PlatuGrammearParser.
		
	}
}

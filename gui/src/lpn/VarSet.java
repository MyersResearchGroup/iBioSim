/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu.lpn;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author ldtwo
 */
public class VarSet extends HashSet<String> {

   public    VarSet(HashSet<String> in) {
        super(in);
    }

  public  VarSet(String[] in) {
        super(Arrays.asList(in));
    }

    public VarSet() {
        super(0);
    }

    @Override
    public VarSet clone() {
        VarSet set=new VarSet();
        set.addAll(this);
        return set;
    }
}

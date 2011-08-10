package platu;

import java.util.*;
import platu.PlatuObj;

public class IndexObjMap {
	
	protected HashMap<PlatuObj, PlatuObj> uniqueObjTbl;
	protected HashMap<String, PlatuObj> str2ObjTbl;
	protected HashMap<Integer, PlatuObj> idx2ObjTbl;
	
	public IndexObjMap() {
		uniqueObjTbl = new HashMap<PlatuObj, PlatuObj>();
		str2ObjTbl = new HashMap<String, PlatuObj>();
		idx2ObjTbl = new HashMap<Integer, PlatuObj>();
	}

	public PlatuObj add(PlatuObj obj) {
		PlatuObj objCopy = uniqueObjTbl.get(obj);
		if(objCopy != null)
			return objCopy;
		
		this.uniqueObjTbl.put(obj, obj);
		
		String objLabel = obj.getLabel();
		if(objLabel != null)
			if(this.str2ObjTbl.containsKey(objLabel) == false)
				this.str2ObjTbl.put(obj.getLabel(), obj);
			else
				;// Thrown an exception
		
		int idx = this.idx2ObjTbl.size();
		obj.setIndex(idx);
		this.idx2ObjTbl.put(idx, obj);
		return obj;
	}
	
	public PlatuObj get(String label) {
		return this.str2ObjTbl.get(label);
	}
	
	public PlatuObj get(Integer index) {
		return this.idx2ObjTbl.get(index);
	}
	
	public int size() {
		return this.str2ObjTbl.size();
	}
}

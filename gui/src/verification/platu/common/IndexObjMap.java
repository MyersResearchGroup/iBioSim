package verification.platu.common;

import java.util.HashMap;

import verification.platu.common.PlatuObj;

public class IndexObjMap<T extends PlatuObj> {
	
	protected HashMap<T, T> uniqueObjTbl;
	protected HashMap<String, T> str2ObjTbl;
	protected HashMap<Integer, T> idx2ObjTbl;
	
	public IndexObjMap() {
		uniqueObjTbl = new HashMap<T, T>();
		str2ObjTbl = new HashMap<String, T>();
		idx2ObjTbl = new HashMap<Integer, T>();
	}

	public T add(T obj) {
		T objCopy = uniqueObjTbl.get(obj);
		if(objCopy != null)
			return objCopy;
		
		this.uniqueObjTbl.put(obj, obj);
		
		String objLabel = obj.getLabel();
		if(objLabel != null){
			if(this.str2ObjTbl.containsKey(objLabel) == false)
				this.str2ObjTbl.put(obj.getLabel(), obj);
			else
				;// Thrown an exception
		}
		
		int idx = this.idx2ObjTbl.size();
		obj.setIndex(idx);
		this.idx2ObjTbl.put(idx, obj);
		return obj;
	}
	
	public T get(String label) {
		return this.str2ObjTbl.get(label);
	}
	
	public T get(Integer index) {
		return this.idx2ObjTbl.get(index);
	}
	
	public int size() {
		return this.uniqueObjTbl.size();
	}
}

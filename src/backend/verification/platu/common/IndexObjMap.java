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
package backend.verification.platu.common;

import java.util.HashMap;

import backend.verification.platu.common.PlatuObj;

public class IndexObjMap<T extends PlatuObj> {
	
	protected HashMap<T, T> uniqueObjTbl;
	// TODO: (temp) str2ObjTbl is not used. 
//	protected HashMap<String, T> str2ObjTbl;
	protected HashMap<Integer, T> idx2ObjTbl;
	
	public IndexObjMap() {
		uniqueObjTbl = new HashMap<T, T>();
//		str2ObjTbl = new HashMap<String, T>();
		idx2ObjTbl = new HashMap<Integer, T>();
	}

	public T add(T obj) {
		T objCopy = uniqueObjTbl.get(obj);
		if(objCopy != null)
			return objCopy;
		this.uniqueObjTbl.put(obj, obj);
		
//		String objLabel = obj.getLabel();
//		if(objLabel != null){
//			if(this.str2ObjTbl.containsKey(objLabel) == false)
//				this.str2ObjTbl.put(obj.getLabel(), obj);
//			else
//				;// Throw an exception
//		}
		
		int idx = this.idx2ObjTbl.size();
		try {
			if (idx > 2147483647) // Index is greater than the maximum number that int can represents. 
				throw new Exception();
		} catch (Exception e) {
			System.out.println("Integer overflow.");
			e.printStackTrace();
		}
		obj.setIndex(idx);
		this.idx2ObjTbl.put(idx, obj);
		return obj;
	}
	
//	public T get(String label) {
//		return this.str2ObjTbl.get(label);
//	}
	
	public T get(Integer index) {
		return this.idx2ObjTbl.get(index);
	}
	
	public int size() {
		return this.uniqueObjTbl.size();
	}
}

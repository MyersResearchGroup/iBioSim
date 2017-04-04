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
package edu.utah.ece.async.dataModels.verification.platu.platuLpn;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public  class DualHashMap<Key, Value> extends HashMap<Key, Value> {
    private static final long serialVersionUID = 239875623984691837L;
    HashMap<Value, Key> ValueMap;

    public DualHashMap(int size) {
        super(size);
        ValueMap = new HashMap<Value, Key>(size);
    }

    public DualHashMap() {
        ValueMap = new HashMap<Value, Key>();
    }

    public DualHashMap(HashMap<Key, Value> other) {
    	ValueMap = new HashMap<Value, Key>(other.size());
        for (Entry<Key, Value> e : other.entrySet()) {
            insert(e.getKey(), e.getValue());
        }
    }

    public DualHashMap(HashMap<Key, Value> other, int size) {
        super(size);
        ValueMap = new HashMap<Value, Key>(size);
        for (Entry<Key, Value> e : other.entrySet()) {
            insert(e.getKey(), e.getValue());
        }
    }

    /**
     * Adds a <key, value> pair into the dual hash map.  This will override existing key and value pairs.
     * @param key
     * @param value
     */
    public void insert(Key key, Value value) {
        put(key, value);
        ValueMap.put(value, key);
    }

    /**
     * Removes the mapping associated with the Key.
     * @param key
     */
    public void delete(Key key) {
        Value v = remove(key);
        ValueMap.remove(v);
    }

    /**
     * @param value Value associated with key.
     * @return Key associated with the specified value, otherwise null.
     */
    public Key getKey(Value value) {
        return ValueMap.get(value);
    }

    /**
     * @param key Key associated with value.
     * @return Returns the value associated with the specified key, otherwise null.
     */
    public Value getValue(Key key) {
        return get(key);
    }
    
    @Override
    public DualHashMap<Key, Value> clone(){
    	DualHashMap<Key, Value> copy = new DualHashMap<Key, Value>();
    	for(Entry<Key, Value> e : this.entrySet()){
    		copy.insert(e.getKey(), e.getValue());
    	}
    	
    	return copy;
    }
}

package backend.verification.platu.lpn;

import java.util.HashMap;
import java.util.Map.Entry;

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

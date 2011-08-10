/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lmoore.zone.map;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import platu.Main;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Note: originally released under the GNU LGPL v2.1,
 * but rereleased by the original author under the ASF license (above).
 */

/**
 * <p>A hash map that uses primitive ints for the key rather than objects.</p>
 *
 * <p>Note that this class is for internal optimization purposes only, and may
 * not be supported in future releases of Apache Commons Lang.  Utilities of
 * this sort may be included in future releases of Apache Commons Collections.</p>
 *
 * @author Apache Software Foundation
 * @author Justin Couch
 * @author Alex Chaffee (alex@apache.org)
 * @since 2.0
 * @version $Revision$
 * @see java.util.HashMap
 */
class IntHashMap3 {

    /**
     * The hash table data.
     */
    private transient Entry table[];

    /**
     * The total number of entries in the hash table.
     */
    private transient int count;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     *
     * @serial
     */
    private int threshold;

    /**
     * The load factor for the hashtable.
     *
     * @serial
     */
    private final float loadFactor;

    Set<Integer> keySet() {
        Set s=new TreeSet();
        for(int i=0;i<table.length;i++)
        for (Entry e = table[i]; e != null; e = e.next) {
           s.add(e.hash);
        }
        return s;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        IntHashMap3 n=new IntHashMap3(this.size());
        int idx=0;
        for(Entry e=table[idx];idx<table.length;idx++){
            for(Entry v=e;v!=null&& v.next!=null;v=e.next){
                n.put(v.hash,v.value);
            }
        }
        return n;
    }

    Iterable<Map.Entry<Integer, Integer>> entrySet() {
        ArrayList<Map.Entry<Integer, Integer>> ret=new ArrayList<Map.Entry<Integer, Integer>>();
        for(final Entry e:table){
            ret.add(new Map.Entry<Integer, Integer>(){

                @Override
                public Integer getKey() {
                    return e.hash;
                }

                @Override
                public Integer getValue() {
                   return e.value;
                }

                @Override
                public Integer setValue(Integer value) {
                    int old =e.value;
                    e.value=value;
                    return old;
                }

                @Override
                public String toString() {
                    if(e==null)return "null";
                    return  "("+e.hash+","+e.value+")";
                }

            });
        }
        return ret;
    }
    /**
     * <p>Innerclass that acts as a datastructure to create a new entry in the
     * table.</p>
     */
    private static class Entry {
        final int hash;
        int value;
        Entry next;

        /**
         * <p>Create a new entry with the given values.</p>
         *
         * @param hash The code used to hash the object with
         * @param key The key used to enter this in the table
         * @param value The value for this key
         * @param next A reference to the next entry in the table
         */
        protected Entry(int hash, int value, Entry next) {
            this.hash = hash;
            this.value = value;
            this.next = next;
        }
    }

    /**
     * <p>Constructs a new, empty hashtable with a default capacity and load
     * factor, which is <code>20</code> and <code>0.75</code> respectively.</p>
     */
    public IntHashMap3() {
        this(4, 0.75f);
    }

    /**
     * <p>Constructs a new, empty hashtable with the specified initial capacity
     * and default load factor, which is <code>0.75</code>.</p>
     *
     * @param  initialCapacity the initial capacity of the hashtable.
     * @throws IllegalArgumentException if the initial capacity is less
     *   than zero.
     */
    public IntHashMap3(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * <p>Constructs a new, empty hashtable with the specified initial
     * capacity and the specified load factor.</p>
     *
     * @param initialCapacity the initial capacity of the hashtable.
     * @param loadFactor the load factor of the hashtable.
     * @throws IllegalArgumentException  if the initial capacity is less
     *             than zero, or if the load factor is nonpositive.
     */
    public IntHashMap3(int initialCapacity, float loadFactor) {
        super();
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        if (loadFactor <= 0) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }

        this.loadFactor = loadFactor;
        table = new Entry[initialCapacity];
        threshold = (int) (initialCapacity * loadFactor);
    }

    /**
     * <p>Returns the number of keys in this hashtable.</p>
     *
     * @return  the number of keys in this hashtable.
     */
    public int size() {
        return count;
    }

    /**
     * <p>Tests if this hashtable maps no keys to values.</p>
     *
     * @return  <code>true</code> if this hashtable maps no keys to values;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * <p>Tests if some key maps into the specified value in this hashtable.
     * This operation is more expensive than the <code>containsKey</code>
     * method.</p>
     *
     * <p>Note that this method is identical in functionality to containsValue,
     * (which is part of the Map interface in the collections framework).</p>
     *
     * @param      value   a value to search for.
     * @return     <code>true</code> if and only if some key maps to the
     *             <code>value</code> argument in this hashtable as
     *             determined by the <tt>equals</tt> method;
     *             <code>false</code> otherwise.
     * @throws  NullPointerException  if the value is <code>null</code>.
     * @see        #containsKey(int)
     * @see        #containsValue(Object)
     * @see        java.util.Map
     */
    public boolean contains(int value) {
       

        Entry tab[] = table;
        for (int i = tab.length; i-- > 0;) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (e.value==(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>Returns <code>true</code> if this HashMap maps one or more keys
     * to this value.</p>
     *
     * <p>Note that this method is identical in functionality to contains
     * (which predates the Map interface).</p>
     *
     * @param value value whose presence in this HashMap is to be tested.
     * @return boolean <code>true</code> if the value is contained
     * @see    java.util.Map
     * @since JDK1.2
     */
    public boolean containsValue(int value) {
        return contains(value);
    }

    /**
     * <p>Tests if the specified object is a key in this hashtable.</p>
     *
     * @param  key  possible key.
     * @return <code>true</code> if and only if the specified object is a
     *    key in this hashtable, as determined by the <tt>equals</tt>
     *    method; <code>false</code> otherwise.
     * @see #contains(Object)
     */
    public boolean containsKey(int key) {
        Entry tab[] = table;
        int hash = key;
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index]; e != null; e = e.next) {
            if (e.hash == hash) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Returns the value to which the specified key is mapped in this map.</p>
     *
     * @param   key   a key in the hashtable.
     * @return  the value to which the key is mapped in this hashtable;
     *          <code>null</code> if the key is not mapped to any value in
     *          this hashtable.
     * @see     #put(int, Object)
     */
    public int get(int key) {
        Entry tab[] = table;
        int hash = key;
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index]; e != null; e = e.next) {
            if (e.hash == hash) {
                return (Integer)e.value;
            }
        }
        return -1;
    }

    /**
     * <p>Increases the capacity of and internally reorganizes this
     * hashtable, in order to accommodate and access its entries more
     * efficiently.</p>
     *
     * <p>This method is called automatically when the number of keys
     * in the hashtable exceeds this hashtable's capacity and load
     * factor.</p>
     */
    protected void rehash() {
        int oldCapacity = table.length;
        Entry oldMap[] = table;

        int newCapacity = oldCapacity * 2 + 1;
        Entry newMap[] = new Entry[newCapacity];

        threshold = (int) (newCapacity * loadFactor);
        table = newMap;

        for (int i = oldCapacity; i-- > 0;) {
            for (Entry old = oldMap[i]; old != null;) {
                Entry e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newMap[index];
                newMap[index] = e;
            }
        }
//        System.gc();
    }

    /**
     * <p>Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. The key cannot be
     * <code>null</code>. </p>
     *
     * <p>The value can be retrieved by calling the <code>get</code> method
     * with a key that is equal to the original key.</p>
     *
     * @param key     the hashtable key.
     * @param value   the value.
     * @return the previous value of the specified key in this hashtable,
     *         or <code>null</code> if it did not have one.
     * @throws  NullPointerException  if the key is <code>null</code>.
     * @see     #get(int)
     */
    public int put(int hash, int value) {
        // Makes sure the key is not already in the hashtable.
//        Entry tab[] = table;
//        int hash = key;
        int index = (hash & 0x7FFFFFFF) % table.length;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.hash == hash) {
//                int old = e.value;
             return   e.value = value;
//                 old;
            }
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash();

//            tab = table;
            index = (hash & 0x7FFFFFFF) % table.length;
        }

        // Creates the new entry.
        Entry e = new Entry(hash, value, table[index]);
        table[index] = e;
        count++;
        return -1;
    }

    /**
     * <p>Removes the key (and its corresponding value) from this
     * hashtable.</p>
     *
     * <p>This method does nothing if the key is not present in the
     * hashtable.</p>
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the key had been mapped in this hashtable,
     *          or <code>null</code> if the key did not have a mapping.
     */
    public int remove(int hash) {
//        Entry tab[] = table;
        int index = (hash & 0x7FFFFFFF) % table.length;
        for (Entry e = table[index], prev = null; e != null; prev = e, e = e.next) {
            if (e.hash == hash) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    table[index] = e.next;
                }
                count--;
                int oldValue = e.value;
                e.value = -1;
                return oldValue;
            }
        }
        return -1;
    }

    /**
     * <p>Clears this hashtable so that it contains no keys.</p>
     */
    public synchronized void clear() {
        Entry tab[] = table;
        for (int index = tab.length; --index >= 0;) {
            tab[index] = null;
        }
        count = 0;
    }

    @Override
    public boolean equals(Object obj) {
        IntHashMap3 o=(IntHashMap3) obj;
        for(Entry e: table){
            for(Entry v=e;v.next!=null;v=e.next){
                if(o.get(v.hash)!=v.value)return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return count;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu.project;

import java.util.HashMap;

	/**
	 * 
	 * @author ldmtwo
	 */
public class IDGenerator<T> extends HashMap<T, Integer> {
	private static final long serialVersionUID = 98976418277654L;

	private int next = 0;

	public IDGenerator() {
		super(100000);
	}

	public IDGenerator(int size) {
		super(size);
	}

	// @Override
	// public int size() {
	// return next;
	// }

	public boolean add(T item) {
		return next == tryInsert(item);
	}

	/**
	 * If the item does not exist, then insert. Return the unique ID for that
	 * item.
	 * 
	 * @param item
	 * @return
	 */
	public int tryInsert(T item) {
		if (containsKey(item)) {
			return get(item);
		} else {
			int ID = next++;
			put(item, ID);
			return ID;
		}
	}

	public int test(T item) {
		if (containsKey(item)) {
			return get(item);
		} else {
			return next;
		}
	}
}

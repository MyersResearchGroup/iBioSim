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

public class Pair<LEFT, RIGHT> {
	LEFT left;
	RIGHT right;
	
	public Pair(LEFT l, RIGHT r) {
		this.left = l;
		this.right = r;
	}
	
	public LEFT getLeft() {
		return this.left;
	}
	
	public RIGHT getRight() {
		return this.right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		Pair other = (Pair) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} 
		else if (!left.equals(other.left))
			return false;
		
		if (right == null) {
			if (other.right != null)
				return false;
		} 
		else if (!right.equals(other.right))
			return false;
		
		return true;
	}
}

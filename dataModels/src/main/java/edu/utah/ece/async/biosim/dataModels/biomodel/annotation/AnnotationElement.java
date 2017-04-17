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
package edu.utah.ece.async.biosim.dataModels.biomodel.annotation;

import java.util.LinkedList;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class AnnotationElement {
	
	private String prefix;
	private String name;
	private String literal;
	private LinkedList<AnnotationAttribute> attributes = new LinkedList<AnnotationAttribute>();
	private LinkedList<AnnotationNamespace> namespaces = new LinkedList<AnnotationNamespace>();
	private LinkedList<AnnotationElement> children = new LinkedList<AnnotationElement>();
	
	public AnnotationElement(String name) {
		this.name = name;
	}
	
	public AnnotationElement(String prefix, String name) {
		this.prefix = prefix;
		this.name = name;
	}
	
	public AnnotationElement(String prefix, String name, String literal) {
		this.prefix = prefix;
		this.name = name;
		this.literal = literal;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getName() {
		return name;
	}
	
	public LinkedList<AnnotationAttribute> getAttributes() {
		return attributes;
	}
	
	public LinkedList<AnnotationNamespace> getNamespaces() {
		return namespaces;
	}
	
	public LinkedList<AnnotationElement> getChildren() {
		return children;
	}
	
	public AnnotationElement getChild(int index) {
		return children.get(index);
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addAttribute(AnnotationAttribute attribute) {
		attributes.add(attribute);
	}
	
	public void setAttributes(LinkedList<AnnotationAttribute> attributes) {
		this.attributes = attributes;
	}
	
	public void addNamespace(AnnotationNamespace namespace) {
		namespaces.add(namespace);
	}
	
	public void setNamespaces(LinkedList<AnnotationNamespace> namespaces) {
		this.namespaces = namespaces;
	}
	
	public void addChild(AnnotationElement child) {
		children.add(child);
	}
	
	public void addChild(int index, AnnotationElement child) {
		children.add(index, child);
	}
	
	public void setChildren(LinkedList<AnnotationElement> children) {
		this.children = children;
	}
	
	public String toXMLString() {
		String xml = "<";
		if (prefix != null)
			xml = xml + prefix + ":";
		xml += name;
		for (AnnotationNamespace namespace : namespaces) {
			xml = xml + " " + namespace.toXMLString();
		}
		for (AnnotationAttribute attribute : attributes)
			xml = xml + " " + attribute.toXMLString();
		if (literal != null || children.size() > 0) {
			xml += ">";
			if (children.size() > 0)
				for (AnnotationElement child : children)
					xml += child.toXMLString();
			else
				xml += literal;
			xml += "</";
			if (prefix != null)
				xml = xml + prefix + ":";
			xml = xml + name + ">";
		} else
			xml += "/>";
		
		return xml;
	}
	
}

package gcm2sbml.gui;

import gcm2sbml.visitor.GuiVisitor;
import gcm2sbml.visitor.VisitableGui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class PropertyList extends JList implements ChangeListener, NamedObject,
		Runnable, VisitableGui {

	public PropertyList(String name) {
		super();
		model = new DefaultListModel();
		setModel(model);
		this.name = name;
	}

	public void removeItem(String item) {
		model.removeElement(item);
	}

	public void addItem(String item) {
		if (model.size() == 0) {
			model.addElement(item);
		} else {
			model.ensureCapacity(model.getSize() + 1);
			for (int i = 0; i < model.size(); i++) {
				if (item.compareTo(model.get(i).toString()) < 0) {
					model.add(i, item);
					return;
				}
			}
			model.addElement(item);
		}
	}

	public void addAllItem(Collection<String> items) {
		for (String s : items) {
			addItem(s);
		}
	}

	public String getName() {
		return this.name;
	}

	public void run() {
	}
	
	public void accept(GuiVisitor visitor) {
		visitor.visitPropertyList(this);
	}
	
	public void updateState(Properties property) {
		if (property.containsKey("deletion")) {
			removeItem(property.getProperty("deletion"));
		} else if (property.containsKey("insertion")) {
			addItem(property.getProperty("insertion"));
		}
	}

	private String name = "";
	private DefaultListModel model = null;
}

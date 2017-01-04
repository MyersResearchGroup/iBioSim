package backend.biomodel.gui.schematic;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import frontend.main.Gui;


public class TreeChooser extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JTree tree;
	
	private TreeChooser(JFrame frame, Vector<Object> list, String message) {
		
		super(new BorderLayout());
		
		tree = new JTree(processHierarchy(list));
		JScrollPane listScroller = new JScrollPane(tree);
		
		this.add(listScroller, BorderLayout.NORTH);
		
		int choice = JOptionPane.showOptionDialog(frame, this, message, JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, null, JOptionPane.OK_OPTION);
		
		if (choice == JOptionPane.CANCEL_OPTION) {
			
			tree.removeAll();
			tree = null;
		}
	}
	
	public String getSelectedValue() {
		
		if (tree == null)
			return null;
		
		TreePath tp = tree.getSelectionPath();
		
		if (tp == null)
			return null;
		
		String out = "";
		
		for (Object part : tp.getPath()) {
			out += Gui.separator + ((DefaultMutableTreeNode)part).toString();
		}
		
		return out;
	}

	// modified from
	// http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/Swing-Tutorial-JTree.html
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode processHierarchy(Vector<Object> hierarchy) {
		DefaultMutableTreeNode node =
			new DefaultMutableTreeNode(hierarchy.get(0));
		DefaultMutableTreeNode child;
		for(int i=1; i<hierarchy.size(); i++) {
			Object nodeSpecifier = hierarchy.get(i);
			if (nodeSpecifier instanceof Vector<?>)  // Ie node with children
				child = processHierarchy((Vector<Object>)nodeSpecifier);
			else
				child = new DefaultMutableTreeNode(nodeSpecifier); // Ie Leaf
			node.add(child);
		}
		return(node);
	}

	/**
	 * given a list of strings, choose one. Throws EmptyTreeException if 
	 * the list is empty. Chooses the first item if there is only one.
	 * Otherwise, pops up a prompt to let the user choose one.
	 * 
	 * NOTE: biosim.frame() has a frame you can pass in.
	 * @param gcmFilename
	 * @param type
	 * @return
	 */
	public static class EmptyTreeException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public static String selectFromTree(JFrame frame, Vector<Object> list, String message) throws EmptyTreeException {
		
		if (list.size() == 0)
			throw new EmptyTreeException();
			
		TreeChooser pc = new TreeChooser(frame, list, message);
		String path = "";
		
		path = pc.getSelectedValue();
		
		return path;
	}
	
	
	
}

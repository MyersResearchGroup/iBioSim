package biomodelsim.core.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * Display a file system in a JTree view
 */
public class FileTree extends JPanel implements MouseListener {

	private static final long serialVersionUID = -6799125543270861304L;

	private String fileLocation; // location of selected file

	private DefaultMutableTreeNode root; // root node

	private File dir; // root directory

	private JTree tree; // JTree

	/**
	 * Construct a FileTree
	 */
	public FileTree(final File dir, BioModelSim biomodelsim) {
		setLayout(new BorderLayout());

		this.dir = dir;
		// Make a tree list with all the nodes, and make it a JTree
		if (dir != null) {
			tree = new JTree(addNodes(null, dir));
			// Add a listener
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath()
							.getLastPathComponent();
					fileLocation = "";
					while (node != null) {
						if (node.getParent() != null) {
							fileLocation = File.separator + node + fileLocation;
						}
						node = (DefaultMutableTreeNode) node.getParent();
					}
					fileLocation = dir.getAbsolutePath() + fileLocation;
				}
			});
			tree.addMouseListener(this);
			tree.addMouseListener(biomodelsim);
		} else {
			tree = new JTree(new DefaultMutableTreeNode());
		}

		// Lastly, put the JTree into a JScrollPane.
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		add(BorderLayout.CENTER, scrollpane);
	}

	/**
	 * Add nodes from under "dir" into curTop. Highly recursive.
	 */
	DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
		String curPath = dir.getPath();
		DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(dir.getName());
		if (curTop != null) { // should only be null at root
			if (curTop.getParent() == null) {
				curTop.add(curDir);
			}
		} else {
			root = curDir;
		}
		ArrayList<String> ol = new ArrayList<String>();
		String[] tmp = dir.list();
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i].charAt(0) != '.') {
				ol.add(tmp[i]);
			}
		}
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		ArrayList<String> files = new ArrayList<String>();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = (String) ol.get(i);
			String newPath;
			if (curPath.equals("."))
				newPath = thisObject;
			else
				newPath = curPath + File.separator + thisObject;
			if ((f = new File(newPath)).isDirectory() && !f.getName().equals("CVS"))
				addNodes(curDir, f);
			else if (!f.getName().equals("CVS"))
				files.add(thisObject);
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++) {
			if (curDir.getParent() == null) {
				curDir.add(new DefaultMutableTreeNode(files.get(fnum)));
			} else if (!(curDir.getParent().toString().equals(root.toString()))) {
				curDir.add(new DefaultMutableTreeNode(files.get(fnum)));
			}
		}
		return curDir;
	}

	public String getFile() {
		return fileLocation;
	}

	public void fixTree() {
		fixTree(null, root, dir, false);
		tree.updateUI();
	}

	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	private void fixTree(DefaultMutableTreeNode parent, DefaultMutableTreeNode current, File dir,
			boolean add) {
		String curPath = dir.getPath();
		if (add) {
			if (parent == null || parent.getParent() == null) {
				int insert = 0;
				for (int i = 0; i < parent.getChildCount(); i++) {
					if (parent.getChildAt(i).toString().compareToIgnoreCase(current.toString()) > 0) {
						break;
					}
					if (parent.getChildAt(i).toString().contains(".sbml")
							|| parent.getChildAt(i).toString().contains(".xml")
							|| parent.getChildAt(i).toString().contains(".xhtml")
							|| parent.getChildAt(i).toString().contains(".dot")) {
						break;
					}
					insert++;
				}
				parent.insert(current, insert);
			}
		}
		ArrayList<String> ol = new ArrayList<String>();
		String[] tmp = dir.list();
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i].charAt(0) != '.') {
				ol.add(tmp[i]);
			}
		}
		ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
		for (int i = 0; i < current.getChildCount(); i++) {
			nodes.add((DefaultMutableTreeNode) current.getChildAt(i));
		}
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		ArrayList<String> files = new ArrayList<String>();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < current.getChildCount(); i++) {
			boolean remove = true;
			for (int j = 0; j < ol.size(); j++) {
				String cur = "" + current.getChildAt(i);
				if (cur.equals(ol.get(j))) {
					remove = false;
				}
			}
			if (remove) {
				current.remove(i);
				i--;
			}
		}
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = (String) ol.get(i);
			String newPath;
			if (curPath.equals("."))
				newPath = thisObject;
			else
				newPath = curPath + File.separator + thisObject;
			if ((f = new File(newPath)).isDirectory() && !f.getName().equals("CVS")) {
				String get = "";
				boolean doAdd = true;
				int getChild = 0;
				for (int j = 0; j < current.getChildCount(); j++) {
					get = "" + current.getChildAt(j);
					if (get.equals(f.getName())) {
						doAdd = false;
						getChild = j;
					}
				}
				if (doAdd) {
					fixTree(current, new DefaultMutableTreeNode(f.getName()), f, doAdd);
				} else {
					fixTree(current, (DefaultMutableTreeNode) current.getChildAt(getChild), f,
							doAdd);
				}
			} else if (!f.getName().equals("CVS"))
				files.add(thisObject);
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++) {
			String get = "";
			boolean doAdd = true;
			for (int j = 0; j < nodes.size(); j++) {
				get = "" + nodes.get(j);
				if (get.equals(files.get(fnum))) {
					doAdd = false;
				}
			}
			if (doAdd) {
				if (parent == null) {
					int insert = 0;
					for (int i = 0; i < current.getChildCount(); i++) {
						if (!current.getChildAt(i).toString().contains(".sbml")
								&& !current.getChildAt(i).toString().contains(".xml")
								&& !current.getChildAt(i).toString().contains(".xhtml")
								&& !current.getChildAt(i).toString().contains(".dot")) {
						} else if (current.getChildAt(i).toString().compareToIgnoreCase(
								files.get(fnum).toString()) > 0) {
							break;
						}
						insert++;
					}
					current.insert(new DefaultMutableTreeNode(files.get(fnum)), insert);
				} else if (!(parent.toString().equals(root.toString()))) {
					int insert = 0;
					for (int i = 0; i < current.getChildCount(); i++) {
						if (!current.getChildAt(i).toString().contains(".sbml")
								&& !current.getChildAt(i).toString().contains(".xml")
								&& !current.getChildAt(i).toString().contains(".xhtml")
								&& !current.getChildAt(i).toString().contains(".dot")) {
						} else if (current.getChildAt(i).toString().compareToIgnoreCase(
								files.get(fnum).toString()) > 0) {
							break;
						}
						insert++;
					}
					current.insert(new DefaultMutableTreeNode(files.get(fnum)), insert);
				}
			}
		}
	}

	public Dimension getMinimumSize() {
		return new Dimension(200, 400);
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 400);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (!e.isPopupTrigger())
			return;

		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		if (selRow < 0)
			return;

		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		tree.setSelectionPath(selPath);
	}

	public void mouseReleased(MouseEvent e) {
		if (!e.isPopupTrigger())
			return;

		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		if (selRow < 0)
			return;

		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		tree.setSelectionPath(selPath);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

}
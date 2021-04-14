/*
 * David Bismut, davidou@mageos.com
 * Intern, SETLabs, Infosys Technologies Ltd. May 2004 - Jul 2004
 * Ecole des Mines de Nantes, France
 */

/*
 * 
 * Extended from
 * @(#)BasicTabbedPaneUI.java	1.126 03/01/23
 *
 * Copyright 2003, 2017 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package edu.utah.ece.async.ibiosim.gui.util.tabs;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

//import com.sun.java.swing.plaf.windows.WindowsIconFactory;

import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.ResourceManager;

/**
 * UI for <code>CloseAndMaxTabbedPane</code>.
 * 
 * @author Amy Fowler
 * @author Philip Milne
 * @author Steve Wilson
 * @author Tom Santos
 * @author Dave Moore
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class CloseTabPaneUI extends BasicTabbedPaneUI {

	// Instance variables initialized at installation

	private ContainerListener containerListener;

	private Vector<View> htmlViews;

	private Hashtable<Integer, Integer> mnemonicToIndexMap;

	/**
	 * InputMap used for mnemonics. Only non-null if the JTabbedPane has
	 * mnemonics associated with it. Lazily created in initMnemonics.
	 */
	private InputMap mnemonicInputMap;

	// For use when tabLayoutPolicy = SCROLL_TAB_LAYOUT
	protected ScrollableTabSupport tabScroller;

	private int tabCount;

	protected MyMouseMotionListener motionListener;

	// UI creation

	private static final int INACTIVE = 0;

	private static final int OVER = 1;

	private static final int PRESSED = 2;

	protected static final int BUTTONSIZE = 15;

	protected static final int WIDTHDELTA = 5;

	private static final Border PRESSEDBORDER = new SoftBevelBorder(BevelBorder.LOWERED);

	private static final Border OVERBORDER = new SoftBevelBorder(BevelBorder.RAISED);

	private BufferedImage closeImgB;

	private BufferedImage closeImgI;

	private JButton closeB;

	private int overTabIndex = -1;

	private int closeIndexStatus = INACTIVE;

	private int maxIndexStatus = INACTIVE;

	private boolean mousePressed = false;

	private boolean isCloseButtonEnabled = true;

	protected JPopupMenu actionPopupMenu;

	protected JMenuItem closeItem;

	private Gui biosim;

	public CloseTabPaneUI(Gui biosim) {

		super();

		this.biosim = biosim;

		closeImgB = new BufferedImage(BUTTONSIZE, BUTTONSIZE, BufferedImage.TYPE_4BYTE_ABGR);

		closeImgI = new BufferedImage(BUTTONSIZE, BUTTONSIZE, BufferedImage.TYPE_4BYTE_ABGR);
		
		closeB = new JButton();
		closeB.setIcon(ResourceManager.getImageIcon("close.gif"));
		closeB.setSize(BUTTONSIZE, BUTTONSIZE);

		//WindowsIconFactory.createFrameCloseIcon().paintIcon(closeB, closeImgI.createGraphics(), 0, 0);

		actionPopupMenu = new JPopupMenu();

		closeItem = new JMenuItem("Close");

		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((CloseAndMaxTabbedPane) tabPane).fireCloseTabEvent(null, tabPane.getSelectedIndex());

			}
		});

		setPopupMenu();
	}

	public void addMouseListener(MouseListener l) {
		tabScroller.tabPanel.addMouseListener(l);
	}

	protected boolean isOneActionButtonEnabled() {
		return isCloseButtonEnabled;
	}

	public boolean isCloseEnabled() {
		return isCloseButtonEnabled;
	}

	public void setCloseIcon(boolean b) {
		isCloseButtonEnabled = b;
		setPopupMenu();
	}

	private void setPopupMenu() {
		actionPopupMenu.removeAll();
		if (isCloseButtonEnabled)
			actionPopupMenu.add(closeItem);
	}

	@Override
	protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
		int delta = 2;
		if (!isOneActionButtonEnabled())
			delta += 6;
		else {
			if (isCloseButtonEnabled)
				delta += BUTTONSIZE + WIDTHDELTA;
		}

		return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + delta;
	}

	@Override
	protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {

		return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 5;
	}

	@Override
	protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect,
			Rectangle textRect, boolean isSelected) {
		textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

		View v = getTextViewForTab(tabIndex);
		if (v != null) {
			tabPane.putClientProperty("html", v);
		}

		SwingUtilities.layoutCompoundLabel(tabPane, metrics, title, icon, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER,
				SwingConstants.CENTER, tabRect, iconRect, textRect, textIconGap);

		tabPane.putClientProperty("html", null);

		iconRect.x = tabRect.x + 8;
		textRect.x = iconRect.x + iconRect.width + textIconGap;
	}

	@Override
	protected MouseListener createMouseListener() {
		return new MyMouseHandler();
	}

	protected ScrollableTabButton createScrollableTabButton(int direction) {
		return new ScrollableTabButton(direction);
	}

	protected static Rectangle newCloseRect(Rectangle rect) {
		int dx = rect.x + rect.width;
		int dy = (rect.y + rect.height) / 2 - 6;
		return new Rectangle(dx - BUTTONSIZE - WIDTHDELTA, dy, BUTTONSIZE, BUTTONSIZE);
	}

	protected Rectangle newMaxRect(Rectangle rect) {
		int dx = rect.x + rect.width;
		int dy = (rect.y + rect.height) / 2 - 6;
		if (isCloseButtonEnabled)
			dx -= BUTTONSIZE;

		return new Rectangle(dx - BUTTONSIZE - WIDTHDELTA, dy, BUTTONSIZE, BUTTONSIZE);
	}

	protected void updateOverTab(int x, int y) {
		if (overTabIndex != (overTabIndex = getTabAtLocation(x, y)))
			tabScroller.tabPanel.repaint();

	}

	protected void updateCloseIcon(int x, int y) {

		if (overTabIndex != -1) {
			int newCloseIndexStatus = INACTIVE;

			Rectangle closeRect = newCloseRect(rects[overTabIndex]);
			if (closeRect.contains(x, y))
				newCloseIndexStatus = mousePressed ? PRESSED : OVER;

			if (closeIndexStatus != (closeIndexStatus = newCloseIndexStatus))
				tabScroller.tabPanel.repaint();
		}
	}

	protected void updateMaxIcon(int x, int y) {
		if (overTabIndex != -1) {
			int newMaxIndexStatus = INACTIVE;

			Rectangle maxRect = newMaxRect(rects[overTabIndex]);

			if (maxRect.contains(x, y))
				newMaxIndexStatus = mousePressed ? PRESSED : OVER;

			if (maxIndexStatus != (maxIndexStatus = newMaxIndexStatus))
				tabScroller.tabPanel.repaint();
		}
	}

	private void setTabIcons(int x, int y) {
		// if the mouse isPressed
		if (!mousePressed) {
			updateOverTab(x, y);
		}

		if (isCloseButtonEnabled)
			updateCloseIcon(x, y);
	}

	public static ComponentUI createUI(Gui biosim) {
		return new CloseTabPaneUI(biosim);
	}

	/**
	 * Invoked by <code>installUI</code> to create a layout manager object to
	 * manage the <code>JTabbedPane</code>.
	 * 
	 * @return a layout manager object
	 * 
	 * @see TabbedPaneLayout
	 * @see javax.swing.JTabbedPane#getTabLayoutPolicy
	 */
	@Override
	protected LayoutManager createLayoutManager() {

		return new TabbedPaneScrollLayout();

	}

	/*
	 * In an attempt to preserve backward compatibility for programs which have
	 * extended BasicTabbedPaneUI to do their own layout, the UI uses the
	 * installed layoutManager (and not tabLayoutPolicy) to determine if
	 * scrollTabLayout is enabled.
	 */

	/**
	 * Creates and installs any required subcomponents for the JTabbedPane.
	 * Invoked by installUI.
	 * 
	 * @since 1.4
	 */
	@Override
	protected void installComponents() {

		if (tabScroller == null) {
			tabScroller = new ScrollableTabSupport();
			tabPane.add(tabScroller.viewport);
			tabPane.add(tabScroller.scrollForwardButton);
			tabPane.add(tabScroller.scrollBackwardButton);
		}

	}

	/**
	 * Removes any installed subcomponents from the JTabbedPane. Invoked by
	 * uninstallUI.
	 * 
	 * @since 1.4
	 */
	@Override
	protected void uninstallComponents() {

		tabPane.remove(tabScroller.viewport);
		tabPane.remove(tabScroller.scrollForwardButton);
		tabPane.remove(tabScroller.scrollBackwardButton);
		tabScroller = null;

	}

	@Override
	protected void installListeners() {
		if ((propertyChangeListener = createPropertyChangeListener()) != null) {
			tabPane.addPropertyChangeListener(propertyChangeListener);
		}
		if ((tabChangeListener = createChangeListener()) != null) {
			tabPane.addChangeListener(tabChangeListener);
		}
		if ((mouseListener = createMouseListener()) != null) {
			tabScroller.tabPanel.addMouseListener(mouseListener);
		}

		if ((focusListener = createFocusListener()) != null) {
			tabPane.addFocusListener(focusListener);
		}

		// PENDING(api) : See comment for ContainerHandler
		if ((containerListener = new ContainerHandler()) != null) {
			tabPane.addContainerListener(containerListener);
			if (tabPane.getTabCount() > 0) {
				htmlViews = createHTMLVector();
			}
		}

		if ((motionListener = new MyMouseMotionListener()) != null) {
			tabScroller.tabPanel.addMouseMotionListener(motionListener);
		}

	}

	@Override
	protected void uninstallListeners() {
		if (mouseListener != null) {
			tabScroller.tabPanel.removeMouseListener(mouseListener);
			mouseListener = null;
		}

		if (motionListener != null) {
			tabScroller.tabPanel.removeMouseMotionListener(motionListener);
			motionListener = null;
		}

		if (focusListener != null) {
			tabPane.removeFocusListener(focusListener);
			focusListener = null;
		}

		// PENDING(api): See comment for ContainerHandler
		if (containerListener != null) {
			tabPane.removeContainerListener(containerListener);
			containerListener = null;
			if (htmlViews != null) {
				htmlViews.removeAllElements();
				htmlViews = null;
			}
		}
		if (tabChangeListener != null) {
			tabPane.removeChangeListener(tabChangeListener);
			tabChangeListener = null;
		}
		if (propertyChangeListener != null) {
			tabPane.removePropertyChangeListener(propertyChangeListener);
			propertyChangeListener = null;
		}

	}

	@Override
	protected ChangeListener createChangeListener() {
		return new TabSelectionHandler();
	}

	@Override
	protected void installKeyboardActions() {
		InputMap km = getMyInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, km);
		km = getMyInputMap(JComponent.WHEN_FOCUSED);
		SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_FOCUSED, km);

		ActionMap am = createMyActionMap();

		SwingUtilities.replaceUIActionMap(tabPane, am);

		tabScroller.scrollForwardButton.setAction(am.get("scrollTabsForwardAction"));
		tabScroller.scrollBackwardButton.setAction(am.get("scrollTabsBackwardAction"));

	}

	static InputMap getMyInputMap(int condition) {
		if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
			return (InputMap) UIManager.get("TabbedPane.ancestorInputMap");
		}
		else if (condition == JComponent.WHEN_FOCUSED) {
			return (InputMap) UIManager.get("TabbedPane.focusInputMap");
		}
		return null;
	}

	static ActionMap createMyActionMap() {
		ActionMap map = new ActionMapUIResource();
		map.put("navigateNext", new NextAction());
		map.put("navigatePrevious", new PreviousAction());
		map.put("navigateRight", new RightAction());
		map.put("navigateLeft", new LeftAction());
		map.put("navigateUp", new UpAction());
		map.put("navigateDown", new DownAction());
		map.put("navigatePageUp", new PageUpAction());
		map.put("navigatePageDown", new PageDownAction());
		map.put("requestFocus", new RequestFocusAction());
		map.put("requestFocusForVisibleComponent", new RequestFocusForVisibleAction());
		map.put("setSelectedIndex", new SetSelectedIndexAction());
		map.put("scrollTabsForwardAction", new ScrollTabsForwardAction());
		map.put("scrollTabsBackwardAction", new ScrollTabsBackwardAction());
		return map;
	}

	@Override
	protected void uninstallKeyboardActions() {
		SwingUtilities.replaceUIActionMap(tabPane, null);
		SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
		SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_FOCUSED, null);
	}

	/**
	 * Reloads the mnemonics. This should be invoked when a memonic changes,
	 * when the title of a mnemonic changes, or when tabs are added/removed.
	 */
	private void updateMnemonics() {
		resetMnemonics();
		for (int counter = tabPane.getTabCount() - 1; counter >= 0; counter--) {
			int mnemonic = tabPane.getMnemonicAt(counter);

			if (mnemonic > 0) {
				addMnemonic(counter, mnemonic);
			}
		}
	}

	/**
	 * Resets the mnemonics bindings to an empty state.
	 */
	private void resetMnemonics() {
		if (mnemonicToIndexMap != null) {
			mnemonicToIndexMap.clear();
			mnemonicInputMap.clear();
		}
	}

	/**
	 * Adds the specified mnemonic at the specified index.
	 */
	private void addMnemonic(int index, int mnemonic) {
		if (mnemonicToIndexMap == null) {
			initMnemonics();
		}
		mnemonicInputMap.put(KeyStroke.getKeyStroke(mnemonic, Event.ALT_MASK), "setSelectedIndex");
		mnemonicToIndexMap.put(new Integer(mnemonic), new Integer(index));
	}

	/**
	 * Installs the state needed for mnemonics.
	 */
	private void initMnemonics() {
		mnemonicToIndexMap = new Hashtable<Integer, Integer>();
		mnemonicInputMap = new InputMapUIResource();
		mnemonicInputMap.setParent(SwingUtilities.getUIInputMap(tabPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
		SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, mnemonicInputMap);
	}

	// UI Rendering

	@Override
	public void paint(Graphics g, JComponent c) {
		int tc = tabPane.getTabCount();

		if (tabCount != tc) {
			tabCount = tc;
			updateMnemonics();
		}

		int selectedIndex = tabPane.getSelectedIndex();
		int tabPlacement = tabPane.getTabPlacement();

		ensureCurrentLayout();

		// Paint content border
		paintContentBorder(g, tabPlacement, selectedIndex);

	}

	@Override
	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
		Rectangle tabRect = rects[tabIndex];
		int selectedIndex = tabPane.getSelectedIndex();
		boolean isSelected = selectedIndex == tabIndex;
		boolean isOver = overTabIndex == tabIndex;
		Graphics2D g2 = null;
		Shape save = null;
		boolean cropShape = false;
		int cropx = 0;
		int cropy = 0;

		if (g instanceof Graphics2D) {
			g2 = (Graphics2D) g;

			// Render visual for cropped tab edge...
			Rectangle viewRect = tabScroller.viewport.getViewRect();
			int cropline;

			cropline = viewRect.x + viewRect.width;
			if ((tabRect.x < cropline) && (tabRect.x + tabRect.width > cropline)) {

				cropx = cropline - 1;
				cropy = tabRect.y;
				cropShape = true;
			}

			if (cropShape) {
				save = g2.getClip();
				g2.clipRect(tabRect.x, tabRect.y, tabRect.width, tabRect.height);

			}
		}

		paintTabBackground(g, tabPlacement, tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, isSelected);

		paintTabBorder(g, tabPlacement, tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, isSelected);

		String title = tabPane.getTitleAt(tabIndex);
		Font font = tabPane.getFont();
		FontMetrics metrics = g.getFontMetrics(font);
		Icon icon = getIconForTab(tabIndex);

		layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected);

		paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);

		paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);

		paintFocusIndicator(g, tabPlacement, rects, tabIndex, iconRect, textRect, isSelected);

		if (cropShape) {
			paintCroppedTabEdge(g, tabIndex, cropx, cropy);
			if (g2 != null)
				g2.setClip(save);

		}
		else { // if (isOver || isSelected) {

			int dx = tabRect.x + tabRect.width - BUTTONSIZE - WIDTHDELTA;
			int dy = (tabRect.y + tabRect.height) / 2 - 6;

			if (isCloseButtonEnabled)
				paintCloseIcon(g2, dx, dy, isOver);
		}

	}

	protected void paintCloseIcon(Graphics g, int dx, int dy, boolean isOver) {
		paintActionButton(g, dx, dy, closeIndexStatus, isOver, closeB, closeImgB);
		// g.drawImage(closeImgI, dx, dy + 1, null);
	}

	protected void paintActionButton(Graphics g, int dx, int dy, int status, boolean isOver, JButton button, BufferedImage image) {

		button.setBorder(null);

		if (isOver) {
			switch (status) {
			case OVER:
				button.setBorder(OVERBORDER);
				break;
			case PRESSED:
				button.setBorder(PRESSEDBORDER);
				break;
			}
		}

		button.setBackground(tabScroller.tabPanel.getBackground());
		button.paint(image.getGraphics());
		g.drawImage(image, dx, dy, null);
	}

	/*
	 * This method will create and return a polygon shape for the given tab
	 * rectangle which has been cropped at the specified cropline with a torn
	 * edge visual. e.g. A "File" tab which has cropped been cropped just after
	 * the "i": ------------- | ..... | | . | | ... . | | . . | | . . | | . . |
	 * --------------
	 * 
	 * The x, y arrays below define the pattern used to create a "torn" edge
	 * segment which is repeated to fill the edge of the tab. For tabs placed on
	 * TOP and BOTTOM, this righthand torn edge is created by line segments
	 * which are defined by coordinates obtained by subtracting xCropLen[i] from
	 * (tab.x + tab.width) and adding yCroplen[i] to (tab.y). For tabs placed on
	 * LEFT or RIGHT, the bottom torn edge is created by subtracting xCropLen[i]
	 * from (tab.y + tab.height) and adding yCropLen[i] to (tab.x).
	 */

	private void paintCroppedTabEdge(Graphics g, int tabIndex, int x, int y) {

		g.setColor(shadow);
		g.drawLine(x, y, x, y + rects[tabIndex].height);

	}

	private void ensureCurrentLayout() {
		if (!tabPane.isValid()) {
			tabPane.validate();
		}
		/*
		 * If tabPane doesn't have a peer yet, the validate() call will silently
		 * fail. We handle that by forcing a layout if tabPane is still invalid.
		 * See bug 4237677.
		 */
		if (!tabPane.isValid()) {
			TabbedPaneLayout layout = (TabbedPaneLayout) tabPane.getLayout();
			layout.calculateLayoutInfo();
		}
	}

	/**
	 * Returns the bounds of the specified tab in the coordinate space of the
	 * JTabbedPane component. This is required because the tab rects are by
	 * default defined in the coordinate space of the component where they are
	 * rendered, which could be the JTabbedPane (for WRAP_TAB_LAYOUT) or a
	 * ScrollableTabPanel (SCROLL_TAB_LAYOUT). This method should be used
	 * whenever the tab rectangle must be relative to the JTabbedPane itself and
	 * the result should be placed in a designated Rectangle object (rather than
	 * instantiating and returning a new Rectangle each time). The tab index
	 * parameter must be a valid tabbed pane tab index (0 to tab count - 1,
	 * inclusive). The destination rectangle parameter must be a valid
	 * <code>Rectangle</code> instance. The handling of invalid parameters is
	 * unspecified.
	 * 
	 * @param tabIndex
	 *            the index of the tab
	 * @param dest
	 *            the rectangle where the result should be placed
	 * @return the resulting rectangle
	 * 
	 * @since 1.4
	 */

	@Override
	protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
		dest.width = rects[tabIndex].width;
		dest.height = rects[tabIndex].height;

		Point vpp = tabScroller.viewport.getLocation();
		Point viewp = tabScroller.viewport.getViewPosition();
		dest.x = rects[tabIndex].x + vpp.x - viewp.x;
		dest.y = rects[tabIndex].y + vpp.y - viewp.y;

		return dest;
	}

	private int getTabAtLocation(int x, int y) {
		ensureCurrentLayout();

		int tabCount = tabPane.getTabCount();
		for (int i = 0; i < tabCount; i++) {
			if (rects[i].contains(x, y)) {
				return i;
			}
		}
		return -1;
	}

	public int getOverTabIndex() {
		return overTabIndex;
	}

	/**
	 * Returns the index of the tab closest to the passed in location, note that
	 * the returned tab may not contain the location x,y.
	 */
	private int getClosestTab(int x, int y) {
		int min = 0;
		int tabCount = Math.min(rects.length, tabPane.getTabCount());
		int max = tabCount;
		int tabPlacement = tabPane.getTabPlacement();
		boolean useX = (tabPlacement == TOP || tabPlacement == BOTTOM);
		int want = (useX) ? x : y;

		while (min != max) {
			int current = (max + min) / 2;
			int minLoc;
			int maxLoc;

			if (useX) {
				minLoc = rects[current].x;
				maxLoc = minLoc + rects[current].width;
			}
			else {
				minLoc = rects[current].y;
				maxLoc = minLoc + rects[current].height;
			}
			if (want < minLoc) {
				max = current;
				if (min == max) {
					return Math.max(0, current - 1);
				}
			}
			else if (want >= maxLoc) {
				min = current;
				if (max - min <= 1) {
					return Math.max(current + 1, tabCount - 1);
				}
			}
			else {
				return current;
			}
		}
		return min;
	}

	// BasicTabbedPaneUI methods

	// Tab Navigation methods

	// REMIND(aim,7/29/98): This method should be made
	// protected in the next release where
	// API changes are allowed
	//
	boolean requestMyFocusForVisibleComponent() {
		Component visibleComponent = getVisibleComponent();
		if (visibleComponent.isFocusable()) {
			visibleComponent.requestFocus();
			return true;
		}
		else if (visibleComponent instanceof JComponent) {
			if (((JComponent) visibleComponent).requestFocusInWindow()) {
				return true;
			}
		}
		return false;
	}

	private static class RightAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1372089458264627655L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			ui.navigateSelectedTab(EAST);
		}
	}

	private static class LeftAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2840127075487509680L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			ui.navigateSelectedTab(WEST);
		}
	}

	private static class UpAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5022434551873267653L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			ui.navigateSelectedTab(NORTH);
		}
	}

	private static class DownAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3095536365104725070L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			ui.navigateSelectedTab(SOUTH);
		}
	}

	private static class NextAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5492119175649068664L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			ui.navigateSelectedTab(NEXT);
		}
	}

	private static class PreviousAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5721846316112045696L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			ui.navigateSelectedTab(PREVIOUS);
		}
	}

	private static class PageUpAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3901523699837556597L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			int tabPlacement = pane.getTabPlacement();
			if (tabPlacement == TOP || tabPlacement == BOTTOM) {
				ui.navigateSelectedTab(WEST);
			}
			else {
				ui.navigateSelectedTab(NORTH);
			}
		}
	}

	private static class PageDownAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8234056647814092293L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			int tabPlacement = pane.getTabPlacement();
			if (tabPlacement == TOP || tabPlacement == BOTTOM) {
				ui.navigateSelectedTab(EAST);
			}
			else {
				ui.navigateSelectedTab(SOUTH);
			}
		}
	}

	private static class RequestFocusAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5155228427948248055L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			pane.requestFocus();
		}
	}

	private static class RequestFocusForVisibleAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7735773325665374956L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
			ui.requestMyFocusForVisibleComponent();
		}
	}

	/**
	 * Selects a tab in the JTabbedPane based on the String of the action
	 * command. The tab selected is based on the first tab that has a mnemonic
	 * matching the first character of the action command.
	 */
	private static class SetSelectedIndexAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8262059252993642009L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = (JTabbedPane) e.getSource();

			if (pane != null && (pane.getUI() instanceof CloseTabPaneUI)) {
				CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();
				String command = e.getActionCommand();

				if (command != null && command.length() > 0) {
					int mnemonic = e.getActionCommand().charAt(0);
					if (mnemonic >= 'a' && mnemonic <= 'z') {
						mnemonic -= ('a' - 'A');
					}
					Integer index = ui.mnemonicToIndexMap.get(new Integer(mnemonic));
					if (index != null && pane.isEnabledAt(index.intValue())) {
						pane.setSelectedIndex(index.intValue());
					}
				}
			}
		}
	}

	private static class ScrollTabsForwardAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4722476981006125635L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = null;
			Object src = e.getSource();
			if (src instanceof JTabbedPane) {
				pane = (JTabbedPane) src;
			}
			else if (src instanceof ScrollableTabButton) {
				pane = (JTabbedPane) ((ScrollableTabButton) src).getParent();
			}
			else {
				return; // shouldn't happen
			}
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();

			ui.tabScroller.scrollForward(pane.getTabPlacement());

		}
	}

	private static class ScrollTabsBackwardAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5913237628993582792L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTabbedPane pane = null;
			Object src = e.getSource();
			if (src instanceof JTabbedPane) {
				pane = (JTabbedPane) src;
			}
			else if (src instanceof ScrollableTabButton) {
				pane = (JTabbedPane) ((ScrollableTabButton) src).getParent();
			}
			else {
				return; // shouldn't happen
			}
			CloseTabPaneUI ui = (CloseTabPaneUI) pane.getUI();

			ui.tabScroller.scrollBackward();

		}
	}

	/**
	 * This inner class is marked &quot;public&quot; due to a compiler bug. This
	 * class should be treated as a &quot;protected&quot; inner class.
	 * Instantiate it only within subclasses of BasicTabbedPaneUI.
	 */

	private class TabbedPaneScrollLayout extends TabbedPaneLayout {

		@Override
		protected int preferredTabAreaHeight(int tabPlacement, int width) {
			return calculateMaxTabHeight(tabPlacement);
		}

		@Override
		protected int preferredTabAreaWidth(int tabPlacement, int height) {
			return calculateMaxTabWidth(tabPlacement);
		}

		@Override
		public void layoutContainer(Container parent) {
			int tabPlacement = tabPane.getTabPlacement();
			int tabCount = tabPane.getTabCount();
			Insets insets = tabPane.getInsets();
			int selectedIndex = tabPane.getSelectedIndex();
			Component visibleComponent = getVisibleComponent();

			calculateLayoutInfo();

			if (selectedIndex < 0) {
				if (visibleComponent != null) {
					// The last tab was removed, so remove the component
					setVisibleComponent(null);
				}
			}
			else {
				Component selectedComponent = tabPane.getComponentAt(selectedIndex);
				boolean shouldChangeFocus = false;

				// In order to allow programs to use a single component
				// as the display for multiple tabs, we will not change
				// the visible compnent if the currently selected tab
				// has a null component. This is a bit dicey, as we don't
				// explicitly state we support this in the spec, but since
				// programs are now depending on this, we're making it work.
				//
				if (selectedComponent != null) {
					if (selectedComponent != visibleComponent && visibleComponent != null) {
						if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() != null) {
							shouldChangeFocus = true;
						}
					}
					setVisibleComponent(selectedComponent);
				}
				int tx, ty, tw, th; // tab area bounds
				int cx, cy, cw, ch; // content area bounds
				Insets contentInsets = getContentBorderInsets(tabPlacement);
				Rectangle bounds = tabPane.getBounds();
				int numChildren = tabPane.getComponentCount();

				if (numChildren > 0) {

					// calculate tab area bounds
					tw = bounds.width - insets.left - insets.right;
					th = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
					tx = insets.left;
					ty = insets.top;

					// calculate content area bounds
					cx = tx + contentInsets.left;
					cy = ty + th + contentInsets.top;
					cw = bounds.width - insets.left - insets.right - contentInsets.left - contentInsets.right;
					ch = bounds.height - insets.top - insets.bottom - th - contentInsets.top - contentInsets.bottom;

					for (int i = 0; i < numChildren; i++) {
						Component child = tabPane.getComponent(i);

						if (child instanceof ScrollableTabViewport) {
							JViewport viewport = (JViewport) child;
							Rectangle viewRect = viewport.getViewRect();
							int vw = tw;
							int vh = th;

							int totalTabWidth = rects[tabCount - 1].x + rects[tabCount - 1].width;
							if (totalTabWidth > tw) {
								// Need to allow space for scrollbuttons
								vw = Math.max(tw - 36, 36);
								if (totalTabWidth - viewRect.x <= vw) {
									// Scrolled to the end, so ensure the
									// viewport size is
									// such that the scroll offset aligns with a
									// tab
									vw = totalTabWidth - viewRect.x;
								}
							}

							child.setBounds(tx, ty, vw, vh);

						}
						else if (child instanceof ScrollableTabButton) {
							ScrollableTabButton scrollbutton = (ScrollableTabButton) child;
							Dimension bsize = scrollbutton.getPreferredSize();
							int bx = 0;
							int by = 0;
							int bw = bsize.width;
							int bh = bsize.height;
							boolean visible = false;

							int totalTabWidth = rects[tabCount - 1].x + rects[tabCount - 1].width;

							if (totalTabWidth > tw) {
								int dir = scrollbutton.scrollsForward() ? EAST : WEST;
								scrollbutton.setDirection(dir);
								visible = true;
								bx = dir == EAST ? bounds.width - insets.left - bsize.width : bounds.width - insets.left - 2 * bsize.width;
								by = (tabPlacement == TOP ? ty + th - bsize.height : ty);
							}

							child.setVisible(visible);
							if (visible) {
								child.setBounds(bx, by, bw, bh);
							}

						}
						else {
							// All content children...
							child.setBounds(cx, cy, cw, ch);
						}
					}
					if (shouldChangeFocus) {
						if (!requestMyFocusForVisibleComponent()) {
							tabPane.requestFocus();
						}
					}
				}
			}
		}

		@Override
		protected void calculateTabRects(int tabPlacement, int tabCount) {
			FontMetrics metrics = getFontMetrics();
			Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
			int i;

			int x = tabAreaInsets.left - 2;
			int y = tabAreaInsets.top;
			int totalWidth = 0;
			int totalHeight = 0;

			//
			// Calculate bounds within which a tab run must fit
			//

			maxTabHeight = calculateMaxTabHeight(tabPlacement);

			runCount = 0;
			selectedRun = -1;

			if (tabCount == 0) {
				return;
			}

			selectedRun = 0;
			runCount = 1;

			// Run through tabs and lay them out in a single run
			Rectangle rect;
			for (i = 0; i < tabCount; i++) {
				rect = rects[i];

				if (i > 0) {
					rect.x = rects[i - 1].x + rects[i - 1].width - 1;
				}
				else {
					tabRuns[0] = 0;
					maxTabWidth = 0;
					totalHeight += maxTabHeight;
					rect.x = x;
				}
				rect.width = calculateTabWidth(tabPlacement, i, metrics);
				totalWidth = rect.x + rect.width;
				maxTabWidth = Math.max(maxTabWidth, rect.width);

				rect.y = y;
				rect.height = maxTabHeight /* - 2 */;

			}

			// tabPanel.setSize(totalWidth, totalHeight);
			tabScroller.tabPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
		}
	}

	private class ScrollableTabSupport implements ChangeListener {
		public ScrollableTabViewport viewport;

		public ScrollableTabPanel tabPanel;

		public ScrollableTabButton scrollForwardButton;

		public ScrollableTabButton scrollBackwardButton;

		public int leadingTabIndex;

		private Point tabViewPosition = new Point(0, 0);

		ScrollableTabSupport() {
			viewport = new ScrollableTabViewport();
			tabPanel = new ScrollableTabPanel();
			viewport.setView(tabPanel);
			viewport.addChangeListener(this);

			scrollForwardButton = createScrollableTabButton(EAST);
			scrollBackwardButton = createScrollableTabButton(WEST);
			// scrollForwardButton = new ScrollableTabButton(EAST);
			// scrollBackwardButton = new ScrollableTabButton(WEST);
		}

		public void scrollForward(int tabPlacement) {
			Dimension viewSize = viewport.getViewSize();
			Rectangle viewRect = viewport.getViewRect();

			if (tabPlacement == TOP || tabPlacement == BOTTOM) {
				if (viewRect.width >= viewSize.width - viewRect.x) {
					return; // no room left to scroll
				}
			}
			else { // tabPlacement == LEFT || tabPlacement == RIGHT
				if (viewRect.height >= viewSize.height - viewRect.y) {
					return;
				}
			}
			setLeadingTabIndex(leadingTabIndex + 1);
		}

		public void scrollBackward() {
			if (leadingTabIndex == 0) {
				return; // no room left to scroll
			}
			setLeadingTabIndex(leadingTabIndex - 1);
		}

		public void setLeadingTabIndex(int index) {
			leadingTabIndex = index;
			Dimension viewSize = viewport.getViewSize();
			Rectangle viewRect = viewport.getViewRect();

			tabViewPosition.x = leadingTabIndex == 0 ? 0 : rects[leadingTabIndex].x;

			if ((viewSize.width - tabViewPosition.x) < viewRect.width) {
				// We've scrolled to the end, so adjust the viewport size
				// to ensure the view position remains aligned on a tab boundary
				Dimension extentSize = new Dimension(viewSize.width - tabViewPosition.x, viewRect.height);
				viewport.setExtentSize(extentSize);
			}

			viewport.setViewPosition(tabViewPosition);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JViewport viewport = (JViewport) e.getSource();
			int tabPlacement = tabPane.getTabPlacement();
			int tabCount = tabPane.getTabCount();
			Rectangle vpRect = viewport.getBounds();
			Dimension viewSize = viewport.getViewSize();
			Rectangle viewRect = viewport.getViewRect();

			leadingTabIndex = getClosestTab(viewRect.x, viewRect.y);

			// If the tab isn't right aligned, adjust it.
			if (leadingTabIndex + 1 < tabCount) {

				if (rects[leadingTabIndex].x < viewRect.x) {
					leadingTabIndex++;
				}

			}
			Insets contentInsets = getContentBorderInsets(tabPlacement);

			tabPane.repaint(vpRect.x, vpRect.y + vpRect.height, vpRect.width, contentInsets.top);
			scrollBackwardButton.setEnabled(viewRect.x > 0);
			scrollForwardButton.setEnabled(leadingTabIndex < tabCount - 1 && viewSize.width - viewRect.x > viewRect.width);

		}

		@Override
		public String toString() {
			return new String("viewport.viewSize=" + viewport.getViewSize() + "\n" + "viewport.viewRectangle=" + viewport.getViewRect() + "\n"
					+ "leadingTabIndex=" + leadingTabIndex + "\n" + "tabViewPosition=" + tabViewPosition);
		}

	}

	private class ScrollableTabViewport extends JViewport implements UIResource {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9089757009472342660L;

		public ScrollableTabViewport() {
			super();
			setScrollMode(SIMPLE_SCROLL_MODE);
		}
	}

	private class ScrollableTabPanel extends JPanel implements UIResource {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3666901839718883744L;

		public ScrollableTabPanel() {
			setLayout(null);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			CloseTabPaneUI.this.paintTabArea(g, tabPane.getTabPlacement(), tabPane.getSelectedIndex());

		}
	}

	protected class ScrollableTabButton extends BasicArrowButton implements UIResource {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9177973352642315639L;

		public ScrollableTabButton(int direction) {
			super(direction, UIManager.getColor("TabbedPane.selected"), UIManager.getColor("TabbedPane.shadow"), UIManager
					.getColor("TabbedPane.darkShadow"), UIManager.getColor("TabbedPane.highlight"));

		}

		public boolean scrollsForward() {
			return direction == EAST || direction == SOUTH;
		}

	}

	/**
	 * This inner class is marked &quot;public&quot; due to a compiler bug. This
	 * class should be treated as a &quot;protected&quot; inner class.
	 * Instantiate it only within subclasses of BasicTabbedPaneUI.
	 */
	public class TabSelectionHandler implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			JTabbedPane tabPane = (JTabbedPane) e.getSource();
			tabPane.revalidate();
			tabPane.repaint();

			if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
				int index = tabPane.getSelectedIndex();
				if (index < rects.length && index != -1) {
					tabScroller.tabPanel.scrollRectToVisible(rects[index]);
				}
			}
		}
	}

	/**
	 * This inner class is marked &quot;public&quot; due to a compiler bug. This
	 * class should be treated as a &quot;protected&quot; inner class.
	 * Instantiate it only within subclasses of BasicTabbedPaneUI.
	 */

	/*
	 * GES 2/3/99: The container listener code was added to support HTML
	 * rendering of tab titles.
	 * 
	 * Ideally, we would be able to listen for property changes when a tab is
	 * added or its text modified. At the moment there are no such events
	 * because the Beans spec doesn't allow 'indexed' property changes (i.e. tab
	 * 2's text changed from A to B).
	 * 
	 * In order to get around this, we listen for tabs to be added or removed by
	 * listening for the container events. we then queue up a runnable (so the
	 * component has a chance to complete the add) which checks the tab title of
	 * the new component to see if it requires HTML rendering.
	 * 
	 * The Views (one per tab title requiring HTML rendering) are stored in the
	 * htmlViews Vector, which is only allocated after the first time we run
	 * into an HTML tab. Note that this vector is kept in step with the number
	 * of pages, and nulls are added for those pages whose tab title do not
	 * require HTML rendering.
	 * 
	 * This makes it easy for the paint and layout code to tell whether to
	 * invoke the HTML engine without having to check the string during
	 * time-sensitive operations.
	 * 
	 * When we have added a way to listen for tab additions and changes to tab
	 * text, this code should be removed and replaced by something which uses
	 * that.
	 */

	private class ContainerHandler implements ContainerListener {
		@Override
		public void componentAdded(ContainerEvent e) {
			JTabbedPane tp = (JTabbedPane) e.getContainer();
			Component child = e.getChild();
			if (child instanceof UIResource) {
				return;
			}
			int index = tp.indexOfComponent(child);
			String title = tp.getTitleAt(index);
			boolean isHTML = BasicHTML.isHTMLString(title);
			if (isHTML) {
				if (htmlViews == null) { // Initialize vector
					htmlViews = createHTMLVector();
				}
				else { // Vector already exists
					View v = BasicHTML.createHTMLView(tp, title);
					htmlViews.insertElementAt(v, index);
				}
			}
			else { // Not HTML
				if (htmlViews != null) { // Add placeholder
					htmlViews.insertElementAt(null, index);
				} // else nada!
			}
		}

		@Override
		public void componentRemoved(ContainerEvent e) {
			JTabbedPane tp = (JTabbedPane) e.getContainer();
			Component child = e.getChild();
			if (child instanceof UIResource) {
				return;
			}

			// NOTE 4/15/2002 (joutwate):
			// This fix is implemented using client properties since there is
			// currently no IndexPropertyChangeEvent. Once
			// IndexPropertyChangeEvents have been added this code should be
			// modified to use it.
			Integer indexObj = (Integer) tp.getClientProperty("__index_to_remove__");
			if (indexObj != null) {
				int index = indexObj.intValue();
				if (htmlViews != null && htmlViews.size() >= index) {
					htmlViews.removeElementAt(index);
				}
			}
		}
	}

	private Vector<View> createHTMLVector() {
		Vector<View> htmlViews = new Vector<View>();
		int count = tabPane.getTabCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				String title = tabPane.getTitleAt(i);
				if (BasicHTML.isHTMLString(title)) {
					htmlViews.addElement(BasicHTML.createHTMLView(tabPane, title));
				}
				else {
					htmlViews.addElement(null);
				}
			}
		}
		return htmlViews;
	}

	class MyMouseHandler extends MouseHandler {

		public MyMouseHandler() {
			super();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			biosim.enableTabMenu(biosim.getTab().getSelectedIndex());
			if (closeIndexStatus == OVER) {
				closeIndexStatus = PRESSED;
				tabScroller.tabPanel.repaint();
				return;
			}

			if (maxIndexStatus == OVER) {
				maxIndexStatus = PRESSED;
				tabScroller.tabPanel.repaint();
				return;
			}

		}

		@Override
		public void mouseClicked(MouseEvent e) {
			super.mousePressed(e);
			biosim.enableTabMenu(biosim.getTab().getSelectedIndex());
			if (e.getClickCount() > 1 && overTabIndex != -1) {
				((CloseAndMaxTabbedPane) tabPane).fireDoubleClickTabEvent(e, overTabIndex);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {

			biosim.enableTabMenu(biosim.getTab().getSelectedIndex());
			updateOverTab(e.getX(), e.getY());

			if (overTabIndex == -1) {
				if (e.isPopupTrigger())
					((CloseAndMaxTabbedPane) tabPane).firePopupOutsideTabEvent(e);
				return;
			}

			if (isOneActionButtonEnabled() && e.isPopupTrigger()) {
				super.mousePressed(e);

				closeIndexStatus = INACTIVE; // Prevent undesired action when
				maxIndexStatus = INACTIVE; // right-clicking on icons

				actionPopupMenu.show(tabScroller.tabPanel, e.getX(), e.getY());
				return;
			}

			if (closeIndexStatus == PRESSED) {
				closeIndexStatus = OVER;
				tabScroller.tabPanel.repaint();
				((CloseAndMaxTabbedPane) tabPane).fireCloseTabEvent(e, overTabIndex);
				return;
			}

			if (maxIndexStatus == PRESSED) {
				maxIndexStatus = OVER;
				tabScroller.tabPanel.repaint();
				((CloseAndMaxTabbedPane) tabPane).fireMaxTabEvent(e, overTabIndex);
				return;
			}

		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (!mousePressed) {
				overTabIndex = -1;
				tabScroller.tabPanel.repaint();
			}
		}

	}

	class MyMouseMotionListener implements MouseMotionListener {

		@Override
		public void mouseMoved(MouseEvent e) {
			if (actionPopupMenu.isVisible())
				return; // No updates when popup is visible
			mousePressed = false;
			setTabIcons(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (actionPopupMenu.isVisible())
				return; // No updates when popup is visible
			mousePressed = true;
			setTabIcons(e.getX(), e.getY());
		}
	}

	public JButton getCloseB() {
		return closeB;
	}

	public void setCloseB(JButton closeB) {
		this.closeB = closeB;
	}
}
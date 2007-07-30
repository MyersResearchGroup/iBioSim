package graph.core.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.batik.dom.*;
import org.apache.batik.svggen.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import org.jibble.epsgraphics.EpsGraphics2D;
import org.w3c.dom.*;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import biomodelsim.core.gui.*;
import buttons.core.gui.*;

/**
 * This is the Graph class. It takes in data and draws a graph of that data. The
 * Graph class implements the ActionListener class, the ChartProgressListener
 * class, and the MouseListener class. This allows the Graph class to perform
 * actions when buttons are pressed, when the chart is drawn, or when the chart
 * is clicked.
 * 
 * @author Curtis Madsen
 */
public class Graph extends JPanel implements ActionListener, MouseListener, ChartProgressListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4350596002373546900L;

	private JFreeChart chart; // Graph of the output data

	private int run; // total number of runs

	private String printer_track_quantity1; // label for y-axis on chart

	private String outDir1; // output directory

	private String printer_id1; // printer id

	/*
	 * Text fields used to change the graph window
	 */
	private JTextField XMin, XMax, XScale, YMin, YMax, YScale;

	private ArrayList<String> graphSpecies; // names of species in the graph

	private String savedPics; // directory for saved pictures

	private BioSim biomodelsim; // tstubd gui

	private JButton exportJPeg, exportPng, exportPdf, exportEps, exportSvg; // buttons

	private HashMap<String, Paint> colors;

	private HashMap<String, Shape> shapes;

	private String selected, lastSelected;

	private LinkedList<GraphSpecies> graphed;

	private JCheckBox resize;

	private boolean displayed;

	/**
	 * Creates a Graph Object from the data given and calls the private graph
	 * helper method.
	 */
	public Graph(String file, String printer_track_quantity, String label, String printer_id,
			String outDir, int run, int readIn, XYSeriesCollection dataset, String time,
			BioSim biomodelsim) {
		// initializes member variables
		this.run = run;
		this.printer_track_quantity1 = printer_track_quantity;
		this.outDir1 = outDir;
		this.printer_id1 = printer_id;
		this.biomodelsim = biomodelsim;
		XYSeriesCollection data;
		if (dataset == null) {
			data = new XYSeriesCollection();
		} else {
			data = dataset;
		}
		displayed = false;

		// graph the output data
		setUpShapesAndColors();
		graphed = new LinkedList<GraphSpecies>();
		selected = "";
		lastSelected = "";
		graph(file, printer_track_quantity, label, readIn, data, time);
	}

	/**
	 * This private helper method calls the private readData method, sets up a
	 * graph frame, and graphs the data.
	 * 
	 * @param dataset
	 * @param time
	 */
	private void graph(String file, String printer_track_quantity, String label, int readIn,
			XYSeriesCollection dataset, String time) {
		// creates the graph from the dataset and adds it to a chart panel
		readGraphSpecies(file, biomodelsim.frame());
		for (int i = 2; i < graphSpecies.size(); i++) {
			String index = graphSpecies.get(i);
			int j = i;
			while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
				graphSpecies.set(j, graphSpecies.get(j - 1));
				j = j - 1;
			}
			graphSpecies.set(j, index);
		}
		chart = ChartFactory.createXYLineChart(label, time, printer_track_quantity, dataset,
				PlotOrientation.VERTICAL, true, true, false);
		chart.addProgressListener(this);
		ChartPanel graph = new ChartPanel(chart);
		graph.setLayout(new GridLayout(1, 1));
		JLabel edit = new JLabel("Click here to create graph");
		Font font = edit.getFont();
		font = font.deriveFont(Font.BOLD, 42.0f);
		edit.setFont(font);
		edit.setHorizontalAlignment(SwingConstants.CENTER);
		graph.add(edit);
		graph.addMouseListener(this);

		// creates text fields for changing the graph's dimensions
		resize = new JCheckBox("Auto Resize");
		resize.setSelected(true);
		XMin = new JTextField();
		XMax = new JTextField();
		XScale = new JTextField();
		YMin = new JTextField();
		YMax = new JTextField();
		YScale = new JTextField();

		// creates the buttons for the graph frame
		JPanel ButtonHolder = new JPanel();
		exportJPeg = new JButton("Export As JPEG");
		exportPng = new JButton("Export As PNG");
		exportPdf = new JButton("Export As PDF");
		exportEps = new JButton("Export As EPS");
		exportSvg = new JButton("Export As SVG");
		exportJPeg.addActionListener(this);
		exportPng.addActionListener(this);
		exportPdf.addActionListener(this);
		exportEps.addActionListener(this);
		exportSvg.addActionListener(this);
		ButtonHolder.add(exportJPeg);
		ButtonHolder.add(exportPng);
		ButtonHolder.add(exportPdf);
		ButtonHolder.add(exportEps);
		ButtonHolder.add(exportSvg);

		// puts all the components of the graph gui into a display panel
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ButtonHolder, null);
		splitPane.setDividerSize(0);
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(graph, "Center");
		this.add(splitPane, "South");

		// determines maximum and minimum values and resizes
		resize(dataset);
		this.revalidate();
	}

	private void readGraphSpecies(String file, Component component) {
		InputStream input;
		component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			input = new BufferedInputStream(new ProgressMonitorInputStream(component,
					"Reading Reb2sac Output Data From " + new File(file).getName(),
					new FileInputStream(new File(file))));
			graphSpecies = new ArrayList<String>();
			boolean reading = true;
			char cha;
			int readCount = 0;
			while (reading) {
				String word = "";
				boolean readWord = true;
				while (readWord) {
					int read = input.read();
					readCount++;
					if (read == -1) {
						reading = false;
						readWord = false;
					}
					cha = (char) read;
					if (Character.isWhitespace(cha)) {
						input.mark(3);
						char next = (char) input.read();
						char after = (char) input.read();
						String check = "" + next + after;
						if (word.equals("") || word.equals("0") || check.equals("0,")) {
							readWord = false;
						} else {
							word += cha;
						}
						input.reset();
					} else if (cha == ',' || cha == ':' || cha == ';' || cha == '!' || cha == '?'
							|| cha == '\"' || cha == '\'' || cha == '(' || cha == ')' || cha == '{'
							|| cha == '}' || cha == '[' || cha == ']' || cha == '<' || cha == '>'
							|| cha == '*' || cha == '=' || cha == '#') {
						readWord = false;
					} else if (read != -1) {
						word += cha;
					}
				}
				int getNum;
				try {
					getNum = Integer.parseInt(word);
					if (getNum == 0) {
						int read = 0;
						while (read != -1) {
							read = input.read();
						}
					}
				} catch (Exception e1) {
					if (word.equals("")) {
					} else {
						graphSpecies.add(word);
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(component, "Error Reading Data!"
					+ "\nThere was an error reading the simulation output data.",
					"Error Reading Data", JOptionPane.ERROR_MESSAGE);
		}
		component.setCursor(null);
	}

	/**
	 * This private helper method parses the output file of ODE, monte carlo,
	 * and markov abstractions.
	 */
	private ArrayList<ArrayList<Double>> readData(String file, Component component,
			String printer_track_quantity, String label) {
		String[] s = file.split(File.separator);
		String getLast = s[s.length - 1];
		String stem = "";
		int t = 0;
		try {
			while (!Character.isDigit(getLast.charAt(t))) {
				stem += getLast.charAt(t);
				t++;
			}
		} catch (Exception e) {

		}
		if (label.contains("variance")) {
			return calculateAverageVarianceDeviation(file, stem, 1);
		} else if (label.contains("deviation")) {
			return calculateAverageVarianceDeviation(file, stem, 2);
		} else if (label.contains("average")) {
			return calculateAverageVarianceDeviation(file, stem, 0);
		} else {
			ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
			InputStream input;
			component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				input = new BufferedInputStream(new ProgressMonitorInputStream(component,
						"Reading Reb2sac Output Data From " + new File(file).getName(),
						new FileInputStream(new File(file))));
				boolean reading = true;
				char cha;
				int readCount = 0;
				while (reading) {
					String word = "";
					boolean readWord = true;
					while (readWord) {
						int read = input.read();
						readCount++;
						if (read == -1) {
							reading = false;
							readWord = false;
						}
						cha = (char) read;
						if (Character.isWhitespace(cha)) {
							input.mark(3);
							char next = (char) input.read();
							char after = (char) input.read();
							String check = "" + next + after;
							if (word.equals("") || word.equals("0") || check.equals("0,")) {
								readWord = false;
							} else {
								word += cha;
							}
							input.reset();
						} else if (cha == ',' || cha == ':' || cha == ';' || cha == '!'
								|| cha == '?' || cha == '\"' || cha == '\'' || cha == '('
								|| cha == ')' || cha == '{' || cha == '}' || cha == '['
								|| cha == ']' || cha == '<' || cha == '>' || cha == '*'
								|| cha == '=' || cha == '#') {
							readWord = false;
						} else if (read != -1) {
							word += cha;
						}
					}
					int getNum;
					try {
						getNum = Integer.parseInt(word);
						if (getNum == 0) {
							for (int i = 0; i < graphSpecies.size(); i++) {
								data.add(new ArrayList<Double>());
							}
							(data.get(0)).add(0.0);
							int counter = 1;
							reading = true;
							while (reading) {
								word = "";
								readWord = true;
								int read;
								while (readWord) {
									read = input.read();
									cha = (char) read;
									while (!Character.isWhitespace(cha) && cha != ',' && cha != ':'
											&& cha != ';' && cha != '!' && cha != '?'
											&& cha != '\"' && cha != '\'' && cha != '('
											&& cha != ')' && cha != '{' && cha != '}' && cha != '['
											&& cha != ']' && cha != '<' && cha != '>' && cha != '_'
											&& cha != '*' && cha != '=' && read != -1) {
										word += cha;
										read = input.read();
										cha = (char) read;
									}
									if (read == -1) {
										reading = false;
									}
									readWord = false;
								}
								int insert;
								if (!word.equals("")) {
									if (counter < graphSpecies.size()) {
										insert = counter;
									} else {
										insert = counter % graphSpecies.size();
									}
									(data.get(insert)).add(Double.parseDouble(word));
									counter++;
								}
							}
						}
					} catch (Exception e1) {
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(component, "Error Reading Data!"
						+ "\nThere was an error reading the simulation output data.",
						"Error Reading Data", JOptionPane.ERROR_MESSAGE);
			}
			component.setCursor(null);
			return data;
		}
	}

	/**
	 * This method adds and removes plots from the graph depending on what check
	 * boxes are selected.
	 */
	public void actionPerformed(ActionEvent e) {
		// if the export as jpeg button is clicked
		if (e.getSource() == exportJPeg) {
			export(0);
		}
		// if the export as png button is clicked
		else if (e.getSource() == exportPng) {
			export(1);
		}
		// if the export as pdf button is clicked
		else if (e.getSource() == exportPdf) {
			export(2);
		}
		// if the export as eps button is clicked
		else if (e.getSource() == exportEps) {
			export(3);
		}
		// if the export as svg button is clicked
		else if (e.getSource() == exportSvg) {
			export(4);
		}
	}

	/**
	 * Private method used to auto resize the graph.
	 */
	private void resize(XYSeriesCollection dataset) {
		NumberFormat num = NumberFormat.getInstance();
		num.setMaximumFractionDigits(4);
		num.setGroupingUsed(false);
		XYPlot plot = chart.getXYPlot();
		XYItemRenderer rend = plot.getRenderer();
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		for (int j = 0; j < dataset.getSeriesCount(); j++) {
			XYSeries series = dataset.getSeries(j);
			Boolean visible = rend.getSeriesVisible(j);
			if (visible == null || visible.equals(true)) {
				for (int k = 0; k < series.getItemCount(); k++) {
					maxY = Math.max(series.getY(k).doubleValue(), maxY);
					minY = Math.min(series.getY(k).doubleValue(), minY);
					maxX = Math.max(series.getX(k).doubleValue(), maxX);
					minX = Math.min(series.getX(k).doubleValue(), minX);
				}
			}
		}
		NumberAxis axis = (NumberAxis) plot.getRangeAxis();
		if (minY == Double.MAX_VALUE || maxY == Double.MIN_VALUE) {
			axis.setRange(-1, 1);
		} else if ((maxY - minY) < .001) {
			axis.setRange(minY - 1, maxY + 1);
		} else {
			axis.setRange(Double.parseDouble(num.format(minY - (Math.abs(minY) * .1))), Double
					.parseDouble(num.format(maxY + (Math.abs(maxY) * .1))));
		}
		axis.setAutoTickUnitSelection(true);
		axis = (NumberAxis) plot.getDomainAxis();
		if (minX == Double.MAX_VALUE || maxX == Double.MIN_VALUE) {
			axis.setRange(-1, 1);
		} else if ((maxX - minX) < .001) {
			axis.setRange(minX - 1, maxX + 1);
		} else {
			axis.setRange(Double.parseDouble(num.format(minX)), Double
					.parseDouble(num.format(maxX)));
		}
		axis.setAutoTickUnitSelection(true);
	}

	/**
	 * After the chart is redrawn, this method calculates the x and y scale and
	 * updates those text fields.
	 */
	public void chartProgress(ChartProgressEvent e) {
		// if the chart drawing is started
		if (e.getType() == ChartProgressEvent.DRAWING_STARTED) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		// if the chart drawing is finished
		else if (e.getType() == ChartProgressEvent.DRAWING_FINISHED) {
			this.setCursor(null);
			JFreeChart chart = e.getChart();
			XYPlot plot = (XYPlot) chart.getPlot();
			NumberAxis axis = (NumberAxis) plot.getRangeAxis();
			YMin.setText("" + axis.getLowerBound());
			YMax.setText("" + axis.getUpperBound());
			YScale.setText("" + axis.getTickUnit().getSize());
			axis = (NumberAxis) plot.getDomainAxis();
			XMin.setText("" + axis.getLowerBound());
			XMax.setText("" + axis.getUpperBound());
			XScale.setText("" + axis.getTickUnit().getSize());
		}
	}

	/**
	 * Invoked when the mouse is clicked on the chart. Allows the user to edit
	 * the title and labels of the chart.
	 */
	public void mouseClicked(MouseEvent e) {
		editGraph();
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	private void setUpShapesAndColors() {
		DefaultDrawingSupplier draw = new DefaultDrawingSupplier();
		colors = new HashMap<String, Paint>();
		shapes = new HashMap<String, Shape>();
		colors.put("Red", draw.getNextPaint());
		colors.put("Blue", draw.getNextPaint());
		colors.put("Green", draw.getNextPaint());
		colors.put("Yellow", draw.getNextPaint());
		colors.put("Magenta", draw.getNextPaint());
		colors.put("Cyan", draw.getNextPaint());
		colors.put("Tan", draw.getNextPaint());
		colors.put("Gray (Dark)", draw.getNextPaint());
		colors.put("Red (Dark)", draw.getNextPaint());
		colors.put("Blue (Dark)", draw.getNextPaint());
		colors.put("Green (Dark)", draw.getNextPaint());
		colors.put("Yellow (Dark)", draw.getNextPaint());
		colors.put("Magenta (Dark)", draw.getNextPaint());
		colors.put("Cyan (Dark)", draw.getNextPaint());
		colors.put("Black", draw.getNextPaint());
		colors.put("Red ", draw.getNextPaint());
		colors.put("Blue ", draw.getNextPaint());
		colors.put("Green ", draw.getNextPaint());
		colors.put("Yellow ", draw.getNextPaint());
		colors.put("Magenta ", draw.getNextPaint());
		colors.put("Cyan ", draw.getNextPaint());
		colors.put("Gray (Light)", draw.getNextPaint());
		colors.put("Red (Extra Dark)", draw.getNextPaint());
		colors.put("Blue (Extra Dark)", draw.getNextPaint());
		colors.put("Green (Extra Dark)", draw.getNextPaint());
		colors.put("Yellow (Extra Dark)", draw.getNextPaint());
		colors.put("Magenta (Extra Dark)", draw.getNextPaint());
		colors.put("Cyan (Extra Dark)", draw.getNextPaint());
		colors.put("Red (Light)", draw.getNextPaint());
		colors.put("Blue (Light)", draw.getNextPaint());
		colors.put("Green (Light)", draw.getNextPaint());
		colors.put("Yellow (Light)", draw.getNextPaint());
		colors.put("Magenta (Light)", draw.getNextPaint());
		colors.put("Cyan (Light)", draw.getNextPaint());
		shapes.put("Square", draw.getNextShape());
		shapes.put("Circle", draw.getNextShape());
		shapes.put("Triangle", draw.getNextShape());
		shapes.put("Diamond", draw.getNextShape());
		shapes.put("Rectangle (Horizontal)", draw.getNextShape());
		shapes.put("Triangle (Upside Down)", draw.getNextShape());
		shapes.put("Circle (Half)", draw.getNextShape());
		shapes.put("Arrow", draw.getNextShape());
		shapes.put("Rectangle (Vertical)", draw.getNextShape());
		shapes.put("Arrow (Backwards)", draw.getNextShape());
	}

	private void editGraph() {
		if (!displayed) {
			final ArrayList<GraphSpecies> old = new ArrayList<GraphSpecies>();
			for (GraphSpecies g : graphed) {
				old.add(g);
			}
			JPanel titlePanel = new JPanel(new GridLayout(4, 6));
			JLabel titleLabel = new JLabel("Title:");
			JLabel xLabel = new JLabel("X-Axis Label:");
			JLabel yLabel = new JLabel("Y-Axis Label:");
			final JTextField title = new JTextField(chart.getTitle().getText(), 5);
			final JTextField x = new JTextField(chart.getXYPlot().getDomainAxis().getLabel(), 5);
			final JTextField y = new JTextField(chart.getXYPlot().getRangeAxis().getLabel(), 5);
			final JLabel xMin = new JLabel("X-Min:");
			final JLabel xMax = new JLabel("X-Max:");
			final JLabel xScale = new JLabel("X-Step:");
			final JLabel yMin = new JLabel("Y-Min:");
			final JLabel yMax = new JLabel("Y-Max:");
			final JLabel yScale = new JLabel("Y-Step:");
			resize.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (((JCheckBox) e.getSource()).isSelected()) {
						xMin.setEnabled(false);
						XMin.setEnabled(false);
						xMax.setEnabled(false);
						XMax.setEnabled(false);
						xScale.setEnabled(false);
						XScale.setEnabled(false);
						yMin.setEnabled(false);
						YMin.setEnabled(false);
						yMax.setEnabled(false);
						YMax.setEnabled(false);
						yScale.setEnabled(false);
						YScale.setEnabled(false);
					} else {
						xMin.setEnabled(true);
						XMin.setEnabled(true);
						xMax.setEnabled(true);
						XMax.setEnabled(true);
						xScale.setEnabled(true);
						XScale.setEnabled(true);
						yMin.setEnabled(true);
						YMin.setEnabled(true);
						yMax.setEnabled(true);
						YMax.setEnabled(true);
						yScale.setEnabled(true);
						YScale.setEnabled(true);
					}
				}
			});
			if (resize.isSelected()) {
				xMin.setEnabled(false);
				XMin.setEnabled(false);
				xMax.setEnabled(false);
				XMax.setEnabled(false);
				xScale.setEnabled(false);
				XScale.setEnabled(false);
				yMin.setEnabled(false);
				YMin.setEnabled(false);
				yMax.setEnabled(false);
				YMax.setEnabled(false);
				yScale.setEnabled(false);
				YScale.setEnabled(false);
			} else {
				xMin.setEnabled(true);
				XMin.setEnabled(true);
				xMax.setEnabled(true);
				XMax.setEnabled(true);
				xScale.setEnabled(true);
				XScale.setEnabled(true);
				yMin.setEnabled(true);
				YMin.setEnabled(true);
				yMax.setEnabled(true);
				YMax.setEnabled(true);
				yScale.setEnabled(true);
				YScale.setEnabled(true);
			}
			titlePanel.add(titleLabel);
			titlePanel.add(title);
			titlePanel.add(xMin);
			titlePanel.add(XMin);
			titlePanel.add(yMin);
			titlePanel.add(YMin);
			titlePanel.add(xLabel);
			titlePanel.add(x);
			titlePanel.add(xMax);
			titlePanel.add(XMax);
			titlePanel.add(yMax);
			titlePanel.add(YMax);
			titlePanel.add(yLabel);
			titlePanel.add(y);
			titlePanel.add(xScale);
			titlePanel.add(XScale);
			titlePanel.add(yScale);
			titlePanel.add(YScale);
			titlePanel.add(new JPanel());
			titlePanel.add(new JPanel());
			titlePanel.add(new JPanel());
			titlePanel.add(new JPanel());
			titlePanel.add(new JPanel());
			titlePanel.add(resize);
			String simDirString = outDir1.split(File.separator)[outDir1.split(File.separator).length - 1];
			final DefaultMutableTreeNode simDir = new DefaultMutableTreeNode(simDirString);
			String[] files = new File(outDir1).list();
			boolean add = false;
			for (String file : files) {
				if (file.contains(printer_id1.substring(0, printer_id1.length() - 8))) {
					if (file.contains("run-")) {
						add = true;
					} else {
						simDir
								.add(new DefaultMutableTreeNode(file
										.substring(0, file.length() - 4)));
					}
				}
			}
			if (add) {
				simDir.add(new DefaultMutableTreeNode("Average"));
				simDir.add(new DefaultMutableTreeNode("Variance"));
				simDir.add(new DefaultMutableTreeNode("Standard Deviation"));
			}
			for (int i = 0; i < run; i++) {
				if (new File(outDir1 + File.separator + "run-" + (i + 1) + "."
						+ printer_id1.substring(0, printer_id1.length() - 8)).exists()) {
					simDir.add(new DefaultMutableTreeNode("run-" + (i + 1)));
				}
			}
			JPanel speciesPanel1 = new JPanel(new GridLayout(graphSpecies.size(), 1));
			JPanel speciesPanel2 = new JPanel(new GridLayout(graphSpecies.size(), 3));
			JPanel speciesPanel3 = new JPanel(new GridLayout(graphSpecies.size(), 3));
			final JCheckBox use = new JCheckBox("Use");
			JLabel specs = new JLabel("Species");
			JLabel color = new JLabel("Color");
			JLabel shape = new JLabel("Shape");
			final JCheckBox connectedLabel = new JCheckBox("Connected");
			final JCheckBox visibleLabel = new JCheckBox("Visible");
			final JCheckBox filledLabel = new JCheckBox("Filled");
			connectedLabel.setSelected(true);
			visibleLabel.setSelected(true);
			filledLabel.setSelected(true);
			final ArrayList<JCheckBox> boxes = new ArrayList<JCheckBox>();
			final ArrayList<JTextField> series = new ArrayList<JTextField>();
			final ArrayList<JComboBox> colors = new ArrayList<JComboBox>();
			final ArrayList<JComboBox> shapes = new ArrayList<JComboBox>();
			final ArrayList<JCheckBox> connected = new ArrayList<JCheckBox>();
			final ArrayList<JCheckBox> visible = new ArrayList<JCheckBox>();
			final ArrayList<JCheckBox> filled = new ArrayList<JCheckBox>();
			use.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (use.isSelected()) {
						for (JCheckBox box : boxes) {
							if (!box.isSelected()) {
								box.doClick();
							}
						}
					} else {
						for (JCheckBox box : boxes) {
							if (box.isSelected()) {
								box.doClick();
							}
						}
					}
				}
			});
			connectedLabel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (connectedLabel.isSelected()) {
						for (JCheckBox box : connected) {
							box.setSelected(true);
						}
					} else {
						for (JCheckBox box : connected) {
							box.setSelected(false);
						}
					}
				}
			});
			visibleLabel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (visibleLabel.isSelected()) {
						for (JCheckBox box : visible) {
							box.setSelected(true);
						}
					} else {
						for (JCheckBox box : visible) {
							box.setSelected(false);
						}
					}
				}
			});
			filledLabel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (filledLabel.isSelected()) {
						for (JCheckBox box : filled) {
							box.setSelected(true);
						}
					} else {
						for (JCheckBox box : filled) {
							box.setSelected(false);
						}
					}
				}
			});
			speciesPanel1.add(use);
			speciesPanel2.add(specs);
			speciesPanel2.add(color);
			speciesPanel2.add(shape);
			speciesPanel3.add(connectedLabel);
			speciesPanel3.add(visibleLabel);
			speciesPanel3.add(filledLabel);
			final HashMap<String, Shape> shapey = this.shapes;
			final HashMap<String, Paint> colory = this.colors;
			final JTree tree = new JTree(simDir);
			for (int i = 0; i < graphSpecies.size() - 1; i++) {
				JCheckBox temp = new JCheckBox();
				temp.setActionCommand("" + i);
				temp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int i = Integer.parseInt(e.getActionCommand());
						if (((JCheckBox) e.getSource()).isSelected()) {
							String s = series.get(i).getText();
							((JCheckBox) e.getSource()).setSelected(false);
							int[] cols = new int[34];
							int[] shaps = new int[10];
							int[] selectedRow = tree.getSelectionRows();
							for (int j = 1; j < tree.getRowCount(); j++) {
								tree.setSelectionRow(j);
								for (int k = 0; k < boxes.size(); k++) {
									if (boxes.get(k).isSelected()) {
										if (colors.get(k).getSelectedItem().equals("Red")) {
											cols[0]++;
										} else if (colors.get(k).getSelectedItem().equals("Blue")) {
											cols[1]++;
										} else if (colors.get(k).getSelectedItem().equals("Green")) {
											cols[2]++;
										} else if (colors.get(k).getSelectedItem().equals("Yellow")) {
											cols[3]++;
										} else if (colors.get(k).getSelectedItem()
												.equals("Magenta")) {
											cols[4]++;
										} else if (colors.get(k).getSelectedItem().equals("Cyan")) {
											cols[5]++;
										} else if (colors.get(k).getSelectedItem().equals("Tan")) {
											cols[6]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Gray (Dark)")) {
											cols[7]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Red (Dark)")) {
											cols[8]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Blue (Dark)")) {
											cols[9]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Green (Dark)")) {
											cols[10]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Yellow (Dark)")) {
											cols[11]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Magenta (Dark)")) {
											cols[12]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Cyan (Dark)")) {
											cols[13]++;
										} else if (colors.get(k).getSelectedItem().equals("Black")) {
											cols[14]++;
										} else if (colors.get(k).getSelectedItem().equals("Red ")) {
											cols[15]++;
										} else if (colors.get(k).getSelectedItem().equals("Blue ")) {
											cols[16]++;
										} else if (colors.get(k).getSelectedItem().equals("Green ")) {
											cols[17]++;
										} else if (colors.get(k).getSelectedItem()
												.equals("Yellow ")) {
											cols[18]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Magenta ")) {
											cols[19]++;
										} else if (colors.get(k).getSelectedItem().equals("Cyan ")) {
											cols[20]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Gray (Light)")) {
											cols[21]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Red (Extra Dark)")) {
											cols[22]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Blue (Extra Dark)")) {
											cols[23]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Green (Extra Dark)")) {
											cols[24]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Yellow (Extra Dark)")) {
											cols[25]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Magenta (Extra Dark)")) {
											cols[26]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Cyan (Extra Dark)")) {
											cols[27]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Red (Light)")) {
											cols[28]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Blue (Light)")) {
											cols[29]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Green (Light)")) {
											cols[30]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Yellow (Light)")) {
											cols[31]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Magenta (Light)")) {
											cols[32]++;
										} else if (colors.get(k).getSelectedItem().equals(
												"Cyan (Light)")) {
											cols[33]++;
										}
										if (shapes.get(k).getSelectedItem().equals("Square")) {
											shaps[0]++;
										} else if (shapes.get(k).getSelectedItem().equals("Circle")) {
											shaps[1]++;
										} else if (shapes.get(k).getSelectedItem().equals(
												"Triangle")) {
											shaps[2]++;
										} else if (shapes.get(k).getSelectedItem()
												.equals("Diamond")) {
											shaps[3]++;
										} else if (shapes.get(k).getSelectedItem().equals(
												"Rectangle (Horizontal)")) {
											shaps[4]++;
										} else if (shapes.get(k).getSelectedItem().equals(
												"Triangle (Upside Down)")) {
											shaps[5]++;
										} else if (shapes.get(k).getSelectedItem().equals(
												"Circle (Half)")) {
											shaps[6]++;
										} else if (shapes.get(k).getSelectedItem().equals("Arrow")) {
											shaps[7]++;
										} else if (shapes.get(k).getSelectedItem().equals(
												"Rectangle (Vertical)")) {
											shaps[8]++;
										} else if (shapes.get(k).getSelectedItem().equals(
												"Arrow (Backwards)")) {
											shaps[9]++;
										}
									}
								}
							}
							tree.setSelectionRows(selectedRow);
							((JCheckBox) e.getSource()).setSelected(true);
							series.get(i).setText(s);
							int colorSet = 0;
							for (int j = 1; j < cols.length; j++) {
								if (cols[j] < cols[colorSet]) {
									colorSet = j;
								}
							}
							int shapeSet = 0;
							for (int j = 1; j < shaps.length; j++) {
								if (shaps[j] < shaps[shapeSet]) {
									shapeSet = j;
								}
							}
							DefaultDrawingSupplier draw = new DefaultDrawingSupplier();
							for (int j = 0; j < colorSet; j++) {
								draw.getNextPaint();
							}
							Paint paint = draw.getNextPaint();
							Object[] set = colory.keySet().toArray();
							for (int j = 0; j < set.length; j++) {
								if (paint == colory.get(set[j])) {
									colors.get(i).setSelectedItem(set[j]);
								}
							}
							for (int j = 0; j < shapeSet; j++) {
								draw.getNextShape();
							}
							Shape shape = draw.getNextShape();
							set = shapey.keySet().toArray();
							for (int j = 0; j < set.length; j++) {
								if (shape == shapey.get(set[j])) {
									shapes.get(i).setSelectedItem(set[j]);
								}
							}
							boolean allChecked = true;
							for (JCheckBox temp : boxes) {
								if (!temp.isSelected()) {
									allChecked = false;
								}
							}
							if (allChecked) {
								use.setSelected(true);
							}
						} else {
							use.setSelected(false);
							colors.get(i).setSelectedIndex(0);
							shapes.get(i).setSelectedIndex(0);

						}
					}
				});
				boxes.add(temp);
				temp = new JCheckBox();
				temp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (((JCheckBox) e.getSource()).isSelected()) {
							boolean allChecked = true;
							for (JCheckBox temp : visible) {
								if (!temp.isSelected()) {
									allChecked = false;
								}
							}
							if (allChecked) {
								visibleLabel.setSelected(true);
							}
						} else {
							visibleLabel.setSelected(false);
						}
					}
				});
				visible.add(temp);
				visible.get(i).setSelected(true);
				temp = new JCheckBox();
				temp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (((JCheckBox) e.getSource()).isSelected()) {
							boolean allChecked = true;
							for (JCheckBox temp : filled) {
								if (!temp.isSelected()) {
									allChecked = false;
								}
							}
							if (allChecked) {
								filledLabel.setSelected(true);
							}
						} else {
							filledLabel.setSelected(false);
						}
					}
				});
				filled.add(temp);
				filled.get(i).setSelected(true);
				temp = new JCheckBox();
				temp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (((JCheckBox) e.getSource()).isSelected()) {
							boolean allChecked = true;
							for (JCheckBox temp : connected) {
								if (!temp.isSelected()) {
									allChecked = false;
								}
							}
							if (allChecked) {
								connectedLabel.setSelected(true);
							}
						} else {
							connectedLabel.setSelected(false);
						}
					}
				});
				connected.add(temp);
				connected.get(i).setSelected(true);
				series.add(new JTextField(graphSpecies.get(i + 1)));
				Object[] col = this.colors.keySet().toArray();
				Arrays.sort(col);
				Object[] shap = this.shapes.keySet().toArray();
				Arrays.sort(shap);
				JComboBox colBox = new JComboBox(col);
				JComboBox shapBox = new JComboBox(shap);
				colors.add(colBox);
				shapes.add(shapBox);
				speciesPanel1.add(boxes.get(i));
				speciesPanel2.add(series.get(i));
				speciesPanel2.add(colors.get(i));
				speciesPanel2.add(shapes.get(i));
				speciesPanel3.add(connected.get(i));
				speciesPanel3.add(visible.get(i));
				speciesPanel3.add(filled.get(i));
			}
			JScrollPane scrollpane = new JScrollPane();
			scrollpane.getViewport().add(tree);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath()
							.getLastPathComponent();
					if (!selected.equals("")) {
						ArrayList<GraphSpecies> remove = new ArrayList<GraphSpecies>();
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected)) {
								remove.add(g);
							}
						}
						for (GraphSpecies g : remove) {
							graphed.remove(g);
						}
						for (int i = 0; i < boxes.size(); i++) {
							if (boxes.get(i).isSelected()) {
								graphed.add(new GraphSpecies(shapey.get(shapes.get(i)
										.getSelectedItem()), colory.get(colors.get(i)
										.getSelectedItem()), filled.get(i).isSelected(), visible
										.get(i).isSelected(), connected.get(i).isSelected(),
										selected, series.get(i).getText().trim(), i));
							}
						}
					}
					selected = node.toString();
					int select;
					if (selected.equals("Average")) {
						select = 0;
					} else if (selected.equals("Variance")) {
						select = 1;
					} else if (selected.equals("Standard Deviation")) {
						select = 2;
					} else if (selected.contains("-run")) {
						select = 0;
					} else {
						try {
							select = Integer.parseInt(selected.substring(4)) + 2;
						} catch (Exception e1) {
							select = -1;
						}
					}
					if (select != -1) {
						for (int i = 0; i < series.size(); i++) {
							series.get(i).setText(graphSpecies.get(i + 1));
						}
						for (int i = 0; i < boxes.size(); i++) {
							boxes.get(i).setSelected(false);
						}
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected)) {
								boxes.get(g.getNumber()).setSelected(true);
								series.get(g.getNumber()).setText(g.getSpecies());
								colors.get(g.getNumber()).setSelectedItem(
										g.getShapeAndPaint().getPaintName());
								shapes.get(g.getNumber()).setSelectedItem(
										g.getShapeAndPaint().getShapeName());
								connected.get(g.getNumber()).setSelected(g.getConnected());
								visible.get(g.getNumber()).setSelected(g.getVisible());
								filled.get(g.getNumber()).setSelected(g.getFilled());
							}
						}
						boolean allChecked = true;
						boolean allCheckedVisible = true;
						boolean allCheckedFilled = true;
						boolean allCheckedConnected = true;
						for (int i = 0; i < boxes.size(); i++) {
							if (!boxes.get(i).isSelected()) {
								allChecked = false;
								String s = "";
								int[] selection = tree.getSelectionRows();
								if (selection != null) {
									for (int j : selection) {
										s = simDir.getChildAt(j - 1).toString();
									}
									if (s.equals("Average")) {
										s = "(" + (char) 967 + ")";
									} else if (s.equals("Variance")) {
										s = "(" + (char) 948 + (char) 178 + ")";
									} else if (s.equals("Standard Deviation")) {
										s = "(" + (char) 948 + ")";
									} else {
										if (s.contains("-run")) {
											s = s.substring(0, s.length() - 4);
										} else if (s.contains("run-")) {
											s = s.substring(4);
										}
										s = "(" + s + ")";
									}
									String text = series.get(i).getText();
									String end = "";
									if (text.length() >= s.length()) {
										for (int j = 0; j < s.length(); j++) {
											end = text.charAt(text.length() - 1 - j) + end;
										}
										if (!s.equals(end)) {
											text += " " + s;
										}
									} else {
										text += " " + s;
									}
									series.get(i).setText(text);
								}
								colors.get(i).setSelectedIndex(0);
								shapes.get(i).setSelectedIndex(0);
							}
							if (!visible.get(i).isSelected()) {
								allCheckedVisible = false;
							}
							if (!connected.get(i).isSelected()) {
								allCheckedConnected = false;
							}
							if (!filled.get(i).isSelected()) {
								allCheckedFilled = false;
							}
						}
						if (allChecked) {
							use.setSelected(true);
						} else {
							use.setSelected(false);
						}
						if (allCheckedVisible) {
							visibleLabel.setSelected(true);
						} else {
							visibleLabel.setSelected(false);
						}
						if (allCheckedFilled) {
							filledLabel.setSelected(true);
						} else {
							filledLabel.setSelected(false);
						}
						if (allCheckedConnected) {
							connectedLabel.setSelected(true);
						} else {
							connectedLabel.setSelected(false);
						}
					}
				}
			});
			boolean stop = false;
			for (int i = 1; i < tree.getRowCount(); i++) {
				tree.setSelectionRow(i);
				if (selected.equals(lastSelected)) {
					stop = true;
					break;
				}
			}
			if (!stop) {
				tree.setSelectionRow(1);
			}
			JScrollPane scroll = new JScrollPane();
			scroll.setPreferredSize(new Dimension(1050, 500));
			JPanel speciesPanel = new JPanel(new BorderLayout());
			speciesPanel.add(speciesPanel1, "West");
			speciesPanel.add(speciesPanel2, "Center");
			speciesPanel.add(speciesPanel3, "East");
			JPanel specPanel = new JPanel();
			specPanel.add(speciesPanel);
			JPanel editPanel = new JPanel(new BorderLayout());
			editPanel.add(titlePanel, "North");
			editPanel.add(specPanel, "Center");
			editPanel.add(scrollpane, "West");
			scroll.setViewportView(editPanel);
			final JFrame f = new JFrame("Edit Graph");
			JButton ok = new JButton("Ok");
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					double minY;
					double maxY;
					double scaleY;
					double minX;
					double maxX;
					double scaleX;
					try {
						minY = Double.parseDouble(YMin.getText().trim());
						maxY = Double.parseDouble(YMax.getText().trim());
						scaleY = Double.parseDouble(YScale.getText().trim());
						minX = Double.parseDouble(XMin.getText().trim());
						maxX = Double.parseDouble(XMax.getText().trim());
						scaleX = Double.parseDouble(XScale.getText().trim());
						NumberFormat num = NumberFormat.getInstance();
						num.setMaximumFractionDigits(4);
						num.setGroupingUsed(false);
						minY = Double.parseDouble(num.format(minY));
						maxY = Double.parseDouble(num.format(maxY));
						scaleY = Double.parseDouble(num.format(scaleY));
						minX = Double.parseDouble(num.format(minX));
						maxX = Double.parseDouble(num.format(maxX));
						scaleX = Double.parseDouble(num.format(scaleX));
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(biomodelsim.frame(),
								"Must enter doubles into the inputs "
										+ "to change the graph's dimensions!", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					lastSelected = selected;
					tree.setSelectionRow(-1);
					for (int i = 1; i < tree.getRowCount(); i++) {
						tree.setSelectionRow(i);
					}
					for (int i = 1; i < tree.getRowCount(); i++) {
						tree.setSelectionRow(i);
						if (selected.equals(lastSelected)) {
							break;
						}
					}
					selected = "";
					ArrayList<XYSeries> graphData = new ArrayList<XYSeries>();
					XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) chart.getXYPlot()
							.getRenderer();
					int thisOne = -1;
					for (int i = 1; i < graphed.size(); i++) {
						GraphSpecies index = graphed.get(i);
						int j = i;
						while ((j > 0)
								&& (graphed.get(j - 1).getSpecies().compareToIgnoreCase(
										index.getSpecies()) > 0)) {
							graphed.set(j, graphed.get(j - 1));
							j = j - 1;
						}
						graphed.set(j, index);
					}
					for (GraphSpecies g : graphed) {
						thisOne++;
						rend.setSeriesVisible(thisOne, true);
						rend.setSeriesLinesVisible(thisOne, g.getConnected());
						rend.setSeriesShapesFilled(thisOne, g.getFilled());
						rend.setSeriesShapesVisible(thisOne, g.getVisible());
						rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
						rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape());
						if (!g.getRunNumber().equals("Average")
								&& !g.getRunNumber().equals("Variance")
								&& !g.getRunNumber().equals("Standard Deviation")) {
							readGraphSpecies(outDir1 + File.separator + g.getRunNumber() + "."
									+ printer_id1.substring(0, printer_id1.length() - 8),
									biomodelsim.frame());
							ArrayList<ArrayList<Double>> data = readData(outDir1 + File.separator
									+ g.getRunNumber() + "."
									+ printer_id1.substring(0, printer_id1.length() - 8),
									biomodelsim.frame(), printer_track_quantity1, g.getRunNumber());
							for (int i = 2; i < graphSpecies.size(); i++) {
								String index = graphSpecies.get(i);
								ArrayList<Double> index2 = data.get(i);
								int j = i;
								while ((j > 1)
										&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
									graphSpecies.set(j, graphSpecies.get(j - 1));
									data.set(j, data.get(j - 1));
									j = j - 1;
								}
								graphSpecies.set(j, index);
								data.set(j, index2);
							}
							graphData.add(new XYSeries(g.getSpecies()));
							if (data.size() != 0) {
								for (int i = 0; i < (data.get(0)).size(); i++) {
									graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
											(data.get(g.getNumber() + 1)).get(i));
								}
							}
						} else {
							readGraphSpecies(outDir1 + File.separator + "run-1."
									+ printer_id1.substring(0, printer_id1.length() - 8),
									biomodelsim.frame());
							ArrayList<ArrayList<Double>> data = readData(
									outDir1 + File.separator + "run-1."
											+ printer_id1.substring(0, printer_id1.length() - 8),
									biomodelsim.frame(), printer_track_quantity1, g.getRunNumber()
											.toLowerCase());
							for (int i = 2; i < graphSpecies.size(); i++) {
								String index = graphSpecies.get(i);
								ArrayList<Double> index2 = data.get(i);
								int j = i;
								while ((j > 1)
										&& graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
									graphSpecies.set(j, graphSpecies.get(j - 1));
									data.set(j, data.get(j - 1));
									j = j - 1;
								}
								graphSpecies.set(j, index);
								data.set(j, index2);
							}
							graphData.add(new XYSeries(g.getSpecies()));
							if (data.size() != 0) {
								for (int i = 0; i < (data.get(0)).size(); i++) {
									graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
											(data.get(g.getNumber() + 1)).get(i));
								}
							}
						}
					}
					XYSeriesCollection dataset = new XYSeriesCollection();
					for (int i = 0; i < graphData.size(); i++) {
						dataset.addSeries(graphData.get(i));
					}
					fixGraph(title.getText().trim(), x.getText().trim(), y.getText().trim(),
							dataset);
					chart.getXYPlot().setRenderer(rend);
					XYPlot plot = chart.getXYPlot();
					printer_track_quantity1 = y.getText().trim();
					if (resize.isSelected()) {
						resize(dataset);
					} else {
						NumberAxis axis = (NumberAxis) plot.getRangeAxis();
						axis.setAutoTickUnitSelection(false);
						axis.setRange(minY, maxY);
						axis.setTickUnit(new NumberTickUnit(scaleY));
						axis = (NumberAxis) plot.getDomainAxis();
						axis.setAutoTickUnitSelection(false);
						axis.setRange(minX, maxX);
						axis.setTickUnit(new NumberTickUnit(scaleX));
					}
					displayed = false;
					f.dispose();
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selected = "";
					int size = graphed.size();
					for (int i = 0; i < size; i++) {
						graphed.remove();
					}
					for (GraphSpecies g : old) {
						graphed.add(g);
					}
					displayed = false;
					f.dispose();
				}
			});
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(ok);
			buttonPanel.add(cancel);
			JPanel all = new JPanel(new BorderLayout());
			all.add(scroll, "Center");
			all.add(buttonPanel, "South");
			f.setContentPane(all);
			f.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = f.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int xx = screenSize.width / 2 - frameSize.width / 2;
			int yy = screenSize.height / 2 - frameSize.height / 2;
			f.setLocation(xx, yy);
			f.setVisible(true);
			displayed = true;
		}
	}

	private void fixGraph(String title, String x, String y, XYSeriesCollection dataset) {
		chart = ChartFactory.createXYLineChart(title, x, y, dataset, PlotOrientation.VERTICAL,
				true, true, false);
		chart.addProgressListener(this);
		ChartPanel graph = new ChartPanel(chart);
		graph.addMouseListener(this);
		JPanel ButtonHolder = new JPanel();
		exportJPeg = new JButton("Export As JPEG");
		exportPng = new JButton("Export As PNG");
		exportPdf = new JButton("Export As PDF");
		exportEps = new JButton("Export As EPS");
		exportSvg = new JButton("Export As SVG");
		exportJPeg.addActionListener(this);
		exportPng.addActionListener(this);
		exportPdf.addActionListener(this);
		exportEps.addActionListener(this);
		exportSvg.addActionListener(this);
		ButtonHolder.add(exportJPeg);
		ButtonHolder.add(exportPng);
		ButtonHolder.add(exportPdf);
		ButtonHolder.add(exportEps);
		ButtonHolder.add(exportSvg);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ButtonHolder, null);
		splitPane.setDividerSize(0);
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(graph, "Center");
		this.add(splitPane, "South");
		this.revalidate();
	}

	/**
	 * This method saves the graph as a jpeg or as a png file.
	 */
	public void export(int output) {
		try {
			int width = -1;
			int height = -1;
			JPanel sizePanel = new JPanel(new GridLayout(2, 2));
			JLabel heightLabel = new JLabel("Desired pixel height:");
			JLabel widthLabel = new JLabel("Desired pixel width:");
			JTextField heightField = new JTextField("400");
			JTextField widthField = new JTextField("650");
			sizePanel.add(widthLabel);
			sizePanel.add(widthField);
			sizePanel.add(heightLabel);
			sizePanel.add(heightField);
			Object[] options2 = { "Save", "Cancel" };
			int value = JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
					"Enter Size Of File", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options2, options2[0]);
			if (value == JOptionPane.YES_OPTION) {
				while (value == JOptionPane.YES_OPTION && (width == -1 || height == -1))
					try {
						width = Integer.parseInt(widthField.getText().trim());
						height = Integer.parseInt(heightField.getText().trim());
						if (width < 1 || height < 1) {
							JOptionPane.showMessageDialog(biomodelsim.frame(),
									"Width and height must be positive integers!", "Error",
									JOptionPane.ERROR_MESSAGE);
							width = -1;
							height = -1;
							value = JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
									"Enter Size Of File", JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
						}
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(biomodelsim.frame(),
								"Width and height must be positive integers!", "Error",
								JOptionPane.ERROR_MESSAGE);
						width = -1;
						height = -1;
						value = JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
								"Enter Size Of File", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
					}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
			File file;
			if (savedPics != null) {
				file = new File(savedPics);
			} else {
				file = null;
			}
			String filename = Buttons.browse(biomodelsim.frame(), file, null,
					JFileChooser.FILES_ONLY, "Save");
			if (!filename.equals("")) {
				if (output == 0) {
					if (filename.length() < 4) {
						filename += ".jpg";
					} else if (filename.length() < 5
							&& !filename.substring((filename.length() - 4), filename.length())
									.equals(".jpg")) {
						filename += ".jpg";
					} else {
						if (filename.substring((filename.length() - 4), filename.length()).equals(
								".jpg")
								|| filename.substring((filename.length() - 5), filename.length())
										.equals(".jpeg")) {
						} else {
							filename += ".jpg";
						}
					}
				} else if (output == 1) {
					if (filename.length() < 4) {
						filename += ".png";
					} else {
						if (filename.substring((filename.length() - 4), filename.length()).equals(
								".png")) {
						} else {
							filename += ".png";
						}
					}
				} else if (output == 2) {
					if (filename.length() < 4) {
						filename += ".pdf";
					} else {
						if (filename.substring((filename.length() - 4), filename.length()).equals(
								".pdf")) {
						} else {
							filename += ".pdf";
						}
					}
				} else if (output == 3) {
					if (filename.length() < 4) {
						filename += ".eps";
					} else {
						if (filename.substring((filename.length() - 4), filename.length()).equals(
								".eps")) {
						} else {
							filename += ".eps";
						}
					}
				} else if (output == 4) {
					if (filename.length() < 4) {
						filename += ".svg";
					} else {
						if (filename.substring((filename.length() - 4), filename.length()).equals(
								".svg")) {
						} else {
							filename += ".svg";
						}
					}
				}
				file = new File(filename);
				if (file.exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					value = JOptionPane.showOptionDialog(biomodelsim.frame(),
							"File already exists." + " Overwrite?", "File Already Exists",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					if (value == JOptionPane.YES_OPTION) {
						if (output == 0) {
							ChartUtilities.saveChartAsJPEG(file, chart, width, height);
						} else if (output == 1) {
							ChartUtilities.saveChartAsPNG(file, chart, width, height);
						} else if (output == 2) {
							Rectangle pagesize = new Rectangle(width, height);
							Document document = new Document(pagesize, 50, 50, 50, 50);
							PdfWriter writer = PdfWriter.getInstance(document,
									new FileOutputStream(file));
							document.open();
							PdfContentByte cb = writer.getDirectContent();
							PdfTemplate tp = cb.createTemplate(width, height);
							Graphics2D g2 = tp.createGraphics(width, height,
									new DefaultFontMapper());
							chart.draw(g2, new java.awt.Rectangle(width, height));
							g2.dispose();
							cb.addTemplate(tp, 0, 0);
							document.close();
						} else if (output == 3) {
							Graphics2D g = new EpsGraphics2D();
							chart.draw(g, new java.awt.Rectangle(width, height));
							Writer out = new FileWriter(file);
							out.write(g.toString());
							out.close();
						} else if (output == 4) {
							DOMImplementation domImpl = GenericDOMImplementation
									.getDOMImplementation();
							org.w3c.dom.Document document = domImpl.createDocument(null, "svg",
									null);
							SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
							svgGenerator.setSVGCanvasSize(new Dimension(width, height));
							chart.draw(svgGenerator, new java.awt.Rectangle(width, height));
							boolean useCSS = true;
							Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
							svgGenerator.stream(out, useCSS);
							out.close();
						}
						savedPics = filename;
					}
				} else {
					if (output == 0) {
						ChartUtilities.saveChartAsJPEG(file, chart, width, height);
					} else if (output == 1) {
						ChartUtilities.saveChartAsPNG(file, chart, width, height);
					} else if (output == 2) {
						Rectangle pagesize = new Rectangle(width, height);
						Document document = new Document(pagesize, 50, 50, 50, 50);
						PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(
								file));
						document.open();
						PdfContentByte cb = writer.getDirectContent();
						PdfTemplate tp = cb.createTemplate(width, height);
						Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
						chart.draw(g2, new java.awt.Rectangle(width, height));
						g2.dispose();
						cb.addTemplate(tp, 0, 0);
						document.close();
					} else if (output == 3) {
						Graphics2D g = new EpsGraphics2D();
						chart.draw(g, new java.awt.Rectangle(width, height));
						Writer out = new FileWriter(file);
						out.write(g.toString());
						out.close();
					} else if (output == 4) {
						DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
						org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);
						SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
						chart.draw(svgGenerator, new java.awt.Rectangle(width, height));
						boolean useCSS = true;
						Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
						svgGenerator.stream(out, useCSS);
						out.close();
					}
					savedPics = filename;
				}
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Save File!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private ArrayList<ArrayList<Double>> calculateAverageVarianceDeviation(String startFile,
			String fileStem, int choice) {
		InputStream input;
		ArrayList<ArrayList<Double>> average = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> variance = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> deviation = new ArrayList<ArrayList<Double>>();
		try {
			input = new BufferedInputStream(new ProgressMonitorInputStream(biomodelsim.frame(),
					"Reading Reb2sac Output Data From " + new File(startFile).getName(),
					new FileInputStream(new File(startFile))));
			boolean reading = true;
			char cha;
			int readCount = 0;
			while (reading) {
				String word = "";
				boolean readWord = true;
				while (readWord) {
					int read = input.read();
					readCount++;
					if (read == -1) {
						reading = false;
						readWord = false;
					}
					cha = (char) read;
					if (Character.isWhitespace(cha)) {
						input.mark(3);
						char next = (char) input.read();
						char after = (char) input.read();
						String check = "" + next + after;
						if (word.equals("") || word.equals("0") || check.equals("0,")) {
							readWord = false;
						} else {
							word += cha;
						}
						input.reset();
					} else if (cha == ',' || cha == ':' || cha == ';' || cha == '!' || cha == '?'
							|| cha == '\"' || cha == '\'' || cha == '(' || cha == ')' || cha == '{'
							|| cha == '}' || cha == '[' || cha == ']' || cha == '<' || cha == '>'
							|| cha == '*' || cha == '=' || cha == '#') {
						readWord = false;
					} else if (read != -1) {
						word += cha;
					}
				}
				int getNum;
				try {
					getNum = Integer.parseInt(word);
					if (getNum == 0) {
						boolean first = true;
						int runsToMake = 1;
						String[] findNum = startFile.split(File.separator);
						String search = findNum[findNum.length - 1];
						int firstOne = Integer.parseInt(search.substring(4, search.length() - 4));
						for (String f : new File(outDir1).list()) {
							if (f.contains(fileStem)) {
								int tempNum = Integer.parseInt(f.substring(fileStem.length(), f
										.length() - 4));
								if (tempNum > runsToMake) {
									runsToMake = tempNum;
								}
							}
						}
						for (int i = 0; i < graphSpecies.size(); i++) {
							average.add(new ArrayList<Double>());
							variance.add(new ArrayList<Double>());
						}
						(average.get(0)).add(0.0);
						(variance.get(0)).add(0.0);
						int count = 0;
						int skip = firstOne;
						for (int j = 0; j < runsToMake; j++) {
							int counter = 1;
							if (!first) {
								if (firstOne != 1) {
									j--;
									firstOne = 1;
								}
								boolean loop = true;
								while (loop && j < runsToMake && (j + 1) != skip) {
									if (new File(outDir1 + File.separator + fileStem + (j + 1)
											+ "."
											+ printer_id1.substring(0, printer_id1.length() - 8))
											.exists()) {
										input = new BufferedInputStream(
												new ProgressMonitorInputStream(
														biomodelsim.frame(),
														"Reading Reb2sac Output Data From "
																+ new File(
																		outDir1
																				+ File.separator
																				+ fileStem
																				+ (j + 1)
																				+ "."
																				+ printer_id1
																						.substring(
																								0,
																								printer_id1
																										.length() - 8))
																		.getName(),
														new FileInputStream(new File(outDir1
																+ File.separator
																+ fileStem
																+ (j + 1)
																+ "."
																+ printer_id1.substring(0,
																		printer_id1.length() - 8)))));
										for (int i = 0; i < readCount; i++) {
											input.read();
										}
										loop = false;
										count++;
									} else {
										j++;
									}
								}
							}
							reading = true;
							while (reading) {
								word = "";
								readWord = true;
								int read;
								while (readWord) {
									read = input.read();
									cha = (char) read;
									while (!Character.isWhitespace(cha) && cha != ',' && cha != ':'
											&& cha != ';' && cha != '!' && cha != '?'
											&& cha != '\"' && cha != '\'' && cha != '('
											&& cha != ')' && cha != '{' && cha != '}' && cha != '['
											&& cha != ']' && cha != '<' && cha != '>' && cha != '_'
											&& cha != '*' && cha != '=' && read != -1) {
										word += cha;
										read = input.read();
										cha = (char) read;
									}
									if (read == -1) {
										reading = false;
										first = false;
									}
									readWord = false;
								}
								int insert;
								if (!word.equals("")) {
									if (first) {
										if (counter < graphSpecies.size()) {
											insert = counter;
											(average.get(insert)).add(Double.parseDouble(word));
											if (insert == 0) {
												(variance.get(insert))
														.add(Double.parseDouble(word));
											} else {
												(variance.get(insert)).add(0.0);
											}
										} else {
											insert = counter % graphSpecies.size();
											(average.get(insert)).add(Double.parseDouble(word));
											if (insert == 0) {
												(variance.get(insert))
														.add(Double.parseDouble(word));
											} else {
												(variance.get(insert)).add(0.0);
											}
										}
									} else {
										if (counter < graphSpecies.size()) {
											insert = counter;
											try {
												double old = (average.get(insert)).get(insert
														/ graphSpecies.size());
												(average.get(insert))
														.set(
																insert / graphSpecies.size(),
																old
																		+ ((Double
																				.parseDouble(word) - old) / (count + 1)));
												double newMean = (average.get(insert)).get(insert
														/ graphSpecies.size());
												if (insert == 0) {
													(variance.get(insert))
															.set(
																	insert / graphSpecies.size(),
																	old
																			+ ((Double
																					.parseDouble(word) - old) / (count + 1)));
												} else {
													double vary = (((count - 1) * (variance
															.get(insert)).get(insert
															/ graphSpecies.size())) + (Double
															.parseDouble(word) - newMean)
															* (Double.parseDouble(word) - old))
															/ count;
													(variance.get(insert)).set(insert
															/ graphSpecies.size(), vary);
												}
											} catch (Exception e2) {
												(average.get(insert)).add(Double.parseDouble(word));
												if (insert == 0) {
													(variance.get(insert)).add(Double
															.parseDouble(word));
												} else {
													(variance.get(insert)).add(0.0);
												}
											}
										} else {
											insert = counter % graphSpecies.size();
											try {
												double old = (average.get(insert)).get(counter
														/ graphSpecies.size());
												(average.get(insert))
														.set(
																counter / graphSpecies.size(),
																old
																		+ ((Double
																				.parseDouble(word) - old) / (count + 1)));
												double newMean = (average.get(insert)).get(counter
														/ graphSpecies.size());
												if (insert == 0) {
													(variance.get(insert))
															.set(
																	counter / graphSpecies.size(),
																	old
																			+ ((Double
																					.parseDouble(word) - old) / (count + 1)));
												} else {
													double vary = (((count - 1) * (variance
															.get(insert)).get(counter
															/ graphSpecies.size())) + (Double
															.parseDouble(word) - newMean)
															* (Double.parseDouble(word) - old))
															/ count;
													(variance.get(insert)).set(counter
															/ graphSpecies.size(), vary);
												}
											} catch (Exception e2) {
												(average.get(insert)).add(Double.parseDouble(word));
												if (insert == 0) {
													(variance.get(insert)).add(Double
															.parseDouble(word));
												} else {
													(variance.get(insert)).add(0.0);
												}
											}
										}
									}
									counter++;
								}
							}
						}
					}
				} catch (Exception e1) {
				}
			}
			deviation = new ArrayList<ArrayList<Double>>();
			for (int i = 0; i < variance.size(); i++) {
				deviation.add(new ArrayList<Double>());
				for (int j = 0; j < variance.get(i).size(); j++) {
					deviation.get(i).add(variance.get(i).get(j));
				}
			}
			for (int i = 1; i < deviation.size(); i++) {
				for (int j = 0; j < deviation.get(i).size(); j++) {
					deviation.get(i).set(j, Math.sqrt(deviation.get(i).get(j)));
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Error Reading Data!"
					+ "\nThere was an error reading the simulation output data.",
					"Error Reading Data", JOptionPane.ERROR_MESSAGE);
		}
		if (choice == 0) {
			return average;
		} else if (choice == 1) {
			return variance;
		} else {
			return deviation;
		}
	}

	private class ShapeAndPaint {
		private Shape shape;

		private Paint paint;

		private ShapeAndPaint(Shape s, Paint p) {
			shape = s;
			paint = p;
		}

		private Shape getShape() {
			return shape;
		}

		private Paint getPaint() {
			return paint;
		}

		private String getShapeName() {
			Object[] set = shapes.keySet().toArray();
			for (int i = 0; i < set.length; i++) {
				if (shape == shapes.get(set[i])) {
					return (String) set[i];
				}
			}
			return "Unknown Shape";
		}

		private String getPaintName() {
			Object[] set = colors.keySet().toArray();
			for (int i = 0; i < set.length; i++) {
				if (paint == colors.get(set[i])) {
					return (String) set[i];
				}
			}
			return "Unknown Color";
		}
	}

	private class GraphSpecies {
		private ShapeAndPaint sP;

		private boolean filled, visible, connected;

		private String runNumber, species;

		private int number;

		private GraphSpecies(Shape s, Paint p, boolean filled, boolean visible, boolean connected,
				String runNumber, String species, int number) {
			sP = new ShapeAndPaint(s, p);
			this.filled = filled;
			this.visible = visible;
			this.connected = connected;
			this.runNumber = runNumber;
			this.species = species;
			this.number = number;
		}

		private int getNumber() {
			return number;
		}

		private String getSpecies() {
			return species;
		}

		private ShapeAndPaint getShapeAndPaint() {
			return sP;
		}

		private boolean getFilled() {
			return filled;
		}

		private boolean getVisible() {
			return visible;
		}

		private boolean getConnected() {
			return connected;
		}

		private String getRunNumber() {
			return runNumber;
		}
	}
}
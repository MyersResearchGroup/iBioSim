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

	private static final long serialVersionUID = 4350596002373546900L;

	private JFreeChart chart; // Graph of the output data

	private String outDir; // output directory

	private String printer_id; // printer id

	/*
	 * Text fields used to change the graph window
	 */
	private JTextField XMin, XMax, XScale, YMin, YMax, YScale;

	private ArrayList<String> graphSpecies; // names of species in the graph

	private String savedPics; // directory for saved pictures

	private BioSim biomodelsim; // tstubd gui

	private JButton save;

	private JButton exportJPeg, exportPng, exportPdf, exportEps, exportSvg; // buttons

	private HashMap<String, Paint> colors;

	private HashMap<String, Shape> shapes;

	private String selected, lastSelected;

	private LinkedList<GraphSpecies> graphed;

	private JCheckBox resize;

	private boolean displayed;

	private Log log;

	private ArrayList<JCheckBox> boxes;

	private ArrayList<JTextField> series;

	private ArrayList<JComboBox> colorsCombo;

	private ArrayList<JComboBox> shapesCombo;

	private ArrayList<JCheckBox> connected;

	private ArrayList<JCheckBox> visible;

	private ArrayList<JCheckBox> filled;

	private JCheckBox use;

	private JCheckBox connectedLabel;

	private JCheckBox visibleLabel;

	private JCheckBox filledLabel;

	private String graphName;

	/**
	 * Creates a Graph Object from the data given and calls the private graph
	 * helper method.
	 */
	public Graph(String printer_track_quantity, String label, String printer_id, String outDir,
			String time, BioSim biomodelsim, String open, Log log, String graphName) {
		// initializes member variables
		this.log = log;
		if (graphName != null) {
			this.graphName = graphName;
		} else {
			this.graphName = outDir.split(File.separator)[outDir.split(File.separator).length - 1]
					+ ".grf";
		}
		this.outDir = outDir;
		this.printer_id = printer_id;
		this.biomodelsim = biomodelsim;
		XYSeriesCollection data = new XYSeriesCollection();
		displayed = false;

		// graph the output data
		setUpShapesAndColors();
		graphed = new LinkedList<GraphSpecies>();
		selected = "";
		lastSelected = "";
		graph(printer_track_quantity, label, data, time);
		if (open != null) {
			open(open);
		}
	}

	/**
	 * This private helper method calls the private readData method, sets up a
	 * graph frame, and graphs the data.
	 * 
	 * @param dataset
	 * @param time
	 */
	private void graph(String printer_track_quantity, String label, XYSeriesCollection dataset,
			String time) {
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
		save = new JButton("Save");
		exportJPeg = new JButton("Export As JPEG");
		exportPng = new JButton("Export As PNG");
		exportPdf = new JButton("Export As PDF");
		exportEps = new JButton("Export As EPS");
		exportSvg = new JButton("Export As SVG");
		save.addActionListener(this);
		exportJPeg.addActionListener(this);
		exportPng.addActionListener(this);
		exportPdf.addActionListener(this);
		exportEps.addActionListener(this);
		exportSvg.addActionListener(this);
		ButtonHolder.add(save);
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
					} else if (cha == ',' || cha == ':' || cha == ';' || cha == '\"' || cha == '\''
							|| cha == '(' || cha == ')' || cha == '[' || cha == ']') {
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
			String printer_track_quantity, String label, String directory) {
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
			return calculateAverageVarianceDeviation(file, stem, 1, directory);
		} else if (label.contains("deviation")) {
			return calculateAverageVarianceDeviation(file, stem, 2, directory);
		} else if (label.contains("average")) {
			return calculateAverageVarianceDeviation(file, stem, 0, directory);
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
		// if the save button is clicked
		if (e.getSource() == save) {
			save();
		}
		// if the export as jpeg button is clicked
		else if (e.getSource() == exportJPeg) {
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
			String simDirString = outDir.split(File.separator)[outDir.split(File.separator).length - 1];
			final DefaultMutableTreeNode simDir = new DefaultMutableTreeNode(simDirString);
			String[] files = new File(outDir).list();
			for (int i = 1; i < files.length; i++) {
				String index = files[i];
				int j = i;
				while ((j > 0) && files[j - 1].compareToIgnoreCase(index) > 0) {
					files[j] = files[j - 1];
					j = j - 1;
				}
				files[j] = index;
			}
			boolean add = false;
			final ArrayList<String> directories = new ArrayList<String>();
			for (String file : files) {
				if (file.length() > 3
						&& file.substring(file.length() - 4).equals(
								"." + printer_id.substring(0, printer_id.length() - 8))) {
					if (file.contains("run-")) {
						add = true;
					} else {
						simDir
								.add(new DefaultMutableTreeNode(file
										.substring(0, file.length() - 4)));
					}
				} else if (new File(outDir + File.separator + file).isDirectory()) {
					boolean addIt = false;
					for (String getFile : new File(outDir + File.separator + file).list()) {
						if (getFile.length() > 3
								&& getFile.substring(getFile.length() - 4).equals(
										"." + printer_id.substring(0, printer_id.length() - 8))) {
							addIt = true;
						}
					}
					if (addIt) {
						directories.add(file);
						DefaultMutableTreeNode d = new DefaultMutableTreeNode(file);
						boolean add2 = false;
						for (String f : new File(outDir + File.separator + file).list()) {
							if (f.contains(printer_id.substring(0, printer_id.length() - 8))) {
								if (f.contains("run-")) {
									add2 = true;
								} else {
									d
											.add(new DefaultMutableTreeNode(f.substring(0, f
													.length() - 4)));
								}
							}
						}
						if (add2) {
							d.add(new DefaultMutableTreeNode("Average"));
							d.add(new DefaultMutableTreeNode("Variance"));
							d.add(new DefaultMutableTreeNode("Standard Deviation"));
						}
						int run = 1;
						for (String s : new File(outDir + File.separator + file).list()) {
							if (s.length() > 4) {
								String end = "";
								for (int j = 1; j < 5; j++) {
									end = s.charAt(s.length() - j) + end;
								}
								if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv")) {
									if (s.contains("run-")) {
										run = Math.max(run, Integer.parseInt(s.substring(4, s
												.length()
												- end.length())));
									}
								}
							}
						}
						for (int i = 0; i < run; i++) {
							if (new File(outDir + File.separator + file + File.separator + "run-"
									+ (i + 1) + "."
									+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								d.add(new DefaultMutableTreeNode("run-" + (i + 1)));
							}
						}
						simDir.add(d);
					}
				}
			}
			if (add) {
				simDir.add(new DefaultMutableTreeNode("Average"));
				simDir.add(new DefaultMutableTreeNode("Variance"));
				simDir.add(new DefaultMutableTreeNode("Standard Deviation"));
			}
			int run = 1;
			for (String s : new File(outDir).list()) {
				if (s.length() > 4) {
					String end = "";
					for (int j = 1; j < 5; j++) {
						end = s.charAt(s.length() - j) + end;
					}
					if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv")) {
						if (s.contains("run-")) {
							run = Math.max(run, Integer.parseInt(s.substring(4, s.length()
									- end.length())));
						}
					}
				}
			}
			for (int i = 0; i < run; i++) {
				if (new File(outDir + File.separator + "run-" + (i + 1) + "."
						+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					simDir.add(new DefaultMutableTreeNode("run-" + (i + 1)));
				}
			}
			if (simDir.getChildCount() == 0) {
				JOptionPane.showMessageDialog(biomodelsim.frame(), "No data to graph."
						+ "\nPerform some simutations to create some data first.", "No Data",
						JOptionPane.PLAIN_MESSAGE);
			} else {
				final JTree tree = new JTree(simDir);
				for (int i = 0; i < tree.getRowCount(); i++) {
					tree.expandRow(i);
				}
				JScrollPane scrollpane = new JScrollPane();
				scrollpane.getViewport().add(tree);
				final JPanel specPanel = new JPanel();
				final JFrame f = new JFrame("Edit Graph");
				tree.addTreeSelectionListener(new TreeSelectionListener() {
					public void valueChanged(TreeSelectionEvent e) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath()
								.getLastPathComponent();
						if (!directories.contains(node.toString())) {
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
								specPanel.removeAll();
								if (directories.contains(node.getParent().toString())) {
									specPanel.add(fixGraphCoices(node.getParent().toString()));
								} else {
									specPanel.add(fixGraphCoices(""));
								}
								specPanel.revalidate();
								specPanel.repaint();
								for (int i = 0; i < series.size(); i++) {
									series.get(i).setText(graphSpecies.get(i + 1));
								}
								for (int i = 0; i < boxes.size(); i++) {
									boxes.get(i).setSelected(false);
								}
								if (directories.contains(node.getParent().toString())) {
									for (GraphSpecies g : graphed) {
										if (g.getRunNumber().equals(selected)
												&& g.getDirectory().equals(
														node.getParent().toString())) {
											boxes.get(g.getNumber()).setSelected(true);
											series.get(g.getNumber()).setText(g.getSpecies());
											colorsCombo.get(g.getNumber()).setSelectedItem(
													g.getShapeAndPaint().getPaintName());
											shapesCombo.get(g.getNumber()).setSelectedItem(
													g.getShapeAndPaint().getShapeName());
											connected.get(g.getNumber()).setSelected(
													g.getConnected());
											visible.get(g.getNumber()).setSelected(g.getVisible());
											filled.get(g.getNumber()).setSelected(g.getFilled());
										}
									}
								} else {
									for (GraphSpecies g : graphed) {
										if (g.getRunNumber().equals(selected)
												&& g.getDirectory().equals("")) {
											boxes.get(g.getNumber()).setSelected(true);
											series.get(g.getNumber()).setText(g.getSpecies());
											colorsCombo.get(g.getNumber()).setSelectedItem(
													g.getShapeAndPaint().getPaintName());
											shapesCombo.get(g.getNumber()).setSelectedItem(
													g.getShapeAndPaint().getShapeName());
											connected.get(g.getNumber()).setSelected(
													g.getConnected());
											visible.get(g.getNumber()).setSelected(g.getVisible());
											filled.get(g.getNumber()).setSelected(g.getFilled());
										}
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
										s = e.getPath().getLastPathComponent().toString();
										if (directories.contains(node.getParent().toString())) {
											if (s.equals("Average")) {
												s = "(" + node.getParent().toString() + ", "
														+ (char) 967 + ")";
											} else if (s.equals("Variance")) {
												s = "(" + node.getParent().toString() + ", "
														+ (char) 948 + (char) 178 + ")";
											} else if (s.equals("Standard Deviation")) {
												s = "(" + node.getParent().toString() + ", "
														+ (char) 948 + ")";
											} else {
												if (s.contains("-run")) {
													s = s.substring(0, s.length() - 4);
												} else if (s.contains("run-")) {
													s = s.substring(4);
												}
												s = "(" + node.getParent().toString() + ", " + s
														+ ")";
											}
										} else {
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
										colorsCombo.get(i).setSelectedIndex(0);
										shapesCombo.get(i).setSelectedIndex(0);
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
						} else {
							specPanel.removeAll();
							specPanel.revalidate();
							specPanel.repaint();
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
				JPanel editPanel = new JPanel(new BorderLayout());
				editPanel.add(titlePanel, "North");
				editPanel.add(specPanel, "Center");
				editPanel.add(scrollpane, "West");
				scroll.setViewportView(editPanel);
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
						ArrayList<GraphSpecies> unableToGraph = new ArrayList<GraphSpecies>();
						HashMap<String, ArrayList<ArrayList<Double>>> allData = new HashMap<String, ArrayList<ArrayList<Double>>>();
						for (GraphSpecies g : graphed) {
							if (g.getDirectory().equals("")) {
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
									if (new File(outDir + File.separator + g.getRunNumber() + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										readGraphSpecies(outDir + File.separator + g.getRunNumber()
												+ "."
												+ printer_id.substring(0, printer_id.length() - 8),
												biomodelsim.frame());
										ArrayList<ArrayList<Double>> data;
										if (allData.containsKey(g.getRunNumber() + " "
												+ g.getDirectory())) {
											data = allData.get(g.getRunNumber() + " "
													+ g.getDirectory());
										} else {
											data = readData(outDir
													+ File.separator
													+ g.getRunNumber()
													+ "."
													+ printer_id.substring(0,
															printer_id.length() - 8), biomodelsim
													.frame(), y.getText().trim(), g.getRunNumber(),
													null);
											for (int i = 2; i < graphSpecies.size(); i++) {
												String index = graphSpecies.get(i);
												ArrayList<Double> index2 = data.get(i);
												int j = i;
												while ((j > 1)
														&& graphSpecies.get(j - 1)
																.compareToIgnoreCase(index) > 0) {
													graphSpecies.set(j, graphSpecies.get(j - 1));
													data.set(j, data.get(j - 1));
													j = j - 1;
												}
												graphSpecies.set(j, index);
												data.set(j, index2);
											}
											allData.put(g.getRunNumber() + " " + g.getDirectory(),
													data);
										}
										graphData.add(new XYSeries(g.getSpecies()));
										if (data.size() != 0) {
											for (int i = 0; i < (data.get(0)).size(); i++) {
												graphData.get(graphData.size() - 1).add(
														(data.get(0)).get(i),
														(data.get(g.getNumber() + 1)).get(i));
											}
										}
									} else {
										unableToGraph.add(g);
										thisOne--;
									}
								} else {
									boolean ableToGraph = false;
									try {
										for (String s : new File(outDir).list()) {
											if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
												ableToGraph = true;
											}
										}
									} catch (Exception e1) {
										ableToGraph = false;
									}
									if (ableToGraph) {
										int next = 1;
										while (!new File(outDir + File.separator + "run-" + next
												+ "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
											next++;
										}
										readGraphSpecies(outDir + File.separator + "run-" + next
												+ "."
												+ printer_id.substring(0, printer_id.length() - 8),
												biomodelsim.frame());
										ArrayList<ArrayList<Double>> data;
										if (allData.containsKey(g.getRunNumber() + " "
												+ g.getDirectory())) {
											data = allData.get(g.getRunNumber() + " "
													+ g.getDirectory());
										} else {
											data = readData(outDir
													+ File.separator
													+ "run-1."
													+ printer_id.substring(0,
															printer_id.length() - 8), biomodelsim
													.frame(), y.getText().trim(), g.getRunNumber()
													.toLowerCase(), null);
											for (int i = 2; i < graphSpecies.size(); i++) {
												String index = graphSpecies.get(i);
												ArrayList<Double> index2 = data.get(i);
												int j = i;
												while ((j > 1)
														&& graphSpecies.get(j - 1)
																.compareToIgnoreCase(index) > 0) {
													graphSpecies.set(j, graphSpecies.get(j - 1));
													data.set(j, data.get(j - 1));
													j = j - 1;
												}
												graphSpecies.set(j, index);
												data.set(j, index2);
											}
											allData.put(g.getRunNumber() + " " + g.getDirectory(),
													data);
										}
										graphData.add(new XYSeries(g.getSpecies()));
										if (data.size() != 0) {
											for (int i = 0; i < (data.get(0)).size(); i++) {
												graphData.get(graphData.size() - 1).add(
														(data.get(0)).get(i),
														(data.get(g.getNumber() + 1)).get(i));
											}
										}
									} else {
										unableToGraph.add(g);
										thisOne--;
									}
								}
							} else {
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
									if (new File(outDir + File.separator + g.getDirectory()
											+ File.separator + g.getRunNumber() + "."
											+ printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										readGraphSpecies(outDir + File.separator + g.getDirectory()
												+ File.separator + g.getRunNumber() + "."
												+ printer_id.substring(0, printer_id.length() - 8),
												biomodelsim.frame());
										ArrayList<ArrayList<Double>> data;
										if (allData.containsKey(g.getRunNumber() + " "
												+ g.getDirectory())) {
											data = allData.get(g.getRunNumber() + " "
													+ g.getDirectory());
										} else {
											data = readData(outDir
													+ File.separator
													+ g.getDirectory()
													+ File.separator
													+ g.getRunNumber()
													+ "."
													+ printer_id.substring(0,
															printer_id.length() - 8), biomodelsim
													.frame(), y.getText().trim(), g.getRunNumber(),
													g.getDirectory());
											for (int i = 2; i < graphSpecies.size(); i++) {
												String index = graphSpecies.get(i);
												ArrayList<Double> index2 = data.get(i);
												int j = i;
												while ((j > 1)
														&& graphSpecies.get(j - 1)
																.compareToIgnoreCase(index) > 0) {
													graphSpecies.set(j, graphSpecies.get(j - 1));
													data.set(j, data.get(j - 1));
													j = j - 1;
												}
												graphSpecies.set(j, index);
												data.set(j, index2);
											}
											allData.put(g.getRunNumber() + " " + g.getDirectory(),
													data);
										}
										graphData.add(new XYSeries(g.getSpecies()));
										if (data.size() != 0) {
											for (int i = 0; i < (data.get(0)).size(); i++) {
												graphData.get(graphData.size() - 1).add(
														(data.get(0)).get(i),
														(data.get(g.getNumber() + 1)).get(i));
											}
										}
									} else {
										unableToGraph.add(g);
										thisOne--;
									}
								} else {
									boolean ableToGraph = false;
									try {
										for (String s : new File(outDir + File.separator
												+ g.getDirectory()).list()) {
											if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
												ableToGraph = true;
											}
										}
									} catch (Exception e1) {
										ableToGraph = false;
									}
									if (ableToGraph) {
										int next = 1;
										while (!new File(outDir + File.separator + g.getDirectory()
												+ File.separator + "run-" + next + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
											next++;
										}
										readGraphSpecies(outDir + File.separator + g.getDirectory()
												+ File.separator + "run-" + next + "."
												+ printer_id.substring(0, printer_id.length() - 8),
												biomodelsim.frame());
										ArrayList<ArrayList<Double>> data;
										if (allData.containsKey(g.getRunNumber() + " "
												+ g.getDirectory())) {
											data = allData.get(g.getRunNumber() + " "
													+ g.getDirectory());
										} else {
											data = readData(outDir
													+ File.separator
													+ g.getDirectory()
													+ File.separator
													+ "run-1."
													+ printer_id.substring(0,
															printer_id.length() - 8), biomodelsim
													.frame(), y.getText().trim(), g.getRunNumber()
													.toLowerCase(), g.getDirectory());
											for (int i = 2; i < graphSpecies.size(); i++) {
												String index = graphSpecies.get(i);
												ArrayList<Double> index2 = data.get(i);
												int j = i;
												while ((j > 1)
														&& graphSpecies.get(j - 1)
																.compareToIgnoreCase(index) > 0) {
													graphSpecies.set(j, graphSpecies.get(j - 1));
													data.set(j, data.get(j - 1));
													j = j - 1;
												}
												graphSpecies.set(j, index);
												data.set(j, index2);
											}
											allData.put(g.getRunNumber() + " " + g.getDirectory(),
													data);
										}
										graphData.add(new XYSeries(g.getSpecies()));
										if (data.size() != 0) {
											for (int i = 0; i < (data.get(0)).size(); i++) {
												graphData.get(graphData.size() - 1).add(
														(data.get(0)).get(i),
														(data.get(g.getNumber() + 1)).get(i));
											}
										}
									} else {
										unableToGraph.add(g);
										thisOne--;
									}
								}
							}
						}
						for (GraphSpecies g : unableToGraph) {
							graphed.remove(g);
						}
						XYSeriesCollection dataset = new XYSeriesCollection();
						for (int i = 0; i < graphData.size(); i++) {
							dataset.addSeries(graphData.get(i));
						}
						fixGraph(title.getText().trim(), x.getText().trim(), y.getText().trim(),
								dataset);
						chart.getXYPlot().setRenderer(rend);
						XYPlot plot = chart.getXYPlot();
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
				final JButton cancel = new JButton("Cancel");
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
				WindowListener w = new WindowListener() {
					public void windowClosing(WindowEvent arg0) {
						cancel.doClick();
					}

					public void windowOpened(WindowEvent arg0) {
					}

					public void windowClosed(WindowEvent arg0) {
					}

					public void windowIconified(WindowEvent arg0) {
					}

					public void windowDeiconified(WindowEvent arg0) {
					}

					public void windowActivated(WindowEvent arg0) {
					}

					public void windowDeactivated(WindowEvent arg0) {
					}
				};
				f.addWindowListener(w);
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
	}

	private JPanel fixGraphCoices(final String directory) {
		if (directory.equals("")) {
			if (selected.equals("Average") || selected.equals("Variance")
					|| selected.equals("Standard Deviation")) {
				int nextOne = 1;
				while (!new File(outDir + File.separator + "run-" + nextOne + "."
						+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
					nextOne++;
				}
				readGraphSpecies(outDir + File.separator + "run-" + nextOne + "."
						+ printer_id.substring(0, printer_id.length() - 8), biomodelsim.frame());
			} else {
				readGraphSpecies(outDir + File.separator + selected + "."
						+ printer_id.substring(0, printer_id.length() - 8), biomodelsim.frame());
			}
		} else {
			if (selected.equals("Average") || selected.equals("Variance")
					|| selected.equals("Standard Deviation")) {
				int nextOne = 1;
				while (!new File(outDir + File.separator + directory + File.separator + "run-"
						+ nextOne + "." + printer_id.substring(0, printer_id.length() - 8))
						.exists()) {
					nextOne++;
				}
				readGraphSpecies(outDir + File.separator + directory + File.separator + "run-"
						+ nextOne + "." + printer_id.substring(0, printer_id.length() - 8),
						biomodelsim.frame());
			} else {
				readGraphSpecies(outDir + File.separator + directory + File.separator + selected
						+ "." + printer_id.substring(0, printer_id.length() - 8), biomodelsim
						.frame());
			}
		}
		for (int i = 2; i < graphSpecies.size(); i++) {
			String index = graphSpecies.get(i);
			int j = i;
			while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
				graphSpecies.set(j, graphSpecies.get(j - 1));
				j = j - 1;
			}
			graphSpecies.set(j, index);
		}
		JPanel speciesPanel1 = new JPanel(new GridLayout(graphSpecies.size(), 1));
		JPanel speciesPanel2 = new JPanel(new GridLayout(graphSpecies.size(), 3));
		JPanel speciesPanel3 = new JPanel(new GridLayout(graphSpecies.size(), 3));
		use = new JCheckBox("Use");
		JLabel specs = new JLabel("Species");
		JLabel color = new JLabel("Color");
		JLabel shape = new JLabel("Shape");
		connectedLabel = new JCheckBox("Connected");
		visibleLabel = new JCheckBox("Visible");
		filledLabel = new JCheckBox("Filled");
		connectedLabel.setSelected(true);
		visibleLabel.setSelected(true);
		filledLabel.setSelected(true);
		boxes = new ArrayList<JCheckBox>();
		series = new ArrayList<JTextField>();
		colorsCombo = new ArrayList<JComboBox>();
		shapesCombo = new ArrayList<JComboBox>();
		connected = new ArrayList<JCheckBox>();
		visible = new ArrayList<JCheckBox>();
		filled = new ArrayList<JCheckBox>();
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
						if (!box.isSelected()) {
							box.doClick();
						}
					}
				} else {
					for (JCheckBox box : connected) {
						if (box.isSelected()) {
							box.doClick();
						}
					}
				}
			}
		});
		visibleLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (visibleLabel.isSelected()) {
					for (JCheckBox box : visible) {
						if (!box.isSelected()) {
							box.doClick();
						}
					}
				} else {
					for (JCheckBox box : visible) {
						if (box.isSelected()) {
							box.doClick();
						}
					}
				}
			}
		});
		filledLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (filledLabel.isSelected()) {
					for (JCheckBox box : filled) {
						if (!box.isSelected()) {
							box.doClick();
						}
					}
				} else {
					for (JCheckBox box : filled) {
						if (box.isSelected()) {
							box.doClick();
						}
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
						for (int k = 0; k < boxes.size(); k++) {
							if (boxes.get(k).isSelected()) {
								if (colorsCombo.get(k).getSelectedItem().equals("Red")) {
									cols[0]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Blue")) {
									cols[1]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Green")) {
									cols[2]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Yellow")) {
									cols[3]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Magenta")) {
									cols[4]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Cyan")) {
									cols[5]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Tan")) {
									cols[6]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Gray (Dark)")) {
									cols[7]++;
								} else if (colorsCombo.get(k).getSelectedItem()
										.equals("Red (Dark)")) {
									cols[8]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Blue (Dark)")) {
									cols[9]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Green (Dark)")) {
									cols[10]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Dark)")) {
									cols[11]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Dark)")) {
									cols[12]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Cyan (Dark)")) {
									cols[13]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Black")) {
									cols[14]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Red ")) {
									cols[15]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Blue ")) {
									cols[16]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Green ")) {
									cols[17]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Yellow ")) {
									cols[18]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Magenta ")) {
									cols[19]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals("Cyan ")) {
									cols[20]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Gray (Light)")) {
									cols[21]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Red (Extra Dark)")) {
									cols[22]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Blue (Extra Dark)")) {
									cols[23]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Green (Extra Dark)")) {
									cols[24]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Extra Dark)")) {
									cols[25]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Extra Dark)")) {
									cols[26]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Cyan (Extra Dark)")) {
									cols[27]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Red (Light)")) {
									cols[28]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Blue (Light)")) {
									cols[29]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Green (Light)")) {
									cols[30]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Yellow (Light)")) {
									cols[31]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Magenta (Light)")) {
									cols[32]++;
								} else if (colorsCombo.get(k).getSelectedItem().equals(
										"Cyan (Light)")) {
									cols[33]++;
								}
								if (shapesCombo.get(k).getSelectedItem().equals("Square")) {
									shaps[0]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals("Circle")) {
									shaps[1]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals("Triangle")) {
									shaps[2]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals("Diamond")) {
									shaps[3]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals(
										"Rectangle (Horizontal)")) {
									shaps[4]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals(
										"Triangle (Upside Down)")) {
									shaps[5]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals(
										"Circle (Half)")) {
									shaps[6]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals("Arrow")) {
									shaps[7]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals(
										"Rectangle (Vertical)")) {
									shaps[8]++;
								} else if (shapesCombo.get(k).getSelectedItem().equals(
										"Arrow (Backwards)")) {
									shaps[9]++;
								}
							}
						}
						for (GraphSpecies graph : graphed) {
							if (graph.getShapeAndPaint().getPaintName().equals("Red")) {
								cols[0]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Blue")) {
								cols[1]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Green")) {
								cols[2]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Yellow")) {
								cols[3]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Magenta")) {
								cols[4]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Cyan")) {
								cols[5]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Tan")) {
								cols[6]++;
							} else if (graph.getShapeAndPaint().getPaintName()
									.equals("Gray (Dark)")) {
								cols[7]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Red (Dark)")) {
								cols[8]++;
							} else if (graph.getShapeAndPaint().getPaintName()
									.equals("Blue (Dark)")) {
								cols[9]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Green (Dark)")) {
								cols[10]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Yellow (Dark)")) {
								cols[11]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Magenta (Dark)")) {
								cols[12]++;
							} else if (graph.getShapeAndPaint().getPaintName()
									.equals("Cyan (Dark)")) {
								cols[13]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Black")) {
								cols[14]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Red ")) {
								cols[15]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Blue ")) {
								cols[16]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Green ")) {
								cols[17]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Yellow ")) {
								cols[18]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Magenta ")) {
								cols[19]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals("Cyan ")) {
								cols[20]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Gray (Light)")) {
								cols[21]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Red (Extra Dark)")) {
								cols[22]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Blue (Extra Dark)")) {
								cols[23]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Green (Extra Dark)")) {
								cols[24]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Yellow (Extra Dark)")) {
								cols[25]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Magenta (Extra Dark)")) {
								cols[26]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Cyan (Extra Dark)")) {
								cols[27]++;
							} else if (graph.getShapeAndPaint().getPaintName()
									.equals("Red (Light)")) {
								cols[28]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Blue (Light)")) {
								cols[29]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Green (Light)")) {
								cols[30]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Yellow (Light)")) {
								cols[31]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Magenta (Light)")) {
								cols[32]++;
							} else if (graph.getShapeAndPaint().getPaintName().equals(
									"Cyan (Light)")) {
								cols[33]++;
							}
							if (graph.getShapeAndPaint().getShapeName().equals("Square")) {
								shaps[0]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals("Circle")) {
								shaps[1]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals("Triangle")) {
								shaps[2]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals("Diamond")) {
								shaps[3]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals(
									"Rectangle (Horizontal)")) {
								shaps[4]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals(
									"Triangle (Upside Down)")) {
								shaps[5]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals(
									"Circle (Half)")) {
								shaps[6]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals("Arrow")) {
								shaps[7]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals(
									"Rectangle (Vertical)")) {
								shaps[8]++;
							} else if (graph.getShapeAndPaint().getShapeName().equals(
									"Arrow (Backwards)")) {
								shaps[9]++;
							}
						}
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
								colorsCombo.get(i).setSelectedItem(set[j]);
							}
						}
						for (int j = 0; j < shapeSet; j++) {
							draw.getNextShape();
						}
						Shape shape = draw.getNextShape();
						set = shapey.keySet().toArray();
						for (int j = 0; j < set.length; j++) {
							if (shape == shapey.get(set[j])) {
								shapesCombo.get(i).setSelectedItem(set[j]);
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
						graphed.add(new GraphSpecies(shapey.get(shapesCombo.get(i)
								.getSelectedItem()), colory.get(colorsCombo.get(i)
								.getSelectedItem()), filled.get(i).isSelected(), visible.get(i)
								.isSelected(), connected.get(i).isSelected(), selected, series.get(
								i).getText().trim(), i, directory));
					} else {
						use.setSelected(false);
						colorsCombo.get(i).setSelectedIndex(0);
						shapesCombo.get(i).setSelectedIndex(0);
						ArrayList<GraphSpecies> remove = new ArrayList<GraphSpecies>();
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								remove.add(g);
							}
						}
						for (GraphSpecies g : remove) {
							graphed.remove(g);
						}
					}
				}
			});
			boxes.add(temp);
			temp = new JCheckBox();
			temp.setActionCommand("" + i);
			temp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
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
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setVisible(true);
							}
						}
					} else {
						visibleLabel.setSelected(false);
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setVisible(false);
							}
						}
					}
				}
			});
			visible.add(temp);
			visible.get(i).setSelected(true);
			temp = new JCheckBox();
			temp.setActionCommand("" + i);
			temp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
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
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setFilled(true);
							}
						}
					} else {
						filledLabel.setSelected(false);
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setFilled(false);
							}
						}
					}
				}
			});
			filled.add(temp);
			filled.get(i).setSelected(true);
			temp = new JCheckBox();
			temp.setActionCommand("" + i);
			temp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
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
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setConnected(true);
							}
						}
					} else {
						connectedLabel.setSelected(false);
						for (GraphSpecies g : graphed) {
							if (g.getRunNumber().equals(selected) && g.getNumber() == i
									&& g.getDirectory().equals(directory)) {
								g.setConnected(false);
							}
						}
					}
				}
			});
			connected.add(temp);
			connected.get(i).setSelected(true);
			JTextField seriesName = new JTextField(graphSpecies.get(i + 1));
			seriesName.setName("" + i);
			seriesName.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}

				public void keyReleased(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}

				public void keyTyped(KeyEvent e) {
					int i = Integer.parseInt(((JTextField) e.getSource()).getName());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setSpecies(((JTextField) e.getSource()).getText());
						}
					}
				}
			});
			series.add(seriesName);
			Object[] col = this.colors.keySet().toArray();
			Arrays.sort(col);
			Object[] shap = this.shapes.keySet().toArray();
			Arrays.sort(shap);
			JComboBox colBox = new JComboBox(col);
			colBox.setActionCommand("" + i);
			colBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setPaint((String) ((JComboBox) e.getSource()).getSelectedItem());
						}
					}
				}
			});
			JComboBox shapBox = new JComboBox(shap);
			shapBox.setActionCommand("" + i);
			shapBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					for (GraphSpecies g : graphed) {
						if (g.getRunNumber().equals(selected) && g.getNumber() == i
								&& g.getDirectory().equals(directory)) {
							g.setShape((String) ((JComboBox) e.getSource()).getSelectedItem());
						}
					}
				}
			});
			colorsCombo.add(colBox);
			shapesCombo.add(shapBox);
			speciesPanel1.add(boxes.get(i));
			speciesPanel2.add(series.get(i));
			speciesPanel2.add(colorsCombo.get(i));
			speciesPanel2.add(shapesCombo.get(i));
			speciesPanel3.add(connected.get(i));
			speciesPanel3.add(visible.get(i));
			speciesPanel3.add(filled.get(i));
		}
		JPanel speciesPanel = new JPanel(new BorderLayout());
		speciesPanel.add(speciesPanel1, "West");
		speciesPanel.add(speciesPanel2, "Center");
		speciesPanel.add(speciesPanel3, "East");
		return speciesPanel;
	}

	private void fixGraph(String title, String x, String y, XYSeriesCollection dataset) {
		chart = ChartFactory.createXYLineChart(title, x, y, dataset, PlotOrientation.VERTICAL,
				true, true, false);
		chart.addProgressListener(this);
		ChartPanel graph = new ChartPanel(chart);
		if (graphed.isEmpty()) {
			graph.setLayout(new GridLayout(1, 1));
			JLabel edit = new JLabel("Click here to create graph");
			Font font = edit.getFont();
			font = font.deriveFont(Font.BOLD, 42.0f);
			edit.setFont(font);
			edit.setHorizontalAlignment(SwingConstants.CENTER);
			graph.add(edit);
		}
		graph.addMouseListener(this);
		JPanel ButtonHolder = new JPanel();
		save = new JButton("Save");
		exportJPeg = new JButton("Export As JPEG");
		exportPng = new JButton("Export As PNG");
		exportPdf = new JButton("Export As PDF");
		exportEps = new JButton("Export As EPS");
		exportSvg = new JButton("Export As SVG");
		save.addActionListener(this);
		exportJPeg.addActionListener(this);
		exportPng.addActionListener(this);
		exportPdf.addActionListener(this);
		exportEps.addActionListener(this);
		exportSvg.addActionListener(this);
		ButtonHolder.add(save);
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
			Object[] options2 = { "Export", "Cancel" };
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
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Export File!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private ArrayList<ArrayList<Double>> calculateAverageVarianceDeviation(String startFile,
			String fileStem, int choice, String directory) {
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
						if (directory == null) {
							for (String f : new File(outDir).list()) {
								if (f.contains(fileStem)) {
									int tempNum = Integer.parseInt(f.substring(fileStem.length(), f
											.length() - 4));
									if (tempNum > runsToMake) {
										runsToMake = tempNum;
									}
								}
							}
						} else {
							for (String f : new File(outDir + File.separator + directory).list()) {
								if (f.contains(fileStem)) {
									int tempNum = Integer.parseInt(f.substring(fileStem.length(), f
											.length() - 4));
									if (tempNum > runsToMake) {
										runsToMake = tempNum;
									}
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
									if (directory == null) {
										if (new File(outDir + File.separator + fileStem + (j + 1)
												+ "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
											input = new BufferedInputStream(
													new ProgressMonitorInputStream(
															biomodelsim.frame(),
															"Reading Reb2sac Output Data From "
																	+ new File(
																			outDir
																					+ File.separator
																					+ fileStem
																					+ (j + 1)
																					+ "."
																					+ printer_id
																							.substring(
																									0,
																									printer_id
																											.length() - 8))
																			.getName(),
															new FileInputStream(
																	new File(
																			outDir
																					+ File.separator
																					+ fileStem
																					+ (j + 1)
																					+ "."
																					+ printer_id
																							.substring(
																									0,
																									printer_id
																											.length() - 8)))));
											for (int i = 0; i < readCount; i++) {
												input.read();
											}
											loop = false;
											count++;
										} else {
											j++;
										}
									} else {
										if (new File(outDir + File.separator + directory
												+ File.separator + fileStem + (j + 1) + "."
												+ printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
											input = new BufferedInputStream(
													new ProgressMonitorInputStream(
															biomodelsim.frame(),
															"Reading Reb2sac Output Data From "
																	+ new File(
																			outDir
																					+ File.separator
																					+ directory
																					+ File.separator
																					+ fileStem
																					+ (j + 1)
																					+ "."
																					+ printer_id
																							.substring(
																									0,
																									printer_id
																											.length() - 8))
																			.getName(),
															new FileInputStream(
																	new File(
																			outDir
																					+ File.separator
																					+ directory
																					+ File.separator
																					+ fileStem
																					+ (j + 1)
																					+ "."
																					+ printer_id
																							.substring(
																									0,
																									printer_id
																											.length() - 8)))));
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

	public void save() {
		Properties graph = new Properties();
		graph.setProperty("title", chart.getTitle().getText());
		graph.setProperty("x.axis", chart.getXYPlot().getDomainAxis().getLabel());
		graph.setProperty("y.axis", chart.getXYPlot().getRangeAxis().getLabel());
		graph.setProperty("x.min", XMin.getText());
		graph.setProperty("x.max", XMax.getText());
		graph.setProperty("x.scale", XScale.getText());
		graph.setProperty("y.min", YMin.getText());
		graph.setProperty("y.max", YMax.getText());
		graph.setProperty("y.scale", YScale.getText());
		graph.setProperty("auto.resize", "" + resize.isSelected());
		for (int i = 0; i < graphed.size(); i++) {
			graph.setProperty("species.connected." + i, "" + graphed.get(i).getConnected());
			graph.setProperty("species.filled." + i, "" + graphed.get(i).getFilled());
			graph.setProperty("species.number." + i, "" + graphed.get(i).getNumber());
			graph.setProperty("species.run.number." + i, graphed.get(i).getRunNumber());
			graph.setProperty("species.name." + i, graphed.get(i).getSpecies());
			graph.setProperty("species.visible." + i, "" + graphed.get(i).getVisible());
			graph.setProperty("species.paint." + i, graphed.get(i).getShapeAndPaint()
					.getPaintName());
			graph.setProperty("species.shape." + i, graphed.get(i).getShapeAndPaint()
					.getShapeName());
			graph.setProperty("species.directory." + i, graphed.get(i).getDirectory());
		}
		try {
			graph.store(new FileOutputStream(new File(outDir + File.separator + graphName)),
					"Graph Data");
			log.addText("Creating graph file:\n" + outDir + File.separator + graphName + "\n");
		} catch (Exception except) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Save Graph!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void open(String filename) {
		Properties graph = new Properties();
		try {
			graph.load(new FileInputStream(new File(filename)));
			XMin.setText(graph.getProperty("x.min"));
			XMax.setText(graph.getProperty("x.max"));
			XScale.setText(graph.getProperty("x.scale"));
			YMin.setText(graph.getProperty("y.min"));
			YMax.setText(graph.getProperty("y.max"));
			YScale.setText(graph.getProperty("y.scale"));
			chart.setTitle(graph.getProperty("title"));
			chart.getXYPlot().getDomainAxis().setLabel(graph.getProperty("x.axis"));
			chart.getXYPlot().getRangeAxis().setLabel(graph.getProperty("y.axis"));
			if (graph.getProperty("auto.resize").equals("true")) {
				resize.setSelected(true);
			} else {
				resize.setSelected(false);
			}
			int next = 0;
			while (graph.containsKey("species.name." + next)) {
				boolean connected, filled, visible;
				if (graph.getProperty("species.connected." + next).equals("true")) {
					connected = true;
				} else {
					connected = false;
				}
				if (graph.getProperty("species.filled." + next).equals("true")) {
					filled = true;
				} else {
					filled = false;
				}
				if (graph.getProperty("species.visible." + next).equals("true")) {
					visible = true;
				} else {
					visible = false;
				}
				graphed.add(new GraphSpecies(
						shapes.get(graph.getProperty("species.shape." + next)), colors.get(graph
								.getProperty("species.paint." + next)), filled, visible, connected,
						graph.getProperty("species.run.number." + next), graph
								.getProperty("species.name." + next), Integer.parseInt(graph
								.getProperty("species.number." + next)), graph
								.getProperty("species.directory." + next)));
				next++;
			}
			refresh();
		} catch (Exception except) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Load Graph!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void refresh() {
		double minY = 0;
		double maxY = 0;
		double scaleY = 0;
		double minX = 0;
		double maxX = 0;
		double scaleX = 0;
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
		}
		ArrayList<XYSeries> graphData = new ArrayList<XYSeries>();
		XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
		int thisOne = -1;
		for (int i = 1; i < graphed.size(); i++) {
			GraphSpecies index = graphed.get(i);
			int j = i;
			while ((j > 0)
					&& (graphed.get(j - 1).getSpecies().compareToIgnoreCase(index.getSpecies()) > 0)) {
				graphed.set(j, graphed.get(j - 1));
				j = j - 1;
			}
			graphed.set(j, index);
		}
		ArrayList<GraphSpecies> unableToGraph = new ArrayList<GraphSpecies>();
		HashMap<String, ArrayList<ArrayList<Double>>> allData = new HashMap<String, ArrayList<ArrayList<Double>>>();
		for (GraphSpecies g : graphed) {
			if (g.getDirectory().equals("")) {
				thisOne++;
				rend.setSeriesVisible(thisOne, true);
				rend.setSeriesLinesVisible(thisOne, g.getConnected());
				rend.setSeriesShapesFilled(thisOne, g.getFilled());
				rend.setSeriesShapesVisible(thisOne, g.getVisible());
				rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
				rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape());
				if (!g.getRunNumber().equals("Average") && !g.getRunNumber().equals("Variance")
						&& !g.getRunNumber().equals("Standard Deviation")) {
					if (new File(outDir + File.separator + g.getRunNumber() + "."
							+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
						readGraphSpecies(outDir + File.separator + g.getRunNumber() + "."
								+ printer_id.substring(0, printer_id.length() - 8), biomodelsim
								.frame());
						ArrayList<ArrayList<Double>> data;
						if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
							data = allData.get(g.getRunNumber() + " " + g.getDirectory());
						} else {
							data = readData(outDir + File.separator + g.getRunNumber() + "."
									+ printer_id.substring(0, printer_id.length() - 8), biomodelsim
									.frame(), chart.getXYPlot().getRangeAxis().getLabel(), g
									.getRunNumber(), null);
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
							allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
						}
						if (g.getNumber() + 1 < graphSpecies.size()) {
							graphData.add(new XYSeries(g.getSpecies()));
							if (data.size() != 0) {
								for (int i = 0; i < (data.get(0)).size(); i++) {
									graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
											(data.get(g.getNumber() + 1)).get(i));
								}
							}
						} else {
							unableToGraph.add(g);
							thisOne--;
						}
					} else {
						unableToGraph.add(g);
						thisOne--;
					}
				} else {
					boolean ableToGraph = false;
					try {
						for (String s : new File(outDir).list()) {
							if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
								ableToGraph = true;
							}
						}
					} catch (Exception e) {
						ableToGraph = false;
					}
					if (ableToGraph) {
						int nextOne = 1;
						while (!new File(outDir + File.separator + "run-" + nextOne + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
							nextOne++;
						}
						readGraphSpecies(outDir + File.separator + "run-" + nextOne + "."
								+ printer_id.substring(0, printer_id.length() - 8), biomodelsim
								.frame());
						ArrayList<ArrayList<Double>> data;
						if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
							data = allData.get(g.getRunNumber() + " " + g.getDirectory());
						} else {
							data = readData(outDir + File.separator + "run-1."
									+ printer_id.substring(0, printer_id.length() - 8), biomodelsim
									.frame(), chart.getXYPlot().getRangeAxis().getLabel(), g
									.getRunNumber().toLowerCase(), null);
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
							allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
						}
						if (g.getNumber() + 1 < graphSpecies.size()) {
							graphData.add(new XYSeries(g.getSpecies()));
							if (data.size() != 0) {
								for (int i = 0; i < (data.get(0)).size(); i++) {
									graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
											(data.get(g.getNumber() + 1)).get(i));
								}
							}
						} else {
							unableToGraph.add(g);
							thisOne--;
						}
					} else {
						unableToGraph.add(g);
						thisOne--;
					}
				}
			} else {
				thisOne++;
				rend.setSeriesVisible(thisOne, true);
				rend.setSeriesLinesVisible(thisOne, g.getConnected());
				rend.setSeriesShapesFilled(thisOne, g.getFilled());
				rend.setSeriesShapesVisible(thisOne, g.getVisible());
				rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
				rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape());
				if (!g.getRunNumber().equals("Average") && !g.getRunNumber().equals("Variance")
						&& !g.getRunNumber().equals("Standard Deviation")) {
					if (new File(outDir + File.separator + g.getDirectory() + File.separator
							+ g.getRunNumber() + "."
							+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
						readGraphSpecies(outDir + File.separator + g.getDirectory()
								+ File.separator + g.getRunNumber() + "."
								+ printer_id.substring(0, printer_id.length() - 8), biomodelsim
								.frame());
						ArrayList<ArrayList<Double>> data;
						if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
							data = allData.get(g.getRunNumber() + " " + g.getDirectory());
						} else {
							data = readData(outDir + File.separator + g.getDirectory()
									+ File.separator + g.getRunNumber() + "."
									+ printer_id.substring(0, printer_id.length() - 8), biomodelsim
									.frame(), chart.getXYPlot().getRangeAxis().getLabel(), g
									.getRunNumber(), g.getDirectory());
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
							allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
						}
						if (g.getNumber() + 1 < graphSpecies.size()) {
							graphData.add(new XYSeries(g.getSpecies()));
							if (data.size() != 0) {
								for (int i = 0; i < (data.get(0)).size(); i++) {
									graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
											(data.get(g.getNumber() + 1)).get(i));
								}
							}
						} else {
							unableToGraph.add(g);
							thisOne--;
						}
					} else {
						unableToGraph.add(g);
						thisOne--;
					}
				} else {
					boolean ableToGraph = false;
					try {
						for (String s : new File(outDir + File.separator + g.getDirectory()).list()) {
							if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
								ableToGraph = true;
							}
						}
					} catch (Exception e) {
						ableToGraph = false;
					}
					if (ableToGraph) {
						int nextOne = 1;
						while (!new File(outDir + File.separator + g.getDirectory()
								+ File.separator + "run-" + nextOne + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
							nextOne++;
						}
						readGraphSpecies(outDir + File.separator + g.getDirectory()
								+ File.separator + "run-" + nextOne + "."
								+ printer_id.substring(0, printer_id.length() - 8), biomodelsim
								.frame());
						ArrayList<ArrayList<Double>> data;
						if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
							data = allData.get(g.getRunNumber() + " " + g.getDirectory());
						} else {
							data = readData(outDir + File.separator + g.getDirectory()
									+ File.separator + "run-1."
									+ printer_id.substring(0, printer_id.length() - 8), biomodelsim
									.frame(), chart.getXYPlot().getRangeAxis().getLabel(), g
									.getRunNumber().toLowerCase(), g.getDirectory());
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
							allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
						}
						if (g.getNumber() + 1 < graphSpecies.size()) {
							graphData.add(new XYSeries(g.getSpecies()));
							if (data.size() != 0) {
								for (int i = 0; i < (data.get(0)).size(); i++) {
									graphData.get(graphData.size() - 1).add((data.get(0)).get(i),
											(data.get(g.getNumber() + 1)).get(i));
								}
							}
						} else {
							unableToGraph.add(g);
							thisOne--;
						}
					} else {
						unableToGraph.add(g);
						thisOne--;
					}
				}
			}
		}
		for (GraphSpecies g : unableToGraph) {
			graphed.remove(g);
		}
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i = 0; i < graphData.size(); i++) {
			dataset.addSeries(graphData.get(i));
		}
		fixGraph(chart.getTitle().getText(), chart.getXYPlot().getDomainAxis().getLabel(), chart
				.getXYPlot().getRangeAxis().getLabel(), dataset);
		chart.getXYPlot().setRenderer(rend);
		XYPlot plot = chart.getXYPlot();
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

		public void setPaint(String paint) {
			this.paint = colors.get(paint);
		}

		public void setShape(String shape) {
			this.shape = shapes.get(shape);
		}
	}

	private class GraphSpecies {
		private ShapeAndPaint sP;

		private boolean filled, visible, connected;

		private String runNumber, species, directory;

		private int number;

		private GraphSpecies(Shape s, Paint p, boolean filled, boolean visible, boolean connected,
				String runNumber, String species, int number, String directory) {
			sP = new ShapeAndPaint(s, p);
			this.filled = filled;
			this.visible = visible;
			this.connected = connected;
			this.runNumber = runNumber;
			this.species = species;
			this.number = number;
			this.directory = directory;
		}

		private void setSpecies(String species) {
			this.species = species;
		}

		private void setPaint(String paint) {
			sP.setPaint(paint);
		}

		private void setShape(String shape) {
			sP.setShape(shape);
		}

		private void setVisible(boolean b) {
			visible = b;
		}

		private void setFilled(boolean b) {
			filled = b;
		}

		private void setConnected(boolean b) {
			connected = b;
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

		private String getDirectory() {
			return directory;
		}
	}

	public void setDirectory(String newDirectory) {
		outDir = newDirectory;
	}

	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}
}
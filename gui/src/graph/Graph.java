package graph.core.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;

import reb2sac.core.gui.Reb2Sac;
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

	// private JFrame graphFrame; // Frame used for graphing

	private Component component; // the main gui's component

	/*
	 * Buttons used in graph window
	 */
	private JButton next, changeSize, addAll, removeAll;

	/*
	 * ArrayList of Check Boxes used to graph output data
	 */
	private ArrayList<JCheckBox> boxes;

	private JCheckBox resize; // Auto resize check box

	private JFreeChart chart; // Graph of the output data

	private int run; // total number of runs

	private String printer_track_quantity1; // label for y-axis on chart

	private String sim1; // simulator used

	private String outDir1; // output directory

	private String printer_id1; // printer id

	private JComboBox select; // combo box for selecting next

	/*
	 * radio button used to determine which abstraction is running
	 */
	private JRadioButton monteCarlo;

	/*
	 * Text fields used to change the graph window
	 */
	private JTextField XMin, XMax, XScale, YMin, YMax, YScale;

	private ArrayList<double[]> maxAndMin; // Arraylist of max and min values

	private ArrayList<ArrayList<Double>> data, variance, deviation; // graph

	// data

	private ArrayList<ArrayList<Double>> average = null; // average of data

	private ArrayList<String> graphSpecies; // names of species in the graph

	private JCheckBox shapes; // check box for visible shapes

	private JCheckBox filled; // check box for filled in shapes

	// private int height = 0; // frame height

	// private int width = 0; // frame width

	private String[] intSpecies; // interesting species

	private String savedPics; // directory for saved pictures

	private String time; // label for x-axis on chart

	private XYSeriesCollection dataset; // dataset of data

	private JCheckBox keep; // check box for keeping data on graph

	private BioModelSim biomodelsim; // tstubd gui

	private JButton save, exportJPeg, exportPng, duplicate; // buttons

	private Reb2Sac reb2sac;

	/**
	 * Creates a Graph Object from the data given and calls the private graph
	 * helper method.
	 */
	public Graph(String file, Component component, String printer_track_quantity, String label,
			JRadioButton monteCarlo, String sim, String printer_id, String outDir, int run,
			String[] intSpecies, int readIn, XYSeriesCollection dataset, String time,
			BioModelSim biomodelsim, Reb2Sac sac) {
		// initializes member variables
		reb2sac = sac;
		this.sim1 = sim;
		this.run = run;
		this.component = component;
		this.printer_track_quantity1 = printer_track_quantity;
		this.outDir1 = outDir;
		this.printer_id1 = printer_id;
		this.monteCarlo = monteCarlo;
		this.intSpecies = intSpecies;
		this.time = time;
		this.biomodelsim = biomodelsim;
		if (dataset == null) {
			this.dataset = new XYSeriesCollection();
		} else {
			this.dataset = dataset;
		}

		// graph the output data
		graph(file, component, printer_track_quantity, label, intSpecies, readIn);
	}

	/**
	 * This private helper method calls the private readData method, sets up a
	 * graph frame, and graphs the data.
	 */
	private void graph(String file, Component component, String printer_track_quantity,
			String label, String[] intSpecies, int readIn) {
		// reads output data
		if (average != null) {
			readData(file, component, printer_track_quantity, label, 2);
		} else {
			readData(file, component, printer_track_quantity, label, 1);
			boxes = new ArrayList<JCheckBox>();
			for (int i = 1; i < graphSpecies.size(); i++) {
				boxes.add(new JCheckBox(graphSpecies.get(i)));
				boxes.get(i - 1).setSelected(false);
				for (int j = 0; j < intSpecies.length; j++) {
					if (graphSpecies.get(i).equals(intSpecies[j])) {
						boxes.get(i - 1).setSelected(true);
					}
				}
				keep = new JCheckBox("Keep Graphed Data");
				if (dataset.getSeriesCount() != 0) {
					keep.setSelected(true);
				} else {
					keep.setSelected(false);
				}
				shapes = new JCheckBox("Visible Shapes");
				shapes.setSelected(true);
				filled = new JCheckBox("Filled In Shapes");
				filled.setSelected(true);
			}
		}
		if (readIn != -1) {
			file = outDir1 + File.separator + "run-" + readIn + "."
					+ printer_id1.substring(0, printer_id1.length() - 8);
			label = sim1 + " run-" + readIn + " simulation results";
			readData(file, component, printer_track_quantity, label, 2);
		}

		// stores data into a dataset
		ArrayList<XYSeries> graphData = new ArrayList<XYSeries>();
		for (int i = 1; i < graphSpecies.size(); i++) {
			graphData.add(new XYSeries(graphSpecies.get(i)));
		}
		for (int i = 0; i < (data.get(0)).size(); i++) {
			for (int j = 1; j < graphSpecies.size(); j++) {
				graphData.get(j - 1).add((data.get(0)).get(i), (data.get(j)).get(i));
			}
		}
		int kept = 0;
		if (!keep.isSelected()) {
			dataset = new XYSeriesCollection();
		} else {
			kept = dataset.getSeriesCount();
		}
		for (int i = 0; i < graphData.size(); i++) {
			dataset.addSeries(graphData.get(i));
		}
		if (keep.isSelected()) {
			boxes = new ArrayList<JCheckBox>();
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				boxes.add(new JCheckBox(dataset.getSeries(i).getKey().toString()));
				boxes.get(i).setSelected(false);
			}
			for (int i = 0; i < kept; i++) {
				boxes.get(i).setSelected(true);
			}
		}
		if (boxes.size() > dataset.getSeriesCount()) {
			boxes = new ArrayList<JCheckBox>();
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				boxes.add(new JCheckBox(dataset.getSeries(i).getKey().toString()));
				boxes.get(i).setSelected(false);
			}
		}

		// creates the graph from the dataset and adds it to a chart panel
		chart = ChartFactory.createXYLineChart(label, time, printer_track_quantity, dataset,
				PlotOrientation.VERTICAL, true, true, false);
		chart.addProgressListener(this);
		XYPlot plot = chart.getXYPlot();
		XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) plot.getRenderer();
		if (filled.isSelected()) {
			rend.setShapesFilled(true);
		} else {
			rend.setShapesFilled(false);
		}
		if (shapes.isSelected()) {
			rend.setShapesVisible(true);
		} else {
			rend.setShapesVisible(false);
		}
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			if (boxes.get(i).isSelected()) {
				rend.setSeriesVisible(i, true);
			} else {
				rend.setSeriesVisible(i, false);
			}
		}
		ChartPanel graph = new ChartPanel(chart);
		graph.addMouseListener(this);

		// creates check boxes for species
		ArrayList<JPanel> checkBoxes = new ArrayList<JPanel>();
		// JPanel display = new JPanel(new BorderLayout());
		checkBoxes.add(new JPanel());
		int check = 0;
		for (int i = 0; i < boxes.size(); i++) {
			if (i != 0 && (i % 10) == 0) {
				checkBoxes.add(new JPanel());
				check++;
			}
			boxes.get(i).addActionListener(this);
			boxes.get(i).setActionCommand("button" + i);
			checkBoxes.get(check).add(boxes.get(i));
		}

		// creates the resize and filled check box
		resize = new JCheckBox("Auto Resize");
		resize.setSelected(true);
		resize.addActionListener(this);
		filled.addActionListener(this);
		shapes.addActionListener(this);

		// creates text fields for changing the graph's dimensions
		JPanel changeHolder1 = new JPanel();
		JPanel changeHolder2 = new JPanel();
		JLabel xMin = new JLabel("X-Min:");
		JLabel xMax = new JLabel("X-Max:");
		JLabel xScale = new JLabel("X-Step:");
		JLabel yMin = new JLabel("Y-Min:");
		JLabel yMax = new JLabel("Y-Max:");
		JLabel yScale = new JLabel("Y-Step:");
		XMin = new JTextField();
		XMax = new JTextField();
		XScale = new JTextField();
		YMin = new JTextField();
		YMax = new JTextField();
		YScale = new JTextField();
		changeSize = new JButton("Change Dimensions");
		addAll = new JButton("Select All");
		removeAll = new JButton("Deselect All");
		changeSize.addActionListener(this);
		addAll.addActionListener(this);
		removeAll.addActionListener(this);
		XMin.setPreferredSize(new java.awt.Dimension(200, 20));
		XMax.setPreferredSize(new java.awt.Dimension(200, 20));
		XScale.setPreferredSize(new java.awt.Dimension(200, 20));
		YMin.setPreferredSize(new java.awt.Dimension(200, 20));
		YMax.setPreferredSize(new java.awt.Dimension(200, 20));
		YScale.setPreferredSize(new java.awt.Dimension(200, 20));
		changeHolder1.add(xMin);
		changeHolder1.add(XMin);
		changeHolder1.add(xMax);
		changeHolder1.add(XMax);
		changeHolder1.add(xScale);
		changeHolder1.add(XScale);
		changeHolder2.add(yMin);
		changeHolder2.add(YMin);
		changeHolder2.add(yMax);
		changeHolder2.add(YMax);
		changeHolder2.add(yScale);
		changeHolder2.add(YScale);
		JPanel inputHolder = new JPanel(new BorderLayout());
		JPanel allCheckBoxes = new JPanel(new GridLayout(checkBoxes.size(), 1));
		for (int i = 0; i < checkBoxes.size(); i++) {
			allCheckBoxes.add(checkBoxes.get(i));
		}
		inputHolder.add(allCheckBoxes, "North");
		inputHolder.add(changeHolder1, "Center");
		inputHolder.add(changeHolder2, "South");

		// creates combo box for choosing which run to display
		String[] add = new String[run + 3];
		add[0] = "Average";
		add[1] = "Variance";
		add[2] = "Standard Deviation";
		for (int i = 0; i < run; i++) {
			add[i + 3] = "" + (i + 1);
		}
		JLabel selectLabel = new JLabel("Select Next:");
		if (monteCarlo.isSelected()) {
			select = new JComboBox(add);
			if (label.contains("variance")) {
				select.setSelectedItem("Variance");
			} else if (label.contains("deviation")) {
				select.setSelectedItem("Standard Deviation");
			} else if (label.contains("average")) {
				select.setSelectedItem("Average");
			} else {
				String selected = "";
				for (int i = 0; i < label.length(); i++) {
					if (Character.isDigit(label.charAt(i))) {
						selected += label.charAt(i);
					}
				}
				select.setSelectedItem(selected);
			}
			next = new JButton("View Next");
			next.addActionListener(this);
		}

		// creates the buttons for the graph frame
		JPanel ButtonHolder = new JPanel();
		JPanel SpecialButtonHolder = new JPanel();
		SpecialButtonHolder.add(keep);
		SpecialButtonHolder.add(shapes);
		SpecialButtonHolder.add(filled);
		SpecialButtonHolder.add(resize);
		SpecialButtonHolder.add(changeSize);
		save = new JButton("Save");
		exportJPeg = new JButton("Export As JPEG");
		exportPng = new JButton("Export As PNG");
		duplicate = new JButton("Duplicate");
		save.addActionListener(this);
		exportJPeg.addActionListener(this);
		exportPng.addActionListener(this);
		duplicate.addActionListener(this);
		// ***ButtonHolder.add(save);***
		ButtonHolder.add(exportJPeg);
		ButtonHolder.add(exportPng);
		// ***ButtonHolder.add(duplicate);***
		JPanel AllButtonsHolder = new JPanel(new BorderLayout());

		// puts all the components of the graph gui into a display panel
		JPanel monteCarloHolder = new JPanel();
		JPanel monteSpecialHolder = new JPanel(new BorderLayout());
		monteSpecialHolder.add(SpecialButtonHolder, "North");
		monteCarloHolder.add(addAll);
		monteCarloHolder.add(removeAll);
		if (monteCarlo.isSelected()) {
			monteCarloHolder.add(selectLabel);
			monteCarloHolder.add(select);
			monteCarloHolder.add(next);
		}
		monteSpecialHolder.add(monteCarloHolder, "South");
		AllButtonsHolder.add(inputHolder, "North");
		AllButtonsHolder.add(monteSpecialHolder, "Center");
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ButtonHolder, null);
		splitPane.setDividerSize(0);
		AllButtonsHolder.add(splitPane, "South");
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(graph, "Center");
		this.add(AllButtonsHolder, "South");

		// determines maximum and minimum values and resizes
		determineMaxAndMin();
		resize();
		this.revalidate();

		/*
		 * // creates the graph frame, adds the display panel, and displays it
		 * graphFrame = new JFrame("Simulation Results"); WindowListener w = new
		 * WindowListener() { public void windowClosing(WindowEvent arg0) {
		 * graphFrame.dispose(); }
		 * 
		 * public void windowOpened(WindowEvent arg0) { }
		 * 
		 * public void windowClosed(WindowEvent arg0) { }
		 * 
		 * public void windowIconified(WindowEvent arg0) { }
		 * 
		 * public void windowDeiconified(WindowEvent arg0) { }
		 * 
		 * public void windowActivated(WindowEvent arg0) { }
		 * 
		 * public void windowDeactivated(WindowEvent arg0) { } };
		 * graphFrame.addWindowListener(w); graphFrame.setContentPane(display);
		 * graphFrame.setJMenuBar(menuBar); graphFrame.pack(); if (height == 0 &&
		 * width == 0) { height = graphFrame.getHeight(); width =
		 * graphFrame.getWidth(); } graphFrame.setSize(width, height); Dimension
		 * screenSize; try { Toolkit tk = Toolkit.getDefaultToolkit();
		 * screenSize = tk.getScreenSize(); } catch (AWTError awe) { screenSize =
		 * new Dimension(640, 480); } Dimension frameSize =
		 * graphFrame.getSize();
		 * 
		 * if (frameSize.height > screenSize.height) { frameSize.height =
		 * screenSize.height; } if (frameSize.width > screenSize.width) {
		 * frameSize.width = screenSize.width; } int x = screenSize.width / 2 -
		 * frameSize.width / 2; int y = screenSize.height / 2 - frameSize.height /
		 * 2; graphFrame.setLocation(x, y); graphFrame.setVisible(true);
		 */
	}

	/**
	 * This private helper method parses the output file of ODE, monte carlo,
	 * and markov abstractions.
	 */
	private void readData(String file, Component component, String printer_track_quantity,
			String label, int num) {
		if (label.contains("variance")) {
			data = variance;
		} else if (label.contains("deviation")) {

			data = deviation;
		} else if (label.contains("average") && num != 1) {
			data = average;
		} else {
			InputStream input;
			component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				input = new BufferedInputStream(new ProgressMonitorInputStream(component,
						"Reading Reb2sac Output Data From " + new File(file).getName(),
						new FileInputStream(new File(file))));
				graphSpecies = new ArrayList<String>();
				data = new ArrayList<ArrayList<Double>>();
				if (label.contains("average")) {
					variance = new ArrayList<ArrayList<Double>>();
				}
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
							boolean first = true;
							int runsToMake;
							if (label.contains("average")) {
								runsToMake = run;
							} else {
								runsToMake = 1;
							}
							for (int i = 0; i < graphSpecies.size(); i++) {
								data.add(new ArrayList<Double>());
								if (label.contains("average")) {
									variance.add(new ArrayList<Double>());
								}
							}
							(data.get(0)).add(0.0);
							if (label.contains("average")) {
								(variance.get(0)).add(0.0);
							}
							for (int j = 0; j < runsToMake; j++) {
								int counter = 1;
								if (!first) {
									input = new BufferedInputStream(new ProgressMonitorInputStream(
											component, "Reading Reb2sac Output Data From "
													+ new File(file.substring(0, file.length() - 5)
															+ (j + 1)
															+ "."
															+ printer_id1.substring(0, printer_id1
																	.length() - 8)).getName(),
											new FileInputStream(new File(file.substring(0, file
													.length() - 5)
													+ (j + 1)
													+ "."
													+ printer_id1.substring(0,
															printer_id1.length() - 8)))));
									for (int i = 0; i < readCount; i++) {
										input.read();
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
										while (!Character.isWhitespace(cha) && cha != ','
												&& cha != ':' && cha != ';' && cha != '!'
												&& cha != '?' && cha != '\"' && cha != '\''
												&& cha != '(' && cha != ')' && cha != '{'
												&& cha != '}' && cha != '[' && cha != ']'
												&& cha != '<' && cha != '>' && cha != '_'
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
												(data.get(insert)).add(Double.parseDouble(word));
												if (label.contains("average")) {
													if (insert == 0) {
														(variance.get(insert)).add(Double
																.parseDouble(word));
													} else {
														(variance.get(insert)).add(0.0);
													}
												}
											} else {
												insert = counter % graphSpecies.size();
												(data.get(insert)).add(Double.parseDouble(word));
												if (label.contains("average")) {
													if (insert == 0) {
														(variance.get(insert)).add(Double
																.parseDouble(word));
													} else {
														(variance.get(insert)).add(0.0);
													}
												}
											}
										} else {
											if (counter < graphSpecies.size()) {
												insert = counter;
												double old = (data.get(insert)).get(insert
														/ graphSpecies.size());
												(data.get(insert))
														.set(
																insert / graphSpecies.size(),
																old
																		+ ((Double
																				.parseDouble(word) - old) / (j + 1)));
												double newMean = (data.get(insert)).get(insert
														/ graphSpecies.size());
												if (label.contains("average")) {
													if (insert == 0) {
														(variance.get(insert))
																.set(
																		insert
																				/ graphSpecies
																						.size(),
																		old
																				+ ((Double
																						.parseDouble(word) - old) / (j + 1)));
													} else {
														double vary = (((j - 1) * (variance
																.get(insert)).get(insert
																/ graphSpecies.size())) + (Double
																.parseDouble(word) - newMean)
																* (Double.parseDouble(word) - old))
																/ j;
														(variance.get(insert)).set(insert
																/ graphSpecies.size(), vary);
													}
												}
											} else {
												insert = counter % graphSpecies.size();
												double old = (data.get(insert)).get(counter
														/ graphSpecies.size());
												(data.get(insert))
														.set(
																counter / graphSpecies.size(),
																old
																		+ ((Double
																				.parseDouble(word) - old) / (j + 1)));
												double newMean = (data.get(insert)).get(counter
														/ graphSpecies.size());
												if (label.contains("average")) {
													if (insert == 0) {
														(variance.get(insert))
																.set(
																		counter
																				/ graphSpecies
																						.size(),
																		old
																				+ ((Double
																						.parseDouble(word) - old) / (j + 1)));
													} else {
														double vary = (((j - 1) * (variance
																.get(insert)).get(counter
																/ graphSpecies.size())) + (Double
																.parseDouble(word) - newMean)
																* (Double.parseDouble(word) - old))
																/ j;
														(variance.get(insert)).set(counter
																/ graphSpecies.size(), vary);
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
						if (word.equals("")) {

						} else {
							graphSpecies.add(word);
						}
					}
				}
				if (label.contains("average")) {
					average = data;
					deviation = variance;
					for (int i = 0; i < deviation.size(); i++) {
						for (int j = 1; j < deviation.get(i).size(); j++) {
							double get = deviation.get(i).get(j);
							double srt = Math.sqrt(get);
							deviation.get(i).set(j, srt);
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
	}

	/**
	 * This method adds and removes plots from the graph depending on what check
	 * boxes are selected.
	 */
	public void actionPerformed(ActionEvent e) {
		// if the next button is clicked
		if (e.getSource() == next) {
			// height = graphFrame.getHeight();
			// width = graphFrame.getWidth();
			// graphFrame.dispose();
			if (monteCarlo.isSelected()) {
				String choice = (String) select.getSelectedItem();
				if (choice.equals("Average")) {
					if (sim1.equals("read-in-data")) {
						if (keep.isSelected()) {
							XYSeriesCollection datasetReplace = new XYSeriesCollection();
							for (int i = 0; i < boxes.size(); i++) {
								if (boxes.get(i).isSelected()) {
									datasetReplace.addSeries(dataset.getSeries(i));
								}
							}
							dataset = datasetReplace;
						}
						graph(outDir1 + File.separator + "run-" + 1 + "."
								+ printer_id1.substring(0, printer_id1.length() - 8), component,
								printer_track_quantity1, sim1 + " average simulation results",
								intSpecies, -1);
					} else {
						if (keep.isSelected()) {
							XYSeriesCollection datasetReplace = new XYSeriesCollection();
							for (int i = 0; i < boxes.size(); i++) {
								if (boxes.get(i).isSelected()) {
									datasetReplace.addSeries(dataset.getSeries(i));
								}
							}
							dataset = datasetReplace;
						}
						graph(outDir1 + File.separator + "run-" + 1 + "."
								+ printer_id1.substring(0, printer_id1.length() - 8), component,
								printer_track_quantity1, sim1 + " run average simulation results",
								intSpecies, -1);
					}
				} else if (choice.equals("Variance")) {
					if (sim1.equals("read-in-data")) {
						if (keep.isSelected()) {
							XYSeriesCollection datasetReplace = new XYSeriesCollection();
							for (int i = 0; i < boxes.size(); i++) {
								if (boxes.get(i).isSelected()) {
									datasetReplace.addSeries(dataset.getSeries(i));
								}
							}
							dataset = datasetReplace;
						}
						graph(outDir1 + File.separator + "run-" + 1 + "."
								+ printer_id1.substring(0, printer_id1.length() - 8), component,
								printer_track_quantity1, sim1 + " variance simulation results",
								intSpecies, -1);
					} else {
						if (keep.isSelected()) {
							XYSeriesCollection datasetReplace = new XYSeriesCollection();
							for (int i = 0; i < boxes.size(); i++) {
								if (boxes.get(i).isSelected()) {
									datasetReplace.addSeries(dataset.getSeries(i));
								}
							}
							dataset = datasetReplace;
						}
						graph(outDir1 + File.separator + "run-" + 1 + "."
								+ printer_id1.substring(0, printer_id1.length() - 8), component,
								printer_track_quantity1, sim1 + " run variance simulation results",
								intSpecies, -1);
					}
				} else if (choice.equals("Standard Deviation")) {
					if (sim1.equals("read-in-data")) {
						if (keep.isSelected()) {
							XYSeriesCollection datasetReplace = new XYSeriesCollection();
							for (int i = 0; i < boxes.size(); i++) {
								if (boxes.get(i).isSelected()) {
									datasetReplace.addSeries(dataset.getSeries(i));
								}
							}
							dataset = datasetReplace;
						}
						graph(outDir1 + File.separator + "run-" + 1 + "."
								+ printer_id1.substring(0, printer_id1.length() - 8), component,
								printer_track_quantity1, sim1
										+ " standard deviation simulation results", intSpecies, -1);
					} else {
						if (keep.isSelected()) {
							XYSeriesCollection datasetReplace = new XYSeriesCollection();
							for (int i = 0; i < boxes.size(); i++) {
								if (boxes.get(i).isSelected()) {
									datasetReplace.addSeries(dataset.getSeries(i));
								}
							}
							dataset = datasetReplace;
						}
						graph(outDir1 + File.separator + "run-" + 1 + "."
								+ printer_id1.substring(0, printer_id1.length() - 8), component,
								printer_track_quantity1, sim1
										+ " run standard deviation simulation results", intSpecies,
								-1);
					}
				} else {
					int next = Integer.parseInt(choice);
					if (keep.isSelected()) {
						XYSeriesCollection datasetReplace = new XYSeriesCollection();
						for (int i = 0; i < boxes.size(); i++) {
							if (boxes.get(i).isSelected()) {
								datasetReplace.addSeries(dataset.getSeries(i));
							}
						}
						dataset = datasetReplace;
					}
					graph(outDir1 + File.separator + "run-" + next + "."
							+ printer_id1.substring(0, printer_id1.length() - 8), component,
							printer_track_quantity1, sim1 + " run-" + next + " simulation results",
							intSpecies, -1);
				}
			}
		}
		/*
		 * // if the open menu item is clicked else if (e.getSource() == open) {
		 * File file = null; String filename = Buttons.browse(this, file, null,
		 * JFileChooser.FILES_ONLY, "Graph"); if (!filename.equals("")) { try {
		 * String[] split = filename.split(File.separator); String last =
		 * split[split.length - 1]; String first = filename.substring(0,
		 * filename.length() - last.length()); String printer =
		 * filename.substring(filename.length() - 3); String id = printer +
		 * ".printer"; JRadioButton button = new JRadioButton(); if
		 * (last.substring(0, 3).equals("run")) { button.setSelected(true);
		 * String get = ""; for (int i = 0; i < last.length(); i++) { if
		 * (Character.isDigit(last.charAt(i))) { get += last.charAt(i); } } int
		 * number = Integer.parseInt(get); int runs = Integer.parseInt((String)
		 * JOptionPane.showInputDialog(this, "Please enter the number of output
		 * files in this simulation:", "Enter Number Of Runs",
		 * JOptionPane.PLAIN_MESSAGE, null, null, number)); int i = 0; try { for
		 * (i = number; i <= runs; i++) { InputStream test = new
		 * FileInputStream(new File((first + "run-" + i + "." + printer)));
		 * test.read(); } } catch (Exception e2) { runs = i - 1; } runs =
		 * Math.max(number, runs); if (keep.isSelected()) { XYSeriesCollection
		 * datasetReplace = new XYSeriesCollection(); for (int j = 0; j <
		 * boxes.size(); j++) { if (boxes.get(j).isSelected()) {
		 * datasetReplace.addSeries(dataset.getSeries(j)); } } dataset =
		 * datasetReplace; biomodelsim.removeTab(this); biomodelsim
		 * .addTab("Graph", new Graph(first + "run-1." + printer, component,
		 * printer_track_quantity1, "read-in-data average" + " simulation
		 * results", button, "read-in-data", id, first, runs, new String[0],
		 * number, dataset, time, biomodelsim)); } else {
		 * biomodelsim.removeTab(this); biomodelsim.addTab("Graph", new
		 * Graph(first + "run-1." + printer, component, printer_track_quantity1,
		 * "read-in-data average" + " simulation results", button,
		 * "read-in-data", id, first, runs, new String[0], number, null, time,
		 * biomodelsim)); } } else { button.setSelected(false); if
		 * (keep.isSelected()) { XYSeriesCollection datasetReplace = new
		 * XYSeriesCollection(); for (int j = 0; j < boxes.size(); j++) { if
		 * (boxes.get(j).isSelected()) {
		 * datasetReplace.addSeries(dataset.getSeries(j)); } } dataset =
		 * datasetReplace; biomodelsim.removeTab(this);
		 * biomodelsim.addTab("Graph", new Graph(filename, component,
		 * printer_track_quantity1, "read-in-data simulation results", button,
		 * "read-in-data", id, first, 1, new String[0], -1, dataset, time,
		 * biomodelsim)); } else { biomodelsim.removeTab(this);
		 * biomodelsim.addTab("Graph", new Graph(filename, component,
		 * printer_track_quantity1, "read-in-data simulation results", button,
		 * "read-in-data", id, first, 1, new String[0], -1, null, time,
		 * biomodelsim)); } } // graphFrame.dispose(); } catch (Exception e1) {
		 * JOptionPane.showMessageDialog(this, "Error reading in data!" +
		 * "\nFile may be named incorrectly" + " or may be invalid.", "Error",
		 * JOptionPane.ERROR_MESSAGE); } } }
		 */
		// if the change dimensions button is clicked
		else if (e.getSource() == changeSize) {
			XYPlot plot = chart.getXYPlot();
			try {
				NumberAxis axis = (NumberAxis) plot.getRangeAxis();
				axis.setAutoTickUnitSelection(false);
				NumberFormat num = NumberFormat.getInstance();
				num.setMaximumFractionDigits(4);
				num.setGroupingUsed(false);
				double minY = Double.parseDouble(YMin.getText().trim());
				minY = Double.parseDouble(num.format(minY));
				double maxY = Double.parseDouble(YMax.getText().trim());
				maxY = Double.parseDouble(num.format(maxY));
				double scaleY = Double.parseDouble(YScale.getText().trim());
				scaleY = Double.parseDouble(num.format(scaleY));
				axis.setRange(minY, maxY);
				axis.setTickUnit(new NumberTickUnit(scaleY));
				axis = (NumberAxis) plot.getDomainAxis();
				axis.setAutoTickUnitSelection(false);
				double minX = Double.parseDouble(XMin.getText().trim());
				minX = Double.parseDouble(num.format(minX));
				double maxX = Double.parseDouble(XMax.getText().trim());
				maxX = Double.parseDouble(num.format(maxX));
				double scaleX = Double.parseDouble(XScale.getText().trim());
				scaleX = Double.parseDouble(num.format(scaleX));
				axis.setRange(minX, maxX);
				axis.setTickUnit(new NumberTickUnit(scaleX));
			} catch (Exception e1) {
				JOptionPane
						.showMessageDialog(biomodelsim.frame(),
								"Must enter doubles into the inputs "
										+ "to change the graph's dimensions!", "Error",
								JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the select all button is clicked
		else if (e.getSource() == addAll) {
			for (int i = 0; i < boxes.size(); i++) {
				boxes.get(i).setSelected(true);
				XYPlot plot = chart.getXYPlot();
				XYItemRenderer rend = plot.getRenderer();
				rend.setSeriesVisible(i, true);
			}
			resize();
		}
		// if the deselect all button is clicked
		else if (e.getSource() == removeAll) {
			for (int i = 0; i < boxes.size(); i++) {
				boxes.get(i).setSelected(false);
				XYPlot plot = chart.getXYPlot();
				XYItemRenderer rend = plot.getRenderer();
				rend.setSeriesVisible(i, false);
			}
			resize();
		}
		// if the resize check box is clicked
		else if (e.getSource() == resize) {
			resize();
		}
		// if the filled in shapes check box is clicked
		else if (e.getSource() == filled) {
			XYPlot plot = chart.getXYPlot();
			XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) plot.getRenderer();
			if (filled.isSelected()) {
				rend.setShapesFilled(true);
			} else {
				rend.setShapesFilled(false);
			}
		}
		// if the visible shapes check box is clicked
		else if (e.getSource() == shapes) {
			XYPlot plot = chart.getXYPlot();
			XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) plot.getRenderer();
			if (shapes.isSelected()) {
				rend.setShapesVisible(true);
			} else {
				rend.setShapesVisible(false);
			}
		}
		// if the save button is clicked
		else if (e.getSource() == save) {
			save();
		}
		// if the export as jpeg button is clicked
		else if (e.getSource() == exportJPeg) {
			export(true);
		}
		// if the export as png button is clicked
		else if (e.getSource() == exportPng) {
			export(false);
		}
		// if the duplicate button is clicked
		else if (e.getSource() == duplicate) {
			if (reb2sac != null) {
				Graph g = this;
				reb2sac.addGraphTab(g);
			}
		}
		// if one of the species check boxes is clicked
		else {
			for (int i = 0; i < boxes.size(); i++) {
				if (e.getActionCommand().equals("button" + i)) {
					if (boxes.get(i).isSelected()) {
						XYPlot plot = chart.getXYPlot();
						XYItemRenderer rend = plot.getRenderer();
						rend.setSeriesVisible(i, true);
						resize();
					} else {
						XYPlot plot = chart.getXYPlot();
						XYItemRenderer rend = plot.getRenderer();
						rend.setSeriesVisible(i, false);
						resize();
					}
				}
			}
		}
	}

	/**
	 * Save the graph data in a file.
	 */
	public void save() {
		String save = "";
		save += "XMin: " + XMin.getText() + "\n";
		save += "XMax: " + XMax.getText() + "\n";
		save += "XScale: " + XScale.getText() + "\n";
		save += "YMin: " + YMin.getText() + "\n";
		save += "YMax: " + YMax.getText() + "\n";
		save += "YScale: " + YScale.getText() + "\n";
		save += "Keep: " + keep.isSelected() + "\n";
		save += "Visible: " + shapes.isSelected() + "\n";
		save += "Filled: " + filled.isSelected() + "\n";
	}

	/**
	 * Private method used to calculate max and min of the graph.
	 */
	private void determineMaxAndMin() {
		maxAndMin = new ArrayList<double[]>();
		XYPlot plot = chart.getXYPlot();
		for (int j = 0; j < dataset.getSeriesCount(); j++) {
			double minY = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;
			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
			XYSeries series = dataset.getSeries(j);
			for (int k = 0; k < series.getItemCount(); k++) {
				maxY = Math.max(series.getY(k).doubleValue(), maxY);
				minY = Math.min(series.getY(k).doubleValue(), minY);
				maxX = Math.max(series.getX(k).doubleValue(), maxX);
				minX = Math.min(series.getX(k).doubleValue(), minX);
			}
			double[] add = { minX, minY, maxX, maxY };
			maxAndMin.add(add);
		}
	}

	/**
	 * Private method used to auto resize the graph.
	 */
	private void resize() {
		if (resize.isSelected()) {
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
				Boolean visible = rend.getSeriesVisible(j);
				if (visible == null || visible.equals(true)) {
					maxY = Math.max(maxAndMin.get(j)[3], maxY);
					minY = Math.min(maxAndMin.get(j)[1], minY);
					maxX = Math.max(maxAndMin.get(j)[2], maxX);
					minX = Math.min(maxAndMin.get(j)[0], minX);
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
				axis.setRange(Double.parseDouble(num.format(minX)), Double.parseDouble(num
						.format(maxX)));
			}
			axis.setAutoTickUnitSelection(true);
		}
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
		JPanel titlePanel = new JPanel(new GridLayout(dataset.getSeriesCount() + 3, 2));
		JLabel titleLabel = new JLabel("Title:");
		JLabel xLabel = new JLabel("X-Axis Label:");
		JLabel yLabel = new JLabel("Y-Axis Label:");
		JTextField title = new JTextField(chart.getTitle().getText(), 20);
		JTextField x = new JTextField(chart.getXYPlot().getDomainAxis().getLabel(), 20);
		JTextField y = new JTextField(chart.getXYPlot().getRangeAxis().getLabel(), 20);
		titlePanel.add(titleLabel);
		titlePanel.add(title);
		titlePanel.add(xLabel);
		titlePanel.add(x);
		titlePanel.add(yLabel);
		titlePanel.add(y);
		ArrayList<JLabel> seriesLabel = new ArrayList<JLabel>();
		ArrayList<JTextField> series = new ArrayList<JTextField>();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			seriesLabel.add(new JLabel("Species " + (i + 1) + " Label"));
			series.add(new JTextField(dataset.getSeries(i).getKey().toString(), 20));
			titlePanel.add(seriesLabel.get(i));
			titlePanel.add(series.get(i));
		}
		JScrollPane scroll = new JScrollPane();
		scroll.setPreferredSize(new Dimension(500, 100));
		scroll.setViewportView(titlePanel);
		Object[] options = { "Ok", "Cancel" };
		int value = JOptionPane.showOptionDialog(biomodelsim.frame(), scroll,
				"Edit Title And Labels", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			chart.setTitle(title.getText().trim());
			time = x.getText().trim();
			chart.getXYPlot().getDomainAxis().setLabel(time);
			printer_track_quantity1 = y.getText().trim();
			chart.getXYPlot().getRangeAxis().setLabel(printer_track_quantity1);
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				dataset.getSeries(i).setKey(series.get(i).getText().trim());
				boxes.get(i).setText(series.get(i).getText().trim());
				if (boxes.get(i).isSelected()) {
					XYPlot plot = chart.getXYPlot();
					XYItemRenderer rend = plot.getRenderer();
					rend.setSeriesVisible(i, true);
				}
			}
		}
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

	/**
	 * This method saves the graph as a jpeg or as a png file.
	 */
	public void export(boolean jpeg) {
		try {
			File file;
			if (savedPics != null) {
				file = new File(savedPics);
			} else {
				file = null;
			}
			String filename = Buttons.browse(biomodelsim.frame(), file, null,
					JFileChooser.FILES_ONLY, "Save");
			if (!filename.equals("")) {
				if (jpeg) {
					if (filename.substring((filename.length() - 4), filename.length()).equals(
							".jpg")
							|| filename.substring((filename.length() - 5), filename.length())
									.equals(".jpeg")) {
					} else {
						filename += ".jpg";
					}
				} else {
					if (filename.substring((filename.length() - 4), filename.length()).equals(
							".png")) {
					} else {
						filename += ".png";
					}
				}
				file = new File(filename);
				if (file.exists()) {
					Object[] options = { "Overwrite", "Cancel" };
					int value = JOptionPane.showOptionDialog(biomodelsim.frame(),
							"File already exists." + " Overwrite?", "File Already Exists",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
					if (value == JOptionPane.YES_OPTION) {
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
						value = JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
								"Enter Size Of File", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
						if (value == JOptionPane.YES_OPTION) {
							while (width == -1 || height == -1)
								try {
									width = Integer.parseInt(widthField.getText().trim());
									height = Integer.parseInt(heightField.getText().trim());
									if (width < 1 || height < 1) {
										JOptionPane.showMessageDialog(biomodelsim.frame(),
												"Width and height must be positive integers!",
												"Error", JOptionPane.ERROR_MESSAGE);
										JOptionPane.showOptionDialog(biomodelsim.frame(),
												sizePanel, "Enter Size Of File",
												JOptionPane.YES_NO_OPTION,
												JOptionPane.PLAIN_MESSAGE, null, options2,
												options2[0]);
									}
								} catch (Exception e2) {
									JOptionPane.showMessageDialog(biomodelsim.frame(),
											"Width and height must be positive integers!", "Error",
											JOptionPane.ERROR_MESSAGE);
									width = -1;
									height = -1;
									JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
											"Enter Size Of File", JOptionPane.YES_NO_OPTION,
											JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
								}
						}
						if (jpeg) {
							ChartUtilities.saveChartAsJPEG(file, chart, width, height);
						} else {
							ChartUtilities.saveChartAsPNG(file, chart, width, height);
						}
						savedPics = filename;
					}
				} else {
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
							"Enter Size Of File", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
					if (value == JOptionPane.YES_OPTION) {
						while (width == -1 || height == -1)
							try {
								width = Integer.parseInt(widthField.getText().trim());
								height = Integer.parseInt(heightField.getText().trim());
								if (width < 1 || height < 1) {
									JOptionPane.showMessageDialog(biomodelsim.frame(),
											"Width and height must be positive integers!", "Error",
											JOptionPane.ERROR_MESSAGE);
									JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
											"Enter Size Of File", JOptionPane.YES_NO_OPTION,
											JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
								}
							} catch (Exception e2) {
								JOptionPane.showMessageDialog(biomodelsim.frame(),
										"Width and height must be positive integers!", "Error",
										JOptionPane.ERROR_MESSAGE);
								width = -1;
								height = -1;
								JOptionPane.showOptionDialog(biomodelsim.frame(), sizePanel,
										"Enter Size Of File", JOptionPane.YES_NO_OPTION,
										JOptionPane.PLAIN_MESSAGE, null, options2, options2[0]);
							}
					}
					if (jpeg) {
						ChartUtilities.saveChartAsJPEG(file, chart, width, height);
					} else {
						ChartUtilities.saveChartAsPNG(file, chart, width, height);
					}
					savedPics = filename;
				}
			}
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Unable To Save File!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
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

	private Component component; // the main gui's component

	/*
	 * Buttons used in graph window
	 */
	private JButton next, changeSize, addAll, removeAll, editGraph;

	/*
	 * ArrayList of Check Boxes used to graph output data
	 */
	private ArrayList<JCheckBox> boxes;

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

	/*
	 * graph data
	 */
	private ArrayList<ArrayList<Double>> data, variance, deviation;

	private ArrayList<ArrayList<Double>> average = null; // average of data

	private ArrayList<String> graphSpecies; // names of species in the graph

	private String[] intSpecies; // interesting species

	private String savedPics; // directory for saved pictures

	private String time; // label for x-axis on chart

	private XYSeriesCollection dataset; // dataset of data

	private JCheckBox keep; // check box for keeping data on graph

	private BioSim biomodelsim; // tstubd gui

	private JButton exportJPeg, exportPng; // buttons

	private HashMap<String, Paint> colors;

	private HashMap<String, Shape> shape;

	/**
	 * Creates a Graph Object from the data given and calls the private graph
	 * helper method.
	 */
	public Graph(String file, Component component, String printer_track_quantity, String label,
			JRadioButton monteCarlo, String sim, String printer_id, String outDir, int run,
			String[] intSpecies, int readIn, XYSeriesCollection dataset, String time,
			BioSim biomodelsim) {
		// initializes member variables
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
		setUpShapesAndColors();
		graph(file, component, printer_track_quantity, label, intSpecies, readIn);
	}

	/**
	 * This private helper method calls the private readData method, sets up a
	 * graph frame, and graphs the data.
	 */
	private void graph(String file, Component component, String printer_track_quantity,
			String label, String[] intSpecies, int readIn) {
		// reads output data
		ArrayList<ShapeAndPaint> shapesAndPaints = new ArrayList<ShapeAndPaint>();
		if (chart != null) {
			for (int i = 0; i < boxes.size(); i++) {
				if (boxes.get(i).isSelected()) {
					shapesAndPaints.add(new ShapeAndPaint(chart.getXYPlot().getRenderer()
							.getSeriesShape(i), chart.getXYPlot().getRenderer().getSeriesPaint(i)));
				}
			}
		}
		if (average != null) {
			readData(file, component, printer_track_quantity, label, 2);
		} else {
			readData(file, component, printer_track_quantity, label, 1);
			boxes = new ArrayList<JCheckBox>();
			for (int i = 1; i < graphSpecies.size(); i++) {
				boxes.add(new JCheckBox());// graphSpecies.get(i)));
				boxes.get(i - 1).setSelected(false);
				for (int j = 0; j < intSpecies.length; j++) {
					if (graphSpecies.get(i).equals(intSpecies[j])) {
						boxes.get(i - 1).setSelected(true);
					}
				}
			}
			keep = new JCheckBox("Keep Graphed Data");
			if (dataset.getSeriesCount() != 0) {
				keep.setSelected(true);
			} else {
				keep.setSelected(false);
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
		if (data.size() != 0) {
			for (int i = 0; i < (data.get(0)).size(); i++) {
				for (int j = 1; j < graphSpecies.size(); j++) {
					graphData.get(j - 1).add((data.get(0)).get(i), (data.get(j)).get(i));
				}
			}
		}
		boolean equal = dataset.getSeriesCount() == graphData.size();
		int kept = 0;
		ArrayList<String> names = new ArrayList<String>();
		if (!keep.isSelected()) {
			if (equal) {
				for (int i = 0; i < dataset.getSeriesCount(); i++) {
					names.add(dataset.getSeries(i).getKey().toString());
				}
			}
			dataset = new XYSeriesCollection();
		} else {
			kept = dataset.getSeriesCount();
		}
		for (int i = 0; i < graphData.size(); i++) {
			dataset.addSeries(graphData.get(i));
			if (names.size() != 0) {
				dataset.getSeries(i).setKey(names.get(i));
			}
		}
		if (keep.isSelected()) {
			boxes = new ArrayList<JCheckBox>();
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				boxes.add(new JCheckBox());
				boxes.get(i).setSelected(false);
			}
			for (int i = 0; i < kept; i++) {
				boxes.get(i).setSelected(true);
			}
		}
		if (boxes.size() > dataset.getSeriesCount()) {
			boxes = new ArrayList<JCheckBox>();
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				boxes.add(new JCheckBox());
				boxes.get(i).setSelected(false);
			}
		}

		// creates the graph from the dataset and adds it to a chart panel
		XYItemRenderer ren = null;
		if (chart != null) {
			ren = chart.getXYPlot().getRenderer();
		}
		chart = ChartFactory.createXYLineChart(label, time, printer_track_quantity, dataset,
				PlotOrientation.VERTICAL, true, true, false);
		chart.addProgressListener(this);
		XYPlot plot = chart.getXYPlot();
		if (keep.isSelected()) {
			for (int i = 0; i < shapesAndPaints.size(); i++) {
				plot.getRenderer().setSeriesShape(i, shapesAndPaints.get(i).getShape());
				plot.getRenderer().setSeriesPaint(i, shapesAndPaints.get(i).getPaint());
			}
		} else if (ren != null && equal) {
			plot.setRenderer(ren);
		}
		XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) plot.getRenderer();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			if (boxes.get(i).isSelected()) {
				rend.setSeriesVisible(i, true);
			} else {
				rend.setSeriesVisible(i, false);
			}
		}
		ChartPanel graph = new ChartPanel(chart);
		graph.addMouseListener(this);

		// creates text fields for changing the graph's dimensions
		XMin = new JTextField();
		XMax = new JTextField();
		XScale = new JTextField();
		YMin = new JTextField();
		YMax = new JTextField();
		YScale = new JTextField();
		changeSize = new JButton("Resize Graph");
		addAll = new JButton("Graph All");
		removeAll = new JButton("Ungraph All");
		editGraph = new JButton("Edit Graph");
		changeSize.addActionListener(this);
		addAll.addActionListener(this);
		removeAll.addActionListener(this);
		editGraph.addActionListener(this);

		// creates combo box for choosing which run to display
		ArrayList<String> add = new ArrayList<String>();
		add.add("Average");
		add.add("Variance");
		add.add("Standard Deviation");
		for (int i = 0; i < run; i++) {
			if (new File(outDir1 + File.separator + "run-" + (i + 1) + "."
					+ printer_id1.substring(0, printer_id1.length() - 8)).exists()) {
				add.add("" + (i + 1));
			}
		}
		JLabel selectLabel = new JLabel("Select Next:");
		if (monteCarlo.isSelected()) {
			select = new JComboBox(add.toArray());
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
		exportJPeg = new JButton("Export As JPEG");
		exportPng = new JButton("Export As PNG");
		exportJPeg.addActionListener(this);
		exportPng.addActionListener(this);
		ButtonHolder.add(exportJPeg);
		ButtonHolder.add(exportPng);
		JPanel AllButtonsHolder = new JPanel(new BorderLayout());

		// puts all the components of the graph gui into a display panel
		JPanel SpecialButtonHolder = new JPanel();
		JPanel monteCarloHolder = new JPanel();
		JPanel monteSpecialHolder = new JPanel(new BorderLayout());
		SpecialButtonHolder.add(editGraph);
		SpecialButtonHolder.add(addAll);
		SpecialButtonHolder.add(removeAll);
		SpecialButtonHolder.add(changeSize);
		if (monteCarlo.isSelected()) {
			monteCarloHolder.add(selectLabel);
			monteCarloHolder.add(select);
			monteCarloHolder.add(next);
			monteCarloHolder.add(keep);
		}
		monteSpecialHolder.add(SpecialButtonHolder, "Center");
		monteSpecialHolder.add(monteCarloHolder, "South");
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
							int firstOne = -1;
							if (label.contains("average")) {
								runsToMake = run;
								String[] findNum = file.split(File.separator);
								String search = findNum[findNum.length - 1];
								firstOne = Integer.parseInt(search
										.substring(4, search.length() - 4));
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
								int skip = firstOne;
								if (!first) {
									if (firstOne != 1) {
										j--;
										firstOne = 1;
									}
									boolean loop = true;
									while (loop && j < runsToMake && (j + 1) != skip) {
										if (new File(outDir1
												+ File.separator
												+ "run-"
												+ (j + 1)
												+ "."
												+ printer_id1
														.substring(0, printer_id1.length() - 8))
												.exists()) {
											input = new BufferedInputStream(
													new ProgressMonitorInputStream(
															component,
															"Reading Reb2sac Output Data From "
																	+ new File(
																			outDir1
																					+ File.separator
																					+ "run-"
																					+ (j + 1)
																					+ "."
																					+ printer_id1
																							.substring(
																									0,
																									printer_id1
																											.length() - 8))
																			.getName(),
															new FileInputStream(
																	new File(
																			outDir1
																					+ File.separator
																					+ "run-"
																					+ (j + 1)
																					+ "."
																					+ printer_id1
																							.substring(
																									0,
																									printer_id1
																											.length() - 8)))));
											for (int i = 0; i < readCount; i++) {
												input.read();
											}
											loop = false;
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
		// if the change dimensions button is clicked
		else if (e.getSource() == changeSize) {
			resize();
		}
		// if the edit Graph button is clicked
		else if (e.getSource() == editGraph) {
			editGraph();
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
		// if the export as jpeg button is clicked
		else if (e.getSource() == exportJPeg) {
			export(true);
		}
		// if the export as png button is clicked
		else if (e.getSource() == exportPng) {
			export(false);
		}
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
		shape = new HashMap<String, Shape>();
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
		colors.put("Red 2", draw.getNextPaint());
		colors.put("Blue 2", draw.getNextPaint());
		colors.put("Green 2", draw.getNextPaint());
		colors.put("Yellow 2", draw.getNextPaint());
		colors.put("Magenta 2", draw.getNextPaint());
		colors.put("Cyan 2", draw.getNextPaint());
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
		shape.put("Square", draw.getNextShape());
		shape.put("Circle", draw.getNextShape());
		shape.put("Triangle", draw.getNextShape());
		shape.put("Diamond", draw.getNextShape());
		shape.put("Rectangle (Vertical)", draw.getNextShape());
		shape.put("Triangle (Upside Down)", draw.getNextShape());
		shape.put("Circle (Half)", draw.getNextShape());
		shape.put("Arrow", draw.getNextShape());
		shape.put("Rectangle (Horizontal)", draw.getNextShape());
		shape.put("Arrow (Backwards)", draw.getNextShape());
	}

	private void editGraph() {
		JPanel titlePanel = new JPanel(new GridLayout(dataset.getSeriesCount() + 4, 6));
		JLabel titleLabel = new JLabel("Title:");
		JLabel xLabel = new JLabel("X-Axis Label:");
		JLabel yLabel = new JLabel("Y-Axis Label:");
		JTextField title = new JTextField(chart.getTitle().getText(), 5);
		JTextField x = new JTextField(chart.getXYPlot().getDomainAxis().getLabel(), 5);
		JTextField y = new JTextField(chart.getXYPlot().getRangeAxis().getLabel(), 5);
		JLabel xMin = new JLabel("X-Min:");
		JLabel xMax = new JLabel("X-Max:");
		JLabel xScale = new JLabel("X-Step:");
		JLabel yMin = new JLabel("Y-Min:");
		JLabel yMax = new JLabel("Y-Max:");
		JLabel yScale = new JLabel("Y-Step:");
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
		ArrayList<JLabel> seriesLabel = new ArrayList<JLabel>();
		ArrayList<JTextField> series = new ArrayList<JTextField>();
		ArrayList<JComboBox> colors = new ArrayList<JComboBox>();
		ArrayList<JComboBox> shapes = new ArrayList<JComboBox>();
		ArrayList<JCheckBox> visible = new ArrayList<JCheckBox>();
		ArrayList<JCheckBox> filled = new ArrayList<JCheckBox>();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
			JCheckBox tempBox = new JCheckBox("Visible Shapes");
			try {
				tempBox.setSelected(rend.getSeriesShapesVisible(i));
			} catch (Exception e) {
				tempBox.setSelected(true);
			}
			visible.add(tempBox);
			tempBox = new JCheckBox("Filled In Shapes");
			try {
				tempBox.setSelected(rend.getSeriesShapesFilled(i));
			} catch (Exception e) {
				tempBox.setSelected(true);
			}
			filled.add(tempBox);
			seriesLabel.add(new JLabel("Species " + (i + 1) + " Label:"));
			series.add(new JTextField(dataset.getSeries(i).getKey().toString(), 5));
			Object[] col = this.colors.keySet().toArray();
			Arrays.sort(col);
			Object[] shap = this.shape.keySet().toArray();
			Arrays.sort(shap);
			JComboBox colBox = new JComboBox(col);
			for (Object c : col) {
				if (this.colors.get(c).equals(chart.getXYPlot().getRenderer().getSeriesPaint(i))) {
					colBox.setSelectedItem(c);
				}
			}
			JComboBox shapBox = new JComboBox(shap);
			for (Object s : shap) {
				if (this.shape.get(s).equals(chart.getXYPlot().getRenderer().getSeriesShape(i))) {
					shapBox.setSelectedItem(s);
				}
			}
			colors.add(colBox);
			shapes.add(shapBox);
			JPanel temp = new JPanel(new BorderLayout());
			temp.add(boxes.get(i), "West");
			temp.add(series.get(i), "Center");
			titlePanel.add(seriesLabel.get(i));
			titlePanel.add(temp);
			titlePanel.add(colors.get(i));
			titlePanel.add(shapes.get(i));
			titlePanel.add(visible.get(i));
			titlePanel.add(filled.get(i));
		}
		JScrollPane scroll = new JScrollPane();
		scroll.setPreferredSize(new Dimension(1100, 300));
		scroll.setViewportView(titlePanel);
		Object[] options = { "Ok", "Cancel" };
		int value = JOptionPane.showOptionDialog(biomodelsim.frame(), scroll,
				"Edit Title And Labels", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			for (int i = 0; i < boxes.size(); i++) {
				if (boxes.get(i).isSelected()) {
					XYPlot plot = chart.getXYPlot();
					XYItemRenderer rend = plot.getRenderer();
					rend.setSeriesVisible(i, true);
				} else {
					XYPlot plot = chart.getXYPlot();
					XYItemRenderer rend = plot.getRenderer();
					rend.setSeriesVisible(i, false);
				}
				XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) chart.getXYPlot()
						.getRenderer();
				if (visible.get(i).isSelected()) {
					rend.setSeriesShapesFilled(i, true);
				} else {
					rend.setSeriesShapesFilled(i, false);
				}
				if (filled.get(i).isSelected()) {
					rend.setSeriesShapesVisible(i, true);
				} else {
					rend.setSeriesShapesVisible(i, false);
				}
			}
			XYPlot plot = chart.getXYPlot();
			chart.setTitle(title.getText().trim());
			time = x.getText().trim();
			chart.getXYPlot().getDomainAxis().setLabel(time);
			printer_track_quantity1 = y.getText().trim();
			chart.getXYPlot().getRangeAxis().setLabel(printer_track_quantity1);
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				dataset.getSeries(i).setKey(series.get(i).getText().trim());
				chart.getXYPlot().getRenderer().setSeriesPaint(i,
						this.colors.get(colors.get(i).getSelectedItem()));
				chart.getXYPlot().getRenderer().setSeriesShape(i,
						this.shape.get(shapes.get(i).getSelectedItem()));
				if (boxes.get(i).isSelected()) {
					XYItemRenderer render = plot.getRenderer();
					render.setSeriesVisible(i, true);
				}
			}
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
	}
}
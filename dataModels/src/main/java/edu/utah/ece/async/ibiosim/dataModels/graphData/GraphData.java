package edu.utah.ece.async.ibiosim.dataModels.graphData;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JComboBox;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.StandardTickUnitSource;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.GradientBarPainter;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jibble.epsgraphics.EpsGraphics2D;
import org.jlibsedml.Curve;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.DataSet;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.Plot2D;
import org.jlibsedml.Report;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Variable;
import org.jlibsedml.XMLException;
import org.jlibsedml.modelsupport.SBMLSupport;
import org.w3c.dom.DOMImplementation;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.SEDMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.DTSDParser;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.DataParser;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.TSDParser;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * This class describes graph data
 *  
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GraphData extends CoreObservable {
	
	private JFreeChart chart; // Graph of the output data

	private LegendTitle legend;
	
	private XYSeriesCollection curData; // Data in the current graph
	
	private Message message = new Message();
	
	private String XMin,XMax,XScale,YMin,YMax,YScale;
	
	private boolean LogX,LogY,resize,visibleLegend;
	
	private String XId;
	
	private LinkedList<GraphSpecies> graphed;

	private LinkedList<GraphProbs> probGraphed;

	private ArrayList<String> graphSpecies; // names of species in the graph

	private ArrayList<String> graphProbs;
	
	private ArrayList<String> learnSpecs;
	
	private boolean timeSeriesPlot;
	
	private ArrayList<String> averageOrder;
	
	private String outDir; // output directory

	private String printer_id; 
	
	private boolean warn;
	
	public final static String TSD_DATA_TYPE = "tsd.printer";

	public final static String CSV_DATA_TYPE = "csv.printer";

	public final static String DAT_DATA_TYPE = "dat.printer";
	
	public final static int JPG_FILE_TYPE = 0;
	
	public final static int PNG_FILE_TYPE = 1;
	
	public final static int PDF_FILE_TYPE = 2;
	
	public final static int EPS_FILE_TYPE = 3;
	
	public final static int SVG_FILE_TYPE = 4;
	
	public final static int CSV_FILE_TYPE = 5;
	
	public final static int DAT_FILE_TYPE = 6;
	
	public final static int TSD_FILE_TYPE = 7;
	
	public GraphData(String printer_id, String outDir, boolean warn, String printer_track_quantity, String label, XYSeriesCollection dataset, String time,
			ArrayList<String> learnSpecs) {
		this.outDir = outDir;
		this.printer_id = printer_id;
		this.warn = warn;
		this.learnSpecs = learnSpecs;
		graphSpecies = new ArrayList<String>();
		graphed = new LinkedList<GraphSpecies>();
		chart = ChartFactory.createXYLineChart(label, time, printer_track_quantity, dataset, PlotOrientation.VERTICAL, true, true, false);
		applyChartTheme();
		chart.setBackgroundPaint(new java.awt.Color(238, 238, 238));
		chart.getPlot().setBackgroundPaint(java.awt.Color.WHITE);
		chart.getXYPlot().setDomainGridlinePaint(java.awt.Color.LIGHT_GRAY);
		chart.getXYPlot().setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
		legend = chart.getLegend();
		timeSeriesPlot = true;
		LogX = false;
		LogY = false;
		visibleLegend = false;
		averageOrder = null;
		resize(dataset);
	}
	
	public GraphData(String printer_id, String outDir, boolean warn, String label,String yLabel, ArrayList<String> learnSpecs) {
		this.outDir = outDir;
		this.printer_id = printer_id;
		this.warn = warn;
		this.learnSpecs = learnSpecs;
		graphProbs = new ArrayList<String>();
		probGraphed = new LinkedList<GraphProbs>();
		chart = ChartFactory.createBarChart(label, "", yLabel, new DefaultCategoryDataset(), PlotOrientation.VERTICAL, true, true, false);
		applyChartTheme();
		((BarRenderer) chart.getCategoryPlot().getRenderer()).setBarPainter(new StandardBarPainter());
		chart.setBackgroundPaint(new java.awt.Color(238, 238, 238));
		chart.getPlot().setBackgroundPaint(java.awt.Color.WHITE);
		chart.getCategoryPlot().setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
		legend = chart.getLegend();
		timeSeriesPlot = false;
		averageOrder = null;
	}
	
	public void exportDataFile(File file, int output) {
		try {
			int count = curData.getSeries(0).getItemCount();
			for (int i = 1; i < curData.getSeriesCount(); i++) {
				if (curData.getSeries(i).getItemCount() != count) {				
					message.setErrorDialog("Unable to Export Data File", "Data series do not have the same number of points!");
					this.notifyObservers(message);
					return;
				}
			}
			for (int j = 0; j < count; j++) {
				Number Xval = curData.getSeries(0).getDataItem(j).getX();
				for (int i = 1; i < curData.getSeriesCount(); i++) {
					if (!curData.getSeries(i).getDataItem(j).getX().equals(Xval)) {
						message.setErrorDialog("Unable to Export Data File", "Data series time points are not the same!");
						this.notifyObservers(message);
						return;
					}
				}
			}
			FileOutputStream csvFile = new FileOutputStream(file);
			PrintWriter csvWriter = new PrintWriter(csvFile);
			if (output == 7) {
				csvWriter.print("((");
			}
			else if (output == 6) {
				csvWriter.print("#");
			}
			csvWriter.print("\"Time\"");
			count = curData.getSeries(0).getItemCount();
			int pos = 0;
			for (int i = 0; i < curData.getSeriesCount(); i++) {
				if (output == 6) {
					csvWriter.print(" ");
				}
				else {
					csvWriter.print(",");
				}
				csvWriter.print("\"" + curData.getSeriesKey(i) + "\"");
				if (curData.getSeries(i).getItemCount() > count) {
					count = curData.getSeries(i).getItemCount();
					pos = i;
				}
			}
			if (output == 7) {
				csvWriter.print(")");
			}
			else {
				csvWriter.println("");
			}
			for (int j = 0; j < count; j++) {
				if (output == 7) {
					csvWriter.print(",");
				}
				for (int i = 0; i < curData.getSeriesCount(); i++) {
					if (i == 0) {
						if (output == 7) {
							csvWriter.print("(");
						}
						csvWriter.print(curData.getSeries(pos).getDataItem(j).getX());
					}
					XYSeries data = curData.getSeries(i);
					if (j < data.getItemCount()) {
						XYDataItem item = data.getDataItem(j);
						if (output == 6) {
							csvWriter.print(" ");
						}
						else {
							csvWriter.print(",");
						}
						csvWriter.print(item.getY());
					}
					else {
						if (output == 6) {
							csvWriter.print(" ");
						}
						else {
							csvWriter.print(",");
						}
					}
				}
				if (output == 7) {
					csvWriter.print(")");
				}
				else {
					csvWriter.println("");
				}
			}
			if (output == 7) {
				csvWriter.println(")");
			}
			csvWriter.close();
			csvFile.close();
		}
		catch (Exception e1) {						
			message.setErrorDialog("Error", "Unable To Export File!");
			this.notifyObservers(message);
		}
	}
	
	public void export(File file, int output, int width, int height) throws DocumentException, IOException {
		if (output == 0) {
			ChartUtilities.saveChartAsJPEG(file, chart, width, height);
		}
		else if (output == 1) {
			ChartUtilities.saveChartAsPNG(file, chart, width, height);
		}
		else if (output == 2) {
			Rectangle pagesize = new Rectangle(width, height);
			Document document = new Document(pagesize, 50, 50, 50, 50);
			FileOutputStream out = new FileOutputStream(file);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
			chart.draw(g2, new java.awt.Rectangle(width, height));
			g2.dispose();
			cb.addTemplate(tp, 0, 0);
			document.close();
			out.close();
		}
		else if (output == 3) {
			Graphics2D g = new EpsGraphics2D();
			chart.draw(g, new java.awt.Rectangle(width, height));
			Writer out = new FileWriter(file);
			out.write(g.toString());
			out.close();
		}
		else if (output == 4) {
			DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
			org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);
			SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
			chart.draw(svgGenerator, new java.awt.Rectangle(width, height));
			boolean useCSS = true;
			FileOutputStream outStream = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(outStream, "UTF-8");
			svgGenerator.stream(out, useCSS);
			out.close();
			outStream.close();
		}
		else if ((output == 5) || (output == 6) || (output == 7)) {
			exportDataFile(file, output);
		}
	}
		
	private String getDataSet(DataGenerator dataGenerator) {
		String dataSet = SEDMLutilities.getSEDBaseAnnotation(dataGenerator, "dataSet", "dataset", "1");
		if (dataSet.equals("mean")) {
			return "\u03C7";
		} else if (dataSet.equals("stddev")) {
			return "\u03B4"; 
		} else if (dataSet.equals("variance")) {
			return "\u03B4\u00B2"; 
		}
		return dataSet;
	}
	
	private String getRunNumber(DataGenerator dataGenerator) {
		String dataSet = SEDMLutilities.getSEDBaseAnnotation(dataGenerator, "dataSet", "dataset", "1");
		if (dataSet.equals("mean")) {
			return "Average";
		} else if (dataSet.equals("stddev")) {
			return "Standard Deviation"; 
		} else if (dataSet.equals("variance")) {
			return "Variance"; 
		}
		return "run-"+dataSet;
	}
	
	public boolean loadSEDML(SEDMLDocument sedmlDoc,String analysisId,String plotId,boolean timeSeries,
			JComboBox XVariable) {
		SedML sedml = sedmlDoc.getSedMLModel();
		SBMLSupport sbmlSupport = new SBMLSupport();
		String[] colorlist = { "Red", "Blue", "Green", "Yellow", "Magenta", "Cyan", "Tan", "Gray (Dark)", "Red (Dark)", "Blue (Dark)",
				"Green (Dark)", "Yellow (Dark)", "Magenta (Dark)", "Cyan (Dark)", "Black", "Gray", "Red (Extra Dark)", "Blue (Extra Dark)",
				"Green (Extra Dark)", "Yellow (Extra Dark)", "Magenta (Extra Dark)", "Cyan (Extra Dark)", "Red (Light)", "Blue (Light)",
				"Green (Light)", "Yellow (Light)", "Magenta (Light)", "Cyan (Light)", "Gray (Light)" };
		int numColors = colorlist.length;
		String[] shapeList = { "Square", "Circle", "Triangle", "Diamond", "Rectangle (Horizontal)", 
				"Triangle (Upside Down)", "Circle (Half)", "Arrow", "Rectangle (Vertical)", "Arrow (Backwards)" };
		int numShapes = shapeList.length;
		Output output = null;
		if (plotId!=null) {
			output = sedml.getOutputWithId(plotId);
		} else {
			if (timeSeries) {
				output = sedml.getOutputWithId(analysisId+"__graph");
			} else {
				output = sedml.getOutputWithId(analysisId+"__report");
			}
		}
		if (output==null) return false;
		if (output.isPlot2d())
		{
			Plot2D plot = (Plot2D) output;
			if (plot.getName() != null)	{
				chart.setTitle(plot.getName());
			} else {
				chart.setTitle(plot.getId());
			}
			XMin = SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "x_min", "0.0");
			XMax = SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "x_max", "1.0");
			XScale = SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "x_scale", "0.1");
			YMin = SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "y_min", "0.0");
			YMax = SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "y_max", "1.0");
			YScale = SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "y_scale", "0.1");
			chart.getXYPlot().getDomainAxis().setLabel(SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "x_axis", "time"));
			chart.getXYPlot().getRangeAxis().setLabel(SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "y_axis", ""));
			chart.setBackgroundPaint(new Color(Integer.parseInt(SEDMLutilities.
					getSEDBaseAnnotation(plot, "graph", "chart_background_paint", "" + new Color(238, 238, 238).getRGB()))));
			chart.getPlot().setBackgroundPaint(new Color(Integer.parseInt(SEDMLutilities.
					getSEDBaseAnnotation(plot, "graph", "plot_background_paint", "" + (Color.WHITE).getRGB()))));
			chart.getXYPlot().setDomainGridlinePaint(new Color(Integer.parseInt(SEDMLutilities.
					getSEDBaseAnnotation(plot, "graph", "plot_domain_grid_line_paint", "" + (Color.LIGHT_GRAY).getRGB()))));
			chart.getXYPlot().setRangeGridlinePaint(new Color(Integer.parseInt(SEDMLutilities.
					getSEDBaseAnnotation(plot, "graph", "plot_range_grid_line_paint", "" + (Color.LIGHT_GRAY).getRGB()))));
			resize = (SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "auto_resize", "true").equals("true"));
			visibleLegend = (SEDMLutilities.getSEDBaseAnnotation(plot, "graph", "visibleLegend", "true").equals("true"));
			timeSeriesPlot = true;
			LogX = false;
			LogY = false;
			List<Curve> curves = plot.getListOfCurves();
			for (int j = 0; j < curves.size(); j++)
			{
				Curve curve = curves.get(j);
				String id = null;
				String taskId = "";
				DataGenerator dg = sedml.getDataGeneratorWithId(curve.getYDataReference());
				if (dg != null)
				{
					// TODO: do not handle multiple variables yet
					//if (dg.getListOfVariables().size()!=1) continue;
					for (Variable var : dg.getListOfVariables()) {
						//Variable var = dg.getListOfVariables().get(0);
						// TODO: hack to deal with concentration calculations
						if (var.isVariable() && 
								(dg.getListOfVariables().size()==1 || 
									!var.getTarget().contains("compartment"))) {
							id = sbmlSupport.getIdFromXPathIdentifer(var.getTarget());
							if (id==null) continue;
							if (analysisId==null) {
								taskId = var.getReference();
								id += " (" + taskId + ", " + getDataSet(dg) + ")";
							} else {
								if (var.getReference().equals(analysisId)) {
									id += " (" + getDataSet(dg) + ")";
								} else {
									taskId = var.getReference().replace(analysisId+"__", "");
									id += " (" + taskId + ", " + getDataSet(dg) + ")";
								}
							}
							break;
						}
					}
				}
				else
				{
					continue;
				}
				String name = null;
				name = curve.getName();
				if (name==null || name.equals("")) {
					name = id;
				}
				int xNumber = 0;
				DataGenerator xdg = sedml.getDataGeneratorWithId(curve.getXDataReference());
				XId = "time";
				String connectedDefault = "true";
				if (xdg != null)
				{
					// TODO: do not handle multiple variables yet
					//if (xdg.getListOfVariables().size()!=1) continue;
					for (Variable var : xdg.getListOfVariables()) {
						//Variable var = dg.getListOfVariables().get(0);
						if (var.isVariable()) {
							XId = sbmlSupport.getIdFromXPathIdentifer(var.getTarget());
							// TODO: need to handle this better with array, not combobox
							if (XVariable != null) {
								xNumber = XVariable.getSelectedIndex();
								if (xNumber==-1) xNumber=0;
							} else {
								xNumber = 0;
							}
							connectedDefault = "false";
							if (chart.getXYPlot().getDomainAxis().getLabel().equals("time")) {	
								chart.getXYPlot().getDomainAxis().setLabel(XId);
							}
							break;
						} 
					}
				}
				LogX = curve.getLogX();
				LogY = curve.getLogY();
				boolean connected = SEDMLutilities.getSEDBaseAnnotation(curve, "tsdGraph", "connected", connectedDefault).equals("true");
				boolean filled = SEDMLutilities.getSEDBaseAnnotation(curve, "tsdGraph", "filled", "true").equals("true");
				boolean visible = SEDMLutilities.getSEDBaseAnnotation(curve, "tsdGraph", "visible", "true").equals("true");
				String paint = SEDMLutilities.getSEDBaseAnnotation(curve, "tsdGraph", "paint", colorlist[j % numColors]);
				String shape = SEDMLutilities.getSEDBaseAnnotation(curve, "tsdGraph", "shape", shapeList[j % numShapes]);
				graphed.add(new GraphSpecies(ShapeMap.getShapeMap().get(shape), paint, filled, visible, connected, getRunNumber(dg), 
						XId, id, name, xNumber, j, taskId.replace("__", "/")));
			}
		}
		else if (output.isReport())
		{
			Report report = (Report) output;
			if (report.getName() != null) {
				chart.setTitle(report.getName());
			} else {
				chart.setTitle(report.getId());
			}

			chart.setBackgroundPaint(new Color(Integer.parseInt(SEDMLutilities.
				getSEDBaseAnnotation(report, "histogram", "chart_background_paint", "" + new Color(238, 238, 238).getRGB()))));
			chart.getPlot().setBackgroundPaint(new Color(Integer.parseInt(SEDMLutilities.
					getSEDBaseAnnotation(report, "histogram", "plot_background_paint", "" + (Color.WHITE).getRGB()))));
			chart.getCategoryPlot().setRangeGridlinePaint(new Color(Integer.parseInt(SEDMLutilities.
					getSEDBaseAnnotation(report, "histogram", "plot_range_grid_line_paint", "" + (Color.LIGHT_GRAY).getRGB()))));
			chart.getCategoryPlot().getDomainAxis().setLabel(SEDMLutilities.getSEDBaseAnnotation(report, "histogram", "x_axis", ""));
			chart.getCategoryPlot().getRangeAxis().setLabel(SEDMLutilities.getSEDBaseAnnotation(report, "histogram", "y_axis", ""));	
			((BarRenderer) chart.getCategoryPlot().getRenderer()).setShadowVisible(
					SEDMLutilities.getSEDBaseAnnotation(report, "histogram", "shadow", "false").equals("true"));
			visibleLegend = (SEDMLutilities.getSEDBaseAnnotation(report, "histogram", "visibleLegend", "true").equals("true"));
			timeSeriesPlot = false;
			if (SEDMLutilities.getSEDBaseAnnotation(report, "histogram", "gradient", "false").equals("true")) {
				((BarRenderer) chart.getCategoryPlot().getRenderer()).setBarPainter(new GradientBarPainter());
			} else {
				((BarRenderer) chart.getCategoryPlot().getRenderer()).setBarPainter(new StandardBarPainter());
			}
			
			List<DataSet> dataSets = report.getListOfDataSets();
			for (int j = 0; j < dataSets.size(); j++)
			{
				DataSet dataSet = dataSets.get(j);
				DataGenerator dg = sedml.getDataGeneratorWithId(dataSet.getDataReference());
				String id = "";
				String taskId = "";
				if (dg != null)
				{
					// TODO: do not handle multiple variables yet
					//if (dg.getListOfVariables().size()!=1) continue;
					for (Variable var : dg.getListOfVariables()) {
						//Variable var = dg.getListOfVariables().get(0);
						if (var.isVariable()) {
							id = sbmlSupport.getIdFromXPathIdentifer(var.getTarget());
							if (id==null) continue;
							// TODO: does not appear to handle subtasks
							if (analysisId==null) {
								taskId = var.getReference();
								id += " (" + taskId + ")";
							} else {
								id += "";
							}
							break;
						}
					}
				}
				else
				{
					continue;
				}
				String name = null;
				name = dataSet.getName();
				if (name==null || name.equals("")) {
					name = id;
				}
				Paint paint = null;
				String color = SEDMLutilities.getSEDBaseAnnotation(dataSet, "dataSet", "paint", colorlist[j % numColors]);
				if (color.startsWith("Custom_")) {
					paint = new Color(Integer.parseInt(color.replace("Custom_", "")));
				} else {
					paint = ColorMap.getColorMap().get(color);
				}
				probGraphed.add(new GraphProbs(paint, color, id, name, j, taskId));
			}
		}
		return true;
	}
	
	public void readProbSpecies(String file) {
		graphProbs = new ArrayList<String>();
		ArrayList<String> data = new ArrayList<String>();
		try {
			Scanner s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				String[] ss = s.nextLine().split(" ");
				if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0")) {
					s.close();
					return;
				}
				if (data.size() == 0) {
					for (String add : ss) {
						data.add(add);
					}
				}
				else {
					for (int i = 0; i < ss.length; i++) {
						data.set(i, data.get(i) + " " + ss[i]);
					}
				}
			}
			s.close();
		}
		catch (Exception e) {
		}
		for (String s : data) {
			if (!s.split(" ")[0].equals("#total")) {
				graphProbs.add(s.split(" ")[0].replace("-", ""));
			}
		}
	}

	public double[] readProbs(String file) {
		ArrayList<String> data = new ArrayList<String>();
		try {
			Scanner s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				String[] ss = s.nextLine().split(" ");
				if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0")) {
					s.close();
					return new double[0];
				}
				if (data.size() == 0) {
					for (String add : ss) {
						data.add(add);
					}
				}
				else {
					for (int i = 0; i < ss.length; i++) {
						data.set(i, data.get(i) + " " + ss[i]);
					}
				}
			}
			s.close();
		}
		catch (Exception e) {
		}
		double[] dataSet = new double[data.size()];
		double total = 0;
		int i = 0;
		if (data.get(0).split(" ")[0].equals("#total")) {
			total = Double.parseDouble(data.get(0).split(" ")[1]);
			i = 1;
		}
		for (; i < data.size(); i++) {
			if (total == 0) {
				dataSet[i] = Double.parseDouble(data.get(i).split(" ")[1]);
			}
			else {
				dataSet[i - 1] = 100 * ((Double.parseDouble(data.get(i).split(" ")[1])) / total);
			}
		}
		return dataSet;
	}

	// TODO: should wrap all calls to readData with this
/*	Gui.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	Gui.frame.setCursor(null);*/

	
	/**
	 * This public helper method parses the output file of ODE, monte carlo, and
	 * markov abstractions.
	 */
	public ArrayList<ArrayList<Double>> readData(String file, String label, String directory, boolean warning) {
		warn = warning;
		String[] s = GlobalConstants.splitPath(file);
		String getLast = s[s.length - 1];
		String stem = "";
		int t = 0;
		try {
			while (!Character.isDigit(getLast.charAt(t))) {
				stem += getLast.charAt(t);
				t++;
			}
		} catch (StringIndexOutOfBoundsException e) {
			
		}
		if ((label.contains("average") && file.contains("mean")) || (label.contains("variance") && file.contains("variance"))
				|| (label.contains("deviation") && file.contains("standard_deviation"))) {
			
			TSDParser p = new TSDParser(file, warn);
			warn = p.getWarning();
			graphSpecies = p.getSpecies();
			ArrayList<ArrayList<Double>> data = p.getData();
			if (learnSpecs != null) {
				for (String spec : learnSpecs) {
					if (!graphSpecies.contains(spec)) {
						graphSpecies.add(spec);
						ArrayList<Double> d = new ArrayList<Double>();
						for (int i = 0; i < data.get(0).size(); i++) {
							d.add(0.0);
						}
						data.add(d);
					}
				}
				for (int i = 1; i < graphSpecies.size(); i++) {
					if (!learnSpecs.contains(graphSpecies.get(i))) {
						graphSpecies.remove(i);
						data.remove(i);
						i--;
					}
				}
			}
			else if (averageOrder != null) {
				for (String spec : averageOrder) {
					if (!graphSpecies.contains(spec)) {
						graphSpecies.add(spec);
						ArrayList<Double> d = new ArrayList<Double>();
						for (int i = 0; i < data.get(0).size(); i++) {
							d.add(0.0);
						}
						data.add(d);
					}
				}
				for (int i = 1; i < graphSpecies.size(); i++) {
					if (!averageOrder.contains(graphSpecies.get(i))) {
						graphSpecies.remove(i);
						data.remove(i);
						i--;
					}
				}
			}
			return data;
		}
		if (label.contains("average") || label.contains("variance") || label.contains("deviation")) {
			ArrayList<String> runs = new ArrayList<String>();
			if (directory == null) {
				String[] files = new File(outDir).list();
				for (String f : files) {
					if (f.contains(stem) && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8))) {
						runs.add(f);
					}
				}
			}
			else {
				String[] files = new File(outDir + File.separator + directory).list();
				for (String f : files) {
					if (f.contains(stem) && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8))) {
						runs.add(f);
					}
				}
			}
			boolean outputFile;
			if (directory == null) {
				outputFile = !(new File(outDir + File.separator + "running").exists());
			}
			else {
				outputFile = !(new File(outDir + File.separator + directory + File.separator + "running").exists());
			}
			if (label.contains("average")) {
				if (directory == null) {
					outputFile = outputFile && !(new File(outDir + File.separator + "mean.tsd").exists());
				}
				else {
					outputFile = outputFile
							&& !(new File(outDir + File.separator + directory + File.separator + "mean.tsd").exists());
				}
				return calculateAverageVarianceDeviation(runs, 0, directory, warn, outputFile);
			}
			else if (label.contains("variance")) {
				if (directory == null) {
					outputFile = outputFile && !(new File(outDir + File.separator + "variance.tsd").exists());
				}
				else {
					outputFile = outputFile
							&& !(new File(outDir + File.separator + directory + File.separator + "variance.tsd").exists());
				}
				return calculateAverageVarianceDeviation(runs, 1, directory, warn, outputFile);
			}
			else {
				if (directory == null) {
					outputFile = outputFile && !(new File(outDir + File.separator + "standard_deviation.tsd").exists());
				}
				else {
					outputFile = outputFile
							&& !(new File(outDir + File.separator + directory + File.separator + "standard_deviation.tsd")
									.exists());
				}
				return calculateAverageVarianceDeviation(runs, 2, directory, warn, outputFile);
			}
		}
		
		DTSDParser dtsdParser;
		TSDParser p;
		ArrayList<ArrayList<Double>> data;
		
		if (file.contains(".dtsd")) {
			
			dtsdParser = new DTSDParser(file);
			warn = false;
			graphSpecies = dtsdParser.getSpecies();
			data = dtsdParser.getData();
		}
		else {
			p = new TSDParser(file, warn);
			warn = p.getWarning();
			graphSpecies = p.getSpecies();
			data = p.getData();
		}
		
		if (learnSpecs != null) {
			for (String spec : learnSpecs) {
				if (!graphSpecies.contains(spec)) {
					graphSpecies.add(spec);
					ArrayList<Double> d = new ArrayList<Double>();
					for (int i = 0; i < data.get(0).size(); i++) {
						d.add(0.0);
					}
					data.add(d);
				}
			}
			for (int i = 1; i < graphSpecies.size(); i++) {
				if (!learnSpecs.contains(graphSpecies.get(i))) {
					graphSpecies.remove(i);
					data.remove(i);
					i--;
				}
			}
		}
		else if (averageOrder != null) {
			for (String spec : averageOrder) {
				if (!graphSpecies.contains(spec)) {
					graphSpecies.add(spec);
					ArrayList<Double> d = new ArrayList<Double>();
					for (int i = 0; i < data.get(0).size(); i++) {
						d.add(0.0);
					}
					data.add(d);
				}
			}
			for (int i = 1; i < graphSpecies.size(); i++) {
				if (!averageOrder.contains(graphSpecies.get(i))) {
					graphSpecies.remove(i);
					data.remove(i);
					i--;
				}
			}
		}
		return data;
	}
	
	public ArrayList<ArrayList<Double>> calculateAverageVarianceDeviation(ArrayList<String> files, int choice, String directory, boolean warning,
			boolean output) {
		if (files.size() > 0) {
			ArrayList<ArrayList<Double>> average = new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<Double>> variance = new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<Double>> deviation = new ArrayList<ArrayList<Double>>();
			try {
				warn = warning;
				// TSDParser p = new TSDParser(startFile, biomodelsim, false);
				ArrayList<ArrayList<Double>> data;
				if (directory == null) {
					data = readData(outDir + File.separator + files.get(0), "", directory, warn);
				}
				else {
					data = readData(outDir + File.separator + directory + File.separator + files.get(0), "", directory, warn);
				}
				averageOrder = graphSpecies;
				boolean first = true;
				for (int i = 0; i < graphSpecies.size(); i++) {
					average.add(new ArrayList<Double>());
					variance.add(new ArrayList<Double>());
				}
				HashMap<Double, Integer> dataCounts = new HashMap<Double, Integer>();
				// int count = 0;
				for (String run : files) {
					if (directory == null) {
						if (new File(outDir + File.separator + run).exists()) {
							ArrayList<ArrayList<Double>> newData = readData(outDir + File.separator + run, "", directory, warn);
							data = new ArrayList<ArrayList<Double>>();
							for (int i = 0; i < averageOrder.size(); i++) {
								for (int k = 0; k < graphSpecies.size(); k++) {
									if (averageOrder.get(i).equals(graphSpecies.get(k))) {
										data.add(newData.get(k));
										break;
									}
								}
							}
						}
					}
					else {
						if (new File(outDir + File.separator + directory + File.separator + run).exists()) {
							ArrayList<ArrayList<Double>> newData = readData(outDir + File.separator + directory + File.separator + run, "", directory, warn);
							data = new ArrayList<ArrayList<Double>>();
							for (int i = 0; i < averageOrder.size(); i++) {
								for (int k = 0; k < graphSpecies.size(); k++) {
									if (averageOrder.get(i).equals(graphSpecies.get(k))) {
										data.add(newData.get(k));
										break;
									}
								}
							}
						}
					}
					// ArrayList<ArrayList<Double>> data = p.getData();
					for (int k = 0; k < data.get(0).size(); k++) {
						if (first) {
							double put;
							if (k == data.get(0).size() - 1 && k >= 2) {
								if (data.get(0).get(k) - data.get(0).get(k - 1) != data.get(0).get(k - 1) - data.get(0).get(k - 2)) {
									put = data.get(0).get(k - 1) + (data.get(0).get(k - 1) - data.get(0).get(k - 2));
									dataCounts.put(put, 1);
								}
								else {
									put = (data.get(0)).get(k);
									dataCounts.put(put, 1);
								}
							}
							else {
								put = (data.get(0)).get(k);
								dataCounts.put(put, 1);
							}
							for (int i = 0; i < data.size(); i++) {
								if (i == 0) {
									variance.get(i).add((data.get(i)).get(k));
									average.get(i).add(put);
								}
								else {
									variance.get(i).add(0.0);
									average.get(i).add((data.get(i)).get(k));
								}
							}
						}
						else {
							int index = -1;
							double put;
							if (k == data.get(0).size() - 1 && k >= 2) {
								if (data.get(0).get(k) - data.get(0).get(k - 1) != data.get(0).get(k - 1) - data.get(0).get(k - 2)) {
									put = data.get(0).get(k - 1) + (data.get(0).get(k - 1) - data.get(0).get(k - 2));
								}
								else {
									put = (data.get(0)).get(k);
								}
							}
							else if (k == data.get(0).size() - 1 && k == 1) {
								if (average.get(0).size() > 1) {
									put = (average.get(0)).get(k);
								}
								else {
									put = (data.get(0)).get(k);
								}
							}
							else {
								put = (data.get(0)).get(k);
							}
							if (average.get(0).contains(put)) {
								index = average.get(0).indexOf(put);
								int count = dataCounts.get(put);
								dataCounts.put(put, count + 1);
								for (int i = 1; i < data.size(); i++) {
									double old = (average.get(i)).get(index);
									(average.get(i)).set(index, old + (((data.get(i)).get(k) - old) / (count + 1)));
									double newMean = (average.get(i)).get(index);
									double vary = (((count - 1) * (variance.get(i)).get(index)) + ((data.get(i)).get(k) - newMean)
											* ((data.get(i)).get(k) - old))
											/ count;
									(variance.get(i)).set(index, vary);
								}
							}
							else {
								dataCounts.put(put, 1);
								for (int a = 0; a < average.get(0).size(); a++) {
									if (average.get(0).get(a) > put) {
										index = a;
										break;
									}
								}
								if (index == -1) {
									index = average.get(0).size() - 1;
								}
								average.get(0).add(put);
								variance.get(0).add(put);
								for (int a = 1; a < average.size(); a++) {
									average.get(a).add(data.get(a).get(k));
									variance.get(a).add(0.0);
								}
								if (index != average.get(0).size() - 1) {
									for (int a = average.get(0).size() - 2; a >= 0; a--) {
										if (average.get(0).get(a) > average.get(0).get(a + 1)) {
											for (int b = 0; b < average.size(); b++) {
												double temp = average.get(b).get(a);
												average.get(b).set(a, average.get(b).get(a + 1));
												average.get(b).set(a + 1, temp);
												temp = variance.get(b).get(a);
												variance.get(b).set(a, variance.get(b).get(a + 1));
												variance.get(b).set(a + 1, temp);
											}
										}
										else {
											break;
										}
									}
								}
							}
						}
					}
					first = false;
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
				averageOrder = null;
			}
			catch (Exception e) {
				message.setErrorDialog("Error", "Unable to output average, variance, and standard deviation!");
				this.notifyObservers(message);
			}
			if (output) {
				DataParser m = new DataParser(graphSpecies, average);
				DataParser d = new DataParser(graphSpecies, deviation);
				DataParser v = new DataParser(graphSpecies, variance);
				if (directory == null) {
					m.outputTSD(outDir + File.separator + "mean.tsd");
					v.outputTSD(outDir + File.separator + "variance.tsd");
					d.outputTSD(outDir + File.separator + "standard_deviation.tsd");
				}
				else {
					m.outputTSD(outDir + File.separator + directory + File.separator + "mean.tsd");
					v.outputTSD(outDir + File.separator + directory + File.separator + "variance.tsd");
					d.outputTSD(outDir + File.separator + directory + File.separator + "standard_deviation.tsd");
				}
			}
			if (choice == 0) {
				return average;
			}
			else if (choice == 1) {
				return variance;
			}
			else {
				return deviation;
			}
		}
		return null;
	}

	public void readGraphSpecies(String file) {
		if (file.contains(".dtsd"))
			graphSpecies = (new DTSDParser(file)).getSpecies();
		else
			graphSpecies = new TSDParser(file, true).getSpecies();
		/*
		if (startsWith!=null) {
			for (int i = 1; i < graphSpecies.size(); i++) {
				if (!graphSpecies.get(i).startsWith(startsWith+"__")) {
					graphSpecies.remove(i);
					i--;
				}
			}
		}
		*/
		if (learnSpecs != null) {
			for (String spec : learnSpecs) {
				if (!graphSpecies.contains(spec)) {
					graphSpecies.add(spec);
				}
			}
			for (int i = 1; i < graphSpecies.size(); i++) {
				if (!learnSpecs.contains(graphSpecies.get(i))) {
					graphSpecies.remove(i);
					i--;
				}
			}
		}
	}
	
	public void loadDataFiles(double minX,double maxX,double scaleX,double minY,double maxY,double scaleY) {
		if (timeSeriesPlot) {
			ArrayList<XYSeries> graphDataSet = new ArrayList<XYSeries>();
			XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
			int thisOne = -1;
			for (int i = 1; i < graphed.size(); i++) {
				GraphSpecies index = graphed.get(i);
				int j = i;
				while ((j > 0) && (graphed.get(j - 1).getSpecies().compareToIgnoreCase(index.getSpecies()) > 0)) {
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
					if (!g.getRunNumber().equals("Average") && !g.getRunNumber().equals("Variance") && !g.getRunNumber().equals("Standard Deviation")
							&& !g.getRunNumber().equals("Termination Time") && !g.getRunNumber().equals("Percent Termination")
							&& !g.getRunNumber().equals("Constraint Termination")) {
						if (new File(outDir + File.separator + g.getRunNumber() + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
							ArrayList<ArrayList<Double>> data;
							if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
								data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								readGraphSpecies(outDir + File.separator + g.getRunNumber() + "." + printer_id.substring(0, printer_id.length() - 8));
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									int j = i;
									while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
								}
							}
							else {
								data = readData(outDir + File.separator + g.getRunNumber() + "." + printer_id.substring(0, printer_id.length() - 8),
										g.getRunNumber(), null, false);
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									ArrayList<Double> index2 = data.get(i);
									int j = i;
									while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										data.set(j, data.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
									data.set(j, index2);
								}
								allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
							}
							boolean set = false;
							for (int i = 1; i < graphSpecies.size(); i++) {
								String compare = g.getID().replace(" (", "~");
								if (graphSpecies.get(i).equals(compare.split("~")[0].trim())) {
									g.setNumber(i - 1);
									set = true;
								}
								if (!g.getXid().equals("time") && graphSpecies.get(i).equals(g.getXid())) {
									g.setXNumber(i);
									set = true;
								}
							}
							if (g.getNumber() + 1 < graphSpecies.size() && set) {
								graphDataSet.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										if (i < data.get(g.getXNumber()).size() && i < data.get(g.getNumber() + 1).size()) {
											graphDataSet.get(graphDataSet.size() - 1).add((data.get(g.getXNumber())).get(i),
													(data.get(g.getNumber() + 1)).get(i));
										}
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
					else {
						boolean ableToGraph = false;
						try {
							for (String s : new File(outDir).list()) {
								if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
									ableToGraph = true;
								}
							}
						}
						catch (Exception e) {
							ableToGraph = false;
						}
						if (!ableToGraph) {
							if (g.getRunNumber().equals("Average")
									&& new File(outDir + File.separator + "mean" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Variance")
									&& new File(outDir + File.separator + "variance" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Standard Deviation")
									&& new File(outDir + File.separator + "standard_deviation" + "." + printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Termination Time")
									&& new File(outDir + File.separator + "term-time" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Percent Termination")
									&& new File(outDir + File.separator + "percent-term-time" + "." + printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Constraint Termination")
									&& new File(outDir + File.separator + "sim-rep" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Bifurcation Statistics")
									&& new File(outDir + File.separator + "bifurcation" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
						}
						if (ableToGraph) {
							int nextOne = 1;
							ArrayList<ArrayList<Double>> data;
							if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
								data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								if (g.getRunNumber().equals("Average")
										&& new File(outDir + File.separator + "mean" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + "mean" + "." + printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Variance")
										&& new File(outDir + File.separator + "variance" + "." + printer_id.substring(0, printer_id.length() - 8),null)
												.exists()) {
									readGraphSpecies(outDir + File.separator + "variance" + "." + printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Standard Deviation")
										&& new File(outDir + File.separator + "standard_deviation" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + "standard_deviation" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Termination Time")
										&& new File(outDir + File.separator + "term-time" + "." + printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									readGraphSpecies(outDir + File.separator + "term-time" + "." + printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Percent Termination")
										&& new File(outDir + File.separator + "percent-term-time" + "." + printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									readGraphSpecies(outDir + File.separator + "percent-term-time" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Constraint Termination")
										&& new File(outDir + File.separator + "sim-rep" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + "sim-rep" + "." + printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Bifurcation Statistics")
										&& new File(outDir + File.separator + "bifurcation" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + "bifurcation" + "." + printer_id.substring(0, printer_id.length() - 8));
								}
								else {
									while (!new File(outDir + File.separator + "run-" + nextOne + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
										nextOne++;
									}
									readGraphSpecies(outDir + File.separator + "run-" + nextOne + "." + printer_id.substring(0, printer_id.length() - 8));
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
							}
							else {
								if (g.getRunNumber().equals("Average")
										&& new File(outDir + File.separator + "mean" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(outDir + File.separator + "mean." + printer_id.substring(0, printer_id.length() - 8), g.getRunNumber()
											.toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Variance")
										&& new File(outDir + File.separator + "variance" + "." + printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + File.separator + "variance." + printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Standard Deviation")
										&& new File(outDir + File.separator + "standard_deviation" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(outDir + File.separator + "standard_deviation." + printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Termination Time")
										&& new File(outDir + File.separator + "term-time" + "." + printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + File.separator + "term-time." + printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Percent Termination")
										&& new File(outDir + File.separator + "percent-term-time" + "." + printer_id.substring(0, printer_id.length() - 8))
												.exists()) {
									data = readData(outDir + File.separator + "percent-term-time." + printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Constraint Termination")
										&& new File(outDir + File.separator + "sim-rep" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(outDir + File.separator + "sim-rep." + printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Bifurcation Statistics")
										&& new File(outDir + File.separator + "bifurcation" + "." + printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(outDir + File.separator + "bifurcation." + printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								else {
									while (!new File(outDir + File.separator + "run-" + nextOne + "." + printer_id.substring(0, printer_id.length() - 8))
											.exists()) {
										nextOne++;
									}
									data = readData(outDir + File.separator + "run-" + nextOne + "." + printer_id.substring(0, printer_id.length() - 8), g
											.getRunNumber().toLowerCase(), null, false);
								}
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									ArrayList<Double> index2 = data.get(i);
									int j = i;
									while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										data.set(j, data.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
									data.set(j, index2);
								}
								allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
							}
							boolean set = false;
							for (int i = 1; i < graphSpecies.size(); i++) {
								String compare = g.getID().replace(" (", "~");
								if (graphSpecies.get(i).equals(compare.split("~")[0].trim())) {
									g.setNumber(i - 1);
									set = true;
								}
								if (!g.getXid().equals("time") && graphSpecies.get(i).equals(g.getXid())) {
									g.setXNumber(i);
									set = true;
								}
							}
							if (g.getNumber() + 1 < graphSpecies.size() && set) {
								graphDataSet.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										if (i < data.get(g.getXNumber()).size() && i < data.get(g.getNumber() + 1).size()) {
											graphDataSet.get(graphDataSet.size() - 1).add((data.get(g.getXNumber())).get(i),
													(data.get(g.getNumber() + 1)).get(i));
										}
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
				}
				else {
					thisOne++;
					rend.setSeriesVisible(thisOne, true);
					rend.setSeriesLinesVisible(thisOne, g.getConnected());
					rend.setSeriesShapesFilled(thisOne, g.getFilled());
					rend.setSeriesShapesVisible(thisOne, g.getVisible());
					rend.setSeriesPaint(thisOne, g.getShapeAndPaint().getPaint());
					rend.setSeriesShape(thisOne, g.getShapeAndPaint().getShape());
					if (!g.getRunNumber().equals("Average") && !g.getRunNumber().equals("Variance") && !g.getRunNumber().equals("Standard Deviation")
							&& !g.getRunNumber().equals("Termination Time") && !g.getRunNumber().equals("Percent Termination")
							&& !g.getRunNumber().equals("Constraint Termination")) {
						if (new File(outDir + File.separator + g.getDirectory() + File.separator + g.getRunNumber() + "."
								+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
							ArrayList<ArrayList<Double>> data;
							if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
								data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + g.getRunNumber() + "."
										+ printer_id.substring(0, printer_id.length() - 8));
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									int j = i;
									while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
								}
							}
							else {
								data = readData(
										outDir + File.separator + g.getDirectory() + File.separator + g.getRunNumber() + "."
												+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber(), g.getDirectory(), false);
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									ArrayList<Double> index2 = data.get(i);
									int j = i;
									while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										data.set(j, data.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
									data.set(j, index2);
								}
								allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
							}
							boolean set = false;
							for (int i = 1; i < graphSpecies.size(); i++) {
								String compare = g.getID().replace(" (", "~");
								if (graphSpecies.get(i).equals(compare.split("~")[0].trim())) {
									g.setNumber(i - 1);
									set = true;
								}
								if (!g.getXid().equals("time") && graphSpecies.get(i).equals(g.getXid())) {
									g.setXNumber(i);
									set = true;
								}
							}
							if (g.getNumber() + 1 < graphSpecies.size() && set) {
								graphDataSet.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										if (i < data.get(g.getXNumber()).size() && i < data.get(g.getNumber() + 1).size()) {
											graphDataSet.get(graphDataSet.size() - 1).add((data.get(g.getXNumber())).get(i),
													(data.get(g.getNumber() + 1)).get(i));
										}
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
							unableToGraph.add(g);
							thisOne--;
						}
					}
					else {
						boolean ableToGraph = false;
						try {
							for (String s : new File(outDir + File.separator + g.getDirectory()).list()) {
								if (s.length() > 3 && s.substring(0, 4).equals("run-")) {
									ableToGraph = true;
								}
							}
						}
						catch (Exception e) {
							ableToGraph = false;
						}
						if (!ableToGraph) {
							if (g.getRunNumber().equals("Average")
									&& new File(outDir + File.separator + g.getDirectory() + File.separator + "mean" + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Variance")
									&& new File(outDir + File.separator + g.getDirectory() + File.separator + "variance" + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Standard Deviation")
									&& new File(outDir + File.separator + g.getDirectory() + File.separator + "standard_deviation" + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Termination Time")
									&& new File(outDir + File.separator + g.getDirectory() + File.separator + "term-time" + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Percent Termination")
									&& new File(outDir + File.separator + g.getDirectory() + File.separator + "percent-term-time" + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Constraint Termination")
									&& new File(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep" + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
							else if (g.getRunNumber().equals("Bifurcation Statistics")
									&& new File(outDir + File.separator + g.getDirectory() + File.separator + "bifurcation" + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
								ableToGraph = true;
							}
						}
						if (ableToGraph) {
							int nextOne = 1;
							ArrayList<ArrayList<Double>> data;
							if (allData.containsKey(g.getRunNumber() + " " + g.getDirectory())) {
								data = allData.get(g.getRunNumber() + " " + g.getDirectory());
								if (g.getRunNumber().equals("Average")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "mean" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + "mean" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Variance")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "variance" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + "variance" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Standard Deviation")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "standard_deviation" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + "standard_deviation" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Termination Time")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "term-time" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + "term-time" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Percent Termination")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "percent-term-time" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + "percent-term-time" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Constraint Termination")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else if (g.getRunNumber().equals("Bifurcation Statistics")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "bifurcation" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + "bifurcation" + "."
											+ printer_id.substring(0, printer_id.length() - 8));
								}
								else {
									while (!new File(outDir + File.separator + g.getDirectory() + File.separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
										nextOne++;
									}
									readGraphSpecies(outDir + File.separator + g.getDirectory() + File.separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8));
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
							}
							else {
								if (g.getRunNumber().equals("Average")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "mean" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(
											outDir + File.separator + g.getDirectory() + File.separator + "mean."
													+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Variance")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "variance" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(
											outDir + File.separator + g.getDirectory() + File.separator + "variance."
													+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Standard Deviation")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "standard_deviation" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(
											outDir + File.separator + g.getDirectory() + File.separator + "standard_deviation."
													+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Termination Time")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "term-time" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(
											outDir + File.separator + g.getDirectory() + File.separator + "term-time."
													+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Percent Termination")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "percent-term-time" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(
											outDir + File.separator + g.getDirectory() + File.separator + "percent-term-time."
													+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Constraint Termination")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(
											outDir + File.separator + g.getDirectory() + File.separator + "sim-rep."
													+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber().toLowerCase(), null, false);
								}
								else if (g.getRunNumber().equals("Bifurcation Statistics")
										&& new File(outDir + File.separator + g.getDirectory() + File.separator + "bifurcation" + "."
												+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
									data = readData(
											outDir + File.separator + g.getDirectory() + File.separator + "bifurcation."
													+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber().toLowerCase(), null, false);
								}
								else {
									while (!new File(outDir + File.separator + g.getDirectory() + File.separator + "run-" + nextOne + "."
											+ printer_id.substring(0, printer_id.length() - 8)).exists()) {
										nextOne++;
									}
									data = readData(
											outDir + File.separator + g.getDirectory() + File.separator + "run-" + nextOne + "."
													+ printer_id.substring(0, printer_id.length() - 8), g.getRunNumber().toLowerCase(),
											g.getDirectory(), false);
								}
								for (int i = 2; i < graphSpecies.size(); i++) {
									String index = graphSpecies.get(i);
									ArrayList<Double> index2 = data.get(i);
									int j = i;
									while ((j > 1) && graphSpecies.get(j - 1).compareToIgnoreCase(index) > 0) {
										graphSpecies.set(j, graphSpecies.get(j - 1));
										data.set(j, data.get(j - 1));
										j = j - 1;
									}
									graphSpecies.set(j, index);
									data.set(j, index2);
								}
								allData.put(g.getRunNumber() + " " + g.getDirectory(), data);
							}
							boolean set = false;
							for (int i = 1; i < graphSpecies.size(); i++) {
								String compare = g.getID().replace(" (", "~");
								if (graphSpecies.get(i).equals(compare.split("~")[0].trim())) {
									g.setNumber(i - 1);
									set = true;
								}
								if (!g.getXid().equals("time") && graphSpecies.get(i).equals(g.getXid())) {
									g.setXNumber(i);
									set = true;
								}
							}
							if (g.getNumber() + 1 < graphSpecies.size() && set) {
								graphDataSet.add(new XYSeries(g.getSpecies()));
								if (data.size() != 0) {
									for (int i = 0; i < (data.get(0)).size(); i++) {
										if (i < data.get(g.getXNumber()).size() && i < data.get(g.getNumber() + 1).size()) {
											graphDataSet.get(graphDataSet.size() - 1).add((data.get(g.getXNumber())).get(i),
													(data.get(g.getNumber() + 1)).get(i));
										}
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
						else {
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
			for (int i = 0; i < graphDataSet.size(); i++) {
				dataset.addSeries(graphDataSet.get(i));
			}
			chart.getXYPlot().setDataset(dataset);
			XYPlot plot = chart.getXYPlot();
			if (resize) {
				resize(dataset);
			}
			else {
				NumberAxis axis = (NumberAxis) plot.getRangeAxis();
				axis.setAutoTickUnitSelection(false);
				axis.setRange(minY, maxY);
				axis.setTickUnit(new NumberTickUnit(scaleY));
				axis = (NumberAxis) plot.getDomainAxis();
				axis.setAutoTickUnitSelection(false);
				axis.setRange(minX, maxX);
				axis.setTickUnit(new NumberTickUnit(scaleX));
			}
			chart.getXYPlot().setRenderer(rend);
		}
		else {
			BarRenderer rend = (BarRenderer) chart.getCategoryPlot().getRenderer();
			int thisOne = -1;
			for (int i = 1; i < probGraphed.size(); i++) {
				GraphProbs index = probGraphed.get(i);
				int j = i;
				while ((j > 0) && (probGraphed.get(j - 1).getSpecies().compareToIgnoreCase(index.getSpecies()) > 0)) {
					probGraphed.set(j, probGraphed.get(j - 1));
					j = j - 1;
				}
				probGraphed.set(j, index);
			}
			ArrayList<GraphProbs> unableToGraph = new ArrayList<GraphProbs>();
			DefaultCategoryDataset histDataset = new DefaultCategoryDataset();
			for (GraphProbs g : probGraphed) {
				if (g.getDirectory().equals("")) {
					thisOne++;
					rend.setSeriesPaint(thisOne, g.getPaint());
					if (new File(outDir + File.separator + "sim-rep.txt").exists()) {
						readProbSpecies(outDir + File.separator + "sim-rep.txt");
						double[] data = readProbs(outDir + File.separator + "sim-rep.txt");
						for (int i = 1; i < graphProbs.size(); i++) {
							String index = graphProbs.get(i);
							double index2 = data[i];
							int j = i;
							while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
								graphProbs.set(j, graphProbs.get(j - 1));
								data[j] = data[j - 1];
								j = j - 1;
							}
							graphProbs.set(j, index);
							data[j] = index2;
						}
						if (graphProbs.size() != 0) {
							if (graphProbs.contains(g.getID())) {
								for (int i = 0; i < graphProbs.size(); i++) {
									if (g.getID().equals(graphProbs.get(i))) {
										g.setNumber(i);
										histDataset.setValue(data[i], g.getSpecies(), "");
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
					}
					else {
						unableToGraph.add(g);
						thisOne--;
					}
				}
				else {
					thisOne++;
					rend.setSeriesPaint(thisOne, g.getPaint());
					if (new File(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep.txt").exists()) {
						readProbSpecies(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep.txt");
						double[] data = readProbs(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep.txt");
						for (int i = 1; i < graphProbs.size(); i++) {
							String index = graphProbs.get(i);
							double index2 = data[i];
							int j = i;
							while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
								graphProbs.set(j, graphProbs.get(j - 1));
								data[j] = data[j - 1];
								j = j - 1;
							}
							graphProbs.set(j, index);
							data[j] = index2;
						}
						if (graphProbs.size() != 0) {
							String compare = g.getID().replace(" (", "~");
							if (graphProbs.contains(compare.split("~")[0].trim())) {
								for (int i = 0; i < graphProbs.size(); i++) {
									if (compare.split("~")[0].trim().equals(graphProbs.get(i))) {
										g.setNumber(i);
										histDataset.setValue(data[i], g.getSpecies(), "");
									}
								}
							}
							else {
								unableToGraph.add(g);
								thisOne--;
							}
						}
					}
					else {
						unableToGraph.add(g);
						thisOne--;
					}
				}
			}
			for (GraphProbs g : unableToGraph) {
				probGraphed.remove(g);
			}
		}
	}
	
	public void applyChartTheme() {
		final StandardChartTheme chartTheme = (StandardChartTheme) org.jfree.chart.StandardChartTheme
				.createJFreeTheme();

		final Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
		final Font oldLargeFont = chartTheme.getLargeFont();
		final Font oldRegularFont = chartTheme.getRegularFont();
		final Font oldSmallFont = chartTheme.getSmallFont();

		final Font extraLargeFont = new Font("Sans-serif", oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
		final Font largeFont = new Font("Sans-serif", oldLargeFont.getStyle(), oldLargeFont.getSize());
		final Font regularFont = new Font("Sans-serif", oldRegularFont.getStyle(), oldRegularFont.getSize());
		final Font smallFont = new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize());

		chartTheme.setExtraLargeFont(extraLargeFont);
		chartTheme.setLargeFont(largeFont);
		chartTheme.setRegularFont(regularFont);
		chartTheme.setSmallFont(smallFont);

		chartTheme.apply(chart);
	}
	
	public void loadProbDataFiles(String outDir) {
		BarRenderer rend = (BarRenderer) chart.getCategoryPlot().getRenderer();
		int thisOne = -1;
		for (int i = 1; i < probGraphed.size(); i++) {
			GraphProbs index = probGraphed.get(i);
			int j = i;
			while ((j > 0) && (probGraphed.get(j - 1).getSpecies().compareToIgnoreCase(index.getSpecies()) > 0)) {
				probGraphed.set(j, probGraphed.get(j - 1));
				j = j - 1;
			}
			probGraphed.set(j, index);
		}
		ArrayList<GraphProbs> unableToGraph = new ArrayList<GraphProbs>();
		DefaultCategoryDataset histDataset = new DefaultCategoryDataset();
		for (GraphProbs g : probGraphed) {
			if (g.getDirectory().equals("")) {
				thisOne++;
				rend.setSeriesPaint(thisOne, g.getPaint());
				if (new File(outDir + File.separator + "sim-rep.txt").exists()) {
					readProbSpecies(outDir + File.separator + "sim-rep.txt");
					double[] data = readProbs(outDir + File.separator + "sim-rep.txt");
					for (int i = 1; i < graphProbs.size(); i++) {
						String index = graphProbs.get(i);
						double index2 = data[i];
						int j = i;
						while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
							graphProbs.set(j, graphProbs.get(j - 1));
							data[j] = data[j - 1];
							j = j - 1;
						}
						graphProbs.set(j, index);
						data[j] = index2;
					}
					if (graphProbs.size() != 0) {
						for (int i = 0; i < graphProbs.size(); i++) {
							if (g.getID().equals(graphProbs.get(i))) {
								g.setNumber(i);
								histDataset.setValue(data[i], g.getSpecies(), "");
							}
						}
					}
				}
				else {
					unableToGraph.add(g);
					thisOne--;
				}
			}
			else {
				thisOne++;
				rend.setSeriesPaint(thisOne, g.getPaint());
				if (new File(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep.txt").exists()) {
					readProbSpecies(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep.txt");
					double[] data = readProbs(outDir + File.separator + g.getDirectory() + File.separator + "sim-rep.txt");
					for (int i = 1; i < graphProbs.size(); i++) {
						String index = graphProbs.get(i);
						double index2 = data[i];
						int j = i;
						while ((j > 0) && graphProbs.get(j - 1).compareToIgnoreCase(index) > 0) {
							graphProbs.set(j, graphProbs.get(j - 1));
							data[j] = data[j - 1];
							j = j - 1;
						}
						graphProbs.set(j, index);
						data[j] = index2;
					}
					if (graphProbs.size() != 0) {
						for (int i = 0; i < graphProbs.size(); i++) {
							String compare = g.getID().replace(" (", "~");
							if (compare.split("~")[0].trim().equals(graphProbs.get(i))) {
								histDataset.setValue(data[i], g.getSpecies(), "");
							}
						}
					}
				}
				else {
					unableToGraph.add(g);
					thisOne--;
				}
			}
		}
		for (GraphProbs g : unableToGraph) {
			probGraphed.remove(g);
		}
		chart.getCategoryPlot().setDataset(histDataset);
		chart.getCategoryPlot().setRenderer(rend);
	}
	
	/**
	 * Private method used to auto resize the graph.
	 */
	public void resize(XYSeriesCollection dataset) { 
		NumberFormat num = NumberFormat.getInstance();
		num.setMaximumFractionDigits(4);
		num.setGroupingUsed(false);
		XYPlot plot = chart.getXYPlot();
		
		XYItemRenderer rend = plot.getRenderer();
		double minY = Double.MAX_VALUE;
		double maxY = (-1)*Double.MAX_VALUE;
		double minX = Double.MAX_VALUE;
		double maxX = (-1)*Double.MAX_VALUE;
		Font rangeFont = chart.getXYPlot().getRangeAxis().getLabelFont();
		Font rangeTickFont = chart.getXYPlot().getRangeAxis().getTickLabelFont();
		Font domainFont = chart.getXYPlot().getDomainAxis().getLabelFont();
		Font domainTickFont = chart.getXYPlot().getDomainAxis().getTickLabelFont();
		if (LogY) {
			try {
				LogarithmicAxis rangeAxis = new LogarithmicAxis(chart.getXYPlot().getRangeAxis().getLabel());
				rangeAxis.setStrictValuesFlag(false);
				plot.setRangeAxis(rangeAxis);
			}
			catch (Exception e1) {
				message.setErrorDialog("Error", "Log plots are not allowed with data\nvalues less than or equal to zero.");
				this.notifyObservers(message);
				NumberAxis rangeAxis = new NumberAxis(chart.getXYPlot().getRangeAxis().getLabel());
				plot.setRangeAxis(rangeAxis);
				LogY = false;
			}
		} else {
			NumberAxis rangeAxis = new NumberAxis(chart.getXYPlot().getRangeAxis().getLabel());
			plot.setRangeAxis(rangeAxis);
		}
		if (LogX) {
			try {
				LogarithmicAxis domainAxis = new LogarithmicAxis(chart.getXYPlot().getDomainAxis().getLabel());
				domainAxis.setStrictValuesFlag(false);
				plot.setDomainAxis(domainAxis);
			}
			catch (Exception e1) {
				message.setErrorDialog("Error", "Log plots are not allowed with data\nvalues less than or equal to zero.");
				this.notifyObservers(message);
				NumberAxis domainAxis = new NumberAxis(chart.getXYPlot().getDomainAxis().getLabel());
				plot.setDomainAxis(domainAxis);
				LogX = false;
			}
		} else {
			NumberAxis domainAxis = new NumberAxis(chart.getXYPlot().getDomainAxis().getLabel());
			plot.setDomainAxis(domainAxis);
		}
		chart.getXYPlot().getDomainAxis().setLabelFont(domainFont);
		chart.getXYPlot().getDomainAxis().setTickLabelFont(domainTickFont);
		chart.getXYPlot().getRangeAxis().setLabelFont(rangeFont);
		chart.getXYPlot().getRangeAxis().setTickLabelFont(rangeTickFont);
		for (int j = 0; j < dataset.getSeriesCount(); j++) {
			XYSeries series = dataset.getSeries(j);
			double[][] seriesArray = series.toArray();
			Boolean visible = rend.getSeriesVisible(j);
			if (visible == null || visible.equals(true)) {
				for (int k = 0; k < series.getItemCount(); k++) {
					maxY = Math.max(seriesArray[1][k], maxY);
					minY = Math.min(seriesArray[1][k], minY);
					maxX = Math.max(seriesArray[0][k], maxX);
					minX = Math.min(seriesArray[0][k], minX);
				}
			}
		}
		NumberAxis axis = (NumberAxis) plot.getRangeAxis();
		if (minY == Double.MAX_VALUE || maxY == Double.MIN_VALUE) {
			axis.setRange(-1, 1);
		}
		else {
			if ((maxY - minY) < .001) {
				axis.setStandardTickUnits(new StandardTickUnitSource());
			}
			else {
				axis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
			}
			axis.setRange(minY - (Math.abs(minY) * .1), maxY + (Math.abs(maxY) * .1));
		}
		axis.setAutoTickUnitSelection(true);
		axis = (NumberAxis) plot.getDomainAxis();
		if (minX == Double.MAX_VALUE || maxX == Double.MIN_VALUE) {
			axis.setRange(-1, 1);
		}
		else {
			if ((maxX - minX) < .001) {
				axis.setStandardTickUnits(new StandardTickUnitSource());
			}
			else {
				axis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
			}
			if(minX == maxX)
			{
			  axis.setRange(minX - (Math.abs(minX) * .1), maxX + (Math.abs(maxX) * .1));
			}
			else
			{
	      axis.setRange(minX, maxX);
			}
		}
		axis.setAutoTickUnitSelection(true);
		if (visibleLegend) {
			if (chart.getLegend() == null) {
				chart.addLegend(legend);
			}
		}
		else {
			if (chart.getLegend() != null) {
				legend = chart.getLegend();
			}
			chart.removeLegend();
		}
	}
	
	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}

	/**
	 * @return the legend
	 */
	public LegendTitle getLegend() {
		return legend;
	}

	/**
	 * @param legend the legend to set
	 */
	public void setLegend(LegendTitle legend) {
		this.legend = legend;
	}

	/**
	 * @return the curData
	 */
	public XYSeriesCollection getCurData() {
		return curData;
	}

	/**
	 * @param curData the curData to set
	 */
	public void setCurData(XYSeriesCollection curData) {
		this.curData = curData;
	}

	/**
	 * @return the xMin
	 */
	public String getXMin() {
		return XMin;
	}

	/**
	 * @param xMin the xMin to set
	 */
	public void setXMin(String xMin) {
		XMin = xMin;
	}

	/**
	 * @return the xMax
	 */
	public String getXMax() {
		return XMax;
	}

	/**
	 * @param xMax the xMax to set
	 */
	public void setXMax(String xMax) {
		XMax = xMax;
	}

	/**
	 * @return the xScale
	 */
	public String getXScale() {
		return XScale;
	}

	/**
	 * @param xScale the xScale to set
	 */
	public void setXScale(String xScale) {
		XScale = xScale;
	}

	/**
	 * @return the yMin
	 */
	public String getYMin() {
		return YMin;
	}

	/**
	 * @param yMin the yMin to set
	 */
	public void setYMin(String yMin) {
		YMin = yMin;
	}

	/**
	 * @return the yMax
	 */
	public String getYMax() {
		return YMax;
	}

	/**
	 * @param yMax the yMax to set
	 */
	public void setYMax(String yMax) {
		YMax = yMax;
	}

	/**
	 * @return the yScale
	 */
	public String getYScale() {
		return YScale;
	}

	/**
	 * @param yScale the yScale to set
	 */
	public void setYScale(String yScale) {
		YScale = yScale;
	}

	/**
	 * @return the resize
	 */
	public Boolean getResize() {
		return resize;
	}

	/**
	 * @param resize the resize to set
	 */
	public void setResize(Boolean resize) {
		this.resize = resize;
	}

	/**
	 * @return the visibleLegend
	 */
	public Boolean getVisibleLegend() {
		return visibleLegend;
	}

	/**
	 * @param visibleLegend the visibleLegend to set
	 */
	public void setVisibleLegend(Boolean visibleLegend) {
		this.visibleLegend = visibleLegend;
	}

	/**
	 * @return the logY
	 */
	public Boolean getLogY() {
		return LogY;
	}

	/**
	 * @param logY the logY to set
	 */
	public void setLogY(Boolean logY) {
		LogY = logY;
	}

	/**
	 * @return the logX
	 */
	public Boolean getLogX() {
		return LogX;
	}

	/**
	 * @param logX the logX to set
	 */
	public void setLogX(Boolean logX) {
		LogX = logX;
	}

	/**
	 * @return the xId
	 */
	public String getXId() {
		return XId;
	}

	/**
	 * @param xId the xId to set
	 */
	public void setXId(String xId) {
		XId = xId;
	}

	/**
	 * @return the graphed
	 */
	public LinkedList<GraphSpecies> getGraphed() {
		return graphed;
	}

	/**
	 * @param graphed the graphed to set
	 */
	public void setGraphed(LinkedList<GraphSpecies> graphed) {
		this.graphed = graphed;
	}

	/**
	 * @return the probGraphed
	 */
	public LinkedList<GraphProbs> getProbGraphed() {
		return probGraphed;
	}

	/**
	 * @param probGraphed the probGraphed to set
	 */
	public void setProbGraphed(LinkedList<GraphProbs> probGraphed) {
		this.probGraphed = probGraphed;
	}

	/**
	 * @return the timeSeriesPlot
	 */
	public boolean isTimeSeriesPlot() {
		return timeSeriesPlot;
	}

	/**
	 * @param timeSeriesPlot the timeSeriesPlot to set
	 */
	public void setTimeSeriesPlot(boolean timeSeriesPlot) {
		this.timeSeriesPlot = timeSeriesPlot;
	}

	/**
	 * @return the graphSpecies
	 */
	public ArrayList<String> getGraphSpecies() {
		return graphSpecies;
	}

	/**
	 * @param graphSpecies the graphSpecies to set
	 */
	public void setGraphSpecies(ArrayList<String> graphSpecies) {
		this.graphSpecies = graphSpecies;
	}

	/**
	 * @return the graphProbs
	 */
	public ArrayList<String> getGraphProbs() {
		return graphProbs;
	}

	/**
	 * @param graphProbs the graphProbs to set
	 */
	public void setGraphProbs(ArrayList<String> graphProbs) {
		this.graphProbs = graphProbs;
	}
	
	public static void createTSDGraph(SEDMLDocument sedmlDocument,String dataFileType,String dataPath,
			String taskId,String plotId,String outputFileName,int outputFileType,int width, int height) 
					throws XMLException, DocumentException, IOException 
	{
		GraphData graphData = new GraphData(dataFileType,dataPath,false,"","",new XYSeriesCollection(),"",null);
		graphData.loadSEDML(sedmlDocument,taskId,plotId,true,null);
		double minY = 0;
		double maxY = 0;
		double scaleY = 0;
		double minX = 0;
		double maxX = 0;
		double scaleX = 0;
		try {
			minY = Double.parseDouble(graphData.getYMin());
			maxY = Double.parseDouble(graphData.getYMax());
			scaleY = Double.parseDouble(graphData.getYScale());
			minX = Double.parseDouble(graphData.getXMin());
			maxX = Double.parseDouble(graphData.getXMax());
			scaleX = Double.parseDouble(graphData.getXScale());
		}
		catch (Exception e1) {
		}
		graphData.loadDataFiles(minX,maxX,scaleX,minY,maxY,scaleY);
		File file = new File(outputFileName);
		graphData.export(file, outputFileType, width, height);
	}
		
	public static void createHistogram(SEDMLDocument sedmlDocument,String dataFileType,String dataPath,
			String taskId,String plotId,String outputFileName,int outputFileType,int width, int height) 
					throws XMLException, DocumentException, IOException 
	{
		GraphData graphData = new GraphData(dataFileType,dataPath,false,"","",null);
		graphData.loadSEDML(sedmlDocument,taskId,plotId,false,null);
		graphData.loadProbDataFiles(dataPath);
		File file = new File(outputFileName);
		graphData.export(file, outputFileType, width, height);
	}

	public static void main(String args[]) throws XMLException, DocumentException, IOException {
		String sedmlFilename = "/Users/myers/Documents/Projects/TestArchive/TestArchive.sedml";
		File sedmlFile = new File(sedmlFilename);
		SEDMLDocument sedmlDocument = Libsedml.readDocument(sedmlFile);
		createTSDGraph(sedmlDocument,GraphData.TSD_DATA_TYPE,
				"/Users/myers/Documents/Projects/TestArchive/",
				null,"topModel__graph","/Users/myers/firstTSD.pdf",GraphData.PDF_FILE_TYPE,650,400);
		createHistogram(sedmlDocument,GraphData.TSD_DATA_TYPE,
				"/Users/myers/Documents/Projects/TestArchive/",
				null,"topModel__report","/Users/myers/firstHist.pdf",GraphData.PDF_FILE_TYPE,650,400);
	}
}

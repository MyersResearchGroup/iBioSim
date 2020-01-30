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
package edu.utah.ece.async.ibiosim.gui.util;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.mail.PasswordAuthentication;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.gui.Gui;

/**
 * This class contains static methods that perform tasks based on which buttons
 * are pressed.
 * 
 * @author Curtis Madsen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Utility {

	public static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
		
		String message;
		
		//Implements Thread.UncaughtExceptionHandler.uncaughtException()
		@Override
		public void uncaughtException(Thread th, Throwable ex) {
			final JFrame exp = new JFrame("Unhandled Exception");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			ex.printStackTrace();
			message = sw.toString(); // stack trace as a string
			JLabel error = new JLabel("Program has thrown an exception of the type:");
			JLabel errMsg = new JLabel(ex.toString());
			JButton details = new JButton("Details");
			details.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object[] options = { "Close" };
					JTextArea textArea = new JTextArea(message);
					JScrollPane scrollPane = new JScrollPane(textArea);  
					textArea.setLineWrap(false);  
					textArea.setWrapStyleWord(false); 
					scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
					JOptionPane.showOptionDialog(exp, scrollPane, "Details", JOptionPane.YES_OPTION, 
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				}
			});
			JButton report = new JButton("Send Bug Report");
			report.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Utility.submitBugReport("\n\nStack trace:\n"+message);
				}
			});
			JButton close = new JButton("Close");
			close.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exp.dispose();
				}
			});
			JPanel errMessage = new JPanel();
			errMessage.add(error);
			JPanel errMsgPanel = new JPanel();
			errMsgPanel.add(errMsg);
			JPanel buttons = new JPanel();
			buttons.add(details);
			buttons.add(report);
			buttons.add(close);
			JPanel expPanel = new JPanel(new BorderLayout());
			expPanel.add(errMessage,"North");
			expPanel.add(errMsgPanel,"Center");
			expPanel.add(buttons,"South");
			exp.setContentPane(expPanel);
			exp.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			}
			catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = exp.getSize();
	
			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			exp.setLocation(x, y);
			exp.setVisible(true);
		}
	}

	public static class MyAuthenticator extends javax.mail.Authenticator {
		String User;
		String Password;
		public MyAuthenticator (String user, String password) {
			User = user;
			Password = password;
		}
	
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new javax.mail.PasswordAuthentication(User, Password);
		}
	}
	
	/**
	 * Returns the pathname of the selected file in the file chooser.
	 */
	public static String browse(JFrame frame, File file, JTextField text, int i, String approve, int fileType) {
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.file_browser", "").equals("FileDialog")) 
		{
			FileDialog fd;
			if (i == JFileChooser.DIRECTORIES_ONLY)  
			{
				if (approve.equals("Save") || approve.equals("New")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE); 
				}
				else if (approve.equals("Open")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
				}
				else {
					fd = new FileDialog(frame, approve);
				}
				fd.setFilenameFilter(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return false;
					}
				});
			}
			else {
				if (approve.equals("Save") || approve.equals("New")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
				}
				else if (approve.equals("Open")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
				}
				else if (approve.equals("Export TSD")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".csv") || name.endsWith(".dat") || name.endsWith(".eps") || name.endsWith(".jpg")
									|| name.endsWith(".pdf") || name.endsWith(".png") || name.endsWith(".svg") || name.endsWith(".tsd");
						}
					});
				}
				else if (approve.equals("Export Probability")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".eps") || name.endsWith(".jpg") || name.endsWith(".pdf") || name.endsWith(".png")
									|| name.endsWith(".svg");
						}
					});
				}
				else if (approve.equals("Import SBOL")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".xml") || name.endsWith(GlobalConstants.SBOL_FILE_EXTENSION)
									|| name.endsWith(GlobalConstants.RDF_FILE_EXTENSION);
						}
					});
				}
				else if (approve.equals("Import GenBank")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".gb");
						}
					});
				}
				else if (approve.equals("Import Fasta")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".fasta");
						}
					});
				}
				else if (approve.equals("Export SBOL")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".xml") || name.endsWith(GlobalConstants.SBOL_FILE_EXTENSION)
									|| name.endsWith(GlobalConstants.RDF_FILE_EXTENSION);
									
						}
					});
				}
				else if (approve.equals("Export GenBank")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".gb");
									
						}
					});
				}
				else if (approve.equals("Export Fasta")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".fasta");
									
						}
					});
				}
				else if (approve.equals("Import SED-ML")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".xml") || name.endsWith(".sedml") 
									|| name.endsWith(".sedx") || name.endsWith(".omex");
						}
					});
				}
				else if (approve.equals("Export SED-ML")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".xml") || name.endsWith(".sedml") || name.endsWith(".sedx");
						}
					});
				}
				else if (approve.equals("Import Archive")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".omex");
						}
					});
				}
				else if (approve.equals("Export Archive")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".omex");
						}
					});
				}
				else if (approve.equals("Import SBML")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".sbml") || name.endsWith(".xml");
						}
					});
				}
				else if (approve.equals("Export SBML")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".sbml") || name.endsWith(".xml");
						}
					});
				}
				else if (approve.equals("Export Schematic")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".jpg");
						}
					});
				}
				else if (approve.equals("Save AVI")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".avi");
						}
					});
				}
				else if (approve.equals("Save MP4")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".mp4");
						}
					});
				}
				else if (approve.equals("Import Genetic Circuit")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".gcm");
						}
					});
				}
				else if (approve.equals("Import")) {
					fd = new FileDialog(frame, approve, FileDialog.LOAD);
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".csv") || name.endsWith(".dat") || name.endsWith(".tsd");
						}
					});
				}
				else if (approve.equals("saveAsSBOL2")) {
					fd = new FileDialog(frame, approve, FileDialog.SAVE); 
					fd.setFilenameFilter(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".sbol") || name.endsWith(".rdf");
						}
					});
				}
				else {
					fd = new FileDialog(frame, approve);
				}
			}
			if (file != null) {
				if (file.isDirectory()) {
					fd.setDirectory(file.getPath());
				}
				else {
					fd.setDirectory(GlobalConstants.getPath(file.getPath()) + File.separator);
					fd.setFile(file.getName());
				}
			}
			if (i == JFileChooser.DIRECTORIES_ONLY) {
				System.setProperty("apple.awt.fileDialogForDirectories", "true");
			}
			fd.setVisible(true);
			if (i == JFileChooser.DIRECTORIES_ONLY) {
				System.setProperty("apple.awt.fileDialogForDirectories", "false");
			}
			if (fd.getFile() != null) {
				if (fd.getDirectory() != null) {
					String selectedFile = fd.getFile();
					if (approve.equals("Export TSD")) {
						if (!selectedFile.endsWith(".csv") && !selectedFile.endsWith(".dat") && !selectedFile.endsWith(".eps")
								&& !selectedFile.endsWith(".jpg") && !selectedFile.endsWith(".pdf") && !selectedFile.endsWith(".png")
								&& !selectedFile.endsWith(".svg") && !selectedFile.endsWith(".tsd")) {
							selectedFile += ".pdf";
						}
					}
					else if (approve.equals("Export Probability")) {
						if (!selectedFile.endsWith(".eps") && !selectedFile.endsWith(".jpg") && !selectedFile.endsWith(".pdf")
								&& !selectedFile.endsWith(".png") && !selectedFile.endsWith(".svg")) {
							selectedFile += ".pdf";
						}
					}
					else if (approve.equals("Import SBOL") || approve.equals("Export SBOL")) {
						if (!selectedFile.endsWith(".sbol") && !selectedFile.endsWith(".xml"))
							selectedFile += ".sbol";
					}
					else if (approve.equals("Import GenBank") || approve.equals("Export GenBank")) {
						if (!selectedFile.endsWith(".gb"))
							selectedFile += ".gb";
					}
					else if (approve.equals("Import Fasta") || approve.equals("Export Fasta")) {
						if (!selectedFile.endsWith(".fasta"))
							selectedFile += ".fasta";
					}
					else if (approve.equals("Import SED-ML") || approve.equals("Export SED-ML")) {
						if (!selectedFile.endsWith(".xml")&&!selectedFile.endsWith(".sedml")
								&& !selectedFile.endsWith(".sedx") && !selectedFile.endsWith(".omex"))
							selectedFile += ".xml";
					}
					else if (approve.equals("Import Archive") || approve.equals("Export Archive")) {
						if (!selectedFile.endsWith(".omex"))
							selectedFile += ".omex";
					}
					else if (approve.equals("Import SBML") || (approve.equals("Export SBML"))) {
						if (!selectedFile.endsWith(".sbml") && !selectedFile.endsWith(".xml")) {
							selectedFile += ".xml";
						}
					}
					else if (approve.equals("Export Schematic")) {
						if (!selectedFile.endsWith(".jpg")) {
							selectedFile += ".jpg";
						}
					}
					else if (approve.equals("Import Genetic Circuit")) {
						if (!selectedFile.endsWith(".gcm")) {
							selectedFile += ".gcm";
						}
					}
					else if (approve.equals("Import")) {
						if (!selectedFile.endsWith(".csv") && !selectedFile.endsWith(".dat") && !selectedFile.endsWith(".tsd")) {
							selectedFile += ".tsd";
						}
					}
					return fd.getDirectory() + File.separator + selectedFile;
				}
				return "";
			}
			else if (fd.getDirectory() != null) {
				return ""; // fd.getDirectory();
			}
			else {
				return "";
			}
		}
		
		String filename = "";
		JFileChooser fc = new JFileChooser();

		ExampleFileFilter csvFilter = new ExampleFileFilter();
		csvFilter.addExtension("csv");
		csvFilter.setDescription("Comma Separated Values");
		ExampleFileFilter datFilter = new ExampleFileFilter();
		datFilter.addExtension("dat");
		datFilter.setDescription("Tab Delimited Data");
		ExampleFileFilter tsdFilter = new ExampleFileFilter();
		tsdFilter.addExtension("tsd");
		tsdFilter.setDescription("Time Series Data");
		ExampleFileFilter epsFilter = new ExampleFileFilter();
		epsFilter.addExtension("eps");
		epsFilter.setDescription("Encapsulated Postscript");
		ExampleFileFilter jpgFilter = new ExampleFileFilter();
		jpgFilter.addExtension("jpg");
		jpgFilter.setDescription("JPEG");
		ExampleFileFilter pdfFilter = new ExampleFileFilter();
		pdfFilter.addExtension("pdf");
		pdfFilter.setDescription("Portable Document Format");
		ExampleFileFilter pngFilter = new ExampleFileFilter();
		pngFilter.addExtension("png");
		pngFilter.setDescription("Portable Network Graphics");
		ExampleFileFilter svgFilter = new ExampleFileFilter();
		svgFilter.addExtension("svg");
		svgFilter.setDescription("Scalable Vector Graphics");
		ExampleFileFilter sbolFilter = new ExampleFileFilter();
		sbolFilter.addExtension("sbol");
		sbolFilter.addExtension("xml");
		sbolFilter.addExtension("rdf");
		sbolFilter.setDescription("Synthetic Biology Open Language");
		ExampleFileFilter sedmlFilter = new ExampleFileFilter();
		sedmlFilter.addExtension("xml");
		sedmlFilter.addExtension("sedml");
		sedmlFilter.addExtension("sedx");
		sedmlFilter.setDescription("Simulation Experiment Description Markup Language");
		ExampleFileFilter archiveFilter = new ExampleFileFilter();
		archiveFilter.addExtension("omex");
		archiveFilter.setDescription("COMBINE Archive");
		ExampleFileFilter sbmlFilter = new ExampleFileFilter();
		sbmlFilter.addExtension("sbml");
		sbmlFilter.setDescription("Systems Biology Markup Language");
		ExampleFileFilter mp4Filter = new ExampleFileFilter();
		mp4Filter.addExtension("mp4");
		mp4Filter.setDescription("Audio Visual Files");
		ExampleFileFilter aviFilter = new ExampleFileFilter();
		aviFilter.addExtension("avi");
		aviFilter.setDescription("Audio Visual Files");
		ExampleFileFilter xmlFilter = new ExampleFileFilter();
		xmlFilter.addExtension("xml");
		xmlFilter.setDescription("Extensible Markup Language");
		ExampleFileFilter gbFilter = new ExampleFileFilter();
		gbFilter.addExtension("gb");
		gbFilter.setDescription("GenBank");
		ExampleFileFilter fastaFilter = new ExampleFileFilter();
		fastaFilter.addExtension("fasta");
		fastaFilter.setDescription("Fasta");
		ExampleFileFilter gcmFilter = new ExampleFileFilter();
		gcmFilter.addExtension("gcm");
		gcmFilter.setDescription("Genetic Circuit Model");
		if (file != null) {
			fc.setSelectedFile(file);
		}
		fc.setFileSelectionMode(i);
		int retValue;
		if (approve.equals("Save")) {
			retValue = fc.showSaveDialog(frame);
		}
		else if (approve.equals("Open")) {
			retValue = fc.showOpenDialog(frame);
		}
		else if (approve.equals("Export TSD")) {
			fc.addChoosableFileFilter(csvFilter);
			fc.addChoosableFileFilter(datFilter);
			fc.addChoosableFileFilter(epsFilter);
			fc.addChoosableFileFilter(jpgFilter);
			fc.addChoosableFileFilter(pdfFilter);
			fc.addChoosableFileFilter(pngFilter);
			fc.addChoosableFileFilter(svgFilter);
			fc.addChoosableFileFilter(tsdFilter);
			fc.setAcceptAllFileFilterUsed(false);
			if (fileType == 5) {
				fc.setFileFilter(csvFilter);
			}
			if (fileType == 6) {
				fc.setFileFilter(datFilter);
			}
			if (fileType == 3) {
				fc.setFileFilter(epsFilter);
			}
			if (fileType == 0) {
				fc.setFileFilter(jpgFilter);
			}
			if (fileType == 2) {
				fc.setFileFilter(pdfFilter);
			}
			if (fileType == 1) {
				fc.setFileFilter(pngFilter);
			}
			if (fileType == 4) {
				fc.setFileFilter(svgFilter);
			}
			if (fileType == 7) {
				fc.setFileFilter(tsdFilter);
			}
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Export Probability")) {
			fc.addChoosableFileFilter(epsFilter);
			fc.addChoosableFileFilter(jpgFilter);
			fc.addChoosableFileFilter(pdfFilter);
			fc.addChoosableFileFilter(pngFilter);
			fc.addChoosableFileFilter(svgFilter);
			fc.setAcceptAllFileFilterUsed(false);
			if (fileType == 3) {
				fc.setFileFilter(epsFilter);
			}
			if (fileType == 0) {
				fc.setFileFilter(jpgFilter);
			}
			if (fileType == 2) {
				fc.setFileFilter(pdfFilter);
			}
			if (fileType == 1) {
				fc.setFileFilter(pngFilter);
			}
			if (fileType == 4) {
				fc.setFileFilter(svgFilter);
			}
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Import SBOL") || approve.equals("Export SBOL")) {
			fc.addChoosableFileFilter(sbolFilter);
			fc.addChoosableFileFilter(xmlFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(sbolFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Import GenBank") || approve.equals("Export GenBank")) {
			fc.addChoosableFileFilter(gbFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(sbolFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Import Fasta") || approve.equals("Export Fasta")) {
			fc.addChoosableFileFilter(fastaFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(sbolFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Import SED-ML") || approve.equals("Export SED-ML")) {
			fc.addChoosableFileFilter(sedmlFilter);
			fc.addChoosableFileFilter(xmlFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(xmlFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Import Archive") || approve.equals("Export Archive")) {
			fc.addChoosableFileFilter(archiveFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(archiveFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Import SBML")) {
			fc.addChoosableFileFilter(sbmlFilter);
			fc.addChoosableFileFilter(xmlFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(xmlFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Export SBML")) {
			fc.addChoosableFileFilter(sbmlFilter);
			fc.addChoosableFileFilter(xmlFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(xmlFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Export Schematic")) {
			fc.addChoosableFileFilter(jpgFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(jpgFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Save MP4")) {
			fc.addChoosableFileFilter(mp4Filter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(mp4Filter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Save AVI")) {
			fc.addChoosableFileFilter(aviFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(aviFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else if (approve.equals("Import Genetic Circuit")) {
			fc.addChoosableFileFilter(gcmFilter);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(gcmFilter);
			retValue = fc.showDialog(frame, approve);
		}
		else {
			retValue = fc.showDialog(frame, approve);
		}
		if (retValue == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			if (text != null) {
				text.setText(file.getPath());
			}
			filename = file.getPath();
			if (approve.equals("Export TSD")) {
				if ((filename.length() < 4)
						|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".jpg"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".png"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".pdf"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".eps"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".svg"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".dat"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".tsd")) && !(filename.substring(
								(filename.length() - 4), filename.length()).equals(".csv")))) {
					ExampleFileFilter selectedFilter = (ExampleFileFilter) fc.getFileFilter();
					if (selectedFilter == jpgFilter) {
						filename += ".jpg";
					}
					else if (selectedFilter == pngFilter) {
						filename += ".png";
					}
					else if (selectedFilter == pdfFilter) {
						filename += ".pdf";
					}
					else if (selectedFilter == epsFilter) {
						filename += ".eps";
					}
					else if (selectedFilter == svgFilter) {
						filename += ".svg";
					}
					else if (selectedFilter == datFilter) {
						filename += ".dat";
					}
					else if (selectedFilter == tsdFilter) {
						filename += ".tsd";
					}
					else if (selectedFilter == csvFilter) {
						filename += ".csv";
					}
				}
			}
			else if (approve.equals("Export Probability")) {
				if ((filename.length() < 4)
						|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".jpg"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".png"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".pdf"))
								&& !(filename.substring((filename.length() - 4), filename.length()).equals(".eps")) && !(filename.substring(
								(filename.length() - 4), filename.length()).equals(".svg")))) {
					ExampleFileFilter selectedFilter = (ExampleFileFilter) fc.getFileFilter();
					if (selectedFilter == jpgFilter) {
						filename += ".jpg";
					}
					else if (selectedFilter == pngFilter) {
						filename += ".png";
					}
					else if (selectedFilter == pdfFilter) {
						filename += ".pdf";
					}
					else if (selectedFilter == epsFilter) {
						filename += ".eps";
					}
					else if (selectedFilter == svgFilter) {
						filename += ".svg";
					}
				}
			}
			else if (approve.equals("Export SBML")) {
				if ((filename.length() < 4)
						|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".xml")))) {
					filename += ".xml";
				}
			}
			else if (approve.equals("Export Schematic")) {
				if ((filename.length() < 4)
						|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".jpg")))) {
					filename += ".jpg";
				}
			}
			else if (approve.equals("Save MP4")) {
				if ((filename.length() < 4)
						|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".mp4")))) {
					filename += ".mp4";
				}
			}
			else if (approve.equals("Save AVI")) {
				if ((filename.length() < 4)
						|| (!(filename.substring((filename.length() - 4), filename.length()).equals(".avi")))) {
					filename += ".avi";
				}
			}
			else if (approve.equals("Import SBOL") || approve.equals("Export SBOL")) {
				if (!filename.endsWith(".sbol") && !filename.endsWith(".xml") && !filename.endsWith(".rdf"))
					filename += ".sbol";
			}
			else if (approve.equals("Import SBOL") || approve.equals("Export SBOL")) {
				if (!filename.endsWith(".gb"))
					filename += ".gb";
			}
			else if (approve.equals("Import SBOL") || approve.equals("Export SBOL")) {
				if (!filename.endsWith(".fasta"))
					filename += ".fasta";
			}
			else if (approve.equals("Import SED-ML") || approve.equals("Export SED-ML")) {
				if (!filename.endsWith(".xml")&&!filename.endsWith(".sedml")
						&&!filename.endsWith(".sedx")&&!filename.endsWith(".omex"))
					filename += ".xml";
			}
			else if (approve.equals("Import Archive") || approve.equals("Export Archive")) {
				if (!filename.endsWith(".omex"))
					filename += ".omex";
			}
		}
		return filename;
	}

	/**
	 * Removes the selected values of the given JList from the given list and
	 * updates the JList.
	 */
	public static Object[] remove(JList currentList, Object[] list) {
		Object[] removeSelected = currentList.getSelectedValues();
		int[] select = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			select[i] = i;
		}
		currentList.setSelectedIndices(select);
		Object[] getAll = currentList.getSelectedValues();
		currentList.removeSelectionInterval(0, list.length - 1);
		ArrayList<Object> remove = new ArrayList<Object>();
		for (int i = 0; i < getAll.length; i++) {
			remove.add(getAll[i]);
		}
		for (int i = 0; i < removeSelected.length; i++) {
			remove.remove(removeSelected[i]);
		}
		String[] keep = new String[remove.size()];
		for (int i = 0; i < remove.size(); i++) {
			keep[i] = (String) remove.get(i);
		}
		currentList.setListData(keep);
		list = keep;
		return list;
	}

	/**
	 * Removes the selected values of the given JList from the given list and
	 * updates the JList.
	 */
	public static void remove(JList currentList) {
		Object[] list = new Object[currentList.getModel().getSize()];
		for (int i = 0; i < currentList.getModel().getSize(); i++) {
			list[i] = currentList.getModel().getElementAt(i);
		}

		Object[] removeSelected = currentList.getSelectedValues();
		int[] select = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			select[i] = i;
		}
		currentList.setSelectedIndices(select);
		Object[] getAll = currentList.getSelectedValues();
		currentList.removeSelectionInterval(0, list.length - 1);
		ArrayList<Object> remove = new ArrayList<Object>();
		for (int i = 0; i < getAll.length; i++) {
			remove.add(getAll[i]);
		}
		for (int i = 0; i < removeSelected.length; i++) {
			remove.remove(removeSelected[i]);
		}
		String[] keep = new String[remove.size()];
		for (int i = 0; i < remove.size(); i++) {
			keep[i] = (String) remove.get(i);
		}
		currentList.setListData(keep);
		list = keep;
	}

	/**
	 * Adds a new item to a JList
	 */
	public static void add(JList currentList, Object newItem) {
		Object[] list = new Object[currentList.getModel().getSize() + 1];
		int addAfter = currentList.getSelectedIndex();
		for (int i = 0; i <= currentList.getModel().getSize(); i++) {
			if (i <= addAfter) {
				list[i] = currentList.getModel().getElementAt(i);
			}
			else if (i == (addAfter + 1)) {
				list[i] = newItem;
			}
			else {
				list[i] = currentList.getModel().getElementAt(i - 1);
			}
		}
		currentList.setListData(list);
	}

	/**
	 * Adds the selected values in the add JList to the list JList. Stores all
	 * these values into the currentList array and returns this array.
	 */
	public static Object[] add(Object[] currentList, JList list, JList add) {
		int[] select = new int[currentList.length];
		for (int i = 0; i < currentList.length; i++) {
			select[i] = i;
		}
		list.setSelectedIndices(select);
		currentList = list.getSelectedValues();
		Object[] newSelected = add.getSelectedValues();
		Object[] temp = currentList;
		int newLength = temp.length;
		for (int i = 0; i < newSelected.length; i++) {
			int j = 0;
			for (j = 0; j < temp.length; j++) {
				if (temp[j].equals(newSelected[i])) {
					break;
				}
			}
			if (j == temp.length)
				newLength++;
		}
		currentList = new Object[newLength];
		for (int i = 0; i < temp.length; i++) {
			currentList[i] = temp[i];
		}
		int num = temp.length;
		for (int i = 0; i < newSelected.length; i++) {
			int j = 0;
			for (j = 0; j < temp.length; j++)
				if (temp[j].equals(newSelected[i]))
					break;
			if (j == temp.length) {
				currentList[num] = newSelected[i];
				num++;
			}
		}
		edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility.sort(currentList);
		list.setListData(currentList);
		return currentList;
	}

	/**
	 * Returns a list of all the objects in the given JList.
	 */
	public static String[] getList(Object[] size, JList objects) {
		String[] list;
		if (size.length == 0) {
			list = new String[0];
		}
		else {
			int[] select = new int[size.length];
			for (int i = 0; i < size.length; i++) {
				select[i] = i;
			}
			objects.setSelectedIndices(select);
			size = objects.getSelectedValues();
			list = new String[size.length];
			for (int i = 0; i < size.length; i++) {
				list[i] = (String) size[i];
			}
		}
		return list;
	}

	public static ArrayList<String> sort(ArrayList<String> components){
		int i, j;
		String index;
		for (i = 1; i < components.size(); i++) {
			index = components.get(i);
			j = i;
			while ((j > 0) && components.get(j - 1).compareToIgnoreCase(index) > 0) {
				components.set(j, components.get(j - 1));
				j = j - 1;
			}
			components.set(j, index);
		}
		return components;
	}
	
	public static void submitBugReportTemp(String message) {
		Preferences biosimrc = Preferences.userRoot();
		String command = biosimrc.get("biosim.general.browser", "");
		command = command + " http://www.github.com/MyersResearchGroup/iBioSim/issues";
		Runtime exec = Runtime.getRuntime();
		try
		{
			exec.exec(command);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Unable to open bug database.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void submitBugReport(String message) {
		JPanel reportBugPanel = new JPanel(new GridLayout(4,1));
		JLabel typeLabel = new JLabel("Type of Report:");
		JComboBox reportType = new JComboBox(Utility.bugReportTypes);
		
		if (!message.equals("")) {
			typeLabel.setEnabled(false);
			reportType.setEnabled(false);
		}
		
		JPanel typePanel = new JPanel(new GridLayout(1,2));
		typePanel.add(typeLabel);
		typePanel.add(reportType);
		JLabel emailLabel = new JLabel("Email address:");
		JTextField emailAddr = new JTextField(30);
		JPanel emailPanel = new JPanel(new GridLayout(1,2));
		emailPanel.add(emailLabel);
		emailPanel.add(emailAddr);
		JLabel bugSubjectLabel = new JLabel("Brief Description:");
		JTextField bugSubject = new JTextField(30);
		JPanel bugSubjectPanel = new JPanel(new GridLayout(1,2));
		bugSubjectPanel.add(bugSubjectLabel);
		bugSubjectPanel.add(bugSubject);
		JLabel bugDetailLabel = new JLabel("Detailed Description:");
		JTextArea bugDetail = new JTextArea(5,30);
		bugDetail.setLineWrap(true);
		bugDetail.setWrapStyleWord(true);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(100, 100));
		scroll.setPreferredSize(new Dimension(100, 100));
		scroll.setViewportView(bugDetail);
		JPanel bugDetailPanel = new JPanel(new GridLayout(1,2));
		bugDetailPanel.add(bugDetailLabel);
		bugDetailPanel.add(scroll);
		reportBugPanel.add(typePanel);
		reportBugPanel.add(emailPanel);
		reportBugPanel.add(bugSubjectPanel);
		reportBugPanel.add(bugDetailPanel);
		Object[] options = { "Send", "Cancel" };
		
		int value = JOptionPane.showOptionDialog(Gui.frame, reportBugPanel, "Bug Report",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		
		if (value == 0) {
			try {
                // Split the token in two to fool GitHub
				GitHub github = GitHub.connectUsingPassword("buggsley", "ibiosim3280");
				//GitHub github = new GitHubBuilder().withOAuthToken("79da2a9510ebdfa5fa2a" + "80416e1598b2ad05190f").build(); 
				GHRepository iBioSimRepository = github.getRepository("MyersResearchGroup/iBioSim");
				GHIssueBuilder issue = iBioSimRepository.createIssue(bugSubject.getText().trim());
				issue.body(System.getProperty("software.running") + "\n\nOperating system: " + 
						System.getProperty("os.name") + "\n\nBug reported by: " + emailAddr.getText().trim() + 
						"\n\nDescription:\n"+bugDetail.getText().trim()+message);
				issue.label(reportType.getSelectedItem().toString());
				issue.create();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(Gui.frame, "Bug report failed, please submit manually.",
						"Bug Report Failed", JOptionPane.ERROR_MESSAGE);
				Preferences biosimrc = Preferences.userRoot();
				String command = biosimrc.get("biosim.general.browser", "");
				command = command + " http://www.github.com/MyersResearchGroup/iBioSim/issues";
				Runtime exec = Runtime.getRuntime();
				
				try
				{
					exec.exec(command);
				}
				catch (IOException e1)
				{
					JOptionPane.showMessageDialog(Gui.frame, "Unable to open bug database.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
//		JOptionPane.showMessageDialog(Gui.frame, "Please verify that your bug report is in the incoming folder.\n" +
//				"If not, please submit your bug report manually using the web interface.\n", 
//				"Bug Report", JOptionPane.ERROR_MESSAGE);
//		submitBugReportTemp(message);
	}

	public static final String[] bugReportTypes = new String[] { "BUG", "CHANGE", "FEATURE" };
}

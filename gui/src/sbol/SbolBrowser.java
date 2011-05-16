package sbol;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.libSBOLj.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import main.Gui;

public class SbolBrowser extends JPanel implements MouseListener {
	
	private JList libraryList;
	private JList componentList;
	private JList featureList;
	private TextArea libraryText;
	private TextArea componentText;
	private TextArea featureText;
	private HashMap<String, Library> libraryMap;
	private HashMap<String, DnaComponent> componentMap;
	private HashMap<String, SequenceFeature> featureMap;
	
	private String[] options = {"Ok"};
	
	public SbolBrowser(String filePath) {
		super(new BorderLayout());
		libraryList = new JList();
		libraryList.addMouseListener(this);
		componentList = new JList();
		componentList.addMouseListener(this);
		featureList = new JList();
		featureList.addMouseListener(this);
		
		JLabel libraryLabel = new JLabel("Libraries:");
		JLabel componentLabel = new JLabel("DNA Components:");
		JLabel featureLabel = new JLabel("Sequence Features:");
		
		JScrollPane libraryScroll = new JScrollPane();
		libraryScroll.setMinimumSize(new Dimension(260, 200));
		libraryScroll.setPreferredSize(new Dimension(276, 132));
		libraryScroll.setViewportView(libraryList);
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(componentList);
		
		JScrollPane featureScroll = new JScrollPane();		
		featureScroll.setMinimumSize(new Dimension(260, 200));
		featureScroll.setPreferredSize(new Dimension(276, 132));
		featureScroll.setViewportView(featureList);
		
		JPanel labelPanel = new JPanel(new GridLayout(1, 3));
		labelPanel.add(libraryLabel);
		labelPanel.add(componentLabel);
		labelPanel.add(featureLabel);
		
		JPanel listPanel = new JPanel(new GridLayout(1, 3));
		listPanel.add(libraryScroll);
		listPanel.add(componentScroll);
		listPanel.add(featureScroll);
		
		libraryText = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		libraryText.setEditable(false);
		componentText = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		componentText.setEditable(false);
		featureText = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		featureText.setEditable(false);
		
		JPanel textPanel = new JPanel(new GridLayout(1, 3));
		textPanel.add(libraryText);
		textPanel.add(componentText);
		textPanel.add(featureText);
		
		this.add(labelPanel, "North");
		this.add(listPanel, "Center");
		this.add(textPanel, "South");
		
		libraryMap = new HashMap<String, Library>();
		componentMap = new HashMap<String, DnaComponent>();
		featureMap = new HashMap<String, SequenceFeature>();
		
		loadRDF(filePath);
		
		boolean display = true;
		while (display)
			display = browserOpen();
	}
	
	private boolean browserOpen() {
		JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Browser", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		return false;
	}
	
	private void loadRDF(String filePath) {
		try {
			FileInputStream in = new FileInputStream(filePath);
			Scanner scanIn = new Scanner(in).useDelimiter("\n");
			String rdfString = "";
			ArrayList<String> libIds = new ArrayList<String>();
			boolean libFlag = false;
			while (scanIn.hasNext()) {
				String token = scanIn.next();
				if (libFlag) {
					String temp = token.split("<")[1];
					libIds.add(temp.substring(temp.indexOf(">") + 1, temp.length()));
					libFlag = false;
				} else if (token.equals("\t<rdf:type rdf:resource=\"http://sbols.org/sbol.owl#Library\"/>"))
					libFlag = true;
				rdfString = rdfString.concat(token) + "\n";
			}
			Object[] idObjects = libIds.toArray();
			libraryList.setListData(idObjects);
//			libraryList.setSelectedIndex(0);
			SBOLservice factory = SBOLutil.fromRDF(rdfString);
			for (String libId : libIds) {
				Library lib = factory.getLibrary(libId);
				libraryMap.put(libId, lib);
				for (DnaComponent dnac : lib.getComponents()) {
					componentMap.put(dnac.getDisplayId(), dnac);
					for (SequenceAnnotation sa : dnac.getAnnotations()) {
						for (SequenceFeature sf : sa.getFeatures())
							featureMap.put(sf.getDisplayId(), sf);
					}
				}
				for (SequenceFeature sf : lib.getFeatures()) {
					if (!featureMap.containsKey(sf.getDisplayId()))
						featureMap.put(sf.getDisplayId(), sf);
				}
			}
				
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "File not found.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == libraryList) {
			libraryText.setText("");
			componentText.setText("");
			featureText.setText("");
			Object[] selected = libraryList.getSelectedValues();
			for (Object o : selected) {
				Library lib = libraryMap.get(o.toString());
				libraryText.append("Name:  " + lib.getName() + "\n");
				libraryText.append("Description:  " + lib.getDescription() + "\n\n");
				
				ArrayList<String> compIds = new ArrayList<String>();
				for (DnaComponent dnac : lib.getComponents())
					compIds.add(dnac.getDisplayId());
				Object[] idObjects = compIds.toArray();
				componentList.setListData(idObjects);
				
				ArrayList<String> featIds = new ArrayList<String>();
				for (SequenceFeature sf : lib.getFeatures()) 
					featIds.add(sf.getDisplayId());
				idObjects = featIds.toArray();
				featureList.setListData(idObjects);
			}
		} else if (e.getSource() == componentList) {
			componentText.setText("");
			featureText.setText("");
			Object[] selected = componentList.getSelectedValues();
			for (Object o : selected) {
				DnaComponent dnac = componentMap.get(o.toString());
				componentText.append("Name:  " + dnac.getName() + "\n");
				componentText.append("Description:  " + dnac.getDescription() + "\n");
				componentText.append("Annotations:  ");
				//Creation of to-be-sorted annotation array
				SequenceAnnotation[] sortedSA = new SequenceAnnotation[dnac.getAnnotations().size()];
				int n = 0;
				for (SequenceAnnotation sa : dnac.getAnnotations()) {
					sortedSA[n] = sa;
					n++;
				}
				//Insert sort of annotations by starting position
				for (int j = 1; j < sortedSA.length; j++) {
					SequenceAnnotation keyAnnotation = sortedSA[j];
					int key = keyAnnotation.getStart();
					int i = j - 1;
					while (i >= 0 && sortedSA[i].getStart() > key) {
						sortedSA[i + 1] = sortedSA[i];
						i = i - 1;
					}
					sortedSA[i + 1] = keyAnnotation;
				}
				//Processing sorted annotations for display
				String annotations = "";
				ArrayList<String> featIds = new ArrayList<String>();
				for (int k = 0; k < sortedSA.length; k++) {
					for (SequenceFeature sf : sortedSA[k].getFeatures()) {
						annotations = annotations + sf.getName() + " + ";
						featIds.add(sf.getDisplayId());
					}
					annotations = annotations.substring(0, annotations.length() - 2);
					String sign = sortedSA[k].getStrand();
					if (sign.equals("+"))
						sign = "";
					annotations = annotations + sign + sortedSA[k].getStart() + " to " + sign + sortedSA[k].getStop() + ", "; 
				}
				componentText.append(annotations.substring(0, annotations.length() - 2) + "\n");
				componentText.append("DNA Sequence:  " + dnac.getDnaSequence().getDnaSequence() + "\n\n");
				Object[] idObjects = featIds.toArray();
				featureList.setListData(idObjects);
			}
		} else if (e.getSource() == featureList) {
			featureText.setText("");
			Object[] selected = featureList.getSelectedValues();
			for (Object o : selected) {
				SequenceFeature sf = featureMap.get(o.toString());
				featureText.append("Name:  " + sf.getName() + "\n");
				featureText.append("Description:  " + sf.getDescription() + "\n");
				featureText.append("Types:  ");
				String types = "";
				for (URI uri : sf.getTypes()) {
					if (!uri.getFragment().equals("SequenceFeature"))
						types = types + uri.getFragment() + ", ";
				}
				featureText.append(types.substring(0, types.length() - 2) + "\n");
				featureText.append("DNA Sequence:  " + sf.getDnaSequence().getDnaSequence() + "\n\n");
			}
		}

	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

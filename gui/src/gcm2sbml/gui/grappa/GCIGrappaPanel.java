/**
 * 
 */
package gcm2sbml.gui.grappa;

//import att.grappa.*;
import att.grappa.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.border.*;

class CouldNotParseException extends Exception{ public CouldNotParseException(String msg){super(msg);} }

/**
 * @author tyler
 *
 */
public class GCIGrappaPanel extends JPanel {

	Graph graph;
	GrappaPanel grappaPanel;
	private boolean isLaidOut;
	
	
    public final static String INSTANCES = "instances";

    
    
	/**
	 * Generates a panel complete with controls 
	 */
	public GCIGrappaPanel() {
		super(new BorderLayout());
	}
	
	
	/**
	 * Called when this grappa panel is displayed. Needs to refresh all 
	 * the layout stuff.
	 */
	// TODO: pass in buffer.
	public void display(StringBuffer gcmStringBuffer){
		isLaidOut = false;
		graph = null;
		grappaPanel = null;
		
		StringReader sr = new StringReader(gcmStringBuffer.toString());
		
		try{
			graph = this.initGraph(sr);
		}catch(CouldNotParseException ex){
			JTextArea jt = new JTextArea(ex.getMessage());
			jt.setBorder(new EmptyBorder(5,5,5,5));
			this.add(jt);
			return;
		}
		
		
		buildPanel(graph);
		
		layoutGraph(graph);
	}

	
	private void buildPanel(Graph graph){
		
		this.removeAll();
		
		// Add a button to re-layout
//
//		JButton relayout = new JButton("Re-Layout");
//		relayout.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				// TODO Auto-generated method stub
//				layoutGraph();
//			}
//		});
//		this.add(relayout);
		

		// build the GrappaPanel
		grappaPanel = new GrappaPanel(graph);
		
		JScrollPane scrollPane = new JScrollPane(grappaPanel);


		this.add(scrollPane);
	}

	
	
	/**
	 * 0
	 * 
	 * +
	 * 952**
	 * Generates a Graph object
	 * @param input: an InputSteam containing the .dot file
	 * @return: A Graph object all initialized, or null if something went wrong.
	 */
	private Graph initGraph(StringReader input) throws CouldNotParseException{
		
		// Parse the .dot file
		Parser program = new Parser(input);//, System.err);
		try {
			program.parse();
		} catch (Exception ex) {
			throw new CouldNotParseException( "Parsing Exception: " + ex.getMessage() );
		}
		
		Graph g = null;
		g = program.getGraph();
		
		g.setEditable(true);
		
		g.setMenuable(true);
		
		g.setSelectable(true);
			
		return(g);
	}
	


	/**
	 * Called when we need to re-position the graph.
	 */
	private void layoutGraph(Graph graph){ 
		Object connector = null;
		// TODO: Attempt to run the connector locally first.
		
		try {
			connector = Runtime.getRuntime().exec("dot");
		} catch (Exception ex) {
			System.err
			.println("Exception while setting up Process: "
					+ ex.getMessage()
					+ "\nTrying URLConnection...");
			connector = null;
		}
		if (connector == null) {
			try {
				connector = (new URL(
				"http://www.research.att.com/~john/cgi-bin/format-graph"))
				.openConnection();
				URLConnection urlConn = (URLConnection) connector;
				urlConn.setDoInput(true);
				urlConn.setDoOutput(true);
				urlConn.setUseCaches(false);
				urlConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
			} catch (Exception ex) {
				System.err
				.println("Exception while setting up URLConnection: "
						+ ex.getMessage()
						+ "\nLayout not performed.");
				connector = null;
			}
		}
		if (connector != null) {
			if (!GrappaSupport.filterGraph(graph, connector)) {
				System.err
				.println("ERROR: somewhere in filterGraph");
			}
			if (connector instanceof Process) {
				try {
					int code = ((Process) connector).waitFor();
					if (code != 0) {
						System.err
						.println("WARNING: proc exit code is: "
								+ code);
					}
				} catch (InterruptedException ex) {
					System.err
					.println("Exception while closing down proc: "
							+ ex.getMessage());
					ex.printStackTrace(System.err);
				}
			}
			connector = null;
		}
		graph.repaint();
	}

	
}

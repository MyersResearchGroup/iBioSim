package lpn.parser;
import lpn.gui.LHPNEditor;
import main.Gui;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

//public class Property implements MouseListener, ActionListener, MouseMotionListener, MouseWheelListener {
public class Property{

	public static JFrame frame;
	private String root;
	private String separator;

	public void property(String root2, String separator2) {
		root=root2;
		separator = separator2;
		try {
			String propertyId = JOptionPane.showInputDialog(frame, "Enter the SVA property name:", "Model ID", JOptionPane.PLAIN_MESSAGE);
			//System.out.println(propertyId);
			if (propertyId!=null){
				String property = JOptionPane.showInputDialog(frame, "Enter the SVA property:", "Model", JOptionPane.PLAIN_MESSAGE);
				LhpnFile lpn = new LhpnFile();
				//File lpnFile = new File(propertyId+".lpn");
				File lpnFile = new File(propertyId+".lpn");
				lpnFile.createNewFile();

				int numPlaces = 0;
				int numTransitions = 0;
				int numFailTransitions =0;
				int numFailPlaces = 0;
				int numIntersectTransitions = 0;
				int numDecisionTransitions =0;
				lpn.save(root + separator +lpnFile);
				//System.out.println(property);
				String[] prop = property.split(" \\(");
				//for (int i=0;i<prop.length;i++){
					//System.out.println("\n"+prop[i]);
				//}

				String enabling=null;
				String delay=null;
				String clock=null;
				lpn.addPlace("p"+numPlaces, true);
				numPlaces++;
				for (int i=0; i<prop.length; i++){
					//if (operand==0){
					String propNew = prop[i].replaceAll("\\(", "");
					propNew = propNew.replaceAll("\\)","");
					//System.out.println("\n PropNew : "+propNew);
					{
						//if(Pattern.matches("\\w+", prop[i])){

						if(Pattern.matches("@posedge"+" "+"\\w+", propNew)){
							//System.out.println("\n This is operand");
							String[] currProp = propNew.split(" ");
							clock = currProp[1];
						}
						else {
							String[] currProp = propNew.split(" ");
							for(int j=0; j<currProp.length; j++){
								if(Pattern.matches("1", currProp[j])){
									if(numPlaces==0){
										lpn.addPlace("p"+numPlaces, true);
									}
									else{
										lpn.addPlace("p"+numPlaces, false);
									}
									numPlaces++;
									lpn.addTransition("t" + numTransitions);
									numTransitions++;
									lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
									//lpn.addInput(clock, "false");
									lpn.addEnabling("t" +(numTransitions-1), clock);
									lpn.addPlace("p"+numPlaces, false);
									numPlaces++;
									lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1)); 
								}
								else if(Pattern.matches("\\w+", currProp[j])){

									if((currProp[j].matches("intersect")) || (currProp[j].matches("or")) ){
										

									}
									else{

										//lpn.addInput(clock, "false");
										if(((j+1)<currProp.length)){
											if(currProp[j+1].matches("intersect") || currProp[j+1].matches("or")){
												lpn.addTransition("tInter" + numIntersectTransitions);
												numIntersectTransitions++;
												lpn.addTransition("tInter" + numIntersectTransitions);
												numIntersectTransitions++;
												lpn.addMovement("p" +(numPlaces-1),"tInter"+(numIntersectTransitions-1));
												lpn.addMovement("p" +(numPlaces-1),"tInter"+(numIntersectTransitions-2));
												lpn.addPlace("p"+numPlaces, false);
												numPlaces++;
												lpn.addMovement("tInter"+(numIntersectTransitions-1), "p" +(numPlaces-1)); 
											}
										}
										if(!((j-1)<0)){
											if(currProp[j-1].matches("intersect") || currProp[j-1].matches("or")){
												lpn.addPlace("p"+numPlaces, false);
												numPlaces++;
												lpn.addMovement("tInter"+(numIntersectTransitions-2), "p" +(numPlaces-1)); 
											}
										}
										lpn.addInput(currProp[j], "false");

										lpn.addTransition("t" + numTransitions);
										numTransitions++;
										//lpn.addEnabling("t" +(numTransitions-1), clock+"&"+currProp[j]);
										lpn.addEnabling("t" +(numTransitions-1), currProp[j]);
										lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));

										if(!(currProp[j].matches("1"))){
											lpn.addTransition("tFail" + numFailTransitions);
											numFailTransitions++;
											lpn.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));

											lpn.addEnabling("tFail" +(numFailTransitions-1), "(~"+currProp[j]+")");

											lpn.addPlace("p"+numPlaces, false);
											numPlaces++;
											lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1)); 

											lpn.addPlace("pFail"+numFailPlaces, false);
											numFailPlaces++;
											lpn.addMovement("tFail"+(numFailTransitions-1), "pFail" +(numFailPlaces-1)); 
										}
										else{
											lpn.addPlace("p"+numPlaces, false);
											numPlaces++;
											lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1)); 

										}
										
										if(((j+1)<currProp.length)){
										if((currProp[j+1].matches("intersect")) || (currProp[j+1].matches("or")) ){
											lpn.addTransition("tDec" + numDecisionTransitions);
											numDecisionTransitions++;
											lpn.addMovement("p" +(numPlaces-1), "tDec" + (numDecisionTransitions-1));
											lpn.addTransition("tDec" + numDecisionTransitions);
											numDecisionTransitions++;
											lpn.addMovement("pFail" +(numFailPlaces-1), "tDec" + (numDecisionTransitions-1));
											lpn.addTransition("tDec" + numDecisionTransitions);
											numDecisionTransitions++;
											lpn.addMovement("p" +(numPlaces-1), "tDec" + (numDecisionTransitions-1));
											lpn.addTransition("tDec" + numDecisionTransitions);
											numDecisionTransitions++;
											lpn.addMovement("pFail" +(numFailPlaces-1), "tDec" + (numDecisionTransitions-1));

										}
									}
										
										if(!((j-1)<0)){
											if((currProp[j-1].matches("intersect")) || (currProp[j-1].matches("or")) ){
												
												lpn.addMovement("p" +(numPlaces-1), "tDec" + (numDecisionTransitions-1));
												
												lpn.addMovement("p" +(numPlaces-1), "tDec" + (numDecisionTransitions-2));
												
												lpn.addMovement("pFail" +(numFailPlaces-1), "tDec" + (numDecisionTransitions-3));
												
												lpn.addMovement("pFail" +(numFailPlaces-1), "tDec" + (numDecisionTransitions-4));
											//}
											//if((currProp[j-1].matches("intersect")) || (currProp[j-1].matches("or")) ){
												lpn.addPlace("p" +numPlaces, false);
												numPlaces++;
												lpn.addPlace("pFail" +numFailPlaces, false);
												numFailPlaces++;
											}
											if(currProp[j-1].matches("intersect")){
												//int counter2=0;
												for(int s=1; s<=4;s++){
													int counter1=0;
												String[] prePlaces = lpn.getPreset("tDec"+(numDecisionTransitions-s));
												for(int t=0; t<prePlaces.length;t++){
													String place = prePlaces[t];
													if(place.matches("p"+"\\d+")){
														counter1++;
													}
													
												}
													if(Integer.toString(counter1).matches("2")){
														
														lpn.addMovement("tDec" + (numDecisionTransitions-s),"p" +(numPlaces-1));
													}
													else{
														
														lpn.addMovement("tDec" + (numDecisionTransitions-s),"pFail" +(numFailPlaces-1));
													}
												} 

											}
											else if(currProp[j-1].matches("or")){
												//int counter2=0;
												for(int s=1; s<=4;s++){
													int counter2=0;
												String[] prePlaces = lpn.getPreset("tDec"+(numDecisionTransitions-s));
												for(int t=0; t<prePlaces.length;t++){
													String place = prePlaces[t];
													if(place.matches("pFail"+"\\d+")){
														counter2++;
													}
													
												}
													if(Integer.toString(counter2).matches("2")){
														
														lpn.addMovement("tDec" + (numDecisionTransitions-s),"pFail" +(numFailPlaces-1));
													}
													else{
														
														lpn.addMovement("tDec" + (numDecisionTransitions-s),"p" +(numPlaces-1));
													}
												} 

											}
										}
									}
								}
								else if(Pattern.matches("#0", currProp[j])){

								}
								else if(Pattern.matches("\\w+"+"\\["+"\\*"+"\\w+"+"\\]", currProp[j])){
									String[] enable = currProp[j].split("\\[");
									for(int l=0;l<enable.length;l++){
										//System.out.println("\n hellos : "+enable[l]);
										//if(enable[l]!=null){
											//delay=enable[l];
										//}
									}
									lpn.addInput(enable[0], "false");

									lpn.addTransition("t" + numTransitions);
									numTransitions++;
									//lpn.addEnabling("t" +(numTransitions-1), clock+"&"+currProp[j]);
									lpn.addEnabling("t" +(numTransitions-1), enable[0]);
									lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
									String[] delay2 = enable[1].split("[\\*\\[\\]]");
									for(int m=0;m<delay2.length;m++){
										//System.out.println("\n delay : "+delay2[m]);
										if(delay2[m]!=null){
											delay=delay2[m];
										}
									}
									lpn.changeDelay("t" +(numTransitions-1), delay);
									
									if(!(enable[0].matches("1"))){
										lpn.addTransition("tFail" + numFailTransitions);
										numFailTransitions++;
										//lpn.addInput(enablingCond[0], "false");

										lpn.addEnabling("tFail" +(numFailTransitions-1), "~"+enable[0]);
										lpn.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
										
										lpn.addPlace("p"+numPlaces, false);
										numPlaces++;
										lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1)); 

										lpn.addPlace("pFail"+numFailPlaces, false);
										numFailPlaces++;
										lpn.addMovement("tFail"+(numFailTransitions-1), "pFail" +(numFailPlaces-1)); 

									}
									else{
										lpn.addPlace("p"+numPlaces, false);
										numPlaces++;
										lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1));

									}
									
								}
								else if(Pattern.matches("\\w+"+"\\[~>\\w+\\]", currProp[j])){
									//System.out.println("\n hello");

									if(((j+1)<currProp.length)){
										if(currProp[j+1].matches("intersect")){
											lpn.addTransition("tInter" + numIntersectTransitions);
											numIntersectTransitions++;
											lpn.addTransition("tInter" + numIntersectTransitions);
											numIntersectTransitions++;
											lpn.addMovement("p" +(numPlaces-1),"tInter"+(numIntersectTransitions-1));
											lpn.addMovement("p" +(numPlaces-1),"tInter"+(numIntersectTransitions-2));
											lpn.addPlace("p"+numPlaces, false);
											numPlaces++;
											lpn.addMovement("tInter"+(numIntersectTransitions-1), "p" +(numPlaces-1)); 
										}
									}
									if(!((j-1)<0)){
										if(currProp[j-1].matches("intersect")){
											lpn.addPlace("p"+numPlaces, false);
											numPlaces++;
											lpn.addMovement("tInter"+(numIntersectTransitions-2), "p" +(numPlaces-1)); 
										}
									}

									//lpn.addMovement("tInter"+(numIntersectTransitions-1), "p" +(numPlaces-1)); 
									//lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1)); 
									String[] enablingCond = currProp[j].split("\\[");
									lpn.addTransition("t" + numTransitions);
									numTransitions++;
									lpn.addInput(enablingCond[0], "false");
									lpn.addEnabling("t" +(numTransitions-1), enablingCond[0]);
									lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));

									if(!(enablingCond[0].matches("1"))){
										lpn.addTransition("tFail" + numFailTransitions);
										numFailTransitions++;
										//lpn.addInput(enablingCond[0], "false");

										lpn.addEnabling("tFail" +(numFailTransitions-1), "~"+enablingCond[0]);

										lpn.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
										lpn.addPlace("p"+numPlaces, false);
										numPlaces++;
										lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1)); 

										lpn.addPlace("pFail"+numFailPlaces, false);
										numFailPlaces++;
										lpn.addMovement("tFail"+(numFailTransitions-1), "pFail" +(numFailPlaces-1)); 

									}
									else{
										lpn.addPlace("p"+numPlaces, false);
										numPlaces++;
										lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1));

									}

								}
								else if(Pattern.matches("intersect", currProp[j])){
									//System.out.println("\n hello");

								}
								else if(Pattern.matches("#"+"\\["+"\\w+"+"\\]", currProp[j])){
									//System.out.println("\n namaste");
									
									lpn.addTransition("t" + numTransitions);
									numTransitions++;
									lpn.addMovement("p" +(numPlaces-1),"t"+(numTransitions-1));
									String[] enable = currProp[j].split("[#\\[\\]]");
									for(int l=0;l<enable.length;l++){
										//System.out.println("\n hellos : "+enable[l]);
										if(enable[l]!=null){
											delay=enable[l];
										}
									}
									
									lpn.changeDelay("t" +(numTransitions-1), delay);
									
									lpn.addPlace("p"+numPlaces, false);
									numPlaces++;
									lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1));

								}
								else if(Pattern.matches("(\\w+)(\\[)(\\*)(\\d+)(\\:)(\\d+)(\\])", currProp[j])){

								//	System.out.println("\n hello");

									if(((j+1)<currProp.length)){
										if(currProp[j+1].matches("intersect")){
											lpn.addTransition("tInter" + numIntersectTransitions);
											numIntersectTransitions++;
											lpn.addTransition("tInter" + numIntersectTransitions);
											numIntersectTransitions++;
											lpn.addMovement("p" +(numPlaces-1),"tInter"+(numIntersectTransitions-1));
											lpn.addMovement("p" +(numPlaces-1),"tInter"+(numIntersectTransitions-2));
											lpn.addPlace("p"+numPlaces, false);
											numPlaces++;
											lpn.addMovement("tInter"+(numIntersectTransitions-1), "p" +(numPlaces-1)); 
										}
									}
									if(!((j-1)<0)){
										if(currProp[j-1].matches("intersect")){
											lpn.addPlace("p"+numPlaces, false);
											numPlaces++;
											lpn.addMovement("tInter"+(numIntersectTransitions-2), "p" +(numPlaces-1)); 
										}
									}

									String[] enablingCond = currProp[j].split("\\[");
									lpn.addTransition("t" + numTransitions);
									numTransitions++;
									lpn.addInput(enablingCond[0], "false");
									String[] enable = enablingCond[1].split("(\\*)");
									String[] enable2 =enable[1].split("\\]");
									String[] enable3 = enable2[0].split("\\:");
									lpn.changeDelay("t"+(numTransitions-1), enable3[0]+","+enable3[1]);
									lpn.addEnabling("t" +(numTransitions-1), enablingCond[0]);
									lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
									if(!(enablingCond[0].matches("1"))){
										lpn.addTransition("tFail" + numFailTransitions);
										numFailTransitions++;
										lpn.addInput(enablingCond[0], "false");

										lpn.addEnabling("tFail" +(numFailTransitions-1), "~"+enablingCond[0]);
										//}
										lpn.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));

										lpn.addPlace("p"+numPlaces, false);
										numPlaces++;
										lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1)); 


										lpn.addPlace("pFail"+numFailPlaces, false);
										numFailPlaces++;
										lpn.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numFailPlaces-1));
									}
									else{
										lpn.addPlace("p"+numPlaces, false);
										numPlaces++;
										lpn.addMovement("t"+(numTransitions-1), "p" +(numPlaces-1)); 

									}

								}
							}
							//if(Pattern.matches("\\|->", currprop[i])){

							//else if(Pattern.matches("\\|=>", prop[i])){

							//else if(Pattern.matches("##"+"\\d+", prop[i])){

							//else if(Pattern.matches("(\\##)(\\[)(\\d+)(\\,)(\\d+)(\\])", prop[i])){

						}  // added now

					}
				}

				lpn.save(root + separator +lpnFile);


			}

		}
		catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Unable to create new model.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}

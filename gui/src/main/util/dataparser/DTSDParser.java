package main.util.dataparser;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import gnu.trove.map.hash.TDoubleObjectHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class DTSDParser {

	private LinkedHashMap<Double, HashMap<String, Double> > timeToDataMapMap = 
		new LinkedHashMap<Double, HashMap<String, Double> >();
	
	private TIntObjectHashMap<HashMap<String, Double> > frameToDataMapMap =
		new TIntObjectHashMap<HashMap<String, Double> >();
	
	private TDoubleObjectHashMap<HashMap<String, Point> > frameToComponentToLocationMapMap = 
		new TDoubleObjectHashMap<HashMap<String, Point> >();
	
	private ArrayList<String> allSpecies = new ArrayList<String>();
	
	int overallMinRow = Integer.MAX_VALUE;
	int overallMinCol = Integer.MAX_VALUE;
	int overallMaxRow = Integer.MIN_VALUE;
	int overallMaxCol = Integer.MIN_VALUE;
	
	
	/**
	 * parses and creates timeToDataMapMap
	 * 
	 * @param filename
	 */
	public DTSDParser(String filename) {
		
		FileInputStream fileInput = null;
		BufferedInputStream fileStream = null;
		
		allSpecies.add("time");
		
		try {
		
			fileInput = new FileInputStream(new File(filename));
			fileStream = new BufferedInputStream(fileInput);
			
			//get rid of the opening '('
			if (((char) fileStream.read()) != '(')
				return;
			
			boolean stopReading = false;
			
			while (stopReading == false) {
				
				HashMap<String, Double> speciesToValueMap = new HashMap<String, Double>();
				ArrayList<String> speciesList = new ArrayList<String>();
				
				char currentChar = (char) fileStream.read();
				
				//get rid of the opening '(' or the '\n' from the previous block
				currentChar = (char) fileStream.read();
				
				//read a species ID block
				while (true) {
					
					String currentSpeciesID = "";
					
					//get rid of the first '"'
					currentChar = (char) fileStream.read();
					
					while (currentChar != '"') {
						
						currentSpeciesID += currentChar;
						currentChar = (char) fileStream.read();
					}
					
					if (currentSpeciesID.equals("time") == false)
						speciesList.add(currentSpeciesID);
					
					//get rid of the space between IDs
					currentChar = (char) fileStream.read();
					
					if (currentChar == ')')
						break;
					
					currentChar = (char) fileStream.read();					
					currentChar = (char) fileStream.read();
				}
				
				//get rid of the opening ",\ns("
				currentChar = (char) fileStream.read();
				currentChar = (char) fileStream.read();
				currentChar = (char) fileStream.read();
				currentChar = (char) fileStream.read();
				
				int index = 0;
				double time = 0.0;
				
				//read a species values block
				while (true) {
					
					String currentSpeciesValue = "";
					
					while (currentChar != ',' && currentChar != ')') {
						
						currentSpeciesValue += currentChar;
						currentChar = (char) fileStream.read();
					}
					
					if (index == 0)
						time = Double.valueOf(currentSpeciesValue);
					else {
						speciesToValueMap.put(speciesList.get(index - 1), Double.valueOf(currentSpeciesValue));
					}
					
					++index;
					
					if (currentChar == ')')
						break;
					
					//get rid of the comma and space
					currentChar = (char) fileStream.read();
					currentChar = (char) fileStream.read();
				}
		
				//look for end of file
				if (currentChar == ')') {
					
					fileStream.mark(1);
					
					if ((char) fileStream.read() == ')') {
						
						stopReading = true;
						fileStream.reset();
					}
					else {
						
						//get rid of the ","
						currentChar = (char) fileStream.read();
					}
				}
				
				timeToDataMapMap.put(time, speciesToValueMap);				
			} //end read loop
			
//			for (double time : timeToDataMapMap.keySet()) {
//				
//				System.err.println("time: " + time);
//				System.err.println("-----------------------");
//				
//				for (String species : timeToDataMapMap.get(time).keySet()) {
//					
//					System.err.println(species + "  " + timeToDataMapMap.get(time).get(species));
//				}
//			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setupGrid();
	}

	public HashMap<String, Point> getComponentToLocationMap(int frameIndex) {
	
		return frameToComponentToLocationMapMap.get(frameIndex);
	}

	/**
	 * returns data in a format for the grapher
	 * @return
	 */
	public ArrayList<ArrayList<Double>> getData() {
		
		ArrayList<ArrayList<Double>> speciesData = new ArrayList<ArrayList<Double>>();
		
		int index = 0;
		
		for (String speciesID : allSpecies) {
			
			speciesData.add(new ArrayList<Double>());
			
			if (speciesID.equals("time")) {
				
				for (Double time : timeToDataMapMap.keySet())
					speciesData.get(index).add(time);
			}
			else {
				for (HashMap<String, Double> dataMap : timeToDataMapMap.values()) {
					
					if (dataMap.containsKey(speciesID)) {					
						speciesData.get(index).add(dataMap.get(speciesID));
					}
					else speciesData.get(index).add(0.0);
				}
			}
			
			++index;
		}
		
		return speciesData;
	}
	
	/**
	 * returns a hashmap in a format for printing statistics
	 * @return
	 */
	public HashMap<String, ArrayList<Double>> getHashMap(ArrayList<String> allSpecies) {
		
		HashMap<String, ArrayList<Double>> speciesData = new HashMap<String, ArrayList<Double>>();
		
		for (String speciesID : allSpecies) {
			
			speciesData.put(speciesID, new ArrayList<Double>());
			
			if (speciesID.equals("time")) {
				
				for (Double time : timeToDataMapMap.keySet())
					speciesData.get(speciesID).add(time);
			}
			else {
				for (HashMap<String, Double> dataMap : timeToDataMapMap.values()) {
					
					if (dataMap.containsKey(speciesID)) {					
						speciesData.get(speciesID).add(dataMap.get(speciesID));
					}
					else speciesData.get(speciesID).add(0.0);
				}
			}
		}
		
		return speciesData;
	}
	
	/**
	 * returns the dynamic tsd data in hashmap form
	 * 
	 * @return
	 */
	public LinkedHashMap<Double, HashMap<String, Double> > getTimeToDataMapMap() {
		
		return timeToDataMapMap;
	}

	/**
	 * returns an arraylist of every species (ie, the max number of species)
	 * 
	 * @return
	 */
	public ArrayList<String> getSpecies() {
		
		return allSpecies;
	}
	
	/**
	 * returns species and values for a particular frame
	 * 
	 * @param frameIndex
	 * @return
	 */
	public HashMap<String, Double> getSpeciesToValueMap(int frameIndex) {
		
		return frameToDataMapMap.get(frameIndex);
	}
	
	/**
	 * returns the number of time data points (ie, samples) for this file
	 * 
	 * @return
	 */
	public int getNumSamples() {
		
		return timeToDataMapMap.size();
	}

	/**
	 * @return number of rows at max grid size
	 */
	public int getNumRows() {
		
		return overallMaxRow - overallMinRow + 1;
	}
	
	/**
	 * @return number of cols at max grid size
	 */
	public int getNumCols() {
		
		return overallMaxCol - overallMinCol + 1;
	}
	
	/**
	 * parses the data and figures out the grid size for each time point
	 */
	public void setupGrid() {
		
		int dataIndex = 0;
		
		for (Double time : timeToDataMapMap.keySet()) {
			
			HashMap<String, Double> frameDataMap = new HashMap<String, Double>();
			frameDataMap = timeToDataMapMap.get(time);
			frameToComponentToLocationMapMap.put(dataIndex, new HashMap<String, Point>());
			HashMap<String, Point> componentToLocationMap = frameToComponentToLocationMapMap.get(dataIndex);
			
			int minRow = Integer.MAX_VALUE;
			int maxRow = Integer.MIN_VALUE;
			int minCol = Integer.MAX_VALUE;
			int maxCol = Integer.MIN_VALUE;
			
			if (frameDataMap != null) {
				
				for (Map.Entry<String, Double> dataIterator : frameDataMap.entrySet()) {
				
					//find the min/max row/col and also create a map from componentID to location
					
					if (dataIterator.getKey().contains("__locationX")) {
						
						int row = (int) dataIterator.getValue().doubleValue();				
						//String compID = dataIterator.getKey().split("__")[0];
						String compID = dataIterator.getKey().replace("__locationX","");
						
						
						if (componentToLocationMap.get(compID) == null)
							componentToLocationMap.put(compID, new Point());
						
						componentToLocationMap.get(compID).x = row;
						
						if (row < minRow)
							minRow = row;
						
						if (row > maxRow)
							maxRow = row;
					}				
					else if (dataIterator.getKey().contains("__locationY")) {
						
						int col = (int) dataIterator.getValue().doubleValue();
						//String compID = dataIterator.getKey().split("__")[0];
						String compID = dataIterator.getKey().replace("__locationY","");
						
						if (componentToLocationMap.get(compID) == null)
							componentToLocationMap.put(compID, new Point());
						
						componentToLocationMap.get(compID).y = col;
						
						if (col < minCol)
							minCol = col;
						
						if (col > maxCol)
							maxCol = col;
					}
				}
			}
			
			frameToDataMapMap.put(dataIndex, frameDataMap);
			
			if (minRow < overallMinRow)
				overallMinRow = minRow;
			
			if (maxRow > overallMaxRow)
				overallMaxRow = maxRow;
			
			if (minCol < overallMinCol)
				overallMinCol = minCol;
				
			if (maxCol > overallMaxCol)
				overallMaxCol = maxCol;
			
			++dataIndex;
		}//end for loop over time points
		
		dataIndex = 0;
		
		HashSet<String> speciesSet = new HashSet<String>();
		
		//adjust locations so that they display properly
		for (HashMap<String, Double> speciesToValueMap : timeToDataMapMap.values()) {
			
			HashMap<String, Double> speciesToAdd = new HashMap<String, Double>();
			HashSet<String> speciesToRemove = new HashSet<String>();
			
			//update the grid species names due to the new grid size
			for (String speciesID : speciesToValueMap.keySet()) {
				
				if (speciesID.contains("ROW") && speciesID.contains("COL") && speciesID.contains("__")) {
					
					int row = Integer.parseInt(((String[])speciesID.split("_"))[0].replace("ROW",""));
					int col = Integer.parseInt(((String[])speciesID.split("_"))[1].replace("COL",""));
					
					String underlyingSpeciesID = ((String[])speciesID.split("__"))[1];
					
					speciesToAdd.put("ROW" + (row - overallMinRow) + "_COL" + (col - overallMinCol) + "__" + underlyingSpeciesID,
							speciesToValueMap.get(speciesID));
					speciesToRemove.add(speciesID);
				}
			}
			
			for (String specToRemove : speciesToRemove)
				speciesToValueMap.remove(specToRemove);
			
			speciesToValueMap.putAll(speciesToAdd);
			
			speciesSet.addAll(speciesToValueMap.keySet());
			
			//update the component locations due to the new grid size
			
			HashMap<String, Point> componentToLocationMap = frameToComponentToLocationMapMap.get(dataIndex);
			
			for (Point location : componentToLocationMap.values()) {
			
				location.x -= overallMinRow;
				location.y -= overallMinCol;
			}
			
			++dataIndex;
		}
		
		allSpecies.addAll(speciesSet);
	}
	

}

package frontend.main.util.dataparser;

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
	
	private TIntObjectHashMap<Point> frameToRowColMap = new TIntObjectHashMap<Point>();
	private TIntObjectHashMap<Point> frameToMinRowColMap = new TIntObjectHashMap<Point>();
	
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
			if (((char) fileStream.read()) != '(') {
				fileInput.close();
				return;
			}
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
	
	public int getNumRowsAtFrame(int frameIndex) {
		return (int) this.frameToRowColMap.get(frameIndex).getX();
	}
	
	public int getNumColsAtFrame(int frameIndex) {
		return (int) this.frameToRowColMap.get(frameIndex).getY();
	}
	
	public int getMinRowAtFrame(int frameIndex) {
		return (int) this.frameToMinRowColMap.get(frameIndex).getX();
	}
	
	public int getMinColAtFrame(int frameIndex) {
		return (int) this.frameToMinRowColMap.get(frameIndex).getY();
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
			
			int minRow = 0; //Integer.MAX_VALUE;
			int maxRow = Integer.MIN_VALUE;
			int minCol = 0; //Integer.MAX_VALUE;
			int maxCol = Integer.MIN_VALUE;
			
			if (frameDataMap != null) {
				
				for (Map.Entry<String, Double> dataIterator : frameDataMap.entrySet()) {
				
					//find the min/max row/col
					//the grid size will be reflected in the existant grid species and therefore their IDs
					if (dataIterator.getKey().contains("ROW") && dataIterator.getKey().contains("COL")) {
						
						String rowCol = dataIterator.getKey().split("__")[0];
						int row = Integer.parseInt(rowCol.split("_")[0].replace("ROW",""));
						int col = Integer.parseInt(rowCol.split("_")[1].replace("COL",""));
						
						if (row < minRow)
							minRow = row;
					
						if (row > maxRow)
							maxRow = row;	
						
						if (col < minCol)
							minCol = col;
						
						if (col > maxCol)
							maxCol = col;
					}
					
					//create a map from componentID to location
					if (dataIterator.getKey().contains("__locationX")) {
						
						int row = (int) dataIterator.getValue().doubleValue();
						String compID = dataIterator.getKey().replace("__locationX","");						
						
						if (componentToLocationMap.get(compID) == null)
							componentToLocationMap.put(compID, new Point());
						
						componentToLocationMap.get(compID).x = row;
						if (row > maxRow) {
							maxRow = row;
						}
						if (row < minRow) {
							minRow = row;
						}
					}				
					else if (dataIterator.getKey().contains("__locationY")) {
						
						int col = (int) dataIterator.getValue().doubleValue();
						String compID = dataIterator.getKey().replace("__locationY","");
						
						if (componentToLocationMap.get(compID) == null)
							componentToLocationMap.put(compID, new Point());
						
						componentToLocationMap.get(compID).y = col;
						if (col > maxCol) {
							maxCol = col;
						}
						if (col < minCol) {
							minCol = col;
						}
					}
				}
			}
			
			int newNumRows = maxRow - minRow + 1;
			int newNumCols = maxCol - minCol + 1;
			int oldNumRows = 0;
			int oldNumCols = 0;
			
			if (dataIndex > 0) {
				
				//don't shrink the number of rows/cols
				oldNumRows = (int) frameToRowColMap.get(dataIndex - 1).getX();
				oldNumCols = (int) frameToRowColMap.get(dataIndex - 1).getY();
				
				if (oldNumRows > newNumRows)
					newNumRows = oldNumRows;
				if (oldNumCols > newNumCols)
					newNumCols = oldNumCols;
				
				//don't increase the min row/col
				int oldMinRow = (int) frameToMinRowColMap.get(dataIndex - 1).getX();
				int oldMinCol = (int) frameToMinRowColMap.get(dataIndex - 1).getY();
				
				if (minRow > oldMinRow)
					minRow = oldMinRow;
				if (minCol > oldMinCol)
					minCol = oldMinCol;
			}
			
			frameToRowColMap.put(dataIndex, new Point(newNumRows, newNumCols));
			frameToMinRowColMap.put(dataIndex, new Point(minRow, minCol));
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
					
					int row = Integer.parseInt(speciesID.split("_")[0].replace("ROW",""));
					int col = Integer.parseInt(speciesID.split("_")[1].replace("COL",""));
					
					String underlyingSpeciesID = speciesID.split("__")[1];
					
					speciesToAdd.put("ROW" + (row - overallMinRow) + "_COL" 
							+ (col - overallMinCol) + "__" + underlyingSpeciesID,
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
		
		for (Point minRowCol : frameToMinRowColMap.valueCollection()) {
			
			minRowCol.x = minRowCol.x - overallMinRow;
			minRowCol.y = minRowCol.y - overallMinCol;
		}
		
		allSpecies.addAll(speciesSet);
	}
	

}

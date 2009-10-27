package learn;

//import java.util.*;

public class Variable implements Comparable<Variable>{

	private String name;
	
	private boolean dmvc;
	
	private boolean input;
	
	private boolean output;
	
	private DMVCrun runs; // tmp
	
	private Double initValue_vMax,initRate_rMax;
	
	private Double initValue_vMin,initRate_rMin;
	
	public Variable(String name){
		this.name = name;
		this.runs = new DMVCrun();
	}

	public boolean isDmvc() {
		return dmvc;
	}

	public void setDmvc(boolean dmvc) {
		this.dmvc = dmvc;
	}

	public boolean isInput() {
		return input;
	}

	public void setInput(boolean input) {
		this.input = input;
	}

	public String getName() {
		return name;
	}

//	public void setName(String name) {
//		this.name = name;
//	}

	public boolean isOutput() {
		return output;
	}

	public void setOutput(boolean output) {
		this.output = output;
	}

	public DMVCrun getRuns() {
		return runs;
	}
	
	public void addInitValues(Double v){
		if ((initValue_vMin == null) && (initValue_vMax == null)){
			initValue_vMin = v;
			initValue_vMax = v;
		}
		else{
			if (v < initValue_vMin){
				initValue_vMin = v;
			}
			else if (v > initValue_vMax){
				initValue_vMax = v;
			}
		}
	}
	
	public void addInitRates(Double r){
		if ((initRate_rMin == null) && (initRate_rMax == null)){
			initRate_rMin = r;
			initRate_rMax = r;
		}
		else{
			if (r < initRate_rMin){
				initRate_rMin = r;
			}
			else if (r > initRate_rMax){
				initRate_rMax = r;
			}
		}
	}
	
	public void reset(){
		initRate_rMax = null;
		initRate_rMin = null;
		initValue_vMin = null;
		initValue_vMax = null;
		this.runs = new DMVCrun();
	}
	public String getInitValue(){
		return ("["+(int)Math.floor(initValue_vMin)+","+(int)Math.ceil(initValue_vMax)+"]");
	}
	
	public String getInitRate(){
		return ("["+(int)Math.floor(initRate_rMin)+","+(int)Math.ceil(initRate_rMax)+"]");
	}
	
	public void scaleInitByDelay(Double dScaleFactor){
		if (!dmvc){
			initRate_rMin /=  dScaleFactor;
			initRate_rMax /=  dScaleFactor;
		}
	}
	
	public void scaleInitByVar(Double vScaleFactor){
		if (!dmvc){
			initRate_rMin *=  vScaleFactor;
			initRate_rMax *=  vScaleFactor;
		}
		initValue_vMax *=  vScaleFactor;
		initValue_vMin *=  vScaleFactor;
	}
	
	//@Override
	public int compareTo(Variable o) {
		return (this.getName().compareToIgnoreCase(o.getName()));
	}
	/*
	public void addInitValues(Double d, int i){
		if (initValues.isEmpty()){
			for (int j = 0; j < vars.length; j++){
				initValues.add(j, new ArrayList<Double>());
			}
			initValues.get(i).add(d);
		}
		else{
			initValues.get(i).add(d);
		}
		//this.initValues[j] = d;
	}
	
	public void addInitRates(Double d, int i){
		if (initRates.isEmpty()){
			for (int j = 0; j < vars.length; j++){
				initRates.add(j, new ArrayList<Double>());
			}
			initRates.get(i).add(d);
		}
		else{
			initRates.get(i).add(d);
		}
		//this.initRates[j] = d;
	}
	*/

	
}

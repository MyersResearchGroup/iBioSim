package VerilogConstructs;

public class VerilogDelay implements AbstractVerilogConstruct{
	
	private int delayValue; 
	
	public void setDelayValue(int value) {
		this.delayValue = value;
	}
	
	public int getDelayValue() {
		return this.delayValue;
	}
	
	@Override
	public void addConstruct(AbstractVerilogConstruct construct) { }


}

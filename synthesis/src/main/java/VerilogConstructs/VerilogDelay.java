package VerilogConstructs;

public class VerilogDelay implements AbstractVerilogConstruct{
	
	private String delayValue;
	
	public void setDelayValue(String value) {
		this.delayValue = value;
	}
	
	public String getDelayValue() {
		return this.delayValue;
	}
	
	@Override
	public void addConstruct(AbstractVerilogConstruct construct) { }


}


public class ConstantSymbol extends Symbol{
	
	private int value;
	
	public ConstantSymbol(int value) {
		this.value = value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public String toString() {
		return String.format("ConstantSymbol(%d)", value);
	}

}


public class ConstantSymbol extends Symbol{
	
	public ConstantSymbol(String value) {
		super(value);
	}
	
	public String toString() {
		return String.format("ConstantSymbol(%s)", super.getId());
	}

}


public class VariableSymbol extends Symbol{
	private int nextUse;
	
	// this isn't data stype isn't the best for
	// memory locations.
	private String memLoc;
	
	public VariableSymbol(String id) {
		super(id);
	}

	public int getNextUse() {
		return nextUse;
	}

	public void setNextUse(int nextUse) {
		this.nextUse = nextUse;
	}

	public String getMemLoc() {
		return memLoc;
	}

	public void setMemLoc(String memLoc) {
		this.memLoc = memLoc;
	}
	
	public String toString() {
		return String.format("VariableSymbol(id = '%s', nextUse = %d, memLoc = '%s')", id, nextUse, memLoc);
	}

}

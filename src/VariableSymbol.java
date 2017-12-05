
public class VariableSymbol extends Symbol{
	private int nextUse;
	
	// this isn't data stype isn't the best for
	// memory locations.
	private int memLoc;
	
	public VariableSymbol(String id) {
		super(id);
		this.memLoc = -1;
	}

	public int getNextUse() {
		return nextUse;
	}

	public void setNextUse(int nextUse) {
		this.nextUse = nextUse;
	}

	public int getMemLoc() {
		return memLoc;
	}

	public void setMemLoc(int memLoc) {
		this.memLoc = memLoc;
	}
	
	public String toString() {
		return String.format("VariableSymbol(id = '%s', nextUse = %d, memLoc = '%s')", id, nextUse, memLoc);
	}
	
	public boolean isTempVariable() {
		return this.getId().startsWith(".") && this.getId().length() == 6;
	}

}

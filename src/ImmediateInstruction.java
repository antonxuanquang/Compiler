
public class ImmediateInstruction {
	private String operator;
	private Symbol op1;
	private Symbol op2;
	private Symbol result;
	private int startAddr;
	
	public ImmediateInstruction(String operator, Symbol op1, Symbol op2, Symbol result) {
		this.operator = operator;
		this.op1 = op1;
		this.op2 = op2;
		this.result = result;
		this.startAddr = 0;
	}
	
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Symbol getOp1() {
		return op1;
	}

	public void setOp1(Symbol op1) {
		this.op1 = op1;
	}

	public Symbol getOp2() {
		return op2;
	}

	public void setOp2(Symbol op2) {
		this.op2 = op2;
	}

	public Symbol getResult() {
		return result;
	}

	public void setResult(Symbol result) {
		this.result = result;
	}

	public int getStartAddr() {
		return startAddr;
	}

	public void setStartAddr(int startAddr) {
		this.startAddr = startAddr;
	}
	
	public String toString() {
		return String.format("operator: %s\n\t"
				+ "op1: %s\n\t"
				+ "op2: %s\n\t"
				+ "result: %s\n\t"
				+ "startAdd: %d\n\n", operator, op1, op2, result, startAddr);
	}
}

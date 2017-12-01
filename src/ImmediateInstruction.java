
public class ImmediateInstruction {
	private String operator;
//	private Symbol op1;
//	private Symbol op2;
//	private Symbol result;
	private String left;
	private int leftNextUse;
	private String right;
	private int rightNextUse;
	private String result;
	private int resultNextUse;
	private int startAddr;
	
	public ImmediateInstruction(String operator, String left, String right, String result) {
		this.operator = operator;
		this.left = left;
		this.right = right;
		this.result = result;
		this.startAddr = 0;
		this.leftNextUse = this.rightNextUse = this.resultNextUse = -1;
	}
	
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public String getLeft() {
		return left;
	}

	public void setLeft(String left) {
		this.left = left;
	}

	public int getLeftNextUse() {
		return leftNextUse;
	}

	public void setLeftNextUse(int leftNextUse) {
		this.leftNextUse = leftNextUse;
	}

	public String getRight() {
		return right;
	}

	public void setRight(String right) {
		this.right = right;
	}

	public int getRightNextUse() {
		return rightNextUse;
	}

	public void setRightNextUse(int rightNextUse) {
		this.rightNextUse = rightNextUse;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public int getResultNextUse() {
		return resultNextUse;
	}

	public void setResultNextUse(int resultNextUse) {
		this.resultNextUse = resultNextUse;
	}

	public int getStartAddr() {
		return startAddr;
	}

	public void setStartAddr(int startAddr) {
		this.startAddr = startAddr;
	}
	
	public String toString() {
		return String.format("%s | %s @ %d | %s @ %d | %s @ %d | %d\n", operator, left, 
				leftNextUse, right, rightNextUse, result, resultNextUse, startAddr);
	}
}

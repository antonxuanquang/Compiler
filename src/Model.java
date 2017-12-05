import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Model {
	
	private Map<String, VariableSymbol> symbolTable;
	private List<ImmediateInstruction> immediateInstructionList;
	private Stack<Integer> instructionCounterStack;
	private Stack<Symbol> symbolStack;
	private Stack<Integer> whileStack;
	
	public Model() {
		symbolTable = new HashMap<String, VariableSymbol>();
		immediateInstructionList = new ArrayList<ImmediateInstruction>();
		instructionCounterStack = new Stack<Integer>();
		symbolStack = new Stack<Symbol>();
		whileStack = new Stack<Integer>();
	}
	
	public int getNextImmediateInstructionCounter() {
		return immediateInstructionList.size();
	}
	
	public Map<String, VariableSymbol> getSymbolTable() {
		return symbolTable;
	}

	public List<ImmediateInstruction> getImmediateInstructionList() {
		return immediateInstructionList;
	}
	
	public Stack<Integer> getInstructionCounterStack() {
		return instructionCounterStack;
	}
	
	public Stack<Symbol> getSymbolStack() {
		return symbolStack;
	}
	
	public Stack<Integer> getWhileStack() {
		return whileStack;
	}
	
	public void generateImmediateInstruction(
			String operator, String left, String right, String result) {
		ImmediateInstruction instruction = new ImmediateInstruction(
				operator, left, right, result);
		immediateInstructionList.add(instruction);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
//		sb.append("**************Immediate Instruction*****************\n");
//		for(int i = 0; i < immediateInstructionList.size(); i++) {
//			sb.append(i + "/ " + immediateInstructionList.get(i) + "\n");
//		}
//		sb.append("\n");
		
		sb.append("**************Symbol Table*****************\n");
		for (String key: symbolTable.keySet()) {
			sb.append(String.format("%s: %s\n", key, symbolTable.get(key)));
		}
		sb.append("\n");
		
//		sb.append("**************Symbol Stack*****************\n");
//		for (Symbol symbol: symbolStack) {
//			sb.append(symbol + "\n");
//		}
//		sb.append("\n");
//		
//		sb.append("**************Counter Stack*****************\n");
//		for (Integer counter: instructionCounterStack) {
//			sb.append(counter + "\n");
//		}
//		sb.append("\n");
//		
//		sb.append("**************While Stack*****************\n");
//		for (Integer counter: whileStack) {
//			sb.append(counter + "\n");
//		}
//		sb.append("\n");
		
		return sb.toString();
		
	}
}
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class CodeGenerator {
	
	private Model model;
	private String inputFileName;
	
	private class BasicBlock {	
		int start;
		int end;
		public BasicBlock(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
		public String toString() {
			return String.format("%d -> %d\n", start, end);
		}
	}
	
	public CodeGenerator(Model model, String fileName) {
		this.model = model;
		this.inputFileName = fileName;
	}
	
	public void genCode() {
		List<BasicBlock> basicBlocks = findBasicBlocks();
		for (BasicBlock block: basicBlocks) {
			int start = block.start;
			int end = block.end;
			List<ImmediateInstruction> instructionList = model.getImmediateInstructionList();
			performLiveVariableAnalysis(start, end);
			System.out.println(model);
			for (int i = start; i <= end; i++) {
				generateCode(instructionList.get(i));
			}
			updateLiveVariable(start, end);
		}
		
	}
	
	private void performLiveVariableAnalysis(int start, int end) {
		// local variables
		Map<String, VariableSymbol> symbolTable = model.getSymbolTable();
		List<ImmediateInstruction> instructionList = model.getImmediateInstructionList();
		
		// initialize next use field in ST for all variables
		for (String key: symbolTable.keySet()) {
			VariableSymbol symbol = symbolTable.get(key);
			String id = symbol.getId();
			if (id.startsWith("T") && id.length() == 6) {
				symbol.setNextUse(-1);
			} else {
				symbol.setNextUse(Integer.MAX_VALUE);
			}
		}
		
		// scan basic block backward
		for (int index = end; index >= start; index--) {
			ImmediateInstruction instruction = instructionList.get(index);
			VariableSymbol left = symbolTable.getOrDefault(instruction.getLeft(), null);
			VariableSymbol right = symbolTable.getOrDefault(instruction.getRight(), null);
			VariableSymbol result = symbolTable.getOrDefault(instruction.getResult(), null);
			
			// copy next use info from ST to the instruction for 'result', 'left', and 'right' symbols
			if (left != null) instruction.setLeftNextUse(left.getNextUse());
			if (right != null) instruction.setRightNextUse(right.getNextUse());
			if (result != null) instruction.setResultNextUse(result.getNextUse());
			
			// set next use = -1 in the ST for 'result' symbols
			if (result != null) result.setNextUse(-1);
			
			// set next use = instruction index for 'left' and 'right' symbols in ST
			if (left != null) left.setNextUse(index);
			if (right != null) right.setNextUse(index);
		}
	}

	private void generateCode(ImmediateInstruction immediateInstruction) {
		// TODO Auto-generated method stub
		
	}

	private void updateLiveVariable(int start, int end) {
		// TODO Auto-generated method stub
		
	}

	private List<BasicBlock> findBasicBlocks() {
		List<BasicBlock> result = new ArrayList<BasicBlock>();
		List<ImmediateInstruction> instructionList = model.getImmediateInstructionList();
		Set<Integer> leaders = new HashSet<Integer>();
		
		// mark leaders
		leaders.add(0);
		for (int i = 0; i < instructionList.size(); i++) {
			ImmediateInstruction instruction = instructionList.get(i);
			if (instruction.getOperator().equals("jump")) {
				leaders.add(Integer.parseInt(instruction.getResult()));
			} else if (instruction.getOperator().equals("jeqz")) {
				leaders.add(i + 1);
			}
		}
		leaders.add(instructionList.size());		
		List<Integer> sortedLeaders = new ArrayList<Integer>(leaders);
		Collections.sort(sortedLeaders);
		
		// find blocks
		int start = sortedLeaders.remove(0);
		while (!sortedLeaders.isEmpty()) {
			int end = sortedLeaders.get(0);
			BasicBlock block = new BasicBlock(start, end - 1);
//			for (int index = start; index < end; index++) {
//				ImmediateInstruction instruction = instructionList.get(index);
//				if (instruction.getOperator().equals("jump")) {
//					block.end = index;
//					break;
//				}
//			}
			result.add(block);
			start = sortedLeaders.remove(0);
		}
		return result;		
	}
	
	

}



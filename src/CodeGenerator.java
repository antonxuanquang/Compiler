import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodeGenerator {
	
	private Model model;
	private String inputFileName;
	private RegisterTable rt;
	private List<String> assemblyCodes;
	
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
		this.assemblyCodes = new ArrayList<String>();
		this.rt = new RegisterTable(model, assemblyCodes);
	}
	
	public void genCode() {
		List<BasicBlock> basicBlocks = findBasicBlocks();
		model.getImmediateInstructionList().add(new ImmediateInstruction("", "", "", ""));
		for (BasicBlock block: basicBlocks) {
			int start = block.start;
			int end = block.end;
			List<ImmediateInstruction> instructionList = model.getImmediateInstructionList();
			performLiveVariableAnalysis(start, end);
			for (int i = start; i <= end; i++) {
				assemblyCodes.add("-----------------");
				assemblyCodes.add("instruction: " + instructionList.get(i).toString());
				generateCode(instructionList.get(i));
				assemblyCodes.add(rt.toString());
			}
			updateLiveVariable();
		}
		for (String line: assemblyCodes) {
			System.out.println(line);
		}
	}
	
	private void performLiveVariableAnalysis(int start, int end) {
		// local variables
		Map<String, VariableSymbol> symbolTable = model.getSymbolTable();
		List<ImmediateInstruction> instructionList = model.getImmediateInstructionList();
		
		// initialize next use field in ST for all variables
		for (String key: symbolTable.keySet()) {
			VariableSymbol symbol = symbolTable.get(key);
			if (symbol.isTempVariable()) {
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
		String operator = immediateInstruction.getOperator();
		if (operator.equals("=")) {
			generateAssignmentCode(immediateInstruction);
		} else if ("+-*/%|&!neg".contains(operator)) {
			generateArithmeticAndLogicalCode(immediateInstruction);
		} else if ("==>=<=".contains(operator)) {
			generateRelationalCode(immediateInstruction);
		} else if (operator.equals("jump")) {
			generateJumpCode(immediateInstruction);
		} else if (operator.equals("jeqz")) {
			generateJeqzCode(immediateInstruction);
		}
	}
	
	private void updateSTandRTforResult(String result, int reg) {
		int oldIndex = model.getSymbolTable().get(result).getMemLoc();
		if (oldIndex != reg && oldIndex >= 0) {
			rt.getRegList()[oldIndex].remove(result);
		}
		model.getSymbolTable().get(result).setMemLoc(reg);
		rt.getRegList()[reg].add(result);
	}
	
	private void generateAssignmentCode(ImmediateInstruction immediateInstruction) {
		String left = immediateInstruction.getLeft();
		int leftNextUse = immediateInstruction.getLeftNextUse();
		String result = immediateInstruction.getResult();
		
		// if 'left' is not in a reg
		int leftReg = rt.where(left);
		int reg = -1;
		if (leftReg < 0) {
			reg = rt.getReg(left, leftNextUse);
			assemblyCodes.add(String.format("%s %s, D%d", "move.l", numberOrVariable(left), reg));
		} else {
			reg = model.getSymbolTable().get(left).getMemLoc();
		}
		
		// if ('left' is not live) and ('left' is in a reg)
		if (leftNextUse < 0 && leftReg >= 0) {
			rt.getRegList()[leftReg].remove(left);
			model.getSymbolTable().get(left).setMemLoc(reg);
		}
		
		updateSTandRTforResult(result, reg);
	}

	private String numberOrVariable(String left) {
		return isNumber(left) ? "#" + left : left;
	}

	private boolean isNumber(String left) {
		try {
			Integer.parseInt(left);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void generateArithmeticAndLogicalCode(ImmediateInstruction immediateInstruction) {
		String operator = immediateInstruction.getOperator();
		String left = immediateInstruction.getLeft();
		int leftNextUse = immediateInstruction.getLeftNextUse();
		String right = immediateInstruction.getRight();
		int rightNextUse = immediateInstruction.getRightNextUse();
		String result = immediateInstruction.getResult();
		
		// if 'left' is not in REG ...
		int reg = rt.getReg(left, leftNextUse);
		int leftReg = rt.where(left);
		if (!rt.isInReg(left, reg)) {
			if (leftReg >= 0) {
				assemblyCodes.add(String.format("%s D%d, D%d", "move.l", leftReg, reg));
			} else {
				assemblyCodes.add(String.format("%s %s, D%d", "move.l", numberOrVariable(left), reg));
			}
		}
		
		// if 'left' = 'right'
		int rightReg = rt.where(right);
		String leftPart = "";
		String rightPart = "";
		if (left.equals(right)) {
			leftPart = String.format("D%d", left); 
			rightPart = String.format("D%d", right);
		} else {
			if (rightReg >= 0) {
				leftPart = String.format("D%d", rightReg); 
				rightPart = String.format("D%d", reg);
			} else {
				leftPart = String.format("%s", right); 
				rightPart = String.format("D%d", reg);
			}			
		}
		
		if ("neg!".contains(operator)) {
			assemblyCodes.add(String.format("%s %s", transformOperation(operator), rightPart));
		} else {
			assemblyCodes.add(String.format("%s %s, %s", transformOperation(operator), 
					numberOrVariable(leftPart), rightPart));
			if (operator.equals("/")) {
				assemblyCodes.add(String.format("%s %s, %s", "and", "#$00FF", rightPart));
			} else if (operator.equals("%")) {
				assemblyCodes.add(String.format("%s %s, %s", "slr", "#16", rightPart));
			}
		}
		
		// if ('left' is not live) and ('left' is in a reg)
		if (leftNextUse < 0 && leftReg >= 0) {
			rt.getRegList()[leftReg].remove(left);
			model.getSymbolTable().get(left).setMemLoc(reg);
		}
		
		// if ('right' is not live) and ('right' is in a reg)
		if (rightNextUse < 0 && rightReg >= 0) {
			rt.getRegList()[rightReg].remove(right);
			model.getSymbolTable().get(right).setMemLoc(reg);
		}
		
		updateSTandRTforResult(result, reg);
	}	

	private String transformOperation(String operator) {
		switch(operator) {
		case "+": return "add";
		case "-": return "sub";
		case "*": return "mul";
		case "/": return "div";
		case "%": return "div";
		case "&": return "and";
		case "|": return "or";
		case "==": return "eq";
		case "<": return "lt";
		case "<=": return "lt";
		case ">": return "gt";
		case ">=": return "ge";
		}
		return operator;
	}

	private void generateRelationalCode(ImmediateInstruction immediateInstruction) {
		String operator = immediateInstruction.getOperator();
		String left = immediateInstruction.getLeft();
		int leftNextUse = immediateInstruction.getLeftNextUse();
		String right = immediateInstruction.getRight();
		int rightNextUse = immediateInstruction.getRightNextUse();
		String result = immediateInstruction.getResult();
		
		// if 'left' is not in REG ...
		int reg = rt.getReg(left, leftNextUse);
		int leftReg = rt.where(left);
		if (!rt.isInReg(left, reg)) {
			if (leftReg >= 0) {
				assemblyCodes.add(String.format("%s D%d, D%d", "move.l", leftReg, reg));
			} else {
				assemblyCodes.add(String.format("%s %s, D%d", "move.l", left, reg));
			}
		}
		
		// if 'right' is in a reg(r) ...
		int rightReg = rt.where(right);
		if (rightReg >= 0) {
			assemblyCodes.add(String.format("%s D%d, D%d", "cmp.l", rightReg, reg));
		} else {
			assemblyCodes.add(String.format("%s %s, D%d", "cmp.l", right, reg));
		}
		
		// generate ...
		assemblyCodes.add(String.format("b%s %d", transformOperation(operator), assemblyCodes.size() * 4 + 8));
		assemblyCodes.add(String.format("%s %d", "clr.l", reg));
		assemblyCodes.add(String.format("%s %d", "bra", assemblyCodes.size() * 4 + 4));
		assemblyCodes.add(String.format("%s #1, D%d", "move.l", rightReg, reg));
		
		// if ('left' is not live) and ('left' is in a reg)
		if (leftNextUse < 0 && leftReg >= 0) {
			rt.getRegList()[leftReg].remove(left);
			model.getSymbolTable().get(left).setMemLoc(reg);
		}
		
		// if ('right' is not live) and ('right' is in a reg)
		if (rightNextUse < 0 && rightReg >= 0) {
			rt.getRegList()[rightReg].remove(right);
			model.getSymbolTable().get(right).setMemLoc(reg);
		}
		
		updateSTandRTforResult(result, reg);
		
	}

	private void generateJumpCode(ImmediateInstruction immediateInstruction) {
		String result = immediateInstruction.getResult();
		
		// generate store instructions for vars that are live & not in memory
		updateLiveVariable();
		
		int nextInstruction = Integer.parseInt(result);
		assemblyCodes.add(String.format("%s #%d", "bra", 
				model.getImmediateInstructionList().get(nextInstruction).getStartAddr()));
	}

	private void generateJeqzCode(ImmediateInstruction immediateInstruction) {
		String left = immediateInstruction.getLeft();
		int leftNextUse = immediateInstruction.getLeftNextUse();
		String result = immediateInstruction.getResult();
		
		// if 'left' is not in a reg
		int leftReg = rt.where(left);
		int reg = rt.getReg(left, leftNextUse);
		if (leftReg < 0) {
			assemblyCodes.add(String.format("%s %s, D%d", "move.l", left, reg));
		}
		
		// generate store instructions for vars that are live & not in memory
		updateLiveVariable();
		
		// generate ...
		assemblyCodes.add(String.format("%s D%d", "tst", reg));
		int nextInstruction = Integer.parseInt(result);
		assemblyCodes.add(String.format("%s #%d", "beq", 
				model.getImmediateInstructionList().get(nextInstruction).getStartAddr()));
		
	}

	private void updateLiveVariable() {
		// generate store instructions for vars that are live & not in memory
		Set<String>[] regList = rt.getRegList();
		for (int i = 0; i < regList.length; i++) {
			for (String name: regList[i]) {
				if (!model.getSymbolTable().get(name).isTempVariable()) {
					assemblyCodes.add(String.format("%s D%d, %s", "move.l", i, name));
				}
			}
			regList[i] = new HashSet<String>();
		}
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



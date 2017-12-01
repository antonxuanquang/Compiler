import java.util.EmptyStackException;
import java.util.Random;

public class IIG {
	
	private static String generateRandomTemp() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        salt.append("T");
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
	}
	
	public static void unary(Model model, String operator) {
		Symbol left = model.getSymbolStack().pop();
		String tempId = generateRandomTemp();
		VariableSymbol temp = new VariableSymbol(tempId);
		model.getSymbolTable().put(tempId, temp);
		model.generateImmediateInstruction(operator, left.getId(), "", temp.getId());
		model.getSymbolStack().push(temp);
	}
	
	public static void binary(Model model, String operator) {
		Symbol right = model.getSymbolStack().pop();
		Symbol left = model.getSymbolStack().pop();
		String tempId = generateRandomTemp();
		VariableSymbol temp = new VariableSymbol(tempId);
		model.getSymbolTable().put(tempId, temp);
		model.generateImmediateInstruction(operator, left.getId(), right.getId(), temp.getId());
		model.getSymbolStack().push(temp);
	}
	
	public static void binary(Model model, Token token) {
		String operator = token.image;
		binary(model, operator);
	}
	
	public static void ternary1(Model model) {
		int nextInstructionCounter = model.getNextImmediateInstructionCounter();
		Symbol left = model.getSymbolStack().pop();
		model.generateImmediateInstruction("jeqz", left.getId(), "", "-1");
		model.getInstructionCounterStack().push(nextInstructionCounter);
	}
	
	public static void ternary2(Model model) {
		String tempId = generateRandomTemp();
		VariableSymbol temp = new VariableSymbol(tempId);
		model.getSymbolTable().put(tempId, temp);
		model.generateImmediateInstruction("=", model.getSymbolStack().pop().getId(), "", temp.getId());
		int nextInstructionCounter = model.getNextImmediateInstructionCounter();
		model.generateImmediateInstruction("jump", "", "", "-1");
		int topCounter = model.getInstructionCounterStack().pop();
		model.getImmediateInstructionList().get(topCounter)
			.setResult("" + model.getNextImmediateInstructionCounter());
		model.getInstructionCounterStack().push(nextInstructionCounter);
		model.getSymbolStack().push(temp);
	}
	
	public static void ternary3(Model model) {
		Symbol left = model.getSymbolStack().pop();
		Symbol temp = model.getSymbolStack().pop();
		model.generateImmediateInstruction("=", left.getId(), "", temp.getId());
		int topCounter = model.getInstructionCounterStack().pop();
		model.getImmediateInstructionList().get(topCounter)
			.setResult("" + model.getNextImmediateInstructionCounter());
		model.getSymbolStack().push(temp);
	}
	
	public static void pushID(Model model, Token token) {
		String id = token.image;
		VariableSymbol symbol = model.getSymbolTable().getOrDefault(id, new VariableSymbol(id));
		model.getSymbolStack().push(symbol);
		model.getSymbolTable().put(token.image, symbol);
	}
	
	public static void pushInt(Model model, Token token) {
		model.getSymbolStack().push(new ConstantSymbol(token.image));
	}
	
	public static void expression1(Model model, Token token) {
		String id = token.image;
		Symbol symbol = model.getSymbolTable().getOrDefault(id, new VariableSymbol(id));
		model.getSymbolStack().push(symbol);
		model.getSymbolTable().put(id, (VariableSymbol) symbol);
	}
	
	public static void expression2(Model model) {
//		System.out.println(model);
		Symbol leftOp = model.getSymbolStack().pop();
		Symbol result = model.getSymbolStack().pop();
		model.generateImmediateInstruction("=", leftOp.getId(), "", result.getId());
		model.getSymbolStack().push(result);
	}
	
	public static void reverseExpression(Model model) {
		model.getSymbolStack().pop();
	}
	
	public static void returnStatement(Model model) {
		model.generateImmediateInstruction("rjs", "", "", "");
	}
	
	public static void if1(Model model) {
		int nextInstructionCounter = model.getNextImmediateInstructionCounter();
		Symbol left = model.getSymbolStack().pop();
		model.generateImmediateInstruction("jeqz", left.getId(), "", "-1");
		model.getInstructionCounterStack().push(nextInstructionCounter);
	}
	
	public static void if2(Model model) {
		int nextInstructionCounter = model.getNextImmediateInstructionCounter();
		model.generateImmediateInstruction("jump", "", "", "-1");
		int topCounter = model.getInstructionCounterStack().pop();
		model.getImmediateInstructionList().get(topCounter)
			.setResult("" + model.getNextImmediateInstructionCounter());
		model.getInstructionCounterStack().push(nextInstructionCounter);
	}
	
	public static void if3(Model model) {
		int topCounter = model.getInstructionCounterStack().pop();
		model.getImmediateInstructionList().get(topCounter)
			.setResult("" + model.getNextImmediateInstructionCounter());
	}
	
	public static void while1(Model model) {
		model.getWhileStack().push(model.getNextImmediateInstructionCounter());
	}
	
	public static void while2(Model model) {
		Symbol left = model.getSymbolStack().pop();
		model.getWhileStack().push(model.getNextImmediateInstructionCounter());
		model.generateImmediateInstruction("jeqz", left.getId(), "", "-1");		
	}
	
	public static void while3(Model model) {
		int a = model.getWhileStack().pop();
		int b = model.getWhileStack().pop();
		model.generateImmediateInstruction("jump", "", "", "" + b);
		backPatch(model, a, model.getNextImmediateInstructionCounter());
	}
	
	public static void breakAction(Model model) throws ParseException {
		if (model.getWhileStack().isEmpty()) 
			throw new ParseException("Attempt to use continue outside while loop");
		int a = model.getWhileStack().pop();
		model.getWhileStack().push(model.getNextImmediateInstructionCounter());
		model.generateImmediateInstruction("jump", "", "", "" + a);
	}
	
	public static void continueAction(Model model) throws ParseException {
		if (model.getWhileStack().isEmpty()) 
			throw new ParseException("Attempt to use break outside while loop");
		int a = model.getWhileStack().pop();
		if (model.getWhileStack().isEmpty()) 
			throw new ParseException("Attempt to use break outside while loop");
		int b = model.getWhileStack().pop();
		model.getWhileStack().push(b);
		model.getWhileStack().push(a);
		model.generateImmediateInstruction("jump", "", "", "" + b);
	}
	
	private static void backPatch(Model model, int index, int nextImmediateInstructionCounter) {
		int nextCounter = index;
		while (nextCounter != -1) {
			ImmediateInstruction instruction =  model.getImmediateInstructionList().get(nextCounter);
			nextCounter = Integer.parseInt(instruction.getResult());
			instruction.setResult("" + nextImmediateInstructionCounter);
			
		}
	}
}

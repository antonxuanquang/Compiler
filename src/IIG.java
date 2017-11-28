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
	
//	public static void add(Model model, String operator) {
//		Symbol right = model.getSymbolStack().pop();
//		Symbol left = model.getSymbolStack().pop();
//		String tempId = generateRandomTemp();
//		VariableSymbol temp = new VariableSymbol(tempId);
//		model.getSymbolTable().put(tempId, temp);
//		model.generateImmediateInstruction(operator, left, right, temp);
//		model.getSymbolStack().push(temp);
//	}
//	
//	public static void multiply(Model model, String operator) {
//		Symbol right = model.getSymbolStack().pop();
//		Symbol left = model.getSymbolStack().pop();
//		String tempId = generateRandomTemp();
//		VariableSymbol temp = new VariableSymbol(tempId);
//		model.getSymbolTable().put(tempId, temp);
//		model.generateImmediateInstruction(operator, left, right, temp);
//		model.getSymbolStack().push(temp);
//	}
	
	public static void binary(Model model, String operator) {
		Symbol right = model.getSymbolStack().pop();
		Symbol left = model.getSymbolStack().pop();
		String tempId = generateRandomTemp();
		VariableSymbol temp = new VariableSymbol(tempId);
		model.getSymbolTable().put(tempId, temp);
		model.generateImmediateInstruction(operator, left, right, temp);
		model.getSymbolStack().push(temp);
	}
	
	public static void binary(Model model, Token token) {
		String operator = token.image;
		binary(model, operator);
	}
	
	public static void pushID(Model model, Token token) {
		VariableSymbol symbol = new VariableSymbol(token.image);
		model.getSymbolStack().push(symbol);
		model.getSymbolTable().put(token.image, symbol);
	}
	
	public static void pushInt(Model model, Token token) {
		model.getSymbolStack().push(new ConstantSymbol(Integer.parseInt(token.image)));
	}
	
	public static void expression1(Model model, Token token) {
		String id = token.image;
		Symbol symbol = model.getSymbolTable().getOrDefault(id, new VariableSymbol(id));
		model.getSymbolStack().push(symbol);
		model.getSymbolTable().put(id, (VariableSymbol) symbol);
	}
	
	public static void expression2(Model model) {
		Symbol leftOp = model.getSymbolStack().pop();
		Symbol result = model.getSymbolStack().pop();
		model.generateImmediateInstruction("=", leftOp, null, result);
	}
	
	public static void if1(Model model) {
		int nextInstructionCounter = model.getNextImmediateInstructionCounter();
		Symbol left = model.getSymbolStack().pop();
		model.generateImmediateInstruction("jeqz", left, null, new ConstantSymbol(0));
		model.getInstructionCounterStack().push(nextInstructionCounter);
	}
	
	public static void if2(Model model) {
		int nextInstructionCounter = model.getNextImmediateInstructionCounter();
		model.generateImmediateInstruction("jump", null, null, new ConstantSymbol(0));
		int topCounter = model.getInstructionCounterStack().pop();
		ConstantSymbol symbol = (ConstantSymbol) model.getImmediateInstructionList().get(topCounter).getResult();
		symbol.setValue(model.getNextImmediateInstructionCounter());
		model.getInstructionCounterStack().push(nextInstructionCounter);
	}
	
	public static void if3(Model model) {
		int topCounter = model.getInstructionCounterStack().pop();
		ConstantSymbol symbol = (ConstantSymbol) model.getImmediateInstructionList().get(topCounter).getResult();
		symbol.setValue(model.getNextImmediateInstructionCounter());
	}
	
	public static void while1(Model model) {
		model.getWhileStack().push(model.getNextImmediateInstructionCounter());
	}
	
	public static void while2(Model model) {
		Symbol left = model.getSymbolStack().pop();
		model.getWhileStack().push(model.getNextImmediateInstructionCounter());
		model.generateImmediateInstruction("jeqz", left, null, new ConstantSymbol(0));		
	}
	
	public static void while3(Model model) {
		int a = model.getWhileStack().pop();
		int b = model.getWhileStack().pop();
		model.generateImmediateInstruction("jump", null, null, new ConstantSymbol(b));
		backPatch(model, a, model.getNextImmediateInstructionCounter());
	}
	
	public static void continueAction(Model model) throws Exception {
		if (model.getWhileStack().isEmpty()) 
			throw new Exception("Attempt to use break outside while loop");
		int a = model.getWhileStack().pop();
		model.getWhileStack().push(model.getNextImmediateInstructionCounter());
		model.generateImmediateInstruction("jump", null, null, new ConstantSymbol(a));
	}
	
	public static void breakAction(Model model) throws Exception {
		if (model.getWhileStack().isEmpty()) 
			throw new Exception("Attempt to use break outside while loop");
		int a = model.getWhileStack().pop();
		if (model.getWhileStack().isEmpty()) 
			throw new Exception("Attempt to use break outside while loop");
		int b = model.getWhileStack().pop();
		model.getWhileStack().push(b);
		model.getWhileStack().push(a);
		model.generateImmediateInstruction("jump", null, null, new ConstantSymbol(b));
	}
	
	private static void backPatch(Model model, int a, int nextImmediateInstructionCounter) {
		// TODO Auto-generated method stub
		
	}

}

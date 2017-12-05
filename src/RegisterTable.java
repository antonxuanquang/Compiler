import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RegisterTable {
	
	private Set<String> [] regList;
	private Model model;
	private List<String> assemblyCodes;
	private final int NUM_REG = 8;
	
	public RegisterTable(Model model, List<String> assemblyCodes) {
		this.model = model;
		this.assemblyCodes = assemblyCodes;
		regList = new HashSet[NUM_REG];
		for (int i = 0; i < NUM_REG; i++) {
			regList[i] = new HashSet<String>();
		}
	}
	
	public Set<String>[]  getRegList() {
		return regList;
	}
	
	public int getReg(String id, int nextUse) {
		for (int i = 1; i < NUM_REG; i++) {
			if (regList[i].contains(id) && regList[i].size() == 1 && nextUse < 0) {
				return i;
			}
		}
		
		for (int i = 1; i < NUM_REG; i++) {
			if(regList[i].isEmpty()) {
				return i;
			}
		}
		
		Random random = new Random();
		int reg = random.nextInt(NUM_REG - 1) + 1;
		
		Set<String> names = regList[reg];
		for (String name: names) {
			// generate store instructions for all names in the reg
			assemblyCodes.add(String.format("%s D%d, %s", "move.l", reg, name));
			
			// update ST locations for those names above
			model.getSymbolTable().get(name).setMemLoc(Integer.MAX_VALUE);
		}
		
		// update RT for that reg
		regList[reg] = new HashSet<String>();
		regList[reg].add(id);
		
		return reg;
	}
	
	public int where(String id) {
		int index = -1;
		for (int i = 0; i < NUM_REG; i++) {
			if (regList[i].contains(id)) return i;
		}
		return index;
	}
	
	public boolean isInReg(String id, int index) {
		return regList[index].contains(id);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("**************Reg Table*****************\n");
		for (int i = 0; i < NUM_REG; i++) {
			sb.append(String.format("%d: %s\n", i, regList[i].toString()));
		}
		return sb.toString();
	}

}

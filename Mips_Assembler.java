import java.util.HashMap;

public class Mips_Assembler {
    private static HashMap<String, Integer> opcodeMap = new HashMap<>();
    private static HashMap<String, Integer> functMap = new HashMap<>();
    private static HashMap<String, Integer> registerMap = new HashMap<>();

    static {
        initializeMaps();
    }

    private static void initializeMaps() {
        //opcode mappins
        opcodeMap.put("add", 0);
        opcodeMap.put("addiu", 9);
        opcodeMap.put("lw", 35);
        opcodeMap.put("sw", 43);
        opcodeMap.put("beq", 4);
        opcodeMap.put("j", 2);




        // function mapping for R type instruction
        functMap.put("add", 32);
        functMap.put("sub", 34);
        functMap.put("and", 36);
        functMap.put("or", 37);
        functMap.put("slt", 42);

        //Register mappings
        String[] register = {};


    }
}

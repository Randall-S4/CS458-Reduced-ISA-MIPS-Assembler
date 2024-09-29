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

   String[] registers = {"zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
                "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"};
        for (int i = 0; i < registers.length; i++) {
            registerMap.put("$" + registers[i], i);
        }
    
}

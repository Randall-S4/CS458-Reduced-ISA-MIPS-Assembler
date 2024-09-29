import java.util.*;

public class MIPSAssembler {
    private static final Map<String, Integer> registerMap = new HashMap<>();
    private static final Map<String, Integer> opcodeMap = new HashMap<>();
    private static final Map<String, Integer> functionMap = new HashMap<>();

    static {
        // Initialize register mappings
        String[] registers = {"zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
                              "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                              "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                              "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"};
        for (int i = 0; i < registers.length; i++) {
            registerMap.put("$" + registers[i], i);
            registerMap.put("$" + i, i);
        }

        // Initialize opcode mappings
        opcodeMap.put("add", 0);
        opcodeMap.put("addiu", 9);
        opcodeMap.put("and", 0);
        opcodeMap.put("andi", 12);
        opcodeMap.put("beq", 4);
        opcodeMap.put("bne", 5);
        opcodeMap.put("j", 2);
        opcodeMap.put("lui", 15);
        opcodeMap.put("lw", 35);
        opcodeMap.put("or", 0);
        opcodeMap.put("ori", 13);
        opcodeMap.put("slt", 0);
        opcodeMap.put("sub", 0);
        opcodeMap.put("sw", 43);
        opcodeMap.put("syscall", 0);

        // Initialize function mappings for R-type instructions
        functionMap.put("add", 32);
        functionMap.put("and", 36);
        functionMap.put("or", 37);
        functionMap.put("slt", 42);
        functionMap.put("sub", 34);
        functionMap.put("syscall", 12);
    }

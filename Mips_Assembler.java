import java.util.*;

public class MIPSAssembler {

    // Mappings for registers, opcodes, and functions
    private static final Map<String, Integer> registerMap = new HashMap<>();
    private static final Map<String, Integer> opcodeMap = new HashMap<>();
    private static final Map<String, Integer> functionMap = new HashMap<>();

    static {
        // Register mappings
        String[] registers = {"zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
                "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"};
        for (int i = 0; i < registers.length; i++) {
            registerMap.put("$" + registers[i], i);
            registerMap.put("$" + i, i);
        }

        // Opcode mappings
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

        // Function mappings for R-type instructions
        functionMap.put("add", 32);
        functionMap.put("and", 36);
        functionMap.put("or", 37);
        functionMap.put("slt", 42);
        functionMap.put("sub", 34);
        functionMap.put("syscall", 12);
    }

    // Entry point: assemble instruction from args
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java MIPSAssembler <MIPS instruction>");
            return;
        }
  String instruction = String.join(" ", args).split("#")[0].trim(); // This ensures only the instruction (without comments) is processed, ignoring everything after "#" 
        int machineCode = assembleLine(instruction);
        System.out.printf("%08x%n", machineCode);
    }
 // Assemble line into machine code
    private static int assembleLine(String line) {
        String[] parts = line.split("[,\\s]+");
        String opcode = parts[0].toLowerCase();
        // R-type instructions
        if (functionMap.containsKey(opcode)) {
            return assembleRType(opcode, parts);
        }
        // Invalid instruction
        else {
            throw new IllegalArgumentException("Invalid instruction: " + line);
        }


   
    }

    private static int assembleRType(String opcode, String[] parts) {

    }


    }



}

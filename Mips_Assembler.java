package defaul;

import java.util.HashMap;
import java.util.Map;

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

        String instruction = String.join(" ", args).split("#")[0].trim(); // Remove comments
        int machineCode = assembleLine(instruction);
        System.out.printf("%08x%n", machineCode);
    }

    // Assemble line into machine code
    private static int assembleLine(String line) {
        String[] parts = line.split("[,\\s]+");
        String opcode = parts[0].toLowerCase();

        if (functionMap.containsKey(opcode)) {
            return assembleR(parts);
        } else if (opcodeMap.containsKey(opcode)) {
            if (opcode.equals("addiu") || opcode.equals("andi") || opcode.equals("ori")) {
                return assembleIImmediate(parts);
            } else if (opcode.equals("beq") || opcode.equals("bne")) {
                return assembleIBranch(parts);
            } else if (opcode.equals("lui")) {
                return assembleILui(parts);
            } else if (opcode.equals("lw") || opcode.equals("sw")) {
                return assembleIMemory(parts);
            } else if (opcode.equals("j")) {
                return assembleJ(parts);
            } else if (opcode.equals("syscall")) {
                return 12; // Fixed machine code for syscall
            }
        }
        System.out.println("Unknown instruction: " + opcode);
        return 0;
    }

    // R-type assembly
    private static int assembleR(String[] parts) {
        int rs = getRegister(parts[2]);
        int rt = getRegister(parts[3]);
        int rd = getRegister(parts[1]);
        int shamt = 0;
        int funct = functionMap.get(parts[0]);

        return (0 << 26) | (rs << 21) | (rt << 16) | (rd << 11) | (shamt << 6) | funct; // Build 32-bit machine code for R-type 
    }

    // I-type assembly for immediate operations
    private static int assembleIImmediate(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int rs = getRegister(parts[2]);
        int rt = getRegister(parts[1]);
        int imm = parseImmediate(parts[3]);

        return (opcode << 26) | (rs << 21) | (rt << 16) | (imm & 0xFFFF);
    }

    // I-type assembly for branch operations
    private static int assembleIBranch(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int rs = getRegister(parts[1]);
        int rt = getRegister(parts[2]);
        int imm = parseImmediate(parts[3]);

        return (opcode << 26) | (rs << 21) | (rt << 16) | (imm & 0xFFFF);
    }

    // I-type assembly for LUI
    private static int assembleILui(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int rt = getRegister(parts[1]);
        int imm = parseImmediate(parts[2]);

        return (opcode << 26) | (rt << 16) | (imm & 0xFFFF);
    }

    // I-type assembly for memory operations
    private static int assembleIMemory(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int rt = getRegister(parts[1]);
        String[] offsetBase = parts[2].split("\\(|\\)");
        int imm = parseImmediate(offsetBase[0]);
        int rs = getRegister(offsetBase[1]);

        return (opcode << 26) | (rs << 21) | (rt << 16) | (imm & 0xFFFF);
    }

    // J-type assembly
    private static int assembleJ(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int address = parseImmediate(parts[1]);

        return (opcode << 26) | (address & 0x3FFFFFF);
    }

    // helper to Get register number
    private static int getRegister(String reg) {
        return registerMap.getOrDefault(reg, 0);
    }

    // Helper to Parse immediate values
    private static int parseImmediate(String imm) {
        if (imm.startsWith("0x")) {
            return Integer.parseInt(imm.substring(2), 16);
        } else {
            return Integer.parseInt(imm);
        }
    }
}

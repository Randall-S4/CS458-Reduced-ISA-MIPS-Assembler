

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MIPSAssembler {
	  // Existing mappings from Milestone 1
    private static final Map<String, Integer> registerMap = new HashMap<>();
    private static final Map<String, Integer> opcodeMap = new HashMap<>();
    private static final Map<String, Integer> functionMap = new HashMap<>();
    
    // New mappings for labels and data
    private static final Map<String, Integer> labelAddresses = new HashMap<>();
    private static final Map<String, Integer> dataAddresses = new HashMap<>();

    private static final List<Byte> dataBytes = new ArrayList<>();
    private static final int TEXT_START = 0x00400000;
    private static final int DATA_START = 0x10010000;
    
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

        // Function mappings
        functionMap.put("add", 32);
        functionMap.put("and", 36);
        functionMap.put("or", 37);
        functionMap.put("slt", 42);
        functionMap.put("sub", 34);
        functionMap.put("syscall", 12);
    }

    public static void main(String[] args) {
  if (args.length != 1) {
    	        System.out.println("Usage: java MIPSAssembler <input.asm>");
    	        return;
    	    }

    	    try {
    	        String inputFile = args[0];
    	        System.out.println("Reading input file: " + inputFile);

    	        List<String> lines = Files.readAllLines(Paths.get(inputFile));
    	        processFile(inputFile, lines);
    	        
    	        System.out.println("Assembler completed successfully.");
    	    } catch (IOException e) {
    	        System.err.println("Error reading file: " + e.getMessage());
    	    }
    }

    private static void processFile(String inputFile, List<String> lines) {
        // Split into sections
        List<String> dataSection = new ArrayList<>();
        List<String> textSection = new ArrayList<>();
        boolean inDataSection = false;
        boolean inTextSection = false;

        for (String line : lines) {
            line = line.split("#")[0].trim(); // Remove comments
            if (line.isEmpty()) continue;

            if (line.equals(".data")) {
                inDataSection = true;
                inTextSection = false;
            } else if (line.equals(".text")) {
                inDataSection = false;
                inTextSection = true;
            } else if (inDataSection) {
                dataSection.add(line);
            } else if (inTextSection) {
                textSection.add(line);
            }
        }

        // Process sections
        processDataSection(dataSection);
        List<Integer> machineCode = processTextSection(textSection);

        // Write output files
        String baseName = inputFile.substring(0, inputFile.lastIndexOf('.'));
        writeDataFile(baseName + ".data");
        writeTextFile(baseName + ".text", machineCode);
    }

    private static void processDataSection(List<String> dataSection) {
        int currentAddress = DATA_START;

        for (String line : dataSection) {
            if (line.contains(":")) {
                String[] parts = line.split(":",2);
                String label = parts[0].trim();
                dataAddresses.put(label, currentAddress);
                if (parts.length > 1) {
                    String[] declaration = parts[1].trim().split("\\s+", 2);
                    if (declaration[0].equals(".asciiz")) {
                        // Extract string between quotes
                        String str = declaration[1].trim();
                        str = str.substring(1, str.length() - 1); // Remove quotes
                        currentAddress += str.length() + 1; // +1 for null terminator

                        byte [] asciiBytes = str.getBytes();
                        int i = 0;

                        for(byte b : asciiBytes){
                            System.out.println(STR."\{i} \{(char)b}");
                            int index = 3 - (i % 4);
                            ++i;
                            dataBytes.add(b);
                        }
                        dataBytes.add((byte)0);
                    }
                }
            }
        }
    }

    private static List<Integer> processTextSection(List<String> textSection) {
        // First pass: collect label addresses
        int currentAddress = TEXT_START;
        for (String line : textSection) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.endsWith(":")) {
                String label = line.substring(0, line.length() - 1).trim();
                labelAddresses.put(label, currentAddress);
            } else if (!line.contains(":")) {
                // Count instructions for pseudo-instructions
                String[] parts = line.split("\\s+");
                String instruction = parts[0].toLowerCase();
                if (isPseudoInstruction(instruction)) {
                    currentAddress += getPseudoInstructionSize(instruction) * 4;
                } else {
                    currentAddress += 4;
                }
            }
        }

        // Second pass: generate machine code
        List<Integer> machineCode = new ArrayList<>();
        currentAddress = TEXT_START;
        
        for (String line : textSection) {
            line = line.trim();
            if (line.isEmpty() || line.endsWith(":")) continue;

            if (line.contains(":")) {
                line = line.split(":", 2)[1].trim();
            }

            String[] parts = line.split("\\s+");
            String instruction = parts[0].toLowerCase();
            
            if (isPseudoInstruction(instruction)) {
                machineCode.addAll(assemblePseudoInstruction(line, currentAddress));
                currentAddress += getPseudoInstructionSize(instruction) * 4;
            } else {
                machineCode.add(assembleLine(line, currentAddress));
                currentAddress += 4;
            }
        }

        return machineCode;
    }

    private static boolean isPseudoInstruction(String instruction) {
        return instruction.equals("li") || instruction.equals("la") || 
               instruction.equals("move") || instruction.equals("blt");
    }

    private static int getPseudoInstructionSize(String instruction) {
        switch (instruction) {
            case "li": return 2;  // lui + ori
            case "la": return 2;  // lui + ori
            case "move": return 1; // add
            case "blt": return 2;  // slt + bne
            default: return 1;
        }
    }

    private static List<Integer> assemblePseudoInstruction(String line, int currentAddress) {
        List<Integer> result = new ArrayList<>();
        String[] parts = line.split("[,\\s]+");
        String instruction = parts[0].toLowerCase();

        switch (instruction) {
            case "li":
                int value = parseImmediate(parts[2]);
                int upper = (value >> 16) & 0xFFFF;
                int lower = value & 0xFFFF;
                if (upper != 0) {
                    result.add(assembleILui(new String[]{"lui", parts[1], String.valueOf(upper)}));
                }
                result.add(assembleIImmediate(new String[]{"ori", parts[1], parts[1], String.valueOf(lower)}));
                break;

            case "la":
                int address = dataAddresses.get(parts[2]);
                upper = (address >> 16) & 0xFFFF;
                lower = address & 0xFFFF;
                result.add(assembleILui(new String[]{"lui", "$at", String.valueOf(upper)}));
                result.add(assembleIImmediate(new String[]{"ori", parts[1], "$at", String.valueOf(lower)}));
                break;

            case "move":
                result.add(assembleR(new String[]{"add", parts[1], "$zero", parts[2]}));
                break;

            case "blt":
                String tempReg = "$at";
                result.add(assembleR(new String[]{"slt", tempReg, parts[1], parts[2]}));
                int offset = calculateBranchOffset(currentAddress + 4, labelAddresses.get(parts[3]));
                result.add(assembleIBranch(new String[]{"bne", tempReg, "$zero", String.valueOf(offset)}));
                break;
        }

        return result;
    }

    // Modified assembleLine to handle labels
    private static int assembleLine(String line, int currentAddress) {
        String[] parts = line.split("[,\\s]+");
        String opcode = parts[0].toLowerCase();

        if (opcode.equals("syscall")) {
            return 12;
        }

        if (functionMap.containsKey(opcode)) {
            return assembleR(parts);
        } else if (opcodeMap.containsKey(opcode)) {
            if (opcode.equals("beq") || opcode.equals("bne")) {
                // Calculate branch offset
                int targetAddress = labelAddresses.get(parts[3]);
                int offset = calculateBranchOffset(currentAddress + 4, targetAddress);
                parts[3] = String.valueOf(offset);
                return assembleIBranch(parts);
            } else if (opcode.equals("j")) {
                // Calculate jump address
                int targetAddress = labelAddresses.get(parts[1]);
                parts[1] = String.valueOf(targetAddress >> 2);
                return assembleJ(parts);
            } else if (opcode.equals("addiu") || opcode.equals("andi") || opcode.equals("ori")) {
                return assembleIImmediate(parts);
            } else if (opcode.equals("lui")) {
                return assembleILui(parts);
            } else if (opcode.equals("lw") || opcode.equals("sw")) {
                return assembleIMemory(parts);
            }
        }
        
        return 0;
    }

    private static int calculateBranchOffset(int fromAddress, int toAddress) {
        return ((toAddress - fromAddress) / 4);
    }

    // Existing assembly methods from Milestone 1
    private static int assembleR(String[] parts) {
        int rs = getRegister(parts[2]);
        int rt = getRegister(parts[3]);
        int rd = getRegister(parts[1]);
        int shamt = 0;
        int funct = functionMap.get(parts[0]);

        return (0 << 26) | (rs << 21) | (rt << 16) | (rd << 11) | (shamt << 6) | funct;
    }

    private static int assembleIImmediate(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int rs = getRegister(parts[2]);
        int rt = getRegister(parts[1]);
        int imm = parseImmediate(parts[3]);

        return (opcode << 26) | (rs << 21) | (rt << 16) | (imm & 0xFFFF);
    }

    private static int assembleIBranch(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int rs = getRegister(parts[1]);
        int rt = getRegister(parts[2]);
        int imm = parseImmediate(parts[3]);

        return (opcode << 26) | (rs << 21) | (rt << 16) | (imm & 0xFFFF);
    }

    private static int assembleILui(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int rt = getRegister(parts[1]);
        int imm = parseImmediate(parts[2]);

        return (opcode << 26) | (rt << 16) | (imm & 0xFFFF);
    }

    private static int assembleIMemory(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int rt = getRegister(parts[1]);

        String[] offsetBase = parts[2].split("\\(|\\)");
        int imm = offsetBase[0].isEmpty() ? 0 : parseImmediate(offsetBase[0]);
        int rs = getRegister(offsetBase[1]);

        return (opcode << 26) | (rs << 21) | (rt << 16) | (imm & 0xFFFF);
    }

    private static int assembleJ(String[] parts) {
        int opcode = opcodeMap.get(parts[0]);
        int address = parseImmediate(parts[1]);

        return (opcode << 26) | (address & 0x3FFFFFF);
    }

    private static int getRegister(String reg) {
        return registerMap.getOrDefault(reg, 0);
    }

    private static int parseImmediate(String imm) {
        try {
            if (imm.startsWith("0x")) {
                return Integer.parseInt(imm.substring(2), 16);
            } else {
                return Integer.parseInt(imm);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Write output files
    private static void writeDataFile(String filename) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            Byte [] dataLine = new Byte[4];
            int i = 0;
            for (byte b : dataBytes){
                int index = 3 - (i%4);
                dataLine[index] = b;
                if( index == 0 ){
                    String b0 = String.format("%02x",dataLine[0]);
                    String b1 = String.format("%02x",dataLine[1]);
                    String b2 = String.format("%02x",dataLine[2]);
                    String b3 = String.format("%02x%n",dataLine[3]);
                    String dataString = b0 + b1+ b2 +b3;
                    //Byte line = Byte.valueOf(dataString);
                    writer.printf(dataString);
                    dataLine = new Byte[4];
                }
                ++i;
            }

        } catch (IOException e) {
            System.err.println("Error writing data file: " + e.getMessage());
        }
    }

    private static void writeTextFile(String filename, List<Integer> machineCode) {
     	  try (PrintWriter writer = new PrintWriter(filename)) {
    	        for (int instruction : machineCode) {
    	            writer.printf("%08x%n", instruction);
    	        }
    	        System.out.println("Successfully wrote to: " + filename);  
    	    } catch (IOException e) {
    	        System.err.println("Error writing text file: " + e.getMessage());
    	    }
    }
}


import java.io.*;
import java.util.*;

public class MIPSAssembler {
    private static Map<String, Integer> dataLabels = new HashMap<>();
    private static Map<String, Integer> textLabels = new HashMap<>();
    private static final int DATA_START = 0x10010000;
    private static final int TEXT_START = 0x00400000;
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MipsAssembler <input.asm>");
            return;
        }

        String inputFile = args[0];
        if (!inputFile.endsWith(".asm")) {
            System.out.println("Input file must have .asm extension");
            return;
        }

        String baseName = inputFile.substring(0, inputFile.length() - 4);
        try {
            List<String> lines = readFile(inputFile);
            processFile(lines, baseName);
        } catch (IOException e) {
            System.out.println("Error processing file: " + e.getMessage());
        }
    }

    private static List<String> readFile(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.trim());
            }
        }
        return lines;
    }

    private static void processFile(List<String> lines, String baseName) throws IOException {
        List<String> dataSection = new ArrayList<>();
        List<String> textSection = new ArrayList<>();
        boolean inDataSection = false;
        boolean inTextSection = false;

        // First pass: Separate sections and collect labels
        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.equals(".data")) {
                inDataSection = true;
                inTextSection = false;
                continue;
            } else if (line.equals(".text")) {
                inDataSection = false;
                inTextSection = true;
                continue;
            }

            if (inDataSection) {
                dataSection.add(line);
            } else if (inTextSection) {
                textSection.add(line);
            }
        }

        // Process data section
        List<String> dataOutput = processDataSection(dataSection);
        
        // Process text section
        List<String> textOutput = processTextSection(textSection);

        // Write output files
        writeOutput(baseName + ".data", dataOutput);
        writeOutput(baseName + ".text", textOutput);
    }

    private static List<String> processDataSection(List<String> dataSection) {
        List<String> output = new ArrayList<>();
        int currentAddress = DATA_START;

        for (String line : dataSection) {
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split(":");
            if (parts.length != 2) continue;

            String label = parts[0].trim();
            String[] declaration = parts[1].trim().split("\\s+", 2);
            
            if (declaration.length < 2) continue;

            dataLabels.put(label, currentAddress);
            String type = declaration[0];
            String data = declaration[1].trim();

            if (type.equals(".asciiz")) {
                // Remove quotes and process string
                String str = data.substring(1, data.length() - 1);
                byte[] bytes = (str + "\0").getBytes();
                
                // Convert to little-endian words
                for (int i = 0; i < bytes.length; i += 4) {
                    int word = 0;
                    for (int j = 0; j < 4 && i + j < bytes.length; j++) {
                        word |= (bytes[i + j] & 0xFF) << (j * 8);
                    }
                    output.add(String.format("%08x", word));
                }
                
                currentAddress += ((bytes.length + 3) / 4) * 4;
            }
        }

        // Pad to match MARS output
        while (output.size() < 600) {
            output.add("00000000");
        }

        return output;
    }

    private static List<String> processTextSection(List<String> textSection) {
        List<String> output = new ArrayList<>();
        int currentAddress = TEXT_START;
        
        // First pass: collect text labels
        for (int i = 0; i < textSection.size(); i++) {
            String line = textSection.get(i);
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.contains(":")) {
                String label = line.substring(0, line.indexOf(":")).trim();
                textLabels.put(label, currentAddress);
                line = line.substring(line.indexOf(":") + 1).trim();
                if (line.isEmpty()) continue;
            }
            
            // Account for pseudo-instructions that expand to multiple instructions
            if (line.startsWith("li ")) currentAddress += 8;
            else if (line.startsWith("la ")) currentAddress += 8;
            else currentAddress += 4;
        }

        // Second pass: generate machine code
        currentAddress = TEXT_START;
        for (String line : textSection) {
            if (line.isEmpty() || line.startsWith("#")) continue;

            // Handle labels
            if (line.contains(":")) {
                line = line.substring(line.indexOf(":") + 1).trim();
                if (line.isEmpty()) continue;
            }

            // Process instruction
            String[] parts = line.split("\\s+");
            String opcode = parts[0];

            // Handle each instruction type
            int instruction = 0;
            switch (opcode) {
                case "li":
                    // Expand to lui + ori
                    int value = Integer.parseInt(parts[2]);
                    String reg = parts[1];
                    int upper = (value >> 16) & 0xFFFF;
                    int lower = value & 0xFFFF;
                    
                    if (upper != 0) {
                        instruction = 0x3C000000 | (getRegNumber(reg) << 16) | upper;
                        output.add(String.format("%08x", instruction));
                        currentAddress += 4;
                    }
                    
                    instruction = 0x34000000 | (getRegNumber(reg) << 16) | (getRegNumber(reg) << 21) | lower;
                    break;

                case "syscall":
                    instruction = 0x0000000C;
                    break;

                case "add":
                    String rd = parts[1].replace(",", "");
                    String rs = parts[2].replace(",", "");
                    String rt = parts[3];
                    instruction = (getRegNumber(rs) << 21) | (getRegNumber(rt) << 16) | 
                                (getRegNumber(rd) << 11) | 0x20;
                    break;

                // Add more instructions as needed
            }

            output.add(String.format("%08x", instruction));
            currentAddress += 4;
        }

        return output;
    }

    private static int getRegNumber(String reg) {
        reg = reg.replace("$", "").replace(",", "");
        switch (reg) {
            case "zero": return 0;
            case "at": return 1;
            case "v0": return 2;
            case "v1": return 3;
            case "a0": return 4;
            // Add more registers as needed
            default:
                if (reg.startsWith("t")) return 8 + Integer.parseInt(reg.substring(1));
                if (reg.startsWith("s")) return 16 + Integer.parseInt(reg.substring(1));
                return 0;
        }
    }

    private static void writeOutput(String filename, List<String> lines) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (String line : lines) {
                writer.println(line);
            }
        }
    }
}

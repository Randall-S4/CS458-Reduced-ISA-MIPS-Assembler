import java.io.*;

public class MIPSAssemblerTester {
public static void main(String[] args) {

    String asmFilePath = "EvenOrOdd.asm"; 
        String expectedTextFilePath = "EvenOrOdd.text";
        String expectedDataFilePath = "EvenOrOdd.data";

        try {
            // Step 1: Run the assembler on the ASM file
            System.out.println("Running MIPS Assembler...");
            MIPSAssembler.main(new String[]{asmFilePath});

            // Step 2: Validate the .text and .data files
            boolean textMatch = compareFiles("EvenOrOdd.text", expectedTextFilePath);
            boolean dataMatch = compareFiles("EvenOrOdd.data", expectedDataFilePath);

            // Step 3: Print results
            if (textMatch && dataMatch) {
                System.out.println("All tests passed! Output matches expected results.");
            } else {
                System.out.println("Tests failed:");
                if (!textMatch) System.out.println("Mismatch in .text section.");
                if (!dataMatch) System.out.println("Mismatch in .data section.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to compare two files line by line
    private static boolean compareFiles(String generatedFilePath, String expectedFilePath) throws IOException {
        List<String> generatedLines = Files.readAllLines(Paths.get(generatedFilePath));
        List<String> expectedLines = Files.readAllLines(Paths.get(expectedFilePath));

        if (generatedLines.size() != expectedLines.size()) {
            return false; // Files have different lengths
        }

        for (int i = 0; i < generatedLines.size(); i++) {
            if (!generatedLines.get(i).equals(expectedLines.get(i))) {
                System.out.println("Mismatch at line " + (i + 1));
                System.out.println("Generated: " + generatedLines.get(i));
                System.out.println("Expected:  " + expectedLines.get(i));
                return false;
            }
        }
        return true;
    }
}

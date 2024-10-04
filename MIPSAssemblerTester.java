import java.io.*;

public class MIPSAssemblerTester {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MIPSAssemblerTester <testfile>");
            return;
        }

        String testFile = args[0];

        try (BufferedReader br = new BufferedReader(new FileReader(testFile))) {
            String line;
            int lineNumber = 0;
            int passedTests = 0;
            int failedTests = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                // Ignore empty lines or lines starting with '#'
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                // Split the line into the expected machine code and the assembly instruction
                String[] parts = line.split(" ", 2);
                String expectedHex = parts[0].trim();
                String assemblyLine = parts[1].split("#")[0].trim();  // Ignore comments

                // Evaluate the assembly instruction using your assembler
                int evaluatedMachineCode = MIPSAssembler.assembleLine(assemblyLine);
                String evaluatedHex = String.format("%08x", evaluatedMachineCode);

                // Print the test result
                System.out.println("Testing line: " + assemblyLine);
                System.out.println("Evaluated output: " + evaluatedHex);
                System.out.println("Expected output:  " + expectedHex);

                // Compare expected with evaluated output
                if (evaluatedHex.equals(expectedHex)) {
                    System.out.println("Output is correct\n");
                    passedTests++;
                } else {
                    System.out.println("Output is incorrect\n");
                    failedTests++;
                }
            }

            // Summary of tests
            System.out.println("Total tests: " + (passedTests + failedTests));
            System.out.println("Passed tests: " + passedTests);
            System.out.println("Failed tests: " + failedTests);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
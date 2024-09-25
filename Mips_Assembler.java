import java.util.HashMap;

public class Mips_Assembler {
    private static HashMap<String, Integer> opcodeMap = new HashMap<>();
    private static HashMap<String, Integer> functMap = new HashMap<>();

    static {
        initializeMaps();
    }

    private static void initializeMaps() {
        opcodeMap.put("add", 0);
        functMap.put("add", 32);
        opcodeMap.put("addiu", 9);
    }
}

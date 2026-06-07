import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JackCompiler {
    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"C:\\Users\\joaoo\\Desktop\\nand2tetris\\nand2tetris\\projects\\11\\ComplexArrays"};
        }

        System.out.println("A tentar abrir o caminho: " + args[0]);
        File file = new File(args[0]);

        if (!file.exists()) {
            System.err.println("ERRO: O caminho especificado NÃO existe!");
            System.exit(1);
        }

        if (file.isDirectory()) {
            System.out.println("O caminho é uma pasta. A procurar ficheiros .jack...");
            File[] files = file.listFiles();
            int cont = 0;
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(".jack")) {
                        compileSingleFile(f);
                        cont++;
                    }
                }
            }
            System.out.println("Processo terminado. Ficheiros compilados nesta pasta: " + cont);
        } else if (file.isFile()) {
            if (!file.getName().endsWith(".jack")) {
                System.err.println("ERRO: O arquivo precisa terminar com .jack");
                System.exit(1);
            } else {
                compileSingleFile(file);
            }
        }
    }

    private static void compileSingleFile(File f) {
        String inputFileName = f.getAbsolutePath();
        int pos = inputFileName.lastIndexOf('.');
        String outputFileName = inputFileName.substring(0, pos) + ".vm";

        System.out.println("A COMPILAR: " + inputFileName);
        System.out.println("A GERAR VM EM: " + outputFileName);
        
        try {
            byte[] inputBytes = Files.readAllBytes(Paths.get(inputFileName));
            CompilationEngine engine = new CompilationEngine(inputBytes);
            engine.parse();
            
            String result = engine.getVMOutput();
            Files.write(Paths.get(outputFileName), result.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Erro de IO ao processar o arquivo " + inputFileName + ": " + e.getMessage());
        }
    }
}
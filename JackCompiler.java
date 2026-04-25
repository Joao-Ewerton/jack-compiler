import java.io.File;
import java.io.IOException;

public class JackCompiler {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java JackCompiler <arquivo_ou_diretorio>");
            return;
        }

        File fileOrDir = new File(args[0]);
        File[] files;

        if (fileOrDir.isFile() && fileOrDir.getName().endsWith(".jack")) {
            files = new File[]{fileOrDir};
        } else if (fileOrDir.isDirectory()) {
            files = fileOrDir.listFiles((dir, name) -> name.endsWith(".jack"));
        } else {
            System.out.println("Nenhum arquivo .jack encontrado.");
            return;
        }

        if (files != null) {
            for (File file : files) {
                try {
                    processFile(file);
                } catch (IOException e) {
                    System.out.println("Erro processando " + file.getName());
                }
            }
        }
    }

    private static void processFile(File inputFile) throws IOException {
        // Agora vamos gerar o arquivo sem o "T", apenas _Output.xml
        String outputFilename = inputFile.getAbsolutePath().replace(".jack", "_Output.xml");
        File outputFile = new File(outputFilename);
        
        JackTokenizer tokenizer = new JackTokenizer(inputFile);
        CompilationEngine engine = new CompilationEngine(tokenizer, outputFile);
        
        // A regra de ouro do Jack: todo arquivo é sempre uma classe.
        // Portanto, a compilação sempre começa pelo compileClass()
        engine.compileClass();
        
        System.out.println("Criado: " + outputFilename);
    }
}
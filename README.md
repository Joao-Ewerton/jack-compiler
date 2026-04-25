# Jack Compiler - Analisador Léxico e Sintático (Scanner & Parser)

Este repositório contém a implementação da **Fase 1 (Analisador Léxico)** e da **Fase 2 (Analisador Sintático)** de um compilador para a linguagem **Jack**. O objetivo deste projeto é ler o código-fonte `.jack`, remover comentários e espaços em branco, gerar os tokens correspondentes (Scanner) e, em seguida, agrupar esses tokens em uma Árvore Sintática XML estruturada (Parser) utilizando a técnica de *Recursive Descent Parsing* (Descida Recursiva).

## 🧑‍💻 Integrante
* **Nome:** João Victor Lima Ewerton
* **Matrícula:** 20250013640

## 🛠️ Tecnologias e Ambiente
* **Linguagem de Programação:** Java
* **Ferramentas:** JDK (Java Development Kit)
* **Objetivo:** Construção da lógica de tokenização e parsing do zero, manipulando expressões regulares, I/O de arquivos e árvores de derivação sem o uso de geradores automáticos.

## 🚀 Como Compilar e Executar

1. Abra o terminal na raiz do projeto e compile os arquivos Java:
   ```bash
   javac *.java
   ```

2. Execute o compilador passando um arquivo `.jack` ou um diretório contendo arquivos `.jack` como argumento:
   ```bash
   java JackCompiler <caminho_do_arquivo_ou_pasta>
   ```
   > *Isso processará o código e gerará o arquivo da árvore sintática com o sufixo `_Output.xml` na mesma pasta dos arquivos originais.*

## 🧪 Instruções para Rodar os Testes

A validação dos arquivos gerados é feita através da ferramenta oficial **TextComparer** do pacote Nand2Tetris.

1. Após rodar o compilador e gerar os arquivos de saída, abra o terminal e navegue até a pasta `tools` do nand2tetris.
2. Execute o script de comparação (no Windows via PowerShell, use `.\`):
   ```bash
   .\TextComparer.bat <caminho_do_seu_arquivo_gerado.xml> <caminho_do_gabarito.xml>
   ```
   
   *Exemplo prático para o Analisador Léxico (Tokens):*
   ```bash
   .\TextComparer.bat C:\projetos\10\Square\MainT_Output.xml C:\projetos\10\Square\MainT.xml
   ```

   *Exemplo prático para o Analisador Sintático (Árvore/Parser):*
   ```bash
   .\TextComparer.bat C:\projetos\10\Square\Main_Output.xml C:\projetos\10\Square\Main.xml
   ```

3. Se a compilação e a estrutura estiverem perfeitas, o terminal retornará a mensagem: **`Comparison ended successfully.`**

---
*Projeto desenvolvido para a disciplina de Compiladores do curso de Engenharia da Computação pela Universidade Federal do Maranhão - UFMA.*
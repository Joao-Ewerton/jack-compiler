# Jack Compiler - Analisador Léxico (Tokenizer)

Este repositório contém a implementação da **Fase 1 (Analisador Léxico)** de um compilador para a linguagem **Jack**. O objetivo deste módulo é ler o código-fonte `.jack`, remover comentários/espaços em branco e gerar os tokens correspondentes em um arquivo XML estruturado.

## 🧑‍💻 Integrante
* **Nome:** João Victor Lima Ewerton
* **Matrícula:** 20250013640

## 🛠️ Tecnologias e Ambiente
* **Linguagem de Programação:** Java
* **Ferramentas:** JDK (Java Development Kit)
* **Objetivo:** Construção da lógica de tokenização do zero, manipulando expressões regulares e I/O de arquivos, sem o uso de geradores automáticos.

## 🚀 Como Compilar e Executar

1. Abra o terminal na raiz do projeto e compile os arquivos Java:
   ```bash
   javac *.java

2. Execute o analisador léxico passando um arquivo .jack ou um diretório contendo arquivos .jack como argumento:
   ```bash
   java JackAnalyzer <caminho_do_arquivo_ou_pasta>
Isso gerará os arquivos de saída com o sufixo `T_Output.xml` na mesma pasta dos arquivos originais.

## 🧪 Instruções para Rodar os Testes

A validação dos tokens gerados é feita através da ferramenta oficial **TextComparer** do pacote Nand2Tetris.

1. Após rodar o analisador léxico e gerar os arquivos `T_Output.xml`, abra o terminal e navegue até a pasta `tools` do nand2tetris.
2. Execute o script de comparação (no Windows via PowerShell, use `.\`):
     ```bash
     .\TextComparer.bat <caminho_do_seu_arquivo_T_Output.xml> <caminho_do_gabarito_T.xml>
  
    -Exemplo prático:
     .\TextComparer.bat C:\projetos\10\Square\MainT_Output.xml C:\projetos\10\Square\MainT.xml

3. Se a tokenização estiver perfeita, o terminal retornará a mensagem: `Comparison ended successfully.`

---
*Projeto desenvolvido para a disciplina de Compiladores do curso de Engenharia da Computação pela Universidade Federal do Maranhão - UFMA.*

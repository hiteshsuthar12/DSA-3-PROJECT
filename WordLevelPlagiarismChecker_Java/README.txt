WORD-LEVEL PLAGIARISM CHECKER - JAVA

Project structure:
- Main.java is the backend. It reads document text, preprocesses and compares words,
  classifies the similarity result, finds common three-word sequences, and writes
  plagiarism_report.txt.
- PlagiarismCheckerUI.java is the Swing frontend. It lets users select or paste
  documents, calls the backend, and displays the returned analysis.

Requirements:
- Windows
- Java JDK 11 or later

Run the desktop application:
1. Open Command Prompt or PowerShell in this folder.
2. Compile both Java files:
   javac Main.java PlagiarismCheckerUI.java
3. Start the user interface:
   java PlagiarismCheckerUI
4. Select or paste an input document and a reference document, then choose
   CHECK PLAGIARISM.
5. The analysis is shown in the window and saved to plagiarism_report.txt.

Analysis steps:
1. Convert text to lowercase.
2. Remove punctuation and common stop words.
3. Tokenize the text into words.
4. Count input words that appear in the reference document.
5. Calculate similarity as a percentage of processed input words.
6. Find shared consecutive three-word sequences.
7. Classify the score: below 40% is low, 40-69.99% is moderate, and 70% or
   above is high potential plagiarism.

This is a word-level similarity checker, not a full commercial plagiarism
detector. It does not search the Internet and may not detect paraphrased or
semantically similar text.

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Backend service for reading documents, analyzing similarity, and writing reports. */
public class Main {

    public enum SimilarityLevel {
        LOW,
        MODERATE,
        HIGH
    }

    public static class AnalysisResult {
        public final int inputWordCount;
        public final int matchingWordCount;
        public final double similarity;
        public final List<String> commonSequences;
        public final SimilarityLevel similarityLevel;
        public final String classification;

        private AnalysisResult(
                int inputWordCount,
                int matchingWordCount,
                double similarity,
                List<String> commonSequences,
                SimilarityLevel similarityLevel,
                String classification) {
            this.inputWordCount = inputWordCount;
            this.matchingWordCount = matchingWordCount;
            this.similarity = similarity;
            this.commonSequences = commonSequences;
            this.similarityLevel = similarityLevel;
            this.classification = classification;
        }
    }

    // Preprocess text: lowercase, remove punctuation, remove stop words, tokenize
    public static List<String> preprocess(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("[^a-z0-9\\s]", " ");

        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "is", "are", "was", "were",
            "to", "of", "in", "on", "for", "with", "at", "by", "from",
            "this", "that", "it", "as", "be", "has", "have", "had"
        ));

        String[] tokens = text.trim().split("\\s+");
        List<String> words = new ArrayList<>();

        for (String token : tokens) {
            if (!token.isEmpty() && !stopWords.contains(token)) {
                words.add(token);
            }
        }
        return words;
    }

    // Count matching words between the input and reference documents.
    public static int countMatchingWords(List<String> input, List<String> reference) {
        Set<String> referenceWords = new HashSet<>(reference);
        int matches = 0;

        for (String word : input) {
            if (referenceWords.contains(word)) {
                matches++;
            }
        }
        return matches;
    }

    // Calculate similarity as percentage of input words found in reference.
    public static double calculateSimilarity(int matchingWords, int inputWordCount) {
        if (inputWordCount == 0) return 0.0;
        return (matchingWords * 100.0) / inputWordCount;
    }

    private static SimilarityLevel getSimilarityLevel(double similarity) {
        if (similarity >= 70) return SimilarityLevel.HIGH;
        if (similarity >= 40) return SimilarityLevel.MODERATE;
        return SimilarityLevel.LOW;
    }

    private static String getClassification(SimilarityLevel level) {
        switch (level) {
            case HIGH:
                return "HIGH POTENTIAL PLAGIARISM";
            case MODERATE:
                return "MODERATE SIMILARITY";
            default:
                return "LOW SIMILARITY";
        }
    }

    // Find common consecutive word sequences.
    public static List<String> findCommonSequences(List<String> input, List<String> reference, int sequenceLength) {
        List<String> sequences = new ArrayList<>();
        Set<String> referenceSequences = new HashSet<>();

        for (int i = 0; i <= reference.size() - sequenceLength; i++) {
            referenceSequences.add(String.join(" ",
                    reference.subList(i, i + sequenceLength)));
        }

        for (int i = 0; i <= input.size() - sequenceLength; i++) {
            String seq = String.join(" ", input.subList(i, i + sequenceLength));
            if (referenceSequences.contains(seq) && !sequences.contains(seq)) {
                sequences.add(seq);
            }
        }
        return sequences;
    }

    public static AnalysisResult analyze(
            String inputText,
            String referenceText,
            String inputName,
            String referenceName) throws IOException {
        List<String> inputWords = preprocess(inputText);
        List<String> referenceWords = preprocess(referenceText);
        int matchingWords = countMatchingWords(inputWords, referenceWords);
        double similarity = calculateSimilarity(matchingWords, inputWords.size());
        List<String> sequences = findCommonSequences(inputWords, referenceWords, 3);
        SimilarityLevel level = getSimilarityLevel(similarity);
        String classification = getClassification(level);

        generateReport(
                inputName,
                referenceName,
                inputWords.size(),
                matchingWords,
                similarity,
                classification,
                sequences);

        return new AnalysisResult(
                inputWords.size(), matchingWords, similarity, sequences, level, classification);
    }

    public static String readFile(String path) throws IOException {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            // If not found, search subdirectories of the working directory
            Path cwd = Path.of(System.getProperty("user.dir"));
            try (var stream = Files.walk(cwd, 3)) {
                Path found = stream
                    .filter(p -> p.getFileName().toString().equals(filePath.getFileName().toString()))
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .orElse(null);
                if (found != null) {
                    return Files.readString(found);
                }
            }
        }
        return Files.readString(filePath);
    }

    public static void generateReport(
            String inputFile,
            String referenceFile,
            int inputWords,
            int matchingWords,
            double similarity,
            String classification,
            List<String> commonSequences) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("WORD-LEVEL PLAGIARISM REPORT\n");
        report.append("====================================\n");
        report.append("Input Document   : ").append(inputFile).append("\n");
        report.append("Reference Document: ").append(referenceFile).append("\n\n");
        report.append("Processed Input Words : ").append(inputWords).append("\n");
        report.append("Matching Words        : ").append(matchingWords).append("\n");
        report.append(String.format(Locale.US, "Similarity Score      : %.2f%%%n", similarity));
        report.append("Result                : ").append(classification).append("\n\n");

        report.append("Common Word Sequences:\n");
        if (commonSequences.isEmpty()) {
            report.append("No common sequences found.\n");
        } else {
            for (String sequence : commonSequences) {
                report.append("- ").append(sequence).append("\n");
            }
        }

        Files.writeString(Path.of("plagiarism_report.txt"), report.toString());
        System.out.println("\nReport saved as: plagiarism_report.txt");
    }

}

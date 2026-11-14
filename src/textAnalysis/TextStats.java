package textAnalysis;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Silnik analityczny tekstu (Java 21).
 * Oblicza statystyki znaków, słów, n-gramów, czytelności (Flesch / Gunning Fog),
 * różnorodności leksykalnej (TTR), czasu czytania oraz rozkładów częstości.
 */
public final class TextStats {

    public record FreqItem(int rank, String token, int count, double percentage, double relativeToMax) {}

    public record CharFreqItem(
            int rank,
            char rawChar,
            String displayChar,
            String unicodeHex,
            String category,
            int count,
            double percentAll,
            double percentCategory,
            double relativeToMax
    ) {}

    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+(?:['’\\-][\\p{L}\\p{N}]+)*");
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("[.!?…]+(?:\\s+|$)");
    private static final String VOWELS = "aąeęiouyóAĄEĘIOUYÓ";

    private static final Set<String> STOP_WORDS = Set.copyOf(Arrays.asList(
            // Polish stop words
            "i", "w", "nie", "na", "z", "do", "to", "że", "się", "o", "jak", "ale", "po", "co", "tak",
            "za", "od", "go", "jego", "jej", "ich", "czy", "już", "tylko", "bardzo", "przez", "dla",
            "jest", "są", "był", "była", "było", "byli", "będzie", "być", "ma", "mają", "ten", "ta",
            "te", "tym", "tej", "tego", "tych", "który", "która", "które", "którzy", "kiedy", "gdy",
            "lub", "albo", "oraz", "więc", "lecz", "czyż", "nad", "pod", "przed", "przy", "bez", "między",
            "ze", "we", "ku", "aż", "też", "także", "jednak", "jeszcze", "wszystko", "nic", "kto",
            "gdzie", "skąd", "dlaczego", "jaki", "jaka", "jakie", "mój", "twój", "swój", "nasz", "wasz",
            "ja", "ty", "on", "ona", "ono", "my", "wy", "oni", "one", "mnie", "ciebie", "nas", "was",
            "mi", "ci", "mu", "nam", "wam", "im", "mną", "tobą", "nim", "nią", "nimi", "sobie",
            // English stop words
            "the", "and", "to", "of", "in", "is", "it", "that", "for", "on", "with", "as", "was", "at",
            "by", "be", "this", "have", "from", "or", "had", "but", "not", "are", "were", "which", "you",
            "all", "an", "can", "her", "has", "there", "been", "if", "more", "when", "will", "would",
            "who", "so", "no", "out", "up", "into", "your", "we", "they", "he", "she", "his", "their",
            "what", "about", "than", "its", "only", "other", "new", "some", "could", "time", "these",
            "two", "may", "then", "do", "first", "any", "my", "now", "such", "like", "our", "over",
            "me", "him", "them", "am", "did", "does", "done"
    ));

    private final String rawText;
    private final int totalChars;
    private final int charsNoSpaces;
    private final int letterCount;
    private final int uppercaseCount;
    private final int lowercaseCount;
    private final int digitCount;
    private final int whitespaceCount;
    private final int punctuationCount;
    private final int lineCount;
    private final int paragraphCount;
    private final int sentenceCount;
    private final int wordCount;
    private final int uniqueWordCount;
    private final int syllableCount;
    private final int complexWordCount;

    private final double avgWordLength;
    private final double avgSentenceLength;
    private final double lexicalDiversityPercent;
    private final double fleschReadingEase;
    private final double gunningFogIndex;
    private final int readingTimeSeconds;
    private final int speakingTimeSeconds;

    private final String longestWord;
    private final String detectedLanguage;
    private final String readabilityLabel;

    private final List<FreqItem> wordFrequencies;
    private final List<FreqItem> keywordFrequencies;
    private final List<FreqItem> bigramFrequencies;
    private final List<FreqItem> trigramFrequencies;
    private final List<CharFreqItem> charFrequenciesCaseSensitive;
    private final List<CharFreqItem> charFrequenciesCaseInsensitive;
    private final Map<Integer, Integer> wordLengthHistogram;

    private TextStats(String text) {
        this.rawText = text == null ? "" : text.replace("\r\n", "\n").replace("\r", "\n");
        this.totalChars = rawText.length();

        int letters = 0;
        int upper = 0;
        int lower = 0;
        int digits = 0;
        int whitespaces = 0;
        int punctuation = 0;
        int plDiacritics = 0;

        Map<Character, Integer> charMapCS = new HashMap<>();
        Map<Character, Integer> charMapCI = new HashMap<>();

        for (int i = 0; i < rawText.length(); i++) {
            char c = rawText.charAt(i);
            charMapCS.merge(c, 1, Integer::sum);
            charMapCI.merge(Character.toLowerCase(c), 1, Integer::sum);

            if (Character.isWhitespace(c)) {
                whitespaces++;
            } else if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) upper++;
                else if (Character.isLowerCase(c)) lower++;
                if ("ąćęłńóśźżĄĆĘŁŃÓŚŹŻ".indexOf(c) >= 0) {
                    plDiacritics++;
                }
            } else if (Character.isDigit(c)) {
                digits++;
            } else {
                punctuation++;
            }
        }

        this.letterCount = letters;
        this.uppercaseCount = upper;
        this.lowercaseCount = lower;
        this.digitCount = digits;
        this.whitespaceCount = whitespaces;
        this.punctuationCount = punctuation;
        this.charsNoSpaces = totalChars - whitespaces;

        if (rawText.isEmpty()) {
            this.lineCount = 0;
            this.paragraphCount = 0;
        } else {
            this.lineCount = (int) rawText.lines().count();
            String[] paras = rawText.trim().split("\\n\\s*\\n+");
            this.paragraphCount = rawText.trim().isEmpty() ? 0 : paras.length;
        }

        // Extract words
        List<String> wordsLower = new ArrayList<>();
        Map<String, Integer> allWordCounts = new HashMap<>();
        Map<String, Integer> keywordCounts = new HashMap<>();
        Map<Integer, Integer> lenHist = new TreeMap<>();

        String longest = "";
        long totalWordChars = 0;
        int totalSyllables = 0;
        int complexWords = 0;

        Matcher matcher = WORD_PATTERN.matcher(rawText);
        while (matcher.find()) {
            String rawWord = matcher.group();
            String norm = rawWord.toLowerCase(Locale.ROOT);
            wordsLower.add(norm);

            allWordCounts.merge(norm, 1, Integer::sum);
            if (norm.length() > 2 && !STOP_WORDS.contains(norm) && !norm.chars().allMatch(Character::isDigit)) {
                keywordCounts.merge(norm, 1, Integer::sum);
            }

            int wLen = rawWord.length();
            totalWordChars += wLen;
            lenHist.merge(Math.min(wLen, 16), 1, Integer::sum);

            if (wLen > longest.length()) {
                longest = rawWord;
            }

            int syl = countSyllables(norm);
            totalSyllables += syl;
            if (syl >= 3) {
                complexWords++;
            }
        }

        this.wordCount = wordsLower.size();
        this.uniqueWordCount = allWordCounts.size();
        this.syllableCount = totalSyllables;
        this.complexWordCount = complexWords;
        this.longestWord = longest.isEmpty() ? "—" : longest;
        this.wordLengthHistogram = Collections.unmodifiableMap(lenHist);

        // Sentence count
        if (wordCount == 0) {
            this.sentenceCount = 0;
        } else {
            String trimmed = rawText.trim();
            String[] parts = SENTENCE_SPLIT.split(trimmed);
            int sCount = 0;
            for (String p : parts) {
                if (!p.isBlank()) sCount++;
            }
            this.sentenceCount = Math.max(1, sCount);
        }

        this.avgWordLength = wordCount > 0 ? (double) totalWordChars / wordCount : 0.0;
        this.avgSentenceLength = sentenceCount > 0 ? (double) wordCount / sentenceCount : 0.0;
        this.lexicalDiversityPercent = wordCount > 0 ? ((double) uniqueWordCount / wordCount) * 100.0 : 0.0;

        this.readingTimeSeconds = (int) Math.ceil((wordCount / 200.0) * 60.0);
        this.speakingTimeSeconds = (int) Math.ceil((wordCount / 130.0) * 60.0);

        // Readability metrics
        if (wordCount > 0 && sentenceCount > 0) {
            double asl = avgSentenceLength;
            double asw = (double) syllableCount / wordCount;
            double rawFlesch = 206.835 - (1.015 * asl) - (84.6 * asw);
            this.fleschReadingEase = Math.max(0.0, Math.min(100.0, rawFlesch));
            this.gunningFogIndex = 0.4 * (asl + 100.0 * ((double) complexWordCount / wordCount));
        } else {
            this.fleschReadingEase = 0.0;
            this.gunningFogIndex = 0.0;
        }

        this.readabilityLabel = computeReadabilityLabel(this.fleschReadingEase, this.wordCount);
        this.detectedLanguage = detectLanguage(plDiacritics, allWordCounts);

        // N-grams (bigrams & trigrams)
        Map<String, Integer> bigramMap = new HashMap<>();
        Map<String, Integer> trigramMap = new HashMap<>();
        for (int i = 0; i < wordsLower.size() - 1; i++) {
            String w1 = wordsLower.get(i);
            String w2 = wordsLower.get(i + 1);
            if (!STOP_WORDS.contains(w1) || !STOP_WORDS.contains(w2)) {
                bigramMap.merge(w1 + " " + w2, 1, Integer::sum);
            }
            if (i < wordsLower.size() - 2) {
                String w3 = wordsLower.get(i + 2);
                trigramMap.merge(w1 + " " + w2 + " " + w3, 1, Integer::sum);
            }
        }

        this.wordFrequencies = buildFreqList(allWordCounts, wordCount, 300, 1);
        this.keywordFrequencies = buildFreqList(keywordCounts, wordCount, 300, 1);
        this.bigramFrequencies = buildFreqList(bigramMap, Math.max(1, wordCount - 1), 150, 2);
        this.trigramFrequencies = buildFreqList(trigramMap, Math.max(1, wordCount - 2), 150, 2);

        this.charFrequenciesCaseSensitive = buildCharFreqList(charMapCS, totalChars, letterCount);
        this.charFrequenciesCaseInsensitive = buildCharFreqList(charMapCI, totalChars, letterCount);
    }

    public static TextStats analyze(String text) {
        return new TextStats(text);
    }

    private static int countSyllables(String word) {
        if (word == null || word.isEmpty()) return 0;
        int count = 0;
        boolean prevVowel = false;
        for (int i = 0; i < word.length(); i++) {
            boolean isVowel = VOWELS.indexOf(word.charAt(i)) >= 0;
            if (isVowel && !prevVowel) {
                count++;
            }
            prevVowel = isVowel;
        }
        return Math.max(1, count);
    }

    private static String computeReadabilityLabel(double flesch, int words) {
        if (words == 0) return "Brak tekstu";
        if (flesch >= 80) return "Bardzo przystępny";
        if (flesch >= 65) return "Przystępny (Standardowy)";
        if (flesch >= 50) return "Średnio zaawansowany";
        if (flesch >= 30) return "Zaawansowany / Specjalistyczny";
        return "Bardzo trudny / Akademicki";
    }

    private static String detectLanguage(int plDiacritics, Map<String, Integer> wordCounts) {
        if (wordCounts.isEmpty()) return "—";
        if (plDiacritics > 0) return "Polski (PL)";
        int plHits = wordCounts.getOrDefault("nie", 0) + wordCounts.getOrDefault("jest", 0)
                + wordCounts.getOrDefault("oraz", 0) + wordCounts.getOrDefault("tylko", 0);
        int enHits = wordCounts.getOrDefault("the", 0) + wordCounts.getOrDefault("and", 0)
                + wordCounts.getOrDefault("that", 0) + wordCounts.getOrDefault("with", 0);
        if (plHits > enHits && plHits > 0) return "Polski (PL)";
        if (enHits > plHits && enHits > 0) return "Angielski (EN)";
        return "Uniwersalny / Kod";
    }

    private static List<FreqItem> buildFreqList(Map<String, Integer> counts, int totalDenominator, int limit, int minCount) {
        List<Map.Entry<String, Integer>> sorted = counts.entrySet().stream()
                .filter(e -> e.getValue() >= minCount)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .toList();

        int maxCount = sorted.isEmpty() ? 1 : sorted.getFirst().getValue();
        List<FreqItem> result = new ArrayList<>(sorted.size());
        int rank = 1;
        for (var entry : sorted) {
            int c = entry.getValue();
            double pct = totalDenominator > 0 ? (c * 100.0) / totalDenominator : 0.0;
            double rel = (double) c / maxCount;
            result.add(new FreqItem(rank++, entry.getKey(), c, pct, rel));
        }
        return Collections.unmodifiableList(result);
    }

    private static List<CharFreqItem> buildCharFreqList(Map<Character, Integer> map, int totalChars, int totalLetters) {
        List<Map.Entry<Character, Integer>> sorted = map.entrySet().stream()
                .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();

        int maxCount = sorted.isEmpty() ? 1 : sorted.getFirst().getValue();
        List<CharFreqItem> result = new ArrayList<>(sorted.size());
        int rank = 1;
        for (var entry : sorted) {
            char c = entry.getKey();
            int count = entry.getValue();
            String category = classifyChar(c);
            String display = formatCharDisplay(c);
            String hex = String.format("U+%04X", (int) c);
            double pctAll = totalChars > 0 ? (count * 100.0) / totalChars : 0.0;
            double pctCat = (Character.isLetter(c) && totalLetters > 0)
                    ? (count * 100.0) / totalLetters
                    : pctAll;
            double rel = (double) count / maxCount;
            result.add(new CharFreqItem(rank++, c, display, hex, category, count, pctAll, pctCat, rel));
        }
        return Collections.unmodifiableList(result);
    }

    public static String classifyChar(char c) {
        if (Character.isLetter(c)) return "Litera";
        if (Character.isDigit(c)) return "Cyfra";
        if (Character.isWhitespace(c)) return "Biały znak";
        return "Interpunkcja / Symbol";
    }

    public static String formatCharDisplay(char c) {
        return switch (c) {
            case ' ' -> "␣ (Spacja)";
            case '\n' -> "↵ (Nowa linia)";
            case '\t' -> "⇥ (Tabulator)";
            case '\r' -> "⏎ (Powrót karetki)";
            case '\u00A0' -> "␣ (Twarda spacja)";
            default -> String.valueOf(c);
        };
    }

    public static String formatDuration(int totalSeconds) {
        if (totalSeconds <= 0) return "0 s";
        if (totalSeconds < 60) return totalSeconds + " s";
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
        if (mins < 60) {
            return secs > 0 ? String.format("%d min %d s", mins, secs) : String.format("%d min", mins);
        }
        int hours = mins / 60;
        int remMins = mins % 60;
        return String.format("%d godz. %d min", hours, remMins);
    }

    public String toMarkdownReport(String sourceName) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Raport Analizy Tekstu — ").append(sourceName).append("\n\n");
        sb.append("## 1. Podsumowanie metryk\n");
        sb.append(String.format(Locale.US, "- **Wszystkie znaki (ze spacjami):** %d\n", totalChars));
        sb.append(String.format(Locale.US, "- **Znaki (bez białych znaków):** %d\n", charsNoSpaces));
        sb.append(String.format(Locale.US, "- **Litery:** %d (wielkie: %d, małe: %d)\n", letterCount, uppercaseCount, lowercaseCount));
        sb.append(String.format(Locale.US, "- **Cyfry:** %d | **Interpunkcja/Symbole:** %d | **Białe znaki:** %d\n", digitCount, punctuationCount, whitespaceCount));
        sb.append(String.format(Locale.US, "- **Liczba słów:** %d (unikalnych: %d)\n", wordCount, uniqueWordCount));
        sb.append(String.format(Locale.US, "- **Różnorodność słownictwa (TTR):** %.1f%%\n", lexicalDiversityPercent));
        sb.append(String.format(Locale.US, "- **Zdania:** %d | **Akapity:** %d | **Linie:** %d\n", sentenceCount, paragraphCount, lineCount));
        sb.append(String.format(Locale.US, "- **Średnia długość słowa:** %.2f zn. | **Najdłuższe słowo:** `%s`\n", avgWordLength, longestWord));
        sb.append(String.format(Locale.US, "- **Średnia długość zdania:** %.1f słów\n", avgSentenceLength));
        sb.append(String.format(Locale.US, "- **Indeks czytelności Flescha:** %.1f / 100 (%s)\n", fleschReadingEase, readabilityLabel));
        sb.append(String.format(Locale.US, "- **Indeks Gunning Fog:** %.1f\n", gunningFogIndex));
        sb.append(String.format(Locale.US, "- **Szacowany czas czytania:** %s | **Czas mówienia:** %s\n\n",
                formatDuration(readingTimeSeconds), formatDuration(speakingTimeSeconds)));

        sb.append("## 2. Najczęstsze słowa kluczowe (Top 15)\n");
        sb.append("| # | Słowo | Wystąpienia | Udział (%) |\n|---|---|---|---|\n");
        keywordFrequencies.stream().limit(15).forEach(item ->
                sb.append(String.format(Locale.US, "| %d | `%s` | %d | %.2f%% |\n",
                        item.rank(), item.token(), item.count(), item.percentage()))
        );

        sb.append("\n## 3. Najczęstsze litery (Top 15)\n");
        sb.append("| # | Znak | Kod Unicode | Wystąpienia | Udział w literach (%) |\n|---|---|---|---|---|\n");
        charFrequenciesCaseInsensitive.stream()
                .filter(c -> Character.isLetter(c.rawChar()))
                .limit(15)
                .forEach(item ->
                        sb.append(String.format(Locale.US, "| %d | `%s` | %s | %d | %.2f%% |\n",
                                item.rank(), item.displayChar(), item.unicodeHex(), item.count(), item.percentCategory()))
                );
        return sb.toString();
    }

    public String toCsvReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Typ,Element,Liczba,Udzial_Procentowy\n");
        for (FreqItem w : wordFrequencies.stream().limit(100).toList()) {
            sb.append(String.format(Locale.US, "Slowo,\"%s\",%d,%.3f\n",
                    w.token().replace("\"", "\"\""), w.count(), w.percentage()));
        }
        for (CharFreqItem c : charFrequenciesCaseInsensitive) {
            sb.append(String.format(Locale.US, "Znak (%s),\"%s\",%d,%.3f\n",
                    c.category(), c.displayChar().replace("\"", "\"\""), c.count(), c.percentAll()));
        }
        return sb.toString();
    }

    public String getRawText() { return rawText; }
    public int getTotalChars() { return totalChars; }
    public int getCharsNoSpaces() { return charsNoSpaces; }
    public int getLetterCount() { return letterCount; }
    public int getUppercaseCount() { return uppercaseCount; }
    public int getLowercaseCount() { return lowercaseCount; }
    public int getDigitCount() { return digitCount; }
    public int getWhitespaceCount() { return whitespaceCount; }
    public int getPunctuationCount() { return punctuationCount; }
    public int getLineCount() { return lineCount; }
    public int getParagraphCount() { return paragraphCount; }
    public int getSentenceCount() { return sentenceCount; }
    public int getWordCount() { return wordCount; }
    public int getUniqueWordCount() { return uniqueWordCount; }
    public int getSyllableCount() { return syllableCount; }
    public int getComplexWordCount() { return complexWordCount; }
    public double getAvgWordLength() { return avgWordLength; }
    public double getAvgSentenceLength() { return avgSentenceLength; }
    public double getLexicalDiversityPercent() { return lexicalDiversityPercent; }
    public double getFleschReadingEase() { return fleschReadingEase; }
    public double getGunningFogIndex() { return gunningFogIndex; }
    public int getReadingTimeSeconds() { return readingTimeSeconds; }
    public int getSpeakingTimeSeconds() { return speakingTimeSeconds; }
    public String getLongestWord() { return longestWord; }
    public String getDetectedLanguage() { return detectedLanguage; }
    public String getReadabilityLabel() { return readabilityLabel; }
    public List<FreqItem> getWordFrequencies() { return wordFrequencies; }
    public List<FreqItem> getKeywordFrequencies() { return keywordFrequencies; }
    public List<FreqItem> getBigramFrequencies() { return bigramFrequencies; }
    public List<FreqItem> getTrigramFrequencies() { return trigramFrequencies; }
    public List<CharFreqItem> getCharFrequencies(boolean caseSensitive) {
        return caseSensitive ? charFrequenciesCaseSensitive : charFrequenciesCaseInsensitive;
    }
    public Map<Integer, Integer> getWordLengthHistogram() { return wordLengthHistogram; }
}

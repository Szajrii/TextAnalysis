package textAnalysis;

/**
 * Klasa startowa dla paczki JAR w Java 21 (omija wymóg modułowego launchera dla klasy dziedziczącej po Application).
 */
public final class Launcher {
    public static void main(String[] args) {
        TextAnalysisApp.main(args);
    }
}

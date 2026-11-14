package textAnalysis;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Kontroler główny aplikacji TextAnalyzer Studio (Java 21 + JavaFX 21).
 */
public class TextAnalyzer {

    private static final List<SampleDocument> SAMPLE_DOCUMENTS = List.of(
            new SampleDocument(
                    "Przykład 1: Literatura i technologia (PL)",
                    """
                    Inżynieria oprogramowania to nie tylko pisanie kodu, ale przede wszystkim sztuka rozwiązywania problemów i tworzenia czytelnej architektury. Kiedy wracamy do projektów sprzed lat, widzimy jak bardzo zmieniło się nasze podejście do czystego kodu, testowania oraz projektowania interfejsów użytkownika.

                    Współczesna analiza tekstu pozwala w ułamku sekundy wyznaczyć nie tylko liczbę znaków czy słów, lecz także złożoność leksykalną, rozkład n-gramów, indeks czytelności Flescha oraz szacowany czas lektury. Dzięki temu autorzy, redaktorzy oraz programiści mogą precyzyjnie ocenić przystępność dokumentacji technicznej, artykułów naukowych oraz literatury pięknej.

                    Dobry programista nie boi się refaktoryzacji. Zastąpienie przestarzałych komponentów AWT natywnymi kontrolkami JavaFX, wyeliminowanie wycieków pamięci w słuchaczach zdarzeń (listeners) oraz wprowadzenie strumieniowego przetwarzania w Java 21 sprawia, że mały akademicki projekt zamienia się w nowoczesne narzędzie analityczne!
                    """
            ),
            new SampleDocument(
                    "Przykład 2: Pan Tadeusz — Inwokacja (PL)",
                    """
                    Litwo! Ojczyzno moja! ty jesteś jak zdrowie.
                    Ile cię trzeba cenić, ten tylko się dowie,
                    Kto cię stracił. Dziś piękność twą w całej ozdobie
                    Widzę i opisuję, bo tęsknię po tobie.

                    Panno Święta, co Jasnej bronisz Częstochowie
                    I w Ostrej świecisz Bramie! Ty, co gród zamkowy
                    Nowogródzki ochraniasz z jego wiernym ludem!
                    Jak mnie dziecko do zdrowia powróciłaś cudem
                    (Gdy od płaczącej matki pod Twoją opiekę
                    Ofiarowany, martwą podniosłem powiekę
                    I zaraz mogłem pieszo do Twych świątyń progu
                    Iść za wrócone życie podziękować Bogu),
                    Tak nas powrócisz cudem na Ojczyzny łono.
                    Tymczasem przenoś moją duszę utęsknioną
                    Do tych pagórków leśnych, do tych łąk zielonych,
                    Szeroko nad błękitnym Niemnem rozciągnionych;
                    Do tych pól malowanych zbożem rozmaitem,
                    Wyzłacanych pszenicą, posrebrzanych żytem.
                    """
            ),
            new SampleDocument(
                    "Przykład 3: Artificial Intelligence & Software Craftsmanship (EN)",
                    """
                    Modern natural language processing starts with fundamental statistical text analysis. Before large language models ever existed, computational linguists relied on token frequency distributions, Zipf's law, lexical diversity metrics, and n-gram Markov chains to understand the structure of human language.

                    Even today, fast deterministic text analytics remain essential. Writers use Flesch Reading Ease and Gunning Fog scores to keep prose crisp and clear. Engineers inspect character encodings, Unicode codepoints, and regular expression matches to validate data pipelines. Great software combines fast algorithms with an intuitive, responsive user interface.
                    """
            )
    );

    private record SampleDocument(String title, String content) {}

    @FXML private BorderPane rootPane;
    @FXML private TabPane mainTabPane;

    // Header & Badges
    @FXML private Label fileChoosen;
    @FXML private Label languageBadge;
    @FXML private Label statusBadge;

    // 6 KPI Cards
    @FXML private Label characteramount;
    @FXML private Label characteramountnospaces;
    @FXML private Label wordsamount;
    @FXML private Label uniqueWordsLabel;
    @FXML private Label sentencesAmountLabel;
    @FXML private Label paragraphsLinesLabel;
    @FXML private Label readingTimeLabel;
    @FXML private Label speakingTimeLabel;
    @FXML private Label readabilityScoreLabel;
    @FXML private Label readabilityDescLabel;
    @FXML private Label avgWordLenLabel;
    @FXML private Label longestWordLabel;

    // Tab 1: Charts & Breakdown
    @FXML private PieChart charCompositionPieChart;
    @FXML private Label lettersBreakdownLabel;
    @FXML private Label digitsPunctLabel;
    @FXML private Label avgSentenceLenLabel;
    @FXML private Label syllablesComplexLabel;
    @FXML private Label fogIndexLabel;
    @FXML private ComboBox<String> chartModeCombo;
    @FXML private BarChart<String, Number> frequencyBarChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;

    // Tab 2: Character Explorer
    @FXML private TextField charSearchField;
    @FXML private ComboBox<String> charCategoryCombo;
    @FXML private CheckBox caseSensitiveCheck;
    @FXML private TableView<TextStats.CharFreqItem> charTableView;
    @FXML private TableColumn<TextStats.CharFreqItem, Integer> charRankCol;
    @FXML private TableColumn<TextStats.CharFreqItem, String> charSymbolCol;
    @FXML private TableColumn<TextStats.CharFreqItem, String> charUnicodeCol;
    @FXML private TableColumn<TextStats.CharFreqItem, String> charCategoryCol;
    @FXML private TableColumn<TextStats.CharFreqItem, Integer> charCountCol;
    @FXML private TableColumn<TextStats.CharFreqItem, String> charPercentCol;

    @FXML private Label inspectorBigChar;
    @FXML private Label mostoccur;
    @FXML private Label characterChoosen;
    @FXML private ProgressBar inspectorProgressBar;
    @FXML private ChoiceBox<String> choice;
    @FXML private ListView<String> charContextListView;

    // Tab 3: Vocabulary & N-Grams
    @FXML private TextField wordSearchField;
    @FXML private ComboBox<String> vocabModeCombo;
    @FXML private TableView<TextStats.FreqItem> wordTableView;
    @FXML private TableColumn<TextStats.FreqItem, Integer> wordRankCol;
    @FXML private TableColumn<TextStats.FreqItem, String> wordTokenCol;
    @FXML private TableColumn<TextStats.FreqItem, Integer> wordCountCol;
    @FXML private TableColumn<TextStats.FreqItem, String> wordPercentCol;
    @FXML private Label selectedWordSummaryLabel;
    @FXML private ListView<String> wordConcordanceListView;

    // Tab 4: Live Editor & Regex Search
    @FXML private CheckBox liveAutoUpdateCheck;
    @FXML private TextArea liveEditorArea;
    @FXML private TextField regexInputField;
    @FXML private CheckBox regexModeCheck;
    @FXML private Label regexResultLabel;
    @FXML private ListView<String> regexMatchesListView;

    private String currentSourceName = "Tekst demonstracyjny";
    private TextStats currentStats = TextStats.analyze("");
    private int currentSampleIndex = 0;
    private boolean suppressEditorListener = false;
    private boolean suppressChoiceListener = false;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupCombosAndFilters();
        setupDragAndDrop();
        setupLiveEditorListeners();

        // Wczytaj pierwszy tekst demonstracyjny na start
        loadSampleByIndex(0);
    }

    private void setupTableColumns() {
        // Character Table
        charRankCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().rank()));
        charSymbolCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayChar()));
        charUnicodeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unicodeHex()));
        charCategoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().category()));
        charCountCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().count()));
        charPercentCol.setCellValueFactory(c -> new SimpleStringProperty(
                String.format(Locale.US, "%.2f%%", c.getValue().percentAll())));

        charTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                inspectCharacter(newVal);
            }
        });

        // Word / N-gram Table
        wordRankCol.setCellValueFactory(w -> new SimpleObjectProperty<>(w.getValue().rank()));
        wordTokenCol.setCellValueFactory(w -> new SimpleStringProperty(w.getValue().token()));
        wordCountCol.setCellValueFactory(w -> new SimpleObjectProperty<>(w.getValue().count()));
        wordPercentCol.setCellValueFactory(w -> new SimpleStringProperty(
                String.format(Locale.US, "%.2f%%", w.getValue().percentage())));

        wordTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showWordConcordance(newVal);
            }
        });
    }

    private void setupCombosAndFilters() {
        chartModeCombo.setItems(FXCollections.observableArrayList(
                "Najczęstsze słowa kluczowe (Top 15)",
                "Najczęstsze litery (Top 15)",
                "Rozkład długości słów (Histogram)",
                "Najczęstsze frazy 2-wyrazowe (Bigrams)"
        ));
        chartModeCombo.getSelectionModel().selectFirst();
        chartModeCombo.valueProperty().addListener((obs, o, n) -> refreshBarChart());

        charCategoryCombo.setItems(FXCollections.observableArrayList(
                "Wszystkie kategorie",
                "Tylko litery",
                "Cyfry",
                "Białe znaki",
                "Interpunkcja / Symbol"
        ));
        charCategoryCombo.getSelectionModel().selectFirst();
        charCategoryCombo.valueProperty().addListener((obs, o, n) -> refreshCharacterTable());
        charSearchField.textProperty().addListener((obs, o, n) -> refreshCharacterTable());
        caseSensitiveCheck.selectedProperty().addListener((obs, o, n) -> refreshCharacterTable());

        vocabModeCombo.setItems(FXCollections.observableArrayList(
                "Słowa kluczowe (bez stop-words)",
                "Wszystkie słowa",
                "Frazy 2-wyrazowe (Bigrams)",
                "Frazy 3-wyrazowe (Trigrams)"
        ));
        vocabModeCombo.getSelectionModel().selectFirst();
        vocabModeCombo.valueProperty().addListener((obs, o, n) -> refreshWordTable());
        wordSearchField.textProperty().addListener((obs, o, n) -> refreshWordTable());

        // Naprawiony listener ChoiceBox (rejestrowany tylko raz, z obsługą null / -1)
        choice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedDisplay) -> {
            if (suppressChoiceListener || selectedDisplay == null) return;
            boolean cs = caseSensitiveCheck.isSelected();
            currentStats.getCharFrequencies(cs).stream()
                    .filter(item -> item.displayChar().equals(selectedDisplay))
                    .findFirst()
                    .ifPresent(item -> {
                        charTableView.getSelectionModel().select(item);
                        charTableView.scrollTo(item);
                        inspectCharacter(item);
                    });
        });
    }

    private void setupDragAndDrop() {
        rootPane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() || db.hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        rootPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && !db.getFiles().isEmpty()) {
                File file = db.getFiles().getFirst();
                loadFile(file.toPath());
                success = true;
            } else if (db.hasString()) {
                applyNewText("Wklejony tekst (Drag & Drop)", db.getString(), true);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupLiveEditorListeners() {
        liveEditorArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!suppressEditorListener && liveAutoUpdateCheck.isSelected()) {
                applyNewText("Edytor na żywo", newText, false);
            }
        });

        regexInputField.textProperty().addListener((obs, o, n) -> runPatternSearch());
        regexModeCheck.selectedProperty().addListener((obs, o, n) -> runPatternSearch());
    }

    @FXML
    public void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Wybierz plik tekstowy do analizy");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Pliki tekstowe i dokumenty", "*.txt", "*.md", "*.csv", "*.log", "*.json", "*.xml", "*.java", "*.py", "*.html"),
                new FileChooser.ExtensionFilter("Wszystkie pliki (*.*)", "*.*")
        );

        Window window = rootPane != null && rootPane.getScene() != null ? rootPane.getScene().getWindow() : null;
        File selectedFile = chooser.showOpenDialog(window);
        if (selectedFile != null) {
            loadFile(selectedFile.toPath());
        }
    }

    private void loadFile(Path path) {
        try {
            String content = readFileSmartEncoding(path);
            String fileName = path.getFileName().toString();
            applyNewText(fileName, content, true);
            statusBadge.setText("Wczytano plik (" + Files.size(path) + " B)");
        } catch (IOException ex) {
            statusBadge.setText("Błąd odczytu pliku");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nie udało się odczytać pliku:\n" + ex.getMessage(), ButtonType.OK);
            alert.setHeaderText("Błąd wczytywania pliku");
            alert.showAndWait();
        }
    }

    private static String readFileSmartEncoding(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            // Fallback na Windows-1250 dla starszych polskich plików .txt
            return Charset.forName("windows-1250").decode(ByteBuffer.wrap(bytes)).toString();
        }
    }

    @FXML
    public void loadSampleText() {
        currentSampleIndex = (currentSampleIndex + 1) % SAMPLE_DOCUMENTS.size();
        loadSampleByIndex(currentSampleIndex);
    }

    private void loadSampleByIndex(int index) {
        SampleDocument sample = SAMPLE_DOCUMENTS.get(index);
        applyNewText(sample.title(), sample.content(), true);
        statusBadge.setText("Przykład " + (index + 1) + "/" + SAMPLE_DOCUMENTS.size());
    }

    @FXML
    public void analyzeFromEditor() {
        applyNewText("Edytor na żywo", liveEditorArea.getText(), false);
        statusBadge.setText("Przeliczono z edytora");
    }

    @FXML
    public void clearEditor() {
        liveEditorArea.clear();
        applyNewText("Edytor na żywo (pusty)", "", false);
        statusBadge.setText("Wyczyszczono");
    }

    private void applyNewText(String sourceName, String text, boolean updateEditorContent) {
        this.currentSourceName = sourceName;
        this.currentStats = TextStats.analyze(text);

        if (updateEditorContent) {
            suppressEditorListener = true;
            liveEditorArea.setText(text);
            suppressEditorListener = false;
        }

        updateDashboardUI();
    }

    private void updateDashboardUI() {
        fileChoosen.setText("Źródło: " + currentSourceName);
        languageBadge.setText(currentStats.getDetectedLanguage());

        // 1. KPI Cards
        characteramount.setText(String.format(Locale.US, "%,d", currentStats.getTotalChars()));
        characteramountnospaces.setText(String.format(Locale.US, "Bez białych znaków: %,d", currentStats.getCharsNoSpaces()));

        wordsamount.setText(String.format(Locale.US, "%,d", currentStats.getWordCount()));
        uniqueWordsLabel.setText(String.format(Locale.US, "Unikalne: %,d (TTR: %.1f%%)",
                currentStats.getUniqueWordCount(), currentStats.getLexicalDiversityPercent()));

        sentencesAmountLabel.setText(currentStats.getSentenceCount() + " zdań");
        paragraphsLinesLabel.setText(String.format("Akapity: %d • Linie: %d",
                currentStats.getParagraphCount(), currentStats.getLineCount()));

        readingTimeLabel.setText(TextStats.formatDuration(currentStats.getReadingTimeSeconds()));
        speakingTimeLabel.setText("Mowa (~130 sł./min): " + TextStats.formatDuration(currentStats.getSpeakingTimeSeconds()));

        readabilityScoreLabel.setText(String.format(Locale.US, "%.1f / 100", currentStats.getFleschReadingEase()));
        readabilityDescLabel.setText(currentStats.getReadabilityLabel());

        avgWordLenLabel.setText(String.format(Locale.US, "%.2f zn.", currentStats.getAvgWordLength()));
        String longest = currentStats.getLongestWord();
        if (longest.length() > 18) {
            longest = longest.substring(0, 17) + "…";
        }
        longestWordLabel.setText("Najdłuższe: " + longest);

        // 2. Breakdown & PieChart
        lettersBreakdownLabel.setText(String.format(Locale.US, "%,d (W: %,d / M: %,d)",
                currentStats.getLetterCount(), currentStats.getUppercaseCount(), currentStats.getLowercaseCount()));
        digitsPunctLabel.setText(String.format(Locale.US, "Cyfry: %,d • Symbole: %,d",
                currentStats.getDigitCount(), currentStats.getPunctuationCount()));
        avgSentenceLenLabel.setText(String.format(Locale.US, "%.1f słów / zdanie", currentStats.getAvgSentenceLength()));
        syllablesComplexLabel.setText(String.format(Locale.US, "%,d sylab • %,d trudnych",
                currentStats.getSyllableCount(), currentStats.getComplexWordCount()));
        fogIndexLabel.setText(String.format(Locale.US, "%.1f lat edukacji", currentStats.getGunningFogIndex()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (currentStats.getLetterCount() > 0)
            pieData.add(new PieChart.Data("Litery (" + currentStats.getLetterCount() + ")", currentStats.getLetterCount()));
        if (currentStats.getWhitespaceCount() > 0)
            pieData.add(new PieChart.Data("Białe znaki (" + currentStats.getWhitespaceCount() + ")", currentStats.getWhitespaceCount()));
        if (currentStats.getPunctuationCount() > 0)
            pieData.add(new PieChart.Data("Interpunkcja (" + currentStats.getPunctuationCount() + ")", currentStats.getPunctuationCount()));
        if (currentStats.getDigitCount() > 0)
            pieData.add(new PieChart.Data("Cyfry (" + currentStats.getDigitCount() + ")", currentStats.getDigitCount()));
        charCompositionPieChart.setData(pieData);

        refreshBarChart();
        refreshCharacterTable();
        refreshWordTable();
        runPatternSearch();
    }

    private void refreshBarChart() {
        String mode = chartModeCombo.getValue();
        if (mode == null) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (mode.startsWith("Najczęstsze słowa")) {
            barChartXAxis.setLabel("Słowo kluczowe");
            barChartYAxis.setLabel("Liczba wystąpień");
            currentStats.getKeywordFrequencies().stream().limit(15).forEach(item ->
                    series.getData().add(new XYChart.Data<>(item.token(), item.count()))
            );
        } else if (mode.startsWith("Najczęstsze litery")) {
            barChartXAxis.setLabel("Litera");
            barChartYAxis.setLabel("Liczba wystąpień");
            currentStats.getCharFrequencies(false).stream()
                    .filter(c -> Character.isLetter(c.rawChar()))
                    .limit(15)
                    .forEach(item -> series.getData().add(new XYChart.Data<>(item.displayChar(), item.count())));
        } else if (mode.startsWith("Rozkład długości")) {
            barChartXAxis.setLabel("Długość słowa (znaki)");
            barChartYAxis.setLabel("Liczba słów");
            for (Map.Entry<Integer, Integer> entry : currentStats.getWordLengthHistogram().entrySet()) {
                String label = entry.getKey() >= 16 ? "16+" : entry.getKey() + " zn.";
                series.getData().add(new XYChart.Data<>(label, entry.getValue()));
            }
        } else {
            barChartXAxis.setLabel("Fraza 2-wyrazowa (Bigram)");
            barChartYAxis.setLabel("Liczba wystąpień");
            currentStats.getBigramFrequencies().stream().limit(12).forEach(item ->
                    series.getData().add(new XYChart.Data<>(item.token(), item.count()))
            );
        }

        frequencyBarChart.getData().setAll(List.of(series));
    }

    private void refreshCharacterTable() {
        boolean cs = caseSensitiveCheck.isSelected();
        String categoryFilter = charCategoryCombo.getValue();
        String query = charSearchField.getText() == null ? "" : charSearchField.getText().trim().toLowerCase(Locale.ROOT);

        List<TextStats.CharFreqItem> allChars = currentStats.getCharFrequencies(cs);
        List<TextStats.CharFreqItem> filtered = allChars.stream()
                .filter(c -> {
                    if (categoryFilter == null || categoryFilter.equals("Wszystkie kategorie")) return true;
                    if (categoryFilter.equals("Tylko litery")) return c.category().equals("Litera");
                    return c.category().equals(categoryFilter);
                })
                .filter(c -> query.isEmpty()
                        || c.displayChar().toLowerCase(Locale.ROOT).contains(query)
                        || c.unicodeHex().toLowerCase(Locale.ROOT).contains(query))
                .toList();

        charTableView.setItems(FXCollections.observableArrayList(filtered));

        // Update ChoiceBox without triggering duplicate listener loops
        suppressChoiceListener = true;
        choice.getItems().setAll(filtered.stream().map(TextStats.CharFreqItem::displayChar).toList());
        if (!filtered.isEmpty()) {
            TextStats.CharFreqItem top = filtered.getFirst();
            mostoccur.setText(String.format(Locale.US, "Najczęstszy w widoku: '%s' — %,d razy (%.2f%%)",
                    top.displayChar(), top.count(), top.percentAll()));
            choice.getSelectionModel().selectFirst();
            charTableView.getSelectionModel().selectFirst();
            inspectCharacter(top);
        } else {
            mostoccur.setText("Brak znaków spełniających kryteria filtra");
            characterChoosen.setText("Zmień filtr kategorii lub wyszukiwania.");
            inspectorBigChar.setText("—");
            inspectorProgressBar.setProgress(0);
            charContextListView.getItems().clear();
        }
        suppressChoiceListener = false;
    }

    private void inspectCharacter(TextStats.CharFreqItem item) {
        inspectorBigChar.setText(String.valueOf(item.rawChar()).isBlank() ? item.displayChar().substring(0, 1) : String.valueOf(item.rawChar()));
        characterChoosen.setText(String.format(Locale.US,
                "Znak '%s' (%s, %s) wystąpił %,d razy • %.2f%% wszystkich znaków",
                item.displayChar(), item.unicodeHex(), item.category(), item.count(), item.percentAll()));
        inspectorProgressBar.setProgress(item.relativeToMax());

        suppressChoiceListener = true;
        choice.getSelectionModel().select(item.displayChar());
        suppressChoiceListener = false;

        // Find sample lines containing this character
        List<String> snippets = new ArrayList<>();
        String raw = currentStats.getRawText();
        boolean cs = caseSensitiveCheck.isSelected();
        char target = cs ? item.rawChar() : Character.toLowerCase(item.rawChar());

        String[] lines = raw.split("\\n");
        int lineNum = 0;
        for (String line : lines) {
            lineNum++;
            String checkLine = cs ? line : line.toLowerCase(Locale.ROOT);
            if (checkLine.indexOf(target) >= 0 && !line.isBlank()) {
                snippets.add(String.format("Linia %d: %s", lineNum, line.trim()));
                if (snippets.size() >= 25) break;
            }
        }
        if (snippets.isEmpty()) {
            snippets.add("(Znak występuje jako znak podziału linii lub w pustych wierszach)");
        }
        charContextListView.setItems(FXCollections.observableArrayList(snippets));
    }

    private void refreshWordTable() {
        String mode = vocabModeCombo.getValue();
        String query = wordSearchField.getText() == null ? "" : wordSearchField.getText().trim().toLowerCase(Locale.ROOT);

        List<TextStats.FreqItem> sourceList;
        if (mode == null || mode.startsWith("Słowa kluczowe")) {
            sourceList = currentStats.getKeywordFrequencies();
        } else if (mode.startsWith("Wszystkie słowa")) {
            sourceList = currentStats.getWordFrequencies();
        } else if (mode.startsWith("Frazy 2-wyrazowe")) {
            sourceList = currentStats.getBigramFrequencies();
        } else {
            sourceList = currentStats.getTrigramFrequencies();
        }

        List<TextStats.FreqItem> filtered = sourceList.stream()
                .filter(item -> query.isEmpty() || item.token().toLowerCase(Locale.ROOT).contains(query))
                .toList();

        wordTableView.setItems(FXCollections.observableArrayList(filtered));
        if (!filtered.isEmpty()) {
            wordTableView.getSelectionModel().selectFirst();
            showWordConcordance(filtered.getFirst());
        } else {
            selectedWordSummaryLabel.setText("Brak słów lub fraz pasujących do filtra.");
            wordConcordanceListView.getItems().clear();
        }
    }

    private void showWordConcordance(TextStats.FreqItem item) {
        selectedWordSummaryLabel.setText(String.format(Locale.US,
                "Wyrażenie \"%s\" — %,d wystąpień (%.2f%%). Poniżej wiersze zawierające to wyrażenie:",
                item.token(), item.count(), item.percentage()));

        String tokenLower = item.token().toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        String[] lines = currentStats.getRawText().split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && line.toLowerCase(Locale.ROOT).contains(tokenLower)) {
                matches.add(String.format("[Wiersz %d] %s", i + 1, line));
                if (matches.size() >= 40) break;
            }
        }
        wordConcordanceListView.setItems(FXCollections.observableArrayList(matches));
    }

    @FXML
    public void runPatternSearch() {
        String query = regexInputField.getText();
        if (query == null || query.isEmpty()) {
            regexResultLabel.setText("Wpisz frazę lub wzorzec powyżej, aby przeszukać tekst.");
            regexMatchesListView.getItems().clear();
            return;
        }

        boolean useRegex = regexModeCheck.isSelected();
        Pattern pattern;
        try {
            pattern = useRegex
                    ? Pattern.compile(query, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                    : Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException ex) {
            regexResultLabel.setText("Błąd składni wyrażenia regularnego: " + ex.getDescription());
            regexMatchesListView.getItems().clear();
            return;
        }

        List<String> results = new ArrayList<>();
        int totalMatches = 0;
        String[] lines = currentStats.getRawText().split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher m = pattern.matcher(line);
            int lineHits = 0;
            while (m.find()) {
                totalMatches++;
                lineHits++;
            }
            if (lineHits > 0 && results.size() < 100) {
                results.add(String.format("Linia %d (%d×): %s", i + 1, lineHits, line.trim()));
            }
        }

        regexResultLabel.setText(String.format("Znaleziono łącznie %,d dopasowań w %,d wierszach.", totalMatches, results.size()));
        regexMatchesListView.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    public void copyReportToClipboard() {
        String report = currentStats.toMarkdownReport(currentSourceName);
        ClipboardContent content = new ClipboardContent();
        content.putString(report);
        Clipboard.getSystemClipboard().setContent(content);
        statusBadge.setText("✓ Skopiowano raport do schowka!");
    }

    @FXML
    public void exportReport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Eksportuj raport analizy tekstu");
        chooser.setInitialFileName("raport_analizy_tekstu.md");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Raport Markdown (*.md)", "*.md"),
                new FileChooser.ExtensionFilter("Dane tabelaryczne CSV (*.csv)", "*.csv")
        );

        Window window = rootPane != null && rootPane.getScene() != null ? rootPane.getScene().getWindow() : null;
        File out = chooser.showSaveDialog(window);
        if (out != null) {
            try {
                boolean isCsv = out.getName().toLowerCase(Locale.ROOT).endsWith(".csv");
                String data = isCsv ? currentStats.toCsvReport() : currentStats.toMarkdownReport(currentSourceName);
                Files.writeString(out.toPath(), data, StandardCharsets.UTF_8);
                statusBadge.setText("✓ Zapisano: " + out.getName());
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd zapisu raportu:\n" + ex.getMessage(), ButtonType.OK);
                alert.showAndWait();
            }
        }
    }
}

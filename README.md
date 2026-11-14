# TextAnalyzer Studio (Java 21 + JavaFX 21)

Nowoczesne narzędzie desktopowe do analizy statystycznej, leksykalnej i strukturalnej tekstu.

## Główne funkcje
- **Karty KPI na żywo:** liczba znaków (z białymi znakami i bez), liczba słów, słownictwo unikalne, współczynnik różnorodności leksykalnej (**TTR**), liczba zdań, akapitów i linii, szacowany czas czytania (~200 słów/min) i mówienia (~130 słów/min).
- **Wskaźniki czytelności:** **Flesch Reading Ease** (0–100) oraz **Gunning Fog Index** wraz z klasyfikacją trudności tekstu i detekcją języka (PL / EN).
- **Interaktywne wykresy:**
  - Wykres kołowy (`PieChart`) struktury znaków (litery, białe znaki, interpunkcja, cyfry).
  - Przełączalny wykres słupkowy (`BarChart`): najczęstsze słowa kluczowe, najczęstsze litery, histogram długości słów oraz najczęstsze frazy 2-wyrazowe (bigrams).
- **Analiza znaków i Inspektor:** filtrowanie po kategorii (litery, cyfry, białe znaki, interpunkcja), przełącznik wielkości liter (`A ≠ a`), podgląd kodu Unicode (`U+XXXX`) oraz kontekst wystąpień w wierszach tekstu.
- **Słownictwo i N-gramy:** ranking słów kluczowych (z automatycznym odfiltrowaniem polskich i angielskich *stop-words*), wszystkich słów, bigramów i trigramów wraz z konkordancją zdań.
- **Edytor na żywo i wyszukiwarka Regex:** edycja/wklejanie tekstu z przeliczaniem w czasie rzeczywistym, obsługa **Drag & Drop** plików oraz eksport raportów do **Markdown (`.md`)** i **CSV (`.csv`)**.

## Uruchomienie
Dwukrotne kliknięcie pliku `run.bat` lub z terminala:
```powershell
& "C:\Program Files\BellSoft\LibericaJDK-21-Full\bin\java.exe" -jar out\artifacts\TextAnalysis_jar\TextAnalysis.jar
```

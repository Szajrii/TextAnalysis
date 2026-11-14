package textAnalysis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

/**
 * Punkt wejścia aplikacji TextAnalyzer Studio (Java 21 + JavaFX 21).
 */
public class TextAnalysisApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(
                TextAnalysisApp.class.getResource("Template.fxml"),
                "Nie znaleziono pliku Template.fxml"
        ));
        Parent layout = fxmlLoader.load();

        Scene scene = new Scene(layout, 1180, 780);
        URL cssUrl = TextAnalysisApp.class.getResource("style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("TextAnalyzer Studio — Zaawansowana Analiza Tekstu");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.toFront();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

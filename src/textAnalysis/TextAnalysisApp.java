package textAnalysis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * Created by Michal Szarek
 **/
public class TextAnalysisApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(TextAnalysisApp.class.getResource("Template.fxml"));
        Parent layout = fxmlLoader.load();
        Scene scene = new Scene(layout);
        scene.getStylesheets().add("textAnalysis/style.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("TextAnalyzer");
        primaryStage.show();
    }
}

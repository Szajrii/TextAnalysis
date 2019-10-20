package textAnalysis;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

/**
 * Created by Michal Szarek
 **/
public class TextAnalyzer {

    private String filePath;

//    @FXML
//    private String file;
    @FXML
    private Label fileChoosen, characteramount, characteramountnospaces, wordsamount, characters, mostoccur, characterChoosen;
    @FXML
    private ChoiceBox choice;


    @FXML
    public void chooseFile() throws IOException {
        String nameOfFile;

        FileDialog chooser = new FileDialog((Frame)null, "Select file to analyze");
        chooser.setMode(FileDialog.LOAD);
        chooser.setVisible(true);
        nameOfFile = chooser.getFile();
        fileChoosen.setText(nameOfFile);

        filePath = chooser.getDirectory() + chooser.getFile();
        analyzeText();
    }

    private void analyzeText()throws IOException {
        String textToAnalyze;

        FileInputStream fileInputStream = new FileInputStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        textToAnalyze = getTextFromBuffer(bufferedReader);

        characteramount.setText("Amount of characters(spaces including): " + textToAnalyze.length());
        characteramountnospaces.setText("Amount of characters: " + numberOfCaracterSpaceExcludin(textToAnalyze));
        wordsamount.setText("Amount of words: " + numberOfWords(textToAnalyze));

        HashMap<String, Integer> charactersMap = new HashMap<>(countEveryCharacter(textToAnalyze));
        StringBuilder charactersString = new StringBuilder();

        for (HashMap.Entry<String, Integer> entry : charactersMap.entrySet()) {
            charactersString.append("'" + entry.getKey() + "'" + " : " + entry.getValue() + "\n");
        }

        characters.setText(charactersString.toString());
        mostoccur.setText(findMostOccurences(charactersMap));

        setChoiceBox(charactersMap);
        choice.setStyle("-fx-opacity: 1");

        //Choice box controller
        choice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                characterChoosen.setText("Character " + "'" + choice.getItems().get((Integer) number2) + "' has " +
                        "occured " +
                        charactersMap.get(choice.getItems().get((Integer) number2)) + " times");
            }
        });

        bufferedReader.close();
    }

    private String getTextFromBuffer(BufferedReader file) {
        String text = "";
        StringBuilder wholeText = new StringBuilder();

        try {
            //read all file lanes
            while ( (text = file.readLine()) != null ){
                wholeText.append(text + "\n");
            }
            text = wholeText.toString();
        } catch (IOException ex) {
            System.out.println("Error has occured");
        }
        return text;
    }

    private int numberOfCaracterSpaceExcludin(String text) {
        int spaces = 0;
        for(int i = 0; i < text.length(); i++) {
            spaces += (Character.isWhitespace(text.charAt(i))) ? 1 : 0;
        }
        return text.length() - spaces;
    }

    private int numberOfWords(String text) {
        int wordCounter = 0;
        boolean spaceOccurenceOnPreviousCharacter = false;
        for(int i = 0; i < text.length(); i++) {
            if(!Character.isWhitespace(text.charAt(i)) ) {
                spaceOccurenceOnPreviousCharacter = false;
            }else if(!spaceOccurenceOnPreviousCharacter) {
                wordCounter ++;
                spaceOccurenceOnPreviousCharacter = true;
            }
        }
        return wordCounter;
    }

    private HashMap<String, Integer> countEveryCharacter(String text) {

        HashMap<String, Integer> charactersMap = new HashMap<>();
        for(int i = 0; i < text.length(); i++) {
            if(charactersMap.containsKey("" + text.charAt(i)) ) {
                charactersMap.put("" + text.charAt(i), (charactersMap.get("" + text.charAt(i) )+ 1));
            } else {
                charactersMap.put("" + text.charAt(i), 1);
            }
        }
        return charactersMap;
    }

    private String findMostOccurences(HashMap<String, Integer> map) {
        int occurences = 0;
        String key = "";

        for (HashMap.Entry<String, Integer> entry : map.entrySet()) {
            if(entry.getValue() > occurences) {
                key = entry.getKey();
                occurences = entry.getValue();
            }else {}
        }
        return "Most occurences in character '" + key + "' : " + occurences + " times.";

    }

    private void setChoiceBox(HashMap<String, Integer> map) {

        ArrayList<Integer> arr = new ArrayList<>(map.values());
        for (HashMap.Entry<String, Integer> entry : map.entrySet()) {
            choice.getItems().add("" + entry.getKey());
        }

    }
}

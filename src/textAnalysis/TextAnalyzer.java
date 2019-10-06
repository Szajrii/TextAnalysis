package textAnalysis;

import javafx.fxml.FXML;

import java.awt.*;
import java.io.*;

import javafx.scene.control.Label;

/**
 * Created by Michal Szarek
 **/
public class TextAnalyzer {

    private String filePath;

//    @FXML
//    private String file;
    @FXML
    private Label fileChoosen, characteramount, characteramountnospaces, wordsamount;

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
        System.out.println(text);
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
}

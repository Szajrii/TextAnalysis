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
    private Label fileChoosen, characteramount;

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

//        FileInputStream fileInputStream = new FileInputStream(file);
//        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//        try {
//            String line = bufferedReader.readLine();
//            bufferedReader.close();
//            System.out.println(line);
//        } catch (IOException ex) {
//            System.out.println("Wystapil blad");
//        }
    }

    private void analyzeText()throws IOException {

        FileInputStream fileInputStream = new FileInputStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        characteramount.setText("Amount of characters(spaces including): " + countFileLenght(bufferedReader));
    }

    private int countFileLenght(BufferedReader file) throws IOException {
        int fileLenght = 0;
        String text;
        StringBuilder wholeText = new StringBuilder();

        try {
            while ( (text = file.readLine()) != null ){
                wholeText.append(text);
            }
            file.close();
            text = wholeText.toString();
            fileLenght = text.length();
        } catch (IOException ex) {
            System.out.println("Error has occured");
        }


        return fileLenght;
    }
}

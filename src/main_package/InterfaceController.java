package main_package;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author Michał Martyniak
 */
public class InterfaceController implements Initializable {

    @FXML
    private TextField inputTextField;

    private Scene sceneReference;
    private Connection connection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inputTextField.setPromptText("Type some text here...");
    }

    void setScene(Scene scene) {
        sceneReference = scene;
    }
    
    void setConnectionReference(Connection connection) {
        this.connection = connection;
    }

    void setShortcutListener() {

        inputTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(oldValue + " -> " + newValue);
        });

        sceneReference.getAccelerators().put(
                new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_ANY),
                new Runnable() {
            @Override
            public void run() {
                //actions performed on shortcut detection
                inputTextField.requestFocus();
                System.out.println("Rozpoznano kombinację ctrl+space dla textu: \"" + inputTextField.getText()+"\"");
                handleTextChange();
            }
        }
        );
    }
    
    private void handleTextChange(){
        
        try {
                String findSecWordQuery = "SELECT DISTINCT SEC_WORD FROM MICZI.WORD_PAIRS WHERE FIRST_WORD = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(findSecWordQuery);
                preparedStatement.setString(1, inputTextField.getText());

                ResultSet results = preparedStatement.executeQuery();
                
                //display matches
                System.out.println("Słowa, które pasują: ");
                while (results.next()) {
                    System.out.println(results.getString("SEC_WORD"));
                }
                //close query
                results.close();
                preparedStatement.close();
            } catch (SQLException ex) {
                Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    
    

}

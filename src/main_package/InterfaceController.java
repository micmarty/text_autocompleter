package main_package;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.URL;
import java.util.ResourceBundle;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        

    }

    void setScene(Scene scene) {
        sceneReference = scene;
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
                //condition here of you that want you want to achive.
                inputTextField.requestFocus();
                System.out.println("rozpoznano ctrl+space");
            }
        }
        );
    }

}

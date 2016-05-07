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
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
    @FXML
    private ComboBox<String> suggestionsComboBox;
    private ObservableList<String> suggestions = FXCollections.observableArrayList();

    private Scene sceneReference;
    private Connection connection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inputTextField.setPromptText("Type some text here...");
        suggestionsComboBox.setVisible(false);
    }

    void setScene(Scene scene) {
        sceneReference = scene;
    }

    void setConnectionReference(Connection connection) {
        this.connection = connection;
    }

    void setShortcutListener() {
        //show how inputTextField changes its content
        inputTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(oldValue + " -> " + newValue);
        });
        
        //CTRL+SPACE Listener
        sceneReference.getAccelerators().put(
                new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_ANY),
                new Runnable() {
            @Override
            public void run() {
                //actions performed on shortcut detection
                inputTextField.requestFocus();
                System.out.println("Rozpoznano kombinację ctrl+space dla textu: \"" + inputTextField.getText() + "\"");
                handleTextChange();
            }
        }
        );
    }

    private void handleTextChange() {

        try {
            //Make query statement, execute it and get execution results
            String findSecWordQuery = "SELECT DISTINCT SEC_WORD FROM MICZI.WORD_PAIRS WHERE FIRST_WORD = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(findSecWordQuery);
            preparedStatement.setString(1, inputTextField.getText());
            ResultSet results = preparedStatement.executeQuery();
            
            
            suggestions.clear();            //list must be always empty at the beginning!
            while (results.next()) {        //populate observable list with query result elements
                
                suggestions.add(results.getString("SEC_WORD"));
            }
            
            //fill combobox with observable list (query results)
            suggestionsComboBox.setItems(suggestions);
            suggestionsComboBox.getSelectionModel().selectFirst();
            suggestionsComboBox.show();

            //close query
            results.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

package main_package;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.event.ItemListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Michał Martyniak
 */
public class InterfaceController implements Initializable {

    @FXML
    private TextField inputTextField;
    private String selectedText;

    @FXML
    private ListView<String> suggestionsList;
    private ObservableList<String> suggestions = FXCollections.observableArrayList();

    private Scene sceneReference;
    private Connection con;
    private IndexRange selectionIndexRange;

    private enum LastSuggestion {
        WORD_ITSELF, SECOND_WORD
    }
    private LastSuggestion lastSuggested;//PROBABLY TO DELETION

    
    
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        suggestionsList.setVisible(false);      //we want to hide suggestions list at this stage
        selectedText = new String();            //initialize with empty string
    }

    void setScene(Scene scene) {
        sceneReference = scene;
    }

    void setConnectionReference(Connection connection) {
        this.con = connection;
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
                handleTextChange();
                System.out.println("Rozpoznano kombinację ctrl+space dla textu: \"" + selectedText + "\"");
            }
        });

        suggestionsList.setOnKeyPressed((e) -> {
            
            if (e.getCode() == KeyCode.ENTER) {
                System.out.println("Wcisnieto ENTER");

                //useful local variables
                ListView<String> s = (ListView) e.getSource();  // 's' means source. Just to shorten very large "suggestionsList" sentence
                String selectedSuggestion = s.getSelectionModel().getSelectedItem();//item currently selected on the suggestionsList
                int initialCursorPosition = selectionIndexRange.getEnd();//index in TextField, where cursor was placed BEFORE acceptin suggestion
                
                //if we want to autocomplete second word, then we add the suggestion on the right side to the selection
                if(lastSuggested == LastSuggestion.SECOND_WORD){
                    inputTextField.insertText(initialCursorPosition, " " + selectedSuggestion);
                    inputTextField.requestFocus();  //abandon suggestionList focus
                    inputTextField.positionCaret(initialCursorPosition);    //put cursor where it was before autocomplete, 
                                                                            //because user would want to make some changes in the middle of the text
                                                                            //EXAMPLE: Where| -> Where| are
                }
                //if we want to autocomplete only one word, we are replacing partially typed word with the whole word
                else if (lastSuggested == LastSuggestion.WORD_ITSELF) {
                    //restore selection after loosing focus from previously saved ivar
                    inputTextField.selectRange(selectionIndexRange.getStart(), selectionIndexRange.getEnd());
                    inputTextField.replaceSelection(selectedSuggestion);
                    inputTextField.requestFocus();  //abandon suggestionList focus
                    //set cursor just after freshly completed word. EXAMPLE: Wh|   -> When|
                    inputTextField.positionCaret(selectionIndexRange.getEnd() + selectedSuggestion.length());
                }
                
                

            }
        });

    }

    /* 
        executes query:
        if user's input was exactly first word, 
        then we're looking only for the second word
    
        EXAMPLE:
        input -> Where
        output -> Where are, Where is
     */
    private ResultSet suggestSecondWord() {
        ResultSet results = null;

        String query = "SELECT DISTINCT SEC_WORD FROM MICZI.WORD_PAIRS WHERE FIRST_WORD = ?";
        PreparedStatement preparedStatement = null;

        //try execute query on database and assign resultset to a variable
        try {
            preparedStatement = con.prepareStatement(query);
            System.out.println(selectedText);
            preparedStatement.setString(1, selectedText);//place string into first ? sign above ^
            results = preparedStatement.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;
    }

    /* 
        executes query:
        if user's input was NOT exactly first word, 
        then we're looking for the rest of the given word
    
        EXAMPLE:
        input -> Whe
        output -> Where, When etc.
     */
    private ResultSet suggestFirstWord() {
        ResultSet results = null;
        //use names specified in other class
        String DBName = DataBaseConnector.DBName;
        String TableName = DataBaseConnector.TableName;

        String query = "SELECT DISTINCT FIRST_WORD FROM " + DBName + "." + TableName + " WHERE FIRST_WORD LIKE ?";
        PreparedStatement preparedStatement = null;

        //try execute query on database and assign resultset to a variable
        try {
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, selectedText + "%");//word% - regex expression
            results = preparedStatement.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;
    }

    /*  
        Make query statement, execute it and get execution results    
     */
    private ResultSet findSuggestions() {
        ResultSet results = null;

        try {
            results = suggestSecondWord();      //see method description above ^
            lastSuggested = LastSuggestion.SECOND_WORD;

            if (!results.next()) {
                results = suggestFirstWord();   //see method description above ^
                lastSuggested = LastSuggestion.WORD_ITSELF;
            }

            //it messes something up, because it closes value that we want to RETURN
            //close query
            //results.close();
            //preparedStatement.close();
        } catch (SQLException ex) {
            Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /*  
        helper method - populates observable list with query result elements    
     */
    private void populateListWithQueryResults(ResultSet results) {
        try {
            if (lastSuggested == LastSuggestion.SECOND_WORD) {
                suggestions.add("");//TO DELETETION LATER ON...
            }
            while (results.next()) {
                if (lastSuggested == LastSuggestion.SECOND_WORD) {
                    suggestions.add(results.getString("SEC_WORD"));
                } else if (lastSuggested == LastSuggestion.WORD_ITSELF) {
                    suggestions.add(results.getString("FIRST_WORD"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* 
        Make selection on whole word, by which cursor is surrounded
     */
    private void selectWordGivenByCursorPosition() {
        inputTextField.selectPreviousWord();        //move cursor to the left edge
        inputTextField.deselect();                  //cancel selection
        
        //now cursor is at the beggining of the word, 
        //so we just need to move selection to the end of the word
        inputTextField.selectEndOfNextWord();
        
        //save selection,because when textfield looses its focus, selection is also lost
        selectionIndexRange = inputTextField.getSelection();
        selectedText = inputTextField.getSelectedText();
    }

    /*  
        Reacts to shortcut and input
        for example: finds second word suggestion(as query)
        then makes list from query results
        finally populates comboBox
        in other words it makes a lot even if it's small(it uses helper methods)    
     */
    private void handleTextChange() {
        //IMPORTANT -> read called methods descriptions
        selectWordGivenByCursorPosition();

        ResultSet results = findSuggestions();

        suggestions.clear();                                //list must be always empty before populating it again!
        populateListWithQueryResults(results);

        suggestionsList.setItems(suggestions);              //populate listView with query results
        suggestionsList.getSelectionModel().selectFirst();  //select first on the list(equally god would be random element)
        suggestionsList.setVisible(true);                   //we need to see suggestions

        //set focus on list, so that user can instantly choose suggestion
        Platform.runLater(() -> {
            suggestionsList.requestFocus();
        });
    }

}

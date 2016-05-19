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
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 *
 * @author Michał Martyniak
 */
//TODO
//1. if word was started with a capital letter, WORD_ITSELF suggestion should respect taht and leave capital letter as it is
//2. do the same for rhymes
//4. think about dividing current class into more accurate and smaller ones.
//5. try to implement double word suggestion
//6. implement multiple file chooser functionality and putting them into db alltogether
public class InterfaceController implements Initializable {

    @FXML
    private TextField inputTextField;
    private String selectedText;

    @FXML
    private ListView<String> suggestionsList;
    private ObservableList<String> suggestions = FXCollections.observableArrayList();

    @FXML
    private ListView<String> rhymesList;
    private ObservableList<String> rhymes = FXCollections.observableArrayList();

    private Scene sceneReference;
    private Stage stageReference;
    
    private Connection con;
    private IndexRange selectionIndexRange;


    private enum LastSuggestion {
        WORD_ITSELF, SECOND_WORD
    }
    private LastSuggestion lastSuggested;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        suggestionsList.setVisible(false);      //we want to hide suggestions list at this stage
        selectedText = new String();            //initialize with empty string
    }

    void setSceneReference(Scene scene) {
        sceneReference = scene;
    }
    
    void setStageReference(Stage stage) {
        stageReference = stage;
    }
    
    void setConnectionReference(Connection connection) {
        con = connection;
    }

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

        //CTRL+R Listener
        sceneReference.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_ANY),
                new Runnable() {
            @Override
            public void run() {
                //actions performed on shortcut detection
                inputTextField.requestFocus();
                handleSuggestionsFinding();
                System.out.println("Rozpoznano kombinację ctrl+R dla textu: \"" + selectedText + "\"");
            }
        });

        suggestionsList.setOnKeyPressed((e) -> {

            if (e.getCode() == KeyCode.ENTER) {
                System.out.println("Wcisnieto ENTER");
                acceptSuggestion();
            }
        });

    }
    
    @FXML
    private void chooseSourceFiles(){
        FileChooser chooser = new FileChooser();
        
        chooser.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));
        chooser.showOpenMultipleDialog(stageReference);
    }
    
    
    
    
    
    
    
    

//-------RHYMEs RELATED METHODS----------------------------------------------------------------------------------------------
    private void handleSuggestionsFinding() {
        selectWordGivenByCursorPosition();

        ResultSet results = findRhymes();

        rhymes.clear();                                //list must be always empty before populating it again!
        populateRhymesWithQueryResults(results);

        rhymesList.setItems(rhymes);              //populate listView with query results

    }

    private ResultSet findRhymes() {
        ResultSet results = null;

        //try execute query on database and assign resultset to a variable
        try {
            //it is crucial to search for SEC_WORD, because we don't avoid last word in a sentence this way.
            //but we omit first word in a sentence, FIX THIS
            String query 
                    = "SELECT SEC_WORD, COUNT(*) AS POPULARITY "
                    + "FROM MICZI.WORD_PAIRS "
                    + "WHERE SEC_WORD LIKE ? "
                    + "AND SEC_WORD NOT LIKE ? "
                    + "GROUP BY SEC_WORD "
                    + "HAVING COUNT(*) >= 1 "
                    + "ORDER BY POPULARITY DESC";
            
            PreparedStatement preparedStatement = null;

            if (selectedText.length() > 3) {
                preparedStatement = con.prepareStatement(query);

                String lastThreeLetters = selectedText.substring(selectedText.length() - 3);
                System.out.println(lastThreeLetters);

                preparedStatement.setString(1, "%" + lastThreeLetters);
                preparedStatement.setString(2, selectedText);
                results = preparedStatement.executeQuery();
            }

        } catch (SQLException ex) {
            Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;
    }

    private void populateRhymesWithQueryResults(ResultSet results) {
        try {
            while (results.next()) {
                //rhymes.add(results.getString("FIRST_WORD"));
                rhymes.add(results.getString("SEC_WORD"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//--------END-------RHYMEs RELATED METHODS----------------------------------------------------------------------------------------------

//----SUGGESTIONs RELATED METHODS-------------------------------------------------------
    /*
        It is responsible for putting suggestion into correct place
        It controls caret position also, so any caret related bugsa after accepting suggestion
        are supposed to be here
     */
    private void acceptSuggestion() {
        //useful local variables
        ListView<String> s = suggestionsList;  // 's' means source. Just to shorten very large "suggestionsList" sentence
        String selectedSuggestion = s.getSelectionModel().getSelectedItem();//item currently selected on the suggestionsList
        int initialCursorPosition = selectionIndexRange.getEnd();//index in TextField, where cursor was placed BEFORE acceptin suggestion

        //if we want to autocomplete second word, then we add the suggestion on the right side to the selection
        if (lastSuggested == LastSuggestion.SECOND_WORD) {
            inputTextField.insertText(initialCursorPosition, " " + selectedSuggestion);
            inputTextField.requestFocus();  //abandon suggestionList focus
            
       
            //inputTextField.positionCaret(initialCursorPosition);    //put cursor where it was before autocomplete, 
            //OR
            inputTextField.positionCaret(selectionIndexRange.getEnd() + 1 + selectedSuggestion.length() + 1); 
            
            //because user would want to make some changes in the middle of the text
            //EXAMPLE: Where| -> Where| are
        } //if we want to autocomplete only one word, we are replacing partially typed word with the whole word
        else if (lastSuggested == LastSuggestion.WORD_ITSELF) {
            //restore selection after loosing focus from previously saved ivar
            inputTextField.selectRange(selectionIndexRange.getStart(), selectionIndexRange.getEnd());
            inputTextField.replaceSelection(selectedSuggestion);
            inputTextField.requestFocus();  //abandon suggestionList focus
            //set cursor just after freshly completed word. EXAMPLE: Wh|   -> When|
            inputTextField.positionCaret(selectionIndexRange.getEnd() + selectedSuggestion.length());
        }
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

        //count all the PAIR duplicates, group them and show how many of them occured in DB
        //counter column is called PAIR_OCCURENCES
        //highest occurence number is at the top of ResultSet
        try {
            String query
                    = "SELECT FIRST_WORD, SEC_WORD, COUNT(*) AS PAIR_OCCURENCES "
                    + "FROM MICZI.WORD_PAIRS "
                    + "WHERE FIRST_WORD = ? "
                    + "GROUP BY FIRST_WORD, SEC_WORD "
                    + "HAVING COUNT(*) >= 1 "
                    + "ORDER BY PAIR_OCCURENCES DESC";
            PreparedStatement preparedStatement = null;

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

        //count all the duplicates, group them and show how many of them occured in DB
        //counter column is called FIRST_WORD_OCCURENCE
        //highest occurence number is at the top of ResultSet
        //http://puu.sh/oXEdY/1c19d39bdb.png
        String query
                = "SELECT FIRST_WORD, COUNT(*) AS FIRST_WORD_OCCURENCE "
                + "FROM " + DBName + "." + TableName + " "
                + "WHERE FIRST_WORD LIKE ? "
                + "GROUP BY FIRST_WORD "
                + "HAVING COUNT(*) >= 1 "
                + "ORDER BY FIRST_WORD_OCCURENCE DESC";

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

            if (lastSuggested == LastSuggestion.WORD_ITSELF) {
                while (results.next()) {
                    suggestions.add(results.getString("FIRST_WORD"));
                }
            } else if (lastSuggested == LastSuggestion.SECOND_WORD) {
                do {
                    suggestions.add(results.getString("SEC_WORD"));
                } while (results.next());
            }

        } catch (SQLException ex) {
            Logger.getLogger(InterfaceController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* 
        Make selection on whole word, by which cursor is surrounded
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
//----END-----SUGGESTIONs RELATED METHODS-------------------------------------------------------
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main_package;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Michał Martyniak
 */
public class InputFileParser {
    private Connection connetion;
    private String[] wordsInLine;
    
    public InputFileParser(Connection connetion) {
        this.connetion = connetion;
    }
    
    
    private void insertWordPairIntoDB(String word1, String word2){
        try {
            // the mysql insert statement
            String query = "INSERT INTO MICZI.WORD_PAIRS (FIRST_WORD, SEC_WORD)"
                    + " VALUES (?, ?)";
            
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = connetion.prepareStatement(query);
            preparedStmt.setString (1, word1);
            preparedStmt.setString (2, word2);
            
            // execute the prepared statement
            preparedStmt.execute();
        } catch (SQLException ex) {
            if(ex.getSQLState().equals(ex))
            Logger.getLogger(InputFileParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /*  
        loads file from given path, remove symbols defined in patter/regular expression
        then insert words into DB
    */
    //TODO split and simpify/split into smaller methods
    //TODO think about smarter patterns to eliminate all unnecessary symbols
    public void readFile(String pathToFile){
        try(BufferedReader br = new BufferedReader(new FileReader(pathToFile)))
        {
            String currentLine;
            while((currentLine = br.readLine()) != null){
                
                System.out.print(currentLine + "\t\t");//print line before
                Pattern p = Pattern.compile("[^a-zA-ZąęćżźńłóśĄĘĆŻŹŃŁÓŚé\\s]");
                currentLine = p.matcher(currentLine).replaceAll("");//delete symbols that doesnt match regex
                System.out.println(currentLine);//print line after regex filtering
                
                wordsInLine = currentLine.split(" "); //divide one line into separate words and put them into an array
                for(int word = 0; word < wordsInLine.length-1; word++){
                    insertWordPairIntoDB(wordsInLine[word], wordsInLine[word+1]);//insert one by one: word, it's right neighboor
                }
                
            }
                
        }catch(FileNotFoundException e){
            System.err.println("File not found!");
        } catch (IOException ex) {
            Logger.getLogger(InputFileParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
}

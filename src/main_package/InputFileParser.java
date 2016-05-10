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

    public InputFileParser(Connection connetion) {
        this.connetion = connetion;
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
            while((currentLine = br.readLine())!=null){
                System.out.print(currentLine + "\t\t");
                Pattern p = Pattern.compile("[^a-zA-ZąęćżźńłóśĄĘĆŻŹŃŁÓŚé\\s]");
                
                currentLine = p.matcher(currentLine).replaceAll("");
                System.out.println(currentLine);
                
                
                String[] wordsInLine = currentLine.split(" ");
                
                for(int word = 0; word < wordsInLine.length-1; word++){
                    // the mysql insert statement
                    String query = "INSERT INTO MICZI.WORD_PAIRS (FIRST_WORD, SEC_WORD, OCCURENCES_COUNTER)"
                      + " VALUES (?, ?, ?)";

                    // create the mysql insert preparedstatement
                    PreparedStatement preparedStmt = connetion.prepareStatement(query);
                    preparedStmt.setString (1, wordsInLine[word]);
                    preparedStmt.setString (2, wordsInLine[word+1]);
                    preparedStmt.setInt(3, 1);

                    // execute the prepared statement
                    preparedStmt.execute();
                }
                //if(wordsInLine.length >= 2){
                    
                //}
            }
                
        }catch(FileNotFoundException e){
            System.err.println("File not found!");
        } catch (IOException ex) {
            Logger.getLogger(InputFileParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(InputFileParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
}

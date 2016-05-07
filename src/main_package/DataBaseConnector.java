/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main_package;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Michał Martyniak
 */
public class DataBaseConnector {
    //should be visible to other classes, but readonly
    public final Connection connection;

    
    
    /*  establishes connection to a database with given parameters  */
    //TODO add in-code automatic DB tables creating(not manually in netbeans -> wrong way)
    public DataBaseConnector(String DBName, String userName, String password) {
        Connection _connection = null; //local variable
        
        try{ 
            //load a driver -> read catch ClassNotFound exeption content if you have any problems
            Class.forName("org.apache.derby.jdbc.ClientDriver");
        
            //try to connect with creditentials
            _connection = DriverManager.getConnection("jdbc:derby://localhost:1527/" + DBName, userName, password);
            
            //Initially, always delete all DB records
            Statement stmt = _connection.createStatement();
            String sql = "DELETE FROM MICZI.WORD_PAIRS WHERE OCCURENCES_COUNTER>=1";
            stmt.executeUpdate(sql);
        }catch(SQLException e){
            System.err.println("Connection refused or records deletion failed!");
        } 
        catch(ClassNotFoundException e){
            System.err.println("Driver loading problem. Go to project properties and add library JAVA DB to solve this problem");
        }
        //final field assignment (proper value or null)
        connection = _connection;
    }
    
}

package main_package;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Michał Martyniak
 */
public class Main extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        //preparation before showing window content
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Interface.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        InterfaceController interfaceController = (InterfaceController) loader.getController();
        
        //pass important references to interface controller
        interfaceController.setScene(scene);
        interfaceController.setShortcutListener();
        
        
        

        
        stage.setScene(scene);
        stage.show();
        DataBaseConnector dataBaseConnector = new DataBaseConnector("WordMagazine", "miczi", "`123`123");
        
        //call parser actions
        InputFileParser parser = new InputFileParser(dataBaseConnector.connection);
        interfaceController.setConnectionReference(dataBaseConnector.connection);
        
        parser.readFile("S:\\book.txt");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

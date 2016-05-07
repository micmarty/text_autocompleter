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
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Interface.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        InterfaceController ic = (InterfaceController) loader.getController();
        ic.setScene(scene);
        ic.setShortcutListener();
        
        stage.setScene(scene);
        stage.show();
        
        InputFileParser parser = new InputFileParser();
        parser.connectToDatabase("miczi", "`123`123");
        parser.readFile("S:\\book.txt");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainPackage;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author bisra
 */
public class About extends Stage {
    
    public About() {
        BorderPane root = null;
        try{
            root = FXMLLoader.load(getClass().getResource("/FXML/FenetreAbout.fxml"));
        }
        catch(IOException exp){
            exp.printStackTrace();
        }
        Scene scene = new Scene(root, 350, 490);
        this.setTitle("About");
        this.getIcons().add(new Image("/FXML/Images/icone.png"));
        this.setResizable(false);
        this.setScene(scene);
        
    }
}

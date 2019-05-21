/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author bisra
 */
public class FenetreAboutController implements Initializable {

    @FXML private Button closeBtn;
    @FXML public void fermer(){
      ((Stage)this.closeBtn.getScene().getWindow()).close();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
}

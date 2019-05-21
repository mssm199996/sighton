package MainPackage;

import Utilities.DetectionRecorder;
import Utilities.UsefulVariables;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainClass extends Application {
    
    @Override
    public void start(Stage primaryStage) throws IOException{
        UsefulVariables.FENETRE_PRINCIPALE = primaryStage;
        File f = new File(DetectionRecorder.OUTPUT_FILE);
        if(!f.exists()) f.mkdir();
        if(System.getProperty("os.arch").indexOf("64") != -1){
            System.out.println("J'utilise 64bits");
            System.loadLibrary("lib/opencv_java320_64bits");
        }
        else if(System.getProperty("os.arch").indexOf("x86") != -1){
            System.out.println("J'utilise 32bits");
            System.loadLibrary("lib/opencv_java320_32bits");
        }
        else throw new NullPointerException("Aucune DLL ne peut etre utilisée");
        BorderPane root = FXMLLoader.load(getClass().getResource("/FXML/FenetrePrincipale.fxml")); 
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("SightOn");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        Application.setUserAgentStylesheet(STYLESHEET_CASPIAN);
        launch(args);
    }
}

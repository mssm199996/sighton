package Controllers;

import MainPackage.About;
import Utilities.Camera;
import Utilities.CameraMan;
import Utilities.DetectionRecorder;
import Utilities.EmailSender;
import Utilities.Intrusion;
import Utilities.Matrice;
import Utilities.SMSSender;
import Utilities.SerialPortCommander;
import Utilities.UsefulVariables;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

/**
 * FXML Controller class
 *
 * @author MSSM
 */
public class FenetrePrincipaleController implements Initializable {
    
    @FXML private ImageView videoOriginale,videoAlgorithme,imageplyBtn; // Afficheurs de la video + image du bouton start
    @FXML private ListView<String> ListeCameras;
    @FXML private Circle signaleAlgorithme,signaleMouvement; // deux cercles (rouge et bleus)
    @FXML private CheckMenuItem visionerOriginale,visionerAlgorithme; // choix du boutton 2 de la barre d'outils
    @FXML private Slider fpsBarre,slideSensibilite; //
    @FXML private Spinner<Integer> tmpMoyen; // temps moyen d'une video en ms
    @FXML private MenuItem menuItemAbout,menuItemClose;  // les items du menu
    @FXML private Button actualiserBtn,plyBtn; // actualier la liste des caméras - actionner/desactionner l'algorithme
    @FXML private ComboBox<String> comboPort,comboSMS; // Nom du port d'ecriture
    @FXML private CheckBox checkSMS,checkEmail,videoCheck; // deux champs checkable pour envoyer ou non email/sms
    @FXML private TableView<Intrusion> tableHistorique; // tableaux qui contient l'historique des instrusions
    @FXML private TextField smsText,emailText;
    @FXML private BorderPane BorderPane;
    @FXML private SplitPane SplitPane;
    
    private static long WAITING_PERIOD_4_NOTIFICATION = 60_000; // temps qu'on doit attendre avant d'envoyer un autre message/sms
    private Camera cam = null; // la camera
    private CameraMan cameraman = null; // celui qui filme et fait le traitement en meme temps (un thread)
    private DetectionRecorder recorderPrincipale,recorderSecondaire;
    private SerialPortCommander commande = null; // le port vers lequel ecrire
    private SMSSender smsNotifier = null; // l'objet qui fera les notification sms
    private EmailSender emailNotifier = null; // l'objet qui fera les notifications emails
    private int selectedCamera = 0; // la camera selectionnée (modifiable via la liste des cameras)
    private boolean previousStateWasDetection; // est ce que le traitement précédent présentait un mouvement ? ceci sert pour
                                            // ne pas ecrire dans le port continuellement si l'était de l'algorithme 
                                            // ne change pas
    private long lastDetectionTime; // Ceci sert à savoir à quel instant
        // a été le dernier mouvement, pour éviter d'envoyer des sms/msgs continuellement
    // cette méthode est utilisé pour initialiser tout ce dont a besoin le programme pour procéder au traitement
    private int nbPhotosNecessaires = 0,nbPhotosPrises = 0;
    
    // intiliaser les outils qui permettent l'envoie d'sms/email
    public boolean initialiserOutilsSMSEmail(){
        boolean result = true;
        if(this.checkSMS.isSelected())
            if(this.smsText.getText().equals("") || this.smsText.getText() == null){
                result = false;
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        Alert t = new Alert(AlertType.ERROR);
                        t.setTitle("SMS Error");
                        t.setHeaderText("Correspondant not specified");
                        t.setContentText("Set the correspondant mobile phone number then try again");
                        t.showAndWait();
                    }
                });
            }
            else this.smsNotifier = new SMSSender(comboSMS.getValue(),this.smsText.getText());
        if(this.checkEmail.isSelected())
            if(this.emailText.getText().equals("") || this.emailText.getText() == null){
                result = false;
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        Alert t = new Alert(AlertType.ERROR);
                        t.setTitle("E-mail Error");
                        t.setHeaderText("Correspondant not specified");
                        t.setContentText("Set the correspondant E-mail then try again");
                        t.showAndWait();
                    }
                });
            }
            else {
                this.emailNotifier = new EmailSender(this.emailText.getText());
            }
        return result;
    }
    private void initialiserRecorders(){
        double k = 1;
        double fpsMax = Camera.getCameraFPS(this.selectedCamera);
        fpsMax = (int)fpsBarre.getValue() >= fpsMax ? fpsMax : (int)fpsBarre.getValue();
        this.nbPhotosNecessaires = (int)(fpsMax * (int)this.tmpMoyen.getValue() * k);
        this.recorderPrincipale = new DetectionRecorder((int) (k * fpsMax));
        this.recorderPrincipale.setIsCharging(true);
        this.recorderSecondaire = new DetectionRecorder((int) (k * fpsMax));
        this.recorderSecondaire.setIsCharging(false);
    }
    public void initialiserOutils() throws EmptyListCameraException, WrongSelectedCamera{
        this.actualiserCameras();
        if(this.ListeCameras.getItems().isEmpty()) throw new EmptyListCameraException();
        else if((this.ListeCameras.getItems().size() - 1) < this.selectedCamera) throw new WrongSelectedCamera();
        if(!this.initialiserOutilsSMSEmail()) return;
        if(this.videoCheck.isSelected()) this.initialiserRecorders();
        this.previousStateWasDetection = false; // etat précédent neutre
        this.lastDetectionTime = -FenetrePrincipaleController.WAITING_PERIOD_4_NOTIFICATION;// Pourquoi un temps négative? très bonne question... x) regardez un peu en bas
        if(!this.comboPort.getSelectionModel().getSelectedItem().equals("R.A.S")) // Si on a bien choisit un port dans lequel ecrire
            this.commande = new SerialPortCommander(this.comboPort.getSelectionModel().getSelectedItem());
        this.cam = new Camera(this.selectedCamera,(int)fpsBarre.getValue());
        this.cameraman = new CameraMan(this.cam){
            public Matrice background;// la photo de l'instant t-1
            // Boocle <=> avant le lancement des prises de photos continuelles
            @Override
            public void QuoiFaireAvantBoocle() {
                this.background = cam.PrendrePhoto();
                if(videoCheck.isSelected()) recorderPrincipale.addImage(background);
                this.background.traiterImage();
                nbPhotosPrises = 1;
            }
            
            @Override
            public void QuoiFaireApresBoocle(){
                try{
                    Matrice photo = cam.PrendrePhoto(); // la photo de l'instant t
                    nbPhotosPrises++;
                    if(visionerOriginale.isSelected()) // affichage d'une video réelle (non traité
                        videoOriginale.setImage(new Image(new ByteArrayInputStream(Camera.MatToByte(photo, "png"))));
                    else if(videoOriginale.getImage() != null)
                        videoOriginale.setImage(null);
                    
                    if(videoCheck.isSelected()){
                        if(!recorderPrincipale.isBusy() && !recorderSecondaire.isCharging()){
                            recorderPrincipale.addImage(photo);
                            // Si on a remplit les photos suffisement (tmpMoyen)
                            System.out.println(recorderPrincipale.toString());
                            System.out.println(recorderSecondaire.toString());
                            System.out.println("-------\nJe travaille avec le Thread 0");
                            if((nbPhotosPrises >= nbPhotosNecessaires) && recorderPrincipale.getRecordingType()){
                                System.out.println("Je laisse le 0 faire la video");
                                recorderPrincipale.setBusy(true);
                                recorderPrincipale.fabriquerLaVideo();
                                recorderPrincipale.setIsCharging(false);
                                recorderSecondaire.setIsCharging(true);
                                nbPhotosPrises = 0;
                            }
                        }
                        else if (!recorderSecondaire.isBusy() && !recorderPrincipale.isCharging()){
                            recorderSecondaire.addImage(photo);
                            System.out.println(recorderPrincipale.toString());
                            System.out.println(recorderSecondaire.toString());
                            System.out.println("-------\nJe travaille avec le Thread 1");
                            if((nbPhotosPrises >= nbPhotosNecessaires) && recorderSecondaire.getRecordingType()){
                                System.out.println("Je laisse le 1 faire la video");
                                recorderSecondaire.setBusy(true);
                                recorderSecondaire.fabriquerLaVideo();
                                recorderSecondaire.setIsCharging(false);
                                recorderPrincipale.setIsCharging(true);
                                nbPhotosPrises = 0;
                            }
                        }
                    }
                    
                    
                    photo.traiterImage(); // appyez sur CTRL + clique de souris sur la méthode pour voir la description
                    Matrice resultat = photo.soustractionAbs(this.background,slideSensibilite.getMax() - slideSensibilite.getValue()); // meme chose CTRL + souris
                
                    if(visionerAlgorithme.isSelected()) // affichage de la photo traitée
                        videoAlgorithme.setImage(new Image(new ByteArrayInputStream(Camera.MatToByte(resultat, "png"))));
                    else if(videoAlgorithme.getImage() != null)
                        videoAlgorithme.setImage(null);
                    
                    // Si précédement il N Y AVAIT PAS un mouvement et que en ce moment il Y EN A
                    if(!previousStateWasDetection && (Core.countNonZero(resultat) > 0)){
                        if(commande != null)commande.allowsSwitching(); 
                        if(commande != null)commande.switchTheIntrusionState();// je switch la led qui initialement n'est pas allumée
                        //Je colorie en Orange le cercle de l'interface graphique (le signale)  
                        RadialGradient gradient = new RadialGradient(0,0,0,0,25,false,CycleMethod.NO_CYCLE, 
                            new Stop(0, Color.ORANGE),
                            new Stop(1, Color.WHITE)
                        );
                        signaleMouvement.setFill(gradient);
                        previousStateWasDetection = true; // l'etait actuel , c'est une detection de mouvement (pour le tour suivant)
                        if(videoCheck.isSelected()){
                            if(recorderPrincipale.isCharging() && !recorderPrincipale.getRecordingType())
                                recorderPrincipale.setRecordingType(true);
                            else if(recorderSecondaire.isCharging() && !recorderSecondaire.getRecordingType())
                                recorderSecondaire.setRecordingType(true);
                        }
                        envoyerSmsEtMessage(); // c'est parlant
                    }   
                    // Si précédement il Y AVAIT un mouvement et que en ce moment il Y EN A PAS
                    else if(previousStateWasDetection && Core.countNonZero(resultat) == 0){
                        if(commande != null)commande.allowsSwitching();
                        if(commande != null)commande.switchTheIntrusionState(); // je switch la led qui est actuellement allumée
                        // Je colorie le cercle en bleu (signale qui veut dire y a pas de mouvement)
                        RadialGradient gradient = new RadialGradient(0,0,0,0,25,false,CycleMethod.NO_CYCLE,
                            new Stop(0, Color.BLUE),
                            new Stop(1, Color.WHITE)
                        );
                        signaleMouvement.setFill(gradient);
                        previousStateWasDetection = false; // pas de mouvement (pour le tour suivant)
                    }
                    background = photo; // la photo de t-1 devient la photo de t (pour le tour suivant)
                }
                catch(org.opencv.core.CvException exp){ // ceci se leve si le programme tourne et la caméra a été debranchée (Probleme
                    // signalé par Israa) un grand merci à toi :D
                    Platform.runLater(new Runnable() {// celui qui veut comprendre ce que cette ligne veut dire, m'en parle :D
                        @Override
                        public void run() {
                            // une simple alerte par boite de dialogue pour signaler une erreur
                            Alert c = new Alert(AlertType.ERROR);
                            String cam = (ListeCameras.getSelectionModel().getSelectedItem() != null ? 
                                         (ListeCameras.getSelectionModel().getSelectedItem()):
                                         (ListeCameras.getItems().get(0)));
                            actualiserCameras(); // actualiser la liste des camera vu qu'une a été debranchée
                            actionerAlgorithme(); // ceci va arreter l'algorithme vu qu'il est déja lancé
                            c.setTitle("Camera: " + cam.substring(3));
                            c.setHeaderText("Erreur de prise d'image.");
                            c.setContentText("Veuillez vérifier la disponibilité de la camera puis relancez la detection de mouvements.");
                            c.showAndWait();
                        }
                    });
                }
            }
        };
        this.cam.setCameraMan(this.cameraman);
        this.imageplyBtn.setImage(new Image("/FXML/Images/plyOFF.png"));
    }
    
    // methode qui actualise la liste des cameras
    @FXML public void actualiserCameras(){
        this.ListeCameras.getItems().clear();
        ArrayList<String> listeCameras = Camera.getListCameras();
        if(listeCameras.size() == 1){
            this.ListeCameras.getItems().add(1 + "- " + listeCameras.get(0) + "   √");
            this.selectedCamera = 0;
        }
        else{
            byte i = 1;
            for(String device: listeCameras){
                if((i-1) == this.selectedCamera)
                    this.ListeCameras.getItems().add(i + "- " + device + "   √");
                else this.ListeCameras.getItems().add(i + "- " + device);
                i++;
            }
        }
        
    };
    // methode qui actualise la liste des ports
    @FXML public void actualiserPorts(){
        this.comboPort.getItems().clear();
        this.comboSMS.getItems().clear();
        this.comboPort.getItems().add("R.A.S");
        String[] tab = SerialPortCommander.getListOfAvailablePorts();
        this.comboPort.getItems().addAll(tab);
        this.comboPort.getSelectionModel().select(0);
        this.comboSMS.getItems().addAll(tab);
        this.comboSMS.getSelectionModel().select(0);
    }
    //methode qui actionne ou desactive le traitement (l'algorithme)
    @FXML public void actionerAlgorithme(){
        if(this.cameraman == null || !this.cameraman.isFilming()){  try {
            // si on est pas entrain de filmer (l'algo ne tourne pas)
            // préparation de la caisse à ouuutss #Katia
            this.initialiserOutils();
            } catch (EmptyListCameraException | WrongSelectedCamera ex) {
                return;
            }
            if(this.cam != null){ // Ceci sert à tester si les outils ont belle et bien étés initialisés
                this.cameraman.Filmer();
                // on signale que l'algo tourne
                RadialGradient gradient = new RadialGradient(0,0,0,0,25,false,CycleMethod.NO_CYCLE,
                    new Stop(0, Color.GREEN),
                    new Stop(1, Color.WHITE)
                );
                this.signaleAlgorithme.setFill(gradient);
                // je désactive tout les composants de la fenetre qui peuvent nuir s'ils sont modifiables pendant l'execution
                // de l'algorithme
                this.smsText.setDisable(true);
                this.emailText.setDisable(true);
                this.fpsBarre.setDisable(true);
                this.tmpMoyen.setDisable(true);
                this.slideSensibilite.setDisable(true);
                this.ListeCameras.setDisable(true);
                this.checkSMS.setDisable(true);
                this.videoCheck.setDisable(true);
                this.checkEmail.setDisable(true);
                this.comboPort.setDisable(true);
                this.comboSMS.setDisable(true);
            }
        }
        else { // j'arrete la video si je ne suis entrain de filmer
            closeProgram();
            // Je signale que l'algo est arreté
            RadialGradient gradient = new RadialGradient(0,0,0,0,25,false,CycleMethod.NO_CYCLE,
                new Stop(0, Color.RED),
                new Stop(1, Color.WHITE)
            );
            // Je signale qu'il n y a pas de mouvement
            RadialGradient gradient2 = new RadialGradient(0,0,0,0,25,false,CycleMethod.NO_CYCLE,
                new Stop(0, Color.BLUE),
                new Stop(1, Color.WHITE)
            );
            this.signaleAlgorithme.setFill(gradient);
            this.signaleMouvement.setFill(gradient2);
            this.previousStateWasDetection = false; // ceci est clair si vous avez suivit ce qui a précédé
            // j'actionne les composants de la fenetre pour qu'ils puissent etre modifiables à nouveau
            this.smsText.setDisable(false);
            this.emailText.setDisable(false);
            this.fpsBarre.setDisable(false);
            this.tmpMoyen.setDisable(false);
            this.slideSensibilite.setDisable(false);
            this.ListeCameras.setDisable(false);
            this.checkSMS.setDisable(false);
            this.videoCheck.setDisable(false);
            this.checkEmail.setDisable(false);
            this.comboPort.setDisable(false);
            this.comboSMS.setDisable(false);
            this.imageplyBtn.setImage(new Image("/FXML/Images/plyON.png"));
        }
    }
    
    @FXML
    public void changerCamera(){// ceci est parlant
        this.selectedCamera = this.ListeCameras.getSelectionModel().getSelectedIndex();
        this.actualiserCameras();
    }
    
    private void envoyerSmsEtMessage(){
        long t = System.currentTimeMillis();
        if((t - this.lastDetectionTime) >= FenetrePrincipaleController.WAITING_PERIOD_4_NOTIFICATION){ // regarder le commentaire à coté
            // de la variable WAITING_PERIOD_4_NOTIFICATION en haut vous allez comprendre cette méthode
            if(this.checkSMS.isSelected())
                this.smsNotifier.sendNotification();
            if(this.checkEmail.isSelected())
                this.emailNotifier.sendNotification();
            this.tableHistorique.getItems().add(new Intrusion(true));
            this.lastDetectionTime = t;
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UsefulVariables.FENETRE_PRINCIPALE.setOnCloseRequest(new EventHandler<WindowEvent>(){// lorsque l'utilisateur ferme le programme
            // on doit arreter d'abords la video et la communication avec le port s'il y en avait
            // le reste n'est pas important à connaitre
            @Override
            public void handle(WindowEvent event){
                closeProgram();
            }
        });
        
        this.BorderPane.setStyle("-fx-background-image: url('" + "/FXML/Images/escheresque_ste.png" + "'); ");
        this.SplitPane.setStyle("-fx-background-image: url('" + "/FXML/Images/cloth.png" + "'); ");
        
        this.initialiserTableau();
        this.imageplyBtn.setImage(new Image("/FXML/Images/plyON.png"));
        this.comboPort.getItems().add("R.A.S");
        this.comboPort.getItems().addAll(SerialPortCommander.getListOfAvailablePorts());
        this.comboPort.getSelectionModel().select(0);
        this.comboSMS.getItems().addAll(SerialPortCommander.getListOfAvailablePorts());
        this.comboSMS.getSelectionModel().select(0);
        this.actualiserBtn.setStyle("-fx-background-radius: 5em; " +  
                                 "-fx-min-width: 105px; " + "-fx-min-height: 60px;" 
                               + "-fx-max-width: 105px; " + "-fx-max-height: 60px;");
        this.plyBtn.setStyle("-fx-background-radius: 5em; " +  
                           "-fx-min-width: 50px; " + "-fx-min-height: 50px; " + 
                           "-fx-max-width: 50px; " + "-fx-max-height: 50px;");
        this.tmpMoyen.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20, 1800));
        this.tmpMoyen.setEditable(true);
        
        this.actualiserCameras();
        
        this.menuItemClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(cameraman != null)
                    cameraman.ArreterVideo();
                if(commande != null)
                    commande.stopTheCommunication();
                System.exit(0);
            }
        });
        
        this.menuItemAbout.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(UsefulVariables.ABOUT == null)
                    UsefulVariables.ABOUT = new About();
                UsefulVariables.ABOUT.show();
            }
        });
    }  
    
    @FXML private TableColumn tempsIntrusion,numeroIntrusion,dateIntrusion;
    private void initialiserTableau(){
        this.numeroIntrusion.setCellValueFactory(new PropertyValueFactory<Intrusion, Integer>("numeroIntrusion"));
        this.dateIntrusion.setCellValueFactory(new PropertyValueFactory<Intrusion, String>("dateIntrusion"));
        this.tempsIntrusion.setCellValueFactory(new PropertyValueFactory<Intrusion, String>("tempsIntrusion"));
    }
    
    private void closeProgram(){
        if(commande != null){
            commande.stopTheCommunication();
            commande = null;
        }
        if(smsNotifier != null){
            smsNotifier.fermerConnexion();
            this.emailNotifier = null;
        }    
        if(this.emailNotifier != null){
            this.emailNotifier.fermerConnexion();
            this.emailNotifier = null;
        }
        
        new Thread(new Runnable(){
            @Override
            public void run() {
                // quand les deux sont pas occupés => j'ai plu besoin de la cam => je la ferme
                while((recorderPrincipale != null) && 
                        ((!recorderPrincipale.isBusy() && recorderPrincipale.getRecordingType()) || 
                        (!recorderSecondaire.isBusy() && recorderSecondaire.getRecordingType()))); 
                if(cameraman != null){
                    cameraman.ArreterVideo();
                    videoOriginale.setImage(null);
                    videoAlgorithme.setImage(null);
                    cam = null;
                    cameraman = null;
                }
            }
        }).start();
        
        while((recorderPrincipale != null) && (recorderPrincipale.getRecordingType() || recorderSecondaire.getRecordingType())) 
            try { throw new RecordingException(); } 
            catch (RecordingException ex) {}
    }

    private class WrongSelectedCamera extends Exception {
        public WrongSelectedCamera() {
            Alert c = new Alert(AlertType.ERROR);
            c.setTitle("Camera notification");
            c.setHeaderText("Camera selection error.");
            c.setContentText("The right camera  then try again");
            c.showAndWait();
        }
    }

    private class EmptyListCameraException extends Exception{

        public EmptyListCameraException() {
            Alert c = new Alert(AlertType.ERROR);
            c.setTitle("Camera notification");
            c.setHeaderText("Cameras list empty.");
            c.setContentText("The Cameras list is empty, plug a camera and try again");
            c.showAndWait();
        }
    }
    
    public class RecordingException extends Exception{
        public RecordingException(){
            super("Recording se fait !");
            Alert c = new Alert(AlertType.ERROR);
            c.setTitle("Recording notification");
            c.setHeaderText("Recording statement is on.");
            c.setContentText("Recording statement is on... please wait...");
            c.showAndWait();
        }
    }
}
package Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author MSSM
 */
public class SerialPortCommander extends SerialPort implements SerialPortEventListener{

    public static final byte OFF_INTRUSION = 0; // Commande pour signaler la fin de l'intrusion
    public static final byte ON_INTRUSION = 1; // Commande pour alerter une instrusion
    
    public static final byte FIRST_STATE = SerialPortCommander.OFF_INTRUSION; // initialement il n y a pas d'intrusion
    
    private boolean isOnHighState = false; // ceci sert à sauvegarder un etat de la LED dans le programme 
                                         // pour éviter de questionner l'arduino à chaque fois sur son état
    private boolean stateHasToChange = false;  // Ceci sert à autoriser ou non le switching de la led
    private String portName = null;
    
    public SerialPortCommander(String portName) {
        super(portName);
        this.portName = portName;
        try{
            this.openPort();
            this.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            // BAUDRATE <=> nombre bits/s, soit 9600bits/s
            // DATABITS_8 <=> 8 bits d'un seul coup
            // la suite, aucune idée :D
            this.addEventListener(this);
        }
        catch(SerialPortException exp){
            System.out.println("Erreur du Port " + exp.getPortName() + " : "+ exp.getMessage());
            Alert c = new Alert(AlertType.ERROR);
            c.setTitle("Port serie: " + this.portName);
            c.setHeaderText("Erreur d'accès au port.");
            c.setContentText("Veuillez vérifier la disponibilité du port puis relancez la detection de mouvements.");
        }
    }
    
    public void allowsSwitching(){
        this.stateHasToChange = true;
    }
    
    public void denySwitching(){
        this.stateHasToChange = false;
    }
    
    // ceci sert à switcher entre l'état de la led (qui se trouve à la sortie)
    public void switchTheIntrusionState(){
        try{
            if(this.stateHasToChange){
                if(!this.isOnHighState)
                    this.writeByte(SerialPortCommander.ON_INTRUSION);
                else this.writeByte(SerialPortCommander.OFF_INTRUSION);
                this.isOnHighState = !this.isOnHighState;
                this.stateHasToChange = false;
            }
        }
        catch(SerialPortException exp){}
    }
    
    public void stopTheCommunication(){
        try{
            this.isOnHighState = false;
            this.stateHasToChange = false;
            this.writeByte(SerialPortCommander.OFF_INTRUSION);
            this.closePort();
        }
        catch(SerialPortException exp){
            System.out.println("Erreur du Port " + exp.getPortName() + " : "+ exp.getMessage());
        }
    }
    
    public static String[] getListOfAvailablePorts(){
        return SerialPortList.getPortNames();
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
        try{
            System.out.println("L'arduino dit: " + this.readString());
        }
        catch(SerialPortException exp){
            System.out.println("Erreur du Port " + exp.getPortName() + " : "+ exp.getMessage());
        }
    }
}

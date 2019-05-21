package Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SMSSender extends SerialPort implements SerialPortEventListener{
    
    private String numeroCorrespondant;
    private static final String
            COMMAND_REMISE_A_ZERO = "ATZ",
            COMMAND_SMS_MODE_TEXT = "AT+CMGF=1",
            COMMAND_ENVOIE_SMS = "AT+CMGS=";
    
    public SMSSender(String port,String correspondant){
        super(port);
        this.numeroCorrespondant = correspondant;
        try{
            this.openPort();
            this.setParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            this.addEventListener(this);
            System.out.println("J'envoie: " + SMSSender.COMMAND_REMISE_A_ZERO +"\r\n");
            this.writeString(SMSSender.COMMAND_REMISE_A_ZERO + "\r\n");
            this.writeString(SMSSender.COMMAND_SMS_MODE_TEXT + "\r\n");
        }
        catch(SerialPortException exp){
            exp.printStackTrace();
        }
    }
    
    public void sendNotification(){
        try{
            if(this.isOpened()){ 
                System.out.println("going to ");
                Intrusion it = new Intrusion(false);
                this.writeString(SMSSender.COMMAND_ENVOIE_SMS + "\"" + this.numeroCorrespondant + "\"\r\n");
                this.writeString("Intrusion: The " + it.getDateIntrusion() + " at " + it.getTempsIntrusion() + '\032');
            }
        }
        catch(SerialPortException exp){
            Alert t = new Alert(AlertType.ERROR);
            t.setTitle("SMS error");
            t.setHeaderText("SMS connexion opening error");
            t.setContentText("Impossible to open a communication to the specified server");
            t.showAndWait();
            exp.printStackTrace();
        }
    }
    
    public void fermerConnexion(){
        try{
            this.closePort();
        }
        catch(SerialPortException exp){
            exp.printStackTrace();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
        try{
            System.out.println("reponse = " + readString());
        }
        catch(SerialPortException exp){
            exp.printStackTrace();
        }
    }
}

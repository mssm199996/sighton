/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.util.Properties;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
/**
 *
 * @author simo996
 */
public class EmailSender implements Runnable{
    
    private static final String USERNAME = "MAKSIMies857@gmail.com";
    private static final String PASSWORD = "123jump456run";
    private static Session MY_SESSION = null;
    private String recepteur = null;

    public EmailSender(String recipient){
        this.recepteur = recipient;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        
        EmailSender.MY_SESSION = Session.getInstance(props, new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication (){
                return new PasswordAuthentication (EmailSender.USERNAME, EmailSender.PASSWORD);
            };
        });
    }
    
    @Override
    public void run(){
        try {
            Message message = new MimeMessage(EmailSender.MY_SESSION);
            message.setFrom(new InternetAddress("MAKSIMies857@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.recepteur));
            message.setSubject("Intrusion");
            Intrusion it = new Intrusion(false);
            message.setText("Intrusion: The " + it.getDateIntrusion() + " at " + it.getTempsIntrusion());
            Transport.send(message);
        }
        catch (MessagingException ex){
            Platform.runLater(new Runnable(){
                @Override
                public void run(){
                    Alert t = new Alert(Alert.AlertType.ERROR);
                    t.setTitle("Email error");
                    t.setHeaderText("Email connexion opening error");
                    t.setContentText("Impossible to send the e-mail to the specified recipient");
                    t.showAndWait();
                }
            });
            ex.printStackTrace() ;
        }
    }
    
    public void sendNotification(){
        Thread t = new Thread(this);
        t.start();
    }
    
    
    public void fermerConnexion(){
        EmailSender.MY_SESSION = null;
        this.recepteur = null;
    }
}

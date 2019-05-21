/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.util.Calendar;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author simo996
 */
public class Intrusion {
    
    private static int NUMERO_COURANT = 0;
    private SimpleIntegerProperty numeroIntrusion = new SimpleIntegerProperty();
    private SimpleStringProperty dateIntrusion = new SimpleStringProperty(),tempsIntrusion = new SimpleStringProperty();
    
    public Intrusion(boolean tableau){
        Calendar c = Calendar.getInstance();
        int jour = c.get(Calendar.DAY_OF_MONTH);
        int mois = c.get(Calendar.MONTH);
        String annee = Integer.toString(c.get(Calendar.YEAR));
        String heure = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        String minute = Integer.toString(c.get(Calendar.MINUTE));
        String seconde = Integer.toString(c.get(Calendar.SECOND));
        if(tableau)this.numeroIntrusion.setValue(++Intrusion.NUMERO_COURANT);
        this.dateIntrusion.setValue(this.parseDay(jour) + "/" + this.parseMonth(mois) + "/" + annee);
        this.tempsIntrusion.setValue(heure + ":" + minute + ":" + seconde);
    }
    
    private String parseMonth(int mois){
        switch(mois){
            case 0: return "January";
            case 1: return "February";
            case 2: return "March";
            case 3: return "April";
            case 4: return "May";
            case 5: return "June";
            case 6: return "July";
            case 7: return "August";
            case 8: return "September";
            case 9: return "October";
            case 10: return "November";
            case 11: return "December";
        }
        return "Julute"; // #13ème mois
    }
    
    private String parseDay(int jour){
        return (jour <= 9 ? "0" + Integer.toString(jour) : Integer.toString(jour));
    }
        
    public int getNumeroIntrusion() {
        return numeroIntrusion.get();
    }

    public void setNumeroIntrusion(int numeroIntrusion) {
        this.numeroIntrusion.set(numeroIntrusion);
    }

    public String getDateIntrusion() {
        return dateIntrusion.get();
    }

    public void setDateIntrusion(String dateIntrusion) {
        this.dateIntrusion.set(dateIntrusion);
    }

    public String getTempsIntrusion() {
        return tempsIntrusion.get();
    }

    public void setTempsIntrusion(String tempsIntrusion) {
        this.tempsIntrusion.set(tempsIntrusion);
    }
    
}

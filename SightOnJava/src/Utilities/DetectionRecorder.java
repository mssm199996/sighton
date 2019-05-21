/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;

/**
 *
 * @author simo996
 */
public class DetectionRecorder implements Runnable{
    
    private static int NB_VIDEOS = 0,NUM_DETECTER_COURANT = 0;
    private int capaciteVideoReelle = 100,indiceReel = -1,indiceVirtuel = -1,fps;
    private Mat[] tabImagesReelles = new Mat[this.capaciteVideoReelle]; // réelle <=> à gauche #systeme #optique  
    public static final String OUTPUT_FILE = "Videos\\";
    private String idRecorder;
    private boolean isBusy,recordingType,isCharging; // RT = false => tableau
    
    public DetectionRecorder(int fps){
        this.fps = fps;
        this.idRecorder = Integer.toString(DetectionRecorder.NUM_DETECTER_COURANT++);
        this.reinitialiser();
    }
    
    public void reinitialiser(){
        this.indiceReel = -1;
        this.indiceVirtuel = -1;
        this.tabImagesReelles = new Matrice[this.capaciteVideoReelle];
        this.isBusy = false;
        this.recordingType = false;
    }
    
    public void addImage(Matrice mat){
        if(!this.recordingType){ // ajout dans le tableau car il n y a pas d'intrusion
            this.indiceReel = (this.indiceReel == (this.capaciteVideoReelle - 1) ? 0 : this.indiceReel + 1);
            this.tabImagesReelles[this.indiceReel] = mat;
        }
        else{
            String name = OUTPUT_FILE + "ID" + this.idRecorder + "Img" + (++indiceVirtuel) + ".png";
            Imgcodecs.imwrite(name,mat); //ajout dans le dossier
        }
    }
    
    @Override
    public void run(){
        try{
            File file = new File(OUTPUT_FILE + "ID" + this.idRecorder + "Video" + (DetectionRecorder.NB_VIDEOS++) + ".avi");
            Size taille = new Size(640,480);
            VideoWriter recorded = new VideoWriter(file.getAbsolutePath(),VideoWriter.fourcc('X','V','I','D'),this.fps,taille,true);
            recorded.open(file.getAbsolutePath(),VideoWriter.fourcc('X','V','I','D'),this.fps,taille,true);
        
            if (recorded.isOpened()){
                if (tabImagesReelles[indiceReel + 1] != null) // s'il y a detection de mouvement et on a déja remplit 
                    // le tableau au complet au paravant, on doit donc d'abords enregistrer les photo qui se trouve à droite de l'indice
                    for(int i = indiceReel + 1; i < capaciteVideoReelle; i++)
                        recorded.write(tabImagesReelles[i]);
                
                // et maintenant on enregistre tout ce qui se trouve à gauche du tableau
                for(int i = 0; i <= indiceReel ; i++)
                    recorded.write(tabImagesReelles[i]);
            
                // et maintenant on enregistre toutes les photos qui se trouvent sur le dossier en question
                for(int i = 0; i <= indiceVirtuel; i++){
                    recorded.write(Imgcodecs.imread(OUTPUT_FILE + "ID" + this.idRecorder + "Img" + i + ".png"));  
                }
                recorded.release();
                for(int i = 0; i <= indiceVirtuel; i++){
                    try{Files.deleteIfExists(Paths.get(OUTPUT_FILE + "ID" + this.idRecorder + "Img" + i + ".png"));}
                    catch(IOException exp){exp.printStackTrace();}
                }
                this.reinitialiser();
            }
        }
        catch(java.lang.Exception exp){exp.printStackTrace();}
    }
    
    public void setBusy(boolean b){
        this.isBusy = b;
    }
    
    public boolean isBusy(){
        return this.isBusy;
    }
    
     
    public void fabriquerLaVideo(){
        new Thread(this).start();
    }
    
    public boolean getRecordingType() {
        return recordingType;
    }

    public void setRecordingType(boolean recordingType) {
        this.recordingType = recordingType;
    }
    
    public boolean isCharging() {
        return isCharging;
    }

    public void setIsCharging(boolean isCharging) {
        this.isCharging = isCharging;
    }
    
   @Override
   public String toString(){
       return "----\nRecorder " + this.idRecorder + "\nisBusy: " + this.isBusy + "\nisCharging: " + this.isCharging + "\nRecordingType: " + this.recordingType;
   }
}

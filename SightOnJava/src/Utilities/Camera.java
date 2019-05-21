package Utilities;

import com.googlecode.javacv.cpp.videoInputLib.videoInput;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
/**
 *
 * @author MSSM
 */
public class Camera extends VideoCapture{
    
    private static int NB_CAMERA_OUVERTES = 0,CAMERA_COURANTE = -1,SAFETY_PICTURES = 9; 
    private int CameraID,FrameRate;
    private CameraMan CameraMan;
    
    public Camera(int ID){
        super();
        this.CameraID = ID;  
        Camera.CAMERA_COURANTE = ID;
        Camera.NB_CAMERA_OUVERTES++;
    }
    public Camera(int ID,int fr){
        this(ID);
        this.FrameRate = fr;
    }
    
    public synchronized boolean isSafe(){
        // Pour prendre une photo il faut d'abords que la caméra ait prise un certains nombre de photos.
        for(int i = 0; i < Camera.SAFETY_PICTURES; i++)
            this.PrendrePhoto();
        return true;
    }

    public synchronized Matrice PrendrePhoto(){
        Matrice image = new Matrice();
        while(!this.isOpened())
            this.open(this.CameraID);
        this.read(image);
        image.convertTo(image, CvType.CV_8U); // on converti l'image prise en une matrice dont les nombre sont variables 
        return image;
    }
    
    public int getCameraID() {
        return CameraID;
    }
    public int getFrameRate() {
        return FrameRate;
    }
    public void setFrameRate(int FrameRate) {
        this.FrameRate = FrameRate;
    }
    public void setCameraID(byte CameraID) {
        this.CameraID = CameraID;
    }
    public void setCameraMan(CameraMan cameraman){
        this.CameraMan = cameraman;
    }
    
    public synchronized void FermerCamera(){
        Camera.CAMERA_COURANTE = this.CameraID;
        Camera.NB_CAMERA_OUVERTES--;
        this.release();
    }
    
    public synchronized void EnregistrerPhoto(String chemin,String format,Matrice image) throws org.opencv.core.CvException{
        try{
            BufferedImage bImageFromConvert = ImageIO.read(
                    new ByteArrayInputStream(Camera.MatToByte(image, format)));
            ImageIO.write(bImageFromConvert, format, new File(chemin));
        }
        catch(IOException exp){
            exp.printStackTrace();
        }
    }
    
    public static synchronized byte[] MatToByte(Matrice image,String formatEncodage) throws org.opencv.core.CvException{
        MatOfByte matb = new MatOfByte();
        Imgcodecs.imencode("." + formatEncodage, image, matb);
        return matb.toArray();
    }     
    
    public static ArrayList<String> getListCameras(){
        ArrayList<String> resultat = new ArrayList<String>();
        int nbDevice = videoInput.listDevices();
        for (int i = 0; i < nbDevice; i++)
            resultat.add(videoInput.getDeviceName(i));
        return resultat;
    }
    
    public static int getCameraFPS(int i){
        VideoCapture capture = new VideoCapture();
        capture.open(i);
        long t0 = System.currentTimeMillis();
        int k = 0;
        while(System.currentTimeMillis() - t0 < 1000){
            k++;
            capture.read(new Mat());
        }
        capture.release();
        return k;
    }
}

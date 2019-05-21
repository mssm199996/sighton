package Utilities;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author simo996
 */

public class Matrice extends Mat{
    
    public Matrice(){
        super();
    }
        
    public void traiterImage(){
        Imgproc.cvtColor(this, this, Imgproc.COLOR_RGB2GRAY); // conversion en gris
        Imgproc.medianBlur(this, this, 3); // application du filtre median
    }
    // ------------------------ Fonctions qu'on utilise ------------------------    
    public Matrice soustractionAbs(Matrice m,double value){
        Matrice result = new Matrice();
        Core.absdiff(this, m, result); // difference entre les deux photos en valeur absolue
        double k = Imgproc.threshold(result, new Mat() ,0, 255,Imgproc.THRESH_OTSU); // ceci sert à chercher le seuil d'otsu
        Imgproc.threshold(result, result , k + value, 255, Imgproc.THRESH_BINARY); // on applique maintenant la binarisation avec 
        // le seul d'otsu + une valeur qu'on choisi nous meme (il faut en choisir une > 5 , sinon le rendu n'est pas satisfaisable)
        Imgproc.erode(result,result, new Mat()); // l'erosion
        return result;
    }
    // ---------------------- Fonctions faites à la main -----------------------
    // ceci sont des fonctions codées à la main, merci à Israa et Soumia pour ce joli travail
    // quoi que, ils sont carrément inutiles à cause de la SIMD Optimization
    // sinon c un bon travail (y)
    // PS: j'expliquerai rien par la suite, carrément pas la peine :D
    public void filtrerMedianement(short[] matrice){
        byte channels = (byte)this.channels();
        int columns = this.cols();
        int rows = this.rows();
        int pointArret = channels * (columns * (rows - 1) - 1);
        for(int i = channels * (columns + 1); i < pointArret; i += channels){
            if(((i + channels) % (channels * columns)) == 0) i += channels;
            else{
                short[] sousMatrice = {
                    matrice[i - (columns + 1) * channels],
                    matrice[i - columns * channels],
                    matrice[i - (columns - 1) * channels],
                    
                    matrice[i - channels],
                    matrice[i],
                    matrice[i + channels],
                    
                    matrice[i + (columns - 1) * channels],
                    matrice[i + columns * channels],
                    matrice[i + (columns + 1) * channels],
                };
                // Tri
                for(int a = 0; a < sousMatrice.length - 1; a++)
                    for(int b = a + 1; b < sousMatrice.length; b++)
                        if(sousMatrice[a] > sousMatrice[b]){
                            short k = sousMatrice[a];
                            sousMatrice[a] = sousMatrice[b];
                            sousMatrice[b] = k;
                        }
                matrice[i] = matrice[i+1] = matrice[i+2] = sousMatrice[4];
            }
        }
    }
    
    public void convertirtEnGris(short[] matrice){
        for(int i = 0; i < matrice.length; i += 3){
            short r = matrice[i];
            short g = matrice[i+1];
            short b = matrice[i+2];
            short v = (short)(0.21 * r + 0.72 * g  + 0.07 * b);
            matrice[i] = matrice[i+1] = matrice[i+2] = v;
        }
    }
    
    public byte[] binarisationImage() {
        short[] image = this.toArray();
        int seuil;
        int[] histogramme = new int[256] ;
	int i;
        
        for (i=0;i < histogramme.length; i++) //initialisation de l'histogramme
            histogramme[i++] = 0;
                
        int pixelsImage = image.length;
        for(i = 0;i < pixelsImage ;i++){     //Construction de l'histogramme 
            int t = 0xFF & image[i];
            histogramme[t]++;
        }
                
        float sommePixels = 0;            // somme(xi*ni)
	for (i = 0; i < 256; i++)
	    sommePixels += i * histogramme[i];

	float sommePixelsB = 0;
	int wB = 0; // weight Background== somme des pixels du Background
	int wF ; // weight Foreground ...
        float mB;   // moyenne Background
        float mF;   // moyenne Foreground
        float varianceMax = 0; 
        float betweenClassVariance ;
	seuil = 0;

	for (i = 0; i < 256; i++) {
            wB += histogramme[i]; 
	    wF = pixelsImage - wB; 
	    sommePixelsB += i * histogramme[i];
            mB = sommePixelsB / wB; 
	    mF = (sommePixels - sommePixelsB) / wF;
            betweenClassVariance =  wB * wF * (mB - mF) * (mB - mF);
            
            if (betweenClassVariance > varianceMax) {
		 varianceMax = betweenClassVariance;
		 seuil = i;
	    }
	}

	byte[] imageBinaire = new byte[image.length];
        for(i = 0; i<pixelsImage; i++){ // pixelsImage==image.length
            if ((0xFF & image[i]) >= seuil) 
                imageBinaire[i] = 1;
            else imageBinaire[i] =0;
        }
	return imageBinaire;
    }
    
    public void EclairerPhoto(double gamma){
        for(int i = 0; i < this.rows(); i++){
            for(int j = 0; j < this.cols(); j++){
                double[] pixel = this.get(i, j);
                pixel[0] = (Math.pow((pixel[0] / 255), (1.0/gamma)))*255;
                pixel[1] = (Math.pow((pixel[1] / 255), (1.0/gamma)))*255;
                pixel[2] = (Math.pow((pixel[2] / 255), (1.0/gamma)))*255;
                this.put(i, j, pixel);
            }
        }
    }
    //--------------------------- Fonction utiles ------------------------------
    public short[] toArray(){
        int bufferSize = this.channels() * this.cols() * this.rows();
        short [] buffer = new short[bufferSize];
        this.get(0, 0, buffer);
        return buffer;
    }
    
    public void initializeWithShort(short[] buffer){
        this.put(0, 0, buffer);
    }
    //----------------- Ca c'est pour faire des testes -------------------------
    
    public void remplir(){
        int columns = this.cols();
        int rows = this.rows();
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                short[] data = new short[this.channels()];
                for(int k = 0; k < this.channels(); k++){
                    data[k] = (short)(255 * Math.random());
                }
                this.put(i, j, data);
            }
        }
    }
    
    public void afficher(){
        int columns = this.cols();
        int rows = this.rows();
        int channels = this.channels();
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                double[] data = this.get(i, j);
                System.out.print("(");
                for(int k = 0; k < channels; k++){
                    System.out.print((k == (channels - 1) ? (short)data[k] : (short)data[k] + ","));
                }
                System.out.print(")   ");
            }
            System.out.println();
        }
    }
}

package Utilities;

/**
 *
 * @author MSSM
 */
public abstract class CameraMan extends Thread{
    private SophisticatedBoolean filmer; // Boolean qui permet l'accès (W/R) par plusieurs threads
    private Camera outil;
    
    public CameraMan(Camera outil){
        super();
        this.outil = outil;
        this.filmer = new SophisticatedBoolean(false);
    }
    
    @Override
    public synchronized void run() {
        this.QuoiFaireAvantBoocle();
        while(this.filmer.getValeur()){
            try{
                this.QuoiFaireApresBoocle();
                this.wait((1000/this.outil.getFrameRate()));
            }
            catch(InterruptedException exp){
                exp.printStackTrace();
            }
        }
        this.outil.FermerCamera();
    }
    
    public synchronized void ArreterVideo(){
        this.filmer.setValeur(false);
    }
    
    public abstract void QuoiFaireApresBoocle();
    public abstract void QuoiFaireAvantBoocle();
    
    public void Filmer(){
        this.filmer.setValeur(true);
        this.start();
    }
    
    public synchronized boolean isFilming(){
        return this.filmer.getValeur();
    }
    
    public class SophisticatedBoolean {
        private boolean valeur;
        
        public SophisticatedBoolean(boolean valeurInitiale){
            this.valeur = valeurInitiale;
        }
        public synchronized boolean getValeur(){
            return this.valeur;
        }
        public synchronized void setValeur(boolean valeur){
            this.valeur = valeur;
        }
    }
}

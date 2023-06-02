/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carRace.carPerception;

import carRace.game.CarView;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;
import telemedicina.XMLInterface;

/**
 *
 * @author barbaradraghi
 * La classe CarPerception rappresenta la percezione che il supervisor ha degli agenti Car.
 * Ogni volta che un agente Car viene creato, questo invia un NotifyMessage al supervisor, avvisandolo e inviandogli l'id.
 * In questo modo, il supervisor potrà creare al suo interno una HashMap contenente come chiavi gli ID degli agenti, e come
 * oggetto una sorta di "gemello" della classe Car. Si tratterà quindi di un oggetto appartenente ad una classe in cui vengono
 * riportati attributi che contiene anche la macchina, con la differenza, però, che qui non si tratta di agenti.
 * Per questo parliamo di Percezione che il supervisor ha della macchina.
 * Una CarPerception contiene:
 * - ID (con cui l'agente corrispondente si è connesso).
 * - L'ultima richiesta effettuata da quell'agente.
 * - L'ultima posizione calcolata per quell'agente.
 * - L'ultima velocità con cui CarPerception si stava muovendo.
 * 
 * Queste caratteristiche sono state pensate in vettori, usando quindi la classe Vector2d, in modo da rendere più verosimile
 * l'applicazione delle leggi fisiche e il raggiungimento di una coordinata a partire da un'altra applicando una certa forza.
 * 
 */
public class CarPerception {
    
    private String carID;
 //private Point carPosition;
    private XMLInterface lastRequest;
    private  Vector2d  lastForce;
    private Vector2d lastVelocity;
    private final Vector2d maxVelocity;
    private Point2D.Double lastPosition;
    private Vector2d lastPositionVector;
    private double mass;
    private CarView carView;
    private int carWidth;
    private int carHeight;
    private double lastAngle;
    //private Vector2d finalPosition;
    private BufferedImage img ;
    
    // Costruttore
    /**
     * 
     * @param id 
     */
    public CarPerception(String id){
        carID = id;
        lastVelocity = new Vector2d(0,0);
        //lastPosition = new Point2D.Double(304,110);
        lastPositionVector= new Vector2d(0,0);
        carHeight= 25;
        carWidth = 15;
        maxVelocity= new Vector2d(carHeight/10,carHeight/10);
        lastAngle = 0;
        
    }
    
     public String getCarID() {
        return carID;
    }

    
    public XMLInterface getLastRequest() {
        return lastRequest;
    }
    
    public void svuotaRequest(){
        lastRequest.clear();
    }
    

    public Vector2d getLastForce() {
        return lastForce;
    }

    public Vector2d getLastVelocity() {
        return lastVelocity;
    }
    

    /**
     * Trasformo il vettore posizione in un punto posizione.
     * Questo servirà per tutti gli altri metodi che non siano quello di applicazione di leggi fisiche.
     * @return 
     */
    public synchronized Point2D.Double getLastPosition() {
//        Point p;
//        p = new Point((int)this.lastPositionVector.x, (int)this.lastPositionVector.y);
        return lastPosition;
    }

    
    public Vector2d getMaxVelocity() {
        return this.maxVelocity;
    }

     
    // All'inizio potremmo settarla a 0,0
    public void setLastVelocity(Vector2d lastVelocity) {
        this.lastVelocity.set(lastVelocity);
    }

    /**
     * Setto l'ultima posizione occupata dalla macchina.
     * Setto sia il punto che il vettore.
     * Il vettore serve per l'applicazione della fisica.
     * @param lastPosition 
     */
    public  void setLastPosition(Point2D.Double lastPosition) {
        this.lastPosition = lastPosition;
    }

    public  void setLastPositionVector(Vector2d lastPositionVector) {
        this.lastPositionVector.set(lastPositionVector);
    }
    
    public  void setLastPositionVector(Point2D.Double lastPosition){
        lastPositionVector = new Vector2d(lastPosition.x, lastPosition.y);
    }
    
    public Vector2d getLastPositionVector() {
         return lastPositionVector;
    }


    public int getCarWidth() {
        return carWidth;
    }

    public int getCarHeight() {
        return carHeight;
    }

    public double getLastAngle() {
        return lastAngle;
    }
    
    

    public void setLastRequest(XMLInterface lastRequest) {
       this.lastRequest = lastRequest;
    }
    
    
    
    public  void setLastVelocityX(double x){
        this.lastVelocity.x = x;
    }
    
    public BufferedImage loadImage(String path) { 
        BufferedImage bimg = null;
        try {
            bimg = ImageIO.read(new File(path));
            System.out.println("immagine trovata");
        } catch (IOException e) { 
            System.out.println("immagine non trovata");
        }
        return bimg ; 
    }

    public BufferedImage getImg() {
        return img;
    }
    
    

    public void setLastVelocityY(double y){
        this.lastVelocity.y = y;
    }
    
    
}

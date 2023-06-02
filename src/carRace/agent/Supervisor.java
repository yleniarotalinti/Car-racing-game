package carRace.agent;

import carRace.game.*;
import carRace.carPerception.*;
import carRace.gui.*;
import carRace.threads.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.StringTokenizer;
import javax.vecmath.Vector2d;
import telemedicina.Connection;
import telemedicina.NotifyMessage;
import telemedicina.XMLInterface;

public class Supervisor extends BaseAgent implements Observer {

    private Map <String, CarPerception> myCars;
    public carRace.game.RaceTrack raceTrack;
    private boolean flagFirstActivityMovement;
    private double AngoloPrecedente = 0;
    boolean lastActivitySteering = false;
    boolean lastActivityAcceleration = false;
    private Vector2d lastAcceleration;
    private boolean flagStartButton;
    private boolean flagStartRace;
    CalculateCarPosition calculateCarPosition;
    SendCarPosition sendCarPosition;

    public Supervisor(Connection conn, boolean debug) {
        super(conn, debug);
        initVariables();
        myCars = new HashMap<>();
        initFlag();
        initThread();
    }
    
    private void initVariables(){

        AngoloPrecedente = 0;
        lastActivitySteering = false;
        lastActivityAcceleration = false;
        lastAcceleration = new  Vector2d();
    }
    private void initThread(){
        calculateCarPosition = new CalculateCarPosition(this); 
        sendCarPosition = new SendCarPosition(this);
    }
    private void initFlag(){
        flagFirstActivityMovement = false;
        flagStartButton = false;
        flagStartRace = false;
    }
    /*
     * RUN. Appena il thread del supervisor viene lanciato dal main, viene fatto
     * partire il thread di aggiornamento della racetrack. Inizia poi il ciclo
     * perenne, che si occupa di recuperare le activity e di svolgerle,
     * smistandole a seconda dell'etichetta. Lo smistamento delle activity viene
     * fatto nel metodo gestioneActivity(XMLInterface).
     */
    @Override
    public void run() {

        RaceTrackUpdate.setPreviousTime(System.currentTimeMillis());
        new RaceTrackUpdate(this).start();

        while (true) {
            System.out.println("Supervisor RUN");
              System.out.println(" Il Thread che gira è " + Thread.currentThread());
            XMLInterface task = nextActivity();
            showMsg("SUP: "
                    + " Activity: " + task.getProperty("Activity")
                    + " richiesta da: " + task.getProperty("Scheduler")
            + " tempo" + System.currentTimeMillis()/1000);

            gestioneActivity(task);

        }
    }
    
    

    /**
     * GESTIONE ACTIVITY. Fa tutte le azione necessarie per la gestione di
     * un'Activity: 1) Se l'activity ricevuta è DO_BIRTH invoca il metodo
     * doBirth 2) Se l'activity non è DO_BIRTH: - Controlla se è la prima
     * activity di movimento ad essere arrivata, se è così fa partire il thread
     * grafica - Salva l'activity in una stringa, invia la notifica e dopo la
     * processa - Deve mettere l'activity all'interno di CarPerception, in modo
     * da gestirla nel thread che gira ogni 0.1
     */
    private void gestioneActivity(XMLInterface task) {
        if (task.getProperty("Activity").equals(SUP_CAR_BIRTH)) {
            doBirth(task);
        } else if (task.getProperty("Activity").equals("startup")) {
            sendOnlyNotify(task,"COMPLETED");
        }else if( task.getProperty("Activity").equals(SUP_NOTIFY_INITIAL_VIEW)){
            flagStartButton = true;
            sendOnlyNotify(task, "COMPLETED");
        }  else {
            doActivityMovement(task);
        }
    }
    
    
    /**
     * DO ACTIVITY MOVEMENTE
     * @param task 
     * Questo metodo si occupa di gestire le activity di movimento inviate dalla macchina.
     *Per prima cosa vede se controllare se la gara è iniziata attraverso il flagStartRace. Se questo è vero allora controlla attraverso il flag First activity movement
     * se è la prima richiesra di movimento fatta dalla macchina. Un altro controllo effettuato è relativo
     * ai thread, si usa questo controllo per la gestione dei tasti start e resume. Quando si preme il 
     * tasto resume i thread vengono interrotti. Il metodo controlla se è la prima volta che viene invocato, se è così 
     * i thread vengono fatti partire, se invece sono già partiti, significa che sono nello stato interrotto e vengono 
     * fatti ripartire.
     * Se la gara non è ancora partita, ovvero non è stato premuto il tasto START, l'activity viene rifiutata
     * 
     */
    private void doActivityMovement( XMLInterface task){
        if(flagStartRace){
                if (flagFirstActivityMovement) {
                    if( calculateCarPosition.isAlive() && sendCarPosition.isAlive()){
                        resetThread();
                        flagFirstActivityMovement = false;
                    }else if(!calculateCarPosition.isAlive() && !sendCarPosition.isAlive()){
                        startThread(); 
                        flagFirstActivityMovement = false;
                    }    
                }
                getMyCars().get(task.getProperty("Scheduler")).setLastRequest(task);
                sendOnlyNotify(task, "COMPLETED");
        }else//devo rifiutare l'activity che mi è arrivata
        {
            sendOnlyNotify(task, "REFUSED");
        }
    }
  
    
    
    
    /**RESET THREAD.
     * Si occupa di far ripartire i thread precedentemente interrotti. 
     * L'interruzione dei thread si ha quando viene premuto il bottone RESUME.
     */
    private void resetThread(){
        CalculateCarPosition.setPreviousTime(System.currentTimeMillis());
        calculateCarPosition.exitFromStateInterrupt();
        SendCarPosition.setPreviousTime(System.currentTimeMillis());
        sendCarPosition.exitFromStateInterrupt();
    }


    /**
     * CREAZIONE NOTIFY MESSAGE. Questo metodo crea il notify message con le
     * proprietà che gli vengono passate in input. Dopodichè invia il messaggio
     * al server ed effettua un check dello stato del messaggio.
     */
    
    private void sendOnlyNotify(XMLInterface task, String state) {
        NotifyMessage nmsg = new NotifyMessage(task.getProperty("Activity"),
                task.getProperty("Serial"),
                state,
                null);

        conn.send(nmsg);
        checkStatus(nmsg, true);
    }
    
    /**
     * Metodi che interagiscono con il metodo che richiama la fisica.
     *
     * @param payload
     * @return
     */
    
    private Point doAccelera(String payload) {
        //lo passo al metodo fisico
        //ritorna la posizione
        return null;
    }

    private Point doFrena(String payload) {
        //lo passo al metodo fisico
        //ritorna la posizione
        return null;
    }

    private Point doTurnLeft(String payload) {
        //lo passo al metodo fisico
        //ritorna la posizione
        return null;
    }

    private Point doTurnRigth(String payload) {
        //lo passo al metodo fisico
        //ritorna la posizione
        return null;
    }

    private Point doGoOn(String payload) {
        //lo passo al metodo fisico
        //ritorna la posizione
        return null;
    }

    public void sendCarActivity(NotifyMessage nmsg, String payload, String id) {
        nmsg.addNext(CAR_UPDATE_POSITION_VIEW, payload, id);
        conn.send(nmsg);
        showMsg("Ho inviato l'activity " + payload + "a: " + id + " tempo" + System.currentTimeMillis()/1000);
        checkStatus(nmsg, true);
    }

    private synchronized void addCar(CarPerception car, String id) {
        myCars.put(id, car);
        System.out.println("Massima velocita quando aggiungo la macchina" 
         + car.getMaxVelocity());
        raceTrack.addCar(car);
    }

    /**
     * CALCOLO DEL RAGGIO DI VISIONE DELLA MACCHINA. Questo metodo consente di
     * calcolare la nuova visuale della macchina a seconda della posizione in
     * cui si trova Radius: è il numero di paletti che la macchina vede davanti
     * a sè, bisognerà dimensionarlo opportunamente. Metodo
     * raceTrack.checkPosition( PointMatching p, Point carPosition ):
     * implementato in raceTrack e trova i paletti tra cui si trova la macchina
     * in questo momento. In base a questi posso così calcolare quali sono i due
     * paletti successivi.
     *
     * @param car
     * @return
     */
    
    private ArrayList<PointMatching> findCarView(CarPerception car) {
        boolean flagInitialView = false;
        int i = 0;
        int radius = 4;
        ArrayList<PointMatching> view = new ArrayList<>();
       
       
        PointMatching carPaletti = raceTrack.checkPosition(car.getLastPosition());
  
       for(int j=0; j < raceTrack.getSelectedList().size(); j++)
       {
           
           PointMatching pointList = raceTrack.getSelectedList().get(j);
       
            if ( (pointList.getPin().x == carPaletti.getPin().x)
                  &&
                  (pointList.getPin().y == carPaletti.getPin().y)
                  && 
                  (pointList.getPout().x == carPaletti.getPout().x)
                  &&
                  (pointList.getPout().y == carPaletti.getPout().y)
                )
            {
                
                 flagInitialView = true;
            
            }
    
           if (flagInitialView) {
               if (i <= radius) {
                   view.add(pointList);
                   i++;

                   // se raggiungo la fine dell'array dovrei riprendere a scorrere dall'inizio
                   if (j == raceTrack.getSelectedList().size() - 1) {

                       j = 0;

                   }

               } else {
                   break; //esco dal ciclo quando ho completato la mia lista
               }

           }
        }
        return view;
    }

    
    
    private String viewToString(ArrayList<PointMatching> view) {
        String s = "";
        String pout;
        String pin;
        for (PointMatching pointMatching : view) {
            pin = pointMatching.getPin().x + " " + pointMatching.getPin().y + ";";
            pout = pointMatching.getPout().x + " " + pointMatching.getPout().y
                    + ";";
            s += pin + pout;

        }
        System.out.println("Stampo dal viewToString il raggio: " + s);
        return s;
    }

    /**
     * UPDATE. Smista gli eventi che arrivano dall'oggetto Osservato, ovvero la
     * inner class presente nella GUI. Se l'evento è: - Viene premuto il bottone
     * start: il supervisor, attraverso l'invio di un'activity, notifica alle
     * macchine che il gioco è partito - Viene premuto il bottone per la
     * chiusuta della GUI: il supervisor attraverso l'invio di un'activity,
     * notifica alle macchine che il gioco è terminato
     *
     * @param o
     * @param o1
     */
    @Override
    public void update(Observable o, Object o1) {
        EventObject evt = (EventObject) o1;

        if (evt instanceof GUI.StartEvent) {
            if(flagStartButton){
                sendStartGame();
                flagStartButton = false;//ovvero significa che start è stato premuto
            }
        } else if (evt instanceof GUI.EndEvent) {
            doDeath();
        }else if(evt instanceof GUI.ResumeEvent){
            doResumeButton();
        }
    }
    
    /**
     * DO RESUME BUTTON.
     * Questo metodo si occupa della gestione del tasto Resume. 
     * Quando viene premuto il RESUME: 
     * -    Il flagStartRace viene settato a false perchè la gara viene fatta ripartire.
     * -    I thread vengono interrotti
     * -    Vengono resettate le condizioni iniziali del supervisor
     * -    Le macchine in myCars vengono resettate con le condizioni iniziali
     * -    Vengono inviate le activity con posizione iniziale agli agenti macchina
     */
    private void doResumeButton(){
        flagStartRace = false;
        interruptThread();
        initVariables();
        Set<String> keySet = getMyCars().keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext()){
            Object key = it.next();
            CarPerception car = getMyCars().get((String)key);
            resetCar(car);
            sendInitialPosition(new NotifyMessage(), key, doPayload(car));
        }
    }
    
    
    
    private void interruptThread(){
        calculateCarPosition.interruptThread();
        sendCarPosition.interruptThread();
    }
    
    /**
     * Reset car
     * @param car
     * 
     * Si occupa di resettare tutto come quando il programma parte
     */
    private void resetCar( CarPerception car){
        setInitialPosition(car);
        car.setLastVelocity( new Vector2d(0,0));
        car.setLastRequest(null);
    }
    

    
    
    private void doDeath() {
        NotifyMessage nmsg = new NotifyMessage();
        nmsg.addNext(CAR_END_GAME, null, "ALL");
        conn.send(nmsg);
    }

    /**
     * DO BIRTH. Questo metodo si occupa di gestire l'activity DO_BIRTH: - Crea
     * un'istanza di CarPerception - La aggiunge alla lista delle macchine -
     * Calcola la posizione iniziale in cui la macchina si dovrebbe trovare -
     * Calcola il raggio di visione - Invia la notifica a cui aggiunge
     * l'activity CAR_INITIAL_POSITION_VIEW
     */
    
    private void doBirth(XMLInterface task) {
        CarPerception myCar = new CarPerception(task.getProperty("Scheduler"));

        setInitialPosition(myCar); 
       
        addCar(myCar, task.getProperty("Scheduler"));
        NotifyMessage nmsg = new NotifyMessage(task.getProperty("Activity"),
                task.getProperty("Serial"),
                "COMPLETED",
                null);
        sendInitialPosition( nmsg,task.getProperty("Scheduler"), doPayload(myCar));
    }
    
    /**
     * SET INITIAL POSITION
     * @param car 
     * 
     */
    private void setInitialPosition(CarPerception car ){
        Point2D.Double initialPosition = calculateInitialPosition();
        car.setLastPosition(initialPosition);
        car.setLastPositionVector(initialPosition);
    }
    
   

    /**
     * CREAZIONE DEL PAYLOAD. Questo metodo consente la creazione del payload
     * che deve essere poi inviato nelle richieste da parte di Car :
     * accelerazione, frenatura ecc. I parametri necessari per la creazione del
     * payload sono: Posizione,Velocità,Raggio di visione.
     *
     * @param car
     * @return
     */
    
    public String doPayload(CarPerception car) {
        String position = car.getLastPosition().x + " " + car.getLastPosition().y + ";";
        String velocity = car.getLastVelocity().x + " " + car.getLastVelocity().y + ";";
        ArrayList<PointMatching> view = findCarView(car);
        //System.out.println(position + velocity + viewToString(view));

        return position + velocity + viewToString(view);
    }
    
    
    /* 
    La posizione iniziale della macchina è tra i primi paletti (interno,esterno)
    della pista. Per ora funziona solo inserendo tre macchine e le spazia in
    orizzontale di una soglia fissa.
    */

    private Point2D.Double calculateInitialPosition() {
        int i;
        if(getMyCars().isEmpty())
            i = 1;
        
        
        i = getMyCars().size()+1;
        int treshold = 5;
        int carDimension = 15; //poi dovrà essere presa carHeight in carPerception
        int k = treshold + carDimension; 
        
        int numPointMatching = raceTrack.getSelectedList().size();
        PointMatching pm = raceTrack.getSelectedList().get(0);
        Point2D.Double pin = pm.getPin();
        Point2D.Double pout = pm.getPout();
       
        int numCars = (int) pin.distance(pout)/k;
        
        
        int n = (int) (i-1)/numCars;
        if(n == 0){
            pm = raceTrack.getSelectedList().get(0);
        } else {
            pm = raceTrack.getSelectedList().get(numPointMatching-n*300);

        }
        
       
//         ArrayList<Point2D.Double> points = pm.betweenPoint();
        double x=0;
        double y=0;

        
        Vector2d v = calculateInitialDirection();

        if (v.x == 0) {
            //nel punto di inizio la pista è verticale
            if(pm.getPin().getX()>pm.getPout().getX()){
                x = pm.getPin().getX() - (numCars-i%numCars)*k;
                y = pm.getPin().getY();  
            } else {
                x = pm.getPin().getX() + (numCars-i%numCars)*k;
                y = pm.getPin().getY();  
            }
            
        } else if (v.y == 0) {
            if(pm.getPin().getY()>pm.getPout().getY()){
                x = pm.getPin().getX();
                y = pm.getPin().getY() - (numCars-i%numCars)*k;
            } else {
                x = pm.getPin().getX();
                y = pm.getPin().getY() + (numCars-i%numCars)*k;
            }
        } else {
            //dobbiamo contemplare il caso in cui la macchina parta in curva
        }
        return new Point2D.Double(x, y);

    }
    
    
    
   
    
    private Vector2d calculateInitialDirection() {
        Point2D.Double pout1 = raceTrack.getPointList().get(1).getPout();
        Point2D.Double pout2 = raceTrack.getPointList().get(2).getPout();
        
        
        
        double x = pout2.getX()-pout1.getX();
        double y = pout2.getY()-pout1.getY();
        
        Vector2d vector = new Vector2d(x, y);
        return vector;
        
        
    }
    
    
    /**
     * NOTIFICA NASCITA ALL'AGENTE CAR. Invia la notifica di avvenuta gestione
     * dell'activity DO_BIRTH e manda l'activity CAR_INITIAL_POSITION_VIEW,
     * necessaria alla macchina per assumere una posizione iniziale sulla pista.
     *
     * @param task
     * @param payload
     */
    private void sendInitialPosition(NotifyMessage nmsg, Object login, String payload) {
        nmsg.addNext(CAR_INITIAL_POSITION_VIEW, payload, (String)login);
        showMsg("Ho inviato l'activity CAR_INITIAL_POSITION_VIEW " + payload + "a: " + login);
        conn.send(nmsg);
        checkStatus(nmsg, true);
    }
    /**
     * SEND START GAME.
     * Si occupa di inviare l'activity di inizio gioco a tutte le macchine .
     * Dopo averla inviata setta il flagStartRace, in questo modo il supervisor
     * capisce che la gara è iniziata
     */
    private void sendStartGame() {
        NotifyMessage nmsg = new NotifyMessage();
        nmsg.addNext(CAR_START_RACE, null, "ALL");
        conn.send(nmsg);
        flagStartRace = true;
        flagFirstActivityMovement = true;
    }

    private void startThreadSendCarPosition() {
        SendCarPosition.setPreviousTime(System.currentTimeMillis());
        sendCarPosition.start();
    }

    private void startThreadCalculateCarPosition() {
        CalculateCarPosition.setPreviousTime(System.currentTimeMillis());
        calculateCarPosition.start();
    }

    /**
     * UPDATE RACETRACK. Metodo richiamato dal thread che aggiorna la grafica
     * ogni 0.5 secondi (RaceTrackUpdate). Itera sulla mappa di macchine
     * presenti e fornisce alla pista le informazioni relative a ciascuna di
     * esse, in modo che la grafica possa aggiornarsi e renderle visibili.
     */
    public void updateRaceTrack() {
        ArrayList<CarPerception> listCarsRaceTrack = raceTrack.getCarsList();
        Set<String> keySet = getMyCars().keySet();
        Iterator it = keySet.iterator();

        for (int i = 0; it.hasNext(); i++) {
            //System.out.println(myCar.getLastPosition().x + " " );
            
            CarPerception myCar =  getMyCars().get((String)it.next());
            Point2D.Double newPosition = myCar.getLastPosition();
            listCarsRaceTrack.get(i).setLastPosition(newPosition);
            
        }
        raceTrack.drawBoard();
    }

    /**
     * APPLICAZIONE DELLE LEGGI FISICHE. Questo metodo viene richiamato dal
     * thread che aggiorna le posizioni delle macchine ogni 0.1 secondi
     * (CalculateCarPosition). Scorre sul vettore di CarPerception e per ognuna,
     * calcolo la velocità e la posizione raggiunta grazie ad essa. Velocità e
     * posizione verranno salvate e utilizzate all'iterazione successiva.
     */
    public void physicalLaws() {

        Set list = getMyCars().keySet();
        Iterator iter = list.iterator();
        String lastActivity;
        String lastActivityPayload;
        // Finchè ci sono chiavi, scorro la mappa
        while (iter.hasNext()) {
            Object key = iter.next();

            CarPerception myCarPerception = getMyCars().get((String)key);

            if (myCarPerception.getLastRequest() != null) {
                lastActivity = myCarPerception.getLastRequest().getProperty("Activity");
                lastActivityPayload = myCarPerception.getLastRequest().getProperty("Payload");
            

                speedUpdate(myCarPerception, lastActivity, lastActivityPayload);
                positionUpdate(myCarPerception);
            }
            
        }
    }

    /**
     * CALCOLO DELLA VELOCITA'. Tre casi: 1. L'ultima richiesta della macchina
     * era di accelerazione. 2. L'ultima richiesta era di frenata. 3. Non ci
     * sono richieste recenti.
     */
    
    
    private void speedUpdate(CarPerception car, String lastActivity, String lastActivityPayload) {
    
 
        
        if(lastActivity != null){
      
            
            if (lastActivity.equals(SUP_ACCELERATION)) {
                System.out.println("SONO IN ACCELERATION");
                /*
                Se prima di accelerare stavo sterzando, dovrò prima ritrovare la direzione verticale o orizzontale
                Questa cosa non andrà bene per i tratti inclinati
                */

                if (lastActivitySteering == true) {
                    System.out.println("Ultima attività di steering");
                    Point2D.Double ultimoPout = findCarView(car).get(findCarView(car).size() - 1).getPout();
                    Point2D.Double palettoPouttracuimitrovo = findCarView(car).get(0).getPout();
                    //System.out.println(" " + ultimoPout + palettoPouttracuimitrovo);
                    double dx = ultimoPout.getX() - palettoPouttracuimitrovo.getX();
                    double dy = ultimoPout.getY() - palettoPouttracuimitrovo.getY();

                    if (dx == 0) {
                        /*
                    Setto a 0 la coordinata x dell'ultima velocita
                         */
                        car.setLastVelocityX(0);
                        System.out.println(" ULTIMA VELOCITA " + car.getLastVelocity());
                    }
                    if (dy == 0) {
                        /*
                    Setto a 0 a coordinata y dell'ultima velocita
                         */
                        car.setLastVelocityY(0);
                        System.out.println(" ULTIMA VELOCITA " + car.getLastVelocity());
                    }

                }

                lastActivitySteering = false;
                /*
                         * Se l'ultima attività di CAR è stata "Accelera"
                         * Devo continuare ad incrementare la velocità del fattore di accelerazione
                         * Magari potrei mettere un controllo che non sia stata raggiunta la velocità
                         * massima della macchina
                 */

                    lastAcceleration = createAccelerationBrakingVector(car, lastActivityPayload);
                    
                    System.out.println(" Grado di accelerazione " + lastActivityPayload);
                    
                     /*
                    Se non è ancora stata raggiunta la massima velocità, creo il vettore accelerazione. 
                    Altrimenti tengo la velocità precedente.
                     */
                    
                   if (((Math.abs(car.getLastVelocity().x) < Math.abs(car.getMaxVelocity().x))
                        && (Math.abs(car.getLastVelocity().y) < Math.abs(car.getMaxVelocity().y)))) {

                    System.out.println("Velocità prima di somma acc" + car.getLastVelocity());
                    
                    //car.getLastVelocity().add(lastAcceleration);
      
                    System.out.println("Accelerazione calcolata: " + lastAcceleration);
                    car.getLastVelocity().add(lastAcceleration);
 
                    
                    System.out.println(" Velocita dopo aggiunta di accelerazione" + car.getLastVelocity());
                    

                    System.out.println("\n");
                } else // altrimenti la fisso pari alla massima velocità raggiungibile
                {
                    fixMaxVelocity(car);

                }

                lastActivityAcceleration = true;
                //car.svuotaRequest();
            } 
            
        else 
            
            if (lastActivity.equals(SUP_BRAKING)) {
            System.out.println("SONO IN BRAKING");
            lastActivitySteering = false;
            lastActivityAcceleration = false;
            brakingCar(car, lastActivityPayload);

        } 
         
        else 
            
            if (lastActivity.equals(SUP_STEERING)) {
                System.out.println("SONO IN STEERING");
                steeringCar(car, lastActivityPayload);
            

            // Per prima cosa frena
            //brakingCar(myCar, payloadSplit[1]);
            
        }
        else 
            if(lastActivity.equals(SUP_GO_ON)){
            lastActivitySteering = false;
            lastActivityAcceleration = false;
            // Altrimenti, lascio la stessa velocità di prima
            System.out.println("SONO IN GO ON");
            car.setLastVelocity(car.getLastVelocity());
            
        }
        // se non c'è nessuna activity rece
        }
        else 
        {
            // Altrimenti, lascio la stessa velocità di prima
            //car.setLastVelocity(car.getLastVelocity());
            
        }

    }

    private String[] splitPayload(String payload, String delim, int dim) {

        String split[] = new String[dim];
        StringTokenizer st = new StringTokenizer(payload, delim);
        int i = 0;

        while (st.hasMoreTokens()) {
            split[i] = st.nextToken();
            i++;
        }
        return split;
    }

    private void fixMaxVelocity(CarPerception car) {

        if (car.getLastVelocity().y == 0 && car.getLastVelocity().x != 0) {
            if (car.getLastVelocity().x > 0) {
                car.setLastVelocityX(car.getMaxVelocity().x);
            } else {
                car.setLastVelocityX(-car.getMaxVelocity().x);
            }
        }

        if (car.getLastVelocity().x == 0 && car.getLastVelocity().y != 0) {
            if (car.getLastVelocity().y > 0) {
                car.setLastVelocityY(car.getMaxVelocity().y);
            } else {
                car.setLastVelocityY(-car.getMaxVelocity().y);
            }
        }

        if (car.getLastVelocity().x != 0 && car.getLastVelocity().y != 0) {
            if (car.getLastVelocity().x > 0) {
                car.setLastVelocityX(car.getMaxVelocity().x);
            } else {
                car.setLastVelocityX(-car.getMaxVelocity().x);
            }
            if (car.getLastVelocity().y > 0) {
                car.setLastVelocityY(car.getMaxVelocity().y);
            } else {
                car.setLastVelocityY(-car.getMaxVelocity().y);
            }

        }

    }
    
    
    private void brakingCar(CarPerception car, String lastActivityPayload) {
        /*
         * Se l'ultima attività di CAR è stata "Frena"
         * Devo continuare a decrementare la velocità del fattore di accelerazione
         * Magari potrei mettere un controllo che non sia stata raggiunta la velocità 0.
         */
        Vector2d brakingVector = createAccelerationBrakingVector(car,lastActivityPayload);
        car.getLastVelocity().sub(brakingVector);
         
    }

    
    private void steeringCar(CarPerception car, String lastActivityPayload){
                
                double angleDifference;
                double rotationAngle=0;
                boolean angoloPrecOttuso = false;
                String[] payloadSplit = splitPayload(lastActivityPayload, ";", 2);
                double angle = Double.parseDouble(payloadSplit[0]);
                
               
                System.out.println("ANGOLO" + angle);
                
                
                if(angle < 0 && (Math.abs(angle) < (Math.PI/4)))
                {   System.out.println("1"  + "\n");
                    //rotationAngle = Math.abs(Math.PI/2  - Math.abs(angle));
                     //rotationAngle = Math.abs(angle);
                      rotationAngle = Math.abs((Math.PI)/2-Math.abs(angle));
                    
                }
                
                
                if(angle < 0 && (Math.abs(angle) < Math.PI/2) && (Math.abs(angle) > (Math.PI/4)) )
                {
                    rotationAngle = Math.abs((Math.PI)/2-Math.abs(angle));
                    System.out.println("    Rotation angle " +  rotationAngle);
                    System.out.println(" 2 "+ "\n");
                }
                
                if(angle > 0 && (Math.abs(angle) <  Math.PI/2))
                {
                    System.out.println(" 3");
                    //rotationAngle = Math.abs((Math.PI)/2-Math.abs(angle));
                    rotationAngle = Math.abs(angle);
                    //rotationAngle = Math.abs((Math.PI)/2-Math.abs(angle));
                    
                }
                
                
                if(angle > 0 && (Math.abs(angle) > Math.PI/2))
                {
                    System.out.println("4");
                      rotationAngle = Math.abs(Math.PI/2 - Math.abs(Math.PI - angle));
                      //rotationAngle = Math.abs(angle);
                      //rotationAngle = Math.abs(Math.PI - Math.abs(angle));
                     //rotationAngle = Math.abs(Math.PI/2 - Math.abs(angle));
                }
                
                
                if(angle < 0 && (Math.abs(angle) > Math.PI/2))
                {
                    System.out.println("5");
                    rotationAngle = Math.abs(Math.PI - Math.abs(angle));
                }
//                
                 
              // Per prima cosa frena
              String gradoBraking = payloadSplit[1];
              if(Double.parseDouble(gradoBraking) != 0 )
                  
              {
                System.out.println("Grado di frenatura " + gradoBraking);
                  System.out.println("Velocità prima della frenatura " + car.getLastVelocity());
                brakingCar(car,gradoBraking);
                gradoBraking = ""+0;
                
              }
              
                System.out.println(" Velocità dopo frenatura " + car.getLastVelocity());
            
            // se l'ultima activity prima di questa era sterza:
            // Calcolo l'angolo usando l'angolo precedente
            // altrimenti angolo precedente è 0
            // per ora lo metto come attributo del Supervisor poi ci penso
                System.out.println(" ULTIMA ATTIVITA ERA  DI STEERING: " + lastActivitySteering);
             
                
                // DIVIDO ANGOLO PER NUMERO DI VOLTE IN CUI RICHIAMO ROTAZIONE
                //rotationAngle = rotationAngle/30;
                
                
            if(lastActivitySteering == true){
                
                //angoloPrecOttuso = (Math.abs(AngoloPrecedente) > Math.PI/2);
                     
                     if(AngoloPrecedente > rotationAngle && AngoloPrecedente > Math.PI/4)
                         AngoloPrecedente = 0;
                  
                     
                    //angleDifference = angle-AngoloPrecedente;
                    angleDifference = Math.abs(rotationAngle-AngoloPrecedente);
                   
//                    System.out.println("\n" + " Ultima velocita = " +
//                                        "(  " +   car.getLastVelocity().x  + " ; " + car.getLastVelocity().y + "  )" + "\n");
                     car.setLastVelocity(rotateVector(angleDifference, car.getLastVelocity()));
                     //car.setLastVelocity(rotateVector(rotationAngle, car.getLastVelocity()));
                     //car.setLastVelocity(rotateVector(-rotationAngle, car.getLastVelocity()));
                     System.out.println("\n" + "------- STO RUOTANDO DI: ANGOLO-PRECEDENTE = " + rotationAngle + " - "  + AngoloPrecedente + " = " + angleDifference);
//                    System.out.println("\n" + "VELOCITA RUOTATA = " + "\n" +
//                    "(  " +   car.getLastVelocity().x  + " ; " + car.getLastVelocity().y + "  )" + "\n");  
                      AngoloPrecedente = rotationAngle;
                      
                      if(angoloPrecOttuso)
                         AngoloPrecedente = 0;
         
            }
            
            else 
            {   AngoloPrecedente = 0;
//                System.out.println("\n" + " Ultima velocita = " +
//                                        "(  " +   car.getLastVelocity().x  + " ; " + car.getLastVelocity().y + "  )" + "\n");
              
                
                car.setLastVelocity(rotateVector(rotationAngle, car.getLastVelocity()));
                System.out.println("\n" + "--------- STO RUOTANDO DI ANGOLO: " + rotationAngle);
                
                System.out.println("\n" + "VELOCITA RUOTATA = " + "\n" +
                  "(  " +   car.getLastVelocity().x  + " ; " + car.getLastVelocity().y + "  )" + "\n");  
               
               
                AngoloPrecedente = rotationAngle;
            }
            
            car.svuotaRequest();

            lastActivitySteering = true;
            
    
    }
    
    
    public Vector2d rotateVector(double angle, Vector2d vec) {
        Vector2d rotateVector;
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        double nx = vec.x * c - vec.y * s;
        double ny = vec.x * s + vec.y * c;
        rotateVector = new Vector2d(nx, ny);
        return rotateVector;
    }

    /**
     * CREAZIONE DEL VETTORE ACCELERAZIONE/FRENATURA. Prendo il vettore della
     * velocità con cui la macchina si stava muovendo all'istante precedente e
     * lo normalizzo (prendo il versore). In questo modo, posso ricavarmi un
     * vettore accelerazione o frenatura che sia diretto nella stessa direzione
     * della velocità, ma assuma un valore pari al grado che è stato selezionato
     * nella richiesta di accelerazione o frenatura. Per diversificare le due
     * azioni mi basterà sommarlo o sottrarlo successivamente.
     */
   
    private Vector2d createAccelerationBrakingVector(CarPerception myCarPerception, String lastActivityPayload) {
        Vector2d lastAccBrak;

        if (myCarPerception.getLastVelocity().x != 0 || myCarPerception.getLastVelocity().y != 0) {
            lastAccBrak = new Vector2d(myCarPerception.getLastVelocity().x,myCarPerception.getLastVelocity().y);
        } else {
            lastAccBrak = calculateInitialDirection();
        }

        //moltiplico per 3 perchè considero un massimo di 3 secondi
        // divido per 30 considerando che ogni 1 secondo il ciclo compie 10 iterazioni
  
        double maxAccelerationValue = (3* (myCarPerception.getMaxVelocity().x)/Double.parseDouble(lastActivityPayload))/30;
        // System.out.println(" CREO ACCEL: Ultima velocita " + myCarPerception.getLastVelocity().x + " " + myCarPerception.getLastVelocity().y);
       
        
        lastAccBrak.normalize();
        // System.out.println("Vettore acelerazione normalizzato " + lastAccBrak);
        // System.out.println("CREO ACCEL: Accelerazione normalizzata : " + lastAccBrak.x + " " + lastAccBrak.y);
        
        lastAccBrak.scale(maxAccelerationValue);
        System.out.println(" Last acceleration Braking Vector" + lastAccBrak);
        // System.out.println(" CREO ACCEL: SCALATO : " + lastAccBrak.x + " " + lastAccBrak.y);
        return lastAccBrak;
    }

    /**
     * CALCOLO DELLA POSIZIONE RAGGIUNTA. Per calcolare la posizione raggiunta
     * dalla macchina basterà sommare al vettore posizione il vettore velocità.
     *
     */
    private void positionUpdate(CarPerception car) {
        car.getLastPositionVector().add(car.getLastVelocity());
        Point2D.Double newPosition = new Point2D.Double(car.getLastPositionVector().x, car.getLastPositionVector().y);
        car.setLastPosition(newPosition);
    }


    public void addRaceTrack(RaceTrack raceTrack) {
        this.raceTrack = raceTrack;

    }

    public synchronized Map<String, CarPerception> getMyCars() {
        return myCars;
    }
    
    /**
     * START THREAD
     * Il metodo si occupa di far partire il thread per il calcolo delle posizioni 
     * e il thread per l'invio delle activity alle macchine 
     */
    private void startThread() {
        startThreadCalculateCarPosition();
        startThreadSendCarPosition();
    }

}



